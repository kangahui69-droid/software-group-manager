package com.softwaregroup.ai.service;

import com.softwaregroup.ai.dao.AIConversationDAO;
import com.softwaregroup.ai.dao.AIMessageDAO;
import com.softwaregroup.ai.dao.AIMessageStatusDAO;
import com.softwaregroup.ai.model.entity.AIConversation;
import com.softwaregroup.ai.model.entity.AIMessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock AIConversationDAO conversations;
    @Mock AIMessageDAO messages;
    @Mock AIMessageStatusDAO statuses;
    @InjectMocks AIService service;

    @Test
    void sessionIdCannotReadOrClearAnotherUsersConversation() {
        AIConversation conversation = new AIConversation(2, "secret");
        conversation.setId(7);
        when(conversations.findBySessionId("secret")).thenReturn(conversation);
        assertThatThrownBy(() -> service.getConversationHistory("secret", 1))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.clearConversation("secret", 1))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.createOrGetConversation("secret", 1))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(messages);
        verify(conversations, never()).delete(anyInt());
    }

    @Test
    void anotherUserCannotChangeMessageStatus() {
        AIMessageStatus status = new AIMessageStatus();
        status.setUserId(2);
        when(statuses.findById(7)).thenReturn(status);
        assertThatThrownBy(() -> service.updateMessageStatus(7, "completed", 1))
                .isInstanceOf(ResponseStatusException.class);
        verify(statuses, never()).update(any());
    }
}
