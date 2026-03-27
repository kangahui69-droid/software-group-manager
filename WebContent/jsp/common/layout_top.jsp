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
                
                /* 按钮hover效果 */
                .btn-hover-lift {
                    transition: all 0.3s ease;
                }
                .btn-hover-lift:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }
                
                /* 空状态图标动画效果 */
                .empty-img i {
                    animation: pulse 2s infinite;
                }
                @keyframes pulse {
                    0%, 100% { opacity: 0.6; }
                    50% { opacity: 1; }
                }
                
                /* 面包屑导航样式优化 */
                .breadcrumb-item a {
                    color: #206bc4;
                    text-decoration: none;
                }
                .breadcrumb-item a:hover {
                    text-decoration: underline;
                }

                /* 全局 Loading 遮罩层 */
                #global-loading {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background-color: rgba(0, 0, 0, 0.5);
                    display: none;
                    justify-content: center;
                    align-items: center;
                    z-index: 9999;
                }

                #global-loading.active {
                    display: flex;
                }

                /* Loading 动画 */
                .loading-spinner {
                    width: 50px;
                    height: 50px;
                    border: 4px solid #f3f3f3;
                    border-top: 4px solid #3498db;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }

                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }

                .loading-text {
                    color: white;
                    margin-top: 15px;
                    font-size: 16px;
                    text-align: center;
                }

                /* 页面加载时的过渡效果 */
                .page-content {
                    opacity: 1;
                    transition: opacity 0.3s ease-in-out;
                }

                .page-content.loading {
                    opacity: 0.7;
                }
                
                /* ========== 消除导航栏与内容之间的空白 ========== */
                
                /* 移除 page 容器的内边距 */
                .page {
                    padding-top: 0 !important;
                }
                
                /* 确保 navbar 底部无空白 */
                header.navbar {
                    margin-bottom: 0 !important;
                    border-bottom: none !important;
                }
                
                /* 移除 navbar 容器可能存在的内边距 */
                .navbar .container-xl {
                    padding-top: 0 !important;
                    padding-bottom: 0 !important;
                }
                
                /* 移除 page-wrapper 的顶部边距和内边距 */
                .page-wrapper {
                    padding-top: 0 !important;
                    margin-top: 0 !important;
                }
                
                /* 移除 page-header 的顶部边距，使其紧贴导航栏 */
                .page-header {
                    margin-top: 0 !important;
                    padding-top: 0 !important;
                }
                
                /* 确保 page-header 容器无多余间距 */
                .page-header .container-xl {
                    padding-top: 0 !important;
                    padding-bottom: 0 !important;
                }
                
                /* 移除 page-title 的顶部边距 */
                .page-title {
                    margin-top: 0 !important;
                    padding-top: 0 !important;
                }
                
                /* 移除 row 的负边距可能造成的空白 */
                .page-header .row {
                    margin-top: 0 !important;
                    margin-bottom: 0 !important;
                }
                
                /* 移除 page-body 的顶部内边距 */
                .page-body {
                    padding-top: 0 !important;
                }
                
                /* 确保卡片容器紧贴上方 */
                .page-body .container-xl {
                    padding-top: 0 !important;
                }
                
                /* 移除 row-cards 可能存在的顶部边距 */
                .row-cards {
                    margin-top: 0 !important;
                }
            </style>
        </head>

        <body>
            <!-- 全局 Loading 组件 -->
            <div id="global-loading">
                <div class="text-center">
                    <div class="loading-spinner"></div>
                    <div class="loading-text">加载中...</div>
                </div>
            </div>

            <div class="page">
                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                    <jsp:include page="admin_sidebar.jsp" />
                </c:if>
                <!-- Sidebar or Top Navbar -->
                <header class="navbar navbar-expand-md navbar-light d-print-none" style="margin-bottom: 0 !important; border-bottom: none !important;">
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
                                                    class="avatar avatar-sm bg-blue-lt">${not empty sessionScope.user.name ? sessionScope.user.name.charAt(0) : sessionScope.user.username.substring(0,1)}</span>
                                                <div class="d-none d-xl-block ps-2">
                                                    <div>${not empty sessionScope.user.name ? sessionScope.user.name : sessionScope.user.username}</div>
                                                    <div class="mt-1 small text-muted text-capitalize">
                                                        ${sessionScope.user.role.toLowerCase()}</div>
                                                </div>
                                            </a>
                                            <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                                                <c:choose>
                                                    <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                                        <a href="${pageContext.request.contextPath}/admin/dashboard"
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
                                            <li class="nav-item ${param.active == 'ai' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/ai?action=chat">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-robot"></i></span>
                                                    <span class="nav-link-title">AI助手</span>
                                                </a>
                                            </li>
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
                                            <li class="nav-item ${param.active == 'problem' ? 'active' : ''}">
                                                <a class="nav-link"
                                                    href="${pageContext.request.contextPath}/problem-report.jsp">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-exclamation-triangle"></i></span>
                                                    <span class="nav-link-title">问题反馈</span>
                                                </a>
                                            </li>
                                        </c:when>
                                        <c:when test="${sessionScope.user.role != 'ADMIN'}">
                                            <li class="nav-item ${param.active == 'ai' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/ai?action=chat">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-robot"></i></span>
                                                    <span class="nav-link-title">AI助手</span>
                                                </a>
                                            </li>
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
                                            <li class="nav-item ${param.active == 'study' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/study">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-book"></i></span>
                                                    <span class="nav-link-title">学习中心</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'problem' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/member/problem">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-exclamation-triangle"></i></span>
                                                    <span class="nav-link-title">问题反馈</span>
                                                </a>
                                            </li>
                                        </c:when>
                                        <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                            <li class="nav-item dropdown ${param.active == 'ai' || param.active == 'ai_stats' ? 'active' : ''}">
                                                <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-robot"></i></span>
                                                    <span class="nav-link-title">AI助手</span>
                                                </a>
                                                <div class="dropdown-menu">
                                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/ai?action=chat">
                                                        <i class="bi bi-chat-dots me-1"></i>AI对话
                                                    </a>
                                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/ai?action=statistics">
                                                        <i class="bi bi-graph-up me-1"></i>AI统计
                                                    </a>
                                                </div>
                                            </li>
                                            <li class="nav-item ${param.active == 'news' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/news?action=manage">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-newspaper"></i></span>
                                                    <span class="nav-link-title">新闻</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'activity' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/activity/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-calendar-check"></i></span>
                                                    <span class="nav-link-title">活动</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'project' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/project/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-kanban"></i></span>
                                                    <span class="nav-link-title">项目</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'award' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/award/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-trophy"></i></span>
                                                    <span class="nav-link-title">奖项</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'user' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/member/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-people"></i></span>
                                                    <span class="nav-link-title">用户</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'recruit' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/recruit/manage">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-person-plus-fill"></i></span>
                                                    <span class="nav-link-title">报名</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'log' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/log/list">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-journal-text"></i></span>
                                                    <span class="nav-link-title">日志</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'attendance' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/attendance/manage">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-clock-history"></i></span>
                                                    <span class="nav-link-title">考勤</span>
                                                </a>
                                            </li>
                                            <li class="nav-item ${param.active == 'problem' ? 'active' : ''}">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/admin/problem">
                                                    <span class="nav-link-icon d-md-none d-lg-inline-block"><i
                                                            class="bi bi-gear"></i></span>
                                                    <span class="nav-link-title">问题管理</span>
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