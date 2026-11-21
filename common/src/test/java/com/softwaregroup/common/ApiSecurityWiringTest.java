package com.softwaregroup.common;

import com.softwaregroup.common.exception.GlobalExceptionHandler;
import com.softwaregroup.common.security.ApiAuthenticationFilter;
import com.softwaregroup.common.security.CurrentUserArgumentResolver;
import com.softwaregroup.common.security.CurrentUserWebConfig;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.security.RoleAuthorizationInterceptor;
import com.softwaregroup.common.util.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiSecurityWiringTest.TestApplication.class,
        properties = "JWT_SECRET=integration-test-secret-at-least-32-bytes")
@AutoConfigureMockMvc
class ApiSecurityWiringTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwt;

    @Test
    void bootRegistersFilterAndRoleInterceptor() throws Exception {
        mvc.perform(get("/api/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        mvc.perform(get("/api/private")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "member", "MEMBER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/api/private")
                        .header("Authorization", "Bearer " + jwt.generateToken(8, "admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({JwtService.class, ApiAuthenticationFilter.class, CurrentUserArgumentResolver.class,
            CurrentUserWebConfig.class, RoleAuthorizationInterceptor.class,
            GlobalExceptionHandler.class, TestController.class})
    static class TestApplication {
    }

    @RestController
    static class TestController {
        @GetMapping("/api/private")
        @RequireRole("ADMIN")
        String privateData() {
            return "admin";
        }
    }
}
