package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeProject;

import java.util.List;

/**
 * 简历项目经历数据访问接口
 */
public interface ResumeProjectDAO {
    boolean save(ResumeProject project);
    boolean update(ResumeProject project);
    boolean delete(Integer id);
    ResumeProject findById(Integer id);
    List<ResumeProject> findByResumeId(Integer resumeId);
}
