<%@ page language="java" contentType="text/html;" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dao.MemberProfileDAO" %>
<%@ page import="model.MemberProfile" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%
    if (session.getAttribute("memberProfile") == null) {
        model.User user = (model.User)session.getAttribute("user");
        if (user != null) {
            MemberProfileDAO profileDAO = new MemberProfileDAO();
            MemberProfile profile = profileDAO.findByUserId(user.getId());
            if (profile != null) {
                session.setAttribute("memberProfile", profile);
            }
        }
    }
    String birthdayDisplay = "";
    Object birthdayObj = session.getAttribute("memberProfile");
    if (birthdayObj != null) {
        try {
            java.lang.reflect.Method getBirthday = birthdayObj.getClass().getMethod("getBirthday");
            Date birthday = (Date) getBirthday.invoke(birthdayObj);
            if (birthday != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                birthdayDisplay = sdf.format(birthday);
            }
        } catch (Exception e) {}
    }
    session.setAttribute("birthdayDisplay", birthdayDisplay);
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="个人中心" />
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
.profile-hero {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 50%, #3b82f6 100%);
    border-radius: var(--radius-generous);
    padding: 40px;
    margin-bottom: 32px;
    color: white;
    position: relative;
    overflow: hidden;
}

.profile-hero::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
}

.profile-hero::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    border-radius: 50%;
}

.profile-avatar-section {
    position: relative;
    z-index: 1;
}

.profile-avatar {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    object-fit: cover;
    border: 5px solid rgba(255,255,255,0.3);
    box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.profile-avatar-placeholder {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    border: 5px solid rgba(255,255,255,0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56px;
    font-weight: 700;
    color: white;
    box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.profile-info {
    position: relative;
    z-index: 1;
}

.profile-name {
    font-family: var(--font-display);
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 8px;
    color: white;
}

.profile-role {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 16px;
    background: rgba(255,255,255,0.2);
    border-radius: var(--radius-pill);
    font-size: 0.875rem;
    margin-bottom: 12px;
}

.quick-card {
    background: var(--bg-white);
    border-radius: var(--radius-generous);
    box-shadow: var(--shadow-brand-purple);
    border: none;
    overflow: hidden;
    transition: all 0.3s ease;
    height: 100%;
}

.quick-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-brand-offset);
}

.quick-card-body {
    padding: 24px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.quick-card-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-standard);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    margin-bottom: 16px;
}

.info-card {
    background: var(--bg-white);
    border-radius: var(--radius-generous);
    box-shadow: var(--shadow-brand-purple);
    border: none;
    overflow: hidden;
}

.info-card-header {
    background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-light) 100%);
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-light);
}

.info-card-title {
    font-family: var(--font-display);
    font-size: 1.25rem;
    font-weight: 600;
    color: white;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.info-card-body {
    padding: 24px;
}

.info-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid var(--border-light);
}

.info-item:last-child {
    border-bottom: none;
}

.info-item-icon {
    width: 44px;
    height: 44px;
    border-radius: var(--radius-standard);
    background: var(--primary-light);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    color: var(--brand-blue);
    font-size: 1.1rem;
}

.info-item-content {
    flex: 1;
}

.info-item-label {
    font-size: 0.81rem;
    color: var(--text-muted);
    margin-bottom: 4px;
}

.info-item-value {
    font-size: 1rem;
    font-weight: 500;
    color: var(--text-dark);
}

.info-item-value.empty {
    color: var(--text-muted);
    font-style: italic;
}

.badge-role {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    border-radius: var(--radius-pill);
    font-size: 0.75rem;
    font-weight: 600;
}

.badge-admin {
    background: rgba(239, 68, 68, 0.1);
    color: #dc2626;
}

.badge-member {
    background: rgba(16, 185, 129, 0.1);
    color: #059669;
}

.badge-teacher {
    background: rgba(139, 92, 246, 0.1);
    color: #7c3aed;
}

