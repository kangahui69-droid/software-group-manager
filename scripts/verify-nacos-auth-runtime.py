#!/usr/bin/env python3
"""Read-only Nacos 2.x authentication and registration check on the Ubuntu VM."""

import json
from pathlib import Path
import subprocess
import urllib.error
import urllib.parse
import urllib.request


EXPECTED_SERVICES = {
    "file-service", "user-service", "activity-service", "project-award-service",
    "hr-service", "monitor-service", "content-service", "ai-service",
}
ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    raise SystemExit(f"OPS-02 FAIL: {message}")


inspect = subprocess.run(
    ["docker", "inspect", "sg_nacos"], text=True, capture_output=True, check=False
)
if inspect.returncode:
    fail("Nacos container is unavailable")
container = json.loads(inspect.stdout)[0]
if container.get("State", {}).get("Health", {}).get("Status") != "healthy":
    fail("Nacos container is not healthy")
networks = container.get("NetworkSettings", {}).get("Networks", {})
addresses = [network.get("IPAddress") for network in networks.values() if network.get("IPAddress")]
if len(addresses) != 1:
    fail("expected one Docker bridge address for Nacos")
base_url = f"http://{addresses[0]}:8848/nacos"
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def request(path: str, data: dict[str, str] | None = None) -> tuple[int, bytes]:
    body = urllib.parse.urlencode(data).encode() if data is not None else None
    req = urllib.request.Request(base_url + path, data=body)
    try:
        with opener.open(req, timeout=10) as response:
            return response.status, response.read()
    except urllib.error.HTTPError as error:
        return error.code, error.read()
    except urllib.error.URLError:
        fail("Nacos container is not reachable on its Docker bridge address")


status, payload = request("/v1/ns/service/list?pageNo=1&pageSize=100")
if status not in (401, 403):
    try:
        body = json.loads(payload)
    except (ValueError, TypeError):
        body = {}
    if body.get("code") not in (401, 403):
        fail("anonymous service-list request was not denied")
print("OPS-02 PASS: anonymous naming request is denied")

status, payload = request("/v1/auth/login", {"username": "nacos", "password": "nacos"})
try:
    default_login = json.loads(payload)
except (ValueError, TypeError):
    default_login = {}
if status == 200 and default_login.get("accessToken"):
    fail("default nacos/nacos administrator credential still works")
print("OPS-02 PASS: default administrator password is no longer accepted")

compose = subprocess.run(
    ["docker", "compose", "config", "--format", "json"],
    cwd=ROOT,
    text=True,
    capture_output=True,
    check=False,
)
if compose.returncode:
    fail("current Compose configuration is unavailable")
client_env = json.loads(compose.stdout)["services"]["file-service"]["environment"]
username = client_env.get("SPRING_CLOUD_NACOS_DISCOVERY_USERNAME", "")
password = client_env.get("SPRING_CLOUD_NACOS_DISCOVERY_PASSWORD", "")
if not username or not password or username.lower() == "nacos":
    fail("Compose does not provide a dedicated client account")
status, payload = request("/v1/auth/login", {"username": username, "password": password})
try:
    login = json.loads(payload)
except (ValueError, TypeError):
    login = {}
token = login.get("accessToken")
if status != 200 or not isinstance(token, str) or not token:
    fail("dedicated client login failed")
print("OPS-02 PASS: dedicated client login succeeds")

params = urllib.parse.urlencode({"pageNo": 1, "pageSize": 100, "accessToken": token})
status, payload = request("/v1/ns/service/list?" + params)
if status != 200:
    fail("authenticated service-list request failed")
try:
    listing = json.loads(payload)
except (ValueError, TypeError):
    fail("authenticated service-list response is not JSON")
names = {str(name).split("@@")[-1] for name in listing.get("doms", [])}
missing = EXPECTED_SERVICES - names
if missing:
    fail("missing registrations: " + ", ".join(sorted(missing)))
print("OPS-02 PASS: all eight services are registered with authenticated Nacos")
