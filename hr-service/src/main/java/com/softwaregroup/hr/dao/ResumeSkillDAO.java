package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeSkill;

import java.util.List;

/**
 * 简历技能数据访问接口
 */
public interface ResumeSkillDAO {
    boolean save(ResumeSkill skill);
    boolean update(ResumeSkill skill);
    boolean delete(Integer id);
    ResumeSkill findById(Integer id);
    List<ResumeSkill> findByResumeId(Integer resumeId);
}
