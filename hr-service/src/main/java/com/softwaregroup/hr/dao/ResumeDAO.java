package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.Resume;

import java.util.List;

/**
 * 简历数据访问接口
 */
public interface ResumeDAO {
    boolean save(Resume resume);
    boolean update(Resume resume);
    boolean softDelete(Integer id);
    boolean setDefaultResume(Integer resumeId, Integer userId);
    boolean restore(Integer id);
    boolean hardDelete(Integer id);
    Resume findById(Integer id);
    Resume findById(Integer id, boolean includeDeleted);
    List<Resume> findByUserId(Integer userId);
    List<Resume> findDeletedByUserId(Integer userId);
}
