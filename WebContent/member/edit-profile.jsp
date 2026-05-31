<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="dao.MemberProfileDAO" %>
<%@ page import="model.MemberProfile" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%
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

<link href="https://cdn.staticfile.org/quill/1.3.6/quill.snow.css" rel="stylesheet">
<script src="https://cdn.staticfile.org/quill/1.3.6/quill.min.js"></script>

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
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

    .card-body-design {
        padding: 24px;
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
        background: var(--bg-white);
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
        background: rgba(20, 86, 240, 0.05);
        color: var(--brand-blue);
        flex-shrink: 0;
    }

    .input-group-design input,
    .input-group-design select,
    .input-group-design textarea {
        flex: 1;
        border: none;
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        outline: none;
        background: transparent;
    }

    .input-group-design input:focus,
    .input-group-design select:focus,
    .input-group-design textarea:focus {
        outline: none;
    }

    .avatar-upload-area {
        display: flex;
        align-items: center;
        gap: 24px;
        padding: 20px;
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        margin-bottom: 24px;
    }

    .avatar-preview {
        width: 100px;
        height: 100px;
        border-radius: var(--radius-comfortable);
        object-fit: cover;
        border: 4px solid var(--bg-white);
        box-shadow: var(--shadow-card-elevated);
    }

    .avatar-fallback {
        width: 100px;
        height: 100px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2.5rem;
        font-weight: 600;
    }

    .avatar-upload-btn {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .avatar-upload-btn:hover {
        background-color: var(--primary-600);
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
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

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .tip-card {
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        border: 1px solid rgba(20, 86, 240, 0.1);
    }

    .tip-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 14px;
    }

    .tip-item:last-child {
        margin-bottom: 0;
    }

    .tip-icon {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: var(--brand-blue);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        flex-shrink: 0;
    }

    .tip-text {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-secondary);
        line-height: 1.5;
    }

    #editor-container {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        background: white;
        min-height: 200px;
    }

    #editor-container:focus-within {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }

        .avatar-upload-area {
            flex-direction: column;
            text-align: center;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-pencil-square me-2"></i>编辑资料
            </h1>
            <p class="member-hero-subtitle">管理您的个人信息</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible mb-4" style="border-radius: var(--radius-standard);" role="alert">
                <i class="bi bi-exclamation-circle me-2"></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible mb-4" style="border-radius: var(--radius-standard);" role="alert">
                <i class="bi bi-check-circle me-2"></i>${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-person-vcard text-brand"></i>编辑个人信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form id="profileForm" method="post" action="${pageContext.request.contextPath}/member/profile/update" enctype="multipart/form-data">
                            <div class="avatar-upload-area">
                                <c:choose>
                                    <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                                        <img id="avatarPreview" src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}" alt="用户头像" class="avatar-preview">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="avatar-fallback">
                                            ${not empty user.name ? user.name.charAt(0) : '用'}
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div>
                                    <label class="avatar-upload-btn">
                                        <i class="bi bi-upload"></i>上传新头像
                                        <input type="file" id="avatarUpload" name="avatar" accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" style="display: none;">
                                    </label>
                                    <p class="text-muted mt-2" style="font-size: 0.81rem;">支持JPG、PNG、GIF格式，大小不超过500KB</p>
                                </div>
                            </div>

                            <div class="row g-4 mb-4">
                                <div class="col-md-6">
                                    <label class="form-label-design">用户名</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-person"></i>
                                        </div>
                                        <input type="text" value="${user.username}" readonly>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">角色</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-shield"></i>
                                        </div>
                                        <input type="text" value="${user.role}" readonly>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">姓名</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-person-badge"></i>
                                        </div>
                                        <input type="text" name="name" value="${user.name}" placeholder="请输入姓名">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">电话</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-telephone"></i>
                                        </div>
                                        <input type="text" name="phone" value="${user.phone}" placeholder="请输入电话">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">邮箱</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-envelope"></i>
                                        </div>
                                        <input type="email" name="email" value="${user.email}" placeholder="请输入邮箱">
                                    </div>
                                </div>
                                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                                    <div class="col-md-6">
                                        <label class="form-label-design">用户类型</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-people"></i>
                                            </div>
                                            <select name="userType">
                                                <option value="TEACHER" ${user.userType eq 'TEACHER' ? 'selected' : ''}>教师</option>
                                                <option value="STUDENT" ${user.userType eq 'STUDENT' ? 'selected' : ''}>学生</option>
                                            </select>
                                        </div>
                                    </div>
                                </c:if>
                                <div class="col-md-6">
                                    <label class="form-label-design">生日</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-calendar"></i>
                                        </div>
                                        <input type="date" name="birthday" value="${birthdayValue}" min="1900-01-01" max="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">学号</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-123"></i>
                                        </div>
                                        <input type="text" name="studentId" value="${memberProfile.studentId}" placeholder="请输入学号">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">专业</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-book"></i>
                                        </div>
                                        <input type="text" name="major" value="${memberProfile.major}" placeholder="请输入专业">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">入学年份</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-calendar-event"></i>
                                        </div>
                                        <select name="grade">
                                            <option value="2026" ${memberProfile.grade eq '2026' or empty memberProfile.grade ? 'selected' : ''}>2026</option>
                                            <option value="2025" ${memberProfile.grade eq '2025' ? 'selected' : ''}>2025</option>
                                            <option value="2024" ${memberProfile.grade eq '2024' ? 'selected' : ''}>2024</option>
                                            <option value="2023" ${memberProfile.grade eq '2023' ? 'selected' : ''}>2023</option>
                                            <option value="2022" ${memberProfile.grade eq '2022' ? 'selected' : ''}>2022</option>
                                            <option value="2021" ${memberProfile.grade eq '2021' ? 'selected' : ''}>2021</option>
                                            <option value="2020" ${memberProfile.grade eq '2020' ? 'selected' : ''}>2020</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">GitHub</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-github"></i>
                                        </div>
                                        <input type="text" name="github" value="${memberProfile.github}" placeholder="请输入GitHub用户名">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design">博客</label>
                                    <div class="input-group-design">
                                        <div class="input-group-icon">
                                            <i class="bi bi-rss"></i>
                                        </div>
                                        <input type="text" name="blog" value="${memberProfile.blog}" placeholder="请输入博客地址">
                                    </div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label-design">个人简介</label>
                                    <div id="editor-container" style="height: 200px; background: white;"></div>
                                    <input type="hidden" name="bio" id="bio-hidden" value="${memberProfile.introduction}">
                                </div>
                            </div>

                            <div class="d-flex justify-content-between">
                                <div class="d-flex gap-3">
                                    <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn-outline-brand">
                                        <i class="bi bi-lock"></i>修改密码
                                    </a>
                                    <a href="${pageContext.request.contextPath}/member/index.jsp" class="btn-outline-brand">
                                        <i class="bi bi-house"></i>返回个人中心
                                    </a>
                                </div>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-check-lg"></i>保存修改
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle text-brand"></i>修改提示
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="tip-card">
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-1-circle"></i>
                                </div>
                                <div class="tip-text">用户名不可修改</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-2-circle"></i>
                                </div>
                                <div class="tip-text">头像大小不能超过500KB</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-3-circle"></i>
                                </div>
                                <div class="tip-text">密码修改需要提供原密码进行验证</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-4-circle"></i>
                                </div>
                                <div class="tip-text">新密码长度不少于6位</div>
                            </div>
                        </div>
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
        notification.className = 'alert alert-' + (type === 'success' ? 'success' : 'danger') + ' alert-dismissible';
        notification.setAttribute('role', 'alert');
        notification.style.marginBottom = '20px';
        notification.style.borderRadius = 'var(--radius-standard)';

        const icon = type === 'success'
            ? '<i class="bi bi-check-circle me-2"></i>'
            : '<i class="bi bi-exclamation-circle me-2"></i>';

        const title = type === 'success' ? '操作成功' : '操作失败';

        notification.innerHTML = icon +
            '<div><h4 class="alert-title">' + title + '</h4><div class="text-secondary">' + message + '</div></div>' +
            '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';

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
            const preview = document.getElementById('avatarPreview');
            if (preview) {
                preview.src = e.target.result;
            } else {
                const fallback = document.querySelector('.avatar-fallback');
                if (fallback) {
                    fallback.style.backgroundImage = 'url(' + e.target.result + ')';
                    fallback.style.backgroundSize = 'cover';
                    fallback.textContent = '';
                }
            }
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

<script>
    document.addEventListener('DOMContentLoaded', function() {
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

        var hiddenInput = document.getElementById('bio-hidden');

        if (hiddenInput && hiddenInput.value) {
            quill.root.innerHTML = hiddenInput.value;
        }

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