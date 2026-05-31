<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); box-shadow: var(--shadow-standard); }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .btn-secondary-light { background: var(--bg-light-gray); color: #333333; border-radius: var(--radius-standard); padding: 11px 20px; border: none; }
    .btn-pill-nav { background: rgba(0, 0, 0, 0.05); color: var(--text-dark); border-radius: var(--radius-pill); border: none; transition: all 0.3s ease; }
    .btn-pill-nav:hover { background: rgba(0, 0, 0, 0.1); color: var(--text-dark); }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); }
    .badge-brand { background: var(--brand-blue); color: white; }
    .badge-success { background: #10b981; color: white; }
    .badge-warning { background: #f59e0b; color: white; }
    .badge-danger { background: #ef4444; color: white; }
    .badge-info { background: var(--primary-500); color: white; }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .data-card { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); padding: 24px; text-align: center; transition: all 0.3s ease; }
    .data-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .stat-value { font-family: var(--font-display); font-size: 2.5rem; font-weight: 600; color: var(--brand-blue); }
    .stat-label { font-family: var(--font-ui); font-size: 0.88rem; color: var(--text-secondary); margin-top: 4px; }
    .nav-link-design { font-family: var(--font-ui); font-size: 0.88rem; font-weight: 500; color: var(--text-dark); text-decoration: none; transition: color 0.3s ease; }
    .nav-link-design:hover { color: var(--brand-blue); }
    .text-brand { color: var(--brand-blue); }
    .section-heading { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; line-height: 1.50; color: var(--text-dark); }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .link-primary { color: var(--text-dark); text-decoration: none; transition: color 0.3s ease; }
    .link-primary:hover { color: var(--brand-blue); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    用户管理
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <a href="${pageContext.request.contextPath}/admin/member/?action=add" class="btn btn-brand d-none d-sm-inline-block">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                    添加成员
                </a>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert" style="border-radius: var(--radius-standard);">
                ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert" style="border-radius: var(--radius-standard);">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
         
        <div class="search-card mb-4">
            <form method="get" action="${pageContext.request.contextPath}/admin/member/" class="row g-3">
                <input type="hidden" name="action" value="list">
                <div class="col-md-4">
                    <label class="form-label">关键词</label>
                    <input type="text" class="form-control input-design" name="keyword" value="${keyword}" placeholder="搜索用户名/姓名/邮箱/手机">
                </div>
                <div class="col-md-3">
                    <label class="form-label">角色</label>
                    <select class="form-select input-design" name="role">
                        <option value="">全部</option>
                        <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                        <option value="MEMBER" ${role == 'MEMBER' ? 'selected' : ''}>成员</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">状态</label>
                    <select class="form-select input-design" name="status">
                        <option value="">全部</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>启用</option>
                        <option value="0" ${status == '0' ? 'selected' : ''}>禁用</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-brand w-100">搜索</button>
                </div>
            </form>
        </div>
        
        <div class="card-design">
            <div class="table-responsive">
                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                    <thead>
                        <tr>
                            <th>用户名</th>
                            <th>姓名</th>
                            <th>邮箱</th>
                            <th>手机</th>
                            <th>角色</th>
                            <th>状态</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td class="fw-medium">${user.username}</td>
                                <td>${user.name}</td>
                                <td class="text-muted">${user.email}</td>
                                <td class="text-muted">${user.phone}</td>
                                <td>
                                    <span class="badge-design badge-brand">
                                        ${user.role}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-design ${user.status == 1 ? 'badge-success' : 'badge-danger'}">
                                        ${user.status == 1 ? '启用' : '禁用'}
                                    </span>
                                </td>
                                <td class="text-end">
                                    <div class="btn-group" style="gap: 8px;">
                                        <a href="${pageContext.request.contextPath}/admin/member/?action=view&id=${user.id}" 
                                           class="btn btn-sm" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard);">查看</a>
                                        <c:if test="${user.id != currentUserId}">
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" 
                                                  onsubmit="return confirm('确定要删除吗？')" style="display: inline;">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="btn btn-sm btn-outline" style="color: #ef4444; border: 1px solid #ef4444; border-radius: var(--radius-standard);">删除</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/admin/member/" method="POST" style="display: inline;">
                                                <input type="hidden" name="action" value="${user.status == 1 ? 'disable' : 'enable'}">
                                                <input type="hidden" name="id" value="${user.id}">
                                                <button type="submit" class="btn btn-sm" style="background: ${user.status == 1 ? 'var(--bg-light-gray)' : '#10b981'}; color: ${user.status == 1 ? '#f59e0b' : 'white'}; border: ${user.status == 1 ? '1px solid #f59e0b' : 'none'}; border-radius: var(--radius-standard);">
                                                    ${user.status == 1 ? '禁用' : '启用'}
                                                </button>
                                            </form>
                                        </c:if>
                                        <form action="${pageContext.request.contextPath}/admin/member/" method="POST" style="display: inline;">
                                            <input type="hidden" name="action" value="resetPassword">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <button type="submit" class="btn btn-sm btn-outline" style="color: var(--brand-blue); border: 1px solid var(--brand-blue); border-radius: var(--radius-standard);" onclick="return confirm('确定要重置密码为123456吗？')">
                                                重置密码
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty users}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-5">暂无成员记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>