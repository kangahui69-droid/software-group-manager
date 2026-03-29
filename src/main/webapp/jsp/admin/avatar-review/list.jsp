<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    model.User currentUser = (model.User) session.getAttribute("user");
    if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
%>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="头像审核 - 管理后台" />
</jsp:include>

<style>
.avatar-review-card {
    transition: all 0.3s ease;
}
.avatar-review-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}
.avatar-comparison {
    display: flex;
    gap: 20px;
    justify-content: center;
    align-items: center;
}
.avatar-box {
    text-align: center;
}
.avatar-box .label {
    font-size: 12px;
    color: #6c757d;
    margin-top: 8px;
}
</style>

<div class="container-xl py-4">
    <div class="row">
        <div class="col-12">
            <div class="page-header d-flex align-items-center mb-4">
                <div class="d-flex align-items-center">
                    <a href="${pageContext.request.contextPath}/admin/index.jsp" class="btn btn-outline-secondary me-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M10 18l-6 -6l6 -6"></path><path d="M10 18l6 -6"></path></svg>
                    </a>
                    <div>
                        <h1 class="page-title mb-0">头像审核</h1>
                        <div class="text-muted small">审核用户上传的新头像</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty pendingProfiles}">
            <div class="card">
                <div class="card-body text-center py-5">
                    <div class="empty-img mb-3">
                        <i class="bi bi-check-circle text-success" style="font-size: 64px;"></i>
                    </div>
                    <h3 class="mb-2">暂无待审核头像</h3>
                    <p class="text-muted">所有用户头像都已审核完成</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-info mb-3">
                <i class="bi bi-info-circle me-2"></i>
                当前有 <strong>${pendingProfiles.size()}</strong> 个头像待审核
            </div>
            <div class="row">
                <c:forEach var="profile" items="${pendingProfiles}">
                    <div class="col-md-6 col-lg-4 mb-4">
                        <div class="card avatar-review-card">
                            <div class="card-header">
                                <h3 class="card-title">用户头像审核</h3>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <table class="table table-sm">
                                        <tr>
                                            <td class="text-muted" style="width: 80px;">用户名</td>
                                            <td><strong>${profile.userId}</strong></td>
                                        </tr>
                                        <tr>
                                            <td class="text-muted">学号</td>
                                            <td>${profile.studentId}</td>
                                        </tr>
                                        <tr>
                                            <td class="text-muted">专业</td>
                                            <td>${profile.major}</td>
                                        </tr>
                                        <tr>
                                            <td class="text-muted">年级</td>
                                            <td>${profile.grade}</td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div class="avatar-comparison mb-4">
                                    <div class="avatar-box">
                                        <div class="avatar-wrapper rounded-circle overflow-hidden mx-auto" 
                                             style="width: 100px; height: 100px; background: #f0f0f0;">
                                            <c:choose>
                                                <c:when test="${profile.avatarFileId != null}">
                                                    <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.avatarFileId}" 
                                                         alt="当前头像" style="width: 100%; height: 100%; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-flex align-items-center justify-content-center h-100 bg-primary text-white" 
                                                         style="font-size: 36px; font-weight: bold;">
                                                        ${profile.studentId.charAt(0)}
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="label">当前头像</div>
                                    </div>
                                    
                                    <div class="avatar-arrow">
                                        <i class="bi bi-arrow-right" style="font-size: 24px; color: #6c757d;"></i>
                                    </div>
                                    
                                    <div class="avatar-box">
                                        <div class="avatar-wrapper rounded-circle overflow-hidden mx-auto" 
                                             style="width: 100px; height: 100px; background: #f0f0f0;">
                                            <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.pendingAvatarFileId}" 
                                                 alt="新头像" style="width: 100%; height: 100%; object-fit: cover;"
                                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                            <div class="d-flex align-items-center justify-content-center h-100 bg-success text-white" 
                                                 style="font-size: 36px; font-weight: bold; display: none;">
                                                新
                                            </div>
                                        </div>
                                        <div class="label text-success">新头像</div>
                                    </div>
                                </div>
                                
                                <div class="d-flex gap-2">
                                    <a href="${pageContext.request.contextPath}/admin/api/avatar-review/approve/${profile.userId}" 
                                       class="btn btn-success flex-fill">
                                        <i class="bi bi-check-lg me-1"></i>通过
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/api/avatar-review/reject/${profile.userId}" 
                                       class="btn btn-outline-danger flex-fill"
                                       onclick="return confirm('确定要拒绝此头像吗？');">
                                        <i class="bi bi-x-lg me-1"></i>拒绝
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />
