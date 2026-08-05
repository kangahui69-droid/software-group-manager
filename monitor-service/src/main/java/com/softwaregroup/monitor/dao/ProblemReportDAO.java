package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.ProblemReport;

import java.util.List;

/**
 * 问题报告数据访问接口
 */
public interface ProblemReportDAO {
    int insert(ProblemReport problem);
    boolean updateCategoryAndStatus(Integer id, String category, String status, String comment, Integer operatorId);
    boolean updateAdminComment(Integer id, String comment);
    ProblemReport findById(Integer id);
    List<ProblemReport> findAll();
    List<ProblemReport> findByCategory(String category);
    List<ProblemReport> findByStatus(String status);
    List<ProblemReport> findByUserId(Integer userId);
    boolean delete(Integer id);
    int countPending();
    int countByCategory(String category);
    int countByStatus(String status);
}
