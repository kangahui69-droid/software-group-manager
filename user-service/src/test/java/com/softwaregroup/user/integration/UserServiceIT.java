package com.softwaregroup.user.integration;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.dao.MemberProfileDAO;
import com.softwaregroup.user.dao.UserDAO;
import com.softwaregroup.user.model.entity.User;
import com.softwaregroup.user.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 集成测试
 *
 * 测试用户服务的核心功能：用户CRUD、登录验证
 * 使用 Mockito 模拟 DAO 层，专注于业务逻辑测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceIT {

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
    void listMembers_shouldReturnMemberList() {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("admin");
        user1.setRole("ADMIN");

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("member1");
        user2.setRole("MEMBER");

        when(userDAO.findByConditions(isNull(), isNull(), isNull())).thenReturn(Arrays.asList(user1, user2));
        when(userDAO.count()).thenReturn(2);

        Result result = memberService.listMembers(null, null, null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void listMembers_withKeywordFilter_shouldReturnFilteredList() {
        when(userDAO.findByConditions(eq("admin"), isNull(), isNull())).thenReturn(Arrays.asList());
        when(userDAO.count()).thenReturn(0);

        Result result = memberService.listMembers("admin", null, null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(userDAO).findByConditions(eq("admin"), isNull(), isNull());
    }

    @Test
    void getMemberDetail_withValidId_shouldReturnMemberDetail() {
        User user = new User();
        user.setId(1);
        user.setUsername("admin");
        user.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(user);
        when(memberProfileDAO.findByUserId(1)).thenReturn(null);

        Result result = memberService.getMemberDetail(1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getMemberDetail_withInvalidId_shouldReturnError() {
        when(userDAO.findById(9999)).thenReturn(null);

        Result result = memberService.getMemberDetail(9999);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void createMember_withValidData_shouldReturnSuccess() {
        // Mock admin user for authorization check
        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.existsByUsername(anyString())).thenReturn(false);
        when(userDAO.insert(any(User.class))).thenReturn(100);

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("username", "newuser");
        memberData.put("password", "password123");
        memberData.put("role", "MEMBER");

        Result result = memberService.createMember(memberData, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void createMember_withDuplicateUsername_shouldReturnError() {
        // Mock admin user for authorization check
        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.existsByUsername("admin")).thenReturn(true);

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("username", "admin");
        memberData.put("password", "password123");
        memberData.put("role", "MEMBER");

        Result result = memberService.createMember(memberData, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void updateMember_withValidData_shouldReturnSuccess() {
        User user = new User();
        user.setId(2);
        user.setUsername("member1");
        user.setRole("MEMBER");
        user.setName("Original Name");

        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        when(userDAO.findById(2)).thenReturn(user);
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.update(any(User.class))).thenReturn(true);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", "updated@example.com");

        Result result = memberService.updateMember(2, updateData, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void enableMember_withValidId_shouldReturnSuccess() {
        User user = new User();
        user.setId(2);
        user.setUsername("member1");

        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        when(userDAO.findById(2)).thenReturn(user);
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.updateStatus(2, 1)).thenReturn(true);

        Result result = memberService.enableMember(2, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void disableMember_withValidId_shouldReturnSuccess() {
        User user = new User();
        user.setId(2);
        user.setUsername("member1");

        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        when(userDAO.findById(2)).thenReturn(user);
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.updateStatus(2, 0)).thenReturn(true);

        Result result = memberService.disableMember(2, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void resetPassword_withValidId_shouldReturnSuccess() {
        User user = new User();
        user.setId(2);

        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        when(userDAO.findById(2)).thenReturn(user);
        when(userDAO.findById(1)).thenReturn(admin);
        when(userDAO.resetPassword(2, "123456")).thenReturn(true);

        Result result = memberService.resetPassword(2, 1);

        assertThat(result.isSuccess()).isTrue();
    }
}
