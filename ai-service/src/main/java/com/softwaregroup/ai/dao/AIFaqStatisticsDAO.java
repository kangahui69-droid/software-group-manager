package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIFaqStatistics;
import java.util.List;

/**
 * AI FAQ统计DAO接口
 */
public interface AIFaqStatisticsDAO {
    AIFaqStatistics findByHash(String questionHash);
    List<AIFaqStatistics> findTopQuestions(int limit);
    List<AIFaqStatistics> findAllOrderByCount();
    Integer save(AIFaqStatistics statistics);
    void update(AIFaqStatistics statistics);
}
