package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIKnowledgeBase;
import java.util.List;

/**
 * AI知识库DAO接口
 */
public interface AIKnowledgeBaseDAO {
    List<AIKnowledgeBase> findAll();
    AIKnowledgeBase findById(Integer id);
    Integer save(AIKnowledgeBase knowledgeBase);
    void update(AIKnowledgeBase knowledgeBase);
    void delete(Integer id);
}
