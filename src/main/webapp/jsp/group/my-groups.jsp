<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="我的群聊" />
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
    }

    .card-design:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-offset);
    }

    .card-body-design {
        padding: 24px;
    }

    .group-icon {
        width: 56px;
        height: 56px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        flex-shrink: 0;
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
        justify-content: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 16px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-chat-dots me-2"></i>我的群聊
            </h1>
            <p class="member-hero-subtitle">您加入的所有群聊</p>
        </div>

        <c:choose>
            <c:when test="${empty userGroups}">
                <div class="card-design">
                    <div class="card-body-design" style="text-align: center; padding: 48px;">
                        <div style="font-size: 64px; color: var(--text-muted); margin-bottom: 16px;">
                            <i class="bi bi-chat-dots"></i>
                        </div>
                        <h3 style="font-family: var(--font-display); font-size: 1.5rem; font-weight: 600; color: var(--text-dark); margin-bottom: 8px;">暂无群聊</h3>
                        <p style="color: var(--text-muted); font-size: 0.94rem;">您还没有加入任何群聊。请先发起活动，活动通过审核后可创建群聊。</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach var="ug" items="${userGroups}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card-design">
                                <div class="card-body-design">
                                    <div class="d-flex align-items-center mb-3">
                                        <a href="${pageContext.request.contextPath}/group/chat/${ug.groupId}" class="text-decoration-none flex-fill">
                                            <div class="group-icon me-3 d-inline-flex">
                                                <i class="bi bi-people"></i>
                                            </div>
                                            <div class="d-inline-block">
                                                <h4 class="mb-1" style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark);">
                                                    ${ug.groupName}
                                                    <c:if test="${ug.unreadCount > 0}">
                                                        <span class="badge bg-danger ms-2">${ug.unreadCount} 未读</span>
                                                    </c:if>
                                                </h4>
                                                <c:if test="${not empty ug.activityName}">
                                                    <small style="color: var(--text-muted);">${ug.activityName}</small>
                                                </c:if>
                                            </div>
                                        </a>
                                    </div>
                                    <div style="display: flex; justify-content: space-between; align-items: center; color: var(--text-secondary); margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border-light);">
                                        <span><i class="bi bi-people me-1"></i> ${ug.memberCount} 人</span>
                                        <span><i class="bi bi-clock me-1"></i> 
                                            <c:choose>
                                                <c:when test="${not empty ug.joinedAt}">${ug.joinedAt}</c:when>
                                                <c:otherwise>--</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div style="display: flex; gap: 8px;">
                                        <a href="${pageContext.request.contextPath}/group/chat/${ug.groupId}" class="btn-brand" style="flex: 1;">
                                            <i class="bi bi-chat-left-text"></i>进入群聊
                                        </a>
                                        <c:if test="${sessionScope.user.id == ug.ownerId}">
                                            <button type="button" class="btn-danger-design" onclick="confirmDisband(${ug.groupId}, '${ug.groupName}')">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
function confirmDisband(groupId, groupName) {
    if (confirm('确定要解散群聊"' + groupName + '"吗？此操作不可恢复！')) {
        window.location.href = '${pageContext.request.contextPath}/group?action=deleteGroup&groupId=' + groupId;
    }
}
</script>

<jsp:include page="../common/layout_bottom.jsp" />