package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Func05ProjectPaginationJdbcTest {
    private ProjectDAO dao;

    @BeforeEach
    void createSchemaAndSeed() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:func05_project;MODE=MySQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("DROP TABLE IF EXISTS project_member");
        jdbc.execute("DROP TABLE IF EXISTS project");
        jdbc.execute("CREATE TABLE project (id INT PRIMARY KEY, name VARCHAR(100), description VARCHAR(200), " +
                "category VARCHAR(50), year INT, status VARCHAR(30), leader_id INT, admin_id INT, " +
                "budget DECIMAL(10,2), repo_url VARCHAR(200), expected_start_date DATE, " +
                "expected_end_date DATE, created_at TIMESTAMP, updated_at TIMESTAMP, deleted INT)");
        jdbc.execute("CREATE TABLE project_member (project_id INT, user_id INT, role VARCHAR(30))");
        jdbc.update("INSERT INTO project (id,name,description,category,year,status,leader_id,admin_id,created_at,deleted) " +
                "VALUES (1,'甲','测试','AI',2026,'approved',1,NULL,'2026-01-01 10:00:00',0)," +
                "(2,'乙','测试','AI',2026,'approved',1,2,'2026-01-01 10:00:00',0)," +
                "(3,'丙','测试','AI',2026,'approved',1,NULL,'2026-01-01 10:00:00',0)," +
                "(4,'丁','测试','AI',2026,'pending',1,NULL,'2026-01-04 10:00:00',0)," +
                "(5,'戊','测试','AI',2025,'completed',2,NULL,'2026-01-05 10:00:00',0)," +
                "(6,'己','测试','AI',2026,'approved',1,NULL,'2026-01-06 10:00:00',1)");
        jdbc.update("INSERT INTO project_member (project_id,user_id,role) VALUES " +
                "(1,2,'MEMBER'),(5,2,'MEMBER')");
        dao = new ProjectDAO(jdbc);
    }

    @Test
    void adminAndPublicCountsMatchStableDatabasePages() {
        assertEquals(5, dao.countByConditions(null, null, null, false));
        assertEquals(3, dao.countByConditions(null, "APPROVED", 2026, false));
        assertEquals(List.of(3, 2), ids(dao.findPageByConditions(null, "approved", 2026, false, 2, 0)));
        assertEquals(List.of(1), ids(dao.findPageByConditions(null, "approved", 2026, false, 2, 2)));

        assertEquals(4, dao.countByConditions(null, null, null, true));
        assertEquals(0, dao.countByConditions(null, "pending", null, true));
        assertEquals(List.of(5, 3), ids(dao.findPageByConditions(null, null, null, true, 2, 0)));
    }

    @Test
    void myProjectPagesDeduplicateLeaderAndMembershipAndExcludeDeleted() {
        assertEquals(3, dao.countProjectsByUserId(2));
        assertEquals(List.of(5, 2), ids(dao.findPageByUserId(2, 2, 0)));
        assertEquals(List.of(1), ids(dao.findPageByUserId(2, 2, 2)));
    }

    @Test
    void createdByMeUsesLeaderOnlyWithStablePagination() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:func05_project;MODE=MySQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.update("INSERT INTO project (id,name,description,category,year,status,leader_id,admin_id,created_at,deleted) " +
                "VALUES (7,'庚','测试','AI',2026,'approved',2,NULL,'2026-02-01 10:00:00',0)," +
                "(8,'辛','测试','AI',2026,'approved',2,NULL,'2026-02-01 10:00:00',0)," +
                "(9,'壬','测试','AI',2026,'approved',2,NULL,'2026-02-02 10:00:00',1)");

        assertEquals(5, dao.countProjectsByUserId(2));
        assertEquals(3, dao.countProjectsByLeaderId(2));
        assertEquals(List.of(8, 7), ids(dao.findPageByLeaderId(2, 2, 0)));
        assertEquals(List.of(5), ids(dao.findPageByLeaderId(2, 2, 2)));
        assertEquals(0, dao.countProjectsByLeaderId(99));
    }

    private List<Integer> ids(List<Project> projects) {
        return projects.stream().map(Project::getId).toList();
    }
}
