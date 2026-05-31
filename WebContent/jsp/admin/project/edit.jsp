<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); box-shadow: var(--shadow-standard); }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .card-title-design { font-family: var(--font-display); font-size: 1.75rem; font-weight: 500; color: var(--text-dark); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    编辑项目信息
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-outline-brand">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回列表
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/project" method="POST" class="card-design">
                    <div class="card-body p-4">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                                ${error}
                            </div>
                        </c:if>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="${project.id}">
                        
                        <div class="mb-4">
                            <label class="form-label required">项目标题</label>
                            <input type="text" class="form-control input-design" name="title" value="${project.title}" placeholder="请输入项目标题" required>
                        </div>
                        
                        <div class="mb-4">
                            <label class="form-label">项目描述</label>
                            <textarea class="form-control input-design" name="description" rows="4" placeholder="请输入项目描述">${project.description}</textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">负责人</label>
                                    <select class="form-select input-design" name="leaderId" required>
                                        <c:forEach var="user" items="${allUsers}">
                                            <option value="${user.id}" ${project.leaderId == user.id ? 'selected' : ''}>${user.username}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">年份</label>
                                    <input type="number" class="form-control input-design" name="year" value="${project.year}" placeholder="请输入项目年份" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">期望开始时间</label>
                                    <input type="date" class="form-control input-design" name="expectedStartDate" value="${project.expectedStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">期望结束时间</label>
                                    <input type="date" class="form-control input-design" name="expectedEndDate" value="${project.expectedEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">实际开始时间</label>
                                    <input type="date" class="form-control input-design" name="actualStartDate" value="${project.actualStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">实际结束时间</label>
                                    <input type="date" class="form-control input-design" name="actualEndDate" value="${project.actualEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-4">
                            <label class="form-label required">项目状态</label>
                            <select class="form-select input-design" name="status" required>
                                <option value="PENDING" ${project.status == 'PENDING' ? 'selected' : ''}>待审批</option>
                                <option value="APPROVED" ${project.status == 'APPROVED' ? 'selected' : ''}>已批准</option>
                                <option value="REJECTED" ${project.status == 'REJECTED' ? 'selected' : ''}>已拒绝</option>
                            </select>
                        </div>
                        
                        <div class="d-flex justify-content-end" style="gap: 12px;">
                            <a href="${pageContext.request.contextPath}/project?action=list" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 11px 20px;">取消</a>
                            <button type="submit" class="btn btn-brand">保存更改</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>