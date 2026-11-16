package service;

import dao.ProjectDAO;
import model.ProjectMemberApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.Result;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Func02ProjectApplicationsServiceTest {
    @Mock private ProjectDAO dao;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService();
        service.setProjectDAO(dao);
    }

    @Test
    void missingUserIdIsRejectedBeforeQuery() {
        assertEquals(400, service.getMyApplications(null, 1, 20).getCode());
        verifyNoInteractions(dao);
    }

    @Test
    void emptyApplicationsReturnAnEmptyFirstPage() {
        when(dao.countMyApplications(7)).thenReturn(0);

        Result result = service.getMyApplications(7, 5, 20);

        assertTrue(result.isSuccess());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(List.of(), data.get("list"));
        assertEquals(0, data.get("total"));
        assertEquals(1, data.get("page"));
        assertEquals(0, data.get("totalPages"));
        verify(dao, never()).getMyApplications(7, 20, 0);
    }

    @Test
    void outOfRangePageUsesLastPageAndCurrentUsersId() {
        ProjectMemberApplication application = new ProjectMemberApplication();
        application.setUserId(7);
        when(dao.countMyApplications(7)).thenReturn(5);
        when(dao.getMyApplications(7, 2, 4)).thenReturn(List.of(application));

        Result result = service.getMyApplications(7, 99, 2);

        assertTrue(result.isSuccess());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(List.of(application), data.get("list"));
        assertEquals(5, data.get("total"));
        assertEquals(3, data.get("page"));
        assertEquals(2, data.get("pageSize"));
        assertEquals(3, data.get("totalPages"));
        verify(dao).getMyApplications(7, 2, 4);
    }

    @Test
    void daoFailureIsNotShownAsAnEmptyList() {
        when(dao.countMyApplications(7)).thenThrow(new RuntimeException("数据库故障"));

        Result result = service.getMyApplications(7, 1, 20);

        assertEquals(500, result.getCode());
    }
}
