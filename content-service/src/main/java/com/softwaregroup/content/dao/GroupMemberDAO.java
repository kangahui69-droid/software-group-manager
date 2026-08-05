package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.GroupMember;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组成员数据访问层
 */
@Repository
public class GroupMemberDAO {

    public boolean isMember(Integer groupId, Integer userId) {
        return false;
    }

    public boolean isOwner(Integer groupId, Integer userId) {
        return false;
    }

    public boolean insertMember(Integer groupId, Integer userId) {
        return true;
    }

    public boolean insertOwner(Integer groupId, Integer userId) {
        return true;
    }

    public boolean delete(Integer groupId, Integer userId) {
        return true;
    }

    public void deleteByGroupId(Integer groupId) {
    }

    public List<GroupMember> findByGroupId(Integer groupId) {
        return new ArrayList<>();
    }
}
