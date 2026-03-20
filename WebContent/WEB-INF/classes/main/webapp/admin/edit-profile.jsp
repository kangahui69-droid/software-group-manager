<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ page import="model.User" %>
            <%@ page import="model.AdminProfile" %>
                <%@ page import="dao.AdminProfileDAO" %>
                    <% Object userObj=session.getAttribute("user"); if (userObj==null || !"ADMIN".equals(((User)
                    userObj).getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); } %>
                        <% User user=(User) session.getAttribute("user"); %>
                        <% 
                            // 直接在JSP中查询adminProfile数据
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
                                <!-- 错误提示 -->
                                <c:if test="${not empty sessionScope.error}">
                                    <div class="row">
                                        <div class="col-12">
                                            <div class="alert alert-danger alert-dismissible" role="alert">
                                                <svg xmlns="http://www.w3.org/2000/svg"
                                                    class="icon alert-icon" width="24"
                                                    height="24" viewBox="0 0 24 24" stroke-width="2"
                                                    stroke="currentColor" fill="none" stroke-linecap="round"
                                                    stroke-linejoin="round">
                                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                                    <circle cx="12" cy="12" r="9"></circle>
                                                    <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                                    <line x1="11" y1="12" x2="12" y2="12"></line>
                                                    <line x1="11" y1="16" x2="12" y2="16"></line>
                                                </svg>
                                                <div>
                                                    <h4 class="alert-title">操作失败</h4>
                                                    <div class="text-secondary">${sessionScope.error}</div>
                                                </div>
                                                <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                                            </div>
                                        </div>
                                    </div>
                                    <% session.removeAttribute("error"); %>
                                </c:if>
                                <!-- 成功提示 -->
                                <c:if test="${not empty sessionScope.success}">
                                    <div class="row">
                                        <div class="col-12">
                                            <div class="alert alert-success alert-dismissible" role="alert">
                                                <svg xmlns="http://www.w3.org/2000/svg"
                                                    class="icon alert-icon" width="24" height="24"
                                                    viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                                    fill="none" stroke-linecap="round" stroke-linejoin="round">
                                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                                    <path d="M5 12l5 5l10 -10"></path>
                                                </svg>
                                                <div>
                                                    <h4 class="alert-title">操作成功</h4>
                                                    <div class="text-secondary">${sessionScope.success}</div>
                                                </div>
                                                <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                                            </div>
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
                                                    <!-- 头像上传区域 -->
                                                    <div class="mb-4">
                                                        <h3 class="mb-3">头像设置</h3>
                                                        <div class="d-flex align-items-center">
                                                            <div class="me-4">
                                                                <img id="avatarPreview"
                                                                    src="${user.avatarFileId != null ? (pageContext.request.contextPath.concat('/file?action=view&id=')).concat(user.avatarFileId) : pageContext.request.contextPath.concat('/images/avatar/default-avatar.svg')}"
                                                                    alt="用户头像" class="rounded-circle" width="120"
                                                                    height="120">
                                                            </div>
                                                            <div id="avatarUploadContainer" style="display: none;">
                                                                <label for="avatarUpload"
                                                                    class="btn btn-primary">上传新头像</label>
                                                                <input type="file" id="avatarUpload" name="avatar"
                                                                    accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                                                                    style="display: none;">
                                                                <p class="text-muted mt-2">支持JPG、PNG、GIF格式，大小不超过500KB
                                                                </p>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <!-- 基本信息 -->
                                                    <div class="mb-4">
                                                        <h3 class="mb-3">基本信息</h3>
                                                        <div class="row">
                                                            <div class="col-md-6 mb-3">
                                                                <label class="form-label">用户名</label>
                                                                <div class="input-group">
                                                                    <span class="input-group-text">
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                                                    </span>
                                                                    <input type="text" class="form-control" name="username"
                                                                        value="${user.username}" disabled>
                                                                </div>
                                                            </div>
                                                            <div class="col-md-6 mb-3">
                                                                <label class="form-label">角色</label>
                                                                <div class="input-group">
                                                                    <span class="input-group-text">
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                                                    </span>
                                                                    <input type="text" class="form-control"
                                                                        value="${user.role}" disabled>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <!-- 管理员个人信息 -->
                                                    <div class="mb-4">
                                                        <h3 class="mb-3">个人信息</h3>
                                                        <div class="row">
                                                            <div class="col-md-6 mb-3">
                                                                <label class="form-label">姓名</label>
                                                                <div class="input-group">
                                                                    <span class="input-group-text">
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V7z"></path><polyline points="3 7 12 13 21 7"></polyline></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="7" width="18" height="10" rx="2"></rect><path d="M7 7V5a2 2 0 0 1 2 -2h6a2 2 0 0 1 2 2v2"></path></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c0 1.657 3.134 3 7 3s7-1.343 7-3v-5"></path></svg>
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
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 1 1 7.072 0l-.548.547A3.374 3.374 0 0 0 14 18.469V19a2 2 0 1 1-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path></svg>
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
                                                                    <span class="input-group-text" style="align-self: flex-start;">
                                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                                                                    </span>
                                                                    <textarea class="form-control profile-field" name="bio"
                                                                        rows="4" placeholder="请输入个人简介"
                                                                        disabled>${adminProfile != null ? adminProfile.bio : ''}</textarea>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <!-- 操作按钮 -->
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
                                        <div class="card">
                                            <div class="card-body">
                                                <h3 class="card-title">修改提示</h3>
                                                <ul class="list-group list-group-flush">
                                                    <li class="list-group-item">
                                                        <svg xmlns="http://www.w3.org/2000/svg"
                                                            class="icon me-2 text-info" width="20" height="20"
                                                            viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                                            fill="none" stroke-linecap="round" stroke-linejoin="round">
                                                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                                            <circle cx="12" cy="12" r="9"></circle>
                                                            <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                                            <polyline points="11 12 12 12 12 16 13 16"></polyline>
                                                        </svg>
                                                        用户名不可修改
                                                    </li>
                                                    <li class="list-group-item">
                                                        <svg xmlns="http://www.w3.org/2000/svg"
                                                            class="icon me-2 text-info" width="20" height="20"
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
                        // 页面加载开始
                        // console.log('=== Edit Profile Page Loading Started ===');
                        // console.log('Current timestamp:', new Date().toISOString());
                        // console.log('Current URL:', window.location.href);
                        // console.log('Document readyState:', document.readyState);

                        // 显示通知函数 - Tabler alert 样式，带关闭按钮
                        function showNotification(message, type) {
                            // 移除已存在的通知
                            const existingNotification = document.getElementById('notification');
                            if (existingNotification) {
                                existingNotification.remove();
                            }

                            // 创建通知元素 - 使用 Tabler 格式
                            const notification = document.createElement('div');
                            notification.id = 'notification';
                            notification.className = 'alert alert-' + (type === 'success' ? 'success' : 'danger') + ' alert-dismissible';
                            notification.setAttribute('role', 'alert');
                            notification.style.marginBottom = '20px';
                            
                            const icon = type === 'success' 
                                ? '<svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>'
                                : '<svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';

                            const title = type === 'success' ? '操作成功' : '操作失败';
                            
                            notification.innerHTML = icon + 
                                '<div><h4 class="alert-title">' + title + '</h4><div class="text-secondary">' + message + '</div></div>' +
                                '<a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>';

                            // 插入到表单最前面
                            const formContainer = document.querySelector('.container-xl .row');
                            if (formContainer) {
                                formContainer.insertBefore(notification, formContainer.firstChild);
                            }
                        }

                        // DOM加载完成
                        document.addEventListener('DOMContentLoaded', function () {
                            console.log('=== DOM Content Loaded ===');
                            console.log('Document readyState:', document.readyState);

                            // 检查页面元素
                            console.log('=== Page Elements Check ===');
                            console.log('Profile form exists:', document.getElementById('profileForm') !== null);
                            console.log('Avatar preview exists:', document.getElementById('avatarPreview') !== null);
                            console.log('Avatar upload exists:', document.getElementById('avatarUpload') !== null);
                            console.log('Edit profile button exists:', document.getElementById('editProfileBtn') !== null);
                            console.log('Form buttons exist:', document.getElementById('formButtons') !== null);

                            // 检查数据
                            console.log('=== Data Check ===');
                            console.log('User object:', '${user != null ? 'exists' : 'null'}');
                            console.log('AdminProfile object:', '${adminProfile != null ? 'exists' : 'null'}');
                            console.log('User username:', '${user != null ? user.username : 'null'}');
                            console.log('User role:', '${user != null ? user.role : 'null'}');
                            console.log('AdminProfile title:', '${adminProfile != null ? adminProfile.title : 'null'}');
                            console.log('AdminProfile department:', '${adminProfile != null ? adminProfile.department : 'null'}');
                            console.log('AdminProfile education:', '${adminProfile != null ? adminProfile.education : 'null'}');
                            console.log('AdminProfile researchArea:', '${adminProfile != null ? adminProfile.researchArea : 'null'}');
                            console.log('AdminProfile bio:', '${adminProfile != null ? adminProfile.bio : 'null'}');

                            // 确保表单按钮始终显示
                            document.getElementById('formButtons').style.display = 'flex';

                            // 确保个人信息字段始终可编辑
                            const profileFields = document.querySelectorAll('.profile-field');
                            profileFields.forEach(function (field) {
                                field.removeAttribute('disabled');
                            });

                            // 显示头像上传容器
                            document.getElementById('avatarUploadContainer').style.display = 'block';

                            // 隐藏编辑资料按钮容器
                            document.getElementById('editProfileContainer').style.display = 'none';

                            // 取消按钮点击事件 - 返回profile页面
                            const cancelEditBtn = document.getElementById('cancelEditBtn');
                            if (cancelEditBtn) {
                                cancelEditBtn.addEventListener('click', function () {
                                    window.location.href = '${pageContext.request.contextPath}/admin/profile.jsp';
                                });
                            }

                            // 头像上传处理
                            const avatarUpload = document.getElementById('avatarUpload');
                            if (avatarUpload) {
                                // console.log('=== Avatar Upload Handler Added ===');
                                avatarUpload.addEventListener('change', function () {
                                    handleAvatarUpload(this);
                                });
                            }

                            // 表单提交处理
                            const profileForm = document.getElementById('profileForm');
                            if (profileForm) {
                                // console.log('=== Form Submission Handler Added ===');
                                profileForm.addEventListener('submit', function (e) {
                                    e.preventDefault();
                                    // console.log('=== AJAX Form Submission Started ===');

                                    const formData = new FormData(this);

                                    // 显示提交中
                                    const submitBtn = this.querySelector('button[type="submit"]');
                                    const originalBtnText = submitBtn.innerText;
                                    submitBtn.disabled = true;
                                    submitBtn.innerText = '正在提交...';

                                    fetch(this.action, {
                                        method: 'POST',
                                        body: formData
                                    })
                                        .then(response => {
                                            // console.log('Response status:', response.status);
                                            if (!response.ok) {
                                                throw new Error('网络响应错误: ' + response.status);
                                            }
                                            return response.json();
                                        })
                                        .then(data => {
                                            // console.log('Response data:', data);
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
                                            // console.error('Error:', error);
                                            showNotification('提交过程中发生错误，请重试', 'error');
                                        })
                                        .finally(() => {
                                            submitBtn.disabled = false;
                                            submitBtn.innerText = originalBtnText;
                                        });
                                });
                            }
                        });

                        // 页面加载完成
                        window.addEventListener('load', function () {
                            // console.log('=== Page Loaded Completely ===');
                            // console.log('Document readyState:', document.readyState);
                        });

                        function handleAvatarUpload(input) {
                            // console.log('=== Avatar Upload ===');
                            // console.log('Input element:', input);
                            // console.log('Selected file:', input.files[0]);

                            if (input.files && input.files[0]) {
                                const file = input.files[0];
                                // console.log('File size:', (file.size / 1024).toFixed(2) + 'KB');
                                // console.log('File type:', file.type);

                                // 验证文件大小
                                if (file.size > 500 * 1024) {
                                    // console.log('Error: File size exceeds 500KB');
                                    showNotification('文件大小不能超过500KB', 'error');
                                    input.value = '';
                                    return;
                                }

                                // 验证文件类型
                                const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
                                if (!allowedTypes.includes(file.type)) {
                                    // console.log('Error: Invalid file type');
                                    showNotification('只支持JPG、PNG、GIF、WebP格式的图片', 'error');
                                    input.value = '';
                                    return;
                                }

                                // console.log('File validation passed, previewing...');

                                // 预览头像
                                const reader = new FileReader();
                                reader.onload = function (e) {
                                    // console.log('Avatar preview loaded successfully');
                                    document.getElementById('avatarPreview').src = e.target.result;
                                };
                                reader.onerror = function (e) {
                                    // console.error('Error reading file:', e);
                                    showNotification('读取文件失败，请重试', 'error');
                                };
                                reader.readAsDataURL(file);
                            }
                        }
                    </script>
                    <style>
                        @keyframes slideIn {
                            from {
                                transform: translateX(100%);
                                opacity: 0;
                            }
                            to {
                                transform: translateX(0);
                                opacity: 1;
                            }
                        }
                    </style>