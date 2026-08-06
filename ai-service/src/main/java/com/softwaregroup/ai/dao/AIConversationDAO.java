package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIConversation;

/**
 * AI对话DAO接口
 */
public interface AIConversationDAO {
    AIConversation findBySessionId(String sessionId);
    Integer save(AIConversation conversation);
    void update(AIConversation conversation);
    void delete(Integer id);
}
