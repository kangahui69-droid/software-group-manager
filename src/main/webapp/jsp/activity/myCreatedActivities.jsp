<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="我发起的活动" />
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

    .btn-sm-outline {
        background: transparent;
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
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

    .btn-sm-outline:hover {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-calendar-plus me-2"></i>我发起的活动
            </h1>
            <p class="member-hero-subtitle">查看和管理您发起的活动</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/activity?action=createForm" class="btn-brand">
                <i class="bi bi-plus-lg"></i>发起新活动
            </a>
        </div>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible" role="alert">
                ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible" role="alert">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card-design">
            <div class="card-body-design">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>类型</th>
                            <th>地点</th>
                            <th>审批状态</th>
                            <th>活动时间</th>
                            <th>报名人数</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${createdActivities}">
                            <tr>
                                <td>
                                    <a href="${pageContext.request.contextPath}/activity?action=detail&id=${a.id}" class="text-decoration-none" style="color: var(--text-dark); font-weight: 500;">
                                        ${a.title}
                                    </a>
                                </td>
                                <td style="color: var(--text-secondary);">
                                    <c:choose>
                                        <c:when test="${a.activityType == 'LECTURE'}">讲座</c:when>
                                        <c:when test="${a.activityType == 'SEMINAR'}">讨论会</c:when>
                                        <c:when test="${a.activityType == 'TEA_PARTY'}">茶话会</c:when>
                                        <c:when test="${a.activityType == 'PROJECT_INTRO'}">项目介绍</c:when>
                                        <c:when test="${a.activityType == 'WORKSHOP'}">工作坊</c:when>
                                        <c:when test="${a.activityType == 'COMPETITION'}">竞赛</c:when>
                                        <c:when test="${a.activityType == 'TRAINING'}">培训</c:when>
                                        <c:when test="${a.activityType == 'TEAM_BUILDING'}">团建</c:when>
                                        <c:otherwise>其他</c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="color: var(--text-secondary);">${a.location}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.approvalStatus == 'approved'}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已批准</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'pending'}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待审核</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'rejected'}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已拒绝</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td style="color: var(--text-secondary);">
                                    <fmt:formatDate value="${a.activityStartTime}" pattern="MM-dd HH:mm" />
                                </td>
                                <td style="color: var(--text-secondary);">
                                    ${a.currentParticipants > 0 ? a.currentParticipants : 0} / ${a.maxParticipants > 0 ? a.maxParticipants : '不限'}
                                </td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/activity?action=detail&id=${a.id}" class="btn-sm-brand">
                                            <i class="bi bi-eye"></i>查看
                                        </a>
                                        <c:if test="${a.approvalStatus == 'approved'}">
                                            <a href="${pageContext.request.contextPath}/group?action=createForActivity&activityId=${a.id}" class="btn-sm-outline">
                                                <i class="bi bi-people"></i>创建群聊
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty createdActivities}">
                            <tr>
                                <td colspan="7" class="text-center text-muted" style="padding: 48px;">您还没有发起过活动</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />