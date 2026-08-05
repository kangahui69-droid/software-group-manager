package com.softwaregroup.project.service;

import com.softwaregroup.project.dao.ProjectDAO;
import com.softwaregroup.project.dao.UserDAO;
import com.softwaregroup.project.dao.FileService;
import com.softwaregroup.project.dao.DictionaryDAO;
import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.User;
import com.softwaregroup.project.model.ProjectMemberApplication;
import com.softwaregroup.project.model.dto.ProjectDTO;
import com.softwaregroup.project.model.dto.ProjectFilterDTO;
import com.softwaregroup.project.model.dto.PlanDTO;
import com.softwaregroup.project.model.dto.ProgressDTO;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectService 单元测试
 * 覆盖所有公开业务方法的正常路径、边界情况和异常场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("项目服务测试")
class ProjectServiceTest {

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private FileService fileService;

    @Mock
    private DictionaryDAO dictionaryDAO;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectDTO testProjectDTO;
    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1);
        testProject.setName("测试项目");
        testProject.setDescription("项目描述");
        testProject.setCategory("CATEGORY");
        testProject.setYear(2026);
        testProject.setStatus("pending");
        testProject.setLeaderId(2);
        testProject.setAdminId(2);
        testProject.setDeleted(0);

        testProjectDTO = new ProjectDTO();
        testProjectDTO.setName("新项目");
        testProjectDTO.setDescription("新项目描述");
        testProjectDTO.setCategory("CATEGORY");
        testProjectDTO.setYear(2026);
        testProjectDTO.setExpectedStartDate(new Date());
        testProjectDTO.setExpectedEndDate(new Date(System.currentTimeMillis() + 86400000));

        testUser = new User();
        testUser.setId(2);
        testUser.setUsername("member1");
        testUser.setRole("MEMBER");

        testAdmin = new User();
        testAdmin.setId(1);
        testAdmin.setUsername("admin");
        testAdmin.setRole("ADMIN");
    }

    @Nested
    @DisplayName("createProject - 创建项目")
    class CreateProjectTests {

        @Test
        @DisplayName("正常路径：成员成功创建项目")
        void should_create_project_when_member() {
            when(userDAO.findById(2)).thenReturn(testUser);
            when(projectDAO.countProjectsByMemberAndYear(2, 2026)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenReturn(true);
            doNothing().when(projectDAO).addMember(anyInt(), anyInt(), anyString(), any());
            doNothing().when(projectDAO).addHistory(anyInt(), anyString(), anyInt(), anyString(), anyString(), any(), any());

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO, times(1)).insert(any(Project.class));
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = projectService.createProject(null, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("请求参数不能为空");
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = projectService.createProject(testProjectDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("异常场景：项目名称为空应返回错误")
        void should_return_error_when_name_is_empty() {
            testProjectDTO.setName("");

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("项目名称不能为空");
        }

        @Test
        @DisplayName("异常场景：项目名称为null应返回错误")
        void should_return_error_when_name_is_null() {
            testProjectDTO.setName(null);

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("边界情况：项目名称超过200字符应返回错误")
        void should_return_error_when_name_too_long() {
            testProjectDTO.setName("a".repeat(201));

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("200");
        }

        @Test
        @DisplayName("边界情况：项目描述超过5000字符应返回错误")
        void should_return_error_when_description_too_long() {
            testProjectDTO.setDescription("a".repeat(5001));

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("5000");
        }

        @Test
        @DisplayName("异常场景：项目分类为空应返回错误")
        void should_return_error_when_category_is_empty() {
            testProjectDTO.setCategory("");

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("项目分类不能为空");
        }

        @Test
        @DisplayName("异常场景：用户不存在应返回404")
        void should_return_404_when_user_not_found() {
            when(userDAO.findById(999)).thenReturn(null);

            Result result = projectService.createProject(testProjectDTO, 999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("用户不存在");
        }

        @Test
        @DisplayName("异常场景：每年参与项目超过3个应返回错误")
        void should_return_error_when_exceeds_yearly_limit() {
            when(userDAO.findById(2)).thenReturn(testUser);
            when(projectDAO.countProjectsByMemberAndYear(2, 2026)).thenReturn(3);

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每年最多参与3个项目");
        }

        @Test
        @DisplayName("异常场景：预计开始时间晚于结束时间应返回错误")
        void should_return_error_when_start_after_end() {
            testProjectDTO.setExpectedStartDate(new Date(System.currentTimeMillis() + 86400000));
            testProjectDTO.setExpectedEndDate(new Date());

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("开始时间不能晚于结束时间");
        }

        @Test
        @DisplayName("异常场景：项目预算为负数应返回错误")
        void should_return_error_when_budget_is_negative() {
            testProjectDTO.setBudget(new java.math.BigDecimal("-100"));

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("预算不能为负数");
        }

        @Test
        @DisplayName("异常场景：仓库地址格式错误应返回错误")
        void should_return_error_when_repo_url_invalid() {
            testProjectDTO.setRepoUrl("ftp://invalid-url");

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("仓库地址格式不正确");
        }

        @Test
        @DisplayName("边界情况：仓库地址为空应正常创建")
        void should_create_when_repo_url_is_empty() {
            testProjectDTO.setRepoUrl(null);
            when(userDAO.findById(2)).thenReturn(testUser);
            when(projectDAO.countProjectsByMemberAndYear(2, 2026)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenReturn(true);

            Result result = projectService.createProject(testProjectDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateProject - 更新项目")
    class UpdateProjectTests {

        @Test
        @DisplayName("正常路径：项目管理员成功更新项目")
        void should_update_project_when_admin() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.update(any(Project.class), any())).thenReturn(true);

            ProjectDTO updateDTO = new ProjectDTO();
            updateDTO.setName("更新后的项目名称");
            updateDTO.setDescription("更新后的描述");
            updateDTO.setCategory("CATEGORY");

            Result result = projectService.updateProject(1, updateDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = projectService.updateProject(null, testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = projectService.updateProject(1, null, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：项目不存在应返回404")
        void should_return_404_when_project_not_found() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = projectService.updateProject(999, testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("项目不存在");
        }

        @Test
        @DisplayName("异常场景：非管理员无权修改应返回403")
        void should_return_403_when_not_admin() {
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.updateProject(1, testProjectDTO, 3);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：已完成项目无法修改")
        void should_return_error_when_project_completed() {
            testProject.setStatus("completed");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.updateProject(1, testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已完成");
        }

        @Test
        @DisplayName("异常场景：进行中项目无法修改")
        void should_return_error_when_project_in_progress() {
            testProject.setStatus("in_progress");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.updateProject(1, testProjectDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("进行中");
        }

        @Test
        @DisplayName("异常场景：项目名称为空应返回错误")
        void should_return_error_when_name_is_empty() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);

            ProjectDTO updateDTO = new ProjectDTO();
            updateDTO.setName("");
            updateDTO.setCategory("CATEGORY");

            Result result = projectService.updateProject(1, updateDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("deleteProject - 删除项目")
    class DeleteProjectTests {

        @Test
        @DisplayName("正常路径：项目管理员成功删除待审核项目")
        void should_delete_project_when_admin() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.delete(eq(1), any())).thenReturn(true);

            Result result = projectService.deleteProject(1, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = projectService.deleteProject(null, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：项目不存在应返回404")
        void should_return_404_when_project_not_found() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = projectService.deleteProject(999, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：已完成项目无法删除")
        void should_return_error_when_project_completed() {
            testProject.setStatus("completed");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.deleteProject(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已完成");
        }

        @Test
        @DisplayName("异常场景：进行中项目无法删除")
        void should_return_error_when_project_in_progress() {
            testProject.setStatus("in_progress");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.deleteProject(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("approveProject - 审批项目")
    class ApproveProjectTests {

        @Test
        @DisplayName("正常路径：管理员成功审批项目")
        void should_approve_project_when_admin() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(projectDAO.approve(eq(1), eq(1), any())).thenReturn(true);

            Result result = projectService.approveProject(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = projectService.approveProject(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("项目ID不能为空");
        }

        @Test
        @DisplayName("异常场景：项目不存在应返回404")
        void should_return_404_when_project_not_found() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = projectService.approveProject(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：项目已审核应返回错误")
        void should_return_error_when_already_approved() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.approveProject(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已审核");
        }

        @Test
        @DisplayName("异常场景：非管理员无权审批")
        void should_return_403_when_not_admin() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(userDAO.findById(2)).thenReturn(testUser);

            Result result = projectService.approveProject(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("rejectProject - 驳回项目")
    class RejectProjectTests {

        @Test
        @DisplayName("正常路径：管理员成功驳回项目")
        void should_reject_project_when_admin() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(projectDAO.reject(eq(1), eq(1), any())).thenReturn(true);

            Result result = projectService.rejectProject(1, "不符合要求", 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = projectService.rejectProject(null, "原因", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：驳回原因为空应返回错误")
        void should_return_error_when_reason_is_empty() {
            Result result = projectService.rejectProject(1, "", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("驳回原因不能为空");
        }

        @Test
        @DisplayName("异常场景：驳回原因超过500字符应返回错误")
        void should_return_error_when_reason_too_long() {
            String longReason = "a".repeat(501);

            Result result = projectService.rejectProject(1, longReason, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("applyMember - 申请加入项目")
    class ApplyMemberTests {

        @Test
        @DisplayName("正常路径：成员成功申请加入已通过审核的项目")
        void should_apply_member_successfully() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 3)).thenReturn(false);
            when(projectDAO.hasPendingApplication(1, 3)).thenReturn(false);
            when(projectDAO.applyMember(eq(1), eq(3), anyString(), any())).thenReturn(true);

            Result result = projectService.applyMember(1, 3, "申请加入");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID为空应返回错误")
        void should_return_error_when_project_id_is_null() {
            Result result = projectService.applyMember(null, 3, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：项目不存在应返回404")
        void should_return_404_when_project_not_found() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = projectService.applyMember(999, 3, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：项目未通过审核无法申请加入")
        void should_return_error_when_project_not_approved() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.applyMember(1, 3, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("未通过审核");
        }

        @Test
        @DisplayName("异常场景：项目负责人不能申请加入自己的项目")
        void should_return_error_when_leader_apply_own_project() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.applyMember(1, 2, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("负责人不能申请加入自己的项目");
        }

        @Test
        @DisplayName("异常场景：已是项目成员不能重复申请")
        void should_return_error_when_already_member() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 3)).thenReturn(true);

            Result result = projectService.applyMember(1, 3, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已是成员");
        }

        @Test
        @DisplayName("异常场景：已有待审核申请不能重复申请")
        void should_return_error_when_has_pending_application() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 3)).thenReturn(false);
            when(projectDAO.hasPendingApplication(1, 3)).thenReturn(true);

            Result result = projectService.applyMember(1, 3, "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已提交申请");
        }
    }

    @Nested
    @DisplayName("approveMember - 审批成员申请")
    class ApproveMemberTests {

        @Test
        @DisplayName("正常路径：项目管理员成功审批成员申请")
        void should_approve_member_successfully() {
            ProjectMemberApplication app = new ProjectMemberApplication();
            app.setId(1);
            app.setProjectId(1);
            app.setUserId(3);
            app.setStatus("PENDING");
            app.setUserName("member3");

            when(projectDAO.getMemberApplicationById(1)).thenReturn(app);
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.approveMemberApplication(eq(1), eq(2), any())).thenReturn(true);

            Result result = projectService.approveMember(1, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：申请ID为空应返回错误")
        void should_return_error_when_application_id_is_null() {
            Result result = projectService.approveMember(null, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：申请不存在应返回404")
        void should_return_404_when_application_not_found() {
            when(projectDAO.getMemberApplicationById(999)).thenReturn(null);

            Result result = projectService.approveMember(999, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：申请非待审核状态应返回错误")
        void should_return_error_when_not_pending() {
            ProjectMemberApplication app = new ProjectMemberApplication();
            app.setId(1);
            app.setStatus("APPROVED");
            when(projectDAO.getMemberApplicationById(1)).thenReturn(app);

            Result result = projectService.approveMember(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("非待审核");
        }
    }

    @Nested
    @DisplayName("rejectMember - 驳回成员申请")
    class RejectMemberTests {

        @Test
        @DisplayName("正常路径：项目管理员成功驳回成员申请")
        void should_reject_member_successfully() {
            ProjectMemberApplication app = new ProjectMemberApplication();
            app.setId(1);
            app.setProjectId(1);
            app.setUserId(3);
            app.setStatus("PENDING");
            app.setUserName("member3");

            when(projectDAO.getMemberApplicationById(1)).thenReturn(app);
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.rejectMemberApplication(eq(1), eq(2), anyString(), any())).thenReturn(true);

            Result result = projectService.rejectMember(1, "不符合要求", 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：申请ID为空应返回错误")
        void should_return_error_when_application_id_is_null() {
            Result result = projectService.rejectMember(null, "原因", 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("addPlan - 添加计划")
    class AddPlanTests {

        @Test
        @DisplayName("正常路径：项目成员成功添加计划")
        void should_add_plan_successfully() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 2)).thenReturn(true);
            when(projectDAO.addPlan(any(), any())).thenReturn(true);

            PlanDTO planDTO = new PlanDTO();
            planDTO.setTitle("第一阶段计划");
            planDTO.setStartDate(new Date());
            planDTO.setEndDate(new Date(System.currentTimeMillis() + 86400000));

            Result result = projectService.addPlan(1, planDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：计划标题为空应返回错误")
        void should_return_error_when_plan_title_is_empty() {
            PlanDTO planDTO = new PlanDTO();
            planDTO.setTitle("");

            Result result = projectService.addPlan(1, planDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("计划标题不能为空");
        }

        @Test
        @DisplayName("异常场景：计划开始时间晚于结束时间应返回错误")
        void should_return_error_when_plan_start_after_end() {
            PlanDTO planDTO = new PlanDTO();
            planDTO.setTitle("计划");
            planDTO.setStartDate(new Date(System.currentTimeMillis() + 86400000));
            planDTO.setEndDate(new Date());

            Result result = projectService.addPlan(1, planDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：待审核项目不能添加计划")
        void should_return_error_when_project_pending() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);

            PlanDTO planDTO = new PlanDTO();
            planDTO.setTitle("计划");

            Result result = projectService.addPlan(1, planDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("当前状态不允许添加计划");
        }
    }

    @Nested
    @DisplayName("addProgress - 添加进度")
    class AddProgressTests {

        @Test
        @DisplayName("正常路径：项目成员成功添加进度")
        void should_add_progress_successfully() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 2)).thenReturn(true);
            when(projectDAO.addProgress(any(), any())).thenReturn(true);

            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("完成设计");
            progressDTO.setCompletionRate(50);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：进度标题为空应返回错误")
        void should_return_error_when_progress_title_is_empty() {
            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("");
            progressDTO.setCompletionRate(50);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：完成率为空应返回错误")
        void should_return_error_when_completion_rate_is_null() {
            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("进度");
            progressDTO.setCompletionRate(null);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：完成率小于0应返回错误")
        void should_return_error_when_rate_negative() {
            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("进度");
            progressDTO.setCompletionRate(-1);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("0-100");
        }

        @Test
        @DisplayName("异常场景：完成率大于100应返回错误")
        void should_return_error_when_rate_over_100() {
            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("进度");
            progressDTO.setCompletionRate(101);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("0-100");
        }

        @Test
        @DisplayName("边界情况：完成率为0应正常添加")
        void should_add_when_rate_is_zero() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 2)).thenReturn(true);
            when(projectDAO.addProgress(any(), any())).thenReturn(true);

            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("刚开始");
            progressDTO.setCompletionRate(0);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("边界情况：完成率为100应正常添加")
        void should_add_when_rate_is_100() {
            testProject.setStatus("approved");
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.isMember(1, 2)).thenReturn(true);
            when(projectDAO.addProgress(any(), any())).thenReturn(true);

            ProgressDTO progressDTO = new ProgressDTO();
            progressDTO.setTitle("已完成");
            progressDTO.setCompletionRate(100);

            Result result = projectService.addProgress(1, progressDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("listProjects - 项目列表")
    class ListProjectsTests {

        @Test
        @DisplayName("正常路径：返回项目列表")
        void should_return_project_list() {
            when(projectDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList(testProject));

            Result result = projectService.listProjects(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("正常路径：按关键词筛选")
        void should_filter_by_keyword() {
            when(projectDAO.findByConditions(eq("测试"), any(), any()))
                    .thenReturn(Arrays.asList(testProject));

            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setKeyword("测试");

            Result result = projectService.listProjects(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：按状态筛选")
        void should_filter_by_status() {
            when(projectDAO.findByConditions(any(), eq("pending"), any()))
                    .thenReturn(Arrays.asList(testProject));

            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setStatus("pending");

            Result result = projectService.listProjects(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：按年度筛选")
        void should_filter_by_year() {
            when(projectDAO.findByConditions(any(), any(), eq(2026)))
                    .thenReturn(Arrays.asList(testProject));

            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setYear(2026);

            Result result = projectService.listProjects(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：无项目时返回空列表")
        void should_return_empty_list_when_no_projects() {
            when(projectDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("getProjectDetail - 项目详情")
    class GetProjectDetailTests {

        @Test
        @DisplayName("正常路径：返回项目详情")
        void should_return_project_detail() {
            when(projectDAO.findById(1)).thenReturn(testProject);
            when(projectDAO.getProjectMembers(1)).thenReturn(Collections.emptyList());
            when(projectDAO.getLabels(1)).thenReturn(Collections.emptyList());
            when(projectDAO.getPlans(1)).thenReturn(Collections.emptyList());
            when(projectDAO.getProgressList(1)).thenReturn(Collections.emptyList());
            when(projectDAO.getHistory(1)).thenReturn(Collections.emptyList());

            Result result = projectService.getProjectDetail(1, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：项目ID无效应返回错误")
        void should_return_error_when_id_invalid() {
            Result result = projectService.getProjectDetail(0, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：项目不存在应返回404")
        void should_return_404_when_project_not_found() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = projectService.getProjectDetail(999, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：未审核项目且非成员查看应返回403")
        void should_return_403_when_not_approved_and_not_member() {
            testProject.setStatus("pending");
            when(projectDAO.findById(1)).thenReturn(testProject);

            Result result = projectService.getProjectDetail(1, 3);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("getMyProjects - 我的项目")
    class GetMyProjectsTests {

        @Test
        @DisplayName("正常路径：返回用户的项目列表")
        void should_return_user_projects() {
            when(projectDAO.findProjectsByUserId(2))
                    .thenReturn(Arrays.asList(testProject));

            Result result = projectService.getMyProjects(2, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = projectService.getMyProjects(null, 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("正常路径：无项目时返回空列表")
        void should_return_empty_list_when_no_projects() {
            when(projectDAO.findProjectsByUserId(2))
                    .thenReturn(Collections.emptyList());

            Result result = projectService.getMyProjects(2, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Project Status 枚举状态测试")
    class ProjectStatusTests {

        @Test
        @DisplayName("状态常量验证：PENDING")
        void should_have_correct_pending_status() {
            assertThat(projectService.getStatusPending()).isEqualTo("pending");
        }

        @Test
        @DisplayName("状态常量验证：APPROVED")
        void should_have_correct_approved_status() {
            assertThat(projectService.getStatusApproved()).isEqualTo("approved");
        }

        @Test
        @DisplayName("状态常量验证：IN_PROGRESS")
        void should_have_correct_in_progress_status() {
            assertThat(projectService.getStatusInProgress()).isEqualTo("in_progress");
        }

        @Test
        @DisplayName("状态常量验证：COMPLETED")
        void should_have_correct_completed_status() {
            assertThat(projectService.getStatusCompleted()).isEqualTo("completed");
        }
    }
}
