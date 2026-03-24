<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 获取当前登录用户信息 --%>
<% 
    model.User currentUser = (model.User) session.getAttribute("user");
    Integer currentUserId = null;
    if (currentUser != null) {
        currentUserId = currentUser.getId();
    }
    pageContext.setAttribute("currentUserId", currentUserId);
%>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="用户管理" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    用户管理
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/admin/member/?action=add" class="btn btn-primary d-none d-sm-inline-block">
                        <i class="bi bi-plus-lg me-2"></i>添加成员
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <!-- 成功信息显示 -->
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <!-- 错误信息显示 -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
         
        <div class="card mb-3">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/admin/member/" class="row g-3">
                    <input type="hidden" name="action" value="list">
                    <div class="col-md-4">
                        <label class="form-label">关键词</label>
                        <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索用户名/姓名/邮箱/手机">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">角色</label>
                        <select class="form-select" name="role">
                            <option value="">全部</option>
                            <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                            <option value="MEMBER" ${role == 'MEMBER' ? 'selected' : ''}>成员</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">状态</label>
                        <select class="form-select" name="status">
                            <option value="">全部</option>
                            <option value="1" ${status == '1' ? 'selected' : ''}>启用</option>
                            <option value="0" ${status == '0' ? 'selected' : ''}>禁用</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">搜索</button>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <a href="${pageContext.request.contextPath}/admin/member/" class="btn btn-outline-secondary w-100">重置</a>
                    </div>
                </form>
            </div>
        </div>
        
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>用户名</th>
                            <th>姓名</th>
                            <th>邮箱</th>
                            <th>手机</th>
                            <th>角色</th>
                            <th>状态</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td>${user.username}</td>
                                <td>${user.name}</td>
                                <td>${user.email}</td>
                                <td>${user.phone}</td>
                                <td>
                                    <span class="badge bg-${user.role == 'ADMIN' ? 'danger' : 'info'}">
                                        ${user.role}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge bg-${user.status == 1 ? 'success' : 'danger'}">
                                        ${user.status == 1 ? '启用' : '禁用'}
                                    </span>
                                </td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/admin/member/?action=view&id=${user.id}" 
                                           class="btn btn-white btn-sm">查看</a>
                                        <c:if test="${user.id != currentUserId}">
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" 
                                                  onsubmit="return confirm('确定要删除吗？')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="btn btn-outline-danger btn-sm">删除</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" class="ms-1">
                                                <input type="hidden" name="action" value="${user.status == 1 ? 'disable' : 'enable'}">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="btn btn-${user.status == 1 ? 'outline-warning' : 'outline-success'} btn-sm">
                                                    ${user.status == 1 ? '禁用' : '启用'}
                                                </button>
                                            </form>
                                        </c:if>
                                        <form action="${pageContext.request.contextPath}/admin/member/" method="POST" class="ms-1">
                                            <input type="hidden" name="action" value="resetPassword">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <button type="submit" class="btn btn-outline-info btn-sm" onclick="return confirm('确定要重置密码为123456吗？')">
                                                重置密码
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty users}">
                            <tr>
                                <td colspan="6" class="text-center text-muted">暂无成员记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />