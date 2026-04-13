<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目历史 - ${project.name}" />
</jsp:include>

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

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .card-body-design {
        padding: 24px;
    }

    .btn-outline-brand {
        background: transparent;
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.3);
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
        background: rgba(255, 255, 255, 0.1);
        border-color: rgba(255, 255, 255, 0.5);
        color: white;
    }

    .timeline {
        position: relative;
        padding-left: 2rem;
    }

    .timeline::before {
        content: '';
        position: absolute;
        left: 0.75rem;
        top: 0;
        bottom: 0;
        width: 2px;
        background: var(--border-gray);
    }

    .timeline-event {
        position: relative;
        padding-bottom: 1.5rem;
    }

    .timeline-event:last-child {
        padding-bottom: 0;
    }

    .timeline-event-icon {
        position: absolute;
        left: -2rem;
        width: 1.5rem;
        height: 1.5rem;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
    }

    .timeline-event-content {
        margin-left: 0.5rem;
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
    }

    .timeline-event-body {
        padding: 16px 20px;
    }

    .timeline-event-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .timeline-event-desc {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-secondary);
        margin-bottom: 4px;
    }

    .timeline-event-meta {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h1 class="member-hero-title">
                        <i class="bi bi-clock-history me-2"></i>项目历史
                    </h1>
                    <p class="member-hero-subtitle">${project.name}</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn-outline-brand">
                    <i class="bi bi-arrow-left"></i>返回项目
                </a>
            </div>
        </div>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-list-ul text-brand"></i>项目历程
                </h3>
            </div>
            <div class="card-body-design">
                <c:if test="${empty historyList}">
                    <div class="text-center text-muted py-5">
                        <i class="bi bi-clock" style="font-size: 48px; opacity: 0.5;"></i>
                        <p class="mt-2">暂无历史记录</p>
                    </div>
                </c:if>

                <div class="timeline">
                    <c:forEach var="history" items="${historyList}">
                        <div class="timeline-event">
                            <div class="timeline-event-icon bg-${history.operationType == 'PROJECT_APPROVE' ? 'success' : 
                                history.operationType == 'PROJECT_REJECT' ? 'danger' : 
                                history.operationType == 'MEMBER_APPROVE' ? 'success' :
                                history.operationType == 'MEMBER_REJECT' ? 'danger' : 'primary'}">
                                <i class="bi bi-clock-history" style="font-size: 0.75rem;"></i>
                            </div>
                            <div class="timeline-event-content">
                                <div class="timeline-event-body">
                                    <div class="timeline-event-title">
                                        <c:choose>
                                            <c:when test="${history.operationType == 'PROJECT_APPLY'}">项目申请</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_APPROVE'}">项目审批通过</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_REJECT'}">项目审批驳回</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_TRANSFER'}">项目管理员转移</c:when>
                                            <c:when test="${history.operationType == 'MEMBER_APPLY'}">成员申请</c:when>
                                            <c:when test="${history.operationType == 'MEMBER_APPROVE'}">成员审批通过</c:when>
                                            <c:when test="${history.operationType == 'MEMBER_REJECT'}">成员审批驳回</c:when>
                                            <c:when test="${history.operationType == 'MEMBER_JOIN'}">成员加入</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_LABEL_ADD'}">添加项目标签</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_LABEL_REMOVE'}">移除项目标签</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_INFO_UPDATE'}">项目信息更新</c:when>
                                            <c:when test="${history.operationType == 'PROJECT_STATUS_CHANGE'}">项目状态变更</c:when>
                                            <c:otherwise>${history.operationType}</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="timeline-event-desc">${history.description}</div>
                                    <div class="timeline-event-meta">
                                        <span class="me-2">操作人: ${history.operatorName}</span>
                                        <span><fmt:formatDate value="${history.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />