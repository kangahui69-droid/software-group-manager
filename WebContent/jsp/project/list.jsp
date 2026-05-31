<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .btn-success { background-color: #10b981; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-success:hover { background-color: #059669; color: white; }
    .btn-danger { background-color: #ef4444; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-danger:hover { background-color: #dc2626; color: white; }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-approved { background: #d1fae5; color: #065f46; }
    .badge-in-progress { background: #dbeafe; color: #1e40af; }
    .badge-completed { background: #d1fae5; color: #065f46; }
    .badge-canceled { background: #fee2e2; color: #991b1b; }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    项目管理
                </h2>
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
                                <td class="text-muted">${p.leaderId}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'pending'}">
                                            <span class="badge-design badge-pending">待审批</span>
                                        </c:when>
                                        <c:when test="${p.status == 'approved'}">
                                            <span class="badge-design badge-approved">已批准</span>
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
                                    </c:choose>
                                </td>
                                <td class="text-muted">${p.year}</td>
                                <td class="text-end">
                                    <c:if test="${p.status == 'pending'}">
                                        <div class="btn-group" style="gap: 8px;">
                                            <form action="${pageContext.request.contextPath}/project" method="post" style="display: inline;">
                                                <input type="hidden" name="action" value="approve">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button type="submit" class="btn btn-success btn-sm">批准</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/project" method="post" style="display: inline;">
                                                <input type="hidden" name="action" value="reject">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button type="submit" class="btn btn-danger btn-sm">拒绝</button>
                                            </form>
                                        </div>
                                    </c:if>
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