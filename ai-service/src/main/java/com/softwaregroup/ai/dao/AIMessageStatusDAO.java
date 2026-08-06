package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessageStatus;

/**
 * AI消息状态DAO接口
 */
public interface AIMessageStatusDAO {
    AIMessageStatus findById(Integer id);
    Integer save(AIMessageStatus status);
    void update(AIMessageStatus status);
}
