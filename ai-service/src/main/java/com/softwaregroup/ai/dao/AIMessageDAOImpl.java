package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessage;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AIMessageDAO内存实现（用于测试）
 */
@Repository
public class AIMessageDAOImpl implements AIMessageDAO {
    private final Map<Integer, AIMessage> storage = new ConcurrentHashMap<>();
    private int nextId = 1;

    @Override
    public AIMessage findById(Integer id) {
        return storage.get(id);
    }

    @Override
    public List<AIMessage> findByConversationId(Integer conversationId) {
        return storage.values().stream()
                .filter(m -> conversationId.equals(m.getConversationId()))
                .sorted(Comparator.comparing(m -> m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public Integer save(AIMessage message) {
        synchronized (this) {
            message.setId(nextId++);
        }
        storage.put(message.getId(), message);
        return message.getId();
    }

    @Override
    public void deleteByConversationId(Integer conversationId) {
        storage.entrySet().removeIf(e -> conversationId.equals(e.getValue().getConversationId()));
    }
}
