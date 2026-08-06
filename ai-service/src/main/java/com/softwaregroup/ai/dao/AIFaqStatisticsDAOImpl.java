package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIFaqStatistics;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AIFaqStatisticsDAO内存实现（用于测试）
 */
@Repository
public class AIFaqStatisticsDAOImpl implements AIFaqStatisticsDAO {
    private final Map<Integer, AIFaqStatistics> storage = new ConcurrentHashMap<>();
    private final Map<String, Integer> hashIndex = new ConcurrentHashMap<>();
    private int nextId = 1;

    @Override
    public AIFaqStatistics findByHash(String questionHash) {
        Integer id = hashIndex.get(questionHash);
        return id != null ? storage.get(id) : null;
    }

    @Override
    public List<AIFaqStatistics> findTopQuestions(int limit) {
        return storage.values().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getQueryCount() != null ? b.getQueryCount() : 0,
                        a.getQueryCount() != null ? a.getQueryCount() : 0))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<AIFaqStatistics> findAllOrderByCount() {
        return storage.values().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getQueryCount() != null ? b.getQueryCount() : 0,
                        a.getQueryCount() != null ? a.getQueryCount() : 0))
                .collect(Collectors.toList());
    }

    @Override
    public Integer save(AIFaqStatistics statistics) {
        synchronized (this) {
            statistics.setId(nextId++);
        }
        storage.put(statistics.getId(), statistics);
        if (statistics.getQuestionHash() != null) {
            hashIndex.put(statistics.getQuestionHash(), statistics.getId());
        }
        return statistics.getId();
    }

    @Override
    public void update(AIFaqStatistics statistics) {
        if (statistics.getId() != null) {
            storage.put(statistics.getId(), statistics);
        }
    }
}
