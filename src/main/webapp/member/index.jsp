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
:root {
    --brand-blue: #1456f0;
    --font-display: 'Outfit', sans-serif;
    --font-ui: 'DM Sans', sans-serif;
    --radius-generous: 16px;
    --radius-standard: 12px;
    --radius-comfortable: 10px;
    --radius-pill: 9999px;
    --shadow-brand-purple: 0 4px 20px rgba(20, 85, 240, 0.15);
    --shadow-brand-offset: 0 8px 32px rgba(20, 85, 240, 0.12);
    --shadow-standard: 0 2px 8px rgba(0, 0, 0, 0.06);
    --bg-white: #ffffff;
    --text-dark: #1a1a2e;
    --text-muted: #6b7280;
    --border-gray: #e5e7eb;
    --border-light: #f3f4f6;
    --primary-light: #eff6ff;
}
body {
    font-family: var(--font-ui);
    background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
}
.page-header {
    background: transparent;
    padding: 24px 0;
}
.page-title {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0;
}
.card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-standard);
    box-shadow: var(--shadow-standard);
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
    background: var(--primary-light);
    color: var(--brand-blue);
}
.bg-green-lt {
    background: #ecfdf5;
    color: #065f46;
}
.badge {
    font-weight: 500;
    font-size: 12px;
    padding: 5px 12px;
    border-radius: var(--radius-pill);
}
.card-btn {
    display: block;
    padding: 14px 24px;
    text-align: center;
    background: var(--primary-light);
    color: var(--brand-blue);
    font-weight: 600;
    font-size: 14px;
    text-decoration: none;
    border-top: 1px solid var(--border-light);
    transition: all 0.2s ease;
    border-radius: 0 0 var(--radius-standard) var(--radius-standard);
}
.card-btn:hover {
    background: var(--brand-blue);
    color: #ffffff;
}
.card-sm {
    transition: all 0.2s ease;
}
.card-sm:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-brand-offset);
}
.list-group-item {
    border: none;
    padding: 14px 16px;
    border-bottom: 1px solid var(--border-light);
    transition: all 0.15s ease;
}
.list-group-item:last-child {
    border-bottom: none;
}
.list-group-item-action:hover {
    background: var(--primary-light);
    color: var(--brand-blue);
}
.list-group-item-action .text-blue { color: var(--brand-blue); }
.list-group-item-action .text-green { color: #10b981; }
.list-group-item-action .text-primary { color: var(--brand-blue); }
.list-group-item-action .text-orange { color: #f97316; }
.list-group-item-action .text-yellow { color: #eab308; }
.list-group-item-action .text-purple { color: #8b5cf6; }
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
            <div class="col-md-6 col-lg-4">
                <div class="card">
                    <div class="card-body p-4 text-center">
                        <% if (memberProfile != null && memberProfile.getAvatarFileId() != null) { %>
                            <img src="${pageContext.request.contextPath}/file?action=view&id=<%=memberProfile.getAvatarFileId()%>" 
                                 alt="用户头像" class="avatar avatar-xl mb-3 avatar-rounded">
                        <% } else { %>
                            <span class="avatar avatar-xl mb-3 avatar-rounded bg-blue-lt">
                                <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName().substring(0,1) : user.getUsername().substring(0,1).toUpperCase() %>
                            </span>
                        <% } %>
                        <h3 class="m-0 mb-1" style="font-family: var(--font-display); font-weight: 600;">
                            <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %>
                        </h3>
                        <div class="text-muted text-capitalize" style="font-size: 14px;">
                            <%= user.getRole().toLowerCase() %>
                        </div>
                        <div class="mt-3">
                            <span class="badge bg-green-lt">正式成员</span>
                        </div>
                    </div>
                    <a href="profile.jsp" class="card-btn">查看个人资料</a>
                </div>
                
                <div class="card mt-4">
                    <div class="card-header" style="padding: 16px 20px; border-bottom: 1px solid var(--border-light);">
                        <h3 class="card-title mb-0" style="font-size: 16px;">快速链接</h3>
                    </div>
                    <div class="list-group list-group-flush">
                        <a href="profile.jsp" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-person-circle me-3"></i> 个人中心
                        </a>
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-calendar-check me-3"></i> 我的活动
                        </a>
                        <a href="${pageContext.request.contextPath}/group/my-groups" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-chat-dots me-3"></i> 我的群聊
                        </a>
                        <a href="${pageContext.request.contextPath}/project?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-briefcase me-3"></i> 我的项目
                        </a>
                        <a href="${pageContext.request.contextPath}/award?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-award me-3"></i> 我的奖项
                        </a>
                        <a href="resume.jsp" class="list-group-item list-group-item-action d-flex align-items-center border-bottom-0">
                            <i class="bi bi-file-earmark-person me-3"></i> 我的简历
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-md-6 col-lg-8">
                <div class="row row-cards">
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-blue text-white avatar"><i class="bi bi-briefcase"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium" style="font-weight: 600;">我的项目</div>
                                        <div class="text-muted" style="font-size: 14px;"><a href="${pageContext.request.contextPath}/project?action=list" class="text-reset">查看 & 管理申请</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-green text-white avatar"><i class="bi bi-calendar-check"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium" style="font-weight: 600;">我的活动</div>
                                        <div class="text-muted" style="font-size: 14px;"><a href="${pageContext.request.contextPath}/activity?action=myActivities" class="text-reset">查看我的报名活动</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-yellow text-white avatar"><i class="bi bi-award"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium" style="font-weight: 600;">我的奖项</div>
                                        <div class="text-muted" style="font-size: 14px;"><a href="${pageContext.request.contextPath}/award?action=list" class="text-reset">荣誉证书管理</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-azure text-white avatar"><i class="bi bi-file-earmark-person"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium" style="font-weight: 600;">我的简历</div>
                                        <div class="text-muted" style="font-size: 14px;"><a href="resume.jsp" class="text-reset">在线简历维护</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />