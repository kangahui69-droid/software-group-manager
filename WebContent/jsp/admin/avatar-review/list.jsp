<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .btn-success { background-color: #10b981; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-success:hover { background-color: #059669; color: white; transform: translateY(-2px); }
    .btn-outline-danger { color: #ef4444; border: 1px solid #ef4444; background: transparent; border-radius: var(--radius-standard); padding: 11px 20px; transition: all 0.3s ease; }
    .btn-outline-danger:hover { background: #ef4444; color: white; }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .btn-back { background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 9px 18px; transition: all 0.3s ease; }
    .btn-back:hover { background: #e5e5e5; color: var(--text-dark); }
    .avatar-review-card { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); transition: all 0.3s ease; overflow: hidden; }
    .avatar-review-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .avatar-comparison { display: flex; gap: 20px; justify-content: center; align-items: center; }
    .avatar-box { text-align: center; }
    .avatar-box .label { font-size: 0.88rem; color: var(--text-muted); margin-top: 8px; font-weight: 500; }
    .avatar-wrapper { border-radius: 50%; overflow: hidden; }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .empty-state { text-align: center; padding: 64px 24px; }
    .empty-icon { font-size: 80px; color: #10b981; margin-bottom: 16px; }
    .info-table td { padding: 8px 0; }
    .info-table td:first-child { color: var(--text-muted); width: 80px; }
</style>

<div class="container-xl py-4">
    <div class="row mb-4">
        <div class="col-12">
            <div class="d-flex align-items-center">
                <a href="${pageContext.request.contextPath}/admin/index.jsp" class="btn btn-back me-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M10 18l-6 -6l6 -6"></path><path d="M10 18l6 -6"></path></svg>
                </a>
                <div>
                    <h1 class="page-title mb-0">头像审核</h1>
                    <div class="text-muted" style="font-size: 0.88rem; margin-top: 4px;">审核用户上传的新头像</div>
                </div>
            </div>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty pendingProfiles}">
            <div class="card-design">
                <div class="empty-state">
                    <div class="empty-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                    </div>
                    <h3 class="mb-2" style="font-family: var(--font-display); color: var(--text-dark);">暂无待审核头像</h3>
                    <p class="text-muted">所有用户头像都已审核完成</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-info mb-3" role="alert" style="border-radius: var(--radius-standard); background: #dbeafe; color: #1e40af; border: 1px solid #bfdbfe; padding: 16px;">
                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                当前有 <strong>${pendingProfiles.size()}</strong> 个头像待审核
            </div>
            <div class="row" style="gap: 20px;">
                <c:forEach var="profile" items="${pendingProfiles}">
                    <div class="col-md-6 col-lg-4">
                        <div class="avatar-review-card">
                            <div class="card-body p-4">
                                <table class="info-table mb-4">
                                    <tr>
                                        <td>用户名</td>
                                        <td><strong>${profile.userId}</strong></td>
                                    </tr>
                                    <tr>
                                        <td>学号</td>
                                        <td>${profile.studentId}</td>
                                    </tr>
                                    <tr>
                                        <td>专业</td>
                                        <td>${profile.major}</td>
                                    </tr>
                                    <tr>
                                        <td>年级</td>
                                        <td>${profile.grade}</td>
                                    </tr>
                                </table>
                                
                                <div class="avatar-comparison mb-4">
                                    <div class="avatar-box">
                                        <div class="avatar-wrapper mx-auto" 
                                             style="width: 100px; height: 100px; background: var(--bg-light-gray);">
                                            <c:choose>
                                                <c:when test="${profile.avatarFileId != null}">
                                                    <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.avatarFileId}" 
                                                         alt="当前头像" style="width: 100%; height: 100%; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-flex align-items-center justify-content-center h-100" style="background: var(--brand-blue); color: white; font-size: 36px; font-weight: bold;">
                                                        ${profile.studentId.charAt(0)}
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="label">当前头像</div>
                                    </div>
                                    
                                    <div class="avatar-arrow">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                                    </div>
                                    
                                    <div class="avatar-box">
                                        <div class="avatar-wrapper mx-auto" 
                                             style="width: 100px; height: 100px; background: var(--bg-light-gray);">
                                            <img src="${pageContext.request.contextPath}/file?action=view&id=${profile.pendingAvatarFileId}" 
                                                 alt="新头像" style="width: 100%; height: 100%; object-fit: cover;"
                                                 onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                            <div class="d-flex align-items-center justify-content-center h-100" style="background: #10b981; color: white; font-size: 36px; font-weight: bold; display: none;">
                                                新
                                            </div>
                                        </div>
                                        <div class="label" style="color: #10b981;">新头像</div>
                                    </div>
                                </div>
                                
                                <div class="d-flex gap-2">
                                    <a href="${pageContext.request.contextPath}/admin/api/avatar-review/approve/${profile.userId}" 
                                       class="btn btn-success flex-fill">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>
                                        通过
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/api/avatar-review/reject/${profile.userId}" 
                                       class="btn btn-outline-danger flex-fill"
                                       onclick="return confirm('确定要拒绝此头像吗？');">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                                        拒绝
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