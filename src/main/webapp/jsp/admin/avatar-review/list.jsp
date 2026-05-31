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
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
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

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-body-design {
        padding: 0;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
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

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero mb-4">
            <h1 class="admin-hero-title">
                <i class="bi bi-person-badge me-2"></i>头像审核
            </h1>
            <p class="admin-hero-subtitle">审核用户上传的新头像</p>
        </div>

        <c:choose>
            <c:when test="${empty pendingProfiles}">
                <div class="card-design">
                    <div class="card-body-design text-center py-5">
                        <div class="empty-img mb-3">
                            <i class="bi bi-check-circle" style="font-size: 64px; color: #059669;"></i>
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
                            <div class="card-design">
                                <div class="card-header-design">
                                    <h3 class="card-title-design">用户头像审核</h3>
                                </div>
                                <div class="card-body-design">
                                    <div class="mb-3">
                                        <table class="table-design">
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
                                    
                                    <div class="avatar-comparison mb-4" style="display: flex; gap: 20px; justify-content: center; align-items: center;">
                                        <div class="avatar-box" style="text-align: center;">
                                            <div class="avatar-wrapper rounded-circle overflow-hidden mx-auto" 
                                                 style="width: 100px; height: 100px; background: #f0f0f0;">
                                                <c:choose>
                                                    <c:when test="${profile.avatarFileId != null}">
                                                        <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.avatarFileId}" 
                                                             alt="当前头像" style="width: 100%; height: 100%; object-fit: cover;">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="d-flex align-items-center justify-content-center h-100" 
                                                             style="background: rgba(20, 86, 240, 0.1); color: #1456f0; font-size: 36px; font-weight: bold;">
                                                            ${profile.studentId.charAt(0)}
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="label" style="font-size: 12px; color: #6c757d; margin-top: 8px;">当前头像</div>
                                        </div>
                                        
                                        <div class="avatar-arrow">
                                            <i class="bi bi-arrow-right" style="font-size: 24px; color: #6c757d;"></i>
                                        </div>
                                        
                                        <div class="avatar-box" style="text-align: center;">
                                            <div class="avatar-wrapper rounded-circle overflow-hidden mx-auto" 
                                                 style="width: 100px; height: 100px; background: #f0f0f0;">
                                                <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.pendingAvatarFileId}" 
                                                     alt="新头像" style="width: 100%; height: 100%; object-fit: cover;"
                                                     onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                                <div class="d-flex align-items-center justify-content-center h-100" 
                                                     style="background: rgba(16, 185, 129, 0.1); color: #059669; font-size: 36px; font-weight: bold; display: none;">
                                                    新
                                                </div>
                                            </div>
                                            <div class="label" style="font-size: 12px; color: #059669; margin-top: 8px;">新头像</div>
                                        </div>
                                    </div>
                                    
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/admin/api/avatar-review/approve/${profile.userId}" 
                                           class="btn-success-design flex-fill">
                                            <i class="bi bi-check-lg me-1"></i>通过
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/api/avatar-review/reject/${profile.userId}" 
                                           class="btn-outline-danger flex-fill"
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
</div>

<jsp:include page="../../common/layout_bottom.jsp" />