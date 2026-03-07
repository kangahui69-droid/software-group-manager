<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="编辑项目" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    编辑项目信息
                </h2>
            </div>
            <div class="col-auto ms-auto">
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-outline-secondary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
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
                <form action="${pageContext.request.contextPath}/project" method="POST" class="card">
                    <div class="card-header">
                        <h3 class="card-title">项目基本信息</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="${project.id}">
                        
                        <div class="mb-3">
                            <label class="form-label required">项目标题</label>
                            <input type="text" class="form-control" name="title" value="${project.title}" placeholder="请输入项目标题" required>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">项目描述</label>
                            <textarea class="form-control" name="description" rows="4" placeholder="请输入项目描述">${project.description}</textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">负责人</label>
                                    <select class="form-select" name="leaderId" required>
                                        <c:forEach var="user" items="${allUsers}">
                                            <option value="${user.id}" ${project.leaderId == user.id ? 'selected' : ''}>${user.username}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">年份</label>
                                    <input type="number" class="form-control" name="year" value="${project.year}" placeholder="请输入项目年份" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">期望开始时间</label>
                                    <input type="date" class="form-control" name="expectedStartDate" value="${project.expectedStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">期望结束时间</label>
                                    <input type="date" class="form-control" name="expectedEndDate" value="${project.expectedEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">实际开始时间</label>
                                    <input type="date" class="form-control" name="actualStartDate" value="${project.actualStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">实际结束时间</label>
                                    <input type="date" class="form-control" name="actualEndDate" value="${project.actualEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label required">项目状态</label>
                            <select class="form-select" name="status" required>
                                <option value="PENDING" ${project.status == 'PENDING' ? 'selected' : ''}>待审批</option>
                                <option value="APPROVED" ${project.status == 'APPROVED' ? 'selected' : ''}>已批准</option>
                                <option value="REJECTED" ${project.status == 'REJECTED' ? 'selected' : ''}>已拒绝</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">保存更改</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />