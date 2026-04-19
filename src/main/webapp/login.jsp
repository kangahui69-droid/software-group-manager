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
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600&family=Outfit:wght@500;600&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            min-height: 100vh;
            background: linear-gradient(135deg, #f0f7ff 0%, #e8f0fe 100%);
        }

        .login-container {
            display: flex;
            min-height: 100vh;
        }

        /* Left Panel - Form */
        .login-panel {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px;
            background: linear-gradient(135deg, #f0f7ff 0%, #e8f0fe 100%);
        }

        .login-form-wrapper {
            width: 100%;
            max-width: 400px;
            animation: fadeInUp 0.6s ease-out;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .login-header {
            text-align: center;
            margin-bottom: 32px;
        }

        .brand-logo {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 24px;
        }

        .brand-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            object-fit: cover;
            box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
        }

        .brand-name {
            font-family: 'Outfit', sans-serif;
            font-size: 28px;
            font-weight: 600;
            color: #222222;
            letter-spacing: -0.02em;
        }

        .form-title {
            font-size: 18px;
            font-weight: 500;
            color: #222222;
            margin-bottom: 28px;
            text-align: center;
        }

        .input-group-custom {
            position: relative;
            margin-bottom: 16px;
        }

        .input-group-custom .form-control {
            width: 100%;
            height: 52px;
            padding: 14px 16px 14px 48px;
            font-size: 15px;
            font-family: 'DM Sans', sans-serif;
            color: #222222;
            background: #ffffff;
            border: 1.5px solid #e5e7eb;
            border-radius: 10px;
            transition: all 0.2s ease;
        }

        .input-group-custom .form-control::placeholder {
            color: #8e8e93;
        }

        .input-group-custom .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
            outline: none;
        }

        .input-group-custom .input-icon {
            position: absolute;
            left: 16px;
            top: 50%;
            transform: translateY(-50%);
            color: #8e8e93;
            font-size: 18px;
            transition: color 0.2s ease;
            pointer-events: none;
        }

        .input-group-custom .form-control:focus + .input-icon {
            color: #3b82f6;
        }

        .btn-submit {
            width: 100%;
            height: 52px;
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            font-family: 'DM Sans', sans-serif;
            letter-spacing: 0.01em;
            cursor: pointer;
            transition: all 0.2s ease;
            box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
            margin-top: 8px;
        }

        .btn-submit:hover {
            background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
            box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
            transform: translateY(-1px);
        }

        .btn-submit:active {
            transform: scale(0.98);
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
            background: #e5e7eb;
        }

        .login-divider span {
            padding: 0 16px;
            color: #8e8e93;
            font-size: 13px;
        }

        .login-footer {
            text-align: center;
        }

        .footer-text {
            font-size: 14px;
            color: #45515e;
        }

        .footer-link {
            color: #3b82f6;
            font-weight: 500;
            text-decoration: none;
            transition: color 0.2s ease;
        }

        .footer-link:hover {
            color: #2563eb;
            text-decoration: underline;
        }

        .back-home {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            margin-top: 16px;
            font-size: 13px;
            color: #8e8e93;
            text-decoration: none;
            transition: all 0.2s ease;
        }

        .back-home:hover {
            color: #45515e;
        }

        .alert-custom {
            padding: 14px 16px;
            border-radius: 10px;
            font-size: 14px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
            animation: shake 0.4s ease;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-4px); }
            75% { transform: translateX(4px); }
        }

        .alert-danger-custom {
            background: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
        }

        /* Right Panel - Illustration */
        .illustration-panel {
            flex: 1;
            background: linear-gradient(135deg, #f0f7ff 0%, #e8f0fe 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px;
            position: relative;
            overflow: hidden;
        }

        .illustration-container {
            width: 100%;
            max-width: 500px;
            position: relative;
        }

        /* Floating Elements */
        .float-element {
            position: absolute;
            animation: float 6s ease-in-out infinite;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-20px); }
        }

        .float-element:nth-child(2) { animation-delay: -2s; }
        .float-element:nth-child(3) { animation-delay: -4s; }

        /* Terminal Window */
        .terminal-window {
            background: #1e1e2e;
            border-radius: 16px;
            padding: 20px;
            box-shadow: 0 20px 60px rgba(44, 30, 116, 0.15);
            position: relative;
        }

        .terminal-header {
            display: flex;
            gap: 8px;
            margin-bottom: 16px;
        }

        .terminal-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
        }

        .terminal-dot.red { background: #ff5f56; }
        .terminal-dot.yellow { background: #ffbd2e; }
        .terminal-dot.green { background: #27ca40; }

        .terminal-content {
            font-family: 'SF Mono', 'Fira Code', monospace;
            font-size: 13px;
            color: #e2e8f0;
            line-height: 1.8;
        }

        .terminal-line {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .terminal-prompt {
            color: #3b82f6;
        }

        .terminal-command {
            color: #e2e8f0;
        }

        .terminal-output {
            color: #8e8e93;
            padding-left: 20px;
        }

        .terminalcursor {
            display: inline-block;
            width: 8px;
            height: 16px;
            background: #3b82f6;
            animation: blink 1s infinite;
        }

        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0; }
        }

        /* Code Blocks */
        .code-block {
            position: absolute;
            background: #ffffff;
            border-radius: 12px;
            padding: 16px;
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
        }

        .code-block-1 {
            top: -30px;
            right: -40px;
            animation: float 5s ease-in-out infinite;
        }

        .code-block-2 {
            bottom: -20px;
            left: -50px;
            animation: float 7s ease-in-out infinite;
        }

        .code-block-header {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 8px;
        }

        .code-block-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #3b82f6;
        }

        .code-block-title {
            font-size: 11px;
            color: #8e8e93;
            font-family: 'DM Sans', sans-serif;
        }

        .code-content {
            font-family: 'SF Mono', 'Fira Code', monospace;
            font-size: 12px;
            color: #45515e;
            line-height: 1.6;
        }

        .code-keyword { color: #3b82f6; }
        .code-string { color: #27ca40; }
        .code-function { color: #8b5cf6; }
        .code-comment { color: #8e8e93; }

        /* Git Branch */
        .git-branch {
            position: absolute;
            right: 30px;
            top: 50%;
            transform: translateY(-50%);
        }

        .branch-line {
            width: 3px;
            height: 120px;
            background: linear-gradient(to bottom, #3b82f6, #60a5fa);
            border-radius: 2px;
            position: relative;
        }

        .branch-node {
            position: absolute;
            width: 14px;
            height: 14px;
            background: #3b82f6;
            border-radius: 50%;
            border: 3px solid #ffffff;
            box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
        }

        .branch-node:nth-child(1) { top: 10px; left: -5.5px; }
        .branch-node:nth-child(2) { top: 50px; left: -5.5px; background: #27ca40; }
        .branch-node:nth-child(3) { top: 90px; left: -5.5px; }

        .branch-commit {
            position: absolute;
            left: 20px;
            font-family: 'DM Sans', sans-serif;
            font-size: 11px;
            color: #45515e;
            background: #ffffff;
            padding: 4px 8px;
            border-radius: 6px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .branch-commit:nth-child(4) { top: 5px; }
        .branch-commit:nth-child(5) { top: 45px; }
        .branch-commit:nth-child(6) { top: 85px; }

        /* Team Silhouettes */
        .team-section {
            position: absolute;
            bottom: 40px;
            left: 40px;
            display: flex;
            gap: -10px;
        }

        .team-member {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            display: flex;
            align-items: center;
            justify-content: center;
            border: 3px solid #ffffff;
            margin-left: -10px;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }

        .team-member:first-child {
            margin-left: 0;
        }

        .team-member i {
            color: white;
            font-size: 18px;
        }

        .team-member:nth-child(2) { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
        .team-member:nth-child(3) { background: linear-gradient(135deg, #27ca40, #16a34a); }

        /* Decorative Shapes */
        .shape {
            position: absolute;
            border-radius: 50%;
            opacity: 0.6;
        }

        .shape-1 {
            width: 200px;
            height: 200px;
            background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(96, 165, 250, 0.1));
            top: -80px;
            right: -80px;
        }

        .shape-2 {
            width: 150px;
            height: 150px;
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(167, 139, 250, 0.1));
            bottom: -60px;
            left: -60px;
        }

        .shape-3 {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, rgba(39, 202, 64, 0.1), rgba(22, 163, 74, 0.1));
            top: 40%;
            left: 20px;
        }

        /* Responsive */
        @media (max-width: 1024px) {
            .illustration-panel {
                display: none;
            }
            
            .login-panel {
                flex: none;
                width: 100%;
            }
        }

        @media (max-width: 480px) {
            .login-panel {
                padding: 24px;
            }
            
            .brand-name {
                font-size: 24px;
            }
        }
    </style>
</head>

<body>
    <div class="login-container">
        <!-- Left Panel - Login Form -->
        <div class="login-panel">
            <div class="login-form-wrapper">
                <div class="login-header">
                    <div class="brand-logo">
                        <img src="${pageContext.request.contextPath}/images/IMG_20260419_175314.jpg" alt="Logo" class="brand-icon">
                        <span class="brand-name">软件小组</span>
                    </div>
                </div>

                <h2 class="form-title">登录您的账户</h2>

                <c:if test="${not empty error}">
                    <div class="alert-custom alert-danger-custom">
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span>${error}</span>
                    </div>
                </c:if>

                <form action="login" method="post" autocomplete="off" novalidate>
                    <div class="input-group-custom">
                        <input type="text" name="username" class="form-control" placeholder="请输入用户名" autocomplete="off" required>
                        <i class="bi bi-person input-icon"></i>
                    </div>

                    <div class="input-group-custom">
                        <input type="password" name="password" class="form-control" placeholder="请输入密码" autocomplete="off" required>
                        <i class="bi bi-lock input-icon"></i>
                    </div>

                    <button type="submit" class="btn-submit">
                        登 录
                    </button>
                </form>

                <div class="login-divider">
                    <span>或</span>
                </div>

                <div class="login-footer">
                    <p class="footer-text">
                        还没有账户? <a href="${pageContext.request.contextPath}/recruit/apply.jsp" class="footer-link">立即报名</a>
                    </p>
                    <a href="${pageContext.request.contextPath}/index.jsp" class="back-home">
                        <i class="bi bi-house"></i>
                        返回首页
                    </a>
                </div>
            </div>
        </div>

        <!-- Right Panel - Illustration -->
        <div class="illustration-panel">
            <div class="illustration-container">
                <!-- Decorative Shapes -->
                <div class="shape shape-1"></div>
                <div class="shape shape-2"></div>
                <div class="shape shape-3"></div>

                <!-- Main Terminal -->
                <div class="terminal-window float-element">
                    <div class="terminal-header">
                        <div class="terminal-dot red"></div>
                        <div class="terminal-dot yellow"></div>
                        <div class="terminal-dot green"></div>
                    </div>
                    <div class="terminal-content">
                        <div class="terminal-line">
                            <span class="terminal-prompt">$</span>
                            <span class="terminal-command">git status</span>
                        </div>
                        <div class="terminal-output">On branch main</div>
                        <div class="terminal-output" style="color: #27ca40;">✓ Changes ready to commit</div>
                        <br>
                        <div class="terminal-line">
                            <span class="terminal-prompt">$</span>
                            <span class="terminal-command">npm run dev</span>
                        </div>
                        <div class="terminal-output">Server running at localhost:8080<span class="terminalcursor"></span></div>
                    </div>
                </div>

                <!-- Code Block 1 -->
                <div class="code-block code-block-1">
                    <div class="code-block-header">
                        <div class="code-block-dot"></div>
                        <span class="code-block-title">TeamCollaboration.java</span>
                    </div>
                    <div class="code-content">
                        <span class="code-keyword">public class</span> <span class="code-function">Team</span> {<br>
                        &nbsp;&nbsp;<span class="code-keyword">void</span> <span class="code-function">collaborate</span>() {<br>
                        &nbsp;&nbsp;&nbsp;&nbsp;<span class="code-comment">// 一起编码</span><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;<span class="code-string">"Hello World"</span>;<br>
                        &nbsp;&nbsp;}<br>
                        }
                    </div>
                </div>

                <!-- Code Block 2 -->
                <div class="code-block code-block-2">
                    <div class="code-block-header">
                        <div class="code-block-dot" style="background: #27ca40;"></div>
                        <span class="code-block-title">Activity.java</span>
                    </div>
                    <div class="code-content">
                        <span class="code-keyword">@Entity</span><br>
                        <span class="code-keyword">public class</span> <span class="code-function">Activity</span> {<br>
                        &nbsp;&nbsp;<span class="code-keyword">String</span> title;<br>
                        &nbsp;&nbsp;<span class="code-keyword">Date</span> startDate;<br>
                        }
                    </div>
                </div>

                <!-- Git Branch -->
                <div class="git-branch float-element">
                    <div class="branch-line">
                        <div class="branch-node"></div>
                        <div class="branch-node"></div>
                        <div class="branch-node"></div>
                    </div>
                    <div class="branch-commit">commit abc123</div>
                    <div class="branch-commit">merge dev</div>
                    <div class="branch-commit">push main</div>
                </div>

                <!-- Team Section -->
                <div class="team-section">
                    <div class="team-member">
                        <i class="bi bi-person"></i>
                    </div>
                    <div class="team-member">
                        <i class="bi bi-person"></i>
                    </div>
                    <div class="team-member">
                        <i class="bi bi-person"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>
</body>

</html>
