package com.softwaregroup.content.integration;

import com.softwaregroup.content.dao.*;
import com.softwaregroup.content.model.ActivityGroup;
import com.softwaregroup.content.model.GroupMessage;
import com.softwaregroup.content.model.News;
import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.service.GroupService;
import com.softwaregroup.content.service.NewsService;
import com.softwaregroup.content.service.NewsContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Uses only a disposable local database loaded with the schema-only SQL baseline. */
@EnabledIfEnvironmentVariable(named = "MIG02_MYSQL_URL", matches = ".+")
class ContentMySqlTest {
    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;
    private Integer ownerId;
    private Integer memberId;
    private ActivityGroupDAO groups;
    private GroupMemberDAO members;
    private GroupMessageDAO messages;
    private UserGroupDAO userGroups;
    private FileStorageDAO files;
    private UserDAO users;
    private MemberProfileDAO profiles;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(System.getenv("MIG02_MYSQL_URL"),
                System.getenv().getOrDefault("MIG02_MYSQL_USER", "root"),
                System.getenv().getOrDefault("MIG02_MYSQL_PASSWORD", ""));
        jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).isEqualTo("mig02_test");
        for (String table : List.of("group_message", "group_member", "user_group", "activity_group",
                "news", "member_profile", "file_storage", "user")) {
            jdbc.update("DELETE FROM `" + table + "`");
        }
        jdbc.update("INSERT INTO user (username,password,name,email,role,status) VALUES " +
                "('mig02_owner','test-hash','Owner','owner@mig02.invalid','ADMIN',1)," +
                "('mig02_member','test-hash','Member','member@mig02.invalid','MEMBER',1)");
        ownerId = jdbc.queryForObject("SELECT id FROM user WHERE username='mig02_owner'", Integer.class);
        memberId = jdbc.queryForObject("SELECT id FROM user WHERE username='mig02_member'", Integer.class);
        groups = new ActivityGroupDAO(jdbc);
        members = new GroupMemberDAO(jdbc);
        messages = new GroupMessageDAO(jdbc);
        userGroups = new UserGroupDAO(jdbc);
        files = new FileStorageDAO(jdbc);
        users = new UserDAO(jdbc);
        profiles = new MemberProfileDAO(jdbc);
    }

    private GroupService groupService() {
        return new GroupService(groups, members, messages, userGroups, files, users, profiles);
    }

    @Test
    void newsIsPersistedFilteredUpdatedAndPublished() throws Exception {
        NewsDAO dao = new NewsDAO(jdbc);
        NewsContentStorage contentStorage = new NewsContentStorage(
                java.nio.file.Path.of("target", "mig02-news-files").toAbsolutePath().toString());
        NewsService service = new NewsService(dao, users, contentStorage);
        NewsDTO dto = new NewsDTO();
        dto.setTitle("MIG-02 news");
        dto.setType("NOTICE");
        dto.setSummary("unique summary");
        dto.setContentPath(null);
        assertThat(service.createNews(dto, ownerId).getCode()).isEqualTo(400);
        dto.setContent("<p>MIG-02 body</p><script>alert(1)</script>");

        assertThat(service.createNews(dto, ownerId).isSuccess()).isTrue();
        News news = dao.findByConditions("MIG-02", "notice", 1).get(0);
        assertThat(news.getId()).isPositive();
        assertThat(contentStorage.read(news.getContentPath())).contains("MIG-02 body").doesNotContain("<script>");
        assertThat(((News) service.getNewsDetail(news.getId()).getData()).getContent()).contains("MIG-02 body");
        assertThat(dao.findByType("NOTICE")).extracting(News::getId).contains(news.getId());

        assertThat(service.unpublishNews(news.getId(), ownerId).isSuccess()).isTrue();
        assertThat(service.getNewsDetail(news.getId()).getCode()).isEqualTo(404);
        assertThat(service.publishNews(news.getId(), ownerId).isSuccess()).isTrue();

        dto.setTitle("MIG-02 updated");
        assertThat(service.updateNews(news.getId(), dto, ownerId).isSuccess()).isTrue();
        assertThat(dao.findById(news.getId()).getTitle()).isEqualTo("MIG-02 updated");
        assertThat(service.deleteNews(news.getId(), ownerId).isSuccess()).isTrue();
        assertThat(dao.findByConditions(null, null, 1)).isEmpty();
        assertThat(jdbc.queryForObject("SELECT status FROM news WHERE id=?", Integer.class, news.getId())).isZero();
    }

    @Test
    void groupMembershipMessagesAndMuteUseRealRows() {
        GroupService service = groupService();
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("MIG-02 group");
        assertThat(service.createGroup(dto, ownerId).isSuccess()).isTrue();
        ActivityGroup group = groups.findByOwnerId(ownerId).get(0);
        assertThat(members.isOwner(group.getId(), ownerId)).isTrue();
        assertThat(userGroups.findByUserId(ownerId)).hasSize(1);

        dto.setGroupName("MIG-02 renamed");
        assertThat(service.updateGroup(group.getId(), dto, ownerId).isSuccess()).isTrue();
        assertThat(groups.findById(group.getId()).getGroupName()).isEqualTo(dto.getGroupName());
        assertThat(service.addMember(group.getId(), memberId, ownerId).isSuccess()).isTrue();
        assertThat(members.isMember(group.getId(), memberId)).isTrue();
        assertThat(userGroups.findByUserId(memberId)).hasSize(1);
        assertThat(service.sendMessage(group.getId(), memberId, "hello").isSuccess()).isTrue();
        GroupMessage message = messages.findByGroupId(group.getId(), 10, 0).get(0);
        assertThat(messages.findById(message.getId()).getSenderId()).isEqualTo(memberId);
        assertThat(service.deleteMessage(message.getId(), ownerId).getCode()).isEqualTo(403);
        assertThat(service.deleteMessage(message.getId(), memberId).isSuccess()).isTrue();
        assertThat(messages.findById(message.getId())).isNull();

        java.util.Date until = java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.now().plusDays(1).withMinute(45).withSecond(30).withNano(0));
        assertThat(service.muteMember(group.getId(), memberId, until, "moderation", ownerId).isSuccess()).isTrue();
        assertThat(groups.findById(group.getId()).isMuted()).isFalse();
        assertThat(jdbc.queryForObject("SELECT muted FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, group.getId(), memberId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT mute_reason FROM group_member WHERE group_id=? AND user_id=?",
                String.class, group.getId(), memberId)).isEqualTo("moderation");
        assertThat(jdbc.queryForObject("SELECT MINUTE(muted_until) FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, group.getId(), memberId)).isEqualTo(45);
        assertThat(jdbc.queryForObject("SELECT SECOND(muted_until) FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, group.getId(), memberId)).isEqualTo(30);
        assertThat(jdbc.queryForObject("SELECT muted FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, group.getId(), ownerId)).isZero();
        assertThat(service.sendMessage(group.getId(), memberId, "blocked").getCode()).isEqualTo(403);
        assertThat(service.sendMessage(group.getId(), ownerId, "owner can speak").isSuccess()).isTrue();
        assertThat(service.unmuteMember(group.getId(), memberId, ownerId).isSuccess()).isTrue();
        assertThat(jdbc.queryForObject("SELECT muted FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, group.getId(), memberId)).isZero();
        assertThat(service.removeMember(group.getId(), memberId, ownerId).isSuccess()).isTrue();
        assertThat(members.isMember(group.getId(), memberId)).isFalse();
        assertThat(userGroups.findByUserId(memberId)).isEmpty();
        assertThat(service.deleteGroup(group.getId(), ownerId).isSuccess()).isTrue();
        assertThat(groups.findById(group.getId())).isNull();
        assertThat(userGroups.findByGroupId(group.getId())).isEmpty();
    }

    @Test
    void auxiliaryReadsAndCreateRollbackUseRealDatabase() {
        jdbc.update("INSERT INTO file_storage (create_by,original_name,stored_name,file_path) " +
                "VALUES (?,'avatar.png','stored-avatar.png','/test/avatar.png')", memberId);
        Integer fileId = jdbc.queryForObject("SELECT id FROM file_storage", Integer.class);
        jdbc.update("INSERT INTO member_profile (user_id,student_id,major,grade,avatar_file_id) " +
                "VALUES (?,'S02','CS','2026',?)", memberId, fileId);
        assertThat(files.findByIds(List.of(fileId))).hasSize(1);
        assertThat(files.findById(fileId).getFileName()).isEqualTo("avatar.png");
        assertThat(profiles.findByUserId(memberId).getName()).isEqualTo("Member");
        assertThat(profiles.findByUserId(memberId).getAvatarFileId()).isEqualTo(fileId);
        assertThat(users.findById(memberId).getPassword()).isNull();

        GroupMemberDAO failing = new GroupMemberDAO(jdbc) {
            @Override public boolean insertOwner(Integer groupId, Integer userId) {
                throw new IllegalStateException("simulated owner relation failure");
            }
        };
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TransactionConfig.class);
            context.registerBean(javax.sql.DataSource.class, () -> dataSource);
            context.registerBean(PlatformTransactionManager.class,
                    () -> new DataSourceTransactionManager(dataSource));
            context.registerBean(ActivityGroupDAO.class, () -> groups);
            context.registerBean(GroupMemberDAO.class, () -> failing);
            context.registerBean(GroupMessageDAO.class, () -> messages);
            context.registerBean(UserGroupDAO.class, () -> userGroups);
            context.registerBean(FileStorageDAO.class, () -> files);
            context.registerBean(UserDAO.class, () -> users);
            context.registerBean(MemberProfileDAO.class, () -> profiles);
            context.registerBean(GroupService.class, () ->
                    new GroupService(groups, failing, messages, userGroups, files, users, profiles));
            context.refresh();
            GroupDTO dto = new GroupDTO();
            dto.setGroupName("must roll back");
            assertThatThrownBy(() -> context.getBean(GroupService.class).createGroup(dto, ownerId))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM activity_group", Integer.class)).isZero();
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionConfig { }
}
