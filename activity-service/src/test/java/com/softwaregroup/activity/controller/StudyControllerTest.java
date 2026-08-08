package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.service.StudyService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudyController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class StudyControllerTest {

    @Mock
    private StudyService studyService;

    private StudyController studyController;

    @BeforeEach
    void setUp() throws Exception {
        studyController = new StudyController();
        // Use reflection to inject the mock service (since @Autowired is used without setter)
        java.lang.reflect.Field field = StudyController.class.getDeclaredField("studyService");
        field.setAccessible(true);
        field.set(studyController, studyService);
    }

    @Test
    void startSession_withValidUserId_shouldReturnSuccess() {
        when(studyService.startSession(1)).thenReturn(Result.ok(100));

        Result result = studyController.startSession(1);

        assertTrue(result.isSuccess());
        verify(studyService).startSession(1);
    }

    @Test
    void startSession_withNullUserId_shouldReturnError() {
        when(studyService.startSession(null)).thenReturn(Result.error(400, "用户ID不能为空"));

        Result result = studyController.startSession(null);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void endSession_withValidUserId_shouldReturnSuccess() {
        when(studyService.endSession(1)).thenReturn(Result.ok());

        Result result = studyController.endSession(1);

        assertTrue(result.isSuccess());
        verify(studyService).endSession(1);
    }

    @Test
    void autoEndSession_shouldReturnSuccess() {
        when(studyService.autoEndSession()).thenReturn(Result.ok(5));

        Result result = studyController.autoEndSession();

        assertTrue(result.isSuccess());
        verify(studyService).autoEndSession();
    }

    @Test
    void getSessionDetail_withValidId_shouldReturnSessionDetail() {
        Map<String, Object> sessionDetail = new HashMap<>();
        sessionDetail.put("id", 1);
        sessionDetail.put("userId", 1);

        when(studyService.getSessionDetail(1)).thenReturn(Result.ok(sessionDetail));

        Result result = studyController.getSessionDetail(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void listSessions_withDefaultParams_shouldReturnSessionList() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);
        pageResult.put("pageSize", 20);

        when(studyService.listSessions(anyMap(), eq(1), eq(20))).thenReturn(Result.ok(pageResult));

        Result result = studyController.listSessions(new HashMap<>(), 1, 20);

        assertTrue(result.isSuccess());
        verify(studyService).listSessions(anyMap(), eq(1), eq(20));
    }

    @Test
    void getMySessions_withUserId_shouldReturnMySessions() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);

        when(studyService.getMySessions(1, 1, 20)).thenReturn(Result.ok(pageResult));

        Result result = studyController.getMySessions(1, 1, 20);

        assertTrue(result.isSuccess());
        verify(studyService).getMySessions(1, 1, 20);
    }

    @Test
    void getTodaySession_withUserId_shouldReturnTodaySession() {
        Map<String, Object> todaySession = new HashMap<>();
        todaySession.put("id", 1);
        todaySession.put("userId", 1);

        when(studyService.getTodaySession(1)).thenReturn(Result.ok(todaySession));

        Result result = studyController.getTodaySession(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getStatistics_withUserId_shouldReturnStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHours", 100);
        stats.put("totalDays", 30);

        when(studyService.getStatistics(1)).thenReturn(Result.ok(stats));

        Result result = studyController.getStatistics(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getWeekStatistics_withUserId_shouldReturnWeekStats() {
        Map<String, Object> weekStats = new HashMap<>();
        weekStats.put("hours", 20);
        weekStats.put("days", 5);

        when(studyService.getWeekStatistics(1)).thenReturn(Result.ok(weekStats));

        Result result = studyController.getWeekStatistics(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getConsecutiveDays_withUserId_shouldReturnConsecutiveDays() {
        when(studyService.getConsecutiveDays(1)).thenReturn(Result.ok(10));

        Result result = studyController.getConsecutiveDays(1);

        assertTrue(result.isSuccess());
        verify(studyService).getConsecutiveDays(1);
    }

    @Test
    void health_shouldReturnServiceStatus() {
        Result result = studyController.health();

        assertTrue(result.isSuccess());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("UP", data.get("status"));
        assertEquals("study-service", data.get("service"));
    }
}
