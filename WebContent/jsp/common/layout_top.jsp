<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>${param.title} - 软件小组管理系统</title>
    <link rel="icon" href="${pageContext.request.contextPath}/images/IMG_20260419_175314.jpg" type="image/jpeg">
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler-vendors.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
    <style>
        .navbar-brand-design {
            font-family: var(--font-display);
            font-weight: 600;
            font-size: 1.25rem;
            color: var(--text-dark);
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .navbar-brand-design:hover {
            color: var(--brand-blue);
        }

        .brand-icon {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-standard);
            object-fit: cover;
        }

        .nav-menu-item {
            font-family: var(--font-ui);
            font-size: 0.88rem;
            font-weight: 500;
            color: var(--text-secondary);
            padding: 10px 16px;
            border-radius: var(--radius-pill);
            text-decoration: none;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .nav-menu-item:hover {
            background: rgba(59, 130, 246, 0.1);
            color: var(--brand-blue);
            box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
        }

        .nav-menu-item.active {
            background: var(--brand-blue);
            color: white;
        }

        .nav-menu-item i {
            font-size: 1rem;
        }

        .user-dropdown .dropdown-toggle {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 6px 12px;
            border-radius: var(--radius-comfortable);
            transition: all 0.3s ease;
            text-decoration: none;
        }

        .user-dropdown .dropdown-toggle:hover {
            background: rgba(0, 0, 0, 0.04);
        }

        .user-dropdown .dropdown-toggle::after {
            display: none;
        }

        .user-avatar {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-comfortable);
            object-fit: cover;
        }

        .user-avatar-fallback {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-comfortable);
            background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 0.875rem;
        }

        .user-name {
            font-family: var(--font-ui);
            font-weight: 500;
            color: var(--text-dark);
            font-size: 0.875rem;
        }

        .user-role {
            font-size: 0.75rem;
            color: var(--text-muted);
            text-transform: capitalize;
        }

        .dropdown-menu-design {
            border: none;
            border-radius: var(--radius-comfortable);
            box-shadow: var(--shadow-card-elevated);
            padding: 8px;
            min-width: 180px;
        }

        .dropdown-menu-design .dropdown-item {
            font-family: var(--font-ui);
            font-size: 0.875rem;
            color: var(--text-dark);
            padding: 10px 14px;
            border-radius: var(--radius-standard);
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .dropdown-menu-design .dropdown-item:hover {
            background: rgba(20, 86, 240, 0.08);
            color: var(--brand-blue);
        }

        .dropdown-menu-design .dropdown-divider {
            margin: 8px 0;
            border-color: var(--border-light);
        }

        .btn-login {
            background: var(--brand-blue);
            color: white;
            border-radius: var(--radius-pill);
            padding: 10px 24px;
            font-family: var(--font-ui);
            font-weight: 600;
            font-size: 0.88rem;
            border: none;
            text-decoration: none;
            transition: all 0.3s ease;
        }

        .btn-login:hover {
            background: var(--primary-600);
            color: white;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(20, 86, 240, 0.3);
        }

        .page-header-design {
            padding: 32px 0;
            border-bottom: 1px solid var(--border-light);
            margin-bottom: 32px;
        }

        .page-title-design {
            font-family: var(--font-display);
            font-size: 1.94rem;
            font-weight: 600;
            color: var(--text-dark);
            margin: 0;
        }

        .breadcrumb-design {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.875rem;
            color: var(--text-muted);
        }

        .breadcrumb-design a {
            color: var(--brand-blue);
            text-decoration: none;
        }

        .breadcrumb-design a:hover {
            text-decoration: underline;
        }

        .breadcrumb-design i {
            font-size: 0.75rem;
        }

        .offcanvas-sidebar {
            width: 320px;
            border: none;
            border-radius: 0 var(--radius-generous) var(--radius-generous) 0;
        }

        .offcanvas-sidebar .offcanvas-header {
            padding: 20px 24px;
            border-bottom: 1px solid var(--border-light);
        }

        .offcanvas-sidebar .offcanvas-title {
            font-family: var(--font-display);
            font-weight: 600;
            color: var(--text-dark);
        }

        .sidebar-title {
            font-family: var(--font-display);
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-dark);
            padding: 16px 20px;
            border-bottom: 1px solid var(--border-light);
        }

        .sidebar-shortcuts {
            padding: 20px;
        }

        .sidebar-card {
            background: var(--bg-white);
            border-radius: var(--radius-comfortable);
            padding: 16px;
            text-decoration: none;
            transition: all 0.3s ease;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 12px;
            box-shadow: var(--shadow-standard);
            border: 1px solid var(--border-light);
        }

        .sidebar-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-brand-purple);
            border-color: var(--brand-blue);
        }

        .sidebar-card-icon {
            width: 48px;
            height: 48px;
            border-radius: var(--radius-standard);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }

        .sidebar-card-title {
            font-family: var(--font-ui);
            font-weight: 500;
            font-size: 0.875rem;
            color: var(--text-dark);
            text-align: center;
        }

        .sidebar-faq {
            padding: 0 20px 20px;
        }

        .accordion-design .accordion-item {
            border: 1px solid var(--border-light);
            border-radius: var(--radius-comfortable);
            margin-bottom: 8px;
            overflow: hidden;
        }

        .accordion-design .accordion-button {
            font-family: var(--font-ui);
            font-weight: 500;
            color: var(--text-dark);
            background: var(--bg-white);
            padding: 14px 16px;
            border-radius: var(--radius-comfortable);
        }

        .accordion-design .accordion-button:not(.collapsed) {
            background: rgba(20, 86, 240, 0.05);
            color: var(--brand-blue);
        }

        .accordion-design .accordion-button::after {
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%231458f0'%3e%3cpath fill-rule='evenodd' d='M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z'/%3e%3c/svg%3e");
        }

        .accordion-design .accordion-body {
            padding: 16px;
            font-size: 0.875rem;
            color: var(--text-secondary);
        }

        .btn-back-home {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            width: 100%;
            padding: 12px 20px;
            background: var(--bg-white);
            border: 1px solid var(--brand-blue);
            color: var(--brand-blue);
            border-radius: var(--radius-standard);
            font-family: var(--font-ui);
            font-weight: 500;
            font-size: 0.875rem;
            text-decoration: none;
            transition: all 0.3s ease;
        }

        .btn-back-home:hover {
            background: var(--brand-blue);
            color: white;
        }
    </style>
</head>

<body>
    <div class="page">
        <c:if test="${sessionScope.user.role == 'ADMIN'}">
            <jsp:include page="admin_sidebar.jsp" />
        </c:if>

        <header class="navbar navbar-expand-md navbar-light d-print-none" style="background: var(--bg-white); border-bottom: 1px solid var(--border-light);">
            <div class="container-xl">
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbar-menu" style="border: none; padding: 8px;">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                    <a href="#" class="btn btn-link d-none d-md-inline-block me-3" data-bs-toggle="offcanvas" data-bs-target="#adminSidebar" style="color: var(--text-secondary); text-decoration: none;">
                        <i class="bi bi-list fs-4"></i>
                    </a>
                </c:if>

                <h1 class="navbar-brand navbar-brand-autodark d-none-xs me-4">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="navbar-brand-design">
                        <img src="${pageContext.request.contextPath}/images/IMG_20260419_175314.jpg" alt="Logo" class="brand-icon">
                        <span class="logo-text">软件小组</span>
                    </a>
                </h1>

                <div class="navbar-nav flex-row order-md-last">
                    <div class="nav-item">
                        <c:choose>
                            <c:when test="${not empty sessionScope.user}">
                                <div class="user-dropdown dropdown">
                                    <a href="#" class="dropdown-toggle" data-bs-toggle="dropdown" aria-label="Open user menu">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.memberProfile and not empty sessionScope.memberProfile.avatarFileId}">
                                                <img src="${pageContext.request.contextPath}/file?action=view&id=${sessionScope.memberProfile.avatarFileId}" 
                                                     alt="用户头像" class="user-avatar">
                                            </c:when>
                                            <c:otherwise>
                                                <span class="user-avatar-fallback">
                                                    ${not empty sessionScope.user.name ? sessionScope.user.name.charAt(0) : sessionScope.user.username.substring(0,1)}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="d-none d-xl-block">
                                            <div class="user-name">${not empty sessionScope.user.name ? sessionScope.user.name : sessionScope.user.username}</div>
                                            <div class="user-role">${sessionScope.user.role.toLowerCase()}</div>
                                        </div>
                                    </a>
                                    <div class="dropdown-menu dropdown-menu-end dropdown-menu-design">
                                        <c:choose>
                                            <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                                <a href="${pageContext.request.contextPath}/admin/index.jsp" class="dropdown-item">
                                                    <i class="bi bi-grid-1x2"></i>管理中心
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/profile.jsp" class="dropdown-item">
                                                    <i class="bi bi-person"></i>个人信息
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/password-change.jsp" class="dropdown-item">
                                                    <i class="bi bi-lock"></i>修改密码
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/member/index.jsp" class="dropdown-item">
                                                    <i class="bi bi-person"></i>个人中心
                                                </a>
                                                <a href="${pageContext.request.contextPath}/member/profile.jsp" class="dropdown-item">
                                                    <i class="bi bi-person-vcard"></i>个人信息
                                                </a>
                                                <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="dropdown-item">
                                                    <i class="bi bi-lock"></i>修改密码
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="dropdown-divider"></div>
                                        <a href="${pageContext.request.contextPath}/logout" class="dropdown-item" style="color: var(--brand-blue);">
                                            <i class="bi bi-box-arrow-right"></i>退出登录
                                        </a>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/login.jsp" class="btn-login d-none d-md-flex">
                                    登录
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="collapse navbar-collapse" id="navbar-menu">
                    <div class="d-flex flex-column flex-md-row flex-fill align-items-stretch align-items-md-center">
                        <ul class="navbar-nav" style="gap: 4px;">
                            <li class="nav-item">
                                <a class="nav-menu-item ${param.active == 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/index.jsp">
                                    <i class="bi bi-house"></i>
                                    <span>首页</span>
                                </a>
                            </li>
                            <c:choose>
                                <c:when test="${empty sessionScope.user}">
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'ai' ? 'active' : ''}" href="${pageContext.request.contextPath}/ai?action=chat">
                                            <i class="bi bi-robot"></i>
                                            <span>AI助手</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'notice' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=notice">
                                            <i class="bi bi-megaphone"></i>
                                            <span>通知公告</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'award' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=award">
                                            <i class="bi bi-trophy"></i>
                                            <span>获奖新闻</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'activity' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=activity">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>活动新闻</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'recruit' ? 'active' : ''}" href="${pageContext.request.contextPath}/recruit/apply.jsp">
                                            <i class="bi bi-person-plus"></i>
                                            <span>招新报名</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'problem' ? 'active' : ''}" href="${pageContext.request.contextPath}/problem-report.jsp">
                                            <i class="bi bi-exclamation-triangle"></i>
                                            <span>问题反馈</span>
                                        </a>
                                    </li>
                                </c:when>
                                <c:when test="${sessionScope.user.role != 'ADMIN'}">
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'ai' ? 'active' : ''}" href="${pageContext.request.contextPath}/ai?action=chat">
                                            <i class="bi bi-robot"></i>
                                            <span>AI助手</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'notice' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=notice">
                                            <i class="bi bi-megaphone"></i>
                                            <span>通知公告</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'award' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=award">
                                            <i class="bi bi-trophy"></i>
                                            <span>获奖新闻</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'activity' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?type=activity">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>活动新闻</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'study' ? 'active' : ''}" href="${pageContext.request.contextPath}/study">
                                            <i class="bi bi-book"></i>
                                            <span>学习中心</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'problem' ? 'active' : ''}" href="${pageContext.request.contextPath}/member/problem">
                                            <i class="bi bi-exclamation-triangle"></i>
                                            <span>问题反馈</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'myActivities' ? 'active' : ''}" href="${pageContext.request.contextPath}/activity?action=myCreatedActivities">
                                            <i class="bi bi-calendar-plus"></i>
                                            <span>我发起的</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'group' ? 'active' : ''}" href="${pageContext.request.contextPath}/group/my-groups">
                                            <i class="bi bi-chat-dots"></i>
                                            <span>我的群聊</span>
                                        </a>
                                    </li>
                                </c:when>
                                <c:when test="${sessionScope.user.role == 'ADMIN'}">
                                    <li class="nav-item dropdown">
                                        <a class="nav-menu-item dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                            <i class="bi bi-robot"></i>
                                            <span>AI助手</span>
                                        </a>
                                        <div class="dropdown-menu dropdown-menu-design" style="margin-top: 8px;">
                                            <a class="dropdown-item" href="${pageContext.request.contextPath}/ai?action=chat">
                                                <i class="bi bi-chat-dots"></i>AI对话
                                            </a>
                                            <a class="dropdown-item" href="${pageContext.request.contextPath}/ai?action=statistics">
                                                <i class="bi bi-graph-up"></i>AI统计
                                            </a>
                                        </div>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'news' ? 'active' : ''}" href="${pageContext.request.contextPath}/news?action=manage">
                                            <i class="bi bi-newspaper"></i>
                                            <span>新闻</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'activity' ? 'active' : ''}" href="${pageContext.request.contextPath}/activity?action=manage">
                                            <i class="bi bi-calendar-check"></i>
                                            <span>活动</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'project' ? 'active' : ''}" href="${pageContext.request.contextPath}/project/list">
                                            <i class="bi bi-kanban"></i>
                                            <span>项目</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'award' ? 'active' : ''}" href="${pageContext.request.contextPath}/award/list">
                                            <i class="bi bi-trophy"></i>
                                            <span>奖项</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'user' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/member/list">
                                            <i class="bi bi-people"></i>
                                            <span>用户</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'recruit' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/recruit/manage">
                                            <i class="bi bi-person-plus-fill"></i>
                                            <span>报名</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'log' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/log/list">
                                            <i class="bi bi-journal-text"></i>
                                            <span>日志</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'attendance' ? 'active' : ''}" href="${pageContext.request.contextPath}/attendance/manage">
                                            <i class="bi bi-book"></i>
                                            <span>学习管理</span>
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-menu-item ${param.active == 'problem' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/problem">
                                            <i class="bi bi-gear"></i>
                                            <span>问题管理</span>
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