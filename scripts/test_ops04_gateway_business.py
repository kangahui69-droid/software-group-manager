#!/usr/bin/env python3
"""Exercise the gateway business smoke script against a disposable HTTPS server."""

import http.server
import json
import os
from pathlib import Path
import shutil
import ssl
import subprocess
import tempfile
import threading
from urllib.parse import parse_qs, urlsplit


ROOT = Path(__file__).resolve().parents[1]
GIT_BASH = Path(r"C:\Program Files\Git\bin\bash.exe")
GIT_OPENSSL = Path(r"C:\Program Files\Git\usr\bin\openssl.exe")
BASH = str(GIT_BASH) if os.name == "nt" and GIT_BASH.exists() else shutil.which("bash")
OPENSSL = str(GIT_OPENSSL) if os.name == "nt" and GIT_OPENSSL.exists() else shutil.which("openssl")


class Handler(http.server.BaseHTTPRequestHandler):
    def log_message(self, *_args):
        pass

    def do_GET(self):
        path = urlsplit(self.path).path
        if path == "/software-group/login.jsp":
            self.send_response(200)
            self.send_header("Set-Cookie", "JSESSIONID=initial; Secure; HttpOnly; Path=/")
            self.end_headers()
            self.wfile.write(b'<meta name="csrf-token" content="test-csrf">')
        elif path == "/api/projects/my" and "JSESSIONID=logged-in" in self.headers.get("Cookie", ""):
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"code": 0, "data": {"items": []}}).encode())
        else:
            self.send_error(401)

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0"))
        form = parse_qs(self.rfile.read(length).decode())
        valid = (
            self.path == "/software-group/login"
            and form.get("username") == ["probe"]
            and form.get("password") == ["test-password"]
            and self.headers.get("X-CSRF-TOKEN") == "test-csrf"
            and "JSESSIONID=initial" in self.headers.get("Cookie", "")
        )
        if not valid:
            self.send_error(401)
            return
        self.send_response(302)
        self.send_header("Location", "/software-group/index.jsp")
        self.send_header("Set-Cookie", "JSESSIONID=logged-in; Secure; HttpOnly; Path=/")
        self.end_headers()


class TLSHTTPServer(http.server.HTTPServer):
    def shutdown_request(self, request):
        try:
            request.unwrap()
        except (OSError, ssl.SSLError):
            pass
        super().shutdown_request(request)


def main():
    (ROOT / "target").mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="ops04-", dir=ROOT / "target") as tmp:
        tmp = Path(tmp)
        cert = tmp / "cert.pem"
        key = tmp / "key.pem"
        subprocess.run(
            [OPENSSL, "req", "-x509", "-newkey", "rsa:2048", "-nodes", "-days", "1",
             "-keyout", str(key), "-out", str(cert), "-subj", "/CN=127.0.0.1",
             "-addext", "subjectAltName=IP:127.0.0.1"],
            check=True, capture_output=True, env={**os.environ, "MSYS_NO_PATHCONV": "1"},
        )
        server = TLSHTTPServer(("127.0.0.1", 0), Handler)
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.load_cert_chain(cert, key)
        server.socket = context.wrap_socket(server.socket, server_side=True)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            env = {**os.environ, "BASE_URL": f"https://127.0.0.1:{server.server_port}",
                   "CURL_CA_BUNDLE": str(cert), "SMOKE_USERNAME": "probe",
                   "SMOKE_PASSWORD": "test-password", "PYTHON_BIN": "python" if os.name == "nt" else "python3"}
            result = subprocess.run(
                [BASH, "scripts/verify-gateway-business.sh"], cwd=ROOT, env=env,
                text=True, errors="replace", capture_output=True, timeout=25,
            )
            assert result.returncode == 0, result.stderr
            assert "Gateway business smoke PASS" in result.stdout
            env["SMOKE_PASSWORD"] = "wrong-password"
            denied = subprocess.run(
                [BASH, "scripts/verify-gateway-business.sh"], cwd=ROOT, env=env,
                text=True, errors="replace", capture_output=True, timeout=25,
            )
            assert denied.returncode != 0
            assert "wrong-password" not in denied.stdout + denied.stderr
            print("OPS04_GATEWAY_SMOKE_TEST_PASS: HTTPS login, CSRF, cookie, project read, bad login")
        finally:
            server.shutdown()
            thread.join(timeout=5)
            server.server_close()


if __name__ == "__main__":
    main()
