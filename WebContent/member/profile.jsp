<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
        height: 100%;
    }

    .profile-card-header {
        padding: 24px;
        text-align: center;
        border-bottom: 1px solid var(--border-light);
        background: linear-gradient(135deg, rgba(20, 86, 240, 0.05), rgba(96, 165, 250, 0.05));
    }

    .profile-avatar {
        width: 100px;
        height: 100px;
        border-radius: var(--radius-comfortable);
        object-fit: cover;
        margin-bottom: 16px;
        border: 4px solid var(--bg-white);
        box-shadow: var(--shadow-card-elevated);
    }

    .profile-avatar-fallback {
        width: 100px;
        height: 100px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2.5rem;
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

    .info-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .info-card-header {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .info-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .info-card-body {
        padding: 24px;
    }

    .info-item {
        padding: 14px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .info-item:last-child {
        border-bottom: none;
    }

    .info-label {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-bottom: 6px;
    }

    .info-value {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        color: var(--text-dark);
        font-weight: 500;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .function-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 12px;
    }

    .function-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 20px 16px;
        text-align: center;
        text-decoration: none;
        transition: all 0.3s ease;
    }

    .function-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-purple);
        border-color: var(--brand-blue);
    }

    .function-icon {
        width: 48px;
        height: 48px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 12px;
        font-size: 1.5rem;
    }

    .function-title {
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--text-dark);
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

<c:if test="${param.success == '1'}">
    <div class="container-xl mt-4">
        <div class="alert alert-success alert-dismissible" style="border-radius: var(--radius-standard);">
            <i class="bi bi-check-circle me-2"></i>资料更新成功！
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </div>
</c:if>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-person-circle me-2"></i>个人中心
            </h1>
            <p class="member-hero-subtitle">欢迎回来，<%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %>！在这里管理您的个人信息和内容</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-4">
                <div class="profile-card">
                    <div class="profile-card-header">
                        <c:choose>
                            <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                                <img src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}"
                                     alt="用户头像" class="profile-avatar">
                            </c:when>
                            <c:otherwise>
                                <div class="profile-avatar-fallback">
                                    ${not empty user.name ? user.name.charAt(0) : '用'}
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="profile-name">${user.username}</div>
                        <div class="profile-role">
                            <c:choose>
                                <c:when test="${user.role eq 'ADMIN'}">管理员</c:when>
                                <c:when test="${user.role eq 'MEMBER'}">成员</c:when>
                                <c:when test="${user.role eq 'TEACHER'}">教师</c:when>
                                <c:otherwise>${user.role}</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="profile-role">
                            <c:choose>
                                <c:when test="${user.userType eq 'TEACHER'}">教师身份</c:when>
                                <c:when test="${user.userType eq 'STUDENT'}">学生身份</c:when>
                                <c:otherwise>其他身份</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="profile-status">
                            <i class="bi bi-check-circle-fill"></i>
                            正式成员
                        </div>
                    </div>
                    <div class="profile-card-body">
                        <div class="d-flex gap-2">
                            <a href="${pageContext.request.contextPath}/member/edit-profile.jsp" class="btn-brand flex-grow-1 justify-content-center">
                                <i class="bi bi-pencil"></i>编辑资料
                            </a>
                            <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn-outline-brand flex-grow-1 justify-content-center">
                                <i class="bi bi-lock"></i>修改密码
                            </a>
                        </div>
                    </div>
                </div>

                <div class="info-card mt-4">
                    <div class="info-card-header">
                        <h3 class="info-card-title">
                            <i class="bi bi-grid-3x3-gap text-brand"></i>我的功能
                        </h3>
                    </div>
                    <div class="info-card-body">
                        <div class="function-grid">
                            <a href="${pageContext.request.contextPath}/award?action=list" class="function-card">
                                <div class="function-icon" style="background: linear-gradient(135deg, #f97316, #fb923c);">
                                    <i class="bi bi-trophy text-white"></i>
                                </div>
                                <div class="function-title">我的奖项</div>
                            </a>
                            <a href="${pageContext.request.contextPath}/project?action=myProjects" class="function-card">
                                <div class="function-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
                                    <i class="bi bi-kanban text-white"></i>
                                </div>
                                <div class="function-title">我的项目</div>
                            </a>
                            <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="function-card">
                                <div class="function-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                                    <i class="bi bi-calendar-check text-white"></i>
                                </div>
                                <div class="function-title">我的活动</div>
                            </a>
                            <a href="${pageContext.request.contextPath}/group/my-groups" class="function-card">
                                <div class="function-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);">
                                    <i class="bi bi-chat-dots text-white"></i>
                                </div>
                                <div class="function-title">我的群聊</div>
                            </a>
                            <a href="${pageContext.request.contextPath}/resume?action=list" class="function-card">
                                <div class="function-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6);">
                                    <i class="bi bi-file-earmark-text text-white"></i>
                                </div>
                                <div class="function-title">我的简历</div>
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="info-card mb-4">
                    <div class="info-card-header">
                        <h3 class="info-card-title">
                            <i class="bi bi-person-vcard text-brand"></i>基本信息
                        </h3>
                    </div>
                    <div class="info-card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">用户名</div>
                                    <div class="info-value">${user.username}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">姓名</div>
                                    <div class="info-value">${user.name}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">角色</div>
                                    <div class="info-value">
                                        <c:choose>
                                            <c:when test="${user.role eq 'ADMIN'}">管理员</c:when>
                                            <c:when test="${user.role eq 'MEMBER'}">成员</c:when>
                                            <c:when test="${user.role eq 'TEACHER'}">教师</c:when>
                                            <c:otherwise>${user.role}</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">电话</div>
                                    <div class="info-value">${user.phone}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">邮箱</div>
                                    <div class="info-value">${user.email}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">创建时间</div>
                                    <div class="info-value">${user.createdAt}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="info-card">
                    <div class="info-card-header">
                        <h3 class="info-card-title">
                            <i class="bi bi-file-earmark-person text-brand"></i>详细资料
                        </h3>
                    </div>
                    <div class="info-card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">生日</div>
                                    <div class="info-value">${birthdayDisplay}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">学号</div>
                                    <div class="info-value">${memberProfile.studentId}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">专业</div>
                                    <div class="info-value">${memberProfile.major}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">年级</div>
                                    <div class="info-value">${memberProfile.grade}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">GitHub</div>
                                    <div class="info-value">${memberProfile.github}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">博客</div>
                                    <div class="info-value">${memberProfile.blog}</div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="info-item">
                                    <div class="info-label">个人简介</div>
                                    <div class="info-value">${memberProfile.introduction}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">最后更新</div>
                                    <div class="info-value">${memberProfile.updatedAt}</div>
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