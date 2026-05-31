<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="编辑成员" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-pencil-square me-2"></i>编辑成员
                    </h1>
                    <p class="admin-hero-subtitle">修改用户信息</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/member" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <div class="row">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-body-design">
                        <form method="post" action="${pageContext.request.contextPath}/admin/member/">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="id" value="${user.id}">
                            
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger" role="alert">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-alert-circle" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <circle cx="12" cy="12" r="9"></circle>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                        <line x1="11" y1="12" x2="12" y2="12"></line>
                                        <line x1="11" y1="16" x2="12" y2="16"></line>
                                    </svg>
                                    ${error}
                                </div>
                            </c:if>
                            
                            <div class="mb-3">
                                <label class="form-label-design required">用户名</label>
                                <input type="text" class="input-design" name="username" value="${user.username}" placeholder="请输入用户名" required>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label-design">密码（留空不修改）</label>
                                <input type="password" class="input-design" name="password" placeholder="请输入密码">
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label-design required">角色</label>
                                <select class="input-design" name="role" required>
                                    <option value="MEMBER" ${user.role == 'MEMBER' ? 'selected' : ''}>普通成员</option>
                                    <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label-design required">用户类型</label>
                                <select class="input-design" name="userType" required>
                                    <option value="STUDENT" ${user.userType == 'STUDENT' ? 'selected' : ''}>学生</option>
                                    <option value="TEACHER" ${user.userType == 'TEACHER' ? 'selected' : ''}>教师</option>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label-design">状态</label>
                                <select class="input-design" name="status">
                                    <option value="1" ${user.status == 1 ? 'selected' : ''}>启用</option>
                                    <option value="0" ${user.status == 0 ? 'selected' : ''}>禁用</option>
                                </select>
                            </div>
                            
                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/admin/member/list" class="btn-outline-brand me-2">取消</a>
                                <button type="submit" class="btn-brand">保存</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-body-design">
                        <h3 class="card-title-design">编辑成员须知</h3>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" style="color: rgba(20, 86, 240, 0.8);" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                用户名可以修改
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" style="color: rgba(20, 86, 240, 0.8);" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                密码留空表示不修改
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" style="color: rgba(20, 86, 240, 0.8);" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                角色和用户类型可以修改
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" style="color: rgba(20, 86, 240, 0.8);" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                状态可以启用或禁用
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />