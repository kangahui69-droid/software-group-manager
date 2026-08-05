package com.softwaregroup.activity.service;

import com.softwaregroup.activity.dao.ActivityDAO;
import com.softwaregroup.activity.dao.ActivityParticipantDAO;
import com.softwaregroup.activity.feign.UserFeignClient;
import com.softwaregroup.activity.model.dto.ActivityDTO;
import com.softwaregroup.activity.model.dto.ActivityFilterDTO;
import com.softwaregroup.activity.model.entity.Activity;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ActivityService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityDAO activityDAO;

    @Mock
    private ActivityParticipantDAO registrationDAO;

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private ActivityService activityService;

    private Activity testActivity;
    private ActivityDTO testActivityDTO;

    @BeforeEach
    void setUp() {
        testActivity = new Activity();
        testActivity.setId(1);
        testActivity.setTitle("测试活动");
        testActivity.setDescription("活动描述");
        testActivity.setActivityType("LECTURE");
        testActivity.setStatus("upcoming");
        testActivity.setApprovalStatus("pending");
        testActivity.setCreatorId(1);
        testActivity.setMaxParticipants(100);

        testActivityDTO = new ActivityDTO();
        testActivityDTO.setTitle("测试活动");
        testActivityDTO.setDescription("活动描述");
        testActivityDTO.setActivityType("LECTURE");
        testActivityDTO.setMaxParticipants(100);
    }

    @Test
    void createActivity_ShouldReturnSuccess_WhenValidInput() {
        // Given
        when(activityDAO.insert(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(1);
            return activity;
        });

        // When
        Result result = activityService.createActivity(testActivityDTO, 1);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData());
        verify(activityDAO, times(1)).insert(any(Activity.class));
    }

    @Test
    void createActivity_ShouldReturnError_WhenTitleIsEmpty() {
        // Given
        testActivityDTO.setTitle("");

        // When
        Result result = activityService.createActivity(testActivityDTO, 1);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("标题不能为空", result.getMessage());
    }

    @Test
    void createActivity_ShouldReturnError_WhenActivityTypeIsEmpty() {
        // Given
        testActivityDTO.setActivityType("");

        // When
        Result result = activityService.createActivity(testActivityDTO, 1);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("活动类型不能为空", result.getMessage());
    }

    @Test
    void createActivity_ShouldReturnError_WhenMaxParticipantsIsNegative() {
        // Given
        testActivityDTO.setMaxParticipants(-1);

        // When
        Result result = activityService.createActivity(testActivityDTO, 1);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("最大参与人数不能为负数", result.getMessage());
    }

    @Test
    void listActivities_ShouldReturnActivityList() {
        // Given
        List<Activity> activities = Arrays.asList(testActivity);
        when(activityDAO.findByConditions(any(), any(), any(), any())).thenReturn(activities);

        ActivityFilterDTO filter = new ActivityFilterDTO();
        filter.setKeyword("测试");

        // When
        Result result = activityService.listActivities(filter);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(activityDAO, times(1)).findByConditions(any(), any(), any(), any());
    }

    @Test
    void getActivityDetail_ShouldReturnActivity_WhenExists() {
        // Given
        when(activityDAO.findById(1)).thenReturn(testActivity);
        when(registrationDAO.getParticipantCount(eq(1), anyString())).thenReturn(10);

        // When
        Result result = activityService.getActivityDetail(1, null);

        // Then
        assertTrue(result.isSuccess());
        Activity activity = (Activity) result.getData();
        assertEquals(1, activity.getId());
        assertEquals("测试活动", activity.getTitle());
    }

    @Test
    void getActivityDetail_ShouldReturnError_WhenNotExists() {
        // Given
        when(activityDAO.findById(999)).thenReturn(null);

        // When
        Result result = activityService.getActivityDetail(999, null);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertEquals("活动不存在", result.getMessage());
    }

    @Test
    void register_ShouldReturnError_WhenActivityNotApproved() {
        // Given
        testActivity.setApprovalStatus("pending");
        when(activityDAO.findById(1)).thenReturn(testActivity);

        // When
        Result result = activityService.register(1, 2);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("活动未通过审核", result.getMessage());
    }

    @Test
    void register_ShouldReturnError_WhenAlreadyRegistered() {
        // Given
        testActivity.setApprovalStatus("approved");
        // 设置报名时间窗口（未来时间）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testActivity.setRegistrationStartTime(new Date());
        testActivity.setRegistrationEndTime(cal.getTime());
        when(activityDAO.findById(1)).thenReturn(testActivity);
        when(registrationDAO.isRegistered(1, 2)).thenReturn(true);

        // When
        Result result = activityService.register(1, 2);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("您已报名此活动", result.getMessage());
    }

    @Test
    void register_ShouldReturnSuccess_WhenValidRegistration() {
        // Given
        testActivity.setApprovalStatus("approved");
        testActivity.setCreatorId(999); // 创建者不是当前用户
        // 设置报名时间窗口（未来时间）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        testActivity.setRegistrationStartTime(new Date());
        testActivity.setRegistrationEndTime(cal.getTime());
        when(activityDAO.findById(1)).thenReturn(testActivity);
        when(registrationDAO.isRegistered(1, 2)).thenReturn(false);
        when(registrationDAO.getParticipantCount(eq(1), anyString())).thenReturn(0);
        when(registrationDAO.register(1, 2)).thenReturn(true);

        // When
        Result result = activityService.register(1, 2);

        // Then
        assertTrue(result.isSuccess());
        verify(registrationDAO, times(1)).register(1, 2);
    }

    @Test
    void deleteActivity_ShouldReturnError_WhenActivityOngoing() {
        // Given
        testActivity.setStatus("ongoing");
        when(activityDAO.findById(1)).thenReturn(testActivity);
        when(userFeignClient.getUserRole(1)).thenReturn(Result.ok("ADMIN"));

        // When
        Result result = activityService.deleteActivity(1, 1);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("活动进行中，无法删除", result.getMessage());
    }

    @Test
    void getActivityParticipants_ShouldReturnParticipantList() {
        // Given
        when(activityDAO.findById(1)).thenReturn(testActivity);
        when(registrationDAO.findByActivityId(1)).thenReturn(Arrays.asList());

        // When
        Result result = activityService.getActivityParticipants(1);

        // Then
        assertTrue(result.isSuccess());
        verify(registrationDAO, times(1)).findByActivityId(1);
    }
}
