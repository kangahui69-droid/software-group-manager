package com.softwaregroup.user.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MemberController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    private MemberController memberController;

    @BeforeEach
    void setUp() {
        memberController = new MemberController(memberService);
    }

    @Test
    void listMembers_withNoParams_shouldReturnMemberList() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);
        pageResult.put("pageSize", 20);

        when(memberService.listMembers(isNull(), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(Result.ok(pageResult));

        Result result = memberController.listMembers(null, null, null, 1, 20);

        assertTrue(result.isSuccess());
        verify(memberService).listMembers(null, null, null, 1, 20);
    }

    @Test
    void listMembers_withKeyword_shouldFilterByKeyword() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);
        pageResult.put("pageSize", 20);

        when(memberService.listMembers(eq("admin"), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(Result.ok(pageResult));

        Result result = memberController.listMembers("admin", null, null, 1, 20);

        assertTrue(result.isSuccess());
        verify(memberService).listMembers("admin", null, null, 1, 20);
    }

    @Test
    void listMembers_withRoleFilter_shouldFilterByRole() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);
        pageResult.put("pageSize", 20);

        when(memberService.listMembers(isNull(), eq("ADMIN"), isNull(), eq(1), eq(20)))
                .thenReturn(Result.ok(pageResult));

        Result result = memberController.listMembers(null, "ADMIN", null, 1, 20);

        assertTrue(result.isSuccess());
        verify(memberService).listMembers(null, "ADMIN", null, 1, 20);
    }

    @Test
    void getMemberDetail_withValidId_shouldReturnMemberDetail() {
        Map<String, Object> memberDetail = new HashMap<>();
        memberDetail.put("id", 1);
        memberDetail.put("username", "admin");

        when(memberService.getMemberDetail(1)).thenReturn(Result.ok(memberDetail));

        Result result = memberController.getMemberDetail(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(memberService).getMemberDetail(1);
    }

    @Test
    void getMemberDetail_withInvalidId_shouldReturnError() {
        when(memberService.getMemberDetail(999)).thenReturn(Result.error(404, "成员不存在"));

        Result result = memberController.getMemberDetail(999);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
    }

    @Test
    void createMember_withValidData_shouldReturnSuccess() {
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("username", "newuser");
        memberData.put("password", "password123");
        memberData.put("role", "MEMBER");

        Map<String, Object> createdUser = new HashMap<>();
        createdUser.put("id", 100);
        createdUser.put("username", "newuser");

        when(memberService.createMember(anyMap(), eq(1))).thenReturn(Result.ok(createdUser));

        Result result = memberController.createMember(memberData, 1);

        assertTrue(result.isSuccess());
        verify(memberService).createMember(anyMap(), eq(1));
    }

    @Test
    void updateMember_withValidData_shouldReturnSuccess() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", "newemail@test.com");
        updateData.put("phone", "13800138000");

        when(memberService.updateMember(eq(2), anyMap(), eq(1))).thenReturn(Result.ok());

        Result result = memberController.updateMember(2, updateData, 1);

        assertTrue(result.isSuccess());
        verify(memberService).updateMember(eq(2), anyMap(), eq(1));
    }

    @Test
    void deleteMember_withValidId_shouldReturnSuccess() {
        when(memberService.deleteMember(2, 1)).thenReturn(Result.ok());

        Result result = memberController.deleteMember(2, 1);

        assertTrue(result.isSuccess());
        verify(memberService).deleteMember(2, 1);
    }

    @Test
    void enableMember_withValidId_shouldReturnSuccess() {
        when(memberService.enableMember(2, 1)).thenReturn(Result.ok());

        Result result = memberController.enableMember(2, 1);

        assertTrue(result.isSuccess());
        verify(memberService).enableMember(2, 1);
    }

    @Test
    void disableMember_withValidId_shouldReturnSuccess() {
        when(memberService.disableMember(2, 1)).thenReturn(Result.ok());

        Result result = memberController.disableMember(2, 1);

        assertTrue(result.isSuccess());
        verify(memberService).disableMember(2, 1);
    }

    @Test
    void resetPassword_withValidId_shouldReturnSuccess() {
        when(memberService.resetPassword(2, 1)).thenReturn(Result.ok());

        Result result = memberController.resetPassword(2, 1);

        assertTrue(result.isSuccess());
        verify(memberService).resetPassword(2, 1);
    }
}
