package com.softwaregroup.content.service;

import com.softwaregroup.content.dao.*;
import com.softwaregroup.content.model.ActivityGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock ActivityGroupDAO groups;
    @Mock GroupMemberDAO members;
    @Mock GroupMessageDAO messages;
    @Mock UserDAO users;
    @Mock NewsDAO news;
    @InjectMocks GroupService service;
    @InjectMocks NewsService newsService;

    @Test
    void nonMemberCannotReadMessages() {
        assertThat(service.getMessages(7, 1, 2).getCode()).isEqualTo(403);
        verifyNoInteractions(messages);
    }

    @Test
    void nonOwnerCannotMuteMember() {
        when(groups.findById(7)).thenReturn(new ActivityGroup());
        assertThat(service.muteMember(7, 3, new Date(), "reason", 2).getCode()).isEqualTo(403);
        verify(groups, never()).muteGroup(anyInt(), any(), any());
    }

    @Test
    void newsTypeListOnlyQueriesPublishedItems() {
        newsService.getNewsByType("NOTICE", 1, 20);
        verify(news).findByConditions(null, "NOTICE", 1);
        verify(news, never()).findByType(anyString());
    }
}
