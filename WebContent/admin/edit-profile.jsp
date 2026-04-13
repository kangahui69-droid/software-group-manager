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
    .page-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .page-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .page-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .form-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        padding: 32px;
        box-shadow: var(--shadow-brand-purple);
        border: none;
    }

    .form-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
    }

    .input-group-design {
        display: flex;
        align-items: center;
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        overflow: hidden;
        transition: all 0.3s ease;
    }

    .input-group-design:focus-within {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    }

    .input-group-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--bg-light-gray);
        color: var(--text-muted);
        flex-shrink: 0;
    }

    .input-design {
        flex: 1;
        border: none;
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        outline: none;
    }

    .input-design:disabled {
        background: var(--bg-light-gray);
        color: var(--text-muted);
    }

    textarea.input-design {
        min-height: 120px;
        resize: vertical;
        align-self: flex-start;
    }

    .btn-primary-design {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 600;
        font-size: 0.94rem;
        border: none;
        transition: all 0.3s ease;
    }

    .btn-primary-design:hover {
        background: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-secondary-design {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 500;
        font-size: 0.94rem;
        border: 1px solid var(--border-gray);
        transition: all 0.3s ease;
        text-decoration: none;
    }

    .btn-secondary-design:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }

    .tip-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        height: 100%;
    }

    .tip-card-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .tip-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .tip-item:last-child {
        border-bottom: none;
    }

    .tip-icon {
        width: 32px;
        height: 32px;
        border-radius: var(--radius-standard);
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        font-size: 0.875rem;
    }

    .tip-text {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-secondary);
        line-height: 1.5;
    }

    .avatar-section {
        display: flex;
        align-items: center;
        gap: 24px;
        margin-bottom: 32px;
        padding-bottom: 32px;
        border-bottom: 1px solid var(--border-light);
    }

    .avatar-preview {
        width: 120px;
        height: 120px;
        border-radius: var(--radius-comfortable);
        object-fit: cover;
        border: 4px solid var(--bg-light-gray);
    }

    .avatar-fallback {
        width: 120px;
        height: 120px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 3rem;
        font-weight: 600;
    }

    .avatar-upload-info {
        flex: 1;
    }

    .avatar-upload-info h4 {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 12px;
    }

    .avatar-upload-info p {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-muted);
        margin-bottom: 16px;
    }

    .btn-upload {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.875rem;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-upload:hover {
        background: var(--primary-600);
        color: white;
    }

    .alert-design {
        border-radius: var(--radius-standard);
        padding: 16px 20px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .alert-danger-design {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
        color: #dc2626;
    }

    .alert-success-design {
        background: rgba(16, 185, 129, 0.1);
        border: 1px solid rgba(16, 185, 129, 0.2);
        color: #059669;
    }

    @media (max-width: 768px) {
        .page-hero {
            padding: 24px;
        }

        .page-hero-title {
            font-size: 1.5rem;
        }

        .form-card {
            padding: 24px;
        }

        .avatar-section {
            flex-direction: column;
            text-align: center;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="page-hero">
            <h1 class="page-hero-title">
                <i class="bi bi-pencil-square me-2"></i>编辑资料
            </h1>
            <p class="page-hero-subtitle">更新您的个人信息和头像</p>
        </div>

        <c:if test="${not empty sessionScope.error}">
            <div class="alert-design alert-danger-design" role="alert">
                <i class="bi bi-exclamation-circle-fill"></i>
                <span>${sessionScope.error}</span>
            </div>
            <% session.removeAttribute("error"); %>
        </c:if>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert-design alert-success-design" role="alert">
                <i class="bi bi-check-circle-fill"></i>
                <span>${sessionScope.success}</span>
            </div>
            <% session.removeAttribute("success"); %>
        </c:if>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="form-card">
                    <form id="profileForm" method="post" action="${pageContext.request.contextPath}/admin/api/profile/update" enctype="multipart/form-data">
                        <div class="avatar-section">
                            <c:choose>
                                <c:when test="${user.avatarFileId != null}">
                                    <img id="avatarPreview" src="${pageContext.request.contextPath}/file?action=view&id=${user.avatarFileId}" alt="用户头像" class="avatar-preview">
                                </c:when>
                                <c:otherwise>
                                    <div class="avatar-fallback">${not empty user.name ? user.name.charAt(0) : user.username.substring(0,1)}</div>
                                </c:otherwise>
                            </c:choose>
                            <div class="avatar-upload-info">
                                <h4>头像设置</h4>
                                <p>支持JPG、PNG、GIF格式，大小不超过500KB</p>
                                <label for="avatarUpload" class="btn-upload">
                                    <i class="bi bi-upload"></i>上传新头像
                                </label>
                                <input type="file" id="avatarUpload" name="avatar" accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" style="display: none;">
                            </div>
                        </div>

                        <h3 class="form-card-title">
                            <i class="bi bi-person text-brand"></i>基本信息
                        </h3>
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label-design">用户名</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-person"></i>
                                    </div>
                                    <input type="text" class="input-design" name="username" value="${user.username}" disabled>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">角色</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-shield-check"></i>
                                    </div>
                                    <input type="text" class="input-design" value="${user.role}" disabled>
                                </div>
                            </div>
                        </div>

                        <h3 class="form-card-title">
                            <i class="bi bi-card-text text-brand"></i>个人信息
                        </h3>
                        <div class="row g-4">
                            <div class="col-md-6">
                                <label class="form-label-design">姓名</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-person-badge"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="name" value="${user != null ? user.name : ''}" placeholder="请输入姓名">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">电话</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-telephone"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="phone" value="${user != null ? user.phone : ''}" placeholder="请输入电话">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">邮箱</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-envelope"></i>
                                    </div>
                                    <input type="email" class="input-design profile-field" name="email" value="${user != null ? user.email : ''}" placeholder="请输入邮箱">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">职称</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-briefcase"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="title" value="${adminProfile != null ? adminProfile.title : ''}" placeholder="请输入职称">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">部门</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-building"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="department" value="${adminProfile != null ? adminProfile.department : ''}" placeholder="请输入部门">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label-design">学历</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-mortarboard"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="education" value="${adminProfile != null ? adminProfile.education : ''}" placeholder="请输入学历">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label-design">研究领域</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-lightbulb"></i>
                                    </div>
                                    <input type="text" class="input-design profile-field" name="researchArea" value="${adminProfile != null ? adminProfile.researchArea : ''}" placeholder="请输入研究领域">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label-design">个人简介</label>
                                <div class="input-group-design" style="align-items: flex-start;">
                                    <div class="input-group-icon" style="height: 120px;">
                                        <i class="bi bi-file-text" style="align-self: flex-start; margin-top: 8px;"></i>
                                    </div>
                                    <textarea class="input-design profile-field" name="bio" rows="4" placeholder="请输入个人简介" style="height: 120px;">${adminProfile != null ? adminProfile.bio : ''}</textarea>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-3 mt-4">
                            <a href="${pageContext.request.contextPath}/admin/profile.jsp" class="btn-secondary-design">取消</a>
                            <button type="submit" class="btn-primary-design">保存修改</button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="tip-card">
                    <h3 class="tip-card-title">
                        <i class="bi bi-info-circle text-brand"></i>修改提示
                    </h3>
                    <div class="tip-item">
                        <div class="tip-icon">
                            <i class="bi bi-check2"></i>
                        </div>
                        <div class="tip-text">用户名不可修改</div>
                    </div>
                    <div class="tip-item">
                        <div class="tip-icon">
                            <i class="bi bi-check2"></i>
                        </div>
                        <div class="tip-text">头像大小不能超过500KB</div>
                    </div>
                    <div class="tip-item">
                        <div class="tip-icon">
                            <i class="bi bi-check2"></i>
                        </div>
                        <div class="tip-text">请确保信息真实有效</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function showNotification(message, type) {
        const existingNotification = document.getElementById('notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        const notification = document.createElement('div');
        notification.id = 'notification';
        notification.className = 'alert-design ' + (type === 'success' ? 'alert-success-design' : 'alert-danger-design');
        notification.setAttribute('role', 'alert');
        
        const icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-circle-fill';
        
        notification.innerHTML = '<i class="bi ' + icon + '"></i><span>' + message + '</span>';

        const formContainer = document.querySelector('.container-xl');
        if (formContainer) {
            formContainer.insertBefore(notification, formContainer.firstChild.nextSibling);
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
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
                const avatarPreview = document.getElementById('avatarPreview');
                if (avatarPreview) {
                    avatarPreview.src = e.target.result;
                } else {
                    const fallback = document.querySelector('.avatar-fallback');
                    if (fallback) {
                        fallback.outerHTML = '<img id="avatarPreview" src="' + e.target.result + '" alt="用户头像" class="avatar-preview">';
                    }
                }
            };
            reader.onerror = function () {
                showNotification('读取文件失败，请重试', 'error');
            };
            reader.readAsDataURL(file);
        }
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />