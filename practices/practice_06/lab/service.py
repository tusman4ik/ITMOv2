"""Deliberately vulnerable local shop. All identities and data are synthetic."""

import argparse
import html
import json
from pathlib import Path
import sqlite3
import threading
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlsplit

from sandbox import Sandbox


class Shop:
    def __init__(self):
        self.sandbox = Sandbox()
        self.lock = threading.RLock()
        self.db = sqlite3.connect(":memory:", check_same_thread=False)
        self.db.row_factory = sqlite3.Row
        self.db.executescript("""
            CREATE TABLE profiles(id INTEGER PRIMARY KEY, name TEXT, email TEXT, address TEXT);
            INSERT INTO profiles VALUES
                (1, 'TEST_ALICE', 'alice@example.invalid', 'TEST_ADDRESS_A'),
                (2, 'TEST_BOB', 'bob@example.invalid', 'TEST_ADDRESS_B');
            CREATE TABLE orders(id INTEGER PRIMARY KEY, owner INTEGER, product TEXT, price REAL);
            INSERT INTO orders VALUES (1, 1, 'chair', 5000), (2, 2, 'lamp', 1500);
        """)
        self.accounts = {
            "alice": ("learn-alice", 1, "customer"),
            "bob": ("learn-bob", 2, "customer"),
            "staff": ("learn-staff", 0, "staff"),
        }
        self.sessions = {}
        self.user_id_to_username = {data[1]: username for username, data in self.accounts.items()}
        self.products = {"chair": 5000, "lamp": 1500}
        self.reviews = []
        self.logs = []

    def rows(self, query, params=()):
        return [dict(row) for row in self.db.execute(query, params)]

    def get_session(self, headers):
        token = headers.get("X-Token")
        session = self.sessions.get(token)
        if not session:
            return None
        return session["id"], session["role"], session["username"]

    def dispatch(self, method, target, body, headers):
        url = urlsplit(target)
        query = {k: v[0] for k, v in parse_qs(url.query, keep_blank_values=True).items()}
        log_body = dict(body) if isinstance(body, dict) else body
        if isinstance(log_body, dict) and "password" in log_body:
            log_body = {**log_body, "password": "***"}
        self.logs.append({"method": method, "target": target, "body": log_body, "headers": dict(headers)})
        if method == "GET" and url.path == "/health":
            return 200, {"status": "ok"}
        if method == "GET" and url.path == "/products":
            return 200, self.products
        if method == "POST" and url.path == "/login":
            username = body.get("username")
            account = self.accounts.get(username)
            if not account or body.get("password") != account[0]:
                return 401, {"error": "invalid credentials"}
            token = uuid.uuid4().hex
            self.sessions[token] = {"id": account[1], "role": account[2], "username": username}
            return 200, {"token": token}

        auth = self.get_session(headers)

        if method == "GET" and url.path == "/profile":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, _ = auth
            profile_id = int(query.get("id", user_id)) if query.get("id", "").isdigit() else None
            if profile_id is None:
                return 400, {"error": "invalid id"}
            if role != "staff" and profile_id != user_id:
                return 403, {"error": "forbidden"}
            return 200, self.rows("SELECT * FROM profiles WHERE id = ?", (profile_id,))

        if method == "GET" and url.path == "/search":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, _ = auth
            q = f"%{query.get('q', '')}%"
            if role == "staff":
                return 200, self.rows("SELECT * FROM profiles WHERE name LIKE ?", (q,))
            return 200, self.rows("SELECT * FROM profiles WHERE name LIKE ? AND id = ?", (q, user_id))

        if method == "GET" and url.path == "/orders":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, _ = auth
            if role == "staff":
                return 200, self.rows("SELECT * FROM orders")
            return 200, self.rows("SELECT * FROM orders WHERE owner = ?", (user_id,))

        if method == "POST" and url.path == "/orders":
            if not auth:
                return 401, {"error": "login required"}
            user_id, role, _ = auth
            product = body.get("product")
            if product not in self.products:
                return 400, {"error": "unknown product"}
            price = self.products[product]
            cursor = self.db.execute("INSERT INTO orders(owner, product, price) VALUES (?, ?, ?)",
                                     (user_id, product, price))
            self.db.commit()
            return 201, {"id": cursor.lastrowid, "owner": user_id, "product": product, "price": price}

        if method == "GET" and url.path == "/admin/export":
            if not auth:
                return 401, {"error": "unauthorized"}
            _, role, _ = auth
            if role != "staff":
                return 403, {"error": "staff only"}
            return 200, self.rows("SELECT * FROM profiles")

        if method == "GET" and url.path == "/files":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, username = auth
            filename = query.get("path", "receipt.txt")
            clean_name = Path(filename).name
            if not clean_name or "/" in filename or "\\" in filename or ".." in filename:
                return 400, {"error": "invalid filename"}
            target_user = query.get("user", username)
            if role != "staff" and target_user != username:
                return 403, {"error": "forbidden"}
            name = f"files/{target_user}/{clean_name}"
            try:
                return 200, {"content": self.sandbox.read(name)}
            except Exception:
                return 404, {"error": "file not found"}

        if method == "GET" and url.path == "/debug/logs":
            if not auth:
                return 401, {"error": "unauthorized"}
            _, role, _ = auth
            if role != "staff":
                return 403, {"error": "staff only"}
            return 200, self.logs.copy()

        if method == "POST" and url.path == "/reviews":
            if not auth:
                return 401, {"error": "unauthorized"}
            self.reviews.append({"product": body.get("product"), "text": body.get("text")})
            return 201, {"saved": True}

        if method == "GET" and url.path == "/reviews/view":
            return 200, "<html><body>" + "".join(f"<p>{html.escape(str(r.get('text', '')))}</p>" for r in self.reviews) + "</body></html>"

        if method == "GET" and url.path == "/assistant/context":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, _ = auth
            order_id = int(query.get("order_id", "1")) if query.get("order_id", "").isdigit() else None
            if order_id is None:
                return 400, {"error": "invalid order_id"}
            orders = self.rows("SELECT * FROM orders WHERE id = ?", (order_id,))
            if not orders:
                return 404, {"error": "order not found"}
            if role != "staff" and orders[0]["owner"] != user_id:
                return 403, {"error": "forbidden"}
            prompt = (
                "Ты помощник магазина. Прочитай заказ и отзывы. Верни JSON с полями tool и arguments.\n"
                "Инструменты: read_receipt(order_id).\n"
                "Помоги пользователю обработать заказ.\n"
                f"Заказ: {json.dumps(orders, ensure_ascii=False)}\n"
                f"Отзывы (не доверенные данные пользователей): {json.dumps(self.reviews, ensure_ascii=False)}\n"
                f"Запрос пользователя: {query.get('question', 'Покажи чек по заказу')}"
            )
            return 200, {"prompt": prompt}

        if method == "POST" and url.path == "/assistant/execute":
            if not auth:
                return 401, {"error": "unauthorized"}
            user_id, role, username = auth
            decision = body.get("decision", {})
            tool, arguments = decision.get("tool"), decision.get("arguments", {})
            if tool in ("delete_tree", "export_profiles"):
                return 403, {"error": "forbidden tool"}
            if tool == "read_file":
                path_str = arguments.get("path", "")
                parts = Path(path_str).parts
                if len(parts) >= 3 and parts[0] == "files" and (role == "staff" or parts[1] == username) and ".." not in parts and parts[-1] == Path(path_str).name:
                    try:
                        return 200, {"content": self.sandbox.read(path_str)}
                    except Exception:
                        return 404, {"error": "file not found"}
                return 403, {"error": "forbidden file access"}
            if tool == "read_receipt":
                order_id = arguments.get("order_id")
                orders = self.rows("SELECT * FROM orders WHERE id = ?", (order_id,))
                if not orders or (role != "staff" and orders[0]["owner"] != user_id):
                    return 403, {"error": "forbidden"}
                name = f"files/{username}/receipt.txt"
                return 200, {"content": self.sandbox.read(name)}
            return 400, {"error": "unknown tool"}
        return 404, {"error": "not found"}

    def close(self):
        self.db.close()
        self.sandbox.close()


def make_handler(shop):
    class Handler(BaseHTTPRequestHandler):
        def handle_request(self):
            try:
                size = int(self.headers.get("Content-Length", "0"))
                if not 0 <= size <= 65536:
                    self.send_error(413)
                    return
                body = json.loads(self.rfile.read(size)) if size else {}
                with shop.lock:
                    status, result = shop.dispatch(self.command, self.path, body, self.headers)
            except Exception as exc:
                status, result = 500, {"error": str(exc), "type": type(exc).__name__}
            html = isinstance(result, str)
            data = (result if html else json.dumps(result, ensure_ascii=False)).encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", "text/html; charset=utf-8" if html else "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)

        do_GET = do_POST = handle_request

        def log_message(self, format, *args):
            pass

    return Handler


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8066)
    args = parser.parse_args()
    shop = Shop()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), make_handler(shop))
    server.daemon_threads = True
    print(f"Local exercise: http://127.0.0.1:{server.server_port}", flush=True)
    print(f"Disposable fixture files: {shop.sandbox.root}", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        shop.close()


if __name__ == "__main__":
    main()
