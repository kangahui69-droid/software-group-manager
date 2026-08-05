package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeEducation;

import java.util.List;

/**
 * 简历教育经历数据访问接口
 */
public interface ResumeEducationDAO {
    boolean save(ResumeEducation education);
    boolean update(ResumeEducation education);
    boolean delete(Integer id);
    ResumeEducation findById(Integer id);
    List<ResumeEducation> findByResumeId(Integer resumeId);
}
