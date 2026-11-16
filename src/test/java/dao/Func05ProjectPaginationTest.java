package dao;

import dto.ProjectFilterDTO;
import model.Project;
import model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ProjectService;
import support.H2Database;
import util.DBUtil;
import util.Result;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Func05ProjectPaginationTest {
    private final ProjectDAO dao = new ProjectDAO();
    private final ProjectService service = new ProjectService();

    @BeforeAll
    static void initDatabase() {
        H2Database.initSchema();
    }

    @BeforeEach
    void seedProjects() throws Exception {
        H2Database.reset();
        service.setProjectDAO(dao);
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project " +
                    "(id,name,description,category,year,status,leader_id,admin_id,created_at,deleted) VALUES " +
                    "(1001,'科研甲','分页测试','AI',2026,'approved',1,NULL,'2026-01-01 10:00:00',0)," +
                    "(1002,'科研乙','分页测试','AI',2026,'approved',1,2,'2026-01-01 10:00:00',0)," +
                    "(1003,'科研丙','分页测试','AI',2026,'approved',1,NULL,'2026-01-01 10:00:00',0)," +
                    "(1004,'科研旧','分页测试','AI',2025,'approved',2,NULL,'2026-01-04 10:00:00',0)," +
                    "(1005,'科研待审','分页测试','AI',2026,'pending',1,NULL,'2026-01-05 10:00:00',0)," +
                    "(1006,'其他项目','分页测试','WEB',2026,'approved',1,NULL,'2026-01-06 10:00:00',0)," +
                    "(1007,'科研已删除','分页测试','AI',2026,'approved',1,NULL,'2026-01-07 10:00:00',1)");
            stmt.execute("INSERT INTO project_member (project_id,user_id,role) VALUES " +
                    "(1001,2,'MEMBER'),(1004,2,'MEMBER')");
        }
    }

    @Test
    void filteredCountAndDatabasePagesUseTheSameConditionsAndStableOrder() {
        assertEquals(3, dao.countByConditions("科研", "APPROVED", "AI", 2026));
        assertEquals(List.of(1003, 1002), ids(dao.findPageByConditions("科研", "approved", "AI", 2026, 2, 0)));
        assertEquals(List.of(1001), ids(dao.findPageByConditions("科研", "approved", "AI", 2026, 2, 2)));
        assertEquals(0, dao.countByConditions("不存在", null, null, null));
    }

    @Test
    void serviceReturnsPageMetadataAndClampsOutOfRangePage() {
        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setKeyword("科研");
        filter.setStatus("approved");
        filter.setCategory("AI");
        filter.setYear(2026);

        Result result = service.listProjectsForAdmin(filter, 99, 2, admin);
        assertTrue(result.isSuccess());
        Map<?, ?> page = (Map<?, ?>) result.getData();
        assertEquals(List.of(1001), ids((List<Project>) page.get("list")));
        assertEquals(3, page.get("total"));
        assertEquals(2, page.get("page"));
        assertEquals(2, page.get("pageSize"));
        assertEquals(2, page.get("totalPages"));
    }

    @Test
    void publicAndMyProjectsPageInDatabaseWithoutLeakingOrDuplicatingRows() {
        Result publicResult = service.browseProjects(new ProjectFilterDTO(), 2, 2);
        Map<?, ?> publicPage = (Map<?, ?>) publicResult.getData();
        assertEquals(5, publicPage.get("total"));
        assertEquals(List.of(1002, 1001), ids((List<Project>) publicPage.get("list")));

        Result mine = service.getMyProjects(2, 99, 2);
        Map<?, ?> myPage = (Map<?, ?>) mine.getData();
        assertEquals(3, myPage.get("total"));
        assertEquals(2, myPage.get("page"));
        assertEquals(List.of(1001), ids((List<Project>) myPage.get("list")));
        assertEquals(3, dao.countProjectsByUserId(2));
    }

    @Test
    void emptyResultHasDefinedPageAndZeroTotalPages() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setKeyword("不存在");
        Map<?, ?> page = (Map<?, ?>) service.listProjects(filter, -1, 0).getData();
        assertEquals(List.of(), page.get("list"));
        assertEquals(0, page.get("total"));
        assertEquals(1, page.get("page"));
        assertEquals(20, page.get("pageSize"));
        assertEquals(0, page.get("totalPages"));
    }

    @Test
    void createdByMeIncludesOnlyCurrentLeaderAndKeepsPageMetadata() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project " +
                    "(id,name,description,category,year,status,leader_id,admin_id,created_at,deleted) VALUES " +
                    "(1008,'我负责甲','测试','AI',2026,'approved',2,NULL,'2026-02-01 10:00:00',0)," +
                    "(1009,'我负责乙','测试','AI',2026,'approved',2,NULL,'2026-02-01 10:00:00',0)," +
                    "(1010,'已删除负责人项目','测试','AI',2026,'approved',2,NULL,'2026-02-03 10:00:00',1)");
        }

        assertEquals(5, dao.countProjectsByUserId(2));
        assertEquals(3, dao.countProjectsByLeaderId(2));
        assertEquals(List.of(1009, 1008), ids(dao.findPageByLeaderId(2, 2, 0)));
        assertEquals(List.of(1004), ids(dao.findPageByLeaderId(2, 2, 2)));

        Map<?, ?> mine = (Map<?, ?>) service.getMyProjects(2, 1, 10).getData();
        assertEquals(List.of(1009, 1008, 1004, 1002, 1001), ids((List<Project>) mine.get("list")));

        Map<?, ?> created = (Map<?, ?>) service.getCreatedByMeProjects(2, 99, 2).getData();
        assertEquals(List.of(1004), ids((List<Project>) created.get("list")));
        assertEquals(3, created.get("total"));
        assertEquals(2, created.get("page"));
        assertEquals(2, created.get("pageSize"));
        assertEquals(2, created.get("totalPages"));

        Map<?, ?> empty = (Map<?, ?>) service.getCreatedByMeProjects(99, 1, 0).getData();
        assertEquals(List.of(), empty.get("list"));
        assertEquals(0, empty.get("total"));
        assertEquals(1, empty.get("page"));
        assertEquals(20, empty.get("pageSize"));
        assertEquals(0, empty.get("totalPages"));
    }

    private List<Integer> ids(List<Project> projects) {
        return projects.stream().map(Project::getId).toList();
    }
}
