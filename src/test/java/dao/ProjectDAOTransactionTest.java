package dao;

import model.*;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - ProjectDAO
 *
 * 验证ProjectDAO写操作（insert/update/delete）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("ProjectDAO 事务重载测试")
class ProjectDAOTransactionTest {

    private ProjectDAO projectDAO = new ProjectDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setName("测试项目");
        project.setDescription("项目描述");
        project.setCategory("software");
        project.setLeaderId(2);
        project.setStatus("PENDING");
        project.setYear(2026);
        return project;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(Project, Connection) 有参版本应能插入项目并设置生成的主键ID")
    void should_insert_project_with_connection_and_set_generated_id() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();

            boolean result = projectDAO.insert(project, conn);

            assertThat(result).isTrue();
            assertThat(project.getId()).isGreaterThan(0);
            Project found = projectDAO.findById(project.getId());
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("测试项目");
        }
    }

    @FastTest
    @DisplayName("insert(Project, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();

        Project project = createTestProject();
        project.setName("不关闭测试");

        projectDAO.insert(project, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(Project, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setName(null); // 导致SQL错误

            assertThatThrownBy(() -> projectDAO.insert(project, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("插入项目");
        }
    }

    @FastTest
    @DisplayName("insert(Project) 无参版本应能正常插入项目")
    void should_insert_project_without_connection() {
        Project project = createTestProject();
        project.setName("无连接项目");

        boolean result = projectDAO.insert(project);

        assertThat(result).isTrue();
        assertThat(project.getId()).isGreaterThan(0);
    }

    // ==================== update 事务重载 ====================

    @FastTest
    @DisplayName("update(Project, Connection) 有参版本应能更新项目信息")
    void should_update_project_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            projectDAO.insert(project);

            project.setName("更新后的名称");
            project.setDescription("更新后的描述");

            boolean result = projectDAO.update(project, conn);

            assertThat(result).isTrue();
            Project updated = projectDAO.findById(project.getId());
            assertThat(updated.getName()).isEqualTo("更新后的名称");
        }
    }

    @FastTest
    @DisplayName("update(Project, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_update_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setId(99999); // 不存在的项目

            assertThatThrownBy(() -> projectDAO.update(project, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("更新项目");
        }
    }

    @FastTest
    @DisplayName("update(Project) 无参版本应能正常更新项目")
    void should_update_project_without_connection() {
        Project project = createTestProject();
        projectDAO.insert(project);

        project.setName("无连接更新");
        boolean result = projectDAO.update(project);

        assertThat(result).isTrue();
        Project updated = projectDAO.findById(project.getId());
        assertThat(updated.getName()).isEqualTo("无连接更新");
    }

    // ==================== approve 事务重载 ====================

    @FastTest
    @DisplayName("approve(Integer, Integer, Connection) 有参版本应能审批通过项目")
    void should_approve_project_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            projectDAO.insert(project);

            boolean result = projectDAO.approve(project.getId(), 1, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("approve(Integer, Integer) 无参版本应能正常审批项目")
    void should_approve_project_without_connection() {
        Project project = createTestProject();
        projectDAO.insert(project);

        boolean result = projectDAO.approve(project.getId(), 1);

        assertThat(result).isTrue();
    }

    // ==================== reject 事务重载 ====================

    @FastTest
    @DisplayName("reject(Integer, Integer, Connection) 有参版本应能拒绝项目")
    void should_reject_project_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            projectDAO.insert(project);

            boolean result = projectDAO.reject(project.getId(), 1, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("reject(Integer, Integer) 无参版本应能正常拒绝项目")
    void should_reject_project_without_connection() {
        Project project = createTestProject();
        projectDAO.insert(project);

        boolean result = projectDAO.reject(project.getId(), 1);

        assertThat(result).isTrue();
    }

    // ==================== delete 事务重载 ====================

    @FastTest
    @DisplayName("delete(Integer, Connection) 有参版本应能软删除项目")
    void should_delete_project_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            projectDAO.insert(project);

            boolean result = projectDAO.delete(project.getId(), conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("delete(Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_delete_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThatThrownBy(() -> projectDAO.delete(99999, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("删除项目");
        }
    }

    @FastTest
    @DisplayName("delete(Integer) 无参版本应能正常软删除项目")
    void should_delete_project_without_connection() {
        Project project = createTestProject();
        projectDAO.insert(project);

        boolean result = projectDAO.delete(project.getId());

        assertThat(result).isTrue();
    }

    // ==================== addMember 事务重载 ====================

    @FastTest
    @DisplayName("addMember(Integer, Integer, String, Connection) 有参版本应能添加项目成员")
    void should_add_member_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            boolean result = projectDAO.addMember(project.getId(), 2, "LEADER", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("addMember(Integer, Integer, String) 无参版本应能正常添加成员")
    void should_add_member_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        boolean result = projectDAO.addMember(project.getId(), 2, "MEMBER");

        assertThat(result).isTrue();
    }

    // ==================== removeMember 事务重载 ====================

    @FastTest
    @DisplayName("removeMember(Integer, Integer, Connection) 有参版本应能移除项目成员")
    void should_remove_member_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);
            projectDAO.addMember(project.getId(), 2, "MEMBER");

            boolean result = projectDAO.removeMember(project.getId(), 2, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("removeMember(Integer, Integer) 无参版本应能正常移除成员")
    void should_remove_member_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);
        projectDAO.addMember(project.getId(), 2, "MEMBER");

        boolean result = projectDAO.removeMember(project.getId(), 2);

        assertThat(result).isTrue();
    }

    // ==================== addLabel/removeLabel 事务重载 ====================

    @FastTest
    @DisplayName("addLabel(Integer, String, Connection) 有参版本应能添加项目标签")
    void should_add_label_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            boolean result = projectDAO.addLabel(project.getId(), "INNOVATION", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("addLabel(Integer, String) 无参版本应能正常添加标签")
    void should_add_label_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        boolean result = projectDAO.addLabel(project.getId(), "INNOVATION");

        assertThat(result).isTrue();
    }

    @FastTest
    @DisplayName("removeLabel(Integer, String, Connection) 有参版本应能移除项目标签")
    void should_remove_label_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);
            projectDAO.addLabel(project.getId(), "TEST_LABEL");

            boolean result = projectDAO.removeLabel(project.getId(), "TEST_LABEL", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("removeLabel(Integer, String) 无参版本应能正常移除标签")
    void should_remove_label_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);
        projectDAO.addLabel(project.getId(), "TEST_LABEL");

        boolean result = projectDAO.removeLabel(project.getId(), "TEST_LABEL");

        assertThat(result).isTrue();
    }

    // ==================== addHistory 事务重载 ====================

    @FastTest
    @DisplayName("addHistory(..., Connection) 有参版本应能添加项目历史记录")
    void should_add_history_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            boolean result = projectDAO.addHistory(
                    project.getId(), "CREATE", 2, "测试成员",
                    "创建了项目", null, "PENDING", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("addHistory(...) 无参版本应能正常添加历史记录")
    void should_add_history_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        boolean result = projectDAO.addHistory(
                project.getId(), "CREATE", 2, "测试成员",
                "创建了项目", null, "PENDING");

        assertThat(result).isTrue();
    }

    // ==================== addPlan/updatePlan/deletePlan 事务重载 ====================

    @FastTest
    @DisplayName("addPlan(ProjectPlan, Connection) 有参版本应能添加项目计划并设置生成ID")
    void should_add_plan_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            ProjectPlan plan = new ProjectPlan();
            plan.setProjectId(project.getId());
            plan.setTitle("第一阶段计划");
            plan.setDescription("计划描述");
            plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
            plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));

            boolean result = projectDAO.addPlan(plan, conn);

            assertThat(result).isTrue();
            assertThat(plan.getId()).isGreaterThan(0);
        }
    }

    @FastTest
    @DisplayName("addPlan(ProjectPlan) 无参版本应能正常添加计划")
    void should_add_plan_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        ProjectPlan plan = new ProjectPlan();
        plan.setProjectId(project.getId());
        plan.setTitle("第一阶段计划");
        plan.setDescription("计划描述");
        plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));

        boolean result = projectDAO.addPlan(plan);

        assertThat(result).isTrue();
    }

    @FastTest
    @DisplayName("updatePlan(ProjectPlan, Connection) 有参版本应能更新项目计划")
    void should_update_plan_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            ProjectPlan plan = new ProjectPlan();
            plan.setProjectId(project.getId());
            plan.setTitle("原始计划");
            plan.setDescription("原始描述");
            plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
            plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));
            projectDAO.addPlan(plan);

            plan.setTitle("更新后的计划");
            boolean result = projectDAO.updatePlan(plan, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("updatePlan(ProjectPlan) 无参版本应能正常更新计划")
    void should_update_plan_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        ProjectPlan plan = new ProjectPlan();
        plan.setProjectId(project.getId());
        plan.setTitle("原始计划");
        plan.setDescription("原始描述");
        plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));
        projectDAO.addPlan(plan);

        plan.setTitle("更新后的计划");
        boolean result = projectDAO.updatePlan(plan);

        assertThat(result).isTrue();
    }

    @FastTest
    @DisplayName("deletePlan(Integer, Connection) 有参版本应能删除项目计划")
    void should_delete_plan_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            ProjectPlan plan = new ProjectPlan();
            plan.setProjectId(project.getId());
            plan.setTitle("待删除计划");
            plan.setDescription("描述");
            plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
            plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));
            projectDAO.addPlan(plan);

            boolean result = projectDAO.deletePlan(plan.getId(), conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("deletePlan(Integer) 无参版本应能正常删除计划")
    void should_delete_plan_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        ProjectPlan plan = new ProjectPlan();
        plan.setProjectId(project.getId());
        plan.setTitle("待删除计划");
        plan.setDescription("描述");
        plan.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        plan.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000 * 30));
        projectDAO.addPlan(plan);

        boolean result = projectDAO.deletePlan(plan.getId());

        assertThat(result).isTrue();
    }

    // ==================== addProgress 事务重载 ====================

    @FastTest
    @DisplayName("addProgress(ProjectProgress, Connection) 有参版本应能添加项目进度")
    void should_add_progress_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            ProjectProgress progress = new ProjectProgress();
            progress.setProjectId(project.getId());
            progress.setTitle("第一周进度");
            progress.setDescription("进度描述");
            progress.setCompletionRate(25);
            progress.setCreatedBy(2);

            boolean result = projectDAO.addProgress(progress, conn);

            assertThat(result).isTrue();
            assertThat(progress.getId()).isGreaterThan(0);
        }
    }

    @FastTest
    @DisplayName("addProgress(ProjectProgress) 无参版本应能正常添加进度")
    void should_add_progress_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        ProjectProgress progress = new ProjectProgress();
        progress.setProjectId(project.getId());
        progress.setTitle("第一周进度");
        progress.setDescription("进度描述");
        progress.setCompletionRate(25);
        progress.setCreatedBy(2);

        boolean result = projectDAO.addProgress(progress);

        assertThat(result).isTrue();
    }

    // ==================== applyMember 事务重载 ====================

    @FastTest
    @DisplayName("applyMember(Integer, Integer, String, Connection) 有参版本应能申请加入项目")
    void should_apply_member_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            boolean result = projectDAO.applyMember(project.getId(), 2, "想要加入", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("applyMember(Integer, Integer, String) 无参版本应能正常申请加入")
    void should_apply_member_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        boolean result = projectDAO.applyMember(project.getId(), 2, "想要加入");

        assertThat(result).isTrue();
    }

    // ==================== approveMemberApplication 事务重载 ====================

    @FastTest
    @DisplayName("approveMemberApplication(Integer, Integer, Connection) 有参版本应能审批通过申请")
    void should_approve_member_application_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);
            projectDAO.applyMember(project.getId(), 2, "申请理由");

            ProjectMemberApplication app = projectDAO.getMemberApplications(project.getId(), "PENDING").get(0);

            boolean result = projectDAO.approveMemberApplication(app.getId(), 1, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("approveMemberApplication(Integer, Integer) 无参版本应能正常审批通过")
    void should_approve_member_application_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);
        projectDAO.applyMember(project.getId(), 2, "申请理由");

        ProjectMemberApplication app = projectDAO.getMemberApplications(project.getId(), "PENDING").get(0);

        boolean result = projectDAO.approveMemberApplication(app.getId(), 1);

        assertThat(result).isTrue();
    }

    // ==================== rejectMemberApplication 事务重载 ====================

    @FastTest
    @DisplayName("rejectMemberApplication(Integer, Integer, String, Connection) 有参版本应能拒绝申请")
    void should_reject_member_application_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);
            projectDAO.applyMember(project.getId(), 2, "申请理由");

            ProjectMemberApplication app = projectDAO.getMemberApplications(project.getId(), "PENDING").get(0);

            boolean result = projectDAO.rejectMemberApplication(app.getId(), 1, "不符合要求", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("rejectMemberApplication(Integer, Integer, String) 无参版本应能正常拒绝申请")
    void should_reject_member_application_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);
        projectDAO.applyMember(project.getId(), 2, "申请理由");

        ProjectMemberApplication app = projectDAO.getMemberApplications(project.getId(), "PENDING").get(0);

        boolean result = projectDAO.rejectMemberApplication(app.getId(), 1, "不符合要求");

        assertThat(result).isTrue();
    }

    // ==================== transferAdmin 事务重载 ====================

    @FastTest
    @DisplayName("transferAdmin(Integer, Integer, Connection) 有参版本应能转让项目管理员")
    void should_transfer_admin_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Project project = createTestProject();
            project.setStatus("APPROVED");
            projectDAO.insert(project);

            boolean result = projectDAO.transferAdmin(project.getId(), 2, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("transferAdmin(Integer, Integer) 无参版本应能正常转让管理员")
    void should_transfer_admin_without_connection() {
        Project project = createTestProject();
        project.setStatus("APPROVED");
        projectDAO.insert(project);

        boolean result = projectDAO.transferAdmin(project.getId(), 2);

        assertThat(result).isTrue();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("多个DAO在同一连接上连续写操作应能保持事务一致")
    void should_maintain_transaction_consistency_across_multiple_dao_operations() throws SQLException {
        UserDAO userDAO = new UserDAO();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 第一个DAO写操作
                User user = new User();
                user.setUsername("trans_multi");
                user.setPassword("password");
                user.setName("多DAO事务");
                user.setEmail("multi@example.com");
                user.setRole("MEMBER");
                user.setStatus(1);
                userDAO.insert(user, conn);

                // 第二个DAO写操作
                Project project = createTestProject();
                project.setName("跨DAO项目");
                project.setLeaderId(user.getId());
                projectDAO.insert(project, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            User foundUser = userDAO.findByUsername("trans_multi");
            assertThat(foundUser).isNotNull();
            List<Project> projects = projectDAO.findByLeaderId(foundUser.getId());
            assertThat(projects).hasSize(1);
            assertThat(projects.get(0).getName()).isEqualTo("跨DAO项目");
        }
    }

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        Project project = createTestProject();

        assertThatThrownBy(() -> projectDAO.insert(project, null))
                .isInstanceOf(NullPointerException.class);
    }
}