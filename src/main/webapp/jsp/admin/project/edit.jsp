<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="编辑项目" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-pencil-square me-2"></i>编辑项目信息
                    </h1>
                    <p class="admin-hero-subtitle">修改项目详细信息</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=manage" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/project" method="POST" class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">项目基本信息</h3>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="${project.id}">
                        
                        <div class="mb-3">
                            <label class="form-label-design required">项目标题</label>
                            <input type="text" class="input-design" name="title" value="${project.title}" placeholder="请输入项目标题" required>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label-design">项目描述</label>
                            <textarea class="input-design" name="description" rows="4" placeholder="请输入项目描述">${project.description}</textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">负责人</label>
                                    <select class="input-design" name="leaderId" required>
                                        <c:forEach var="user" items="${allUsers}">
                                            <option value="${user.id}" ${project.leaderId == user.id ? 'selected' : ''}>${user.username}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">年份</label>
                                    <input type="number" class="input-design" name="year" value="${project.year}" placeholder="请输入项目年份" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">期望开始时间</label>
                                    <input type="date" class="input-design" name="expectedStartDate" value="${project.expectedStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">期望结束时间</label>
                                    <input type="date" class="input-design" name="expectedEndDate" value="${project.expectedEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">实际开始时间</label>
                                    <input type="date" class="input-design" name="actualStartDate" value="${project.actualStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">实际结束时间</label>
                                    <input type="date" class="input-design" name="actualEndDate" value="${project.actualEndDate}">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label-design required">项目状态</label>
                            <select class="input-design" name="status" required>
                                <option value="PENDING" ${project.status == 'PENDING' ? 'selected' : ''}>待审批</option>
                                <option value="APPROVED" ${project.status == 'APPROVED' ? 'selected' : ''}>已批准</option>
                                <option value="REJECTED" ${project.status == 'REJECTED' ? 'selected' : ''}>已拒绝</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer-design text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/project?action=list" class="btn-outline-brand">取消</a>
                            <button type="submit" class="btn-brand ms-auto">保存更改</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />