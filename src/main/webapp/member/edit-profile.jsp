<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
.edit-hero {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 50%, #3b82f6 100%);
    border-radius: var(--radius-generous);
    padding: 32px 40px;
    margin-bottom: 32px;
    color: white;
}

.edit-hero-title {
    font-family: var(--font-display);
    font-size: 1.75rem;
    font-weight: 600;
    margin-bottom: 4px;
}

.edit-hero-subtitle {
    opacity: 0.9;
    font-size: 0.94rem;
}

.form-card {
    background: var(--bg-white);
    border-radius: var(--radius-generous);
    box-shadow: var(--shadow-brand-purple);
    border: none;
    overflow: hidden;
}

.form-card-header {
    background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-light) 100%);
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-light);
}

.form-card-title {
    font-family: var(--font-display);
    font-size: 1.1rem;
    font-weight: 600;
    color: white;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.form-card-body {
    padding: 28px;
}

.section-divider {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 28px 0 20px;
}

.section-divider::before,
.section-divider::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border-light);
}

.section-divider span {
    font-family: var(--font-display);
    font-size: 0.94rem;
    font-weight: 600;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.avatar-edit-section {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 24px;
    background: var(--bg-white);
    border-radius: var(--radius-generous);
    box-shadow: var(--shadow-brand-purple);
    margin-bottom: 24px;
}

.avatar-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid var(--primary-light);
    box-shadow: var(--shadow-brand-offset);
}

.avatar-placeholder {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48px;
    font-weight: 700;
    color: white;
    border: 4px solid rgba(20, 86, 240, 0.2);
}

.avatar-upload-btn {
    background: var(--brand-blue);
    color: white;
    border: none;
    border-radius: var(--radius-standard);
    padding: 12px 24px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.avatar-upload-btn:hover {
    background: var(--primary-600);
    transform: translateY(-2px);
    box-shadow: var(--shadow-brand-purple);
}

.avatar-upload-hint {
    font-size: 0.81rem;
    color: var(--text-muted);
    margin-top: 8px;
}

.hidden-file-input {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
}

.form-label-custom {
    font-family: var(--font-ui);
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 8px;
    font-size: 0.88rem;
}

.form-control-custom {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-standard);
    padding: 12px 16px;
    font-size: 0.94rem;
    transition: all 0.2s ease;
    background: var(--bg-white);
}

.form-control-custom:focus {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    outline: none;
}

.form-control-custom[readonly] {
    background: var(--primary-light);
    color: var(--text-dark);
}

.btn-save {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
    color: white;
    border: none;
    border-radius: var(--radius-standard);
    padding: 14px 32px;
    font-weight: 600;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.btn-save:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-brand-purple);
    color: white;
}

.btn-cancel {
    background: var(--bg-white);
    color: var(--text-muted);
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-standard);
    padding: 14px 24px;
    font-weight: 500;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s ease;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.btn-cancel:hover {
    border-color: var(--brand-blue);
    color: var(--brand-blue);
}

.tips-card {
    background: linear-gradient(135deg, var(--primary-light) 0%, #dbeafe 100%);
    border: 1px solid rgba(20, 86, 240, 0.1);
    border-radius: var(--radius-generous);
    padding: 24px;
}

.tips-title {
    font-family: var(--font-display);
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--brand-blue);
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.tips-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid rgba(20, 86, 240, 0.1);
    font-size: 0.88rem;
    color: var(--text-secondary);
}

.tips-item:last-child {
    border-bottom: none;
}

.tips-item i {
    color: var(--brand-blue);
    margin-top: 2px;
}

textarea.form-control-custom {
    min-height: 120px;
    resize: vertical;
}

#editor-container {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-standard);
    background: white;
    min-height: 150px;
}

#editor-container .ql-toolbar {
    border-top: none;
    border-left: none;
    border-right: none;
    border-bottom: 1px solid var(--border-gray);
    border-radius: var(--radius-standard) var(--radius-standard) 0 0;
}

#editor-container .ql-container {
    border: none;
    font-size: 0.94rem;
}

#editor-container .ql-editor {
    min-height: 120px;
    padding: 12px 16px;
}

