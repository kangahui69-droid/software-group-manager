package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.ActivityGroup;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动群组数据访问层
 */
@Repository
public class ActivityGroupDAO {

    public List<ActivityGroup> findAll() {
        return new ArrayList<>();
    }

    public ActivityGroup findById(Integer id) {
        return null;
    }

    public List<ActivityGroup> findByOwnerId(Integer ownerId) {
        return new ArrayList<>();
    }

    public int insert(ActivityGroup group) {
        return 1;
    }

    public boolean update(ActivityGroup group) {
        return true;
    }

    public boolean delete(Integer id) {
        return true;
    }

    public boolean muteGroup(Integer groupId, Date until, String reason) {
        return true;
    }

    public boolean unmuteGroup(Integer groupId) {
        return true;
    }
}
