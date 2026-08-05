package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.Award;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖项数据访问层
 */
@Repository
public class AwardDAO {

    public List<Award> findAll() {
        return new ArrayList<>();
    }

    public List<Award> findByStatus(String status) {
        return new ArrayList<>();
    }

    public Award findById(Integer id) {
        return null;
    }

    public List<Award> findByUserId(Integer userId) {
        return null;
    }

    public boolean insert(Award award) {
        return true;
    }

    public boolean update(Award award) {
        return true;
    }

    public boolean delete(Integer id) {
        return true;
    }

    public boolean approveAward(Integer id, Integer approverId) {
        return true;
    }

    public boolean rejectAward(Integer id, Integer approverId) {
        return true;
    }
}
