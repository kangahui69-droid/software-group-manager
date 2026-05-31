<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%@ page import="dao.MemberProfileDAO" %>
<%@ page import="model.MemberProfile" %>
<% User user=(User) session.getAttribute("user"); 
   if (user==null) {
       response.sendRedirect(request.getContextPath() + "/login.jsp" ); 
       return; 
   }
   MemberProfile memberProfile = (MemberProfile) session.getAttribute("memberProfile");
   if (memberProfile == null) {
       MemberProfileDAO profileDAO = new MemberProfileDAO();
       memberProfile = profileDAO.findByUserId(user.getId());
       if (memberProfile != null) {
           session.setAttribute("memberProfile", memberProfile);
       }
   }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="个人中心" />
</jsp:include>

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

    .profile-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
        height: 100%;
    }

    .profile-card-header {
        padding: 24px;
        text-align: center;
        border-bottom: 1px solid var(--border-light);
    }

    .profile-avatar {
        width: 80px;
        height: 80px;
        border-radius: var(--radius-comfortable);
        object-fit: cover;
        margin-bottom: 16px;
        border: 3px solid var(--bg-white);
        box-shadow: var(--shadow-standard);
    }

    .profile-avatar-fallback {
        width: 80px;
        height: 80px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2rem;
        font-weight: 600;
        margin: 0 auto 16px;
    }

    .profile-name {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 4px;
    }

    .profile-role {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
        text-transform: capitalize;
    }

    .profile-status {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        margin-top: 12px;
        padding: 6px 14px;
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
        border-radius: var(--radius-pill);
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
    }

    .profile-card-body {
        padding: 20px;
    }

    .profile-link {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 14px;
        border-radius: var(--radius-standard);
        text-decoration: none;
        transition: all 0.3s ease;
        margin-bottom: 8px;
    }

    .profile-link:last-child {
        margin-bottom: 0;
    }

    .profile-link:hover {
        background: rgba(20, 86, 240, 0.05);
    }

    .profile-link-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.125rem;
        flex-shrink: 0;
    }

    .profile-link-content {
        flex: 1;
    }

    .profile-link-title {
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--text-dark);
        margin-bottom: 2px;
    }

    .profile-link-desc {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
    }

    .profile-link-arrow {
        color: var(--text-muted);
        transition: transform 0.3s ease;
    }

    .profile-link:hover .profile-link-arrow {
        transform: translateX(4px);
        color: var(--brand-blue);
    }

    .quick-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 20px;
        margin-bottom: 16px;
        transition: all 0.3s ease;
        text-decoration: none;
        display: block;
    }

    .quick-card:hover {
        transform: translateX(8px);
        border-color: var(--brand-blue);
        box-shadow: var(--shadow-brand-offset);
    }

    .quick-card:last-child {
        margin-bottom: 0;
    }

    .quick-card-row {
        display: flex;
        align-items: center;
        gap: 14px;
    }

    .quick-card-icon {
        width: 44px;
        height: 44px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.25rem;
        flex-shrink: 0;
    }

    .quick-card-content {
        flex: 1;
    }

    .quick-card-title {
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.94rem;
        color: var(--text-dark);
        margin-bottom: 2px;
    }

    .quick-card-desc {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
    }

    .quick-card-arrow {
        color: var(--text-muted);
        font-size: 1.25rem;
    }

    .section-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
    }

    .stat-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: rgba(20, 86, 240, 0.08);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        margin-top: 12px;
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-person-circle me-2"></i>个人中心
            </h1>
            <p class="member-hero-subtitle">欢迎回来，<%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %>！在这里管理您的个人信息和内容</p>
        </div>

        <div class="row g-4">
            <div class="col-md-6 col-lg-4">
                <div class="profile-card">
                    <div class="profile-card-header">
                        <% if (memberProfile != null && memberProfile.getAvatarFileId() != null) { %>
                            <img src="${pageContext.request.contextPath}/file?action=view&id=<%=memberProfile.getAvatarFileId()%>" 
                                 alt="用户头像" class="profile-avatar">
                        <% } else { %>
                            <div class="profile-avatar-fallback">
                                <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName().substring(0,1) : user.getUsername().substring(0,1).toUpperCase() %>
                            </div>
                        <% } %>
                        <div class="profile-name"><%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %></div>
                        <div class="profile-role"><%= user.getRole().toLowerCase() %></div>
                        <div class="profile-status">
                            <i class="bi bi-check-circle-fill"></i>
                            正式成员
                        </div>
                    </div>
                    <div class="profile-card-body">
                        <a href="profile.jsp" class="profile-link">
                            <div class="profile-link-icon" style="background: rgba(99, 102, 241, 0.1); color: #6366f1;">
                                <i class="bi bi-person-vcard"></i>
                            </div>
                            <div class="profile-link-content">
                                <div class="profile-link-title">个人资料</div>
                                <div class="profile-link-desc">查看和编辑个人信息</div>
                            </div>
                            <i class="bi bi-chevron-right profile-link-arrow"></i>
                        </a>
                        <a href="password-change.jsp" class="profile-link">
                            <div class="profile-link-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                                <i class="bi bi-lock"></i>
                            </div>
                            <div class="profile-link-content">
                                <div class="profile-link-title">修改密码</div>
                                <div class="profile-link-desc">更新账户密码</div>
                            </div>
                            <i class="bi bi-chevron-right profile-link-arrow"></i>
                        </a>
                        <a href="resume.jsp" class="profile-link">
                            <div class="profile-link-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                <i class="bi bi-file-earmark-person"></i>
                            </div>
                            <div class="profile-link-content">
                                <div class="profile-link-title">我的简历</div>
                                <div class="profile-link-desc">在线简历维护</div>
                            </div>
                            <i class="bi bi-chevron-right profile-link-arrow"></i>
                        </a>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-8">
                <h3 class="section-title">
                    <i class="bi bi-lightning me-2 text-brand"></i>快捷功能
                </h3>
                <div class="row g-3">
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                                    <i class="bi bi-calendar-check text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">我的活动</div>
                                    <div class="quick-card-desc">查看已报名的活动</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/group/my-groups" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
                                    <i class="bi bi-chat-dots text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">我的群聊</div>
                                    <div class="quick-card-desc">参与小组讨论交流</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/project?action=list" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
                                    <i class="bi bi-briefcase text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">我的项目</div>
                                    <div class="quick-card-desc">查看和管理项目申请</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/award?action=list" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #f97316, #fb923c);">
                                    <i class="bi bi-award text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">我的奖项</div>
                                    <div class="quick-card-desc">荣誉证书管理</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/activity?action=myCreatedActivities" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6);">
                                    <i class="bi bi-calendar-plus text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">我发起的</div>
                                    <div class="quick-card-desc">管理我创建的活动</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/ai?action=chat" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);">
                                    <i class="bi bi-robot text-white"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">AI助手</div>
                                    <div class="quick-card-desc">智能问答助手</div>
                                </div>
                                <i class="bi bi-chevron-right quick-card-arrow"></i>
                            </div>
                        </a>
                    </div>
                </div>

                <h3 class="section-title mt-5">
                    <i class="bi bi-newspaper me-2 text-brand"></i>内容浏览
                </h3>
                <div class="row g-3">
                    <div class="col-sm-4">
                        <a href="${pageContext.request.contextPath}/news?type=notice" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                                    <i class="bi bi-megaphone"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">通知公告</div>
                                    <div class="quick-card-desc">查看通知</div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-4">
                        <a href="${pageContext.request.contextPath}/news?type=award" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                                    <i class="bi bi-trophy"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">获奖新闻</div>
                                    <div class="quick-card-desc">了解荣誉</div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-4">
                        <a href="${pageContext.request.contextPath}/news?type=activity" class="quick-card">
                            <div class="quick-card-row">
                                <div class="quick-card-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                    <i class="bi bi-calendar-event"></i>
                                </div>
                                <div class="quick-card-content">
                                    <div class="quick-card-title">活动新闻</div>
                                    <div class="quick-card-desc">回顾活动</div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />