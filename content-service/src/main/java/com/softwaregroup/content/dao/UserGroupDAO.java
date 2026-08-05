package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.UserGroup;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户群组关联数据访问层
 */
@Repository
public class UserGroupDAO {

    public List<UserGroup> findByUserId(Integer userId) {
        return new ArrayList<>();
    }

    public List<UserGroup> findByGroupId(Integer groupId) {
        return new ArrayList<>();
    }

    public boolean insertUserToGroup(Integer userId, Integer groupId) {
        return true;
    }

    public boolean delete(Integer userId, Integer groupId) {
        return true;
    }

    public void deleteByGroupId(Integer groupId) {
    }
}