.text-yellow { color: #eab308; }
.text-purple { color: #8b5cf6; }
.text-green { color: #10b981; }
.text-blue { color: var(--brand-blue); }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="profile-hero">
            <div class="row align-items-center">
                <div class="col-auto profile-avatar-section">
                    <c:choose>
                        <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                            <img src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}" alt="用户头像" class="profile-avatar">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-avatar-placeholder">
                                ${not empty user.name ? user.name.charAt(0) : '用'}
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="col profile-info">
                    <h1 class="profile-name">${user.name}</h1>
                    <div class="profile-role">
                        <c:choose>
                            <c:when test="${user.role eq 'ADMIN'}">
                                <i class="bi bi-shield-star"></i> 管理员
                            </c:when>
                            <c:when test="${user.role eq 'MEMBER'}">
                                <i class="bi bi-person-check"></i> 成员
                            </c:when>
                            <c:when test="${user.role eq 'TEACHER'}">
                                <i class="bi bi-mortarboard"></i> 教师
                            </c:when>
                            <c:otherwise>
                                <i class="bi bi-person"></i> ${user.role}
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="d-flex gap-2 mt-3">
                        <a href="${pageContext.request.contextPath}/member/edit-profile.jsp" class="btn" style="background: white; color: var(--brand-blue); border-radius: var(--radius-standard); padding: 10px 20px; font-weight: 600;">
                            <i class="bi bi-pencil me-2"></i>编辑资料
                        </a>
                        <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn" style="background: rgba(255,255,255,0.2); color: white; border: 1px solid rgba(255,255,255,0.3); border-radius: var(--radius-standard); padding: 10px 20px; font-weight: 500;">
                            <i class="bi bi-lock me-2"></i>修改密码
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/award?action=list" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(234, 179, 8, 0.1); color: #eab308;">
                                <i class="bi bi-trophy"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">我的奖项</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">查看获奖记录</p>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/project?action=myProjects" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                <i class="bi bi-kanban"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">我的项目</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">参与的项目</p>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                <i class="bi bi-calendar-check"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">我的活动</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">参与的活动</p>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/group/my-groups" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                                <i class="bi bi-chat-dots"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">我的群聊</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">加入的群组</p>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/study" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                <i class="bi bi-book"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">学习中心</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">每日学习打卡</p>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-4 col-xxl-2">
                <a href="${pageContext.request.contextPath}/news?type=activity" class="text-decoration-none">
                    <div class="quick-card">
                        <div class="quick-card-body">
                            <div class="quick-card-icon" style="background: rgba(249, 115, 22, 0.1); color: #f97316;">
                                <i class="bi bi-newspaper"></i>
                            </div>
                            <h4 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 4px;">活动新闻</h4>
                            <p class="text-muted mb-0" style="font-size: 0.81rem;">查看动态资讯</p>
                        </div>
                    </div>
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-5">
                <div class="info-card">
                    <div class="info-card-header">
                        <h3 class="info-card-title">
                            <i class="bi bi-person-badge"></i>基本信息
                        </h3>
                    </div>
                    <div class="info-card-body">
                        <div class="info-item">
                            <div class="info-item-icon">
                                <i class="bi bi-person"></i>
                            </div>
                            <div class="info-item-content">
                                <div class="info-item-label">用户名</div>
                                <div class="info-item-value">${user.username}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-item-icon">
                                <i class="bi bi-shield"></i>
                            </div>
                            <div class="info-item-content">
                                <div class="info-item-label">角色</div>
                                <div class="info-item-value">
                                    <c:choose>
                                        <c:when test="${user.role eq 'ADMIN'}">
                                            <span class="badge-role badge-admin"><i class="bi bi-shield-star"></i> 管理员</span>
                                        </c:when>
                                        <c:when test="${user.role eq 'MEMBER'}">
                                            <span class="badge-role badge-member"><i class="bi bi-person-check"></i> 成员</span>
                                        </c:when>
                                        <c:when test="${user.role eq 'TEACHER'}">
                                            <span class="badge-role badge-teacher"><i class="bi bi-mortarboard"></i> 教师</span>
                                        </c:when>
                                        <c:otherwise>${user.role}</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-item-icon">
                                <i class="bi bi-telephone"></i>
                            </div>
                            <div class="info-item-content">
                                <div class="info-item-label">电话</div>
                                <div class="info-item-value ${empty user.phone ? 'empty' : ''}">${not empty user.phone ? user.phone : '未填写'}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-item-icon">
                                <i class="bi bi-envelope"></i>
                            </div>
                            <div class="info-item-content">
                                <div class="info-item-label">邮箱</div>
                                <div class="info-item-value ${empty user.email ? 'empty' : ''}">${not empty user.email ? user.email : '未填写'}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-item-icon">
                                <i class="bi bi-calendar3"></i>
                            </div>
                            <div class="info-item-content">
                                <div class="info-item-label">创建时间</div>
                                <div class="info-item-value">${user.createdAt}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-7">
                <div class="info-card">
                    <div class="info-card-header">
                        <h3 class="info-card-title">
                            <i class="bi bi-card-list"></i>详细资料
                        </h3>
                    </div>
                    <div class="info-card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-cake2"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">生日</div>
                                        <div class="info-item-value ${empty birthdayDisplay ? 'empty' : ''}">${not empty birthdayDisplay ? birthdayDisplay : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-123"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">学号</div>
                                        <div class="info-item-value ${empty memberProfile.studentId ? 'empty' : ''}">${not empty memberProfile.studentId ? memberProfile.studentId : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-mortarboard"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">专业</div>
                                        <div class="info-item-value ${empty memberProfile.major ? 'empty' : ''}">${not empty memberProfile.major ? memberProfile.major : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-calendar-week"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">年级</div>
                                        <div class="info-item-value ${empty memberProfile.grade ? 'empty' : ''}">${not empty memberProfile.grade ? memberProfile.grade : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-github"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">GitHub</div>
                                        <div class="info-item-value ${empty memberProfile.github ? 'empty' : ''}">${not empty memberProfile.github ? memberProfile.github : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="info-item">
                                    <div class="info-item-icon">
                                        <i class="bi bi-globe2"></i>
                                    </div>
                                    <div class="info-item-content">
                                        <div class="info-item-label">博客</div>
                                        <div class="info-item-value ${empty memberProfile.blog ? 'empty' : ''}">${not empty memberProfile.blog ? memberProfile.blog : '未填写'}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="info-item" style="flex-direction: column; align-items: flex-start;">
                                    <div class="info-item-icon mb-2">
                                        <i class="bi bi-card-text"></i>
                                    </div>
                                    <div class="info-item-content w-100">
                                        <div class="info-item-label">个人简介</div>
                                        <div class="info-item-value ${empty memberProfile.introduction ? 'empty' : ''}" style="white-space: pre-wrap;">${not empty memberProfile.introduction ? memberProfile.introduction : '未填写'}</div>
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