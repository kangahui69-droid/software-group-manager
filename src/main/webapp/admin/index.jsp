<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%@ page import="dao.AwardDAO" %>
<%@ page import="dao.ActivityDAO" %>
<%@ page import="dao.RecruitApplicationDAO" %>
<%@ page import="dao.ProjectDAO" %>
<%@ page import="dao.NewsDAO" %>
<%@ page import="dao.UserDAO" %>
<%@ page import="dao.OperationLogDAO" %>
<%@ page import="dao.ProblemReportDAO" %>
<%@ page import="dao.AttendanceDAO" %>
<%@ page import="dao.GroupMemberDAO" %>
<%
    String contextPath = request.getContextPath();
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    
    // 获取各项统计数据
    AwardDAO awardDAO = new AwardDAO();
    ActivityDAO activityDAO = new ActivityDAO();
    RecruitApplicationDAO recruitDAO = new RecruitApplicationDAO();
    ProjectDAO projectDAO = new ProjectDAO();
    NewsDAO newsDAO = new NewsDAO();
    UserDAO userDAO = new UserDAO();
    OperationLogDAO logDAO = new OperationLogDAO();
    ProblemReportDAO problemDAO = new ProblemReportDAO();
    AttendanceDAO attendanceDAO = new AttendanceDAO();
    GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    
    int pendingAwards = 0;
    int pendingActivities = 0;
    int pendingRecruits = 0;
    int pendingProblems = 0;
    int totalPending = 0;
    
    try {
        pendingAwards = awardDAO.countPending();
    } catch (Exception e) {}
    try {
        pendingActivities = activityDAO.countPendingReview();
    } catch (Exception e) {}
    try {
        pendingRecruits = recruitDAO.countPending();
    } catch (Exception e) {}
    try {
        pendingProblems = problemDAO.countPending();
    } catch (Exception e) {}
    totalPending = pendingAwards + pendingActivities + pendingRecruits + pendingProblems;
    
    int newsCount = 0;
    int projectCount = 0;
    int userCount = 0;
    int logCount = 0;
    int totalGroups = 0;
    
    try { newsCount = newsDAO.count(); } catch (Exception e) {}
    try { projectCount = projectDAO.count(); } catch (Exception e) {}
    try { userCount = userDAO.count(); } catch (Exception e) {}
    try { logCount = logDAO.count(); } catch (Exception e) {}
    try { pendingProblems = problemDAO.countPending(); } catch (Exception e) {}
    try { totalGroups = groupMemberDAO.countGroups(); } catch (Exception e) {}
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="管理中心" />
</jsp:include>

