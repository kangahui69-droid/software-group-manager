<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <jsp:include page="jsp/common/layout_top.jsp">
        <jsp:param name="title" value="欢迎" />
        <jsp:param name="active" value="home" />
    </jsp:include>

    <div class="page-header d-print-none">
        <div class="container-xl">
            <div class="row g-2 align-items-center">
                <div class="col">
                    <h2 class="page-title">
                        最新动态
                    </h2>
                </div>
            </div>
        </div>
    </div>

    <div class="page-body">
        <div class="container-xl">
            <div class="row row-cards">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-status-top bg-blue"></div>
                        <div class="list-group list-group-flush">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="avatar avatar-md bg-blue-lt"><i
                                                class="bi bi-info-circle"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium">
                                            欢迎使用软件小组管理系统
                                        </div>
                                        <div class="text-muted">
                                            系统已启动，请登录使用更多功能。支持新闻发布、项目管理、活动报名等功能。
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <small class="text-muted">刚刚</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="card-footer text-center">
                            <a href="${pageContext.request.contextPath}/news/list" class="btn btn-link">查看更多动态</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">快速链接</h3>
                        </div>
                        <div class="list-group list-group-flush">
                            <a href="news?type=notice"
                                class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-megaphone me-3 text-red"></i> 通知公告
                            </a>
                            <a href="news?type=award"
                                class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-trophy me-3 text-yellow"></i> 获奖新闻
                            </a>
                            <a href="news?type=activity"
                                class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-calendar-event me-3 text-green"></i> 活动新闻
                            </a>
                            <c:if test="${empty sessionScope.user}">
                                <a href="recruit/apply.jsp"
                                    class="list-group-item list-group-item-action d-flex align-items-center border-bottom-0">
                                    <i class="bi bi-person-plus me-3 text-blue"></i> 招新报名
                                </a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="jsp/common/layout_bottom.jsp" />