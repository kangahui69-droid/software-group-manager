package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.service.AttendanceService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttendanceController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() throws Exception {
        attendanceController = new AttendanceController();
        // Use reflection to inject the mock service (since @Autowired is used without setter)
        java.lang.reflect.Field field = AttendanceController.class.getDeclaredField("attendanceService");
        field.setAccessible(true);
        field.set(attendanceController, attendanceService);
    }

    @Test
    void checkIn_withValidUserId_shouldReturnSuccess() {
        when(attendanceService.checkIn(1)).thenReturn(Result.ok("NORMAL"));

        Result result = attendanceController.checkIn(1);

        assertTrue(result.isSuccess());
        verify(attendanceService).checkIn(1);
    }

    @Test
    void checkIn_withNullUserId_shouldReturnError() {
        when(attendanceService.checkIn(null)).thenReturn(Result.error(400, "用户ID不能为空"));

        Result result = attendanceController.checkIn(null);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void checkOut_withValidUserId_shouldReturnSuccess() {
        when(attendanceService.checkOut(1)).thenReturn(Result.ok("NORMAL"));

        Result result = attendanceController.checkOut(1);

        assertTrue(result.isSuccess());
        verify(attendanceService).checkOut(1);
    }

    @Test
    void listAttendance_withDefaultPage_shouldReturnAttendanceList() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);

        when(attendanceService.listAttendance(anyMap(), eq(1))).thenReturn(Result.ok(pageResult));

        Result result = attendanceController.listAttendance(new HashMap<>(), 1);

        assertTrue(result.isSuccess());
        verify(attendanceService).listAttendance(anyMap(), eq(1));
    }

    @Test
    void getAttendanceStats_withUserId_shouldReturnStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", 20);
        stats.put("presentDays", 18);

        when(attendanceService.getAttendanceStats(1)).thenReturn(Result.ok(stats));

        Result result = attendanceController.getAttendanceStats(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getMyAttendance_withUserId_shouldReturnMyAttendance() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);

        when(attendanceService.getMyAttendance(1, 1)).thenReturn(Result.ok(pageResult));

        Result result = attendanceController.getMyAttendance(1, 1);

        assertTrue(result.isSuccess());
        verify(attendanceService).getMyAttendance(1, 1);
    }

    @Test
    void getMyStats_withUserId_shouldReturnMyStats() {
        Map<String, Object> myStats = new HashMap<>();
        myStats.put("onTimeRate", 0.9);

        when(attendanceService.getMyStats(1)).thenReturn(Result.ok(myStats));

        Result result = attendanceController.getMyStats(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void applyMakeup_withValidData_shouldReturnSuccess() {
        Map<String, Object> body = new HashMap<>();
        body.put("date", System.currentTimeMillis());
        body.put("reason", "因病缺席");

        when(attendanceService.applyMakeup(any(Date.class), eq("因病缺席"), eq(1)))
                .thenReturn(Result.ok());

        Result result = attendanceController.applyMakeup(body, 1);

        assertTrue(result.isSuccess());
        verify(attendanceService).applyMakeup(any(Date.class), eq("因病缺席"), eq(1));
    }

    @Test
    void approveMakeup_withValidId_shouldReturnSuccess() {
        when(attendanceService.approveMakeup(1, 1)).thenReturn(Result.ok());

        Result result = attendanceController.approveMakeup(1, 1);

        assertTrue(result.isSuccess());
        verify(attendanceService).approveMakeup(1, 1);
    }

    @Test
    void rejectMakeup_withValidId_shouldReturnSuccess() {
        when(attendanceService.rejectMakeup(1, 1)).thenReturn(Result.ok());

        Result result = attendanceController.rejectMakeup(1, 1);

        assertTrue(result.isSuccess());
        verify(attendanceService).rejectMakeup(1, 1);
    }

    @Test
    void getPendingMakeupList_withDefaultPage_shouldReturnPendingList() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);

        when(attendanceService.getPendingMakeupList(1)).thenReturn(Result.ok(pageResult));

        Result result = attendanceController.getPendingMakeupList(1);

        assertTrue(result.isSuccess());
        verify(attendanceService).getPendingMakeupList(1);
    }

    @Test
    void health_shouldReturnServiceStatus() {
        Result result = attendanceController.health();

        assertTrue(result.isSuccess());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("UP", data.get("status"));
        assertEquals("attendance-service", data.get("service"));
    }
}
