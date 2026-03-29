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
    // Format birthday for display
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

<c:if test="${param.success == '1'}">
    <div class="alert alert-success alert-dismissible" style="margin: 20px;">
        <i class="bi bi-check-circle me-2"></i>资料更新成功！
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="container-xl">
    <div class="row">
        <div class="col-lg-4">
            <!-- 个人信息卡片 -->
            <div class="card">
                <div class="card-body">
                    <div class="d-flex flex-column align-items-center text-center">
                        <div class="mb-4">
                            <!-- 头像显示区域 -->
                            <c:choose>
                                <%-- 情况1：有头像文件 --%>
                                <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                                    <img id="avatarPreview"
                                         src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}"
                                         alt="用户头像"
                                         class="rounded-circle"
                                         width="150"
                                         height="150"
                                         onerror="this.style.display='none'; document.getElementById('avatarInitial').style.display='flex';">
                                    <div id="avatarInitial"
                                         class="rounded-circle d-flex align-items-center justify-content-center bg-primary text-white"
                                         style="width: 150px; height: 150px; font-size: 60px; font-weight: bold; display: none;">
                                        ${not empty user.name ? user.name.charAt(0) : '用'}
                                    </div>
                                </c:when>
                                <%-- 情况2：无头像，显示姓名首字 --%>
                                <c:otherwise>
                                    <div id="avatarPreview"
                                         class="rounded-circle d-flex align-items-center justify-content-center bg-primary text-white"
                                         style="width: 150px; height: 150px; font-size: 60px; font-weight: bold;">
                                        ${not empty user.name ? user.name.charAt(0) : '用'}
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="mt-3">
                            <h4>${user.username}</h4>
                            <p class="text-muted font-size-sm">${user.role}</p>
                            <p class="text-muted font-size-sm">
                                <c:choose>
                                    <c:when test="${user.userType eq 'TEACHER'}">
                                        教师
                                    </c:when>
                                    <c:when test="${user.userType eq 'STUDENT'}">
                                        学生
                                    </c:when>
                                    <c:otherwise>
                                        其他
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <div class="mt-3">
                                <a href="${pageContext.request.contextPath}/member/edit-profile.jsp" class="btn btn-primary">编辑资料</a>
                                <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn btn-outline-secondary">修改密码</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 个人功能入口 -->
            <div class="card mt-4">
                <div class="card-body">
                    <h3 class="card-title">我的功能</h3>
                    <div class="row row-cards">
                        <div class="col-6 col-md-6">
                            <a href="${pageContext.request.contextPath}/award?action=list" class="card card-link">
                                <div class="card-body p-4 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-trophy h2 text-yellow"></i>
                                    </div>
                                    <div class="text-truncate">我的奖项</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6 col-md-6">
                            <a href="${pageContext.request.contextPath}/project?action=myProjects" class="card card-link">
                                <div class="card-body p-4 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-kanban h2 text-purple"></i>
                                    </div>
                                    <div class="text-truncate">我的项目</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6 col-md-6">
                            <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="card card-link">
                                <div class="card-body p-4 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-calendar-check h2 text-green"></i>
                                    </div>
                                    <div class="text-truncate">我的活动</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6 col-md-6">
                            <a href="${pageContext.request.contextPath}/resume?action=list" class="card card-link">
                                <div class="card-body p-4 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-file-earmark-text h2 text-blue"></i>
                                    </div>
                                    <div class="text-truncate">我的简历</div>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="col-lg-8">
            <!-- 详细信息 -->
            <div class="card">
                <div class="card-body">
                    <h3 class="mb-4">基本信息</h3>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">用户名</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.username}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">角色</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.role}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">姓名</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.name}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">电话</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.phone}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">邮箱</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V7z"></path><polyline points="3 7 12 13 21 7"></polyline></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.email}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 详细资料 -->
            <div class="card mt-4">
                <div class="card-body">
                    <h3 class="mb-4">详细资料</h3>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">生日</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                </span>
                                <input type="text" class="form-control" value="${birthdayDisplay}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">姓名</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.name}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">学号</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="4" width="16" height="16" rx="2"></rect><path d="M9 9h6v6H9z"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.studentId}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">专业</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.major}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">年级</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.grade}" readonly>
                            </div>
                        </div>
                        <div class="col-md-12 mb-3">
                            <label class="form-label">个人简介</label>
                            <div class="input-group">
                                <span class="input-group-text" style="align-self: flex-start;">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>
                                </span>
                                <textarea class="form-control" rows="4" readonly><c:out value="${memberProfile.introduction}" default=""/></textarea>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">GitHub</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.3c0 -1.1 -0.4 -2.2 -1.2 -3.1a4 4 0 0 0 -5.8 0c-0.8 0.9 -1.2 2 -1.2 3.1V21"></path><path d="M12 22c5.5 0 10 -4 10 -10c0 -2.2 -0.7 -4.3 -2 -6.1"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.github}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">博客</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="16" y2="12"></line></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.blog}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 创建时间 -->
            <div class="card mt-4">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">创建时间</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                </span>
                                <input type="text" class="form-control" value="${user.createdAt}" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">最后更新</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M4 20h4l10.5 -10.5a1.5 1.5 0 0 0 4 -4l-4.5 -4.5a1.5 1.5 0 0 0 -4 4l-10.5 10.5v4"></path><line x1="13.5" y1="6.5" x2="17.5" y2="10.5"></line></svg>
                                </span>
                                <input type="text" class="form-control" value="${memberProfile.updatedAt}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
