<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-blue-lt { background: #dbeafe; color: #1e40af; }
    .badge-raw { background: #fef3c7; color: #92400e; }
    .badge-cleaned { background: #dbeafe; color: #1e40af; }
    .badge-published { background: #d1fae5; color: #065f46; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .card-title-design { font-family: var(--font-display); font-size: 1.5rem; font-weight: 500; color: var(--text-dark); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    奖项管理
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
                            <th>比赛</th>
                            <th>级别</th>
                            <th>年份</th>
                            <th>状态</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${awards}">
                            <tr>
                                <td class="fw-medium">${a.title}</td>
                                <td class="text-muted">${a.competition}</td>
                                <td class="text-muted">${a.level}</td>
                                <td class="text-muted">${a.year}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.status == 'RAW'}">
                                            <span class="badge-design badge-raw">原始</span>
                                        </c:when>
                                        <c:when test="${a.status == 'CLEANED'}">
                                            <span class="badge-design badge-cleaned">已清洗</span>
                                        </c:when>
                                        <c:when test="${a.status == 'PUBLISHED'}">
                                            <span class="badge-design badge-published">已发布</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <div class="btn-group" style="gap: 8px;">
                                        <button class="btn btn-sm" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard);">清洗</button>
                                        <button class="btn btn-sm" style="background: #10b981; color: white; border-radius: var(--radius-standard);">发布</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty awards}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-5">暂无奖项记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>