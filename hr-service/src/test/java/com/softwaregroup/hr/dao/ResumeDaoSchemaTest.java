package com.softwaregroup.hr.dao;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeDaoSchemaTest {

    @Test
    void resumeDaosQueryTheTablesDefinedByTheBusinessSchema() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:resume_dao_schema;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE resumes (id INT PRIMARY KEY, user_id INT, deleted INT, is_default INT)");
        jdbc.execute("CREATE TABLE resume_awards (id INT PRIMARY KEY, resume_id INT, display_order INT)");
        jdbc.execute("CREATE TABLE resume_educations (id INT PRIMARY KEY, resume_id INT, display_order INT)");
        jdbc.execute("CREATE TABLE resume_projects (id INT PRIMARY KEY, resume_id INT, display_order INT)");
        jdbc.execute("CREATE TABLE resume_skills (id INT PRIMARY KEY, resume_id INT, display_order INT)");

        assertThat(new ResumeDAOImpl(jdbc).findByUserId(1)).isEmpty();
        assertThat(new ResumeAwardDAOImpl(jdbc).findByResumeId(1)).isEmpty();
        assertThat(new ResumeEducationDAOImpl(jdbc).findByResumeId(1)).isEmpty();
        assertThat(new ResumeProjectDAOImpl(jdbc).findByResumeId(1)).isEmpty();
        assertThat(new ResumeSkillDAOImpl(jdbc).findByResumeId(1)).isEmpty();
    }
}
