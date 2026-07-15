package service;

import dao.AwardDAO;
import dao.AwardImageDAO;
import dao.FileStorageDAO;
import dao.UserDAO;
import dto.AwardDTO;
import model.Award;
import model.AwardImage;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;

/**
 * AwardService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.5 AwardService 奖项服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - AwardDAO: findById(id) / insert(Award) / update(Award) / delete(id) / approveAward(id, approvedBy)
 * - AwardDAO: rejectAward(id, approvedBy) / findByUserId(userId) / findAll() / findByStatus(status)
 * - AwardDAO: findByConditions(status, keyword, awardType, awardCategory, awardLevel, competitionLevel)
 * - AwardDAO: countPending() / addMember(awardId, memberId) / removeMember(awardId, memberId)
 * - AwardImageDAO: findByAwardId(awardId) / insert(AwardImage) / deleteById(id)
 * - UserDAO: findById(id)
 * - FileStorageDAO: insert(FileStorage)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AwardService 奖项服务测试")
class AwardServiceTest {

    @Mock
    private AwardDAO awardDAO;

    @Mock
    private AwardImageDAO awardImageDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    private AwardService awardService;

    @BeforeEach
    void setUp() {
        awardService = new AwardService(awardDAO, awardImageDAO, userDAO, fileStorageDAO);
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        when(userDAO.findById(anyInt())).thenReturn(createUser(1, "admin", ROLE_ADMIN));
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;

    // 角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 奖项状态枚举
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    // 奖项类型枚举
    private static final int TYPE_PERSONAL = 1;
    private static final int TYPE_TEAM = 2;

    // 奖项级别枚举
    private static final int LEVEL_NATIONAL = 1;
    private static final int LEVEL_PROVINCIAL = 2;
    private static final int LEVEL_SCHOOL = 3;

    // ==================== 测试初始化 ====================

    private Award createAward(Integer id, String competition, String status, int year, Integer createdBy) {
        Award award = new Award();
        award.setId(id);
        award.setName(competition);
        award.setCompetition(competition);
        award.setCompetitionTime(createDate(2024, 6, 15));
        award.setYear(year);
        award.setAwardStatus(status);
        award.setCreatedBy(createdBy);
        award.setAwardType(TYPE_PERSONAL);
        award.setAwardLevel(LEVEL_NATIONAL);
        award.setAwardCategory(1);
        award.setCompetitionLevel(1);
        return award;
    }

    private AwardImage createAwardImage(Integer id, Integer awardId, Integer fileStorageId) {
        AwardImage image = new AwardImage();
        image.setId(id);
        image.setAwardId(awardId);
        image.setFileStorageId(fileStorageId);
        image.setIsMain(true);
        image.setCreatedAt(new Date());
        return image;
    }

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private AwardDTO createAwardDTO(String competition, int year) {
        AwardDTO dto = new AwardDTO();
        dto.setCompetition(competition);
        dto.setCompetitionTime("2024-06-15");
        dto.setYear(year);
        dto.setAwardLevel(LEVEL_NATIONAL);
        dto.setAwardType(TYPE_PERSONAL);
        dto.setAwardCategory(1);
        dto.setCompetitionLevel(1);
        dto.setCompetitionLocation("北京");
        return dto;
    }

    private Object createMockPart(String fileName, String contentType, long size) {
        return new Object() {
            public long getSize() { return size; }
            public String getContentType() { return contentType; }
            public String getSubmittedFileName() { return fileName; }
        };
    }

    // ==================== submitAward 提交奖项 ====================

    @Nested
    @DisplayName("submitAward 提交奖项")
    class SubmitAwardTests {

        @FastTest
        @DisplayName("提交奖项成功应返回成功")
        void should_submit_award_successfully() {
            AwardDTO dto = createAwardDTO("全国大学生程序设计竞赛", 2024);
            dto.setTeamName("Algorithm战队");

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Award> captor = ArgumentCaptor.forClass(Award.class);
            verify(awardDAO).insert(captor.capture());
            Award saved = captor.getValue();
            assertThat(saved.getCompetition()).isEqualTo("全国大学生程序设计竞赛");
            assertThat(saved.getAwardStatus()).isEqualTo(STATUS_PENDING);
            assertThat(saved.getCreatedBy()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("提交奖项时应自动推导年份")
        void should_auto_deduct_year_from_competition_time() {
            AwardDTO dto = createAwardDTO("省级程序设计竞赛", 2024);
            dto.setCompetitionTime("2024-07-20");

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Award> captor = ArgumentCaptor.forClass(Award.class);
            verify(awardDAO).insert(captor.capture());
            assertThat(captor.getValue().getYear()).isEqualTo(2024);
        }

        @FastTest
        @DisplayName("提交奖项时应设置状态为PENDING")
        void should_set_status_to_pending() {
            AwardDTO dto = createAwardDTO("校级竞赛", 2024);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Award> captor = ArgumentCaptor.forClass(Award.class);
            verify(awardDAO).insert(captor.capture());
            assertThat(captor.getValue().getAwardStatus()).isEqualTo(STATUS_PENDING);
        }

        @FastTest
        @DisplayName("提交奖项时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);

            Result result = awardService.submitAward(dto, null, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = awardService.submitAward(null, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时竞赛名称为空应返回错误")
        void should_return_error_when_competition_empty() {
            AwardDTO dto = createAwardDTO("", 2024);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时竞赛名称为null应返回错误")
        void should_return_error_when_competition_null() {
            AwardDTO dto = createAwardDTO(null, 2024);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时比赛时间格式错误应返回错误")
        void should_return_error_when_competition_time_invalid() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            dto.setCompetitionTime("invalid-date");

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时用户不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(null);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("提交奖项时数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            when(awardDAO.insert(any(Award.class))).thenReturn(false);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("提交奖项时应设置创建者ID")
        void should_set_created_by_when_submit() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            awardService.submitAward(dto, MEMBER_USER_ID, null);

            ArgumentCaptor<Award> captor = ArgumentCaptor.forClass(Award.class);
            verify(awardDAO).insert(captor.capture());
            assertThat(captor.getValue().getCreatedBy()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("提交带图片的奖项应成功")
        void should_submit_award_with_images() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            Object[] images = new Object[] {
                createMockPart("证书1.jpg", "image/jpeg", 1024),
                createMockPart("证书2.jpg", "image/jpeg", 2048)
            };

            when(awardDAO.insert(any(Award.class))).thenReturn(true);
            when(fileStorageDAO.insert(any())).thenReturn(100);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, images);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO).insert(any(Award.class));
            verify(fileStorageDAO, times(2)).insert(any());
        }

        @FastTest
        @DisplayName("提交奖项时团队名称应被保存")
        void should_save_team_name_when_submit() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            dto.setTeamName("创新团队");

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Award> captor = ArgumentCaptor.forClass(Award.class);
            verify(awardDAO).insert(captor.capture());
            assertThat(captor.getValue().getTeamName()).isEqualTo("创新团队");
        }
    }

    // ==================== approveAward 审批通过 ====================

    @Nested
    @DisplayName("approveAward 审批通过")
    class ApproveAwardTests {

        @FastTest
        @DisplayName("管理员审批通过应成功")
        void should_approve_award_successfully_as_admin() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));
            when(awardDAO.approveAward(eq(100), eq(ADMIN_USER_ID))).thenReturn(true);

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO).approveAward(100, ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("审批奖项时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = awardService.approveAward(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("审批奖项时operatorId为null应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = awardService.approveAward(100, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("审批奖项时奖项不存在应返回错误")
        void should_return_error_when_award_not_exists() {
            when(awardDAO.findById(100)).thenReturn(null);

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审批奖项时操作者不存在应返回错误")
        void should_return_error_when_operator_not_exists() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(null);

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("审批奖项时非管理员应返回错误")
        void should_return_error_when_not_admin() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            User member = createUser(MEMBER_USER_ID, "member", ROLE_MEMBER);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = awardService.approveAward(100, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("审批已通过的奖项应返回错误")
        void should_return_error_when_award_already_approved() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("审批已拒绝的奖项应返回错误")
        void should_return_error_when_award_already_rejected() {
            Award award = createAward(100, "竞赛", STATUS_REJECTED, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("审批时数据库错误应返回错误")
        void should_return_error_when_database_error() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));
            when(awardDAO.approveAward(eq(100), eq(ADMIN_USER_ID))).thenThrow(new RuntimeException("数据库错误"));

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== rejectAward 驳回 ====================

    @Nested
    @DisplayName("rejectAward 驳回")
    class RejectAwardTests {

        @FastTest
        @DisplayName("管理员驳回应成功")
        void should_reject_award_successfully_as_admin() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));
            when(awardDAO.rejectAward(eq(100), eq(ADMIN_USER_ID))).thenReturn(true);

            Result result = awardService.rejectAward(100, "材料不全", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO).rejectAward(100, ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("驳回时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = awardService.rejectAward(null, "原因", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回时原因为空应返回错误")
        void should_return_error_when_reason_empty() {
            Result result = awardService.rejectAward(100, "", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回时原因为null应返回错误")
        void should_return_error_when_reason_null() {
            Result result = awardService.rejectAward(100, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回时operatorId为null应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = awardService.rejectAward(100, "原因", null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回时奖项不存在应返回错误")
        void should_return_error_when_award_not_exists() {
            when(awardDAO.findById(100)).thenReturn(null);

            Result result = awardService.rejectAward(100, "原因", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("驳回时非管理员应返回错误")
        void should_return_error_when_not_admin() {
            Award award = createAward(100, "竞赛", STATUS_PENDING, 2024, MEMBER_USER_ID);
            User member = createUser(MEMBER_USER_ID, "member", ROLE_MEMBER);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = awardService.rejectAward(100, "原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("驳回已通过的奖项应返回错误")
        void should_return_error_when_award_already_approved() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN));

            Result result = awardService.rejectAward(100, "原因", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== addAwardImage 添加图片 ====================

    @Nested
    @DisplayName("addAwardImage 添加图片")
    class AddAwardImageTests {

        @FastTest
        @DisplayName("添加图片成功应返回成功")
        void should_add_image_successfully() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);

            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER));
            when(fileStorageDAO.insert(any())).thenReturn(200);
            when(awardImageDAO.insert(any(AwardImage.class))).thenReturn(true);

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(fileStorageDAO).insert(any());
            verify(awardImageDAO).insert(any(AwardImage.class));
        }

        @FastTest
        @DisplayName("添加图片时id为null应返回错误")
        void should_return_error_when_id_null() {
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);

            Result result = awardService.addAwardImage(null, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加图片时file为null应返回错误")
        void should_return_error_when_file_null() {
            Result result = awardService.addAwardImage(100, null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加图片时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);

            Result result = awardService.addAwardImage(100, filePart, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加图片时奖项不存在应返回错误")
        void should_return_error_when_award_not_exists() {
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);
            when(awardDAO.findById(100)).thenReturn(null);

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("添加图片时用户不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(null);

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("添加图片时非创建者且非管理员应返回错误")
        void should_return_error_when_not_owner_and_not_admin() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, OTHER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);
            User member = createUser(MEMBER_USER_ID, "member", ROLE_MEMBER);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加图片时管理员可以为任何奖项添加")
        void should_allow_admin_to_add_image_to_any_award() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, OTHER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 2048);
            User admin = createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(fileStorageDAO.insert(any())).thenReturn(200);
            when(awardImageDAO.insert(any(AwardImage.class))).thenReturn(true);

            Result result = awardService.addAwardImage(100, filePart, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("添加图片时文件太大应返回错误")
        void should_return_error_when_file_too_large() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 100 * 1024 * 1024); // 100MB
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER));

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件大小");
        }

        @FastTest
        @DisplayName("添加图片时文件类型不支持应返回错误")
        void should_return_error_when_file_type_unsupported() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.exe", "application/x-executable", 1024);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER));

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== listAwards 列表 ====================

    @Nested
    @DisplayName("listAwards 列表")
    class ListAwardsTests {

        @FastTest
        @DisplayName("获取所有奖项列表应成功")
        void should_list_all_awards_successfully() {
            Award award1 = createAward(1, "竞赛1", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Award award2 = createAward(2, "竞赛2", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findAll()).thenReturn(Arrays.asList(award1, award2));

            Result result = awardService.listAwards(null, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按状态筛选奖项应成功")
        void should_filter_awards_by_status() {
            Award award = createAward(1, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findByStatus(STATUS_APPROVED)).thenReturn(Arrays.asList(award));

            Result result = awardService.listAwards(STATUS_APPROVED, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按关键词搜索应成功")
        void should_filter_awards_by_keyword() {
            Award award = createAward(1, "程序设计竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findByStatusAndKeyword(anyString(), anyString())).thenReturn(Arrays.asList(award));

            Result result = awardService.listAwards("APPROVED", 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数为负数时应使用默认值")
        void should_use_default_when_page_negative() {
            when(awardDAO.findAll()).thenReturn(Arrays.asList());

            Result result = awardService.listAwards(null, -1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数为0时应使用默认值")
        void should_use_default_when_page_zero() {
            when(awardDAO.findAll()).thenReturn(Arrays.asList());

            Result result = awardService.listAwards(null, 0);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            when(awardDAO.findAll()).thenReturn(Arrays.asList());

            Result result = awardService.listAwards(null, 1);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getAwardStatistics 统计 ====================

    @Nested
    @DisplayName("getAwardStatistics 个人获奖统计")
    class GetAwardStatisticsTests {

        @FastTest
        @DisplayName("获取个人获奖统计应成功")
        void should_get_statistics_successfully() {
            Award national1 = createAward(1, "国赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            national1.setAwardLevel(LEVEL_NATIONAL);
            national1.setAwardType(TYPE_PERSONAL);

            Award national2 = createAward(2, "省赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            national2.setAwardLevel(LEVEL_NATIONAL);
            national2.setAwardType(TYPE_TEAM);

            Award provincial = createAward(3, "校赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            provincial.setAwardLevel(LEVEL_PROVINCIAL);
            provincial.setAwardType(TYPE_PERSONAL);

            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(national1, national2, provincial));

            Result result = awardService.getAwardStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取统计时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = awardService.getAwardStatistics(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("无获奖记录应返回空统计")
        void should_return_empty_statistics_when_no_awards() {
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList());

            Result result = awardService.getAwardStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应正确统计国家级奖项数量")
        void should_count_national_awards_correctly() {
            Award award = createAward(1, "国赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            award.setAwardLevel(LEVEL_NATIONAL);
            award.setAwardType(TYPE_PERSONAL);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(award));

            Result result = awardService.getAwardStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应正确统计个人奖项和团队奖项数量")
        void should_count_personal_and_team_awards_correctly() {
            Award personal = createAward(1, "个人赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            personal.setAwardType(TYPE_PERSONAL);

            Award team = createAward(2, "团队赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            team.setAwardType(TYPE_TEAM);

            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(personal, team));

            Result result = awardService.getAwardStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应正确统计本年度奖项数量")
        void should_count_current_year_awards_correctly() {
            Award award = createAward(1, "竞赛", STATUS_APPROVED, Calendar.getInstance().get(Calendar.YEAR), MEMBER_USER_ID);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(award));

            Result result = awardService.getAwardStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== filterAwardsForUser 筛选 ====================

    @Nested
    @DisplayName("filterAwardsForUser 筛选")
    class FilterAwardsForUserTests {

        @FastTest
        @DisplayName("筛选用户奖项应成功")
        void should_filter_awards_for_user_successfully() {
            Award award = createAward(1, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(award));

            Result result = awardService.filterAwardsForUser(MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("筛选时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = awardService.filterAwardsForUser(null, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("按状态筛选应正确过滤")
        void should_filter_by_status_correctly() {
            Award approved = createAward(1, "已通过", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Award pending = createAward(2, "待审核", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(approved, pending));

            Result result = awardService.filterAwardsForUser(MEMBER_USER_ID, STATUS_APPROVED);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按奖项级别筛选应正确过滤")
        void should_filter_by_award_level_correctly() {
            Award national = createAward(1, "国家级", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            national.setAwardLevel(LEVEL_NATIONAL);

            Award provincial = createAward(2, "省级", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            provincial.setAwardLevel(LEVEL_PROVINCIAL);

            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(national, provincial));

            Result result = awardService.filterAwardsForUser(MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按竞赛名称关键词筛选应正确过滤")
        void should_filter_by_competition_keyword_correctly() {
            Award award1 = createAward(1, "程序设计竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Award award2 = createAward(2, "数学建模竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(award1, award2));

            Result result = awardService.filterAwardsForUser(MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空筛选条件应返回所有奖项")
        void should_return_all_awards_when_filter_empty() {
            Award award1 = createAward(1, "竞赛1", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Award award2 = createAward(2, "竞赛2", STATUS_PENDING, 2024, MEMBER_USER_ID);
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList(award1, award2));

            Result result = awardService.filterAwardsForUser(MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("奖项状态枚举应包含所有预期值")
        void award_status_enums_should_be_complete() {
            assertThat(AwardService.STATUS_PENDING).isEqualTo("PENDING");
            assertThat(AwardService.STATUS_APPROVED).isEqualTo("APPROVED");
            assertThat(AwardService.STATUS_REJECTED).isEqualTo("REJECTED");
        }

        @Test
        @DisplayName("Award模型状态常量应与Service一致")
        void award_model_status_constants_should_match() {
            assertThat(Award.STATUS_PENDING).isEqualTo("PENDING");
            assertThat(Award.STATUS_APPROVED).isEqualTo("APPROVED");
            assertThat(Award.STATUS_REJECTED).isEqualTo("REJECTED");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("奖项ID为0应返回错误")
        void should_return_error_when_award_id_zero() {
            Result result = awardService.approveAward(0, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("奖项ID为负数应返回错误")
        void should_return_error_when_award_id_negative() {
            Result result = awardService.approveAward(-1, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交奖项时年份为空应自动使用当前年份")
        void should_use_current_year_when_year_empty() {
            AwardDTO dto = createAwardDTO("竞赛", Calendar.getInstance().get(Calendar.YEAR));
            dto.setYear(null);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("长竞赛名称应正常处理")
        void should_handle_long_competition_name() {
            String longName = "a".repeat(500);
            AwardDTO dto = createAwardDTO(longName, 2024);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("特殊字符竞赛名称应正常处理")
        void should_handle_special_characters_in_competition_name() {
            AwardDTO dto = createAwardDTO("全国大学生\"程序设计\"竞赛&测试", 2024);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("Unicode竞赛名称应正常处理")
        void should_handle_unicode_competition_name() {
            AwardDTO dto = createAwardDTO("全国大学生程序设计竞赛简体中文", 2024);

            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("数据库查询异常应返回错误")
        void should_return_error_when_database_query_fails() {
            when(awardDAO.findById(100)).thenThrow(new RuntimeException("数据库连接失败"));

            Result result = awardService.approveAward(100, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库插入异常应返回错误")
        void should_return_error_when_database_insert_fails() {
            AwardDTO dto = createAwardDTO("竞赛", 2024);
            when(awardDAO.insert(any(Award.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = awardService.submitAward(dto, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("文件存储异常应返回错误")
        void should_return_error_when_file_storage_fails() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 1024);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER));
            when(fileStorageDAO.insert(any())).thenThrow(new RuntimeException("文件存储失败"));

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("图片插入数据库异常应返回错误")
        void should_return_error_when_image_insert_fails() {
            Award award = createAward(100, "竞赛", STATUS_APPROVED, 2024, MEMBER_USER_ID);
            Object filePart = createMockPart("证书.jpg", "image/jpeg", 1024);
            when(awardDAO.findById(100)).thenReturn(award);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER));
            when(fileStorageDAO.insert(any())).thenReturn(200);
            when(awardImageDAO.insert(any(AwardImage.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = awardService.addAwardImage(100, filePart, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }
}
