package com.softwaregroup.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Identity obtained from a verified JWT, never from client-supplied identity fields. */
public record CurrentUser(int id, String username, String role) {
    public static void requireSameId(Integer targetId, Integer currentId) {
        if (targetId == null || !targetId.equals(currentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access another user's data");
        }
    }
}