#editor-container .ql-editor.ql-blank::before {
    color: var(--text-muted);
    font-style: normal;
}
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="edit-hero">
            <h1 class="edit-hero-title">
                <i class="bi bi-pencil-square me-2"></i>编辑资料
            </h1>
            <p class="edit-hero-subtitle">完善您的个人信息，让更多人了解您</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <form id="profileForm" method="post" action="${pageContext.request.contextPath}/member/profile/update" enctype="multipart/form-data">
                    <div class="avatar-edit-section">
                        <c:choose>
                            <c:when test="${memberProfile != null && memberProfile.avatarFileId != null}">
                                <img id="avatarPreview" src="${pageContext.request.contextPath}/file?action=view&id=${memberProfile.avatarFileId}" alt="用户头像" class="avatar-preview">
                            </c:when>
                            <c:otherwise>
                                <div class="avatar-placeholder" id="avatarPreviewPlaceholder">
                                    ${not empty user.name ? user.name.charAt(0) : '用'}
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div>
                            <label for="avatarUpload" class="avatar-upload-btn">
                                <i class="bi bi-cloud-arrow-up"></i>上传新头像
                            </label>
                            <input type="file" id="avatarUpload" name="avatar" accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" class="hidden-file-input">
                            <p class="avatar-upload-hint">支持JPG、PNG、GIF、WebP格式，大小不超过500KB</p>
                        </div>
                    </div>
                    <div class="form-card mb-4">
                        <div class="form-card-header">
                            <h3 class="form-card-title">
                                <i class="bi bi-person"></i>基本信息
                            </h3>
                        </div>
                        <div class="form-card-body">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label-custom">用户名</label>
                                    <input type="text" class="form-control form-control-custom" name="username" value="${user.username}" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">角色</label>
                                    <input type="text" class="form-control form-control-custom" value="<c:choose><c:when test='${user.role eq \"ADMIN\"}'>管理员</c:when><c:when test='${user.role eq \"MEMBER\"}'>成员</c:when><c:when test='${user.role eq \"TEACHER\"}'>教师</c:when><c:otherwise>${user.role}</c:otherwise></c:choose>" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">姓名 <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control form-control-custom" name="name" value="${user.name}" placeholder="请输入姓名" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">电话</label>
                                    <input type="tel" class="form-control form-control-custom" name="phone" value="${user.phone}" placeholder="请输入电话号码">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">邮箱 <span class="text-danger">*</span></label>
                                    <input type="email" class="form-control form-control-custom" name="email" value="${user.email}" placeholder="请输入邮箱" required>
                                </div>
                                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                                    <div class="col-md-6">
                                        <label class="form-label-custom">用户类型</label>
                                        <select class="form-control form-control-custom" name="userType">
                                            <option value="TEACHER" ${user.userType eq 'TEACHER' ? 'selected' : '' }>教师</option>
                                            <option value="STUDENT" ${user.userType eq 'STUDENT' ? 'selected' : '' }>学生</option>
                                        </select>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="form-card mb-4">
                        <div class="form-card-header">
                            <h3 class="form-card-title">
                                <i class="bi bi-card-list"></i>详细资料
                            </h3>
                        </div>
                        <div class="form-card-body">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label-custom">生日</label>
                                    <input type="date" class="form-control form-control-custom" name="birthday" value="${birthdayValue}" min="1900-01-01" max="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">学号</label>
                                    <input type="text" class="form-control form-control-custom" name="studentId" value="${memberProfile.studentId}" placeholder="请输入学号">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">专业</label>
                                    <input type="text" class="form-control form-control-custom" name="major" value="${memberProfile.major}" placeholder="请输入专业">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">年级</label>
                                    <select class="form-control form-control-custom" name="grade">
                                        <option value="">请选择年级</option>
                                        <option value="2026" ${memberProfile.grade eq '2026' ? 'selected' : ''}>2026级</option>
                                        <option value="2025" ${memberProfile.grade eq '2025' ? 'selected' : ''}>2025级</option>
                                        <option value="2024" ${memberProfile.grade eq '2024' ? 'selected' : ''}>2024级</option>
                                        <option value="2023" ${memberProfile.grade eq '2023' ? 'selected' : ''}>2023级</option>
                                        <option value="2022" ${memberProfile.grade eq '2022' ? 'selected' : ''}>2022级</option>
                                        <option value="2021" ${memberProfile.grade eq '2021' ? 'selected' : ''}>2021级</option>
                                        <option value="2020" ${memberProfile.grade eq '2020' ? 'selected' : ''}>2020级</option>
                                    </select>
                                </div>
                                <div class="col-12">
                                    <label class="form-label-custom">个人简介</label>
                                    <div id="editor-container"></div>
                                    <input type="hidden" name="bio" id="bio-hidden" value="${memberProfile.introduction}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">GitHub</label>
                                    <div class="input-group">
                                        <span class="input-group-text" style="background: var(--primary-light); border: 1px solid var(--border-gray); border-right: none; border-radius: var(--radius-standard) 0 0 var(--radius-standard);">
                                            <i class="bi bi-github"></i>
                                        </span>
                                        <input type="text" class="form-control form-control-custom" name="github" value="${memberProfile.github}" placeholder="请输入GitHub用户名" style="border-radius: 0 var(--radius-standard) var(--radius-standard) 0;">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-custom">博客</label>
                                    <div class="input-group">
                                        <span class="input-group-text" style="background: var(--primary-light); border: 1px solid var(--border-gray); border-right: none; border-radius: var(--radius-standard) 0 0 var(--radius-standard);">
                                            <i class="bi bi-globe2"></i>
                                        </span>
                                        <input type="text" class="form-control form-control-custom" name="blog" value="${memberProfile.blog}" placeholder="请输入博客地址" style="border-radius: 0 var(--radius-standard) var(--radius-standard) 0;">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex gap-3">
                            <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn-cancel">
                                <i class="bi bi-lock"></i>修改密码
                            </a>
                            <a href="${pageContext.request.contextPath}/member/profile.jsp" class="btn-cancel">
                                <i class="bi bi-arrow-left"></i>返回
                            </a>
                        </div>
                        <button type="submit" class="btn-save">
                            <i class="bi bi-check-lg"></i>保存修改
                        </button>
                    </div>
                </form>
            </div>

            <div class="col-lg-4">
                <div class="tips-card">
                    <h4 class="tips-title">
                        <i class="bi bi-lightbulb"></i>修改提示
                    </h4>
                    <div class="tips-item">
                        <i class="bi bi-info-circle"></i>
                        <span>用户名不可修改，如有疑问请联系管理员</span>
                    </div>
                    <div class="tips-item">
                        <i class="bi bi-image"></i>
                        <span>头像大小不能超过500KB，支持JPG、PNG、GIF、WebP格式</span>
                    </div>
                    <div class="tips-item">
                        <i class="bi bi-lock"></i>
                        <span>修改密码需要提供原密码进行验证</span>
                    </div>
                    <div class="tips-item">
                        <i class="bi bi-key"></i>
                        <span>新密码长度不能少于6位</span>
                    </div>
                    <div class="tips-item">
                        <i class="bi bi-shield-check"></i>
                        <span>带 <span class="text-danger">*</span> 的字段为必填项</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.staticfile.org/quill/1.3.6/quill.min.js"></script>
