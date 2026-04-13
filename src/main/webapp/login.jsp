<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>用户登录 - 软件小组管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
    <style>
        body {
            background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .login-container {
            width: 100%;
            max-width: 440px;
            padding: 20px;
        }

        .login-card {
            background: var(--bg-white);
            border-radius: var(--radius-generous);
            box-shadow: var(--shadow-brand-offset);
            overflow: hidden;
        }

        .login-header {
            padding: 40px 40px 32px;
            text-align: center;
            background: var(--bg-white);
        }

        .login-logo {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 8px;
            text-decoration: none;
        }

        .login-logo-icon {
            width: 56px;
            height: 56px;
            background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
            border-radius: var(--radius-standard);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.75rem;
            color: white;
        }

        .login-logo-text {
            font-family: var(--font-display);
            font-size: 1.75rem;
            font-weight: 600;
            color: var(--text-dark);
        }

        .login-subtitle {
            font-family: var(--font-ui);
            color: var(--text-muted);
            font-size: 0.94rem;
            margin-top: 4px;
        }

        .login-body {
            padding: 0 40px 40px;
        }

        .form-title {
            font-family: var(--font-display);
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-dark);
            text-align: center;
            margin-bottom: 24px;
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
            position: relative;
            margin-bottom: 20px;
        }

        .input-group-design .form-control {
            width: 100%;
            padding: 14px 16px;
            font-family: var(--font-ui);
            font-size: 0.94rem;
            border: 1px solid var(--border-gray);
            border-radius: var(--radius-standard);
            transition: all 0.3s ease;
            background: var(--bg-white);
        }

        .input-group-design .form-control:focus {
            border-color: var(--brand-blue);
            box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
            outline: none;
        }

        .input-group-design .input-icon {
            position: absolute;
            left: 16px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-muted);
            font-size: 1rem;
            pointer-events: none;
        }

        .input-group-design .form-control {
            padding-left: 44px;
        }

        .btn-login-design {
            width: 100%;
            padding: 14px 24px;
            background: linear-gradient(135deg, var(--brand-blue), var(--primary-600));
            color: white;
            border: none;
            border-radius: var(--radius-standard);
            font-family: var(--font-ui);
            font-size: 0.94rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 14px rgba(20, 86, 240, 0.35);
        }

        .btn-login-design:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(20, 86, 240, 0.4);
            background: linear-gradient(135deg, var(--primary-600), var(--primary-700));
        }

        .btn-login-design:active {
            transform: translateY(0);
        }

        .login-divider {
            display: flex;
            align-items: center;
            margin: 24px 0;
        }

        .login-divider::before,
        .login-divider::after {
            content: "";
            flex: 1;
            height: 1px;
            background: var(--border-gray);
        }

        .login-divider span {
            padding: 0 16px;
            color: var(--text-muted);
            font-size: 0.81rem;
        }

        .login-footer {
            text-align: center;
            padding: 24px 40px;
            background: var(--bg-light-gray);
            border-top: 1px solid var(--border-light);
        }

        .login-footer-text {
            font-family: var(--font-ui);
            font-size: 0.875rem;
            color: var(--text-secondary);
        }

        .login-footer-text a {
            color: var(--brand-blue);
            font-weight: 500;
            text-decoration: none;
        }

        .login-footer-text a:hover {
            text-decoration: underline;
        }

        .back-home-link {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            margin-top: 16px;
            font-family: var(--font-ui);
            font-size: 0.875rem;
            color: var(--text-muted);
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .back-home-link:hover {
            color: var(--brand-blue);
        }

        .alert-design {
            padding: 14px 16px;
            border-radius: var(--radius-standard);
            font-family: var(--font-ui);
            font-size: 0.875rem;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .alert-danger-design {
            background: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
        }

        @media (max-width: 480px) {
            .login-header,
            .login-body,
            .login-footer {
                padding-left: 24px;
                padding-right: 24px;
            }

            .login-logo-icon {
                width: 48px;
                height: 48px;
                font-size: 1.5rem;
            }

            .login-logo-text {
                font-size: 1.5rem;
            }
        }
    </style>
</head>

<body>
    <div class="login-container">
        <div class="login-card">
            <div class="login-header">
                <a href="${pageContext.request.contextPath}/index.jsp" class="login-logo">
                    <span class="login-logo-icon">
                        <i class="bi bi-cpu"></i>
                    </span>
                    <span class="login-logo-text">软件小组</span>
                </a>
                <p class="login-subtitle">信息工程学院软件兴趣小组管理系统</p>
            </div>

            <div class="login-body">
                <h2 class="form-title">登录您的账户</h2>

                <c:if test="${not empty error}">
                    <div class="alert-design alert-danger-design">
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span>${error}</span>
                    </div>
                </c:if>

                <form action="login" method="post" autocomplete="off" novalidate>
                    <div class="input-group-design">
                        <i class="bi bi-person input-icon"></i>
                        <input type="text" name="username" class="form-control" placeholder="请输入用户名" autocomplete="off" required>
                    </div>

                    <div class="input-group-design">
                        <i class="bi bi-lock input-icon"></i>
                        <input type="password" name="password" class="form-control" placeholder="请输入密码" autocomplete="off" required>
                    </div>

                    <button type="submit" class="btn-login-design">
                        登录
                    </button>
                </form>
            </div>

            <div class="login-footer">
                <p class="login-footer-text">
                    还没有账户? <a href="${pageContext.request.contextPath}/recruit/apply.jsp">立即报名</a>
                </p>
                <a href="${pageContext.request.contextPath}/index.jsp" class="back-home-link">
                    <i class="bi bi-house"></i>
                    返回首页
                </a>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>
</body>

</html>