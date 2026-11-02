"""Functional contract and exercise containment; security regressions are student work."""

import unittest
from pathlib import Path
from tempfile import TemporaryDirectory

from sandbox import Sandbox
from service import Shop


class ShopTest(unittest.TestCase):
    def setUp(self):
        self.shop = Shop()
        self.addCleanup(self.shop.close)

    def request(self, method, path, body=None, headers=None):
        with self.shop.lock:
            return self.shop.dispatch(method, path, body or {}, headers or {})

    def login(self, username="alice", password="learn-alice"):
        status, result = self.request("POST", "/login", {"username": username, "password": password})
        self.assertEqual(status, 200)
        return {"X-Token": result["token"]}

    def test_health_and_catalog(self):
        self.assertEqual(self.request("GET", "/health"), (200, {"status": "ok"}))
        self.assertEqual(self.request("GET", "/products"), (200, {"chair": 5000, "lamp": 1500}))

    def test_wrong_password_rejected(self):
        self.assertEqual(self.request("POST", "/login", {"username": "alice", "password": "wrong"})[0], 401)

    def test_tokens_identify_distinct_users(self):
        alice, bob = self.login(), self.login("bob", "learn-bob")
        self.assertNotEqual(alice, bob)
        self.assertEqual(self.request("GET", "/profile?id=1", headers=alice)[1][0]["name"], "TEST_ALICE")
        self.assertEqual(self.request("GET", "/profile?id=2", headers=bob)[1][0]["name"], "TEST_BOB")

    def test_staff_can_export_with_staff_session(self):
        auth = {**self.login("staff", "learn-staff"), "X-Role": "staff"}
        status, profiles = self.request("GET", "/admin/export", headers=auth)
        self.assertEqual(status, 200)
        self.assertEqual(len(profiles), 2)

    def test_own_profile_and_receipt(self):
        auth = self.login()
        status, profile = self.request("GET", "/profile?id=1", headers=auth)
        self.assertEqual(status, 200)
        self.assertEqual(profile[0]["name"], "TEST_ALICE")
        status, receipt = self.request("GET", "/files?user=alice&path=receipt.txt", headers=auth)
        self.assertEqual(status, 200)
        self.assertIn("TEST_ALICE", receipt["content"])

    def test_customer_can_buy_at_catalog_price(self):
        status, order = self.request("POST", "/orders", {"product": "lamp"}, self.login())
        self.assertEqual(status, 201)
        self.assertEqual((order["owner"], order["price"]), (1, 1500))

    def test_unknown_product_rejected(self):
        self.assertEqual(self.request("POST", "/orders", {"product": "unknown"}, self.login())[0], 400)

    def test_review_can_be_submitted_and_viewed(self):
        auth = self.login()
        self.assertEqual(self.request("POST", "/reviews", {"product": "chair", "text": "Comfortable"}, auth)[0], 201)
        self.assertIn("Comfortable", self.request("GET", "/reviews/view", headers=auth)[1])

    def test_assistant_can_return_own_receipt(self):
        auth = self.login()
        status, context = self.request("GET", "/assistant/context?order_id=1", headers=auth)
        self.assertEqual(status, 200)
        self.assertIn("chair", context["prompt"])
        status, result = self.request("POST", "/assistant/execute", {
            "decision": {"tool": "read_file", "arguments": {"path": "files/alice/receipt.txt"}}
        }, auth)
        self.assertEqual(status, 200)
        self.assertIn("TEST_ALICE", result["content"])


class SandboxTest(unittest.TestCase):
    def setUp(self):
        self.sandbox = Sandbox()
        self.addCleanup(self.sandbox.close)

    def test_parent_paths_cannot_escape_exercise(self):
        for name in ("../../outside.txt", "/etc/passwd"):
            with self.subTest(name=name), self.assertRaises(ValueError):
                self.sandbox.read(name)

    def test_external_directory_cannot_be_deleted(self):
        with TemporaryDirectory() as directory:
            sentinel = Path(directory) / "sentinel.txt"
            sentinel.write_text("keep")
            with self.assertRaises(ValueError):
                self.sandbox.delete_tree(directory)
            self.assertEqual(sentinel.read_text(), "keep")

    def test_symlink_cannot_escape_exercise(self):
        with TemporaryDirectory() as directory:
            sentinel = Path(directory) / "sentinel.txt"
            sentinel.write_text("keep")
            link = self.sandbox.root / "escape"
            link.symlink_to(directory, target_is_directory=True)
            with self.assertRaises(ValueError):
                self.sandbox.read("escape/sentinel.txt")
            with self.assertRaises(ValueError):
                self.sandbox.delete_tree("escape")
            self.assertEqual(sentinel.read_text(), "keep")

    def test_delete_of_fixture_root_stays_inside_exercise(self):
        with TemporaryDirectory() as directory:
            sentinel = Path(directory) / "sentinel.txt"
            sentinel.write_text("keep")
            self.sandbox.delete_tree(".")
            self.assertFalse(self.sandbox.root.exists())
            self.assertEqual(sentinel.read_text(), "keep")


class SecurityTest(unittest.TestCase):
    def setUp(self):
        self.shop = Shop()
        self.addCleanup(self.shop.close)

    def request(self, method, path, body=None, headers=None):
        with self.shop.lock:
            return self.shop.dispatch(method, path, body or {}, headers or {})

    def login(self, username="alice", password="learn-alice"):
        status, result = self.request("POST", "/login", {"username": username, "password": password})
        self.assertEqual(status, 200)
        return {"X-Token": result["token"]}

    def test_unauthenticated_access_blocked(self):
        endpoints = [
            ("GET", "/profile"),
            ("GET", "/search"),
            ("GET", "/orders"),
            ("POST", "/orders", {"product": "chair"}),
            ("GET", "/admin/export"),
            ("GET", "/files"),
            ("GET", "/debug/logs"),
            ("POST", "/reviews", {"product": "chair", "text": "nice"}),
            ("GET", "/assistant/context"),
            ("POST", "/assistant/execute", {"decision": {"tool": "read_file", "arguments": {"path": "files/alice/receipt.txt"}}}),
        ]
        for item in endpoints:
            method, path = item[0], item[1]
            body = item[2] if len(item) > 2 else None
            with self.subTest(method=method, path=path):
                status, _ = self.request(method, path, body=body)
                self.assertEqual(status, 401)

    def test_sqli_blocked_in_profile_and_search(self):
        auth = self.login("alice", "learn-alice")
        # SQLi injection payload in /profile?id=
        status, res = self.request("GET", "/profile?id=1%20OR%201=1", headers=auth)
        self.assertEqual(status, 400)

        # SQLi injection in /search?q=
        status, res = self.request("GET", "/search?q=' OR '1'='1", headers=auth)
        self.assertEqual(status, 200)
        # As customer alice, should not leak bob's profile even with SQL injection attempt
        for row in res:
            self.assertEqual(row["name"], "TEST_ALICE")

    def test_idor_blocked_in_profile_and_files(self):
        alice = self.login("alice", "learn-alice")
        # Alice tries to view Bob's profile
        status, _ = self.request("GET", "/profile?id=2", headers=alice)
        self.assertEqual(status, 403)

        # Alice tries to view Bob's files
        status, _ = self.request("GET", "/files?user=bob&path=receipt.txt", headers=alice)
        self.assertEqual(status, 403)

    def test_role_spoofing_blocked_in_admin_export(self):
        alice = self.login("alice", "learn-alice")
        headers = {**alice, "X-Role": "staff"}
        status, _ = self.request("GET", "/admin/export", headers=headers)
        self.assertEqual(status, 403)

    def test_price_tampering_prevented_in_orders(self):
        alice = self.login("alice", "learn-alice")
        status, order = self.request("POST", "/orders", {"product": "chair", "price": 1}, headers=alice)
        self.assertEqual(status, 201)
        self.assertEqual(order["price"], 5000)

    def test_path_traversal_blocked_in_files(self):
        alice = self.login("alice", "learn-alice")
        for bad_path in ("../private/operator-note.txt", "/etc/passwd", "private/operator-note.txt", ".."):
            with self.subTest(path=bad_path):
                status, _ = self.request("GET", f"/files?path={bad_path}", headers=alice)
                self.assertIn(status, (400, 403, 404))

    def test_xss_prevented_in_reviews_view(self):
        alice = self.login("alice", "learn-alice")
        payload = "<script>alert(1)</script>"
        status, _ = self.request("POST", "/reviews", {"product": "chair", "text": payload}, headers=alice)
        self.assertEqual(status, 201)
        status, body = self.request("GET", "/reviews/view", headers=alice)
        self.assertEqual(status, 200)
        self.assertNotIn("<script>alert(1)</script>", body)
        self.assertIn("&lt;script&gt;alert(1)&lt;/script&gt;", body)

    def test_assistant_execute_blocks_forbidden_tools_and_unauthorized_files(self):
        alice = self.login("alice", "learn-alice")
        # Blocks delete_tree
        status, _ = self.request("POST", "/assistant/execute", {
            "decision": {"tool": "delete_tree", "arguments": {"path": "files/alice/receipt.txt"}}
        }, headers=alice)
        self.assertEqual(status, 403)

        # Blocks export_profiles
        status, _ = self.request("POST", "/assistant/execute", {
            "decision": {"tool": "export_profiles"}
        }, headers=alice)
        self.assertEqual(status, 403)

        # Blocks reading Bob's receipt
        status, _ = self.request("POST", "/assistant/execute", {
            "decision": {"tool": "read_file", "arguments": {"path": "files/bob/receipt.txt"}}
        }, headers=alice)
        self.assertEqual(status, 403)

        # Blocks path traversal to operator note
        status, _ = self.request("POST", "/assistant/execute", {
            "decision": {"tool": "read_file", "arguments": {"path": "private/operator-note.txt"}}
        }, headers=alice)
        self.assertEqual(status, 403)

    def test_debug_logs_blocked_for_customer_and_passwords_sanitized(self):
        alice = self.login("alice", "learn-alice")
        status, _ = self.request("GET", "/debug/logs", headers=alice)
        self.assertEqual(status, 403)

        staff = self.login("staff", "learn-staff")
        status, logs = self.request("GET", "/debug/logs", headers=staff)
        self.assertEqual(status, 200)
        for entry in logs:
            body = entry.get("body")
            if isinstance(body, dict) and "password" in body:
                self.assertNotEqual(body["password"], "learn-alice")
                self.assertNotEqual(body["password"], "learn-staff")
                self.assertEqual(body["password"], "***")


if __name__ == "__main__":
    unittest.main()
