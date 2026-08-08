package com.softwaregroup.hr.integration;

import com.softwaregroup.hr.dao.MemberProfileDAO;
import com.softwaregroup.hr.dao.RecruitApplicationDAO;
import com.softwaregroup.hr.dao.UserDAO;
import com.softwaregroup.hr.model.entity.MemberProfile;
import com.softwaregroup.hr.model.entity.RecruitApplication;
import com.softwaregroup.hr.model.entity.User;
import com.softwaregroup.hr.model.dto.RecruitApplicationDTO;
import com.softwaregroup.hr.service.RecruitService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecruitService 集成测试
 *
 * 测试招新服务的核心功能：申请提交、审批管理
 */
@ExtendWith(MockitoExtension.class)
class RecruitServiceIT {

    @Mock
    private RecruitApplicationDAO recruitDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    private RecruitService recruitService;

    @BeforeEach
    void setUp() {
        recruitService = new RecruitService(recruitDAO, userDAO, memberProfileDAO);
    }

    @Test
    void submitApplication_withValidData_shouldReturnSuccess() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("20210001");
        dto.setMajor("计算机科学");
        dto.setEmail("zhangsan@example.com");

        when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

        Result result = recruitService.submitApplication(dto);

        assertThat(result.isSuccess()).isTrue();
        verify(recruitDAO).insert(any(RecruitApplication.class));
    }

    @Test
    void submitApplication_withNullDto_shouldReturnError() {
        Result result = recruitService.submitApplication(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void submitApplication_withEmptyName_shouldReturnError() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("");
        dto.setStudentId("20210001");
        dto.setMajor("计算机科学");
        dto.setEmail("zhangsan@example.com");

        Result result = recruitService.submitApplication(dto);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("姓名");
    }

    @Test
    void submitApplication_withEmptyStudentId_shouldReturnError() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("");
        dto.setMajor("计算机科学");
        dto.setEmail("zhangsan@example.com");

        Result result = recruitService.submitApplication(dto);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("学号");
    }

    @Test
    void submitApplication_withEmptyMajor_shouldReturnError() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("20210001");
        dto.setMajor("");
        dto.setEmail("zhangsan@example.com");

        Result result = recruitService.submitApplication(dto);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("专业");
    }

    @Test
    void submitApplication_withEmptyEmail_shouldReturnError() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("20210001");
        dto.setMajor("计算机科学");
        dto.setEmail("");

        Result result = recruitService.submitApplication(dto);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("邮箱");
    }

    @Test
    void approveApplication_withValidId_shouldReturnSuccess() {
        RecruitApplication app = new RecruitApplication();
        app.setId(1);
        app.setName("张三");
        app.setStudentId("20210001");
        app.setMajor("计算机科学");
        app.setEmail("zhangsan@example.com");
        app.setStatus(1);

        when(recruitDAO.findById(1)).thenReturn(app);
        when(userDAO.existsByUsername("20210001")).thenReturn(false);
        when(userDAO.existsByEmail("zhangsan@example.com")).thenReturn(false);

        User newUser = new User();
        newUser.setId(100);
        when(userDAO.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100);
            return true;
        });

        MemberProfile profile = new MemberProfile();
        profile.setUserId(100);
        when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
        when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

        Result result = recruitService.approveApplication(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void approveApplication_withNullId_shouldReturnError() {
        Result result = recruitService.approveApplication(null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void approveApplication_withNonExistentId_shouldReturnError() {
        when(recruitDAO.findById(9999)).thenReturn(null);

        Result result = recruitService.approveApplication(9999, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void rejectApplication_withValidId_shouldReturnSuccess() {
        RecruitApplication app = new RecruitApplication();
        app.setId(1);
        app.setStatus(1);

        when(recruitDAO.findById(1)).thenReturn(app);
        when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

        Result result = recruitService.rejectApplication(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void rejectApplication_withAlreadyHandled_shouldReturnError() {
        RecruitApplication app = new RecruitApplication();
        app.setId(1);
        app.setStatus(2); // Neither PENDING(1) nor REJECTED(0)

        when(recruitDAO.findById(1)).thenReturn(app);

        Result result = recruitService.rejectApplication(1, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("审批");
    }

    @Test
    void listApplications_withNoFilter_shouldReturnAll() {
        RecruitApplication app = new RecruitApplication();
        app.setId(1);
        app.setName("张三");
        when(recruitDAO.findByConditions(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Arrays.asList(app));

        Result result = recruitService.listApplications(null, null, null, null);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getApplicationDetail_withValidId_shouldReturnApplication() {
        RecruitApplication app = new RecruitApplication();
        app.setId(1);
        app.setName("张三");
        when(recruitDAO.findById(1)).thenReturn(app);

        Result result = recruitService.getApplicationDetail(1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getApplicationDetail_withInvalidId_shouldReturnError() {
        Result result = recruitService.getApplicationDetail(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void countPending_shouldReturnCount() {
        when(recruitDAO.countPending()).thenReturn(5);

        Result result = recruitService.countPending();

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateApplication_withValidData_shouldReturnSuccess() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("20210001");
        dto.setMajor("计算机科学");
        dto.setEmail("zhangsan@example.com");

        Result result = recruitService.validateApplication(dto);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateApplication_withInvalidData_shouldReturnError() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();

        Result result = recruitService.validateApplication(dto);

        assertThat(result.isSuccess()).isFalse();
    }
}
