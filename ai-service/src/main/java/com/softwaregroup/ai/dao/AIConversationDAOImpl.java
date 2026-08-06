package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIConversation;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AIConversationDAO内存实现（用于测试）
 */
@Repository
public class AIConversationDAOImpl implements AIConversationDAO {
    private final Map<Integer, AIConversation> storage = new ConcurrentHashMap<>();
    private int nextId = 1;

    @Override
    public AIConversation findBySessionId(String sessionId) {
        return storage.values().stream()
                .filter(c -> sessionId.equals(c.getSessionId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer save(AIConversation conversation) {
        synchronized (this) {
            conversation.setId(nextId++);
        }
        storage.put(conversation.getId(), conversation);
        return conversation.getId();
    }

    @Override
    public void update(AIConversation conversation) {
        if (conversation.getId() != null) {
            storage.put(conversation.getId(), conversation);
        }
    }

    @Override
    public void delete(Integer id) {
        storage.remove(id);
    }
}
