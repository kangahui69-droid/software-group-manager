<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
            <meta http-equiv="X-UA-Compatible" content="ie=edge" />
            <title>${param.title} - 软件小组管理系统</title>
            <!-- Tabler Core CSS -->
            <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
            <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler-vendors.min.css"
                rel="stylesheet" />
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');

                :root {
                    --tblr-font-sans-serif: 'Inter', -apple-system, BlinkMacSystemFont, San Francisco, Segoe UI, Roboto, Helvetica Neue, sans-serif;
                }

                body {
                    font-feature-settings: "cv03", "cv04", "cv11";
                }

                .badge.bg-blue {
                    color: #ffffff !important;
                }
            </style>
        </head>

        <body>
            <div class="page">
                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                    <jsp:include page="admin_sidebar.jsp" />
                </c:if>
                <!-- Sidebar or Top Navbar -->
                <header class="navbar navbar-expand-md navbar-light d-print-none">
                    <div class="container-xl">
                        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                            data-bs-target="#navbar-menu">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <c:if test="${sessionScope.user.role == 'ADMIN'}">
                            <a href="#" class="btn btn-link d-none d-md-inline-block me-3" data-bs-toggle="offcanvas"
                                data-bs-target="#adminSidebar">
                                <i class="bi bi-list fs-2 text-dark"></i>
                            </a>
                        </c:if>
                        <h1 class="navbar-brand navbar-brand-autodark d-none-xs me-md-3">
                            <a href="${pageContext.request.contextPath}/index.jsp">
                                <i class="bi bi-cpu text-primary me-2"></i>软件小组
                            </a>
                        </h1>
                        <div class="navbar-nav flex-row order-md-last">
                            <div class="nav-item">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.user}">
                                        <div class="nav-item dropdown">
                                            <a href="#" class="nav-link d-flex lh-1 text-reset p-0"
                                                data-bs-toggle="dropdown" aria-label="Open user menu">
                                                <span
                                                    class="avatar avatar-sm bg-blue-lt">${sessionScope.user.username.substring(0,1).toUpperCase()}</span>
                                                <div class="d-none d-xl-block ps-2">
                                                    <div>${sessionScope.user.username}</div>
                                                    <div class="mt-1 small text-muted text-capitalize">
                                                        ${sessionScope.user.role.toLowerCase()}</div>
                                                </div>
                                            </a>
                                            <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                                                <c:choose>
                                                    <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                                        <a href="${pageContext.request.contextPath}/admin/index.jsp"
                                                            class="dropdown-item">管理中心</a>
                                                        <a href="${pageContext.request.contextPath}/admin/profile.jsp"
                                                            class="dropdown-item">个人信息</a>
                                                        <a href="${pageContext.request.contextPath}/admin/password-change.jsp"
                                                            class="dropdown-item">修改密码</a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a href="${pageContext.request.contextPath}/member/index.jsp"
                                                            class="dropdown-item">个人中心</a>
                                                        <a href="${pageContext.request.contextPath}/member/profile.jsp"
                                                            class="dropdown-item">个人信息</a>
                                                        <a href="${pageContext.request.contextPath}/member/password-change.jsp"
                                                            class="dropdown-item">修改密码</a>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div class="dropdown-divider"></div>
                                                <a href="${pageContext.request.contextPath}/logout"
                                                    class="dropdown-item">退出登录</a>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/login.jsp"
                                            class="btn btn-primary d-none d-md-flex">登录</a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="collapse navbar-collapse" id="navbar-menu">
                            <div
                                class="d-flex flex-column flex-md-row flex-fill align-items-stretch align-items-md-center">
                                <ul class="navbar-nav">
                                    <li class="nav-item ${param.active == 'home' ? 'active' : ''}">
                                        <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">
                                            <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                    class="bi bi-house"></i></span>
                                            <span class="nav-link-title">首页</span>
                                        </a>
                                    </li>
                                    <c:choose>
                                        <c:when test="${empty sessionScope.user}">
                                            <li class="nav-item ${param.active == 'notice' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?type=notice">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-megaphone"></i></span>
                                                    <span class="nav-link-title">通知公告</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'award' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?type=award">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-trophy"></i></span>
                                                    <span class="nav-link-title">获奖新闻</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'activity' ? 'active' : ''}">
                                                <a class="nav-link"
                                                    href="${pageContext.request.contextPath}/news?type=activity">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-calendar-event"></i></span>
                                                    <span class="nav-link-title">活动新闻</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'recruit' ? 'active' : ''}">
                                                <a class="nav-link"
                                                    href="${pageContext.request.contextPath}/recruit/apply.jsp">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-person-plus"></i></span>
                                                    <span class="nav-link-title">招新报名</span>
                                                </a>
                                            </li>
                                        </c:when>
                                        <c:when test="${sessionScope.user.role != 'ADMIN'}">
                                            <li class="nav-item ${param.active == 'notice' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?type=notice">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-megaphone"></i></span>
                                                    <span class="nav-link-title">通知公告</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'award' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?type=award">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-trophy"></i></span>
                                                    <span class="nav-link-title">获奖新闻</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'activity' ? 'active' : ''}">
                                                <a class="nav-link"
                                                    href="${pageContext.request.contextPath}/news?type=activity">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-calendar-event"></i></span>
                                                    <span class="nav-link-title">活动新闻</span>
                                                </a>
                                            </li>
                                        </c:when>
                                        <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                            <li class="nav-item ${param.active == 'news' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?action=manage">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-newspaper"></i></span>
                                                    <span class="nav-link-title">新闻管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'activity' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/activity/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-calendar-check"></i></span>
                                                    <span class="nav-link-title">活动管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'project' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/project/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-kanban"></i></span>
                                                    <span class="nav-link-title">项目管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'award' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/award/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-trophy"></i></span>
                                                    <span class="nav-link-title">奖项管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'user' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/member/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-people"></i></span>
                                                    <span class="nav-link-title">用户管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'recruit' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/recruit/manage">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-person-plus-fill"></i></span>
                                                    <span class="nav-link-title">报名管理</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'log' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/log/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-journal-text"></i></span>
                                                    <span class="nav-link-title">日志管理</span>
                                                </a>
                                            </li>
                                        </c:when>
                                    </c:choose>
                                </ul>
                            </div>
                        </div>
                    </div>
                </header>
                <div class="page-wrapper">