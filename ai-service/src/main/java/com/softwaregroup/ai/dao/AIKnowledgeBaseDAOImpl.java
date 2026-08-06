package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIKnowledgeBase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AIKnowledgeBaseDAO内存实现（用于测试）
 */
@Repository
public class AIKnowledgeBaseDAOImpl implements AIKnowledgeBaseDAO {
    private final Map<Integer, AIKnowledgeBase> storage = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public List<AIKnowledgeBase> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public AIKnowledgeBase findById(Integer id) {
        return storage.get(id);
    }

    @Override
    public Integer save(AIKnowledgeBase knowledgeBase) {
        int id = nextId.getAndIncrement();
        knowledgeBase.setId(id);
        storage.put(id, knowledgeBase);
        return id;
    }

    @Override
    public void update(AIKnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() != null) {
            storage.put(knowledgeBase.getId(), knowledgeBase);
        }
    }

    @Override
    public void delete(Integer id) {
        storage.remove(id);
    }
}