<link href="https://cdn.staticfile.org/quill/1.3.6/quill.snow.css" rel="stylesheet">

<script>
document.addEventListener('DOMContentLoaded', function() {
    var avatarUpload = document.getElementById('avatarUpload');
    if (avatarUpload) {
        avatarUpload.addEventListener('change', function() {
            var file = this.files[0];
            if (!file) return;
            
            if (file.size > 500 * 1024) {
                alert('头像大小不能超过500KB');
                this.value = '';
                return;
            }
            
            var allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            if (!allowedTypes.includes(file.type)) {
                alert('仅支持JPEG、JPG、PNG、GIF、WebP格式的图片');
                this.value = '';
                return;
            }
            
            var reader = new FileReader();
            reader.onload = function(e) {
                var preview = document.getElementById('avatarPreview');
                var placeholder = document.getElementById('avatarPreviewPlaceholder');
                if (preview) {
                    preview.src = e.target.result;
                } else if (placeholder) {
                    placeholder.innerHTML = '<img src="' + e.target.result + '" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;">';
                }
            };
            reader.readAsDataURL(file);
        });
    }

    var quill = new Quill('#editor-container', {
        theme: 'snow',
        placeholder: '请输入个人简介...',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline', 'strike'],
                ['blockquote', 'code-block'],
                [{ 'header': [1, 2, 3, false] }],
                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                [{ 'color': [] }, { 'background': [] }],
                [{ 'align': [] }],
                ['link'],
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