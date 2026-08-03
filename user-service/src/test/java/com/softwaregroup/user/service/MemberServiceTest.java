package com.softwaregroup.user.service;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.dao.MemberProfileDAO;
import com.softwaregroup.user.dao.UserDAO;
import com.softwaregroup.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MemberService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(userDAO, memberProfileDAO);
    }

    @Test
    void listMembers_withValidParams_shouldReturnMemberList() {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("admin");
        user1.setRole("ADMIN");

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("member1");
        user2.setRole("MEMBER");

        List<User> mockUsers = Arrays.asList(user1, user2);
        when(userDAO.findByConditions(null, null, null)).thenReturn(mockUsers);
        when(userDAO.count()).thenReturn(2);

        Result result = memberService.listMembers(null, null, null, 1, 20);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Map);
    }

    @Test
    void listMembers_withKeyword_shouldFilterResults() {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("admin");
        user1.setRole("ADMIN");

        List<User> mockUsers = Arrays.asList(user1);
        when(userDAO.findByConditions("admin", null, null)).thenReturn(mockUsers);
        when(userDAO.count()).thenReturn(1);

        Result result = memberService.listMembers("admin", null, null, 1, 20);

        assertTrue(result.isSuccess());
        verify(userDAO).findByConditions("admin", null, null);
    }

    @Test
    void getMemberDetail_withValidId_shouldReturnMember() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("admin");
        mockUser.setName("Administrator");
        mockUser.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(mockUser);
        when(memberProfileDAO.findByUserId(1)).thenReturn(null);

        Result result = memberService.getMemberDetail(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getMemberDetail_withInvalidId_shouldReturnError() {
        when(userDAO.findById(999)).thenReturn(null);

        Result result = memberService.getMemberDetail(999);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
    }

    @Test
    void getMemberDetail_withNullId_shouldReturnError() {
        Result result = memberService.getMemberDetail(null);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void deleteMember_asAdmin_shouldSucceed() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        User targetUser = new User();
        targetUser.setId(2);
        targetUser.setUsername("member1");

        when(userDAO.findById(2)).thenReturn(targetUser);
        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.delete(2)).thenReturn(true);

        Result result = memberService.deleteMember(2, 1);

        assertTrue(result.isSuccess());
        verify(userDAO).delete(2);
    }

    @Test
    void deleteMember_asNonAdmin_shouldReturnError() {
        User nonAdminUser = new User();
        nonAdminUser.setId(2);
        nonAdminUser.setRole("MEMBER");

        User targetUser = new User();
        targetUser.setId(3);

        when(userDAO.findById(3)).thenReturn(targetUser);
        when(userDAO.findById(2)).thenReturn(nonAdminUser);

        Result result = memberService.deleteMember(3, 2);

        assertFalse(result.isSuccess());
        assertEquals(403, result.getCode());
    }

    @Test
    void deleteMember_selfDelete_shouldReturnError() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(adminUser);

        Result result = memberService.deleteMember(1, 1);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能删除自己"));
    }

    @Test
    void enableMember_asAdmin_shouldSucceed() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        User targetUser = new User();
        targetUser.setId(2);
        targetUser.setUsername("member1");

        when(userDAO.findById(2)).thenReturn(targetUser);
        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.updateStatus(2, 1)).thenReturn(true);

        Result result = memberService.enableMember(2, 1);

        assertTrue(result.isSuccess());
        verify(userDAO).updateStatus(2, 1);
    }

    @Test
    void disableMember_asAdmin_shouldSucceed() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        User targetUser = new User();
        targetUser.setId(2);
        targetUser.setUsername("member1");

        when(userDAO.findById(2)).thenReturn(targetUser);
        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.updateStatus(2, 0)).thenReturn(true);

        Result result = memberService.disableMember(2, 1);

        assertTrue(result.isSuccess());
        verify(userDAO).updateStatus(2, 0);
    }

    @Test
    void disableMember_selfDisable_shouldReturnError() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(adminUser);

        Result result = memberService.disableMember(1, 1);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能禁用自己"));
    }

    @Test
    void resetPassword_asAdmin_shouldSucceed() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        User targetUser = new User();
        targetUser.setId(2);
        targetUser.setUsername("member1");

        when(userDAO.findById(2)).thenReturn(targetUser);
        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.resetPassword(eq(2), eq("123456"))).thenReturn(true);

        Result result = memberService.resetPassword(2, 1);

        assertTrue(result.isSuccess());
        verify(userDAO).resetPassword(eq(2), eq("123456"));
    }

    @Test
    void resetPassword_selfReset_shouldReturnError() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(adminUser);

        Result result = memberService.resetPassword(1, 1);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能重置自己的密码"));
    }
}
