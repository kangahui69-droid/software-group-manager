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

<style>
    .news-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
        gap: 24px;
    }
    
    .news-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        transition: all 0.3s ease;
        overflow: hidden;
        height: 100%;
        display: flex;
        flex-direction: column;
    }
    
    .news-card:hover {
        transform: translateY(-6px);
        box-shadow: var(--shadow-brand-offset);
    }
    
    .news-card-body {
        padding: 24px;
        flex: 1;
        display: flex;
        flex-direction: column;
    }
    
    .news-card-badge {
        display: inline-flex;
        align-items: center;
        background: var(--brand-blue);
        color: white;
        padding: 6px 14px;
        border-radius: var(--radius-pill);
        font-size: 0.75rem;
        font-weight: 500;
        margin-bottom: 16px;
        align-self: flex-start;
    }
    
    .news-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        line-height: 1.4;
        margin-bottom: 12px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    
    .news-card-text {
        color: var(--text-secondary);
        font-size: 0.94rem;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
        flex: 1;
    }
    
    .news-card-footer {
        padding: 16px 24px;
        border-top: 1px solid var(--border-light);
        background: var(--bg-white);
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .news-card-date {
        color: var(--text-muted);
        font-size: 0.81rem;
        display: flex;
        align-items: center;
        gap: 6px;
    }
    
    .news-card-link {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        text-decoration: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    
    .news-card-link:hover {
        background: var(--primary-600);
        color: white;
        transform: translateX(4px);
    }
    
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .empty-state {
        text-align: center;
        padding: 80px 24px;
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-standard);
    }
    
    .empty-state-icon {
        font-size: 64px;
        color: var(--text-muted);
        margin-bottom: 24px;
    }
    
    .empty-state-title {
        font-family: var(--font-display);
        font-size: 1.5rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }
    
    .empty-state-subtitle {
        color: var(--text-muted);
        font-size: 0.94rem;
    }
    
    .more-link {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        padding: 10px 20px;
        font-size: 0.88rem;
        font-weight: 500;
        text-decoration: none;
        transition: all 0.3s ease;
        border: none;
    }
    
    .more-link:hover {
        background: var(--brand-blue);
        color: white;
    }
</style>

<div class="page-wrapper">
    <div class="page-header d-print-none">
        <div class="container-xl">
            <div class="row g-2 align-items-center">
                <div class="col">
                    <h2 class="page-title">${pageTitle}</h2>
                    <c:if test="${activeNav == 'activity'}">
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="more-link mt-3">
                            更多活动详情 <i class="bi bi-arrow-right"></i>
                        </a>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <div class="page-body">
        <div class="container-xl">
            <c:choose>
                <c:when test="${not empty newsList}">
                    <div class="news-grid">
                        <c:forEach var="n" items="${newsList}">
                            <div class="news-card">
                                <div class="news-card-body">
                                    <span class="news-card-badge">${n.type}</span>
                                    <h3 class="news-card-title">${n.title}</h3>
                                    <p class="news-card-text">${n.summary}</p>
                                </div>
                                <div class="news-card-footer">
                                    <div class="news-card-date">
                                        <i class="bi bi-calendar3"></i> ${n.createdAt}
                                    </div>
                                    <a href="news?action=detail&id=${n.id}" class="news-card-link">
                                        阅读全文 <i class="bi bi-arrow-right"></i>
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-newspaper"></i>
                        </div>
                        <p class="empty-state-title">暂无新闻</p>
                        <p class="empty-state-subtitle">目前还没有发布任何新闻动态。</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />