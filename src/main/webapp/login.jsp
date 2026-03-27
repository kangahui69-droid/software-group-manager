<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
            <meta http-equiv="X-UA-Compatible" content="ie=edge" />
            <title>用户登录 - 软件小组管理系统</title>
            <!-- Tabler Core CSS -->
            <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');

                :root {
                    --tblr-font-sans-serif: 'Inter', -apple-system, BlinkMacSystemFont, San Francisco, Segoe UI, Roboto, Helvetica Neue, sans-serif;
                }
            </style>
        </head>

        <body class="d-flex flex-column bg-light">
            <div class="page page-center">
                <div class="container container-tight py-4">
                    <div class="text-center mb-4">
                        <a href="${pageContext.request.contextPath}/index.jsp"
                            class="navbar-brand navbar-brand-autodark">
                            <i class="bi bi-cpu text-primary me-2 h1 mb-0"></i>
                            <span class="h1 mb-0">软件小组</span>
                        </a>
                    </div>
                    <div class="card card-md">
                        <div class="card-body">
                            <h2 class="h2 text-center mb-4">登录您的账户</h2>
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger" role="alert">
                                    ${error}
                                </div>
                            </c:if>
                            <form action="login" method="post" autocomplete="off" novalidate>
                                <div class="mb-3">
                                    <label class="form-label">用户名</label>
                                    <input type="text" name="username" class="form-control" placeholder="请输入用户名"
                                        autocomplete="off" required>
                                </div>
                                <div class="mb-2">
                                    <label class="form-label">
                                        密码
                                    </label>
                                    <input type="password" name="password" class="form-control" placeholder="请输入密码"
                                        autocomplete="off" required>
                                </div>
                                <div class="form-footer">
                                    <button type="submit" class="btn btn-primary w-100">登录</button>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="text-center text-muted mt-3">
                        还没有账户? <a href="${pageContext.request.contextPath}/recruit/apply.jsp" tabindex="-1">立即报名</a>
                    </div>

                </div>
            </div>
            <!-- Tabler Core JS -->
            <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>
        </body>

        </html>