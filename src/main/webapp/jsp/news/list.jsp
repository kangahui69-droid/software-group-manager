<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 根据新闻类型设置导航激活状态和页面标题 --%>
<%
    String newsType = request.getParameter("type");
    String activeNav = "notice";
    String pageTitle = "通知公告";
    if ("award".equals(newsType)) {
        activeNav = "award";
        pageTitle = "获奖新闻";
    } else if ("activity".equals(newsType)) {
        activeNav = "activity";
        pageTitle = "活动新闻";
    }
    request.setAttribute("activeNav", activeNav);
    request.setAttribute("pageTitle", pageTitle);
%>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="${pageTitle}" />
    <jsp:param name="active" value="${activeNav}" />
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
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-newspaper me-2"></i>${pageTitle}
            </h1>
            <p class="member-hero-subtitle">查看最新的新闻动态</p>
        </div>

        <c:choose>
            <c:when test="${not empty newsList}">
                <div class="row g-4">
                    <c:forEach var="n" items="${newsList}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card-design" style="height: 100%;">
                                <div class="card-body-design" style="padding: 24px; flex: 1; display: flex; flex-direction: column;">
                                    <div style="margin-bottom: 12px;">
                                        <span class="badge-design" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">${n.type}</span>
                                    </div>
                                    <h3 style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); line-height: 1.4; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">${n.title}</h3>
                                    <p style="color: var(--text-secondary); font-size: 0.94rem; line-height: 1.6; flex: 1; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 16px;">${n.summary}</p>
                                    <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 16px; border-top: 1px solid var(--border-light);">
                                        <span style="color: var(--text-muted); font-size: 0.81rem; display: flex; align-items: center; gap: 6px;">
                                            <i class="bi bi-calendar3"></i> ${n.createdAt}
                                        </span>
                                        <a href="${pageContext.request.contextPath}/news?action=detail&id=${n.id}" class="btn-sm-brand">
                                            阅读全文 <i class="bi bi-arrow-right"></i>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-design">
                    <div class="card-body-design" style="padding: 48px; text-align: center;">
                        <div style="font-size: 64px; color: var(--text-muted); margin-bottom: 16px;">
                            <i class="bi bi-newspaper"></i>
                        </div>
                        <h3 style="font-family: var(--font-display); font-size: 1.5rem; font-weight: 600; color: var(--text-dark); margin-bottom: 8px;">暂无新闻</h3>
                        <p style="color: var(--text-muted); font-size: 0.94rem;">目前还没有发布任何新闻动态。</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />