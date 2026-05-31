<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .btn-danger { background-color: #ef4444; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-danger:hover { background-color: #dc2626; color: white; }
    .btn-sm { padding: 6px 12px; font-size: 0.875rem; }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-approved { background: #d1fae5; color: #065f46; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .card-title-design { font-family: var(--font-display); font-size: 1.5rem; font-weight: 500; color: var(--text-dark); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    招新报名管理
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty error}">
            <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                ${error}
            </div>
        </c:if>
        
        <div class="search-card mb-4">
            <form method="get" action="${pageContext.request.contextPath}/admin/recruit/manage" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">关键词</label>
                    <input type="text" class="form-control input-design" name="keyword" value="${keyword}" placeholder="搜索姓名/学号/手机/邮箱">
                </div>
                <div class="col-md-2">
                    <label class="form-label">年份</label>
                    <select class="form-select input-design" name="year">
                        <option value="">全部</option>
                        <c:forEach var="y" items="${years}">
                            <option value="${y}" ${selectedYear==y ? 'selected' : ''}>${y}年</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">状态</label>
                    <select class="form-select input-design" name="status">
                        <option value="">全部</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>待审核</option>
                        <option value="2" ${status == '2' ? 'selected' : ''}>已通过</option>
                        <option value="0" ${status == '0' ? 'selected' : ''}>已拒绝</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-brand w-100">搜索</button>
                </div>
            </form>
        </div>
        
        <div class="card-design">
            <div class="card-body p-0">
                <div class="p-4 border-bottom" style="border-color: var(--border-light);">
                    <h3 class="card-title-design mb-0">报名列表</h3>
                </div>
                <div class="table-responsive">
                    <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                        <thead>
                            <tr>
                                <th>姓名</th>
                                <th>学号</th>
                                <th>专业班级</th>
                                <th>手机号</th>
                                <th>状态</th>
                                <th>提交时间</th>
                                <th class="text-end">操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="app" items="${applicationList}">
                                <tr>
                                    <td class="fw-medium">${app.name}</td>
                                    <td class="text-muted">${app.studentId}</td>
                                    <td class="text-muted">${app.major} / ${app.grade}</td>
                                    <td class="text-muted">${app.phone}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${app.status == 1}">
                                                <span class="badge-design badge-pending">待处理</span>
                                            </c:when>
                                            <c:when test="${app.status == 2}">
                                                <span class="badge-design badge-approved">已通过</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-design badge-rejected">已拒绝</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-muted">
                                        <fmt:formatDate value="${app.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end">
                                        <div class="btn-group" style="gap: 8px;">
                                            <a href="review?id=${app.id}" class="btn btn-sm" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard);">查看详情</a>
                                            <form action="manage" method="POST" style="display:inline;" onsubmit="return confirm('确定要删除吗？')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${app.id}">
                                                <button type="submit" class="btn btn-sm btn-danger">删除</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty applicationList}">
                                <tr>
                                    <td colspan="7" class="text-center text-muted py-5">暂无申请记录</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>