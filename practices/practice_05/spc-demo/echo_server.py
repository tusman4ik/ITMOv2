from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import sys

class EchoHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length).decode('utf-8')
        print(f"\n[ECHO SERVER] Received POST {self.path}:", flush=True)
        try:
            parsed = json.loads(body)
            print(json.dumps(parsed, indent=2, ensure_ascii=False), flush=True)
            with open("/tmp/last_verdict.json", "w") as f:
                f.write(json.dumps(parsed, indent=2, ensure_ascii=False))
        except Exception as e:
            print(body, flush=True)

        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(b'{"status":"ok"}')

    def log_message(self, format, *args):
        # suppress default access logs to stdout to keep it clean
        pass

if __name__ == '__main__':
    port = 8090
    server = HTTPServer(('0.0.0.0', port), EchoHandler)
    print(f"[ECHO SERVER] Listening on port {port}...", flush=True)
    server.serve_forever()