<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .admin-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        padding: 24px;
        transition: all 0.3s ease;
        height: 100%;
        text-decoration: none;
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .admin-card:hover {
        transform: translateY(-6px);
        box-shadow: var(--shadow-brand-offset);
    }

    .admin-card-icon {
        width: 64px;
        height: 64px;
        border-radius: var(--radius-comfortable);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.75rem;
        margin-bottom: 16px;
    }

    .admin-card-title {
        font-family: var(--font-display);
        font-size: 1.1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .admin-card-count {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
    }

    .admin-card-badge {
        position: absolute;
        top: 12px;
        right: 12px;
        background: #ef4444;
        color: white;
        font-size: 0.75rem;
        font-weight: 600;
        padding: 2px 8px;
        border-radius: var(--radius-pill);
    }

    .pending-banner {
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        border: 1px solid #fcd34d;
        border-radius: var(--radius-generous);
        padding: 20px 24px;
        margin-bottom: 32px;
        display: flex;
        align-items: center;
        gap: 20px;
    }

    .pending-banner-icon {
        width: 56px;
        height: 56px;
        background: rgba(217, 119, 6, 0.2);
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        color: #d97706;
        flex-shrink: 0;
    }

    .pending-banner-content {
        flex: 1;
    }

    .pending-banner-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: #92400e;
        margin-bottom: 4px;
    }

    .pending-banner-badges {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    .pending-badge {
        font-size: 0.75rem;
        padding: 2px 10px;
        border-radius: var(--radius-pill);
        background: rgba(217, 119, 6, 0.2);
        color: #92400e;
    }

    .pending-banner-btn {
        background: #d97706;
        color: white;
        border: none;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        font-size: 0.875rem;
        text-decoration: none;
        transition: all 0.3s ease;
        white-space: nowrap;
    }

    .pending-banner-btn:hover {
        background: #b45309;
        color: white;
    }

    .section-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    @media (max-width: 768px) {
        .admin-hero {
            padding: 24px;
        }
        .pending-banner {
            flex-direction: column;
            text-align: center;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-gear me-2"></i>管理中心
            </h1>
            <p class="admin-hero-subtitle">欢迎回来，<%= user.getName() != null ? user.getName() : user.getUsername() %>！在这里管理小组的各个方面</p>
        </div>

        <% if (totalPending > 0) { %>
        <div class="pending-banner">
            <div class="pending-banner-icon">
                <i class="bi bi-clock-history"></i>
            </div>
            <div class="pending-banner-content">
                <div class="pending-banner-title"><%= totalPending %> 项待审核</div>
                <div class="pending-banner-badges">
                    <% if (pendingAwards > 0) { %><span class="pending-badge"><%= pendingAwards %> 奖项</span><% } %>
                    <% if (pendingActivities > 0) { %><span class="pending-badge"><%= pendingActivities %> 活动</span><% } %>
                    <% if (pendingRecruits > 0) { %><span class="pending-badge"><%= pendingRecruits %> 招新</span><% } %>
                    <% if (pendingProblems > 0) { %><span class="pending-badge"><%= pendingProblems %> 问题</span><% } %>
                </div>
            </div>
            <% if (pendingRecruits > 0) { %>
                <a href="<%= contextPath %>/admin/recruit/manage" class="pending-banner-btn">去审核</a>
            <% } else if (pendingActivities > 0) { %>
                <a href="<%= contextPath %>/activity/list" class="pending-banner-btn">去审核</a>
            <% } else if (pendingAwards > 0) { %>
                <a href="<%= contextPath %>/award?action=approveList&status=PENDING" class="pending-banner-btn">去审核</a>
            <% } else if (pendingProblems > 0) { %>
                <a href="<%= contextPath %>/admin/problem" class="pending-banner-btn">去处理</a>
            <% } %>
        </div>
        <% } %>

        <h3 class="section-title">
            <i class="bi bi-collection text-primary"></i>内容管理
        </h3>
        <div class="row g-4 mb-5">
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/news?action=manage" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                        <i class="bi bi-newspaper"></i>
                    </div>
                    <div class="admin-card-title">新闻管理</div>
                    <div class="admin-card-count">发布和编辑新闻</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/activity?action=manage" class="admin-card" style="position: relative;">
                    <% if (pendingActivities > 0) { %>
                    <span class="admin-card-badge"><%= pendingActivities %></span>
                    <% } %>
                    <div class="admin-card-icon" style="background: rgba(236, 72, 153, 0.1); color: #ec4899;">
                        <i class="bi bi-calendar-check"></i>
                    </div>
                    <div class="admin-card-title">活动管理</div>
                    <div class="admin-card-count">创建和管理活动</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/project/list" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                        <i class="bi bi-kanban"></i>
                    </div>
                    <div class="admin-card-title">项目管理</div>
                    <div class="admin-card-count">管理项目进度</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/award/list" class="admin-card" style="position: relative;">
                    <% if (pendingAwards > 0) { %>
                    <span class="admin-card-badge"><%= pendingAwards %></span>
                    <% } %>
                    <div class="admin-card-icon" style="background: rgba(249, 115, 22, 0.1); color: #f97316;">
                        <i class="bi bi-trophy"></i>
                    </div>
                    <div class="admin-card-title">奖项管理</div>
                    <div class="admin-card-count">审核奖项申请</div>
                </a>
            </div>
        </div>

        <h3 class="section-title">
            <i class="bi bi-people text-primary"></i>成员管理
        </h3>
        <div class="row g-4 mb-5">
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/admin/member/list" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                        <i class="bi bi-people"></i>
                    </div>
                    <div class="admin-card-title">用户管理</div>
                    <div class="admin-card-count">管理小组成员</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/admin/recruit/manage" class="admin-card" style="position: relative;">
                    <% if (pendingRecruits > 0) { %>
                    <span class="admin-card-badge"><%= pendingRecruits %></span>
                    <% } %>
                    <div class="admin-card-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                        <i class="bi bi-person-plus-fill"></i>
                    </div>
                    <div class="admin-card-title">报名管理</div>
                    <div class="admin-card-count">处理招新申请</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/attendance/manage" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">
                        <i class="bi bi-book"></i>
                    </div>
                    <div class="admin-card-title">学习管理</div>
                    <div class="admin-card-count">查看学习记录</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/admin/problem" class="admin-card" style="position: relative;">
                    <% if (pendingProblems > 0) { %>
                    <span class="admin-card-badge"><%= pendingProblems %></span>
                    <% } %>
                    <div class="admin-card-icon" style="background: rgba(168, 85, 247, 0.1); color: #a855f7;">
                        <i class="bi bi-exclamation-triangle"></i>
                    </div>
                    <div class="admin-card-title">问题管理</div>
                    <div class="admin-card-count">处理问题反馈</div>
                </a>
            </div>
        </div>

        <h3 class="section-title">
            <i class="bi bi-chat-dots text-primary"></i>群聊管理
        </h3>
        <div class="row g-4 mb-5">
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/group/admin?action=list" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(20, 184, 166, 0.1); color: #14b8a6;">
                        <i class="bi bi-chat-dots-fill"></i>
                    </div>
                    <div class="admin-card-title">群聊管理</div>
                    <div class="admin-card-count">查看所有群聊</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/group/admin?action=myGroups" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                        <i class="bi bi-people"></i>
                    </div>
                    <div class="admin-card-title">我的群聊</div>
                    <div class="admin-card-count">管理我创建的群聊</div>
                </a>
            </div>
        </div>

        <h3 class="section-title">
            <i class="bi bi-bar-chart text-primary"></i>系统统计
        </h3>
        <div class="row g-4">
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/admin/log/list" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">
                        <i class="bi bi-journal-text"></i>
                    </div>
                    <div class="admin-card-title">操作日志</div>
                    <div class="admin-card-count"><%= logCount %> 条记录</div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xl-3">
                <a href="<%= contextPath %>/ai?action=statistics" class="admin-card">
                    <div class="admin-card-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                        <i class="bi bi-robot"></i>
                    </div>
                    <div class="admin-card-title">AI统计</div>
                    <div class="admin-card-count">查看AI使用情况</div>
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />