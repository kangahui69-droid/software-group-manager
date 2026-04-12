<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dao.AdminProfileDAO" %>
<%@ page import="model.AdminProfile" %>
<%
    model.User user = (model.User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    AdminProfile adminProfile = (AdminProfile) session.getAttribute("adminProfile");
    if (adminProfile == null && user != null) {
        AdminProfileDAO adminProfileDAO = new AdminProfileDAO();
        adminProfile = adminProfileDAO.findByUserId(user.getId());
        if (adminProfile != null) {
            session.setAttribute("adminProfile", adminProfile);
        }
    }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="管理员个人中心" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-lg-8">
            <!-- 基本信息 -->
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">基本信息</h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">用户名</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${user.username}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">角色</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${user.role}'/>" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 个人信息 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3 class="card-title">个人信息</h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">姓名</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${user.name}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">电话</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${user.phone}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">邮箱</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V7z"></path><polyline points="3 7 12 13 21 7"></polyline></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${user.email}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">职称</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="7" width="18" height="10" rx="2"></rect><path d="M7 7V5a2 2 0 0 1 2 -2h6a2 2 0 0 1 2 2v2"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${adminProfile.title}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">部门</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${adminProfile.department}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">学历</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c0 1.657 3.134 3 7 3s7-1.343 7-3v-5"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${adminProfile.education}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-12 mb-3">
                            <label class="form-label">研究领域</label>
                            <div class="input-group">
                                <span class="input-group-text">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 1 1 7.072 0l-.548.547A3.374 3.374 0 0 0 14 18.469V19a2 2 0 1 1-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path></svg>
                                </span>
                                <input type="text" class="form-control" value="<c:out value='${adminProfile.researchArea}'/>" readonly>
                            </div>
                        </div>
                        <div class="col-md-12 mb-3">
                            <label class="form-label">个人简介</label>
                            <div class="input-group">
                                <span class="input-group-text" style="align-self: flex-start;">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                                </span>
                                <textarea class="form-control" rows="4" readonly><c:out value='${adminProfile.bio}'/></textarea>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 操作按钮 -->
            <div class="mt-4">
                <a href="${pageContext.request.contextPath}/admin/edit-profile.jsp" class="btn btn-primary">编辑资料</a>
                <a href="${pageContext.request.contextPath}/admin/password-change.jsp" class="btn btn-outline-secondary">修改密码</a>
            </div>
        </div>

        <!-- 右侧系统管理功能 -->
        <div class="col-lg-4">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">系统管理</h3>
                </div>
                <div class="card-body">
                    <div class="row row-cards">
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/news?action=manage" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-newspaper h2 text-blue"></i>
                                    </div>
                                    <div class="text-truncate">新闻管理</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/project/list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-kanban h2 text-purple"></i>
                                    </div>
                                    <div class="text-truncate">项目管理</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-people h2 text-cyan"></i>
                                    </div>
                                    <div class="text-truncate">成员管理</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/activity/list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-calendar-check h2 text-green"></i>
                                    </div>
                                    <div class="text-truncate">活动管理</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/award/list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-trophy h2 text-yellow"></i>
                                    </div>
                                    <div class="text-truncate">奖项管理</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/admin/log/list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-journal-text h2 text-secondary"></i>
                                    </div>
                                    <div class="text-truncate">操作日志</div>
                                </div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/group/admin?action=list" class="card card-link">
                                <div class="card-body p-3 text-center">
                                    <div class="mb-2">
                                        <i class="bi bi-chat-dots h2 text-orange"></i>
                                    </div>
                                    <div class="text-truncate">群聊管理</div>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
