<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="dao.ResumeDAO,model.Resume,java.util.List"%>
<%
    // 获取当前登录用户的ID，这里假设是27
    Integer userId = 27;
    ResumeDAO resumeDAO = new ResumeDAO();
    List<Resume> resumes = resumeDAO.findByUserId(userId);
    
    out.println("<h1>简历调试信息</h1>");
    out.println("<p>用户ID: " + userId + "</p>");
    out.println("<p>查询到的简历数量: " + resumes.size() + "</p>");
    out.println("<hr>");
    
    for (Resume r : resumes) {
        out.println("<p>简历ID: " + r.getId() + ", 名称: " + r.getResumeName() + ", 是否删除: " + r.getDeleted() + "</p>");
    }
%    
>
