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
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-body-design {
        padding: 0;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-people me-2"></i>用户管理
            </h1>
            <p class="admin-hero-subtitle">管理系统用户</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/admin/member/?action=add" class="btn-brand">
                <i class="bi bi-plus-lg"></i>添加用户
            </a>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
         
        <div class="card-design mb-3">
            <div class="card-body-design">
                <form method="get" action="${pageContext.request.contextPath}/admin/member/" class="row g-2 p-3">
                    <input type="hidden" name="action" value="list">
                    <div class="col-md-4">
                        <label class="form-label-design" style="font-size: 13px; font-weight: 500; color: #64748b;">关键词</label>
                        <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索标题/摘要/内容">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label-design" style="font-size: 13px; font-weight: 500; color: #64748b;">类型</label>
                        <select class="input-design" name="role">
                            <option value="">全部</option>
                            <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                            <option value="MEMBER" ${role == 'MEMBER' ? 'selected' : ''}>成员</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label-design" style="font-size: 13px; font-weight: 500; color: #64748b;">状态</label>
                        <select class="input-design" name="status">
                            <option value="">全部</option>
                            <option value="1" ${status == '1' ? 'selected' : ''}>启用</option>
                            <option value="0" ${status == '0' ? 'selected' : ''}>禁用</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn-brand w-100">
                            <i class="bi bi-search me-1"></i>搜索
                        </button>
                    </div>
                </form>
            </div>
        </div>
        
        <div class="card-design">
            <div class="table-responsive">
                <table class="table-design">
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
                                    <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">
                                        ${user.role}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-design" style="background: ${user.status == 1 ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)'}; color: ${user.status == 1 ? '#059669' : '#dc2626'};">
                                        ${user.status == 1 ? '启用' : '禁用'}
                                    </span>
                                </td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/admin/member/?action=view&id=${user.id}" 
                                           class="btn-sm-brand">查看</a>
                                        <c:if test="${user.id != currentUserId}">
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" 
                                                  onsubmit="return confirm('确定要删除吗？')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="btn-danger-design btn-sm">删除</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" class="ms-1">
                                                <input type="hidden" name="action" value="${user.status == 1 ? 'disable' : 'enable'}">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="${user.status == 1 ? 'btn-outline-brand' : 'btn-success-design'} btn-sm">
                                                    ${user.status == 1 ? '禁用' : '启用'}
                                                </button>
                                            </form>
                                        </c:if>
                                        <form action="${pageContext.request.contextPath}/admin/member/" method="POST" class="ms-1">
                                            <input type="hidden" name="action" value="resetPassword">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <button type="submit" class="btn-outline-brand btn-sm" onclick="return confirm('确定要重置密码为123456吗？')">
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