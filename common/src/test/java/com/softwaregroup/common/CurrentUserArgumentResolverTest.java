package com.softwaregroup.common;

import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.CurrentUserArgumentResolver;
import com.softwaregroup.common.exception.GlobalExceptionHandler;
import com.softwaregroup.common.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrentUserArgumentResolverTest {
    private final JwtService jwt = new JwtService("principal-test-secret-at-least-32-bytes", "v1", "software-group", "software-group-api");
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(jwt))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void ignoresForgedIdentityHeaderQueryAndBody() throws Exception {
        mvc.perform(post("/identity?userId=999&operatorId=999")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER"))
                        .header("X-User-Id", "999")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":999,\"operatorId\":999,\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void rejectsMissingOrUntrustedToken() throws Exception {
        mvc.perform(post("/identity").header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
        JwtService otherKey = new JwtService("different-principal-test-secret-32-bytes", "v1", "software-group", "software-group-api");
        mvc.perform(post("/identity")
                        .header("Authorization", "Bearer " + otherKey.generateToken(7, "alice", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectsOtherUsersSelfRoute() throws Exception {
        mvc.perform(post("/users/8/self")
                        .header("Authorization", "Bearer " + jwt.generateToken(7, "alice", "MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @RestController
    static class TestController {
        @PostMapping("/identity")
        CurrentUser identity(@RequestBody Map<String, Object> ignored,
                             @AuthenticatedUser CurrentUser user) {
            return user;
        }

        @PostMapping("/users/{id}/self")
        Integer self(@PathVariable Integer id, @AuthenticatedUser Integer currentId) {
            CurrentUser.requireSameId(id, currentId);
            return currentId;
        }
    }
}
