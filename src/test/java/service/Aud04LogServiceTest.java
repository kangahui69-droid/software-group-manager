package service;

import dao.OperationLogDAO;
import org.junit.jupiter.api.Test;
import util.Result;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class Aud04LogServiceTest {
    @Test
    void explicitRangeReachesBothQueriesAndInvalidRangeIsRejected() {
        OperationLogDAO dao = mock(OperationLogDAO.class);
        LogService service = new LogService(dao);
        Map<String, Object> filter = Map.of("startTime", "2026-11-16T10:00",
                "endTime", "2026-11-16T11:00", "operation", "PATCH");
        Result result = service.listLogs(filter, 1, 20);
        assertTrue(result.isSuccess());
        LocalDateTime start = LocalDateTime.of(2026, 11, 16, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 16, 11, 0);
        verify(dao).findByTimeRange(isNull(), eq("PATCH"), isNull(), eq(start), eq(end), eq(1), eq(20));
        verify(dao).countByTimeRange(isNull(), eq("PATCH"), isNull(), eq(start), eq(end));

        OperationLogDAO invalidDao = mock(OperationLogDAO.class);
        assertEquals(400, new LogService(invalidDao).listLogs(
                Map.of("startTime", "2026-11-16T11:00", "endTime", "2026-11-16T10:00"),
                1, 20).getCode());
        verifyNoInteractions(invalidDao);
    }
}
