package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.RecruitApplication;

import java.util.List;

/**
 * 招新申请数据访问接口
 */
public interface RecruitApplicationDAO {
    boolean insert(RecruitApplication app);
    boolean update(RecruitApplication app);
    boolean delete(Integer id);
    RecruitApplication findById(Integer id);
    List<RecruitApplication> findByConditions(String keyword, Integer year, String status, Integer round);
    int countPending();
    List<Integer> findAllYears();
}
