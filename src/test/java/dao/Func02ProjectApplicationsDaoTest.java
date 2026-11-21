package dao;

import model.ProjectMemberApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class Func02ProjectApplicationsDaoTest {
    private final ProjectDAO dao = new ProjectDAO();

    @BeforeAll
    static void initDatabase() {
        H2Database.initSchema();
    }

    @BeforeEach
    void seedApplications() throws Exception {
        H2Database.reset();
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            // 历史档案可能重复，申请列表的分页不能因此出现重复行。
            stmt.execute("INSERT INTO member_profile (user_id, student_id, major, grade) " +
                    "VALUES (2, '2026X', '软件工程', '2026')");
            stmt.execute("INSERT INTO project (id, name, year, leader_id, status) VALUES " +
                    "(101, '项目甲', 2026, 1, 'approved'), " +
                    "(102, '项目乙', 2026, 1, 'approved')");
            stmt.execute("INSERT INTO project_member_application " +
                    "(id, project_id, user_id, status, applied_at, handled_at) VALUES " +
                    "(501, 101, 2, 'PENDING', '2026-01-01 10:00:00', NULL), " +
                    "(502, 101, 2, 'CONFIRMED', '2026-01-02 10:00:00', '2026-01-03 10:00:00'), " +
                    "(503, 102, 2, 'REJECTED', '2026-01-04 10:00:00', '2026-01-05 10:00:00'), " +
                    "(504, 102, 1, 'PENDING', '2026-01-06 10:00:00', NULL)");
        }
    }

    @Test
    void pagesAreOrderedAndContainOnlyCurrentUsersApplications() {
        assertEquals(3, dao.countMyApplications(2));
        assertEquals(1, dao.countMyApplications(1));

        List<ProjectMemberApplication> first = dao.getMyApplications(2, 2, 0);
        List<ProjectMemberApplication> second = dao.getMyApplications(2, 2, 2);

        assertEquals(List.of(503, 502), first.stream().map(ProjectMemberApplication::getId).toList());
        assertEquals(List.of(501), second.stream().map(ProjectMemberApplication::getId).toList());
        assertEquals("项目乙", first.get(0).getProjectName());
        assertEquals("2026X", first.get(0).getStudentId());
        assertEquals("REJECTED", first.get(0).getStatus());
        assertNotNull(first.get(0).getHandledAt());
        assertEquals("CONFIRMED", first.get(1).getStatus());
        assertNotNull(first.get(1).getHandledAt());
        assertEquals("PENDING", second.get(0).getStatus());
        assertNull(second.get(0).getHandledAt());
    }

    @Test
    void userWithoutApplicationsGetsEmptyPage() {
        assertEquals(0, dao.countMyApplications(999));
        assertEquals(List.of(), dao.getMyApplications(999, 20, 0));
    }
}
