<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="修改密码" />
</jsp:include>

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

    .input-group-design input {
        flex: 1;
        border: none;
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        outline: none;
        background: transparent;
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

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-lock me-2"></i>修改密码
            </h1>
            <p class="member-hero-subtitle">保障账户安全，定期更换密码</p>
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
                            <i class="bi bi-shield-lock text-brand"></i>密码设置
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form id="passwordForm" method="post" action="${pageContext.request.contextPath}/member/password">
                            <div class="mb-4">
                                <label class="form-label-design required">原密码</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-key"></i>
                                    </div>
                                    <input type="password" name="oldPassword" placeholder="请输入当前密码" required>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design required">新密码</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-lock"></i>
                                    </div>
                                    <input type="password" name="newPassword" placeholder="请输入新密码（不少于6位）" required minlength="6">
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design required">确认新密码</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-lock-fill"></i>
                                    </div>
                                    <input type="password" name="confirmPassword" placeholder="请再次输入新密码" required>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end gap-3">
                                <a href="${pageContext.request.contextPath}/member/profile.jsp" class="btn-outline-brand">
                                    <i class="bi bi-arrow-left"></i>返回
                                </a>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-check-lg"></i>修改密码
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
                                <div class="tip-text">密码修改需要提供原密码进行验证</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-2-circle"></i>
                                </div>
                                <div class="tip-text">新密码长度不少于6位</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-3-circle"></i>
                                </div>
                                <div class="tip-text">新密码与确认密码必须一致</div>
                            </div>
                            <div class="tip-item">
                                <div class="tip-icon">
                                    <i class="bi bi-4-circle"></i>
                                </div>
                                <div class="tip-text">建议使用大小写字母、数字组合</div>
                            </div>
                        </div>
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