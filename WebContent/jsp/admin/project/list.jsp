<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); box-shadow: var(--shadow-standard); }
    .btn-success { background-color: #10b981; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-success:hover { background-color: #059669; color: white; transform: translateY(-2px); }
    .btn-danger { background-color: #ef4444; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-danger:hover { background-color: #dc2626; color: white; }
    .btn-outline { background: transparent; border-radius: var(--radius-standard); padding: 8px 16px; font-weight: 500; transition: all 0.3s ease; }
    .btn-outline-danger { color: #ef4444; border: 1px solid #ef4444; }
    .btn-outline-danger:hover { background: #ef4444; color: white; }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-approved { background: #d1fae5; color: #065f46; }
    .badge-in-progress { background: #dbeafe; color: #1e40af; }
    .badge-completed { background: #d1fae5; color: #065f46; }
    .badge-canceled { background: #fee2e2; color: #991b1b; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    项目管理
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <a href="${pageContext.request.contextPath}/project?action=create" class="btn btn-brand d-none d-sm-inline-block">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                    添加项目
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="search-card mb-4">
            <form method="get" action="${pageContext.request.contextPath}/project" class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">关键词</label>
                    <input type="text" class="form-control input-design" name="keyword" value="${keyword}" placeholder="搜索标题/描述/团队名">
                </div>
                <div class="col-md-3">
                    <label class="form-label">状态</label>
                    <select class="form-select input-design" name="status">
                        <option value="">全部</option>
                        <option value="pending" ${status == 'pending' ? 'selected' : ''}>待审核</option>
                        <option value="approved" ${status == 'approved' ? 'selected' : ''}>已通过</option>
                        <option value="rejected" ${status == 'rejected' ? 'selected' : ''}>已拒绝</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">年份</label>
                    <select class="form-select input-design" name="year">
                        <option value="">全部</option>
                        <option value="2026" ${year == '2026' ? 'selected' : ''}>2026</option>
                        <option value="2025" ${year == '2025' ? 'selected' : ''}>2025</option>
                        <option value="2024" ${year == '2024' ? 'selected' : ''}>2024</option>
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
                            <th>标题</th>
                            <th>负责人</th>
                            <th>状态</th>
                            <th>年份</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${projects}">
                            <tr>
                                <td class="fw-medium">${p.name}</td>
                                <td class="text-muted">${p.leaderName}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'pending'}">
                                            <span class="badge-design badge-pending">待审核</span>
                                        </c:when>
                                        <c:when test="${p.status == 'approved'}">
                                            <span class="badge-design badge-approved">已通过</span>
                                        </c:when>
                                        <c:when test="${p.status == 'in_progress'}">
                                            <span class="badge-design badge-in-progress">进行中</span>
                                        </c:when>
                                        <c:when test="${p.status == 'completed'}">
                                            <span class="badge-design badge-completed">已完成</span>
                                        </c:when>
                                        <c:when test="${p.status == 'canceled'}">
                                            <span class="badge-design badge-canceled">已取消</span>
                                        </c:when>
                                        <c:when test="${p.status == 'rejected'}">
                                            <span class="badge-design badge-rejected">已拒绝</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design badge-pending">${p.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${p.year}</td>
                                <td class="text-end">
                                    <div class="btn-group" style="gap: 8px;">
                                        <a href="${pageContext.request.contextPath}/project?action=edit&id=${p.id}" 
                                           class="btn btn-sm" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard);">编辑</a>
                                        <form action="${pageContext.request.contextPath}/project" method="POST"
                                            onsubmit="return confirm('确定要删除吗？')" style="display: inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit" class="btn btn-sm btn-outline btn-outline-danger">删除</button>
                                        </form>
                                        <c:if test="${p.status == 'pending'}">
                                            <form action="${pageContext.request.contextPath}/project" method="POST" style="display: inline;">
                                                <input type="hidden" name="action" value="approve">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button type="submit" class="btn btn-sm btn-success">批准</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty projects}">
                            <tr>
                                <td colspan="5" class="text-center text-muted py-5">暂无项目记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>