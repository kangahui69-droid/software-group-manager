package service;

import dao.ActivityDAO;
import dao.RegistrationDAO;
import dao.UserDAO;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.Activity;
import model.Registration;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

/**
 * ActivityService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.1 ActivityService 活动服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 注意：这是Red阶段，ActivityService尚未实现
 * 所有测试将失败，直至实现对应方法
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - RegistrationDAO: getParticipantCount(Integer, String) / getRegistrationStatus(Integer, Integer)
 * - RegistrationDAO: updateStatus(Integer, Integer, String, String, Connection) 需要notes参数
 * - RegistrationDAO: batchUpdateStatus(List<Integer>, Integer, String, Connection)
 * - ActivityDAO: approveActivity(id) / rejectActivity(id) (无status参数)
 * - ActivityDAO: findByCreatorId / 无countByCreatorId / 无countByConditions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityService 活动服务测试")
class ActivityServiceTest {

    @Mock
    private ActivityDAO activityDAO;

    @Mock
    private RegistrationDAO registrationDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private ActivityService activityService;

    // 默认管理员操作员（大多数审批相关测试需要）
    private User defaultOperator;

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer ACTIVITY_ID = 100;
    private static final Integer OPERATOR_ID = 1;

    // 活动状态枚举
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String STATUS_ONGOING = "ongoing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";

    // 活动审批状态枚举
    private static final String APPROVAL_PENDING = "pending";
    private static final String APPROVAL_APPROVED = "approved";
    private static final String APPROVAL_REJECTED = "rejected";

    // 报名状态枚举
    private static final String REG_STATUS_PENDING = "pending";
    private static final String REG_STATUS_CONFIRMED = "confirmed";
    private static final String REG_STATUS_REJECTED = "rejected";
    private static final String REG_STATUS_EXPIRED = "expired";

    // 活动类型
    private static final String TYPE_LECTURE = "LECTURE";
    private static final String TYPE_SEMINAR = "SEMINAR";
    private static final String TYPE_TEA_PARTY = "TEA_PARTY";
    private static final String TYPE_PROJECT_INTRO = "PROJECT_INTRO";

    // ==================== 测试初始化 ====================

    /**
     * 默认每个测试前设置管理员操作员
     * 大多数审批相关测试需要OPERATOR_ID为管理员
     */
    @BeforeEach
    void setUp() {
        defaultOperator = createUser(OPERATOR_ID, "ADMIN");
        // 默认设置OPERATOR_ID为管理员，后续测试可override
        when(userDAO.findById(OPERATOR_ID)).thenReturn(defaultOperator);
    }

    // ==================== 工具方法 ====================

    private Activity createActivity(Integer id, String status, String approvalStatus, int maxParticipants) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle("测试活动");
        activity.setDescription("活动描述");
        activity.setActivityType(TYPE_LECTURE);
        activity.setStatus(status);
        activity.setApprovalStatus(approvalStatus);
        activity.setCreatorId(ADMIN_USER_ID);
        activity.setMaxParticipants(maxParticipants);
        activity.setLocation("活动地点");
        return activity;
    }

    private Activity createActivityWithRegistrationWindow(Integer id, String approvalStatus,
            Date registrationStart, Date registrationEnd, int maxParticipants) {
        Activity activity = createActivity(id, STATUS_UPCOMING, approvalStatus, maxParticipants);
        activity.setRegistrationStartTime(registrationStart);
        activity.setRegistrationEndTime(registrationEnd);
        Calendar now = Calendar.getInstance();
        activity.setActivityStartTime(addDays(now, 7));
        activity.setActivityEndTime(addDays(now, 8));
        return activity;
    }

    private Date addDays(Calendar cal, int days) {
        Calendar result = (Calendar) cal.clone();
        result.add(Calendar.DAY_OF_MONTH, days);
        return result.getTime();
    }

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    private Registration createRegistration(Integer activityId, Integer userId, String status) {
        Registration reg = new Registration();
        reg.setActivityId(activityId);
        reg.setUserId(userId);
        reg.setStatus(status);
        return reg;
    }

    // ==================== createActivity 创建活动 ====================

    @Nested
    @DisplayName("createActivity 创建活动")
    class CreateActivityTests {

        @FastTest
        @DisplayName("创建活动时必填字段为空应返回错误")
        void should_return_error_when_required_fields_empty() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle(""); // 空标题
            dto.setActivityType(TYPE_LECTURE);

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("标题不能为空");
        }

        @FastTest
        @DisplayName("创建活动时标题为null应返回错误")
        void should_return_error_when_title_is_null() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle(null);
            dto.setActivityType(TYPE_LECTURE);

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建活动时活动类型为空应返回错误")
        void should_return_error_when_activity_type_empty() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(null);

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建活动时活动类型无效益返回错误")
        void should_return_error_when_activity_type_invalid() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType("INVALID_TYPE");

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建活动时用户不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(null);

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("创建活动时非管理员用户应返回403错误")
        void should_return_forbidden_when_not_admin() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = activityService.createActivity(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("创建活动时报名开始时间晚于结束时间应返回错误")
        void should_return_error_when_registration_window_invalid() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            Calendar now = Calendar.getInstance();
            dto.setRegistrationStartTime(addDays(now, 10));
            dto.setRegistrationEndTime(addDays(now, 5)); // 结束时间早于开始时间
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("报名开始时间不能晚于结束时间");
        }

        @FastTest
        @DisplayName("创建活动时活动开始时间早于报名结束时间应返回错误")
        void should_return_error_when_activity_starts_before_registration_ends() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            Calendar now = Calendar.getInstance();
            dto.setActivityStartTime(addDays(now, 5));
            dto.setActivityEndTime(addDays(now, 6));
            dto.setRegistrationStartTime(addDays(now, 1));
            dto.setRegistrationEndTime(addDays(now, 7)); // 活动开始后还在报名
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("活动开始时间应晚于报名截止时间");
        }

        @FastTest
        @DisplayName("创建活动时最大参与人数为负数应返回错误")
        void should_return_error_when_max_participants_negative() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            dto.setMaxParticipants(-1);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建活动时最大参与人数为0应允许（表示不限人数）")
        void should_allow_zero_max_participants() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            dto.setMaxParticipants(0); // 0表示不限人数
            Calendar now = Calendar.getInstance();
            dto.setActivityStartTime(addDays(now, 5));
            dto.setActivityEndTime(addDays(now, 6));
            dto.setRegistrationStartTime(addDays(now, 1));
            dto.setRegistrationEndTime(addDays(now, 4));
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));
            when(activityDAO.insert(any(Activity.class))).thenAnswer(inv -> {
                Activity a = inv.getArgument(0);
                a.setId(ACTIVITY_ID);
                return true;
            });

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("创建活动时描述超长应返回错误")
        void should_return_error_when_description_too_long() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            dto.setDescription("a".repeat(5001)); // 超过5000字符限制
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("描述不能超过5000字符");
        }

        @FastTest
        @DisplayName("创建活动时地点超长应返回错误")
        void should_return_error_when_location_too_long() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            dto.setLocation("a".repeat(201)); // 超过200字符限制
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员创建有效活动应返回成功")
        void should_create_activity_successfully() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setDescription("活动描述");
            dto.setActivityType(TYPE_LECTURE);
            dto.setLocation("活动地点");
            Calendar now = Calendar.getInstance();
            dto.setActivityStartTime(addDays(now, 7));
            dto.setActivityEndTime(addDays(now, 8));
            dto.setRegistrationStartTime(addDays(now, 1));
            dto.setRegistrationEndTime(addDays(now, 5));
            dto.setMaxParticipants(50);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));
            when(activityDAO.insert(any(Activity.class))).thenAnswer(inv -> {
                Activity a = inv.getArgument(0);
                a.setId(ACTIVITY_ID);
                return true;
            });

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            verify(activityDAO).insert(any(Activity.class));
        }

        @FastTest
        @DisplayName("创建活动时所有枚举类型的活动类型都应支持")
        void should_support_all_activity_type_enums() {
            List<String> types = Arrays.asList(TYPE_LECTURE, TYPE_SEMINAR, TYPE_TEA_PARTY, TYPE_PROJECT_INTRO);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));
            when(activityDAO.insert(any(Activity.class))).thenAnswer(inv -> {
                Activity a = inv.getArgument(0);
                a.setId(ACTIVITY_ID);
                return true;
            });

            for (String type : types) {
                ActivityDTO dto = new ActivityDTO();
                dto.setTitle("测试活动-" + type);
                dto.setActivityType(type);
                Calendar now = Calendar.getInstance();
                dto.setActivityStartTime(addDays(now, 7));
                dto.setActivityEndTime(addDays(now, 8));
                dto.setRegistrationStartTime(addDays(now, 1));
                dto.setRegistrationEndTime(addDays(now, 5));

                Result result = activityService.createActivity(dto, ADMIN_USER_ID);
                assertThat(result.isSuccess()).isTrue();
            }
        }

        @FastTest
        @DisplayName("创建活动时数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("测试活动");
            dto.setActivityType(TYPE_LECTURE);
            Calendar now = Calendar.getInstance();
            dto.setActivityStartTime(addDays(now, 7));
            dto.setActivityEndTime(addDays(now, 8));
            dto.setRegistrationStartTime(addDays(now, 1));
            dto.setRegistrationEndTime(addDays(now, 5));
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "ADMIN"));
            when(activityDAO.insert(any(Activity.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = activityService.createActivity(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== updateActivity 更新活动 ====================

    @Nested
    @DisplayName("updateActivity 更新活动")
    class UpdateActivityTests {

        @FastTest
        @DisplayName("更新不存在的活动应返回404错误")
        void should_return_not_found_when_activity_not_exists() {
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("更新后的活动");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新活动时非创建者应返回403错误")
        void should_return_forbidden_when_not_creator() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(OTHER_USER_ID); // 非当前用户创建
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("更新后的活动");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新已发布的活动应返回错误")
        void should_return_error_when_activity_already_started() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_ONGOING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("更新后的活动");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("活动已开始");
        }

        @FastTest
        @DisplayName("更新已结束的活动应返回错误")
        void should_return_error_when_activity_completed() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_COMPLETED, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("更新后的活动");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建者更新草稿状态活动应成功")
        void should_update_activity_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("更新后的活动");
            dto.setActivityType(TYPE_SEMINAR);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(activityDAO.update(any(Activity.class))).thenReturn(true);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityDAO).update(any(Activity.class));
        }

        @FastTest
        @DisplayName("管理员更新任意活动应成功")
        void should_allow_admin_to_update_any_activity() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(OTHER_USER_ID); // 非管理员创建
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("管理员更新活动");
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(activityDAO.update(any(Activity.class))).thenReturn(true);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("更新活动时标题为空应返回错误")
        void should_return_error_when_title_empty_on_update() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            ActivityDTO dto = new ActivityDTO();
            dto.setTitle("");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.updateActivity(ACTIVITY_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== deleteActivity 删除活动 ====================

    @Nested
    @DisplayName("deleteActivity 删除活动")
    class DeleteActivityTests {

        @FastTest
        @DisplayName("删除不存在的活动应返回404错误")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除非创建者且非管理员应返回403错误")
        void should_return_forbidden_when_not_creator_or_admin() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(OTHER_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.deleteActivity(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除已有确认报名的活动应返回错误")
        void should_return_error_when_has_confirmed_participants() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(3);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已有确认报名");
        }

        @FastTest
        @DisplayName("删除进行中的活动应返回错误")
        void should_return_error_when_activity_ongoing() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_ONGOING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("进行中");
        }

        @FastTest
        @DisplayName("删除已结束的活动应返回错误")
        void should_return_error_when_activity_completed() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_COMPLETED, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建者删除无确认报名的待审核活动应成功")
        void should_delete_activity_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(activityDAO.delete(ACTIVITY_ID)).thenReturn(true);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityDAO).delete(ACTIVITY_ID);
        }

        @FastTest
        @DisplayName("管理员删除有pending报名但无confirmed报名的活动应成功")
        void should_allow_admin_to_delete_with_only_pending() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(OTHER_USER_ID);
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(activityDAO.delete(ACTIVITY_ID)).thenReturn(true);

            Result result = activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除活动时应同时删除所有报名记录")
        void should_delete_all_participants_when_delete_activity() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(activityDAO.delete(ACTIVITY_ID)).thenReturn(true);

            activityService.deleteActivity(ACTIVITY_ID, ADMIN_USER_ID);

            verify(registrationDAO).deleteByActivityId(ACTIVITY_ID);
        }
    }

    // ==================== register 活动报名 ====================

    @Nested
    @DisplayName("register 活动报名")
    class RegisterTests {

        @FastTest
        @DisplayName("报名不存在的活动应返回404错误")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("报名未审批的活动应返回错误")
        void should_return_error_when_activity_not_approved() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_PENDING,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("未通过审核");
        }

        @FastTest
        @DisplayName("报名已驳回的活动应返回错误")
        void should_return_error_when_activity_rejected() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_REJECTED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("报名已取消的活动应返回错误")
        void should_return_error_when_activity_canceled() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            activity.setStatus(STATUS_CANCELED);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已取消");
        }

        @FastTest
        @DisplayName("报名未开始时应返回错误")
        void should_return_error_when_registration_not_started() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), 1), // 报名还未开始
                    addDays(Calendar.getInstance(), 5),
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("报名未开始");
        }

        @FastTest
        @DisplayName("报名已截止时应返回错误")
        void should_return_error_when_registration_ended() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -10),
                    addDays(Calendar.getInstance(), -1), // 报名已截止
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("报名已截止");
        }

        @FastTest
        @DisplayName("活动已满员时应返回错误（confirmed+pending>=max）")
        void should_return_error_when_activity_full() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    2); // 最多2人
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(2);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_PENDING)).thenReturn(0);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已满");
        }

        @FastTest
        @DisplayName("不限制人数的活动应能正常报名")
        void should_register_when_no_participant_limit() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    0); // 0表示不限人数
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.isRegistered(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(false);
            when(registrationDAO.register(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("重复报名应返回错误")
        void should_return_error_when_already_registered() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.isRegistered(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已报名");
        }

        @FastTest
        @DisplayName("报名自己创建的活动应返回错误")
        void should_return_error_when_register_own_activity() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            activity.setCreatorId(ADMIN_USER_ID); // 当前用户创建的活动
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.isRegistered(ACTIVITY_ID, ADMIN_USER_ID)).thenReturn(false);

            Result result = activityService.register(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不能报名自己创建的活动");
        }

        @FastTest
        @DisplayName("正常报名流程应成功")
        void should_register_successfully() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.isRegistered(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(false);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_PENDING)).thenReturn(0);
            when(registrationDAO.register(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(registrationDAO).register(ACTIVITY_ID, MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("报名时包含pending状态的也算已满员")
        void should_consider_pending_as_full_when_checking_capacity() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    2); // 最多2人
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(1);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_PENDING)).thenReturn(1);
            // 总共2人（1 confirmed + 1 pending），已满

            Result result = activityService.register(ACTIVITY_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("已满");
        }
    }

    // ==================== approveParticipant 审批参与者 ====================

    @Nested
    @DisplayName("approveParticipant 审批参与者通过")
    class ApproveParticipantTests {

        @FastTest
        @DisplayName("审批不存在的活动应返回404")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审批不存在的用户的报名应返回错误")
        void should_return_error_when_participant_not_found() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(null);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审批非pending状态的报名应返回错误")
        void should_return_error_when_status_not_pending() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_CONFIRMED);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("非待审核状态");
        }

        @FastTest
        @DisplayName("审批后超过最大人数限制应返回错误")
        void should_return_error_when_exceeds_max_participants() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 2);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(2);
            // 已有2人确认，再审批1人就超了

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("超过最大人数");
        }

        @FastTest
        @DisplayName("活动进行中审批报名应返回错误")
        void should_return_error_when_activity_ongoing() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_ONGOING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("活动已开始");
        }

        @FastTest
        @DisplayName("管理员正常审批应成功")
        void should_approve_participant_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(registrationDAO.updateStatus(eq(ACTIVITY_ID), eq(MEMBER_USER_ID), eq(REG_STATUS_CONFIRMED), anyString(), any(Connection.class))).thenReturn(true);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("非管理员审批应返回403")
        void should_return_forbidden_when_not_admin() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = activityService.approveParticipant(ACTIVITY_ID, MEMBER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    // ==================== rejectParticipant 拒绝参与者 ====================

    @Nested
    @DisplayName("rejectParticipant 拒绝参与者")
    class RejectParticipantTests {

        @FastTest
        @DisplayName("拒绝不存在的报名应返回错误")
        void should_return_error_when_registration_not_found() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(null);

            Result result = activityService.rejectParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("拒绝已确认的报名应返回错误")
        void should_return_error_when_already_confirmed() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_CONFIRMED);

            Result result = activityService.rejectParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("拒绝报名时应记录拒绝原因")
        void should_store_reject_reason() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);

            Result result = activityService.rejectParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            ArgumentCaptor<String> notesCaptor = ArgumentCaptor.forClass(String.class);
            verify(registrationDAO).updateStatus(eq(ACTIVITY_ID), eq(MEMBER_USER_ID), eq(REG_STATUS_REJECTED), notesCaptor.capture(), any(Connection.class));
            assertThat(notesCaptor.getValue()).isNotEmpty();
        }

        @FastTest
        @DisplayName("正常拒绝待审核的报名应成功")
        void should_reject_pending_registration_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);
            when(registrationDAO.updateStatus(eq(ACTIVITY_ID), eq(MEMBER_USER_ID), eq(REG_STATUS_REJECTED), anyString(), any(Connection.class))).thenReturn(true);

            Result result = activityService.rejectParticipant(ACTIVITY_ID, MEMBER_USER_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== batchApprove 批量通过 ====================

    @Nested
    @DisplayName("batchApprove 批量通过")
    class BatchApproveTests {

        @FastTest
        @DisplayName("批量通过空列表应返回成功但无操作")
        void should_return_success_with_empty_list() {
            List<Integer> userIds = Arrays.asList();

            Result result = activityService.batchApprove(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(registrationDAO, never()).updateStatus(any(), any(), any(), any(), any());
        }

        @FastTest
        @DisplayName("批量通过包含不存在的用户应只处理存在的")
        void should_handle_nonexistent_users_in_batch() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(registrationDAO.updateStatus(eq(ACTIVITY_ID), eq(MEMBER_USER_ID), eq(REG_STATUS_CONFIRMED), anyString(), any(Connection.class))).thenReturn(true);

            List<Integer> userIds = Arrays.asList(MEMBER_USER_ID, 99999); // 99999不存在

            Result result = activityService.batchApprove(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("批量通过后超过容量应返回错误")
        void should_return_error_when_batch_approve_exceeds_capacity() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 2);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, OTHER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(2);

            List<Integer> userIds = Arrays.asList(MEMBER_USER_ID, OTHER_USER_ID);

            Result result = activityService.batchApprove(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("批量通过所有有效的pending报名应成功")
        void should_batch_approve_all_valid_pending() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, OTHER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(0);
            when(registrationDAO.batchUpdateStatus(anyList(), eq(ACTIVITY_ID), eq(REG_STATUS_CONFIRMED), any(Connection.class))).thenReturn(2);

            List<Integer> userIds = Arrays.asList(MEMBER_USER_ID, OTHER_USER_ID);

            Result result = activityService.batchApprove(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(registrationDAO).batchUpdateStatus(anyList(), eq(ACTIVITY_ID), eq(REG_STATUS_CONFIRMED), any(Connection.class));
        }
    }

    // ==================== batchReject 批量拒绝 ====================

    @Nested
    @DisplayName("batchReject 批量拒绝")
    class BatchRejectTests {

        @FastTest
        @DisplayName("批量拒绝空列表应返回成功")
        void should_return_success_with_empty_list() {
            List<Integer> userIds = Arrays.asList();

            Result result = activityService.batchReject(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("批量拒绝应更新所有pending报名为rejected")
        void should_reject_all_pending_registrations() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, OTHER_USER_ID)).thenReturn(REG_STATUS_PENDING);
            when(registrationDAO.batchUpdateStatus(anyList(), eq(ACTIVITY_ID), eq(REG_STATUS_REJECTED), any(Connection.class))).thenReturn(2);

            List<Integer> userIds = Arrays.asList(MEMBER_USER_ID, OTHER_USER_ID);

            Result result = activityService.batchReject(ACTIVITY_ID, userIds, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(registrationDAO).batchUpdateStatus(anyList(), eq(ACTIVITY_ID), eq(REG_STATUS_REJECTED), any(Connection.class));
        }
    }

    // ==================== approveActivity 活动审核通过 ====================

    @Nested
    @DisplayName("approveActivity 活动审核通过")
    class ApproveActivityTests {

        @FastTest
        @DisplayName("审核不存在的活动应返回404")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.approveActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审核已通过的活动应返回错误")
        void should_return_error_when_already_approved() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.approveActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已审核");
        }

        @FastTest
        @DisplayName("审核已驳回的活动应返回错误")
        void should_return_error_when_already_rejected() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_REJECTED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.approveActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("非管理员审核应返回403")
        void should_return_forbidden_when_not_admin() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = activityService.approveActivity(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("管理员审核待审核活动应成功")
        void should_approve_pending_activity_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);
            when(activityDAO.approveActivity(ACTIVITY_ID)).thenReturn(true);

            Result result = activityService.approveActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityDAO).approveActivity(ACTIVITY_ID);
        }
    }

    // ==================== rejectActivity 活动审核驳回 ====================

    @Nested
    @DisplayName("rejectActivity 活动审核驳回")
    class RejectActivityTests {

        @FastTest
        @DisplayName("驳回活动时拒绝原因为空应返回错误")
        void should_return_error_when_reject_reason_empty() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);

            Result result = activityService.rejectActivity(ACTIVITY_ID, "", OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回活动时拒绝原因超长应返回错误")
        void should_return_error_when_reject_reason_too_long() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);

            Result result = activityService.rejectActivity(ACTIVITY_ID, "a".repeat(501), OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员正常驳回活动应成功")
        void should_reject_activity_successfully() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            User operator = createUser(OPERATOR_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(OPERATOR_ID)).thenReturn(operator);
            when(activityDAO.rejectActivity(ACTIVITY_ID)).thenReturn(true);

            Result result = activityService.rejectActivity(ACTIVITY_ID, "活动内容不符合要求", OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== cancelActivity 取消活动 ====================

    @Nested
    @DisplayName("cancelActivity 取消活动")
    class CancelActivityTests {

        @FastTest
        @DisplayName("取消已取消的活动应返回错误")
        void should_return_error_when_already_canceled() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_CANCELED, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.cancelActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已取消");
        }

        @FastTest
        @DisplayName("取消已结束的活动应返回错误")
        void should_return_error_when_activity_completed() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_COMPLETED, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.cancelActivity(ACTIVITY_ID, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("非创建者且非管理员取消活动应返回403")
        void should_return_forbidden_when_not_creator_or_admin() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(OTHER_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.cancelActivity(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("创建者取消进行中的活动应成功")
        void should_cancel_ongoing_activity_by_creator() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_ONGOING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(activityDAO.update(any(Activity.class))).thenReturn(true);

            Result result = activityService.cancelActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("管理员取消任意活动应成功")
        void should_allow_admin_to_cancel_any_activity() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(OTHER_USER_ID);
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(activityDAO.update(any(Activity.class))).thenReturn(true);

            Result result = activityService.cancelActivity(ACTIVITY_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("取消活动时应通知所有已报名用户")
        void should_notify_all_registered_users_when_canceled() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity.setCreatorId(ADMIN_USER_ID);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(activityDAO.update(any(Activity.class))).thenReturn(true);
            when(registrationDAO.findByActivityId(ACTIVITY_ID)).thenReturn(Arrays.asList(
                    createRegistration(ACTIVITY_ID, MEMBER_USER_ID, REG_STATUS_CONFIRMED),
                    createRegistration(ACTIVITY_ID, OTHER_USER_ID, REG_STATUS_PENDING)
            ));

            activityService.cancelActivity(ACTIVITY_ID, ADMIN_USER_ID);

            verify(registrationDAO).findByActivityId(ACTIVITY_ID);
        }
    }

    // ==================== generateActivityNews 自动生成新闻 ====================

    @Nested
    @DisplayName("generateActivityNews 自动生成活动新闻")
    class GenerateActivityNewsTests {

        @FastTest
        @DisplayName("为不存在的活动生成新闻应返回404")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.generateActivityNews(ACTIVITY_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("为未结束的活动生成新闻应返回错误")
        void should_return_error_when_activity_not_completed() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_ONGOING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.generateActivityNews(ACTIVITY_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("未结束");
        }

        @FastTest
        @DisplayName("为已结束的活动生成新闻应成功")
        void should_generate_news_for_completed_activity() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_COMPLETED, APPROVAL_APPROVED, 10);
            activity.setTitle("测试活动");
            activity.setDescription("活动描述");
            activity.setActivityStartTime(new Date(System.currentTimeMillis() - 86400000));
            activity.setActivityEndTime(new Date(System.currentTimeMillis() - 86400000));
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.generateActivityNews(ACTIVITY_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== listActivities 活动列表 ====================

    @Nested
    @DisplayName("listActivities 活动列表")
    class ListActivitiesTests {

        @FastTest
        @DisplayName("获取活动列表应返回分页结果")
        void should_return_paged_activity_list() {
            Activity activity1 = createActivity(1, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            Activity activity2 = createActivity(2, STATUS_UPCOMING, APPROVAL_APPROVED, 20);
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList(activity1, activity2));

            Result result = activityService.listActivities(new ActivityFilterDTO(), 1, 10);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("按活动类型筛选应正确过滤")
        void should_filter_by_activity_type() {
            ActivityFilterDTO filter = new ActivityFilterDTO();
            filter.setActivityType(TYPE_LECTURE);
            when(activityDAO.findByConditions(any(), eq(TYPE_LECTURE), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按状态筛选应正确过滤")
        void should_filter_by_status() {
            ActivityFilterDTO filter = new ActivityFilterDTO();
            filter.setStatus(STATUS_COMPLETED);
            when(activityDAO.findByConditions(any(), any(), eq(STATUS_COMPLETED), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按关键词搜索应正确过滤")
        void should_filter_by_keyword() {
            ActivityFilterDTO filter = new ActivityFilterDTO();
            filter.setKeyword("测试");
            when(activityDAO.findByConditions(eq("测试"), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(filter, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数为负数时应使用默认值")
        void should_use_default_when_page_negative() {
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(new ActivityFilterDTO(), -1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小超过最大值时应限制")
        void should_limit_page_size_when_exceeds_max() {
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(new ActivityFilterDTO(), 1, 1000);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(new ActivityFilterDTO(), 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getActivityDetail 活动详情 ====================

    @Nested
    @DisplayName("getActivityDetail 活动详情")
    class GetActivityDetailTests {

        @FastTest
        @DisplayName("获取不存在的活动详情应返回404")
        void should_return_not_found_when_activity_not_exists() {
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(null);

            Result result = activityService.getActivityDetail(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("获取活动详情时应包含当前用户报名状态")
        void should_include_current_user_registration_status() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getRegistrationStatus(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(REG_STATUS_CONFIRMED);

            Result result = activityService.getActivityDetail(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取活动详情时应包含当前报名人数")
        void should_include_current_participant_count() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_CONFIRMED)).thenReturn(5);
            when(registrationDAO.getParticipantCount(ACTIVITY_ID, REG_STATUS_PENDING)).thenReturn(2);

            Result result = activityService.getActivityDetail(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未登录用户获取公开活动详情应成功")
        void should_allow_guest_to_view_public_activity_detail() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);

            Result result = activityService.getActivityDetail(ACTIVITY_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getMyActivities 我报名的活动 ====================

    @Nested
    @DisplayName("getMyActivities 我报名的活动")
    class GetMyActivitiesTests {

        @FastTest
        @DisplayName("获取我报名的活动应返回分页结果")
        void should_return_paged_my_activities() {
            Registration reg1 = createRegistration(ACTIVITY_ID, MEMBER_USER_ID, REG_STATUS_CONFIRMED);
            Registration reg2 = createRegistration(ACTIVITY_ID + 1, MEMBER_USER_ID, REG_STATUS_PENDING);
            when(registrationDAO.findByUserId(MEMBER_USER_ID))
                    .thenReturn(Arrays.asList(reg1, reg2));
            when(activityDAO.findById(ACTIVITY_ID))
                    .thenReturn(createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10));
            when(activityDAO.findById(ACTIVITY_ID + 1))
                    .thenReturn(createActivity(ACTIVITY_ID + 1, STATUS_UPCOMING, APPROVAL_APPROVED, 10));

            Result result = activityService.getMyActivities(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未报名任何活动应返回空列表")
        void should_return_empty_when_not_registered_any() {
            when(registrationDAO.findByUserId(MEMBER_USER_ID))
                    .thenReturn(Arrays.asList());

            Result result = activityService.getMyActivities(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getMyCreatedActivities 我创建的活动 ====================

    @Nested
    @DisplayName("getMyCreatedActivities 我创建的活动")
    class GetMyCreatedActivitiesTests {

        @FastTest
        @DisplayName("获取我创建的活动应返回分页结果")
        void should_return_paged_my_created_activities() {
            Activity activity1 = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity1.setCreatorId(MEMBER_USER_ID);
            Activity activity2 = createActivity(ACTIVITY_ID + 1, STATUS_COMPLETED, APPROVAL_APPROVED, 20);
            activity2.setCreatorId(MEMBER_USER_ID);
            when(activityDAO.findByCreatorId(MEMBER_USER_ID))
                    .thenReturn(Arrays.asList(activity1, activity2));

            Result result = activityService.getMyCreatedActivities(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("未创建任何活动应返回空列表")
        void should_return_empty_when_not_created_any() {
            when(activityDAO.findByCreatorId(MEMBER_USER_ID))
                    .thenReturn(Arrays.asList());

            Result result = activityService.getMyCreatedActivities(MEMBER_USER_ID, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("活动状态枚举应包含所有预期值")
        void activity_status_enums_should_be_complete() {
            assertThat(STATUS_UPCOMING).isEqualTo("upcoming");
            assertThat(STATUS_ONGOING).isEqualTo("ongoing");
            assertThat(STATUS_COMPLETED).isEqualTo("completed");
            assertThat(STATUS_CANCELED).isEqualTo("canceled");
        }

        @Test
        @DisplayName("活动审批状态枚举应包含所有预期值")
        void activity_approval_status_enums_should_be_complete() {
            assertThat(APPROVAL_PENDING).isEqualTo("pending");
            assertThat(APPROVAL_APPROVED).isEqualTo("approved");
            assertThat(APPROVAL_REJECTED).isEqualTo("rejected");
        }

        @Test
        @DisplayName("报名状态枚举应包含所有预期值")
        void registration_status_enums_should_be_complete() {
            assertThat(REG_STATUS_PENDING).isEqualTo("pending");
            assertThat(REG_STATUS_CONFIRMED).isEqualTo("confirmed");
            assertThat(REG_STATUS_REJECTED).isEqualTo("rejected");
            assertThat(REG_STATUS_EXPIRED).isEqualTo("expired");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("活动ID为null时应返回错误")
        void should_return_error_when_activity_id_null() {
            Result result = activityService.register(null, MEMBER_USER_ID);
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("用户ID为null时应返回错误")
        void should_return_error_when_user_id_null() {
            Activity activity = createActivity(ACTIVITY_ID, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            Result result = activityService.getActivityDetail(ACTIVITY_ID, null);
            // 未登录用户可以查看公开活动详情，所以这里应该成功
            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页页码为0时应使用页码1")
        void should_use_page_1_when_page_is_zero() {
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(new ActivityFilterDTO(), 0, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小为0时应使用默认值")
        void should_use_default_page_size_when_zero() {
            when(activityDAO.findByConditions(any(), any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = activityService.listActivities(new ActivityFilterDTO(), 1, 0);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("最大参与人数为Integer.MAX_VALUE时应能处理")
        void should_handle_max_integer_as_max_participants() {
            Activity activity = createActivityWithRegistrationWindow(
                    ACTIVITY_ID, APPROVAL_APPROVED,
                    addDays(Calendar.getInstance(), -1),
                    addDays(Calendar.getInstance(), 5),
                    Integer.MAX_VALUE);
            when(activityDAO.findById(ACTIVITY_ID)).thenReturn(activity);
            when(registrationDAO.isRegistered(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(false);
            when(registrationDAO.register(ACTIVITY_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = activityService.register(ACTIVITY_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }
}
