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

.avatar-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid var(--primary-light);
}

.section-title {
    font-family: var(--font-display);
    font-size: 1rem;
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
    background: rgba(16, 185, 129, 0.1);
    border: 1px solid rgba(16, 185, 129, 0.2);
    color: #059669;
}

.alert-danger {
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.2);
    color: #dc2626;
}
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-pencil me-2"></i>编辑资料
            </h1>
            <p class="admin-hero-subtitle">修改您的个人信息和资料</p>
        </div>

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
                    <div class="card-design">
                        <div class="card-body-design">
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
                                                class="btn btn-brand">上传新头像</label>
                                            <input type="file" id="avatarUpload" name="avatar"
                                                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp">
                                            <p class="text-muted mt-2" style="font-size: 13px;">支持JPG、PNG、GIF格式，大小不超过500KB</p>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <h3 class="section-title">基本信息</h3>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">用户名</label>
                                            <input type="text" class="input-design" name="username"
                                                value="${user.username}" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">角色</label>
                                            <input type="text" class="input-design"
                                                value="${user.role}" disabled>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-4">
                                    <h3 class="section-title">个人信息</h3>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">姓名</label>
                                            <input type="text" class="input-design profile-field"
                                                name="name" value="${user != null ? user.name : ''}"
                                                placeholder="请输入姓名" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">电话</label>
                                            <input type="text" class="input-design profile-field"
                                                name="phone"
                                                value="${user != null ? user.phone : ''}"
                                                placeholder="请输入电话" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">邮箱</label>
                                            <input type="email" class="input-design profile-field"
                                                name="email"
                                                value="${user != null ? user.email : ''}"
                                                placeholder="请输入邮箱" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">职称</label>
                                            <input type="text" class="input-design profile-field"
                                                name="title"
                                                value="${adminProfile != null ? adminProfile.title : ''}"
                                                placeholder="请输入职称" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">部门</label>
                                            <input type="text" class="input-design profile-field"
                                                name="department"
                                                value="${adminProfile != null ? adminProfile.department : ''}"
                                                placeholder="请输入部门" disabled>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label-design">学历</label>
                                            <input type="text" class="input-design profile-field"
                                                name="education"
                                                value="${adminProfile != null ? adminProfile.education : ''}"
                                                placeholder="请输入学历" disabled>
                                        </div>
                                        <div class="col-md-12 mb-3">
                                            <label class="form-label-design">研究领域</label>
                                            <input type="text" class="input-design profile-field"
                                                name="researchArea"
                                                value="${adminProfile != null ? adminProfile.researchArea : ''}"
                                                placeholder="请输入研究领域" disabled>
                                        </div>
                                        <div class="col-md-12 mb-3">
                                            <label class="form-label-design">个人简介</label>
                                            <textarea class="input-design profile-field" name="bio"
                                                rows="4" placeholder="请输入个人简介"
                                                disabled>${adminProfile != null ? adminProfile.bio : ''}</textarea>
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-end">
                                    <div id="editProfileContainer">
                                        <button type="button" id="editProfileBtn"
                                            class="btn btn-brand">编辑资料</button>
                                    </div>
                                    <div id="formButtons" style="display: none;">
                                        <button type="button" id="cancelEditBtn"
                                            class="btn btn-outline-brand me-2">取消</button>
                                        <button type="submit" class="btn btn-brand">保存修改</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card-design tips-card">
                        <div class="card-body-design">
                            <h3 class="card-title-design">修改提示</h3>
                            <ul class="list-group list-group-flush">
                                <li class="list-group-item d-flex align-items-center">
                                    <i class="bi bi-info-circle me-2" style="color: var(--brand-blue);"></i>
                                    用户名不可修改
                                </li>
                                <li class="list-group-item d-flex align-items-center">
                                    <i class="bi bi-info-circle me-2" style="color: var(--brand-blue);"></i>
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