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

        <div class="page-wrapper">
            <div class="page-header d-print-none">
                <div class="container-xl">
                    <div class="row g-2 align-items-center">
                        <div class="col">
                            <h2 class="page-title">
                                ${pageTitle}
                            </h2>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-body">
                <div class="container-xl">
                    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
                        <c:forEach var="n" items="${newsList}">
                            <div class="col">
                                <div class="card h-100" style="transition: transform 0.3s ease, box-shadow 0.3s ease; border: none; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
                                    <div class="card-body">
                                        <span class="badge bg-blue mb-2">${n.type}</span>
                                        <h3 class="card-title">${n.title}</h3>
                                        <p class="text-muted">${n.summary}</p>
                                    </div>
                                    <div class="card-footer bg-transparent">
                                        <div class="d-flex align-items-center">
                                            <div class="text-muted" style="font-size: 0.85rem;">
                                                <i class="bi bi-calendar3 me-1"></i> ${n.createdAt}
                                            </div>
                                            <div class="ms-auto">
                                                <a href="news?action=detail&id=${n.id}"
                                                    class="btn btn-outline-primary btn-sm">
                                                    阅读全文 <i class="bi bi-arrow-right ms-1"></i>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty newsList}">
                            <div class="col-12 text-center py-5">
                                <div class="empty">
                                    <div class="empty-icon">
                                        <i class="bi bi-info-circle h1"></i>
                                    </div>
                                    <p class="empty-title">暂无新闻</p>
                                    <p class="empty-subtitle text-muted">目前还没有发布任何新闻动态。</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../common/layout_bottom.jsp" />
