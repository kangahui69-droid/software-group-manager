<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="model.User" %>
<%@ page import="model.AdminProfile" %>
<%@ page import="dao.AdminProfileDAO" %>
<% Object userObj=session.getAttribute("user"); if (userObj==null || !"ADMIN".equals(((User)userObj).getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); } %>
<% User user=(User) session.getAttribute("user"); %>
<% 
    AdminProfile adminProfile = (AdminProfile) session.getAttribute("adminProfile");
    if (adminProfile == null) {
        AdminProfileDAO adminProfileDAO = new AdminProfileDAO();
        adminProfile = adminProfileDAO.findByUserId(user.getId());
        if (adminProfile != null) {
            session.setAttribute("adminProfile", adminProfile);
        }
    }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
<jsp:param name="title" value="编辑资料" />
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
    --primary-600: #2563eb;
}
body {
    font-family: var(--font-ui);
    background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
}
.page-header {
    background: transparent;
    padding: 24px 0;
}
.page-pretitle {
    font-family: var(--font-ui);
    font-size: 13px;
    font-weight: 500;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.5px;
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
.card-title {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
}
.form-label {
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 8px;
}
.input-group {
    border-radius: var(--radius-comfortable);
    overflow: hidden;
}
.input-group-text {
    background: var(--primary-light);
    border: 1px solid var(--border-gray);
    border-right: none;
    padding: 10px 14px;
}
.input-group-text svg {
    color: var(--brand-blue);
}
.form-control {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 10px 14px;
    font-size: 14px;
    transition: all 0.2s ease;
}
.form-control:focus {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 85, 240, 0.1);
}
textarea.form-control {
    resize: vertical;
    min-height: 100px;
}
.btn-primary {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 10px 20px;
    font-weight: 600;
    font-size: 14px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-primary:hover {
    transform: translateY(-1px);
    box-shadow: var(--shadow-brand-purple);
}
.btn-secondary {
    background: var(--bg-white);
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 10px 20px;
    font-weight: 500;
    font-size: 14px;
    color: var(--text-muted);
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-secondary:hover {
    border-color: var(--brand-blue);
    color: var(--brand-blue);
}
.avatar-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid var(--primary-light);
}
.section-title {
    font-family: var(--font-display);
    font-size: 16px;
    font-weight: 600;
    color: var(--text-dark);
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 2px solid var(--primary-light);
}
.tips-card {
    background: var(--primary-light);
    border: 1px solid rgba(20, 85, 240, 0.1);
    border-radius: var(--radius-standard);
}
.tips-card .card-body {
    padding: 20px;
}
.list-group-item {
    border: none;
    padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
}
.list-group-item:last-child {
    border-bottom: none;
}
.list-group-item .icon {
    color: var(--brand-blue);
}
.alert-custom {
    border-radius: var(--radius-comfortable);
    padding: 16px;
    margin-bottom: 20px;
}
.alert-success {
    background: #ecfdf5;
    border: 1px solid #d1fae5;
    color: #065f46;
}
.alert-danger {
    background: #fef2f2;
    border: 1px solid #fee2e2;
    color: #991b1b;
}
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <div class="page-pretitle">
                    个人设置
                </div>
                <h2 class="page-title">
                    编辑资料
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <c:if test="${not empty sessionScope.error}">
                <div class="col-12">
                    <div class="alert-custom alert-danger" role="alert">
                        <strong>操作失败</strong> ${sessionScope.error}
                    </div>
                </div>
                <% session.removeAttribute("error"); %>
            </c:if>
            <c:if test="${not empty sessionScope.success}">
                <div class="col-12">
                    <div class="alert-custom alert-success" role="alert">
                        <strong>操作成功</strong> ${sessionScope.success}
                    </div>
                </div>
                <% session.removeAttribute("success"); %>
            </c:if>
            <div class="row">
                <div class="col-lg-8">
                    <div class="card">
                        <div class="card-body">
                            <form id="profileForm" method="post"
                                action="${pageContext.request.contextPath}/admin/api/profile/update"
                                enctype="multipart/form-data">
                                <div class="mb-4">
                                    <h3 class="section-title">头像设置</h3>
                                    <div class="d-flex align-items-center">
                                        <div class="me-4">
                                            <img id="avatarPreview"
                                                src="${user.avatarFileId != null ? (pageContext.request.contextPath.concat('/file?action=view&id=')).concat(user.avatarFileId) : pageContext.request.contextPath.concat('/images/avatar/default-avatar.svg')}"
                                                alt="用户头像" class="avatar-preview">
                                        </div>
                                        <div id="avatarUploadContainer" style="display: none;">
                                            <label for="avatarUpload"
                                                class="btn btn-primary">上传新头像</label>
                                            <input type="file" id="avatarUpload" name="avatar"
                                                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                                                style="display: none;">
                                            <p class="text-muted mt-2" style="font-size: 13px;">支持JPG、PNG、GIF格式，大小不超过500KB</p>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <h3 class="section-title">基本信息</h3>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">用户名</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                                </span>
                                                <input type="text" class="form-control" name="username"
                                                    value="${user.username}" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">角色</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                                </span>
                                                <input type="text" class="form-control"
                                                    value="${user.role}" disabled>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <h3 class="section-title">个人信息</h3>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">姓名</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="name" value="${user != null ? user.name : ''}"
                                                    placeholder="请输入姓名" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">电话</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="phone"
                                                    value="${user != null ? user.phone : ''}"
                                                    placeholder="请输入电话" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">邮箱</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V7z"></path><polyline points="3 7 12 13 21 7"></polyline></svg>
                                                </span>
                                                <input type="email" class="form-control profile-field"
                                                    name="email"
                                                    value="${user != null ? user.email : ''}"
                                                    placeholder="请输入邮箱" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">职称</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="7" width="18" height="10" rx="2"></rect><path d="M7 7V5a2 2 0 0 1 2 -2h6a2 2 0 0 1 2 2v2"></path></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="title"
                                                    value="${adminProfile != null ? adminProfile.title : ''}"
                                                    placeholder="请输入职称" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">部门</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="department"
                                                    value="${adminProfile != null ? adminProfile.department : ''}"
                                                    placeholder="请输入部门" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">学历</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c0 1.657 3.134 3 7 3s7-1.343 7-3v-5"></path></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="education"
                                                    value="${adminProfile != null ? adminProfile.education : ''}"
                                                    placeholder="请输入学历" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-12 mb-3">
                                            <label class="form-label">研究领域</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 1 1 7.072 0l-.548.547A3.374 3.374 0 0 0 14 18.469V19a2 2 0 1 1-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path></svg>
                                                </span>
                                                <input type="text" class="form-control profile-field"
                                                    name="researchArea"
                                                    value="${adminProfile != null ? adminProfile.researchArea : ''}"
                                                    placeholder="请输入研究领域" disabled>
                                            </div>
                                        </div>
                                        <div class="col-md-12 mb-3">
                                            <label class="form-label">个人简介</label>
                                            <div class="input-group">
                                                <span class="input-group-text" style="align-self: flex-start; padding-top: 12px;">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                                                </span>
                                                <textarea class="form-control profile-field" name="bio"
                                                    rows="4" placeholder="请输入个人简介"
                                                    disabled>${adminProfile != null ? adminProfile.bio : ''}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-end">
                                    <div id="editProfileContainer">
                                        <button type="button" id="editProfileBtn"
                                            class="btn btn-primary">编辑资料</button>
                                    </div>
                                    <div id="formButtons" style="display: none;">
                                        <button type="button" id="cancelEditBtn"
                                            class="btn btn-secondary me-2">取消</button>
                                        <button type="submit" class="btn btn-primary">保存修改</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card tips-card">
                        <div class="card-body">
                            <h3 class="card-title">修改提示</h3>
                            <ul class="list-group list-group-flush">
                                <li class="list-group-item d-flex align-items-center">
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                        class="icon me-2" width="18" height="18"
                                        viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                        fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <circle cx="12" cy="12" r="9"></circle>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                        <polyline points="11 12 12 12 12 16 13 16"></polyline>
                                    </svg>
                                    用户名不可修改
                                </li>
                                <li class="list-group-item d-flex align-items-center">
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                        class="icon me-2" width="18" height="18"
                                        viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                        fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <circle cx="12" cy="12" r="9"></circle>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                        <polyline points="11 12 12 12 12 16 13 16"></polyline>
                                    </svg>
                                    头像大小不能超过500KB
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
<script>
function showNotification(message, type) {
    const existingNotification = document.getElementById('notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.id = 'notification';
    notification.className = 'alert-custom alert-' + (type === 'success' ? 'success' : 'danger');
    notification.setAttribute('role', 'alert');
    notification.style.marginBottom = '20px';
    
    const title = type === 'success' ? '操作成功' : '操作失败';
    notification.innerHTML = '<strong>' + title + '</strong> ' + message;

    const formContainer = document.querySelector('.container-xl .row');
    if (formContainer) {
        formContainer.insertBefore(notification, formContainer.firstChild);
    }
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('formButtons').style.display = 'flex';

    const profileFields = document.querySelectorAll('.profile-field');
    profileFields.forEach(function (field) {
        field.removeAttribute('disabled');
    });

    document.getElementById('avatarUploadContainer').style.display = 'block';
    document.getElementById('editProfileContainer').style.display = 'none';

    const cancelEditBtn = document.getElementById('cancelEditBtn');
    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', function () {
            window.location.href = '${pageContext.request.contextPath}/admin/profile.jsp';
        });
    }

    const avatarUpload = document.getElementById('avatarUpload');
    if (avatarUpload) {
        avatarUpload.addEventListener('change', function () {
            handleAvatarUpload(this);
        });
    }

    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', function (e) {
            e.preventDefault();

            const formData = new FormData(this);

            const submitBtn = this.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerText;
            submitBtn.disabled = true;
            submitBtn.innerText = '正在提交...';

            fetch(this.action, {
                method: 'POST',
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('网络响应错误: ' + response.status);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        showNotification('资料更新成功！', 'success');
                        setTimeout(() => {
                            window.location.href = '${pageContext.request.contextPath}/admin/profile.jsp';
                        }, 1000);
                    } else {
                        showNotification('更新失败: ' + (data.message || '未知错误'), 'error');
                    }
                })
                .catch(error => {
                    showNotification('提交过程中发生错误，请重试', 'error');
                })
                .finally(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerText = originalBtnText;
                });
        });
    }
});

function handleAvatarUpload(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];

        if (file.size > 500 * 1024) {
            showNotification('文件大小不能超过500KB', 'error');
            input.value = '';
            return;
        }

        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            showNotification('只支持JPG、PNG、GIF、WebP格式的图片', 'error');
            input.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('avatarPreview').src = e.target.result;
        };
        reader.onerror = function (e) {
            showNotification('读取文件失败，请重试', 'error');
        };
        reader.readAsDataURL(file);
    }
}
</script>