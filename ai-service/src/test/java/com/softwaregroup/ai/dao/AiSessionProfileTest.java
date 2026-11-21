package com.softwaregroup.ai.dao;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AiSessionProfileTest {
    @Test
    void testProfileUsesOnlyMemoryRepositories() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("test");
            context.register(AIConversationDAOImpl.class, AIMessageDAOImpl.class,
                    AIKnowledgeBaseDAOImpl.class, AIFaqStatisticsDAOImpl.class, AIMessageStatusDAOImpl.class,
                    JdbcAIConversationDAO.class, JdbcAIMessageDAO.class,
                    JdbcAIKnowledgeBaseDAO.class, JdbcAIFaqStatisticsDAO.class, JdbcAIMessageStatusDAO.class);
            context.refresh();
            assertThat(context.getBean(AIConversationDAO.class)).isInstanceOf(AIConversationDAOImpl.class);
            assertThat(context.getBean(AIMessageDAO.class)).isInstanceOf(AIMessageDAOImpl.class);
            assertThat(context.getBean(AIKnowledgeBaseDAO.class)).isInstanceOf(AIKnowledgeBaseDAOImpl.class);
            assertThat(context.getBean(AIFaqStatisticsDAO.class)).isInstanceOf(AIFaqStatisticsDAOImpl.class);
            assertThat(context.getBean(AIMessageStatusDAO.class)).isInstanceOf(AIMessageStatusDAOImpl.class);
        }
    }

    @Test
    void defaultProfileUsesOnlyJdbcRepositories() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class));
            context.register(AIConversationDAOImpl.class, AIMessageDAOImpl.class,
                    AIKnowledgeBaseDAOImpl.class, AIFaqStatisticsDAOImpl.class, AIMessageStatusDAOImpl.class,
                    JdbcAIConversationDAO.class, JdbcAIMessageDAO.class,
                    JdbcAIKnowledgeBaseDAO.class, JdbcAIFaqStatisticsDAO.class, JdbcAIMessageStatusDAO.class);
            context.refresh();
            assertThat(context.getBean(AIConversationDAO.class)).isInstanceOf(JdbcAIConversationDAO.class);
            assertThat(context.getBean(AIMessageDAO.class)).isInstanceOf(JdbcAIMessageDAO.class);
            assertThat(context.getBean(AIKnowledgeBaseDAO.class)).isInstanceOf(JdbcAIKnowledgeBaseDAO.class);
            assertThat(context.getBean(AIFaqStatisticsDAO.class)).isInstanceOf(JdbcAIFaqStatisticsDAO.class);
            assertThat(context.getBean(AIMessageStatusDAO.class)).isInstanceOf(JdbcAIMessageStatusDAO.class);
        }
    }
}
