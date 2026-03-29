<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="dao.MemberProfileDAO" %>
<%@ page import="model.MemberProfile" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%
    // 检查用户是否已登录
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

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
    String birthdayValue = "";
    Object birthdayObj = session.getAttribute("memberProfile");
    if (birthdayObj != null) {
        try {
            java.lang.reflect.Method getBirthday = birthdayObj.getClass().getMethod("getBirthday");
            Date birthday = (Date) getBirthday.invoke(birthdayObj);
            if (birthday != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                birthdayValue = sdf.format(birthday);
            }
        } catch (Exception e) {}
    }
    session.setAttribute("birthdayValue", birthdayValue);
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="编辑资料" />
</jsp:include>

<!-- Quill.js 富文本编辑器 -->
<link href="https://cdn.staticfile.org/quill/1.3.6/quill.snow.css" rel="stylesheet">
<script src="https://cdn.staticfile.org/quill/1.3.6/quill.min.js"></script>

<div class="container-xl">
    <div class="row">
        <div class="col-12">
            <div class="page-header">
                <h1 class="page-title">编辑资料</h1>
                <div class="page-subtitle">管理您的个人信息</div>
            </div>
        </div>
    </div>
    <!-- 错误提示 -->
    <c:if test="${not empty error}">
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
                        <div class="text-secondary">${error}</div>
                    </div>
                    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                </div>
            </div>
        </div>
    </c:if>
    <!-- 成功提示 -->
    <c:if test="${not empty success}">
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
                        <div class="text-secondary">${success}</div>
                    </div>
                    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                </div>
            </div>
        </div>
    </c:if>
    <div class="row">
        <div class="col-lg-8">
            <div class="card">
                <div class="card-body">
                    <form id="profileForm" method="post"
                        action="${pageContext.request.contextPath}/member/profile/update"
                        enctype="multipart/form-data">
                        <%-- CSRF Token --%>
                        <!-- 头像上传区域 -->
                        <div class="mb-4">
                            <h3 class="mb-3">头像设置</h3>
                            <div class="d-flex align-items-center">
                                <div class="me-4">
                                    <c:choose>
                                        <%-- 情况1：有头像文件 --%>
                                        <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                                            <img id="avatarPreview"
                                                 src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}"
                                                 alt="用户头像"
                                                 class="rounded-circle"
                                                 width="120"
                                                 height="120">
                                        </c:when>
                                        <%-- 情况2：无头像，显示姓名首字 --%>
                                        <c:otherwise>
                                            <div class="rounded-circle d-flex align-items-center justify-content-center bg-primary text-white"
                                                 style="width: 120px; height: 120px; font-size: 48px; font-weight: bold;">
                                                ${not empty user.name ? user.name.charAt(0) : '用'}
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div>
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
                                    <input type="text" class="form-control" name="username"
                                        value="${user.username}" readonly>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">角色</label>
                                    <input type="text" class="form-control"
                                        value="${user.role}" readonly>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">姓名</label>
                                    <input type="text" class="form-control" name="name"
                                        value="${user.name}" placeholder="请输入姓名">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">电话</label>
                                    <input type="text" class="form-control" name="phone"
                                        value="${user.phone}" placeholder="请输入电话">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">邮箱</label>
                                    <input type="email" class="form-control" name="email"
                                        value="${user.email}" placeholder="请输入邮箱">
                                </div>
                                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">用户类型</label>
                                        <select class="form-select" name="userType">
                                            <option value="TEACHER" ${user.userType eq 'TEACHER'
                                                ? 'selected' : '' }>教师</option>
                                            <option value="STUDENT" ${user.userType eq 'STUDENT'
                                                ? 'selected' : '' }>学生</option>
                                        </select>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- 详细资料 -->
                        <div class="mb-4">
                            <h3 class="mb-3">详细资料</h3>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">生日</label>
                                    <input type="date" class="form-control" name="birthday"
                                        value="${birthdayValue}" min="1900-01-01" max="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">姓名</label>
                                    <input type="text" class="form-control" value="${user.name}" readonly>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">学号</label>
                                    <input type="text" class="form-control" name="studentId"
                                        value="${memberProfile.studentId}"
                                        placeholder="请输入学号">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">专业</label>
                                    <input type="text" class="form-control" name="major"
                                        value="${memberProfile.major}" placeholder="请输入专业">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">入学年份</label>
                                    <select class="form-select" name="grade">
                                        <option value="2026" ${memberProfile.grade eq '2026' or empty memberProfile.grade ? 'selected' : ''}>2026</option>
                                        <option value="2025" ${memberProfile.grade eq '2025' ? 'selected' : ''}>2025</option>
                                        <option value="2024" ${memberProfile.grade eq '2024' ? 'selected' : ''}>2024</option>
                                        <option value="2023" ${memberProfile.grade eq '2023' ? 'selected' : ''}>2023</option>
                                        <option value="2022" ${memberProfile.grade eq '2022' ? 'selected' : ''}>2022</option>
                                        <option value="2021" ${memberProfile.grade eq '2021' ? 'selected' : ''}>2021</option>
                                        <option value="2020" ${memberProfile.grade eq '2020' ? 'selected' : ''}>2020</option>
                                    </select>
                                </div>
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">个人简介</label>
                                    <!-- 富文本编辑器容器 -->
                                    <div id="editor-container" style="height: 200px; background: white;"></div>
                                    <!-- 隐藏域用于表单提交 -->
                                    <input type="hidden" name="bio" id="bio-hidden" value="${memberProfile.introduction}">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">GitHub</label>
                                    <input type="text" class="form-control" name="github"
                                        value="${memberProfile.github}" placeholder="请输入GitHub用户名">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">博客</label>
                                    <input type="text" class="form-control" name="blog"
                                        value="${memberProfile.blog}" placeholder="请输入博客地址">
                                </div>
                            </div>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="d-flex justify-content-between">
                            <div>
                                <a href="${pageContext.request.contextPath}/member/password-change.jsp"
                                    class="btn btn-outline-secondary">修改密码</a>
                                <a href="${pageContext.request.contextPath}/member/index.jsp"
                                    class="btn btn-outline-secondary ms-2">返回个人中心</a>
                            </div>
                            <div>
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
                            密码修改需要提供原密码进行验证
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
                            新密码长度不少于6位
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // 显示通知函数 - Tabler alert 样式，带关闭按钮
    function showNotification(message, type) {
        // 移除已存在的通知
        const existingNotification = document.getElementById('notification');
        if (existingNotification) {
            existingNotification.remove();
        }

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
        const container = document.querySelector('.container-xl');
        if (container) {
            container.insertBefore(notification, container.firstChild);
        }
    }

    document.getElementById('avatarUpload').addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;

        if (file.size > 500 * 1024) {
            showNotification('头像大小不能超过500KB', 'error');
            this.value = '';
            return;
        }

        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            showNotification('仅支持JPEG、JPG、PNG、GIF、WebP格式的图片', 'error');
            this.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('avatarPreview').src = e.target.result;
        };
        reader.readAsDataURL(file);
    });

    document.getElementById('profileForm').addEventListener('submit', function (e) {
        const oldPassword = this.oldPassword.value;
        const newPassword = this.newPassword.value;
        const confirmPassword = this.confirmPassword.value;

        if ((newPassword || confirmPassword) && !oldPassword) {
            alert('修改密码时必须提供原密码');
            e.preventDefault();
            return;
        }

        if (newPassword && newPassword !== confirmPassword) {
            alert('两次输入的新密码不一致');
            e.preventDefault();
            return;
        }

        if (newPassword && newPassword.length < 6) {
            alert('新密码长度不能少于6位');
            e.preventDefault();
            return;
        }
    });
</script>

<!-- Quill富文本编辑器初始化 -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // 初始化Quill编辑器
        var quill = new Quill('#editor-container', {
            theme: 'snow',
            placeholder: '请输入个人简介...',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline', 'strike'],
                    ['blockquote', 'code-block'],
                    [{ 'header': [1, 2, 3, false] }],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    [{ 'script': 'sub'}, { 'script': 'super' }],
                    [{ 'indent': '-1'}, { 'indent': '+1' }],
                    [{ 'color': [] }, { 'background': [] }],
                    [{ 'align': [] }],
                    ['link', 'image'],
                    ['clean']
                ]
            }
        });

        // 获取隐藏域
        var hiddenInput = document.getElementById('bio-hidden');

        // 如果隐藏域已有内容，设置为编辑器内容
        if (hiddenInput && hiddenInput.value) {
            quill.root.innerHTML = hiddenInput.value;
        }

        // 在表单提交前同步编辑器内容到隐藏域
        var form = document.getElementById('profileForm');
        if (form) {
            form.addEventListener('submit', function() {
                if (hiddenInput) {
                    hiddenInput.value = quill.root.innerHTML;
                }
            });
        }
    });
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />