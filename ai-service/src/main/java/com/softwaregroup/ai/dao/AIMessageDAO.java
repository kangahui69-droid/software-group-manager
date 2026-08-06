package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessage;
import java.util.List;

/**
 * AI消息DAO接口
 */
public interface AIMessageDAO {
    AIMessage findById(Integer id);
    List<AIMessage> findByConversationId(Integer conversationId);
    Integer save(AIMessage message);
    void deleteByConversationId(Integer conversationId);
}
