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

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
.page-header {
    background: transparent;
    padding: 24px 0;
}
.page-title {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0;
}
.card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-generous);
    box-shadow: var(--shadow-standard);
    overflow: hidden;
    transition: all 0.25s ease;
}
.card:hover {
    box-shadow: var(--shadow-card-elevated);
    transform: translateY(-2px);
}
.card-body {
    padding: 24px;
}
.avatar-xl {
    width: 80px;
    height: 80px;
    font-size: 32px;
}
.avatar-rounded {
    border-radius: 50%;
}
.bg-blue-lt {
    background: #eff6ff;
    color: var(--brand-blue);
}
.bg-green-lt {
    background: #ecfdf5;
    color: #059669;
}
.bg-purple-lt {
    background: #f3e8ff;
    color: #7c3aed;
}
.bg-yellow-lt {
    background: #fef9c3;
    color: #ca8a04;
}
.bg-orange-lt {
    background: #fff7ed;
    color: #ea580c;
}
.badge {
    font-weight: 500;
    font-size: 12px;
    padding: 5px 12px;
    border-radius: var(--radius-pill);
}
.badge-primary {
    background: var(--brand-blue);
    color: white;
}
.card-btn {
    display: block;
    padding: 14px 24px;
    text-align: center;
    background: #f8fafc;
    color: var(--brand-blue);
    font-weight: 600;
    font-size: 14px;
    text-decoration: none;
    border-top: 1px solid var(--border-light);
    transition: all 0.2s ease;
}
.card-btn:hover {
    background: var(--brand-blue);
    color: #ffffff;
}
.card-sm {
    transition: all 0.25s ease;
}
.card-sm:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-card-elevated);
}
.card-sm .icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    transition: all 0.25s ease;
}
.card-sm:hover .icon-wrap {
    transform: scale(1.05);
}
.list-group-item {
    border: none;
    padding: 16px 20px;
    border-bottom: 1px solid var(--border-light);
    transition: all 0.15s ease;
    background: transparent;
}
.list-group-item:last-child {
    border-bottom: none;
}
.list-group-item-action:hover {
    background: #f8fafc;
    color: var(--brand-blue);
    padding-left: 24px;
}
.text-blue { color: var(--brand-blue) !important; }
.text-green { color: #059669 !important; }
.text-purple { color: #7c3aed !important; }
.text-orange { color: #ea580c !important; }
.text-yellow { color: #ca8a04 !important; }
.quick-link-card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
}
.quick-link-card:hover {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
}
.profile-section {
    background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-600) 100%);
    padding: 28px 24px;
    border-radius: var(--radius-generous) var(--radius-generous) 0 0;
    color: white;
    text-align: center;
    position: relative;
    overflow: hidden;
}
.profile-section::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
}
.profile-section .avatar-xl {
    border: 4px solid white;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
}
.profile-section h3 {
    color: white;
    margin-top: 12px;
}
.profile-section .badge {
    background: rgba(255, 255, 255, 0.2);
    color: white;
}
.section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
}
.section-header i {
    font-size: 20px;
    color: var(--brand-blue);
}
.section-header h3 {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0;
}
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    个人中心
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-md-5 col-lg-4">
                <div class="card">
                    <div class="profile-section">
                        <% if (memberProfile != null && memberProfile.getAvatarFileId() != null) { %>
                            <img src="${pageContext.request.contextPath}/file?action=view&id=<%=memberProfile.getAvatarFileId()%>" 
                                 alt="用户头像" class="avatar avatar-xl mb-3 avatar-rounded">
                        <% } else { %>
                            <span class="avatar avatar-xl mb-3 avatar-rounded" style="background: rgba(255,255,255,0.2); color: white;">
                                <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName().substring(0,1) : user.getUsername().substring(0,1).toUpperCase() %>
                            </span>
                        <% } %>
                        <h3 style="font-family: var(--font-display); font-weight: 600;">
                            <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %>
                        </h3>
                        <div class="text-white-50 text-capitalize" style="font-size: 14px;">
                            <%= user.getRole().toLowerCase() %>
                        </div>
                        <div class="mt-3">
                            <span class="badge badge-primary">正式成员</span>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div class="list-group list-group-flush">
                            <a href="profile.jsp" class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-person me-3 text-blue"></i> 个人资料
                            </a>
                            <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-calendar-check me-3 text-green"></i> 我的活动
                            </a>
                            <a href="${pageContext.request.contextPath}/group/my-groups" class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-chat-dots me-3 text-purple"></i> 我的群聊
                            </a>
                            <a href="${pageContext.request.contextPath}/project?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-briefcase me-3 text-blue"></i> 我的项目
                            </a>
                            <a href="${pageContext.request.contextPath}/award?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                                <i class="bi bi-award me-3 text-yellow"></i> 我的奖项
                            </a>
                            <a href="${pageContext.request.contextPath}/study" class="list-group-item list-group-item-action d-flex align-items-center border-bottom-0">
                                <i class="bi bi-book me-3 text-blue"></i> 学习中心
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-7 col-lg-8">
                <div class="section-header">
                    <i class="bi bi-grid-3x3-gap"></i>
                    <h3>我的功能</h3>
                </div>
                <div class="row row-cards">
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/project?action=list" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-blue-lt">
                                        <i class="bi bi-briefcase text-blue"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">我的项目</div>
                                        <div class="text-muted" style="font-size: 13px;">查看 & 管理</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-green-lt">
                                        <i class="bi bi-calendar-check text-green"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">我的活动</div>
                                        <div class="text-muted" style="font-size: 13px;">查看报名</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/award?action=list" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-yellow-lt">
                                        <i class="bi bi-award text-yellow"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">我的奖项</div>
                                        <div class="text-muted" style="font-size: 13px;">荣誉管理</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/study" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-blue-lt">
                                        <i class="bi bi-book text-blue"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">学习中心</div>
                                        <div class="text-muted" style="font-size: 13px;">在线学习</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/member/problem" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap" style="background: rgba(168, 85, 247, 0.1);">
                                        <i class="bi bi-exclamation-triangle text-purple"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">问题反馈</div>
                                        <div class="text-muted" style="font-size: 13px;">提交 & 查看</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6 col-lg-4">
                        <a href="${pageContext.request.contextPath}/group/my-groups" class="card card-sm d-block text-decoration-none">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap" style="background: rgba(20, 184, 166, 0.1);">
                                        <i class="bi bi-chat-dots text-teal"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">我的群聊</div>
                                        <div class="text-muted" style="font-size: 13px;">加入的群聊</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </div>

                <div class="section-header mt-4">
                    <i class="bi bi-lightning-charge"></i>
                    <h3>快捷操作</h3>
                </div>
                <div class="row row-cards">
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/ai?action=chat" class="card card-sm d-block text-decoration-none quick-link-card">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-blue-lt">
                                        <i class="bi bi-robot text-blue"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">AI智能助手</div>
                                        <div class="text-muted" style="font-size: 13px;">有问题找小AI</div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="card card-sm d-block text-decoration-none quick-link-card">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="icon-wrap bg-green-lt">
                                        <i class="bi bi-calendar-plus text-green"></i>
                                    </div>
                                    <div class="ms-3">
                                        <div class="font-weight-medium" style="font-weight: 600; color: var(--text-dark);">浏览活动</div>
                                        <div class="text-muted" style="font-size: 13px;">查看可报名活动</div>
                                    </div>
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