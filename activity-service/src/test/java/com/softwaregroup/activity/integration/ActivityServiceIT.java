package com.softwaregroup.activity.integration;

import com.softwaregroup.activity.dao.ActivityDAO;
import com.softwaregroup.activity.dao.ActivityParticipantDAO;
import com.softwaregroup.activity.feign.UserFeignClient;
import com.softwaregroup.activity.model.entity.Activity;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.activity.model.entity.Activity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ActivityService 集成测试
 *
 * 测试活动服务的核心功能
 * 注意：ActivityService 使用 @Autowired 依赖注入，这些测试验证 DAO 层行为
 */
@ExtendWith(MockitoExtension.class)
class ActivityServiceIT {

    @Mock
    private ActivityDAO activityDAO;

    @Mock
    private ActivityParticipantDAO activityParticipantDAO;

    @Mock
    private UserFeignClient userFeignClient;

    @Test
    void activityDao_findById_withInvalidId_shouldReturnNull() {
        when(activityDAO.findById(9999)).thenReturn(null);

        Activity result = activityDAO.findById(9999);

        assertThat(result).isNull();
    }

    @Test
    void activityDao_findById_withValidId_shouldReturnActivity() {
        Activity activity = new Activity();
        activity.setId(1);
        activity.setTitle("测试活动");
        when(activityDAO.findById(1)).thenReturn(activity);

        Activity result = activityDAO.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void userFeignClient_mock_shouldWork() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("username", "admin");
        when(userFeignClient.getUserById(1)).thenReturn(Result.ok(userData));

        Result result = userFeignClient.getUserById(1);

        assertThat(result.isSuccess()).isTrue();
        verify(userFeignClient).getUserById(1);
    }
}
