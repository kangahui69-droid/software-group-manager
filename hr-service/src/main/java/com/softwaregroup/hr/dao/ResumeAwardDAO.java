package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeAward;

import java.util.List;

/**
 * 简历获奖情况数据访问接口
 */
public interface ResumeAwardDAO {
    boolean save(ResumeAward award);
    boolean update(ResumeAward award);
    boolean delete(Integer id);
    ResumeAward findById(Integer id);
    List<ResumeAward> findByResumeId(Integer resumeId);
}
