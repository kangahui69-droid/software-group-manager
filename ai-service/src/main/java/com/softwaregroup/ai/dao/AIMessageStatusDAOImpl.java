package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessageStatus;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AIMessageStatusDAO内存实现（用于测试）
 */
@Repository
public class AIMessageStatusDAOImpl implements AIMessageStatusDAO {
    private final Map<Integer, AIMessageStatus> storage = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public AIMessageStatus findById(Integer id) {
        return storage.get(id);
    }

    @Override
    public Integer save(AIMessageStatus status) {
        int id = nextId.getAndIncrement();
        status.setId(id);
        storage.put(id, status);
        return id;
    }

    @Override
    public void update(AIMessageStatus status) {
        if (status.getId() != null) {
            storage.put(status.getId(), status);
        }
    }
}
