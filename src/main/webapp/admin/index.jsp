<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%
    String contextPath = request.getContextPath();
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    Integer newsCount = (Integer) request.getAttribute("newsCount");
    Integer projectCount = (Integer) request.getAttribute("projectCount");
    Integer userCount = (Integer) request.getAttribute("userCount");
    Integer logCount = (Integer) request.getAttribute("logCount");
    Integer pendingAwards = (Integer) request.getAttribute("pendingAwards");
    Integer pendingRecruits = (Integer) request.getAttribute("pendingRecruits");
    Integer pendingActivities = (Integer) request.getAttribute("pendingActivities");
    Integer totalPending = (Integer) request.getAttribute("totalPending");
    if (newsCount == null) newsCount = 0;
    if (projectCount == null) projectCount = 0;
    if (userCount == null) userCount = 0;
    if (logCount == null) logCount = 0;
    if (pendingAwards == null) pendingAwards = 0;
    if (pendingRecruits == null) pendingRecruits = 0;
    if (pendingActivities == null) pendingActivities = 0;
    if (totalPending == null) totalPending = 0;
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

    .stat-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        transition: all 0.3s ease;
        height: 100%;
        text-decoration: none;
        display: block;
    }

    .stat-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-purple);
        border-color: transparent;
    }

    .stat-card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
    }

    .stat-card-title {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-secondary);
    }

    .stat-card-badge {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 600;
        padding: 4px 10px;
        border-radius: var(--radius-pill);
    }

    .stat-card-body {
        display: flex;
        align-items: center;
        gap: 16px;
    }

    .stat-card-icon {
        width: 56px;
        height: 56px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        flex-shrink: 0;
    }

    .stat-card-content {
        flex: 1;
    }

    .stat-card-number {
        font-family: var(--font-display);
        font-size: 2rem;
        font-weight: 600;
        color: var(--text-dark);
        line-height: 1;
        margin-bottom: 4px;
    }

    .stat-card-label {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
    }

    .pending-card {
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        border-radius: var(--radius-comfortable);
        padding: 20px 24px;
        margin-bottom: 32px;
        display: flex;
        align-items: center;
        gap: 20px;
        border: 1px solid #fcd34d;
    }

    .pending-icon {
        width: 52px;
        height: 52px;
        background: rgba(245, 158, 11, 0.2);
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        color: #d97706;
        flex-shrink: 0;
    }

    .pending-content {
        flex: 1;
    }

    .pending-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: #92400e;
        margin-bottom: 4px;
    }

    .pending-count {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: #b45309;
    }

    .pending-badges {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    .pending-badge {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 10px;
        border-radius: var(--radius-pill);
        background: rgba(245, 158, 11, 0.3);
        color: #92400e;
    }

    .pending-btn {
        padding: 10px 20px;
        background: #d97706;
        color: white;
        border: none;
        border-radius: var(--radius-standard);
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.875rem;
        cursor: pointer;
        text-decoration: none;
        transition: all 0.3s ease;
        white-space: nowrap;
    }

    .pending-btn:hover {
        background: #b45309;
        color: white;
        transform: translateY(-1px);
    }

    .section-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
    }

    .activity-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        transition: all 0.3s ease;
    }

    .activity-card:hover {
        transform: translateX(4px);
        border-color: var(--brand-blue);
    }

    .activity-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .activity-item:last-child {
        border-bottom: none;
        padding-bottom: 0;
    }

    .activity-item:first-child {
        padding-top: 0;
    }

    .activity-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1rem;
        flex-shrink: 0;
    }

    .activity-content {
        flex: 1;
        min-width: 0;
    }

    .activity-title {
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--text-dark);
        margin-bottom: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .activity-meta {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
    }

    @media (max-width: 768px) {
        .admin-hero {
            padding: 24px;
        }

        .admin-hero-title {
            font-size: 1.5rem;
        }

        .pending-card {
            flex-direction: column;
            text-align: center;
        }

        .pending-badges {
            justify-content: center;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-grid-3x3-gap me-2"></i>管理中心
            </h1>
            <p class="admin-hero-subtitle">欢迎回来，<%= user.getName() != null ? user.getName() : user.getUsername() %>！在这里管理小组的各个方面</p>
        </div>

        <% if (totalPending > 0) { %>
        <div class="pending-card">
            <div class="pending-icon">
                <i class="bi bi-clock-history"></i>
            </div>
            <div class="pending-content">
                <div class="pending-title"><%= totalPending %> 项待审核</div>
                <div class="pending-badges">
                    <% if (pendingAwards > 0) { %><span class="pending-badge"><%= pendingAwards %> 奖项</span><% } %>
                    <% if (pendingRecruits > 0) { %><span class="pending-badge"><%= pendingRecruits %> 招新</span><% } %>
                    <% if (pendingActivities > 0) { %><span class="pending-badge"><%= pendingActivities %> 活动</span><% } %>
                </div>
            </div>
            <%
                String reviewUrl;
                if (pendingRecruits > 0) {
                    reviewUrl = contextPath + "/admin/recruit/manage";
                } else if (pendingActivities > 0) {
                    reviewUrl = contextPath + "/activity/list";
                } else {
                    reviewUrl = contextPath + "/award?action=approveList&status=PENDING";
                }
            %>
            <a href="<%= reviewUrl %>" class="pending-btn">去审核</a>
        </div>
        <% } %>

        <div class="row g-4 mb-5">
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/news?action=manage" class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-card-title">新闻管理</span>
                        <span class="stat-card-badge" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;"><%= newsCount %></span>
                    </div>
                    <div class="stat-card-body">
                        <div class="stat-card-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
                            <i class="bi bi-newspaper text-white"></i>
                        </div>
                        <div class="stat-card-content">
                            <div class="stat-card-number"><%= newsCount %></div>
                            <div class="stat-card-label">新闻数量</div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/project/list" class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-card-title">项目管理</span>
                        <span class="stat-card-badge" style="background: rgba(16, 185, 129, 0.1); color: #10b981;"><%= projectCount %></span>
                    </div>
                    <div class="stat-card-body">
                        <div class="stat-card-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                            <i class="bi bi-kanban text-white"></i>
                        </div>
                        <div class="stat-card-content">
                            <div class="stat-card-number"><%= projectCount %></div>
                            <div class="stat-card-label">项目数量</div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/admin/member/list" class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-card-title">成员管理</span>
                        <span class="stat-card-badge" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;"><%= userCount %></span>
                    </div>
                    <div class="stat-card-body">
                        <div class="stat-card-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
                            <i class="bi bi-people text-white"></i>
                        </div>
                        <div class="stat-card-content">
                            <div class="stat-card-number"><%= userCount %></div>
                            <div class="stat-card-label">成员数量</div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/admin/log/list" class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-card-title">操作日志</span>
                        <span class="stat-card-badge" style="background: rgba(100, 116, 139, 0.1); color: #64748b;"><%= logCount %></span>
                    </div>
                    <div class="stat-card-body">
                        <div class="stat-card-icon" style="background: linear-gradient(135deg, #64748b, #94a3b8);">
                            <i class="bi bi-journal-text text-white"></i>
                        </div>
                        <div class="stat-card-content">
                            <div class="stat-card-number"><%= logCount %></div>
                            <div class="stat-card-label">日志记录</div>
                        </div>
                    </div>
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-md-6">
                <h3 class="section-title">
                    <i class="bi bi-lightning me-2 text-brand"></i>快捷操作
                </h3>
                <div class="activity-card">
                    <a href="<%= contextPath %>/news?action=manage" class="activity-item">
                        <div class="activity-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                            <i class="bi bi-plus-circle"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">发布新新闻</div>
                            <div class="activity-meta">创建通知公告、活动新闻等</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/activity?action=manage" class="activity-item">
                        <div class="activity-icon" style="background: rgba(236, 72, 153, 0.1); color: #ec4899;">
                            <i class="bi bi-calendar-plus"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">创建活动</div>
                            <div class="activity-meta">组织新的小组活动</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/award/list" class="activity-item">
                        <div class="activity-icon" style="background: rgba(249, 115, 22, 0.1); color: #f97316;">
                            <i class="bi bi-trophy"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">审核奖项</div>
                            <div class="activity-meta">审批成员的奖项申请</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/admin/member/list" class="activity-item">
                        <div class="activity-icon" style="background: rgba(20, 184, 166, 0.1); color: #14b8a6;">
                            <i class="bi bi-person-plus"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">处理招新</div>
                            <div class="activity-meta">审核新的成员申请</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/ai?action=chat" class="activity-item">
                        <div class="activity-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                            <i class="bi bi-robot"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">AI助手</div>
                            <div class="activity-meta">获取管理方面的建议</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                </div>
            </div>
            <div class="col-md-6">
                <h3 class="section-title">
                    <i class="bi bi-gear me-2 text-brand"></i>系统管理
                </h3>
                <div class="activity-card">
                    <a href="<%= contextPath %>/admin/profile.jsp" class="activity-item">
                        <div class="activity-icon" style="background: rgba(99, 102, 241, 0.1); color: #6366f1;">
                            <i class="bi bi-person-circle"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">个人信息</div>
                            <div class="activity-meta">查看和编辑个人资料</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/admin/password-change.jsp" class="activity-item">
                        <div class="activity-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                            <i class="bi bi-lock"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">修改密码</div>
                            <div class="activity-meta">更新账户密码</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/attendance/manage" class="activity-item">
                        <div class="activity-icon" style="background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">
                            <i class="bi bi-book"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">学习管理</div>
                            <div class="activity-meta">查看成员学习时长</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/admin/problem" class="activity-item">
                        <div class="activity-icon" style="background: rgba(168, 85, 247, 0.1); color: #a855f7;">
                            <i class="bi bi-exclamation-triangle"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">问题反馈</div>
                            <div class="activity-meta">处理成员提交的问题</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                    <a href="<%= contextPath %>/index.jsp" class="activity-item">
                        <div class="activity-icon" style="background: rgba(75, 85, 99, 0.1); color: #4b5563;">
                            <i class="bi bi-display"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-title">返回前台</div>
                            <div class="activity-meta">查看前台首页</div>
                        </div>
                        <i class="bi bi-chevron-right text-muted"></i>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />