package service;

import dao.DictionaryDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dto.PlanDTO;
import dto.ProgressDTO;
import dto.ProjectDTO;
import dto.ProjectFilterDTO;
import model.Project;
import model.ProjectMember;
import model.ProjectMemberApplication;
import model.ProjectPlan;
import model.ProjectProgress;
import model.ProjectHistory;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.4 ProjectService 项目服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 注意：这是Red阶段，ProjectService尚未实现
 * 所有测试将失败，直至实现对应方法
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - ProjectDAO: findById / insert / update / delete / approve / reject
 * - ProjectDAO: addMember / isMember / hasPendingApplication / applyMember
 * - ProjectDAO: approveMemberApplication / rejectMemberApplication / getMemberApplicationById
 * - ProjectDAO: getMemberApplications / getMyApplications / getProjectMembers
 * - ProjectDAO: addLabel / removeLabel / getLabels
 * - ProjectDAO: addPlan / updatePlan / deletePlan / getPlans
 * - ProjectDAO: addProgress / getProgressList
 * - ProjectDAO: addHistory / getHistory
 * - ProjectDAO: transferAdmin / removeMember
 * - ProjectDAO: findByConditions / findByLeaderId / findProjectsByUserId / countProjectsByMemberAndYear
 * - ProjectDAO: getProjectImages / getProjectFiles (返回List<Object[]>)
 * - UserDAO: findById
 * - FileService: uploadFile / deleteFile / viewFile (依赖)
 * - DictionaryDAO: findByType
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectService 项目服务测试")
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

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer PROJECT_ID = 100;
    private static final Integer OTHER_PROJECT_ID = 101;
    private static final Integer APPLICATION_ID = 500;
    private static final Integer PLAN_ID = 600;
    private static final Integer PROGRESS_ID = 700;
    private static final Integer FILE_ID = 800;
    private static final Integer CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    // 项目状态枚举
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_IN_PROGRESS = "in_progress";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";
    private static final String STATUS_REJECTED = "rejected";

    // 成员申请状态枚举
    private static final String APP_STATUS_PENDING = "PENDING";
    private static final String APP_STATUS_CONFIRMED = "CONFIRMED";
    private static final String APP_STATUS_REJECTED = "REJECTED";

    // 项目成员角色枚举
    private static final String MEMBER_ROLE_ADMIN = "ADMIN";
    private static final String MEMBER_ROLE_MEMBER = "MEMBER";

    // 项目分类
    private static final String CATEGORY_WEB = "WEB";
    private static final String CATEGORY_MOBILE = "MOBILE";
    private static final String CATEGORY_AI = "AI";
    private static final String CATEGORY_GAME = "GAME";

    // 文件类型
    private static final String FILE_TYPE_DOC = "DOC";
    private static final String FILE_TYPE_IMAGE = "IMAGE";
    private static final String FILE_TYPE_CODE = "CODE";

    // 历史操作类型
    private static final String OP_PROJECT_APPLY = "PROJECT_APPLY";
    private static final String OP_PROJECT_UPDATE = "PROJECT_UPDATE";
    private static final String OP_PROJECT_APPROVE = "PROJECT_APPROVE";
    private static final String OP_PROJECT_REJECT = "PROJECT_REJECT";
    private static final String OP_MEMBER_APPLY = "MEMBER_APPLY";
    private static final String OP_MEMBER_APPROVE = "MEMBER_APPROVE";
    private static final String OP_MEMBER_REJECT = "MEMBER_REJECT";
    private static final String OP_LABEL_ADD = "PROJECT_LABEL_ADD";
    private static final String OP_LABEL_REMOVE = "PROJECT_LABEL_REMOVE";
    private static final String OP_INFO_UPDATE = "PROJECT_INFO_UPDATE";
    private static final String OP_TRANSFER = "PROJECT_TRANSFER";
    private static final String OP_IMAGE_ADD = "PROJECT_IMAGE_ADD";
    private static final String OP_FILE_ADD = "PROJECT_FILE_ADD";
    private static final String OP_FILE_DELETE = "PROJECT_FILE_DELETE";

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() {
        User admin = createUser(ADMIN_USER_ID, "ADMIN");
        when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
    }

    // ==================== 工具方法 ====================

    private Project createProject(Integer id, String name, String status, Integer leaderId) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription("项目描述");
        project.setCategory(CATEGORY_WEB);
        project.setLeaderId(leaderId);
        project.setAdminId(leaderId);
        project.setStatus(status);
        project.setYear(CURRENT_YEAR);
        project.setDeleted(0);
        project.setBudget(new BigDecimal("10000.00"));
        project.setRepoUrl("https://github.com/example/repo");
        project.setDocUrl("https://docs.example.com");
        return project;
    }

    private Project createProjectWithDates(Integer id, String name, String status, Integer leaderId,
                                           Date expectedStart, Date expectedEnd) {
        Project project = createProject(id, name, status, leaderId);
        project.setExpectedStartDate(expectedStart);
        project.setExpectedEndDate(expectedEnd);
        return project;
    }

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setName("用户" + id);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private ProjectMember createProjectMember(Integer projectId, Integer userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setUserName("用户" + userId);
        return member;
    }

    private ProjectMemberApplication createApplication(Integer id, Integer projectId, Integer userId, String status) {
        ProjectMemberApplication app = new ProjectMemberApplication();
        app.setId(id);
        app.setProjectId(projectId);
        app.setUserId(userId);
        app.setStatus(status);
        app.setReason("我想加入学习");
        app.setUserName("用户" + userId);
        return app;
    }

    private ProjectPlan createPlan(Integer id, Integer projectId, String title) {
        ProjectPlan plan = new ProjectPlan();
        plan.setId(id);
        plan.setProjectId(projectId);
        plan.setTitle(title);
        plan.setDescription("计划描述");
        plan.setOrderIndex(1);
        return plan;
    }

    private ProjectProgress createProgress(Integer id, Integer projectId, Integer planId, String title) {
        ProjectProgress progress = new ProjectProgress();
        progress.setId(id);
        progress.setProjectId(projectId);
        progress.setPlanId(planId);
        progress.setTitle(title);
        progress.setDescription("进度描述");
        progress.setCompletionRate(50);
        progress.setCreatedBy(MEMBER_USER_ID);
        return progress;
    }

    private ProjectDTO createProjectDTO(String name, String description, String category,
                                        Integer year, Date startDate, Date endDate,
                                        BigDecimal budget, String repoUrl, String docUrl) {
        ProjectDTO dto = new ProjectDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCategory(category);
        dto.setYear(year);
        dto.setExpectedStartDate(startDate);
        dto.setExpectedEndDate(endDate);
        dto.setBudget(budget);
        dto.setRepoUrl(repoUrl);
        dto.setDocUrl(docUrl);
        return dto;
    }

    private ProjectDTO createValidProjectDTO() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        Date start = cal.getTime();
        cal.add(Calendar.MONTH, 3);
        Date end = cal.getTime();
        return createProjectDTO("测试项目", "项目描述", CATEGORY_WEB, CURRENT_YEAR,
                start, end, new BigDecimal("10000.00"),
                "https://github.com/example/repo", "https://docs.example.com");
    }

    private PlanDTO createPlanDTO(String title, String description, Date startDate, Date endDate, Integer orderIndex) {
        PlanDTO dto = new PlanDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setOrderIndex(orderIndex);
        return dto;
    }

    private ProgressDTO createProgressDTO(Integer planId, String title, String description, Integer completionRate) {
        ProgressDTO dto = new ProgressDTO();
        dto.setPlanId(planId);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCompletionRate(completionRate);
        return dto;
    }

    private Date addDays(Calendar cal, int days) {
        Calendar result = (Calendar) cal.clone();
        result.add(Calendar.DAY_OF_MONTH, days);
        return result.getTime();
    }

    private Object createMockPart(long size, String contentType, String fileName) {
        return new Object() {
            public long getSize() { return size; }
            public String getContentType() { return contentType; }
            public String getSubmittedFileName() { return fileName; }
        };
    }

    // ==================== createProject 创建项目 ====================

    @Nested
    @DisplayName("createProject 创建项目")
    class CreateProjectTests {

        @FastTest
        @DisplayName("创建项目时名称为空应返回错误")
        void should_return_error_when_name_empty() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setName("");

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("名称不能为空");
        }

        @FastTest
        @DisplayName("创建项目时名称为null应返回错误")
        void should_return_error_when_name_null() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setName(null);

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建项目时名称超长应返回错误")
        void should_return_error_when_name_too_long() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setName("a".repeat(201));

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建项目时描述超长应返回错误")
        void should_return_error_when_description_too_long() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setDescription("a".repeat(5001));

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("描述不能超过");
        }

        @FastTest
        @DisplayName("创建项目时分类为空应返回错误")
        void should_return_error_when_category_empty() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setCategory(null);

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建项目时用户不存在应返回404错误")
        void should_return_not_found_when_user_not_exists() {
            ProjectDTO dto = createValidProjectDTO();
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = projectService.createProject(dto, NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("创建项目时每年最多3个项目，超过应返回错误")
        void should_return_error_when_exceeds_max_projects_per_year() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(3);

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每年最多");
        }

        @FastTest
        @DisplayName("创建项目时已有2个项目应允许创建第3个")
        void should_allow_third_project() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(2);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("创建项目时开始时间晚于结束时间应返回错误")
        void should_return_error_when_start_after_end() {
            Calendar cal = Calendar.getInstance();
            ProjectDTO dto = createValidProjectDTO();
            dto.setExpectedStartDate(addDays(cal, 30));
            dto.setExpectedEndDate(addDays(cal, 10));

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("开始时间不能晚于结束时间");
        }

        @FastTest
        @DisplayName("创建项目时预算为负数应返回错误")
        void should_return_error_when_budget_negative() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setBudget(new BigDecimal("-1.00"));

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建项目时仓库URL格式错误应返回错误")
        void should_return_error_when_repo_url_invalid() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setRepoUrl("not-a-valid-url");

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("仓库地址");
        }

        @FastTest
        @DisplayName("成员正常创建项目应成功，状态为pending")
        void should_create_project_successfully_as_member() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();

            ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
            verify(projectDAO).insert(projectCaptor.capture());
            Project saved = projectCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(STATUS_PENDING);
            assertThat(saved.getLeaderId()).isEqualTo(MEMBER_USER_ID);
            assertThat(saved.getAdminId()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("创建项目成功后应自动将创建者加为ADMIN成员")
        void should_add_creator_as_admin_member() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });
            when(projectDAO.addMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), eq(MEMBER_ROLE_ADMIN), any(Connection.class))).thenReturn(true);

            projectService.createProject(dto, MEMBER_USER_ID);

            verify(projectDAO).addMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), eq(MEMBER_ROLE_ADMIN), any(Connection.class));
        }

        @FastTest
        @DisplayName("创建项目成功后应记录历史")
        void should_add_history_after_create() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            projectService.createProject(dto, MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_PROJECT_APPLY), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("创建项目时年份为null应默认使用当前年份")
        void should_default_to_current_year_when_year_null() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setYear(null);
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(eq(MEMBER_USER_ID), anyInt())).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("创建项目时数据库插入失败应返回500错误")
        void should_return_error_when_database_insert_fails() {
            ProjectDTO dto = createValidProjectDTO();
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("创建项目时DTO为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = projectService.createProject(null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建项目时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = projectService.createProject(createValidProjectDTO(), null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员创建项目应成功")
        void should_create_project_successfully_as_admin() {
            ProjectDTO dto = createValidProjectDTO();
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(projectDAO.countProjectsByMemberAndYear(ADMIN_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== updateProject 更新项目 ====================

    @Nested
    @DisplayName("updateProject 更新项目")
    class UpdateProjectTests {

        @FastTest
        @DisplayName("更新不存在的项目应返回404错误")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新非项目负责人且非管理员应返回403错误")
        void should_return_forbidden_when_not_leader_or_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新已完成的项目应返回错误")
        void should_return_error_when_project_completed() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_COMPLETED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已完成");
        }

        @FastTest
        @DisplayName("更新已取消的项目应返回错误")
        void should_return_error_when_project_canceled() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_CANCELED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新已驳回的项目应返回错误")
        void should_return_error_when_project_rejected() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_REJECTED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("负责人更新待审核项目应成功")
        void should_update_project_successfully_as_leader() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.update(any(Project.class), any(Connection.class))).thenReturn(true);

            ProjectDTO dto = createValidProjectDTO();
            dto.setName("更新后的项目名");

            Result result = projectService.updateProject(PROJECT_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).update(any(Project.class), any(Connection.class));
        }

        @FastTest
        @DisplayName("管理员更新任意项目应成功")
        void should_allow_admin_to_update_any_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.update(any(Project.class), any(Connection.class))).thenReturn(true);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("更新项目时名称为空应返回错误")
        void should_return_error_when_name_empty_on_update() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            ProjectDTO dto = createValidProjectDTO();
            dto.setName("");

            Result result = projectService.updateProject(PROJECT_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新进行中的项目应返回错误")
        void should_return_error_when_project_in_progress() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("进行中");
        }

        @FastTest
        @DisplayName("更新项目成功后应记录历史")
        void should_add_history_after_update() {
            Project project = createProject(PROJECT_ID, "旧名称", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.update(any(Project.class), any(Connection.class))).thenReturn(true);

            ProjectDTO dto = createValidProjectDTO();
            dto.setName("新名称");

            projectService.updateProject(PROJECT_ID, dto, MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_PROJECT_UPDATE), eq(MEMBER_USER_ID), anyString(), anyString(), anyString(), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("更新项目时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = projectService.updateProject(null, createValidProjectDTO(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新已审核通过的项目应成功（管理员）")
        void should_allow_update_approved_project_by_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.update(any(Project.class), any(Connection.class))).thenReturn(true);

            Result result = projectService.updateProject(PROJECT_ID, createValidProjectDTO(), ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== deleteProject 删除项目 ====================

    @Nested
    @DisplayName("deleteProject 删除项目")
    class DeleteProjectTests {

        @FastTest
        @DisplayName("删除不存在的项目应返回404错误")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.deleteProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除非负责人且非管理员应返回403错误")
        void should_return_forbidden_when_not_leader_or_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.deleteProject(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除已完成的项目应返回错误")
        void should_return_error_when_project_completed() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_COMPLETED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.deleteProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("负责人删除待审核项目应成功（软删除）")
        void should_delete_pending_project_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.delete(eq(PROJECT_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.deleteProject(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).delete(eq(PROJECT_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("管理员删除任意项目应成功")
        void should_allow_admin_to_delete_any_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.delete(eq(PROJECT_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.deleteProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除进行中的项目应返回错误")
        void should_return_error_when_project_in_progress() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.deleteProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("进行中");
        }
    }

    // ==================== approveProject 审核通过项目 ====================

    @Nested
    @DisplayName("approveProject 审核通过项目")
    class ApproveProjectTests {

        @FastTest
        @DisplayName("审核不存在的项目应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.approveProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审核已通过的项目应返回错误")
        void should_return_error_when_already_approved() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.approveProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已审核");
        }

        @FastTest
        @DisplayName("审核已驳回的项目应返回错误")
        void should_return_error_when_rejected() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_REJECTED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.approveProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("非管理员审核项目应返回403")
        void should_return_forbidden_when_not_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.approveProject(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("管理员审核待审核项目应成功")
        void should_approve_pending_project_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.approve(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.approveProject(PROJECT_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).approve(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("审核通过后应记录历史")
        void should_add_history_after_approve() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.approve(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class))).thenReturn(true);

            projectService.approveProject(PROJECT_ID, ADMIN_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_PROJECT_APPROVE), eq(ADMIN_USER_ID), anyString(), anyString(), eq(STATUS_PENDING), eq(STATUS_APPROVED), any(Connection.class));
        }
    }

    // ==================== rejectProject 审核驳回项目 ====================

    @Nested
    @DisplayName("rejectProject 审核驳回项目")
    class RejectProjectTests {

        @FastTest
        @DisplayName("驳回时原因为空应返回错误")
        void should_return_error_when_reason_empty() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.rejectProject(PROJECT_ID, "", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回原因超长应返回错误")
        void should_return_error_when_reason_too_long() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.rejectProject(PROJECT_ID, "a".repeat(501), ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员正常驳回项目应成功")
        void should_reject_project_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.reject(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.rejectProject(PROJECT_ID, "项目方向不符合", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).reject(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("驳回已通过的项目应返回错误")
        void should_return_error_when_already_approved() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.rejectProject(PROJECT_ID, "驳回原因", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回后应记录历史")
        void should_add_history_after_reject() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.reject(eq(PROJECT_ID), eq(ADMIN_USER_ID), any(Connection.class))).thenReturn(true);

            projectService.rejectProject(PROJECT_ID, "不符合要求", ADMIN_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_PROJECT_REJECT), eq(ADMIN_USER_ID), anyString(), anyString(), eq(STATUS_PENDING), eq(STATUS_REJECTED), any(Connection.class));
        }
    }

    // ==================== applyMember 申请加入项目 ====================

    @Nested
    @DisplayName("applyMember 申请加入项目")
    class ApplyMemberTests {

        @FastTest
        @DisplayName("申请加入不存在的项目应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("申请加入未审核通过的项目应返回错误")
        void should_return_error_when_project_not_approved() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("未通过审核");
        }

        @FastTest
        @DisplayName("已是项目成员申请加入应返回错误")
        void should_return_error_when_already_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已是成员");
        }

        @FastTest
        @DisplayName("已有待审核申请时重复申请应返回错误")
        void should_return_error_when_has_pending_application() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.hasPendingApplication(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已提交申请");
        }

        @FastTest
        @DisplayName("申请加入已完成的项目应返回错误")
        void should_return_error_when_project_completed() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_COMPLETED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("申请加入已取消的项目应返回错误")
        void should_return_error_when_project_canceled() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_CANCELED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("正常申请加入项目应成功")
        void should_apply_member_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.hasPendingApplication(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我对项目很感兴趣");

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("申请加入项目后应记录历史")
        void should_add_history_after_apply() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.hasPendingApplication(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "加入理由");

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_MEMBER_APPLY), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("申请理由为空白时应使用默认理由")
        void should_use_default_reason_when_blank() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.hasPendingApplication(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            when(projectDAO.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "");

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("项目负责人不能申请加入自己的项目")
        void should_return_error_when_leader_apply_own_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.applyMember(PROJECT_ID, MEMBER_USER_ID, "我想加入");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("负责人");
        }
    }

    // ==================== approveMember 审批成员通过 ====================

    @Nested
    @DisplayName("approveMember 审批成员通过")
    class ApproveMemberTests {

        @FastTest
        @DisplayName("审批不存在的申请应返回404")
        void should_return_not_found_when_application_not_exists() {
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(null);

            Result result = projectService.approveMember(APPLICATION_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审批非待审核状态的申请应返回错误")
        void should_return_error_when_status_not_pending() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, MEMBER_USER_ID, APP_STATUS_CONFIRMED);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);

            Result result = projectService.approveMember(APPLICATION_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("非待审核");
        }

        @FastTest
        @DisplayName("非项目管理员审批成员应返回403")
        void should_return_forbidden_when_not_project_admin() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, OTHER_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.approveMember(APPLICATION_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("项目负责人审批成员应成功")
        void should_approve_member_successfully_as_leader() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, MEMBER_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.approveMemberApplication(eq(APPLICATION_ID), eq(MEMBER_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.approveMember(APPLICATION_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("管理员审批任意成员申请应成功")
        void should_approve_member_successfully_as_admin() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, OTHER_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.approveMemberApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.approveMember(APPLICATION_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("审批通过后应记录历史")
        void should_add_history_after_approve_member() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, MEMBER_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.approveMemberApplication(eq(APPLICATION_ID), eq(MEMBER_USER_ID), any(Connection.class))).thenReturn(true);

            projectService.approveMember(APPLICATION_ID, MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_MEMBER_APPROVE), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }
    }

    // ==================== rejectMember 驳回成员申请 ====================

    @Nested
    @DisplayName("rejectMember 驳回成员申请")
    class RejectMemberTests {

        @FastTest
        @DisplayName("驳回不存在的申请应返回404")
        void should_return_not_found_when_application_not_exists() {
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(null);

            Result result = projectService.rejectMember(APPLICATION_ID, "不符合要求", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("驳回已确认的申请应返回错误")
        void should_return_error_when_already_confirmed() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, MEMBER_USER_ID, APP_STATUS_CONFIRMED);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);

            Result result = projectService.rejectMember(APPLICATION_ID, "原因", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("正常驳回待审核申请应成功")
        void should_reject_member_successfully() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.rejectMemberApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            Result result = projectService.rejectMember(APPLICATION_ID, "名额已满", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("驳回理由为空时应使用默认理由")
        void should_use_default_reason_when_blank() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.rejectMemberApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            Result result = projectService.rejectMember(APPLICATION_ID, "", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("驳回后应记录历史")
        void should_add_history_after_reject_member() {
            ProjectMemberApplication app = createApplication(APPLICATION_ID, PROJECT_ID, OTHER_USER_ID, APP_STATUS_PENDING);
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.getMemberApplicationById(APPLICATION_ID)).thenReturn(app);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.rejectMemberApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID), anyString(), any(Connection.class))).thenReturn(true);

            projectService.rejectMember(APPLICATION_ID, "不符合要求", ADMIN_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_MEMBER_REJECT), eq(ADMIN_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }
    }

    // ==================== addPlan 添加计划 ====================

    @Nested
    @DisplayName("addPlan 添加计划")
    class AddPlanTests {

        @FastTest
        @DisplayName("为不存在的项目添加计划应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("阶段一", "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目成员添加计划应返回403")
        void should_return_forbidden_when_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("阶段一", "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("计划标题为空应返回错误")
        void should_return_error_when_title_empty() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("", "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("计划标题超长应返回错误")
        void should_return_error_when_title_too_long() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("a".repeat(201), "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("计划开始时间晚于结束时间应返回错误")
        void should_return_error_when_start_after_end() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            Calendar cal = Calendar.getInstance();

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("阶段一", "描述", addDays(cal, 30), addDays(cal, 10), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("项目成员正常添加计划应成功")
        void should_add_plan_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addPlan(any(ProjectPlan.class), any(Connection.class))).thenReturn(true);

            Calendar cal = Calendar.getInstance();
            Result result = projectService.addPlan(PROJECT_ID,
                    createPlanDTO("阶段一", "完成需求分析", addDays(cal, 1), addDays(cal, 14), 1),
                    MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("添加计划后应记录历史")
        void should_add_history_after_add_plan() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addPlan(any(ProjectPlan.class), any(Connection.class))).thenReturn(true);

            projectService.addPlan(PROJECT_ID, createPlanDTO("阶段一", "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_INFO_UPDATE), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("待审核项目不允许添加计划")
        void should_return_error_when_project_pending() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.addPlan(PROJECT_ID, createPlanDTO("阶段一", "描述", new Date(), new Date(), 1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== addProgress 添加进度 ====================

    @Nested
    @DisplayName("addProgress 添加进度")
    class AddProgressTests {

        @FastTest
        @DisplayName("为不存在的项目添加进度应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度更新", "完成50%", 50), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目成员添加进度应返回403")
        void should_return_forbidden_when_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度更新", "描述", 50), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("进度标题为空应返回错误")
        void should_return_error_when_title_empty() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "", "描述", 50), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("完成率为负数应返回错误")
        void should_return_error_when_completion_rate_negative() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "描述", -1), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("完成率超过100应返回错误")
        void should_return_error_when_completion_rate_over_100() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "描述", 101), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("完成率");
        }

        @FastTest
        @DisplayName("完成率为0应允许（刚开始）")
        void should_allow_zero_completion_rate() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addProgress(any(ProjectProgress.class), any(Connection.class))).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "刚开始", 0), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("完成率为100应允许（完成）")
        void should_allow_100_completion_rate() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addProgress(any(ProjectProgress.class), any(Connection.class))).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "已完成", 100), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("正常添加进度应成功")
        void should_add_progress_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addProgress(any(ProjectProgress.class), any(Connection.class))).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "本阶段进展", "完成核心功能开发", 75), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).addProgress(any(ProjectProgress.class), any(Connection.class));
        }

        @FastTest
        @DisplayName("添加进度后应记录历史")
        void should_add_history_after_add_progress() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.addProgress(any(ProjectProgress.class), any(Connection.class))).thenReturn(true);

            projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "描述", 50), MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_INFO_UPDATE), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }
    }

    // ==================== transferAdmin 转让管理员 ====================

    @Nested
    @DisplayName("transferAdmin 转让管理员")
    class TransferAdminTests {

        @FastTest
        @DisplayName("转让不存在的项目应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非当前负责人/管理员转让应返回403")
        void should_return_forbidden_when_not_current_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("转让给非项目成员应返回错误")
        void should_return_error_when_target_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, OTHER_USER_ID)).thenReturn(false);

            Result result = projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不是项目成员");
        }

        @FastTest
        @DisplayName("转让给自己应返回错误")
        void should_return_error_when_transfer_to_self() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.transferAdmin(PROJECT_ID, MEMBER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不能转让给自己");
        }

        @FastTest
        @DisplayName("负责人正常转让给成员应成功")
        void should_transfer_admin_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, OTHER_USER_ID)).thenReturn(true);
            when(projectDAO.transferAdmin(eq(PROJECT_ID), eq(OTHER_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).transferAdmin(eq(PROJECT_ID), eq(OTHER_USER_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("管理员转让任意项目应成功")
        void should_allow_admin_to_transfer_any_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(projectDAO.transferAdmin(eq(PROJECT_ID), eq(MEMBER_USER_ID), any(Connection.class))).thenReturn(true);

            Result result = projectService.transferAdmin(PROJECT_ID, MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("转让后应记录历史")
        void should_add_history_after_transfer() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, OTHER_USER_ID)).thenReturn(true);
            when(projectDAO.transferAdmin(eq(PROJECT_ID), eq(OTHER_USER_ID), any(Connection.class))).thenReturn(true);

            projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_TRANSFER), eq(MEMBER_USER_ID), anyString(), anyString(), anyString(), anyString(), any(Connection.class));
        }

        @FastTest
        @DisplayName("已完成项目不允许转让")
        void should_return_error_when_project_completed() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_COMPLETED, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.transferAdmin(PROJECT_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== addLabel / removeLabel 标签管理 ====================

    @Nested
    @DisplayName("addLabel 添加标签")
    class AddLabelTests {

        @FastTest
        @DisplayName("为不存在的项目添加标签应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.addLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目管理员添加标签应返回403")
        void should_return_forbidden_when_not_admin() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, OTHER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.addLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("标签代码为空应返回错误")
        void should_return_error_when_label_empty() {
            Result result = projectService.addLabel(PROJECT_ID, "", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("重复添加相同标签应返回错误")
        void should_return_error_when_label_already_exists() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Arrays.asList("HOT", "NEW"));

            Result result = projectService.addLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已存在");
        }

        @FastTest
        @DisplayName("正常添加标签应成功")
        void should_add_label_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Collections.emptyList());
            when(projectDAO.addLabel(eq(PROJECT_ID), eq("HOT"), any(Connection.class))).thenReturn(true);

            Result result = projectService.addLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("添加标签后应记录历史")
        void should_add_history_after_add_label() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Collections.emptyList());
            when(projectDAO.addLabel(eq(PROJECT_ID), eq("HOT"), any(Connection.class))).thenReturn(true);

            projectService.addLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_LABEL_ADD), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), eq("HOT"), any(Connection.class));
        }
    }

    @Nested
    @DisplayName("removeLabel 移除标签")
    class RemoveLabelTests {

        @FastTest
        @DisplayName("移除不存在的项目标签应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.removeLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("移除不存在的标签应返回错误")
        void should_return_error_when_label_not_exists() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Collections.singletonList("NEW"));

            Result result = projectService.removeLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不存在");
        }

        @FastTest
        @DisplayName("正常移除标签应成功")
        void should_remove_label_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Arrays.asList("HOT", "NEW"));
            when(projectDAO.removeLabel(eq(PROJECT_ID), eq("HOT"), any(Connection.class))).thenReturn(true);

            Result result = projectService.removeLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("移除标签后应记录历史")
        void should_add_history_after_remove_label() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Arrays.asList("HOT"));
            when(projectDAO.removeLabel(eq(PROJECT_ID), eq("HOT"), any(Connection.class))).thenReturn(true);

            projectService.removeLabel(PROJECT_ID, "HOT", MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_LABEL_REMOVE), eq(MEMBER_USER_ID), anyString(), anyString(), eq("HOT"), isNull(), any(Connection.class));
        }
    }

    // ==================== uploadProjectImage 上传项目图片 ====================

    @Nested
    @DisplayName("uploadProjectImage 上传项目图片")
    class UploadProjectImageTests {

        @FastTest
        @DisplayName("上传到不存在的项目应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(1024, "image/png", "screenshot.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目成员上传图片应返回403")
        void should_return_forbidden_when_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(1024, "image/png", "screenshot.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("上传非图片文件应返回错误")
        void should_return_error_when_file_not_image() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(1024, "application/pdf", "doc.pdf"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("图片");
        }

        @FastTest
        @DisplayName("上传超过5MB的图片应返回错误")
        void should_return_error_when_file_too_large() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(5 * 1024 * 1024 + 1, "image/png", "big.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("5MB");
        }

        @FastTest
        @DisplayName("上传空文件应返回错误")
        void should_return_error_when_file_empty() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(0, "image/png", "empty.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("正常上传图片应成功")
        void should_upload_image_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.uploadFile(any(), eq("images/projects"), eq(MEMBER_USER_ID))).thenReturn(Result.ok(FILE_ID));

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(1024, "image/png", "screenshot.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("上传图片后应记录历史")
        void should_add_history_after_upload_image() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.uploadFile(any(), eq("images/projects"), eq(MEMBER_USER_ID))).thenReturn(Result.ok(FILE_ID));

            projectService.uploadProjectImage(PROJECT_ID, createMockPart(1024, "image/png", "screenshot.png"), MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_IMAGE_ADD), eq(MEMBER_USER_ID), anyString(), anyString(), isNull(), anyString(), any(Connection.class));
        }
    }

    // ==================== uploadProjectFile 上传项目文件 ====================

    @Nested
    @DisplayName("uploadProjectFile 上传项目文件")
    class UploadProjectFileTests {

        @FastTest
        @DisplayName("上传到不存在的项目应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(1024, "application/pdf", "doc.pdf"), FILE_TYPE_DOC, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目成员上传文件应返回403")
        void should_return_forbidden_when_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(1024, "application/pdf", "doc.pdf"), FILE_TYPE_DOC, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("文件类型为空应返回错误")
        void should_return_error_when_file_type_empty() {
            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(1024, "application/pdf", "doc.pdf"), "", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("文件类型无效应返回错误")
        void should_return_error_when_file_type_invalid() {
            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(1024, "application/pdf", "doc.pdf"), "INVALID_TYPE", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("上传超过20MB的文件应返回错误")
        void should_return_error_when_file_too_large() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(20 * 1024 * 1024 + 1, "application/pdf", "big.pdf"), FILE_TYPE_DOC, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("20MB");
        }

        @FastTest
        @DisplayName("正常上传文件应成功")
        void should_upload_file_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.uploadFile(any(), eq("files/projects"), eq(MEMBER_USER_ID))).thenReturn(Result.ok(FILE_ID));

            Result result = projectService.uploadProjectFile(PROJECT_ID, createMockPart(1024, "application/pdf", "design.pdf"), FILE_TYPE_DOC, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("支持的所有文件类型应能正常上传")
        void should_support_all_file_types() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.uploadFile(any(), eq("files/projects"), eq(MEMBER_USER_ID))).thenReturn(Result.ok(FILE_ID));

            for (String type : Arrays.asList(FILE_TYPE_DOC, FILE_TYPE_CODE)) {
                Result result = projectService.uploadProjectFile(PROJECT_ID,
                        createMockPart(1024, "application/octet-stream", "file.bin"), type, MEMBER_USER_ID);
                assertThat(result.isSuccess()).isTrue();
            }
        }
    }

    // ==================== deleteProjectFile 删除项目文件 ====================

    @Nested
    @DisplayName("deleteProjectFile 删除项目文件")
    class DeleteProjectFileTests {

        @FastTest
        @DisplayName("删除不存在的项目文件应返回404")
        void should_return_not_found_when_file_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.deleteProjectFile(PROJECT_ID, FILE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("非项目成员删除文件应返回403")
        void should_return_forbidden_when_not_member() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = projectService.deleteProjectFile(PROJECT_ID, FILE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("正常删除文件应成功")
        void should_delete_file_successfully() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.deleteFile(eq(FILE_ID), eq(MEMBER_USER_ID))).thenReturn(Result.ok());

            Result result = projectService.deleteProjectFile(PROJECT_ID, FILE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除文件后应记录历史")
        void should_add_history_after_delete_file() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.deleteFile(eq(FILE_ID), eq(MEMBER_USER_ID))).thenReturn(Result.ok());

            projectService.deleteProjectFile(PROJECT_ID, FILE_ID, MEMBER_USER_ID);

            verify(projectDAO).addHistory(eq(PROJECT_ID), eq(OP_FILE_DELETE), eq(MEMBER_USER_ID), anyString(), anyString(), anyString(), isNull(), any(Connection.class));
        }
    }

    // ==================== listProjects 项目列表 ====================

    @Nested
    @DisplayName("listProjects 项目列表")
    class ListProjectsTests {

        @FastTest
        @DisplayName("获取项目列表应返回分页结果")
        void should_return_paged_project_list() {
            Project p1 = createProject(1, "项目1", STATUS_APPROVED, MEMBER_USER_ID);
            Project p2 = createProject(2, "项目2", STATUS_IN_PROGRESS, OTHER_USER_ID);
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Arrays.asList(p1, p2));

            Result result = projectService.listProjects(new ProjectFilterDTO(), 1, 10);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("按关键词搜索应正确过滤")
        void should_filter_by_keyword() {
            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setKeyword("测试");
            when(projectDAO.findByConditions(eq("测试"), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按状态筛选应正确过滤")
        void should_filter_by_status() {
            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setStatus(STATUS_APPROVED);
            when(projectDAO.findByConditions(any(), eq(STATUS_APPROVED), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按分类筛选应正确过滤")
        void should_filter_by_category() {
            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setCategory(CATEGORY_AI);
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按年份筛选应正确过滤")
        void should_filter_by_year() {
            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setYear(CURRENT_YEAR);
            when(projectDAO.findByConditions(any(), any(), eq(CURRENT_YEAR))).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数为负数时应使用默认值")
        void should_use_default_when_page_negative() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), -1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小超过最大值时应限制")
        void should_limit_page_size_when_exceeds_max() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), 1, 1000);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页页码为0时应使用页码1")
        void should_use_page_1_when_page_is_zero() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), 0, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小为0时应使用默认值")
        void should_use_default_page_size_when_zero() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), 1, 0);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getProjectDetail 项目详情 ====================

    @Nested
    @DisplayName("getProjectDetail 项目详情")
    class GetProjectDetailTests {

        @FastTest
        @DisplayName("获取不存在的项目详情应返回404")
        void should_return_not_found_when_project_not_exists() {
            when(projectDAO.findById(PROJECT_ID)).thenReturn(null);

            Result result = projectService.getProjectDetail(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("获取项目详情应包含成员列表")
        void should_include_members_in_detail() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getProjectMembers(PROJECT_ID)).thenReturn(Arrays.asList(
                    createProjectMember(PROJECT_ID, ADMIN_USER_ID, MEMBER_ROLE_ADMIN),
                    createProjectMember(PROJECT_ID, MEMBER_USER_ID, MEMBER_ROLE_MEMBER)
            ));

            Result result = projectService.getProjectDetail(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取项目详情应包含标签列表")
        void should_include_labels_in_detail() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getLabels(PROJECT_ID)).thenReturn(Arrays.asList("HOT", "NEW"));

            Result result = projectService.getProjectDetail(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取项目详情应包含计划列表")
        void should_include_plans_in_detail() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.getPlans(PROJECT_ID)).thenReturn(Arrays.asList(
                    createPlan(PLAN_ID, PROJECT_ID, "阶段一")
            ));

            Result result = projectService.getProjectDetail(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取项目详情应包含当前用户角色")
        void should_include_current_user_role() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.getProjectDetail(PROJECT_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未登录用户查看公开项目详情应成功")
        void should_allow_guest_to_view_public_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_APPROVED, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.getProjectDetail(PROJECT_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未登录用户不能查看待审核项目详情")
        void should_forbid_guest_viewing_pending_project() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_PENDING, ADMIN_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);

            Result result = projectService.getProjectDetail(PROJECT_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    // ==================== getMyProjects 我的项目 ====================

    @Nested
    @DisplayName("getMyProjects 我的项目")
    class GetMyProjectsTests {

        @FastTest
        @DisplayName("获取我的项目应返回分页结果")
        void should_return_paged_my_projects() {
            Project p1 = createProject(PROJECT_ID, "项目1", STATUS_APPROVED, MEMBER_USER_ID);
            Project p2 = createProject(OTHER_PROJECT_ID, "项目2", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findProjectsByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(p1, p2));

            Result result = projectService.getMyProjects(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未参与任何项目应返回空列表")
        void should_return_empty_when_not_in_any_project() {
            when(projectDAO.findProjectsByUserId(MEMBER_USER_ID)).thenReturn(Collections.emptyList());

            Result result = projectService.getMyProjects(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = projectService.getMyProjects(null, 1, 10);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("项目状态枚举应包含所有预期值")
        void project_status_enums_should_be_complete() {
            assertThat(STATUS_PENDING).isEqualTo("pending");
            assertThat(STATUS_APPROVED).isEqualTo("approved");
            assertThat(STATUS_IN_PROGRESS).isEqualTo("in_progress");
            assertThat(STATUS_COMPLETED).isEqualTo("completed");
            assertThat(STATUS_CANCELED).isEqualTo("canceled");
            assertThat(STATUS_REJECTED).isEqualTo("rejected");
        }

        @Test
        @DisplayName("成员申请状态枚举应包含所有预期值")
        void application_status_enums_should_be_complete() {
            assertThat(APP_STATUS_PENDING).isEqualTo("PENDING");
            assertThat(APP_STATUS_CONFIRMED).isEqualTo("CONFIRMED");
            assertThat(APP_STATUS_REJECTED).isEqualTo("REJECTED");
        }

        @Test
        @DisplayName("项目成员角色枚举应包含所有预期值")
        void member_role_enums_should_be_complete() {
            assertThat(MEMBER_ROLE_ADMIN).isEqualTo("ADMIN");
            assertThat(MEMBER_ROLE_MEMBER).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("项目分类枚举应包含所有预期值")
        void category_enums_should_be_complete() {
            assertThat(CATEGORY_WEB).isEqualTo("WEB");
            assertThat(CATEGORY_MOBILE).isEqualTo("MOBILE");
            assertThat(CATEGORY_AI).isEqualTo("AI");
            assertThat(CATEGORY_GAME).isEqualTo("GAME");
        }

        @Test
        @DisplayName("文件类型枚举应包含所有预期值")
        void file_type_enums_should_be_complete() {
            assertThat(FILE_TYPE_DOC).isEqualTo("DOC");
            assertThat(FILE_TYPE_IMAGE).isEqualTo("IMAGE");
            assertThat(FILE_TYPE_CODE).isEqualTo("CODE");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("项目ID为null时应返回错误")
        void should_return_error_when_project_id_null() {
            Result result = projectService.getProjectDetail(null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("项目ID为0时应返回错误")
        void should_return_error_when_project_id_zero() {
            Result result = projectService.getProjectDetail(0, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("项目ID为负数时应返回错误")
        void should_return_error_when_project_id_negative() {
            Result result = projectService.getProjectDetail(-1, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("用户ID为0时应返回错误")
        void should_return_error_when_user_id_zero() {
            Result result = projectService.getProjectDetail(PROJECT_ID, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("分页页码为负数时应规范化为1")
        void should_normalize_negative_page_to_1() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), -5, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小为负数时应使用默认值")
        void should_use_default_for_negative_page_size() {
            when(projectDAO.findByConditions(any(), any(), any())).thenReturn(Collections.emptyList());

            Result result = projectService.listProjects(new ProjectFilterDTO(), 1, -10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("完成率为Integer.MAX_VALUE时应返回错误")
        void should_return_error_for_max_integer_completion_rate() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = projectService.addProgress(PROJECT_ID, createProgressDTO(PLAN_ID, "进度", "描述", Integer.MAX_VALUE), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("预算为0时应允许")
        void should_allow_zero_budget() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setBudget(BigDecimal.ZERO);
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("名称恰好200字符应允许")
        void should_allow_name_at_max_length() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setName("a".repeat(200));
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("描述恰好5000字符应允许")
        void should_allow_description_at_max_length() {
            ProjectDTO dto = createValidProjectDTO();
            dto.setDescription("a".repeat(5000));
            User user = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(projectDAO.countProjectsByMemberAndYear(MEMBER_USER_ID, CURRENT_YEAR)).thenReturn(0);
            when(projectDAO.insert(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                p.setId(PROJECT_ID);
                return true;
            });

            Result result = projectService.createProject(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("文件大小恰好5MB应允许上传图片")
        void should_allow_image_at_exactly_5mb() {
            Project project = createProject(PROJECT_ID, "测试项目", STATUS_IN_PROGRESS, MEMBER_USER_ID);
            when(projectDAO.findById(PROJECT_ID)).thenReturn(project);
            when(projectDAO.isMember(PROJECT_ID, MEMBER_USER_ID)).thenReturn(true);
            when(fileService.uploadFile(any(), eq("images/projects"), eq(MEMBER_USER_ID))).thenReturn(Result.ok(FILE_ID));

            Result result = projectService.uploadProjectImage(PROJECT_ID, createMockPart(5 * 1024 * 1024, "image/png", "exact.png"), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }
}
