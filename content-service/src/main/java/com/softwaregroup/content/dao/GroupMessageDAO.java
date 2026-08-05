package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.GroupMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组消息数据访问层
 */
@Repository
public class GroupMessageDAO {

    public List<GroupMessage> findByGroupId(Integer groupId, int limit, int offset) {
        return new ArrayList<>();
    }

    public GroupMessage findById(Integer messageId) {
        return null;
    }

    public int insert(GroupMessage message) {
        return 1;
    }

    public boolean delete(Integer messageId) {
        return true;
    }

    public void deleteByGroupId(Integer groupId) {
    }
}
