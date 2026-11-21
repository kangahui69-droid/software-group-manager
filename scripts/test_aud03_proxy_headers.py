"""Check the proxy identity boundary in each Nginx route to the legacy WAR."""

import re
import unittest
from pathlib import Path


CONFIG = (Path(__file__).resolve().parents[1] / "nginx" / "nginx.conf").read_text(encoding="utf-8")


class AuditProxyHeadersTest(unittest.TestCase):
    def test_each_war_proxy_route_overwrites_identity_headers(self):
        locations = re.findall(r"location\s+[^{}]+\{([^{}]*)\}", CONFIG, re.DOTALL)
        proxy_routes = [block for block in locations if "proxy_pass http://tomcat_backend" in block]
        self.assertEqual(4, len(proxy_routes))
        for block in proxy_routes:
            self.assertIn("proxy_set_header X-Real-IP $remote_addr;", block)
            self.assertIn("proxy_set_header X-Forwarded-For $remote_addr;", block)
            self.assertIn("proxy_set_header Forwarded '';", block)
            self.assertIn("proxy_set_header Proxy-Client-IP '';", block)
            self.assertIn("proxy_set_header WL-Proxy-Client-IP '';", block)
            self.assertNotIn("$proxy_add_x_forwarded_for", block)

    def test_access_log_does_not_present_incoming_forwarded_for_as_client_ip(self):
        self.assertNotIn("$http_x_forwarded_for", CONFIG)


if __name__ == "__main__":
    unittest.main()
