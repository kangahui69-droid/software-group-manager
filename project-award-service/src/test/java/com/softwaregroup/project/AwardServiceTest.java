package com.softwaregroup.project.service;

import com.softwaregroup.project.dao.AwardDAO;
import com.softwaregroup.project.dao.AwardImageDAO;
import com.softwaregroup.project.dao.UserDAO;
import com.softwaregroup.project.dao.FileStorageDAO;
import com.softwaregroup.project.model.Award;
import com.softwaregroup.project.model.AwardImage;
import com.softwaregroup.project.model.User;
import com.softwaregroup.project.model.FileStorage;
import com.softwaregroup.project.model.dto.AwardDTO;
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
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AwardService 单元测试
 * 覆盖所有公开业务方法的正常路径、边界情况和异常场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("奖项服务测试")
class AwardServiceTest {

    @Mock
    private AwardDAO awardDAO;

    @Mock
    private AwardImageDAO awardImageDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    @InjectMocks
    private AwardService awardService;

    private Award testAward;
    private AwardDTO testAwardDTO;
    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testAward = new Award();
        testAward.setId(1);
        testAward.setName("测试竞赛");
        testAward.setCompetition("软件设计大赛");
        testAward.setAwardStatus("PENDING");
        testAward.setAwardLevel(1);
        testAward.setAwardType(1);
        testAward.setCreatedBy(2);
        testAward.setCreatedAt(new Date());

        testAwardDTO = new AwardDTO();
        testAwardDTO.setCompetition("软件设计大赛");
        testAwardDTO.setCompetitionTime("2026-01-01");
        testAwardDTO.setAwardLevel(1);
        testAwardDTO.setAwardType(1);

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
    @DisplayName("submitAward - 提交奖项")
    class SubmitAwardTests {

        @Test
        @DisplayName("正常路径：成功提交奖项")
        void should_submit_award_successfully() {
            when(userDAO.findById(2)).thenReturn(testUser);
            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO, times(1)).insert(any(Award.class));
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = awardService.submitAward(null, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("奖项信息不能为空");
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = awardService.submitAward(testAwardDTO, null, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("异常场景：用户不存在应返回404")
        void should_return_404_when_user_not_found() {
            when(userDAO.findById(999)).thenReturn(null);

            Result result = awardService.submitAward(testAwardDTO, 999, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("用户不存在");
        }

        @Test
        @DisplayName("异常场景：竞赛名称为空应返回错误")
        void should_return_error_when_competition_is_empty() {
            testAwardDTO.setCompetition("");

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("竞赛名称不能为空");
        }

        @Test
        @DisplayName("异常场景：竞赛名称为null应返回错误")
        void should_return_error_when_competition_is_null() {
            testAwardDTO.setCompetition(null);

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：比赛时间格式错误应返回错误")
        void should_return_error_when_competition_time_format_invalid() {
            testAwardDTO.setCompetitionTime("2026/01/01");

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("比赛时间格式错误");
        }

        @Test
        @DisplayName("异常场景：数据库插入失败应返回500")
        void should_return_500_when_insert_fails() {
            when(userDAO.findById(2)).thenReturn(testUser);
            when(awardDAO.insert(any(Award.class))).thenReturn(false);

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("提交失败");
        }

        @Test
        @DisplayName("边界情况：正确的日期格式应正常提交")
        void should_submit_when_date_format_is_valid() {
            testAwardDTO.setCompetitionTime("2026-01-01");
            when(userDAO.findById(2)).thenReturn(testUser);
            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Result result = awardService.submitAward(testAwardDTO, 2, null);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("approveAward - 审批通过")
    class ApproveAwardTests {

        @Test
        @DisplayName("正常路径：管理员成功审批奖项")
        void should_approve_award_when_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(awardDAO.approveAward(1, 1)).thenReturn(true);

            Result result = awardService.approveAward(1, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO, times(1)).approveAward(1, 1);
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = awardService.approveAward(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("奖项ID不能为空");
        }

        @Test
        @DisplayName("异常场景：奖项ID为0应返回错误")
        void should_return_error_when_id_is_zero() {
            Result result = awardService.approveAward(0, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项ID为负数应返回错误")
        void should_return_error_when_id_is_negative() {
            Result result = awardService.approveAward(-1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = awardService.approveAward(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项不存在应返回404")
        void should_return_404_when_award_not_found() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = awardService.approveAward(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("奖项不存在");
        }

        @Test
        @DisplayName("异常场景：奖项已审批应返回错误")
        void should_return_error_when_award_not_pending() {
            testAward.setAwardStatus("APPROVED");
            when(awardDAO.findById(1)).thenReturn(testAward);

            Result result = awardService.approveAward(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("待审核");
        }

        @Test
        @DisplayName("异常场景：操作者不存在应返回404")
        void should_return_404_when_operator_not_found() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(999)).thenReturn(null);

            Result result = awardService.approveAward(1, 999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("操作者不存在");
        }

        @Test
        @DisplayName("异常场景：非管理员审批应返回403")
        void should_return_403_when_not_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(2)).thenReturn(testUser);

            Result result = awardService.approveAward(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("无权限");
        }

        @Test
        @DisplayName("异常场景：数据库更新失败应返回500")
        void should_return_500_when_approve_fails() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(awardDAO.approveAward(1, 1)).thenReturn(false);

            Result result = awardService.approveAward(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("审批失败");
        }
    }

    @Nested
    @DisplayName("rejectAward - 驳回奖项")
    class RejectAwardTests {

        @Test
        @DisplayName("正常路径：管理员成功驳回奖项")
        void should_reject_award_when_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(awardDAO.rejectAward(1, 1)).thenReturn(true);

            Result result = awardService.rejectAward(1, "不符合要求", 1);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO, times(1)).rejectAward(1, 1);
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = awardService.rejectAward(null, "原因", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = awardService.rejectAward(1, "原因", null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：驳回原因为空应返回错误")
        void should_return_error_when_reason_is_empty() {
            Result result = awardService.rejectAward(1, "", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("驳回原因不能为空");
        }

        @Test
        @DisplayName("异常场景：驳回原因为null应返回错误")
        void should_return_error_when_reason_is_null() {
            Result result = awardService.rejectAward(1, null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项不存在应返回404")
        void should_return_404_when_award_not_found() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = awardService.rejectAward(999, "原因", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：奖项已通过应返回错误")
        void should_return_error_when_award_already_approved() {
            testAward.setAwardStatus("APPROVED");
            when(awardDAO.findById(1)).thenReturn(testAward);

            Result result = awardService.rejectAward(1, "原因", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：非管理员驳回应返回403")
        void should_return_403_when_not_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(2)).thenReturn(testUser);

            Result result = awardService.rejectAward(1, "原因", 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：驳回失败应返回500")
        void should_return_500_when_reject_fails() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(awardDAO.rejectAward(1, 1)).thenReturn(false);

            Result result = awardService.rejectAward(1, "原因", 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("驳回失败");
        }
    }

    @Nested
    @DisplayName("listAwards - 奖项列表")
    class ListAwardsTests {

        @Test
        @DisplayName("正常路径：返回所有奖项")
        void should_return_all_awards() {
            when(awardDAO.findAll()).thenReturn(Arrays.asList(testAward));

            Result result = awardService.listAwards(null, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("正常路径：按状态筛选待审核")
        void should_filter_by_pending_status() {
            when(awardDAO.findByStatus("PENDING")).thenReturn(Arrays.asList(testAward));

            Result result = awardService.listAwards("PENDING", 1);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO, times(1)).findByStatus("PENDING");
        }

        @Test
        @DisplayName("正常路径：按状态筛选已通过")
        void should_filter_by_approved_status() {
            when(awardDAO.findByStatus("APPROVED")).thenReturn(Arrays.asList());

            Result result = awardService.listAwards("APPROVED", 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：按状态筛选已驳回")
        void should_filter_by_rejected_status() {
            when(awardDAO.findByStatus("REJECTED")).thenReturn(Arrays.asList());

            Result result = awardService.listAwards("REJECTED", 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：空字符串当作ALL处理")
        void should_treat_empty_string_as_all() {
            when(awardDAO.findAll()).thenReturn(Arrays.asList(testAward));

            Result result = awardService.listAwards("", 1);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getAwardStatistics - 个人获奖统计")
    class GetAwardStatisticsTests {

        @Test
        @DisplayName("正常路径：返回获奖统计")
        void should_return_statistics() {
            testAward.setAwardStatus("APPROVED");
            testAward.setAwardLevel(1);
            testAward.setAwardType(1);
            testAward.setYear(2026);
            when(awardDAO.findByUserId(2)).thenReturn(Arrays.asList(testAward));

            Result result = awardService.getAwardStatistics(2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = awardService.getAwardStatistics(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("正常路径：无获奖记录返回空统计")
        void should_return_empty_statistics_when_no_awards() {
            when(awardDAO.findByUserId(2)).thenReturn(null);

            Result result = awardService.getAwardStatistics(2);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("getMyAwards - 获取我的奖项")
    class GetMyAwardsTests {

        @Test
        @DisplayName("正常路径：返回用户的奖项列表")
        void should_return_user_awards() {
            when(awardDAO.findByUserId(2)).thenReturn(Arrays.asList(testAward));

            Result result = awardService.getMyAwards(2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = awardService.getMyAwards(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("正常路径：无奖项返回空列表")
        void should_return_empty_list_when_no_awards() {
            when(awardDAO.findByUserId(2)).thenReturn(null);

            Result result = awardService.getMyAwards(2);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("getAwardDetail - 获取奖项详情")
    class GetAwardDetailTests {

        @Test
        @DisplayName("正常路径：返回奖项详情")
        void should_return_award_detail() {
            when(awardDAO.findById(1)).thenReturn(testAward);

            Result result = awardService.getAwardDetail(1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(Award.class);
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = awardService.getAwardDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项不存在应返回404")
        void should_return_404_when_award_not_found() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = awardService.getAwardDetail(999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("getAwardImages - 获取奖项图片列表")
    class GetAwardImagesTests {

        @Test
        @DisplayName("正常路径：返回奖项图片列表")
        void should_return_award_images() {
            AwardImage image = new AwardImage();
            image.setId(1);
            image.setAwardId(1);
            when(awardImageDAO.findByAwardId(1)).thenReturn(Arrays.asList(image));

            Result result = awardService.getAwardImages(1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_award_id_is_null() {
            Result result = awardService.getAwardImages(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("deleteAward - 删除奖项")
    class DeleteAwardTests {

        @Test
        @DisplayName("正常路径：创建者成功删除待审核奖项")
        void should_delete_award_when_owner_pending() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(2)).thenReturn(testUser);
            when(awardDAO.delete(1)).thenReturn(true);

            Result result = awardService.deleteAward(1, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("正常路径：管理员成功删除待审核奖项")
        void should_delete_award_when_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(1)).thenReturn(testAdmin);
            when(awardDAO.delete(1)).thenReturn(true);

            Result result = awardService.deleteAward(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = awardService.deleteAward(null, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = awardService.deleteAward(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项不存在应返回404")
        void should_return_404_when_award_not_found() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = awardService.deleteAward(999, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：非创建者且非管理员删除应返回403")
        void should_return_403_when_not_owner_or_admin() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(3)).thenReturn(new User() {{ setId(3); setRole("MEMBER"); }});

            Result result = awardService.deleteAward(1, 3);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：已通过的奖项不能删除")
        void should_return_error_when_award_already_approved() {
            testAward.setAwardStatus("APPROVED");
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(2)).thenReturn(testUser);

            Result result = awardService.deleteAward(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("只能删除待审核的奖项");
        }

        @Test
        @DisplayName("异常场景：数据库删除失败应返回500")
        void should_return_500_when_delete_fails() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(userDAO.findById(2)).thenReturn(testUser);
            when(awardDAO.delete(1)).thenReturn(false);

            Result result = awardService.deleteAward(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("updateAward - 更新奖项")
    class UpdateAwardTests {

        @Test
        @DisplayName("正常路径：创建者成功更新待审核奖项")
        void should_update_award_when_owner_pending() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(awardDAO.update(any(Award.class))).thenReturn(true);

            AwardDTO updateDTO = new AwardDTO();
            updateDTO.setCompetition("更新后的竞赛名称");

            Result result = awardService.updateAward(1, updateDTO, 2);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：奖项ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = awardService.updateAward(null, testAwardDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = awardService.updateAward(1, testAwardDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：奖项不存在应返回404")
        void should_return_404_when_award_not_found() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = awardService.updateAward(999, testAwardDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：非创建者修改应返回403")
        void should_return_403_when_not_owner() {
            when(awardDAO.findById(1)).thenReturn(testAward);

            Result result = awardService.updateAward(1, testAwardDTO, 3);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：已通过的奖项不能修改")
        void should_return_error_when_award_already_approved() {
            testAward.setAwardStatus("APPROVED");
            when(awardDAO.findById(1)).thenReturn(testAward);

            Result result = awardService.updateAward(1, testAwardDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("只能修改待审核的奖项");
        }

        @Test
        @DisplayName("异常场景：数据库更新失败应返回500")
        void should_return_500_when_update_fails() {
            when(awardDAO.findById(1)).thenReturn(testAward);
            when(awardDAO.update(any(Award.class))).thenReturn(false);

            Result result = awardService.updateAward(1, testAwardDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("AwardStatus 枚举状态测试")
    class AwardStatusTests {

        @Test
        @DisplayName("状态常量验证：PENDING")
        void should_have_correct_pending_status() {
            assertThat(AwardService.STATUS_PENDING).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("状态常量验证：APPROVED")
        void should_have_correct_approved_status() {
            assertThat(AwardService.STATUS_APPROVED).isEqualTo("APPROVED");
        }

        @Test
        @DisplayName("状态常量验证：REJECTED")
        void should_have_correct_rejected_status() {
            assertThat(AwardService.STATUS_REJECTED).isEqualTo("REJECTED");
        }
    }
}
