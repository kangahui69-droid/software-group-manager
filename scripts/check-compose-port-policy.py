#!/usr/bin/env python3
"""Fail if a service other than Nginx publishes a host port."""

import json
import os
from pathlib import Path
import subprocess


ROOT = Path(__file__).resolve().parents[1]
env = os.environ.copy()
for line in (ROOT / ".env.example").read_text(encoding="utf-8").splitlines():
    if "=" in line and not line.startswith("#"):
        name = line.split("=", 1)[0]
        if not env.get(name):
            env[name] = (
                "/tmp/software-group-test-tls"
                if name == "TLS_CERT_DIR"
                else "test-only-placeholder-value-0123456789"
            )

result = subprocess.run(
    ["docker", "compose", "config", "--format", "json"],
    cwd=ROOT,
    env=env,
    text=True,
    capture_output=True,
    check=False,
)
if result.returncode:
    raise SystemExit(f"Compose config failed: {result.stderr.strip()}")

services = json.loads(result.stdout)["services"]
nginx_ports = services["nginx"].get("ports", [])
published = {
    (str(port.get("published")), str(port.get("target")), port.get("protocol", "tcp"))
    for port in nginx_ports
}
if len(nginx_ports) != 2 or published != {("80", "80", "tcp"), ("443", "443", "tcp")}:
    raise SystemExit(f"Nginx must publish only TCP 80 and 443: {nginx_ports}")

for name, service in services.items():
    if name == "nginx":
        continue
    if service.get("ports"):
        raise SystemExit(f"Internal service {name} publishes host ports: {service['ports']}")

networked = (
    "nginx", "app", "mysql", "redis", "rabbitmq", "nacos", "sentinel", "minio",
    "file-service", "user-service", "activity-service", "project-award-service",
    "hr-service", "monitor-service", "content-service", "ai-service",
)
for name in networked:
    if "software-group-net" not in services[name].get("networks", {}):
        raise SystemExit(f"Service {name} is missing the internal Docker network")

print("OPS-01/03 PASS: only Nginx publishes TCP 80 and 443; application services share the internal network")
