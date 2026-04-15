<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="修改密码" />
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
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-lock me-2"></i>修改密码
            </h1>
            <p class="admin-hero-subtitle">保障账户安全，定期更换密码</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert-design alert-danger-design" role="alert">
                <i class="bi bi-exclamation-circle-fill"></i>
                <span>${error}</span>
            </div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="alert-design alert-success-design" role="alert">
                <i class="bi bi-check-circle-fill"></i>
                <span>${success}</span>
            </div>
        </c:if>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design" style="background: var(--bg-white); border-radius: var(--radius-generous); padding: 32px; box-shadow: var(--shadow-brand-purple); border: none;">
                    <h3 class="card-title-design" style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); margin-bottom: 24px; display: flex; align-items: center; gap: 10px;">
                        <i class="bi bi-shield-lock text-brand"></i>密码设置
                    </h3>
                    <form id="passwordForm" method="post" action="${pageContext.request.contextPath}/admin/password">
                        <div class="mb-4">
                            <label class="form-label-design" for="oldPassword">原密码</label>
                            <input type="password" class="input-design" id="oldPassword" name="oldPassword" placeholder="请输入当前密码" required>
                        </div>
                        <div class="mb-4">
                            <label class="form-label-design" for="newPassword">新密码</label>
                            <input type="password" class="input-design" id="newPassword" name="newPassword" placeholder="请输入新密码（不少于6位）" required minlength="6">
                        </div>
                        <div class="mb-4">
                            <label class="form-label-design" for="confirmPassword">确认新密码</label>
                            <input type="password" class="input-design" id="confirmPassword" name="confirmPassword" placeholder="请再次输入新密码" required>
                        </div>

                        <div class="d-flex justify-content-end gap-3">
                            <a href="${pageContext.request.contextPath}/admin/profile.jsp" class="btn btn-outline-brand">返回</a>
                            <button type="submit" class="btn btn-brand">修改密码</button>
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
                        <div class="tip-text">密码修改需要提供原密码进行验证</div>
                    </div>
                    <div class="tip-item">
                        <div class="tip-icon">
                            <i class="bi bi-check2"></i>
                        </div>
                        <div class="tip-text">新密码长度不少于6位</div>
                    </div>
                    <div class="tip-item">
                        <div class="tip-icon">
                            <i class="bi bi-check2"></i>
                        </div>
                        <div class="tip-text">建议使用字母、数字和符号的组合</div>
                    </div>
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