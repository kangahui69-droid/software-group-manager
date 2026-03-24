<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // 检查是否为必须修改密码的场景
    boolean mustChange = "1".equals(request.getParameter("mustChange"));
    pageContext.setAttribute("mustChange", mustChange);
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="修改密码" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-12">
            <div class="page-header">
                <h1 class="page-title">修改密码</h1>
                <div class="page-subtitle">${mustChange ? '首次登录，请设置您的新密码' : '保障账户安全'}</div>
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
                    <c:if test="${mustChange}">
                        <div class="alert alert-warning mb-4">
                            <strong>温馨提示：</strong>您的账户设置了临时密码，为保障账户安全，请立即修改密码。
                        </div>
                    </c:if>
                    <form id="passwordForm" method="post"
                        action="${pageContext.request.contextPath}/member/password">
                        <div class="mb-4">
                            <h3 class="mb-3">密码设置</h3>
                            <c:choose>
                                <c:when test="${mustChange}">
                                    <%-- 必须修改密码时隐藏原密码输入 --%>
                                </c:when>
                                <c:otherwise>
                                    <div class="row">
                                        <div class="col-md-12 mb-3">
                                            <label class="form-label">原密码</label>
                                            <input type="password" class="form-control"
                                                name="oldPassword" placeholder="请输入当前密码" required>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">新密码</label>
                                    <input type="password" class="form-control"
                                        name="newPassword" placeholder="请输入新密码（不少于6位）" required minlength="6">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label class="form-label">确认新密码</label>
                                    <input type="password" class="form-control"
                                        name="confirmPassword" placeholder="请再次输入新密码" required>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end">
                            <c:choose>
                                <c:when test="${mustChange}">
                                    <%-- 必须修改密码时隐藏返回按钮 --%>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/member/profile.jsp"
                                        class="btn btn-secondary me-2">返回</a>
                                </c:otherwise>
                            </c:choose>
                            <button type="submit" class="btn btn-primary">修改密码</button>
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
                        <c:choose>
                            <c:when test="${mustChange}">
                                <li class="list-group-item">
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                        class="icon me-2 text-warning" width="20" height="20"
                                        viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                        fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <circle cx="12" cy="12" r="9"></circle>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                        <polyline points="11 12 12 12 12 16 13 16"></polyline>
                                    </svg>
                                    系统检测到您首次登录，请设置新密码
                                </li>
                            </c:when>
                            <c:otherwise>
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
                            </c:otherwise>
                        </c:choose>
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
                        <li class="list-group-item">
                            <svg xmlns="http://www.w3.org/2000/svg"
                                class="icon me-2 text-info" width="20" height="20"
                                viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
                                fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                            </svg>
                            建议使用字母、数字组合的强密码
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.getElementById('passwordForm').addEventListener('submit', function (e) {
        const newPassword = this.newPassword.value;
        const confirmPassword = this.confirmPassword.value;

        // 如果有原密码输入框则验证
        const oldPasswordInput = this.oldPassword;
        if (oldPasswordInput && !oldPasswordInput.value) {
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
