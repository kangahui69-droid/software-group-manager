package com.softwaregroup.common;

import com.softwaregroup.common.security.ApiAuthenticationFilter;
import com.softwaregroup.common.security.ApiSecurityPolicy;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.CurrentUserArgumentResolver;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.security.RoleAuthorizationInterceptor;
import com.softwaregroup.common.util.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiAuthenticationFilterTest {
    private final JwtService jwt = new JwtService("shared-api-filter-test-secret-32-bytes", "v1", "software-group", "software-group-api");
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController(), new AdminController())
                .addFilters(new ApiAuthenticationFilter(jwt))
                .addInterceptors(new RoleAuthorizationInterceptor())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(jwt))
                .build();
    }

    @Test
    void blocksAnonymousRequestsAcrossAllEightServices() throws Exception {
        for (String path : new String[]{"/api/users/profile", "/api/files", "/api/attendance",
                "/api/resumes", "/api/problems", "/api/groups", "/api/ai/history",
                "/api/projects/my"}) {
            mvc.perform(get(path).header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    @Test
    void publicPathsAreNarrowAndWriteRequestsNeedToken() throws Exception {
        mvc.perform(get("/api/news")).andExpect(status().isOk());
        mvc.perform(get("/api/news/7")).andExpect(status().isOk());
        mvc.perform(post("/api/recruit/apply")).andExpect(status().isOk());
        mvc.perform(post("/api/activities"))
                .andExpect(status().isUnauthorized());
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/activities")).isTrue();
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/projects/7")).isTrue();
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/awards/7/images")).isTrue();
        mvc.perform(get("/api/activities/my"))
                .andExpect(status().isUnauthorized());
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/activities/7/participants")).isFalse();
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/projects/my")).isFalse();
        assertThat(ApiSecurityPolicy.isPublic("GET", "/actuator/metrics")).isFalse();
        assertThat(ApiSecurityPolicy.isPublic("POST", "/api/users/login/extra")).isFalse();
        assertThat(ApiSecurityPolicy.isPublic("GET", "/api/news/type/notice")).isTrue();
    }

    @Test
    void rejectsInvalidTokenAndAcceptsVerifiedIdentity() throws Exception {
        mvc.perform(post("/api/activities").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        mvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER"))
                        .header("X-User-Id", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7));
    }

    @Test
    void publicRouteUsesVerifiedIdentityWhenBearerTokenIsPresent() throws Exception {
        mvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
        mvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
        mvc.perform(get("/api/projects").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminMethodsRejectForgedRoleHeader() throws Exception {
        mvc.perform(get("/api/attendance")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER"))
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/api/attendance")
                        .header("Authorization", "Bearer " + jwt.generateToken(8, "admin", "ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/members")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER")))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {
        @GetMapping("/api/projects")
        String projects(HttpServletRequest request) {
            CurrentUser user = (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
            return user == null ? "anonymous" : Integer.toString(user.id());
        }

        @GetMapping("/api/news")
        String list() {
            return "public";
        }

        @GetMapping("/api/news/{id}")
        String detail() {
            return "public";
        }

        @PostMapping("/api/recruit/apply")
        String apply() {
            return "public";
        }

        @PostMapping("/api/activities")
        Integer create(@AuthenticatedUser Integer userId) {
            return userId;
        }

        @GetMapping("/api/attendance")
        @RequireRole("ADMIN")
        String attendance() {
            return "admin";
        }
    }

    @RestController
    @RequireRole("ADMIN")
    static class AdminController {
        @GetMapping("/api/members")
        String members() {
            return "admin";
        }
    }
}
