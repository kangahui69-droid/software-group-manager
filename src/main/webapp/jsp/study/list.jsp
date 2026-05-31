<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="学习记录"/>
    <jsp:param name="active" value="study"/>
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
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

    .stat-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        text-align: center;
        transition: all 0.3s ease;
    }

    .stat-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-purple);
    }

    .stat-number {
        font-family: var(--font-display);
        font-size: 2rem;
        font-weight: 600;
        margin-bottom: 4px;
    }

    .stat-label {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
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

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-list-ul me-2"></i>学习记录
            </h1>
            <p class="member-hero-subtitle">查看您的学习历程</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${ctx}/study" class="btn-outline-brand">
                <i class="bi bi-arrow-left"></i>返回学习中心
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="row g-4 mb-4">
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-number" style="color: var(--brand-blue);">${statistics.totalSessions}</div>
                    <div class="stat-label">总学习次数</div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-number" style="color: #10b981;">${statistics.completedSessions}</div>
                    <div class="stat-label">已完成</div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-number" style="color: #8b5cf6;">${statistics.totalDuration}</div>
                    <div class="stat-label">总时长(分钟)</div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="stat-number" style="color: #f59e0b;">
                        <c:choose>
                            <c:when test="${not empty statistics.avgDuration}">
                                <fmt:formatNumber value="${statistics.avgDuration}" pattern="#0"/>
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="stat-label">平均时长(分钟)</div>
                </div>
            </div>
        </div>

        <div class="card-design">
            <div class="card-body-design">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>日期</th>
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
                                    <td colspan="5" class="text-center text-muted" style="padding: 48px;">
                                        暂无学习记录
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${sessionList}" var="sess">
                                    <tr>
                                        <td><fmt:formatDate value="${sess.sessionDate}" pattern="yyyy-MM-dd"/></td>
                                        <td style="color: var(--text-secondary);"><fmt:formatDate value="${sess.checkInTime}" pattern="HH:mm:ss"/></td>
                                        <td style="color: var(--text-secondary);">
                                            <c:if test="${not empty sess.checkOutTime}">
                                                <fmt:formatDate value="${sess.checkOutTime}" pattern="HH:mm:ss"/>
                                            </c:if>
                                            <c:if test="${empty sess.checkOutTime}">--</c:if>
                                        </td>
                                        <td style="color: var(--text-secondary);">
                                            <c:if test="${not empty sess.duration}">${sess.duration} 分钟</c:if>
                                            <c:if test="${empty sess.duration}"><span style="color: #f59e0b;">进行中</span></c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${sess.status == 'ACTIVE'}">
                                                    <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">进行中</span>
                                                </c:when>
                                                <c:when test="${sess.status == 'COMPLETED'}">
                                                    <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已完成</span>
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

<jsp:include page="/jsp/common/layout_bottom.jsp"/>