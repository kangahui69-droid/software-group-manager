package com.softwaregroup.project.integration;

import com.softwaregroup.project.dao.*;
import com.softwaregroup.project.model.Award;
import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.ProjectMemberApplication;
import com.softwaregroup.project.model.dto.AwardDTO;
import com.softwaregroup.project.model.dto.PlanDTO;
import com.softwaregroup.project.model.dto.ProgressDTO;
import com.softwaregroup.project.model.dto.ProjectDTO;
import com.softwaregroup.project.service.AwardService;
import com.softwaregroup.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs only against an isolated MySQL database loaded with sql/software_group.sql. */
@EnabledIfEnvironmentVariable(named = "MIG01_MYSQL_URL", matches = ".+")
class ProjectAwardMySqlTest {
    private JdbcTemplate jdbc;
    private DriverManagerDataSource dataSource;
    private ProjectDAO projects;
    private AwardDAO awards;
    private AwardImageDAO images;
    private UserDAO users;
    private Integer ownerId;
    private Integer memberId;
    private Integer adminId;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(
                System.getenv("MIG01_MYSQL_URL"),
                System.getenv().getOrDefault("MIG01_MYSQL_USER", "root"),
                System.getenv().getOrDefault("MIG01_MYSQL_PASSWORD", ""));
        jdbc = new JdbcTemplate(dataSource);
        // The URL must point to a disposable test database. Never pass a production URL.
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertThat(database).isEqualTo("mig01_test");
        for (String table : List.of("project_progress", "project_plan", "project_history",
                "project_member_application", "project_member", "project_label", "project_image",
                "project_file", "award_image", "award_member", "project", "award",
                "file_storage", "dictionary", "user")) {
            jdbc.update("DELETE FROM `" + table + "`");
        }
        jdbc.update("INSERT INTO user (username,password,name,email,role,status) VALUES " +
                "('mig01_owner','test-hash','Owner','owner@example.invalid','MEMBER',1)," +
                "('mig01_member','test-hash','Member','member@example.invalid','MEMBER',1)," +
                "('mig01_admin','test-hash','Admin','admin@example.invalid','ADMIN',1)");
        ownerId = jdbc.queryForObject("SELECT id FROM user WHERE username='mig01_owner'", Integer.class);
        memberId = jdbc.queryForObject("SELECT id FROM user WHERE username='mig01_member'", Integer.class);
        adminId = jdbc.queryForObject("SELECT id FROM user WHERE username='mig01_admin'", Integer.class);
        projects = new ProjectDAO(jdbc);
        awards = new AwardDAO(jdbc);
        images = new AwardImageDAO(jdbc);
        users = new UserDAO(jdbc);
    }

    @Test
    void projectWritesAndReadsRealTables() {
        ProjectService service = new ProjectService(projects, users);
        ProjectDTO dto = new ProjectDTO();
        dto.setName("MIG-01 test project");
        dto.setYear(2026);
        dto.setDescription("initial");
        dto.setCategory("software");

        var created = service.createProject(dto, ownerId);
        assertThat(created.isSuccess()).isTrue();
        Project project = (Project) created.getData();
        assertThat(project.getId()).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM project WHERE id=?", Integer.class,
                project.getId())).isEqualTo(1);
        assertThat(projects.findById(project.getId()).getName()).isEqualTo(dto.getName());
        assertThat(projects.countProjectsByMemberAndYear(ownerId, 2026)).isEqualTo(1);
        assertThat(projects.getProjectMembers(project.getId())).hasSize(1);
        assertThat(projects.getHistory(project.getId())).hasSize(1);

        dto.setDescription("updated");
        assertThat(service.updateProject(project.getId(), dto, ownerId).isSuccess()).isTrue();
        assertThat(projects.findById(project.getId()).getDescription()).isEqualTo("updated");

        assertThat(service.approveProject(project.getId(), adminId).isSuccess()).isTrue();
        assertThat(projects.findById(project.getId()).getStatus()).isEqualTo("approved");
        assertThat(service.applyMember(project.getId(), memberId, "joining").isSuccess()).isTrue();
        Integer applicationId = jdbc.queryForObject("SELECT id FROM project_member_application " +
                "WHERE project_id=? AND user_id=?", Integer.class, project.getId(), memberId);
        ProjectMemberApplication application = projects.getMemberApplicationById(applicationId);
        assertThat(application.getStatus()).isEqualTo("PENDING");
        assertThat(service.approveMember(applicationId, ownerId).isSuccess()).isTrue();
        assertThat(projects.isMember(project.getId(), memberId)).isTrue();
        assertThat(projects.findProjectsByUserId(memberId)).extracting(Project::getId)
                .contains(project.getId());

        PlanDTO plan = new PlanDTO();
        plan.setTitle("Plan");
        plan.setStartDate(Date.valueOf("2026-11-01"));
        plan.setEndDate(Date.valueOf("2026-11-30"));
        assertThat(service.addPlan(project.getId(), plan, memberId).isSuccess()).isTrue();
        ProgressDTO progress = new ProgressDTO();
        progress.setTitle("Progress");
        progress.setCompletionRate(50);
        assertThat(service.addProgress(project.getId(), progress, memberId).isSuccess()).isTrue();
        assertThat(projects.getPlans(project.getId())).hasSize(1);
        assertThat(projects.getProgressList(project.getId())).hasSize(1);
        var detailResult = service.getProjectDetail(project.getId(), memberId, false);
        assertThat(detailResult.isSuccess()).isTrue();
        java.util.Map<?, ?> detail = (java.util.Map<?, ?>) detailResult.getData();
        assertThat((List<?>) detail.get("members")).hasSize(2);
        assertThat((List<?>) detail.get("plans")).hasSize(1);
        assertThat((List<?>) detail.get("progress")).hasSize(1);
        assertThat((List<?>) detail.get("history")).isNotEmpty();
        assertThat(detail.get("isMember")).isEqualTo(true);
        assertThat(detail.get("currentUserRole")).isEqualTo("MEMBER");
        assertThat(jdbc.queryForObject("SELECT created_by FROM project_progress WHERE project_id=?",
                Integer.class, project.getId())).isEqualTo(memberId);

        assertThat(service.deleteProject(project.getId(), ownerId).isSuccess()).isTrue();
        assertThat(projects.findById(project.getId())).isNull();
        assertThat(jdbc.queryForObject("SELECT deleted FROM project WHERE id=?", Integer.class,
                project.getId())).isEqualTo(1);

        dto.setName("MIG-01 rejected project");
        Project rejected = (Project) service.createProject(dto, ownerId).getData();
        assertThat(service.rejectProject(rejected.getId(), "rejected", adminId).isSuccess()).isTrue();
        assertThat(projects.findById(rejected.getId()).getStatus()).isEqualTo("rejected");
        assertThat(projects.reject(rejected.getId(), adminId, null)).isFalse();
    }

    @Test
    void awardWritesMembersImagesAndApprovalToRealTables() {
        jdbc.update("INSERT INTO dictionary (code,name,type) VALUES " +
                "('type','Team','award_type'),('level','First','award_level')");
        Integer typeId = jdbc.queryForObject("SELECT id FROM dictionary WHERE code='type'", Integer.class);
        Integer levelId = jdbc.queryForObject("SELECT id FROM dictionary WHERE code='level'", Integer.class);
        jdbc.update("INSERT INTO file_storage (create_by,original_name,stored_name,file_path) " +
                "VALUES (?,'proof.png','stored.png','/test/stored.png')", ownerId);
        Integer fileId = jdbc.queryForObject("SELECT id FROM file_storage", Integer.class);
        AwardService service = new AwardService(awards, images, users);
        AwardDTO dto = new AwardDTO();
        dto.setCompetition("MIG-01 competition");
        dto.setCompetitionTime("2026-11-01");
        dto.setAwardType(typeId);
        dto.setAwardLevel(levelId);
        dto.setMemberIds(List.of(memberId));

        var created = service.submitAward(dto, ownerId, List.of(fileId));
        assertThat(created.isSuccess()).isTrue();
        Award award = (Award) created.getData();
        assertThat(award.getId()).isPositive();
        assertThat(awards.findById(award.getId()).getCompetition()).isEqualTo(dto.getCompetition());
        assertThat(awards.findByUserId(memberId)).extracting(Award::getId).contains(award.getId());
        assertThat(images.findByAwardId(award.getId())).hasSize(1);
        assertThat(new DictionaryDAO(jdbc).findByCode("level")).isEqualTo("First");
        assertThat(new FileStorageDAO(jdbc).findById(fileId).getFileName()).isEqualTo("proof.png");

        dto.setCompetition("MIG-01 updated competition");
        assertThat(service.updateAward(award.getId(), dto, ownerId).isSuccess()).isTrue();
        assertThat(awards.findById(award.getId()).getCompetition()).isEqualTo(dto.getCompetition());
        assertThat(awards.findById(award.getId()).getName()).isEqualTo(dto.getCompetition());

        assertThat(service.approveAward(award.getId(), adminId).isSuccess()).isTrue();
        assertThat(awards.findByStatus("APPROVED")).extracting(Award::getId).contains(award.getId());
        assertThat(awards.approveAward(award.getId(), adminId)).isFalse();
        assertThat(jdbc.queryForObject("SELECT approved_by FROM award WHERE id=?", Integer.class,
                award.getId())).isEqualTo(adminId);

        Award pending = (Award) service.submitAward(dto, ownerId, null).getData();
        assertThat(service.deleteAward(pending.getId(), ownerId).isSuccess()).isTrue();
        assertThat(awards.findById(pending.getId())).isNull();
        assertThat(jdbc.queryForObject("SELECT deleted FROM award WHERE id=?", Integer.class,
                pending.getId())).isEqualTo(1);
    }

    @Test
    void projectCreationRollsBackWhenLaterWriteFails() {
        ProjectDAO failing = new ProjectDAO(jdbc) {
            @Override
            public void addHistory(Integer projectId, String action, Integer operatorId, String detail,
                                   String reason, Integer targetId, Object extra) {
                throw new IllegalStateException("simulated history write failure");
            }
        };
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TransactionConfig.class);
            context.registerBean(javax.sql.DataSource.class, () -> dataSource);
            context.registerBean(PlatformTransactionManager.class,
                    () -> new DataSourceTransactionManager(dataSource));
            context.registerBean(ProjectDAO.class, () -> failing);
            context.registerBean(UserDAO.class, () -> users);
            context.registerBean(ProjectService.class, () -> new ProjectService(failing, users));
            context.refresh();

            ProjectDTO dto = new ProjectDTO();
            dto.setName("must roll back");
            dto.setCategory("software");
            dto.setYear(2026);
            assertThatThrownBy(() -> context.getBean(ProjectService.class).createProject(dto, ownerId))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM project", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM project_member", Integer.class)).isZero();
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionConfig { }
}
