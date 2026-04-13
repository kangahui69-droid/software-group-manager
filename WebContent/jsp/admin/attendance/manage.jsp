<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .data-card { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); padding: 24px; text-align: center; transition: all 0.3s ease; }
    .data-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .stat-value { font-family: var(--font-display); font-size: 2.5rem; font-weight: 600; color: var(--brand-blue); }
    .stat-value-blue { color: var(--brand-blue); }
    .stat-value-green { color: #10b981; }
    .stat-value-purple { color: #8b5cf6; }
    .stat-value-orange { color: #f59e0b; }
    .stat-label { font-family: var(--font-ui); font-size: 0.88rem; color: var(--text-secondary); margin-top: 4px; }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-orange { background: #fed7aa; color: #9a3412; }
    .badge-green { background: #d1fae5; color: #065f46; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
</style>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none mb-4">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title mb-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
                            学习管理
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${ctx}/admin/dashboard" class="btn btn-outline-brand">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                            返回首页
                        </a>
                    </div>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                    ${error}
                </div>
            </c:if>

            <div class="row row-cards mb-4" style="gap: 20px;">
                <div class="col-sm-6 col-lg-3">
                    <div class="data-card">
                        <div class="stat-value stat-value-blue">${empty sessionStats.totalSessions ? 0 : sessionStats.totalSessions}</div>
                        <div class="stat-label">总学习次数</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="data-card">
                        <div class="stat-value stat-value-green">${empty sessionStats.completedSessions ? 0 : sessionStats.completedSessions}</div>
                        <div class="stat-label">已完成</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="data-card">
                        <div class="stat-value stat-value-purple">${empty sessionStats.totalDuration ? 0 : sessionStats.totalDuration}</div>
                        <div class="stat-label">总时长(分钟)</div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="data-card">
                        <div class="stat-value stat-value-orange">
                            <c:choose>
                                <c:when test="${not empty sessionStats.avgDuration}">
                                    <fmt:formatNumber value="${sessionStats.avgDuration}" pattern="#0"/>
                                </c:when>
                                <c:otherwise>0</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="stat-label">平均时长(分钟)</div>
                    </div>
                </div>
            </div>

            <div class="search-card mb-4">
                <form method="get" action="${ctx}/attendance/manage">
                    <div class="row g-3">
                        <div class="col-md-3">
                            <label class="form-label">开始日期</label>
                            <input type="date" name="startDate" class="form-control input-design" value="${startDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">结束日期</label>
                            <input type="date" name="endDate" class="form-control input-design" value="${endDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">选择成员</label>
                            <select name="userId" class="form-select input-design">
                                <option value="">全部成员</option>
                                <c:forEach items="${memberList}" var="member">
                                    <option value="${member.id}" ${selectedUserId == member.id ? 'selected' : ''}>
                                        ${not empty member.name ? member.name : member.username}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="submit" class="btn btn-brand me-2">查询</button>
                        </div>
                    </div>
                </form>
            </div>

            <div class="card-design">
                <div class="table-responsive">
                    <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                        <thead>
                            <tr>
                                <th>日期</th>
                                <th>成员</th>
                                <th>开始时间</th>
                                <th>结束时间</th>
                                <th>学习时长</th>
                                <th>状态</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty sessionList}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted py-5">
                                            暂无学习记录
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${sessionList}" var="sess">
                                        <tr>
                                            <td><fmt:formatDate value="${sess.sessionDate}" pattern="yyyy-MM-dd"/></td>
                                            <td>${sess.userName}</td>
                                            <td><fmt:formatDate value="${sess.checkInTime}" pattern="HH:mm:ss"/></td>
                                            <td>
                                                <c:if test="${not empty sess.checkOutTime}">
                                                    <fmt:formatDate value="${sess.checkOutTime}" pattern="HH:mm:ss"/>
                                                </c:if>
                                                <c:if test="${empty sess.checkOutTime}"><span class="text-muted">--</span></c:if>
                                            </td>
                                            <td>
                                                <c:if test="${not empty sess.duration}">${sess.duration} 分钟</c:if>
                                                <c:if test="${empty sess.duration}"><span class="badge-design badge-orange">进行中</span></c:if>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sess.status == 'ACTIVE'}">
                                                        <span class="badge-design badge-orange">进行中</span>
                                                    </c:when>
                                                    <c:when test="${sess.status == 'COMPLETED'}">
                                                        <span class="badge-design badge-green">已完成</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>