package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.ProjectMemberApplication;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目数据访问层
 */
@Repository
public class ProjectDAO {

    public List<Project> findByConditions(String keyword, String status, Integer year) {
        return new ArrayList<>();
    }

    public Project findById(Integer id) {
        return null;
    }

    public List<Project> findByUserId(Integer userId) {
        return new ArrayList<>();
    }

    public List<Project> findProjectsByUserId(Integer userId) {
        return new ArrayList<>();
    }

    public List<Project> findByOwnerId(Integer ownerId) {
        return new ArrayList<>();
    }

    public int countProjectsByMemberAndYear(Integer userId, Integer year) {
        return 0;
    }

    public boolean insert(Project project) {
        if (project.getId() == null) {
            project.setId(1);
        }
        return true;
    }

    public boolean update(Project project, Integer operatorId) {
        return true;
    }

    public boolean delete(Integer id, Integer operatorId) {
        return true;
    }

    public boolean approve(Integer id, Integer approverId, Object extra) {
        return true;
    }

    public boolean reject(Integer id, Integer approverId, Object extra) {
        return true;
    }

    public boolean isMember(Integer projectId, Integer userId) {
        return false;
    }

    public boolean hasPendingApplication(Integer projectId, Integer userId) {
        return false;
    }

    public boolean applyMember(Integer projectId, Integer userId, String reason, Integer approverId) {
        return true;
    }

    public boolean approveMemberApplication(Integer applicationId, Integer approverId, Object extra) {
        return true;
    }

    public boolean rejectMemberApplication(Integer applicationId, Integer approverId, String reason, Object extra) {
        return true;
    }

    public ProjectMemberApplication getMemberApplicationById(Integer applicationId) {
        return null;
    }

    public void addMember(Integer projectId, Integer userId, String role, Integer approverId) {
    }

    public void addHistory(Integer projectId, String action, Integer operatorId, String detail, String reason, Integer targetId, Object extra) {
    }

    public boolean addPlan(Integer projectId, Object plan) {
        return true;
    }

    public boolean addProgress(Integer projectId, Object progress) {
        return true;
    }

    public List<Object> getProjectMembers(Integer projectId) {
        return new ArrayList<>();
    }

    public List<Object> getLabels(Integer projectId) {
        return new ArrayList<>();
    }

    public List<Object> getPlans(Integer projectId) {
        return new ArrayList<>();
    }

    public List<Object> getProgressList(Integer projectId) {
        return new ArrayList<>();
    }

    public List<Object> getHistory(Integer projectId) {
        return new ArrayList<>();
    }
}
