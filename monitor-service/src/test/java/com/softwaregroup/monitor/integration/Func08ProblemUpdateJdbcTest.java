package com.softwaregroup.monitor.integration;

import com.softwaregroup.monitor.dao.ProblemReportDAOImpl;
import com.softwaregroup.monitor.dao.UserDAO;
import com.softwaregroup.monitor.model.ProblemReport;
import com.softwaregroup.monitor.model.User;
import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.service.ProblemService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Func08ProblemUpdateJdbcTest {
    private JdbcTemplate jdbc;
    private ProblemReportDAOImpl dao;
    private ProblemService service;

    @BeforeEach
    void seedDatabase() {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:func08_problem;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("DROP TABLE IF EXISTS problem_report");
        jdbc.execute("CREATE TABLE problem_report (id INT PRIMARY KEY, title VARCHAR(200) NOT NULL, " +
                "content TEXT NOT NULL, reporter_name VARCHAR(100), reporter_contact VARCHAR(100), " +
                "user_id INT, category VARCHAR(50), status VARCHAR(50), admin_comment TEXT, " +
                "created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbc.update("INSERT INTO problem_report " +
                "(id,title,content,reporter_name,reporter_contact,user_id,category,status,admin_comment,created_at,updated_at) " +
                "VALUES (1,'旧标题','旧内容','旧姓名','旧联系方式',2,'UNVERIFIED','PENDING','原备注'," +
                "'2020-01-01 00:00:00','2020-01-01 00:00:00')," +
                "(2,'其他人的标题','其他人的内容','其他姓名','其他联系方式',3,'VERIFIED','SOLVING',NULL," +
                "'2020-01-01 00:00:00','2020-01-01 00:00:00')");
        dao = new ProblemReportDAOImpl(jdbc);
        UserDAO users = mock(UserDAO.class);
        User admin = new User();
        admin.setRole("ADMIN");
        when(users.findById(1)).thenReturn(admin);
        User member = new User();
        member.setRole("MEMBER");
        when(users.findById(4)).thenReturn(member);
        service = new ProblemService(dao, users);
    }

    @Test
    void ownerUpdatePersistsAllEditableFieldsAndPreservesWorkflowFields() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("  新标题  ");
        dto.setContent("  新内容  ");
        dto.setReporterName("  新姓名  ");
        dto.setReporterContact("  新联系方式  ");

        Result result = service.updateProblem(1, dto, 2);

        assertTrue(result.isSuccess());
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM problem_report WHERE id=1");
        assertEquals("新标题", row.get("TITLE"));
        assertEquals("新内容", row.get("CONTENT"));
        assertEquals("新姓名", row.get("REPORTER_NAME"));
        assertEquals("新联系方式", row.get("REPORTER_CONTACT"));
        assertEquals("UNVERIFIED", row.get("CATEGORY"));
        assertEquals("PENDING", row.get("STATUS"));
        assertEquals("原备注", row.get("ADMIN_COMMENT"));
        assertEquals(2, row.get("USER_ID"));
        assertTrue(((java.sql.Timestamp) row.get("UPDATED_AT")).after(
                java.sql.Timestamp.valueOf("2020-01-01 00:00:00")));
    }

    @Test
    void partialUpdateKeepsAbsentFieldsAndAdminMayUpdateAnotherOwnersProblem() {
        ProblemDTO titleOnly = new ProblemDTO();
        titleOnly.setTitle("只改标题");
        assertTrue(service.updateProblem(1, titleOnly, 2).isSuccess());
        assertEquals("旧内容", jdbc.queryForObject(
                "SELECT content FROM problem_report WHERE id=1", String.class));

        ProblemDTO adminEdit = new ProblemDTO();
        adminEdit.setContent("管理员修改内容");
        assertTrue(service.updateProblem(2, adminEdit, 1).isSuccess());
        assertEquals("管理员修改内容", jdbc.queryForObject(
                "SELECT content FROM problem_report WHERE id=2", String.class));
        assertEquals("其他人的标题", jdbc.queryForObject(
                "SELECT title FROM problem_report WHERE id=2", String.class));
    }

    @Test
    void otherMemberCannotUpdateAndOwnerPredicateRejectsStaleOwnership() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("越权修改");
        Result denied = service.updateProblem(1, dto, 4);
        assertFalse(denied.isSuccess());
        assertEquals(403, denied.getCode());
        assertEquals("旧标题", jdbc.queryForObject(
                "SELECT title FROM problem_report WHERE id=1", String.class));

        ProblemReport stale = dao.findById(1);
        stale.setTitle("旧所有者修改");
        jdbc.update("UPDATE problem_report SET user_id=3 WHERE id=1");
        assertEquals(0, dao.updateDetails(stale, 2, false));
        assertEquals("旧标题", jdbc.queryForObject(
                "SELECT title FROM problem_report WHERE id=1", String.class));
    }
}
