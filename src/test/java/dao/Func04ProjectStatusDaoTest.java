package dao;

import model.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Func04ProjectStatusDaoTest {
    private final ProjectDAO dao = new ProjectDAO();

    @BeforeAll
    static void initDatabase() {
        H2Database.initSchema();
    }

    @BeforeEach
    void seedEveryProjectStatus() throws Exception {
        H2Database.reset();
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project (id, name, year, leader_id, status, deleted) VALUES " +
                    "(201, '待审', 2026, 1, 'pending', 0), " +
                    "(202, '已批', 2026, 1, 'approved', 0), " +
                    "(203, '进行', 2026, 1, 'in_progress', 0), " +
                    "(204, '完成', 2026, 1, 'completed', 0), " +
                    "(205, '取消', 2026, 1, 'canceled', 0), " +
                    "(206, '驳回', 2026, 1, 'rejected', 0), " +
                    "(207, '已删除', 2026, 1, 'approved', 1)");
        }
    }

    @Test
    void everyStatusFilterUsesCanonicalValue() {
        List<String> statuses = List.of(Project.STATUS_PENDING, Project.STATUS_APPROVED,
                Project.STATUS_IN_PROGRESS, Project.STATUS_COMPLETED,
                Project.STATUS_CANCELED, Project.STATUS_REJECTED);
        for (int index = 0; index < statuses.size(); index++) {
            String status = statuses.get(index);
            int expectedId = 201 + index;
            assertEquals(List.of(expectedId), dao.findByConditions(null, status, null)
                    .stream().map(Project::getId).toList());
            assertEquals(List.of(expectedId), dao.findByConditions(null, status.toUpperCase(java.util.Locale.ROOT), null)
                    .stream().map(Project::getId).toList());
        }
    }

    @Test
    void approvedListAndCountExcludeDeletedProjects() {
        assertEquals(List.of(202), dao.findApprovedProjects().stream().map(Project::getId).toList());
        assertEquals(6, dao.count());
    }

    @Test
    void approvalRejectionAndDirectUpdatePersistLowercase() throws Exception {
        assertTrue(dao.approve(201, 1));
        assertTrue(dao.reject(206, 1));
        Project project = dao.findById(203);
        project.setStatus("COMPLETED");
        assertTrue(dao.update(project));

        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, status FROM project WHERE id IN (201,203,206) ORDER BY id")) {
            assertTrue(rs.next());
            assertEquals(Project.STATUS_APPROVED, rs.getString("status"));
            assertTrue(rs.next());
            assertEquals(Project.STATUS_COMPLETED, rs.getString("status"));
            assertTrue(rs.next());
            assertEquals(Project.STATUS_REJECTED, rs.getString("status"));
        }
        assertEquals(List.of(201, 202), dao.findApprovedProjects().stream().map(Project::getId).sorted().toList());
    }
}
