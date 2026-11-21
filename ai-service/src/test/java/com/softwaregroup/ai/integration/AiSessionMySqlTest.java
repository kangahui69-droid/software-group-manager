package com.softwaregroup.ai.integration;

import com.softwaregroup.ai.dao.*;
import com.softwaregroup.ai.feign.*;
import com.softwaregroup.ai.model.entity.AIConversation;
import com.softwaregroup.ai.model.entity.AIMessage;
import com.softwaregroup.ai.model.entity.AIKnowledgeBase;
import com.softwaregroup.ai.model.entity.AIFaqStatistics;
import com.softwaregroup.ai.model.entity.AIMessageStatus;
import com.softwaregroup.ai.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/** Runs only against a disposable database named mig03_test. */
@EnabledIfEnvironmentVariable(named = "MIG03_MYSQL_URL", matches = ".+")
class AiSessionMySqlTest {
    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(System.getenv("MIG03_MYSQL_URL"),
                System.getenv().getOrDefault("MIG03_MYSQL_USER", "root"),
                System.getenv().getOrDefault("MIG03_MYSQL_PASSWORD", ""));
        jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).isEqualTo("mig03_test");
        jdbc.update("DELETE FROM ai_message_status");
        jdbc.update("DELETE FROM ai_faq_statistics");
        jdbc.update("DELETE FROM ai_knowledge_base");
        jdbc.update("DELETE FROM ai_message");
        jdbc.update("DELETE FROM ai_conversation");
    }

    @Test
    void knowledgeFaqAndMessageStatusSurviveNewDaoInstances() {
        JdbcAIKnowledgeBaseDAO knowledge = new JdbcAIKnowledgeBaseDAO(jdbc);
        AIKnowledgeBase item = new AIKnowledgeBase();
        item.setCategory("test");
        item.setQuestion("How to join?");
        item.setAnswer("Apply online");
        knowledge.save(item);
        assertThat(new JdbcAIKnowledgeBaseDAO(new JdbcTemplate(dataSource)).findById(item.getId())
                .getAnswer()).isEqualTo("Apply online");
        item.setAnswer("Contact admin");
        knowledge.update(item);
        assertThat(knowledge.findById(item.getId()).getAnswer()).isEqualTo("Contact admin");

        JdbcAIFaqStatisticsDAO faq = new JdbcAIFaqStatisticsDAO(jdbc);
        AIFaqStatistics stats = new AIFaqStatistics();
        stats.setQuestionHash("a".repeat(64));
        stats.setNormalizedQuestion("How to join?");
        stats.setQueryCount(1);
        faq.save(stats);
        faq.update(stats);
        assertThat(new JdbcAIFaqStatisticsDAO(new JdbcTemplate(dataSource))
                .findByHash(stats.getQuestionHash()).getQueryCount()).isEqualTo(2);
        assertThat(faq.findTopQuestions(1)).hasSize(1);

        JdbcAIMessageStatusDAO statuses = new JdbcAIMessageStatusDAO(jdbc);
        AIMessageStatus status = new AIMessageStatus();
        status.setSessionId("status-session");
        status.setUserId(101);
        status.setUserMessage("Hello");
        statuses.save(status);
        assertThat(status.getMessageId()).isNotBlank();
        status.setStatus(AIMessageStatus.STATUS_COMPLETED);
        status.setAiResponse("Hi");
        statuses.update(status);
        AIMessageStatus restored = new JdbcAIMessageStatusDAO(new JdbcTemplate(dataSource)).findById(status.getId());
        assertThat(restored.getStatus()).isEqualTo(AIMessageStatus.STATUS_COMPLETED);
        assertThat(restored.getAiResponse()).isEqualTo("Hi");
        assertThat(restored.getUserId()).isEqualTo(101);

        knowledge.delete(item.getId());
        assertThat(knowledge.findById(item.getId())).isNull();
    }

    private AIService service(AIConversationDAO conversations, AIMessageDAO messages) {
        return new AIService(conversations, messages,
                mock(AIKnowledgeBaseDAO.class), mock(AIFaqStatisticsDAO.class), mock(AIMessageStatusDAO.class),
                mock(UserFeignClient.class), mock(ActivityFeignClient.class), mock(ProjectFeignClient.class),
                mock(AwardFeignClient.class), mock(GroupFeignClient.class), mock(FileFeignClient.class));
    }

    @Test
    void newServiceInstanceReadsPersistedSessionAndMessages() {
        AIService first = service(new JdbcAIConversationDAO(jdbc), new JdbcAIMessageDAO(jdbc));
        AIConversation created = first.createOrGetConversation("mig03-session", 101);
        assertThat(created.getId()).isPositive();
        AIMessage message = first.saveMessage(created.getId(), "user", "persist after restart");
        assertThat(message.getId()).isPositive();

        // New DAO and service objects simulate a container process restart with the same MySQL database.
        AIService restarted = service(new JdbcAIConversationDAO(new JdbcTemplate(dataSource)),
                new JdbcAIMessageDAO(new JdbcTemplate(dataSource)));
        assertThat(restarted.createOrGetConversation("mig03-session", 101).getId()).isEqualTo(created.getId());
        assertThat(restarted.getConversationHistory("mig03-session", 101))
                .extracting(AIMessage::getContent).containsExactly("persist after restart");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_conversation", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_message", Integer.class)).isEqualTo(1);
    }

    @Test
    void otherUserCannotReadClearOrClaimSession() {
        JdbcAIConversationDAO conversations = new JdbcAIConversationDAO(jdbc);
        JdbcAIMessageDAO messages = new JdbcAIMessageDAO(jdbc);
        AIService service = service(conversations, messages);
        AIConversation owned = service.createOrGetConversation("owner-session", 101);
        service.saveMessage(owned.getId(), "user", "private");

        assertForbidden(() -> service.getConversationHistory("owner-session", 202));
        assertForbidden(() -> service.clearConversation("owner-session", 202));
        assertForbidden(() -> service.createOrGetConversation("owner-session", 202));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_message", Integer.class)).isEqualTo(1);
        assertThatThrownBy(() -> conversations.save(new AIConversation(202, "owner-session")))
                .isInstanceOf(DuplicateKeyException.class);
        service.clearConversation("owner-session", 101);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_conversation", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_message", Integer.class)).isZero();
    }

    private void assertForbidden(ThrowingAction action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode().value()).isEqualTo(403));
    }

    @FunctionalInterface
    interface ThrowingAction { void run(); }

    @Test
    void clearRollsBackWhenConversationDeleteFails() {
        JdbcAIConversationDAO failing = new JdbcAIConversationDAO(jdbc) {
            @Override public void delete(Integer id) {
                throw new IllegalStateException("simulated conversation delete failure");
            }
        };
        JdbcAIMessageDAO messages = new JdbcAIMessageDAO(jdbc);
        AIConversation created = new AIConversation(101, "rollback-session");
        failing.save(created);
        messages.save(new AIMessage(created.getId(), "user", "must remain"));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TransactionConfig.class);
            context.registerBean(javax.sql.DataSource.class, () -> dataSource);
            context.registerBean(PlatformTransactionManager.class,
                    () -> new DataSourceTransactionManager(dataSource));
            context.registerBean(AIService.class, () -> service(failing, messages));
            context.refresh();
            assertThatThrownBy(() -> context.getBean(AIService.class).clearConversation("rollback-session", 101))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_conversation", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_message", Integer.class)).isEqualTo(1);
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionConfig { }
}
