package com.softwaregroup.project.controller;

import com.softwaregroup.project.model.dto.AwardDTO;
import com.softwaregroup.project.service.AwardService;
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
 * AwardController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AwardControllerTest {

    @Mock
    private AwardService awardService;

    private AwardController awardController;

    @BeforeEach
    void setUp() throws Exception {
        awardController = new AwardController();
        // Use reflection to inject the mock service (since @Autowired is used without setter)
        java.lang.reflect.Field field = AwardController.class.getDeclaredField("awardService");
        field.setAccessible(true);
        field.set(awardController, awardService);
    }

    @Test
    void listAwards_withDefaultParams_shouldReturnAwardList() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);
        pageResult.put("page", 1);

        when(awardService.listAwards(isNull(), eq(1))).thenReturn(Result.ok(pageResult));

        Result result = awardController.listAwards(null, 1);

        assertTrue(result.isSuccess());
        verify(awardService).listAwards(isNull(), eq(1));
    }

    @Test
    void listAwards_withStatusFilter_shouldFilterByStatus() {
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", List.of());
        pageResult.put("total", 0);

        when(awardService.listAwards(eq("PENDING"), eq(1))).thenReturn(Result.ok(pageResult));

        Result result = awardController.listAwards("PENDING", 1);

        assertTrue(result.isSuccess());
        verify(awardService).listAwards(eq("PENDING"), eq(1));
    }

    @Test
    void getAwardDetail_withValidId_shouldReturnAwardDetail() {
        Map<String, Object> awardDetail = new HashMap<>();
        awardDetail.put("id", 1);
        awardDetail.put("competition", "挑战杯");

        when(awardService.getAwardDetail(1)).thenReturn(Result.ok(awardDetail));

        Result result = awardController.getAwardDetail(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getAwardDetail_withInvalidId_shouldReturnError() {
        when(awardService.getAwardDetail(999)).thenReturn(Result.error(404, "奖项不存在"));

        Result result = awardController.getAwardDetail(999);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
    }

    @Test
    void getAwardImages_withValidId_shouldReturnImages() {
        when(awardService.getAwardImages(1)).thenReturn(Result.ok(List.of()));

        Result result = awardController.getAwardImages(1);

        assertTrue(result.isSuccess());
        verify(awardService).getAwardImages(1);
    }

    @Test
    void getMyAwards_withUserId_shouldReturnMyAwards() {
        when(awardService.getMyAwards(1)).thenReturn(Result.ok(List.of()));

        Result result = awardController.getMyAwards(1);

        assertTrue(result.isSuccess());
        verify(awardService).getMyAwards(1);
    }

    @Test
    void getAwardStatistics_withUserId_shouldReturnStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAwards", 10);
        stats.put("pendingAwards", 2);

        when(awardService.getAwardStatistics(1)).thenReturn(Result.ok(stats));

        Result result = awardController.getAwardStatistics(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void submitAward_withValidData_shouldReturnSuccess() {
        AwardDTO dto = new AwardDTO();
        dto.setCompetition("挑战杯");
        dto.setCompetitionTime("2026-05-01");

        when(awardService.submitAward(any(AwardDTO.class), eq(1), isNull()))
                .thenReturn(Result.ok(Map.of("id", 1)));

        Result result = awardController.submitAward(dto, 1);

        assertTrue(result.isSuccess());
        verify(awardService).submitAward(any(AwardDTO.class), eq(1), isNull());
    }

    @Test
    void updateAward_withValidData_shouldReturnSuccess() {
        AwardDTO dto = new AwardDTO();
        dto.setCompetition("互联网+");

        when(awardService.updateAward(eq(1), any(AwardDTO.class), eq(1)))
                .thenReturn(Result.ok());

        Result result = awardController.updateAward(1, dto, 1);

        assertTrue(result.isSuccess());
        verify(awardService).updateAward(eq(1), any(AwardDTO.class), eq(1));
    }

    @Test
    void deleteAward_withValidId_shouldReturnSuccess() {
        when(awardService.deleteAward(1, 1)).thenReturn(Result.ok());

        Result result = awardController.deleteAward(1, 1);

        assertTrue(result.isSuccess());
        verify(awardService).deleteAward(1, 1);
    }

    @Test
    void approveAward_withValidId_shouldReturnSuccess() {
        Map<String, Integer> body = new HashMap<>();
        body.put("operatorId", 1);

        when(awardService.approveAward(1, 1)).thenReturn(Result.ok());

        Result result = awardController.approveAward(1, body);

        assertTrue(result.isSuccess());
        verify(awardService).approveAward(1, 1);
    }

    @Test
    void rejectAward_withValidId_shouldReturnSuccess() {
        Map<String, Object> body = new HashMap<>();
        body.put("reason", "材料不全");
        body.put("operatorId", 1);

        when(awardService.rejectAward(eq(1), eq("材料不全"), eq(1)))
                .thenReturn(Result.ok());

        Result result = awardController.rejectAward(1, body);

        assertTrue(result.isSuccess());
        verify(awardService).rejectAward(eq(1), eq("材料不全"), eq(1));
    }
}
