package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Aud04LogFiltersTest {
    @Test
    void optionsMatchEveryValueWrittenByTheFilter() {
        assertEquals(List.of(AuditLogCategory.Operation.GET, AuditLogCategory.Operation.POST, AuditLogCategory.Operation.PUT,
                AuditLogCategory.Operation.PATCH, AuditLogCategory.Operation.DELETE), AuditLogCategory.operations());
        assertNull(AuditLogCategory.loggedOperation("GET"));
        assertEquals(AuditLogCategory.Operation.GET,
                AuditLogCategory.loggedOperation("GET", "/software-group/admin/member/list"));
        assertNull(AuditLogCategory.loggedOperation("GET", "/software-group/admin/log/list"));
        assertEquals(AuditLogCategory.Operation.PATCH, AuditLogCategory.loggedOperation("patch"));
        assertEquals("管理员", AuditLogCategory.moduleForUri("/admin/project").getValue());
        assertEquals("成员", AuditLogCategory.moduleForUri("/member/project").getValue());
        assertEquals("招新", AuditLogCategory.moduleForUri("/api/recruit").getValue());
        assertTrue(AuditLogCategory.modules().stream().noneMatch(module -> "用户".equals(module.getValue())));
    }

    @Test
    void explicitAndLegacyRangesUseBeijingWallClockAndExclusiveEnd() {
        LogTimeRange explicit = LogTimeRange.explicit("2026-11-16T10:00", "2026-11-16T11:00");
        assertEquals(LocalDateTime.of(2026, 11, 16, 10, 0), explicit.getStart());
        assertEquals(LocalDateTime.of(2026, 11, 16, 11, 0), explicit.getEnd());

        LogTimeRange day = LogTimeRange.legacy("2026-11-16");
        assertEquals(LocalDateTime.of(2026, 11, 16, 0, 0), day.getStart());
        assertEquals(LocalDateTime.of(2026, 11, 17, 0, 0), day.getEnd());
        assertNotNull(LogTimeRange.legacy("7").getStart());
        assertNull(LogTimeRange.legacy("7").getEnd());
        assertThrows(IllegalArgumentException.class,
                () -> LogTimeRange.explicit("2026-11-16T11:00", "2026-11-16T10:00"));
        assertThrows(IllegalArgumentException.class, () -> LogTimeRange.legacy("2026-99-99"));
    }
}
