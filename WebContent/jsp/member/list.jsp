<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-blue-lt { background: #dbeafe; color: #1e40af; }
    .badge-success { background: #d1fae5; color: #065f46; }
    .badge-danger { background: #fee2e2; color: #991b1b; }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    成员管理
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/admin/member/add" class="btn btn-brand">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    添加成员
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card-design">
            <div class="table-responsive">
                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>用户名</th>
                            <th>角色</th>
                            <th>状态</th>
                            <th>创建时间</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="u" items="${userList}">
                            <tr>
                                <td>${u.id}</td>
                                <td class="fw-medium">${u.username}</td>
                                <td>
                                    <span class="badge-design badge-blue-lt text-capitalize">${u.role.toLowerCase()}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.status == 1}">
                                            <span class="badge-design badge-success">启用</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design badge-danger">禁用</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-muted">${u.createdAt}</td>
                                <td class="text-end">
                                    <div class="btn-group" style="gap: 8px;">
                                        <a href="${pageContext.request.contextPath}/admin/member/edit?id=${u.id}" 
                                           class="btn btn-sm" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard);">编辑</a>
                                        <a href="${pageContext.request.contextPath}/admin/member/toggleStatus?id=${u.id}" 
                                           class="btn btn-sm" style="background: var(--bg-light-gray); color: ${u.status == 1 ? '#f59e0b' : '#10b981'}; border-radius: var(--radius-standard);">
                                            ${u.status == 1 ? '禁用' : '启用'}
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/member/delete?id=${u.id}" 
                                           class="btn btn-sm" style="background: var(--bg-light-gray); color: #ef4444; border-radius: var(--radius-standard);" onclick="return confirm('确定要删除这个成员吗？');">删除</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty userList}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-5">暂无成员记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>