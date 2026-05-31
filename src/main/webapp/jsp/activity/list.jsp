<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动列表" />
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
        transition: all 0.3s ease;
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    .card-design:hover {
        transform: translateY(-6px);
        box-shadow: var(--shadow-brand-offset);
    }

    .card-body-design {
        padding: 24px;
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
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

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        justify-content: center;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .activity-status {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
    }

    .empty-state {
        text-align: center;
        padding: 48px;
    }

    .empty-state-icon {
        font-size: 64px;
        color: var(--text-muted);
        margin-bottom: 16px;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-calendar-event me-2"></i>活动列表
            </h1>
            <p class="member-hero-subtitle">
                <c:choose>
                    <c:when test="${viewMode == 'inPeriod'}">
                        显示正在报名中的活动
                    </c:when>
                    <c:otherwise>
                        显示所有活动（包括已过报名时间的）
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <div class="d-flex gap-2 mb-4">
            <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="btn-outline-brand">
                <i class="bi bi-list-ul"></i>我的活动
            </a>
            <c:choose>
                <c:when test="${viewMode == 'inPeriod'}">
                    <a href="${pageContext.request.contextPath}/activity?action=list" class="btn-outline-brand">
                        <i class="bi bi-grid-3x3-gap"></i>查看全部
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/activity?action=list&viewMode=inPeriod" class="btn-brand">
                        <i class="bi bi-filter"></i>仅看可报名
                    </a>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="card-design mb-4" style="padding: 20px 24px;">
            <form action="${pageContext.request.contextPath}/activity" method="get" class="row g-3 align-items-end">
                <input type="hidden" name="action" value="list">
                <input type="hidden" name="viewMode" value="${viewMode}">
                <div class="col-md-4">
                    <input type="text" name="keyword" class="form-control" placeholder="搜索活动名称、描述、地点" value="${keyword}" style="border-radius: var(--radius-standard);">
                </div>
                <div class="col-md-3">
                    <select name="activityType" class="form-select" style="border-radius: var(--radius-standard);">
                        <option value="">所有类型</option>
                        <option value="LECTURE" ${activityType == 'LECTURE' ? 'selected' : ''}>讲座</option>
                        <option value="SEMINAR" ${activityType == 'SEMINAR' ? 'selected' : ''}>讨论会</option>
                        <option value="TEA_PARTY" ${activityType == 'TEA_PARTY' ? 'selected' : ''}>茶话会</option>
                        <option value="PROJECT_INTRO" ${activityType == 'PROJECT_INTRO' ? 'selected' : ''}>项目介绍</option>
                        <option value="WORKSHOP" ${activityType == 'WORKSHOP' ? 'selected' : ''}>工作坊</option>
                        <option value="COMPETITION" ${activityType == 'COMPETITION' ? 'selected' : ''}>竞赛</option>
                        <option value="TRAINING" ${activityType == 'TRAINING' ? 'selected' : ''}>培训</option>
                        <option value="TEAM_BUILDING" ${activityType == 'TEAM_BUILDING' ? 'selected' : ''}>团建</option>
                        <option value="OTHER" ${activityType == 'OTHER' ? 'selected' : ''}>其他</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn-brand w-100" style="justify-content: center;">
                        <i class="bi bi-search"></i>搜索
                    </button>
                </div>
                <div class="col-md-2">
                    <a href="${pageContext.request.contextPath}/activity?action=list" class="btn-outline-brand w-100" style="justify-content: center;">
                        <i class="bi bi-arrow-counterclockwise"></i>重置
                    </a>
                </div>
            </form>
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

        <c:choose>
            <c:when test="${not empty activities}">
                <div class="row g-4">
                    <c:forEach var="activity" items="${activities}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card-design" style="position: relative;">
                                <div class="activity-status" style="
                                    <c:choose>
                                        <c:when test="${activity.inRegistrationPeriod}">background: #10b981;</c:when>
                                        <c:when test="${activity.registrationEnded}">background: #64748b;</c:when>
                                        <c:otherwise>background: #f59e0b;</c:otherwise>
                                    </c:choose>
                                "></div>
                                <div class="card-body-design">
                                    <div class="d-flex flex-wrap gap-2 mb-3">
                                        <span class="badge-design" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                            <c:choose>
                                                <c:when test="${activity.activityType == 'LECTURE'}">讲座</c:when>
                                                <c:when test="${activity.activityType == 'SEMINAR'}">讨论会</c:when>
                                                <c:when test="${activity.activityType == 'TEA_PARTY'}">茶话会</c:when>
                                                <c:when test="${activity.activityType == 'PROJECT_INTRO'}">项目介绍</c:when>
                                                <c:when test="${activity.activityType == 'WORKSHOP'}">工作坊</c:when>
                                                <c:when test="${activity.activityType == 'COMPETITION'}">竞赛</c:when>
                                                <c:when test="${activity.activityType == 'TRAINING'}">培训</c:when>
                                                <c:when test="${activity.activityType == 'TEAM_BUILDING'}">团建</c:when>
                                                <c:otherwise>其他</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <c:choose>
                                            <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                                                <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">报名已结束</span>
                                            </c:when>
                                            <c:when test="${activity.inRegistrationPeriod}">
                                                <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">报名进行中</span>
                                            </c:when>
                                            <c:when test="${activity.registrationEnded}">
                                                <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">报名已结束</span>
                                            </c:when>
                                            <c:when test="${!activity.registrationStarted}">
                                                <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">报名未开始</span>
                                            </c:when>
                                        </c:choose>
                                        <c:if test="${activity.registeredByCurrentUser}">
                                            <span class="badge-design" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">已报名</span>
                                        </c:if>
                                    </div>
                                    
                                    <h3 class="mb-2" style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark);">${activity.title}</h3>
                                    <p class="mb-3" style="color: var(--text-secondary); font-size: 0.94rem; line-height: 1.6; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">${activity.description}</p>
                                    
                                    <div class="mb-2" style="color: var(--text-secondary); font-size: 0.875rem;">
                                        <i class="bi bi-calendar3 me-2"></i>
                                        <fmt:formatDate value="${activity.activityStartTime}" pattern="MM-dd HH:mm" /> ~ 
                                        <fmt:formatDate value="${activity.activityEndTime}" pattern="MM-dd HH:mm" />
                                    </div>
                                    
                                    <div class="mb-2" style="color: var(--text-secondary); font-size: 0.875rem;">
                                        <i class="bi bi-geo-alt me-2"></i>${activity.location}
                                    </div>
                                    
                                    <div class="mb-2" style="color: var(--text-secondary); font-size: 0.875rem;">
                                        <i class="bi bi-clock me-2"></i>报名截止：<fmt:formatDate value="${activity.registrationEndTime}" pattern="MM-dd HH:mm" />
                                    </div>
                                    
                                    <c:if test="${activity.maxParticipants > 0}">
                                        <div class="mb-3" style="color: var(--text-secondary); font-size: 0.875rem;">
                                            <i class="bi bi-people me-2"></i>名额：${activity.maxParticipants}人
                                        </div>
                                    </c:if>
                                    
                                    <a href="${pageContext.request.contextPath}/activity?action=detail&id=${activity.id}" class="btn-sm-brand w-100 mt-auto">
                                        <i class="bi bi-eye"></i>查看详情
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-design">
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-calendar-x"></i>
                        </div>
                        <h3 style="font-family: var(--font-display); font-size: 1.5rem; font-weight: 600; color: var(--text-dark); margin-bottom: 8px;">暂无活动</h3>
                        <p style="color: var(--text-muted);">
                            <c:choose>
                                <c:when test="${viewMode == 'all'}">当前没有活动记录</c:when>
                                <c:otherwise>当前没有正在报名中的活动</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
