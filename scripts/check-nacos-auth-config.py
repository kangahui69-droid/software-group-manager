#!/usr/bin/env python3
"""Check Nacos 2.2.3 auth inputs without printing secret values."""

import argparse
import base64
import binascii
import json
import os
from pathlib import Path
import re
import subprocess


ROOT = Path(__file__).resolve().parents[1]
SERVICES = (
    "file-service", "user-service", "activity-service", "project-award-service",
    "hr-service", "monitor-service", "content-service", "ai-service",
)


def fail(message: str) -> None:
    raise SystemExit(f"Nacos auth check failed: {message}")


parser = argparse.ArgumentParser()
parser.add_argument("--env-file", type=Path, help="private Compose environment file")
args = parser.parse_args()

command = ["docker", "compose"]
env = os.environ.copy()
if args.env_file:
    command += ["--env-file", str(args.env_file.resolve())]
else:
    # Safe placeholders let CI check the checked-in Compose file without private secrets.
    for line in (ROOT / ".env.example").read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.startswith("#"):
            name = line.split("=", 1)[0]
            env[name] = (
                "/tmp/software-group-test-tls"
                if name == "TLS_CERT_DIR"
                else "test-only-placeholder-value-0123456789"
            )
    env["NACOS_AUTH_TOKEN"] = base64.b64encode(b"test-only-token-secret-at-least-32-bytes").decode("ascii")
    env["NACOS_AUTH_IDENTITY_KEY"] = "test-only-identity-key"
    env["NACOS_AUTH_IDENTITY_VALUE"] = "test-only-identity-value-at-least-32-bytes"
    env["NACOS_USERNAME"] = "sg_nacos_client"
    env["NACOS_PASSWORD"] = "test-only-client-password-at-least-32-bytes"

result = subprocess.run(
    command + ["config", "--format", "json"],
    cwd=ROOT,
    env=env,
    text=True,
    capture_output=True,
    check=False,
)
if result.returncode:
    fail("Compose configuration or required variables are invalid")

services = json.loads(result.stdout)["services"]
server = services["nacos"]["environment"]
if str(server.get("NACOS_AUTH_ENABLE", "")).lower() != "true":
    fail("NACOS_AUTH_ENABLE must be true")

token = server.get("NACOS_AUTH_TOKEN", "")
try:
    decoded_token = base64.b64decode(token, validate=True)
except (binascii.Error, ValueError):
    fail("NACOS_AUTH_TOKEN must be valid Base64")
if len(decoded_token) < 32 or token == "SecretKey012345678901234567890123456789012345678901234567890123456789":
    fail("NACOS_AUTH_TOKEN must be a unique secret of at least 32 decoded bytes")

identity_key = server.get("NACOS_AUTH_IDENTITY_KEY", "")
identity_value = server.get("NACOS_AUTH_IDENTITY_VALUE", "")
if len(identity_key) < 16 or identity_key == "serverIdentity":
    fail("NACOS_AUTH_IDENTITY_KEY must be deployment-specific")
if len(identity_value) < 32 or identity_value == "security" or identity_value == identity_key:
    fail("NACOS_AUTH_IDENTITY_VALUE must be deployment-specific")

expected_user = services[SERVICES[0]]["environment"].get("SPRING_CLOUD_NACOS_DISCOVERY_USERNAME", "")
expected_password = services[SERVICES[0]]["environment"].get("SPRING_CLOUD_NACOS_DISCOVERY_PASSWORD", "")
if not re.fullmatch(r"[A-Za-z0-9_-]{3,64}", expected_user) or expected_user.lower() == "nacos":
    fail("the client must use a dedicated non-default Nacos user")
if not re.fullmatch(r"[A-Za-z0-9_-]{32,128}", expected_password) or expected_password == expected_user:
    fail("the client password must be 32-128 letters, digits, _ or -")
if args.env_file and (
    b"test-only" in decoded_token
    or any("test-only" in value for value in (identity_key, identity_value, expected_password))
):
    fail("test placeholders are not valid production credentials")
for name in SERVICES:
    client = services[name]["environment"]
    if client.get("SPRING_CLOUD_NACOS_DISCOVERY_USERNAME") != expected_user:
        fail(f"{name} has a different or missing Nacos username")
    if client.get("SPRING_CLOUD_NACOS_DISCOVERY_PASSWORD") != expected_password:
        fail(f"{name} has a different or missing Nacos password")

for resource in ROOT.glob("*-service/src/main/resources/*.yml"):
    source = resource.read_text(encoding="utf-8")
    if "NACOS_USERNAME:nacos" in source or "NACOS_PASSWORD:nacos" in source:
        fail(f"{resource.parents[3].name} still contains default Nacos credentials")

print("OPS-02 PASS: Nacos auth and eight discovery clients have complete private credentials")
