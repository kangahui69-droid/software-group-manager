<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="修改密码" />
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
}
body {
    font-family: var(--font-ui);
    background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
}
.page-header { padding: 24px 0; }
.page-title {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0;
}
.page-subtitle {
    font-size: 14px;
    color: var(--text-muted);
    margin-top: 4px;
}
.card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-standard);
    box-shadow: var(--shadow-standard);
}
.card-body { padding: 24px; }
.card-title {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0 0 16px;
}
.alert-success {
    background: #ecfdf5;
    border: 1px solid #d1fae5;
    border-radius: var(--radius-comfortable);
    color: #065f46;
    padding: 12px 16px;
    margin-bottom: 20px;
}
.alert-danger {
    background: #fef2f2;
    border: 1px solid #fee2e2;
    border-radius: var(--radius-comfortable);
    color: #991b1b;
    padding: 12px 16px;
    margin-bottom: 20px;
}
.form-label {
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 8px;
}
.form-control {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 12px 16px;
    font-size: 14px;
    transition: all 0.2s ease;
}
.form-control:focus {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 85, 240, 0.1);
}
.btn-primary {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 12px 24px;
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
    padding: 12px 24px;
    font-weight: 500;
    font-size: 14px;
    color: var(--text-muted);
    cursor: pointer;
    transition: all 0.2s ease;
    text-decoration: none;
    display: inline-block;
}
.btn-secondary:hover {
    border-color: var(--brand-blue);
    color: var(--brand-blue);
}
.tips-card {
    background: var(--primary-light);
    border: 1px solid rgba(20, 85, 240, 0.1);
    border-radius: var(--radius-standard);
}
.list-group-item {
    border: none;
    padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
}
.list-group-item:last-child { border-bottom: none; }
.list-group-item .icon { color: var(--brand-blue); }
</style>

<div class="container-xl">
    <div class="row">
        <div class="col-12">
            <div class="page-header">
                <h1 class="page-title">修改密码</h1>
                <div class="page-subtitle">保障账户安全</div>
            </div>
        </div>
    </div>
    
    <c:if test="${not empty error}">
        <div class="row">
            <div class="col-12">
                <div class="alert alert-danger" role="alert">
                    ${error}
                </div>
            </div>
        </div>
    </c:if>
    
    <c:if test="${not empty success}">
        <div class="row">
            <div class="col-12">
                <div class="alert alert-success" role="alert">
                    ${success}
                </div>
            </div>
        </div>
    </c:if>
    
    <div class="row">
        <div class="col-lg-8">
            <div class="card">
                <div class="card-body">
                    <form id="passwordForm" method="post" action="${pageContext.request.contextPath}/member/password">
                        <div class="mb-4">
                            <h3 class="card-title">密码设置</h3>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">原密码</label>
                                    <input type="password" class="form-control" name="oldPassword" placeholder="请输入当前密码" required>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">新密码</label>
                                    <input type="password" class="form-control" name="newPassword" placeholder="请输入新密码（不少于6位）" required minlength="6">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">确认新密码</label>
                                    <input type="password" class="form-control" name="confirmPassword" placeholder="请再次输入新密码" required>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end">
                            <a href="${pageContext.request.contextPath}/member/profile.jsp" class="btn btn-secondary me-2">返回</a>
                            <button type="submit" class="btn btn-primary">修改密码</button>
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
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                            密码修改需要提供原密码进行验证
                        </li>
                        <li class="list-group-item d-flex align-items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                            新密码长度不少于6位
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.getElementById('passwordForm').addEventListener('submit', function (e) {
    const oldPassword = this.oldPassword.value;
    const newPassword = this.newPassword.value;
    const confirmPassword = this.confirmPassword.value;
    if (!oldPassword) {
        alert('请输入原密码');
        e.preventDefault();
        return;
    }
    if (newPassword.length < 6) {
        alert('新密码长度不能少于6位');
        e.preventDefault();
        return;
    }
    if (newPassword !== confirmPassword) {
        alert('两次输入的新密码不一致');
        e.preventDefault();
        return;
    }
});
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />