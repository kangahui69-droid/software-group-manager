"""Static release guard for the MIG-04 Nginx safe-default policy."""

from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[1]
NGINX = (ROOT / "nginx" / "nginx.conf").read_text(encoding="utf-8")
WEB_XML = (ROOT / "src/main/webapp/WEB-INF/web.xml").read_text(encoding="utf-8")
VERIFY = (ROOT / "scripts/verify-routes.sh").read_text(encoding="utf-8")
DB_VERIFY = (ROOT / "scripts/verify-db-backed-services.sh").read_text(encoding="utf-8")
SERVLETS = ROOT / "src/main/java/servlet/api"
DOMAINS = (
    "files", "users", "activities", "attendance", "study",
    "projects", "awards", "recruit", "resumes", "problems", "logs",
    "groups", "news",
)


class RoutePolicyTest(unittest.TestCase):
    def test_all_supported_domains_have_legacy_war_servlets(self):
        annotations = "\n".join(path.read_text(encoding="utf-8") for path in SERVLETS.glob("*.java"))
        self.assertIn("<url-pattern>/api/*</url-pattern>", WEB_XML)
        self.assertIn('USERS_PREFIX = "/users"', (SERVLETS / "UserApiServlet.java").read_text(encoding="utf-8"))
        for domain in DOMAINS:
            with self.subTest(domain=domain):
                if domain != "users":
                    self.assertIn(f"/api/{domain}/*", WEB_XML + annotations)

    def test_default_api_route_rewrites_to_war_context(self):
        self.assertRegex(NGINX, r"location\s+\^~\s+/api/\s*\{")
        self.assertIn("proxy_pass http://tomcat_backend/software-group/api/;", NGINX)
        self.assertIn('add_header X-SG-Route "legacy-war" always;', NGINX)

    def test_unverified_microservices_are_not_proxied(self):
        self.assertNotRegex(NGINX, r"proxy_pass\s+http://\$[a-z_]+_service")
        self.assertTrue(all("tomcat_backend" in target for target in re.findall(r"proxy_pass\s+([^;]+);", NGINX)))
        for domain in DOMAINS:
            with self.subTest(domain=domain):
                self.assertNotRegex(NGINX, rf"location\s+(?:=|\^~)?\s*/api/{domain}(?:/|\s)")

    def test_ai_without_legacy_equivalent_fails_closed(self):
        self.assertRegex(NGINX, r"location\s+=\s+/api/ai\s*\{")
        self.assertRegex(NGINX, r"location\s+\^~\s+/api/ai/\s*\{")
        self.assertEqual(NGINX.count('"message":"AI API is not enabled"'), 2)
        self.assertIn('add_header X-SG-Route "disabled" always;', NGINX)

    def test_members_without_legacy_equivalent_fails_closed(self):
        self.assertRegex(NGINX, r"location\s+=\s+/api/members\s*\{")
        self.assertRegex(NGINX, r"location\s+\^~\s+/api/members/\s*\{")
        self.assertEqual(NGINX.count("return 503 '{\"code\":503"), 4)
        self.assertIn("check_route /api/members disabled 503", VERIFY)

    def test_health_and_route_check_do_not_claim_business_success(self):
        self.assertIn('"businessValidated":false', NGINX)
        self.assertNotIn('"status":"UP","services"', NGINX)
        self.assertIn('check_route /api/ai/chat disabled 503', VERIFY)
        self.assertIn('200|400|401|403|405)', VERIFY)
        self.assertNotIn('if [ "$http_code" != "000" ];', VERIFY)

    def test_write_read_gate_requires_disposable_local_databases(self):
        for name in ("mig01_test", "mig02_test", "mig03_test"):
            self.assertIn(name, DB_VERIFY)
        self.assertIn("127\\.0\\.0\\.1|localhost", DB_VERIFY)
        self.assertIn("-Dtest=ProjectAwardMySqlTest", DB_VERIFY)
        self.assertIn("-Dtest=ContentMySqlTest", DB_VERIFY)
        self.assertIn("-Dtest=AiSessionMySqlTest", DB_VERIFY)


if __name__ == "__main__":
    unittest.main()
