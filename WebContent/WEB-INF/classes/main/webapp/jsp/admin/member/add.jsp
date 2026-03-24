<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="添加成员" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    添加成员
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <form method="post" action="${pageContext.request.contextPath}/admin/member/" class="card" id="addMemberForm">
                    <div class="card-header">
                        <h3 class="card-title">填写成员信息</h3>
                    </div>
                    <div class="card-body">
                        <input type="hidden" name="action" value="add">

                        <!-- 错误提示 -->
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-1"></i>
                                ${error}
                            </div>
                        </c:if>

                        <div class="mb-3">
                            <label class="form-label required">真实姓名</label>
                            <input type="text" class="form-control" name="name"
                                   value="${not empty param.name ? param.name : ''}"
                                   placeholder="请输入真实姓名" required maxlength="50"
                                   oninvalid="this.setCustomValidity('真实姓名不能为空')"
                                   oninput="this.setCustomValidity('')">
                            <div class="form-hint">最多50个字符</div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">学号</label>
                                    <input type="text" class="form-control" name="username"
                                           value="${not empty param.username ? param.username : ''}"
                                           placeholder="请输入学号" required minlength="6" maxlength="20"
                                           pattern="[a-zA-Z0-9]+"
                                           oninvalid="this.setCustomValidity('请输入6-20位字母或数字组成的学号')"
                                           oninput="this.setCustomValidity('')">
                                    <div class="form-hint">6-20位字母或数字</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">密码</label>
                                    <input type="password" class="form-control" name="password"
                                           id="password" placeholder="请输入密码" required minlength="6" maxlength="20"
                                           oninvalid="this.setCustomValidity('密码长度6-20位')"
                                           oninput="this.setCustomValidity('')">
                                    <div class="form-hint">6-20位字符</div>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">角色</label>
                                    <select class="form-select" name="role" required>
                                        <option value="MEMBER" ${param.role == 'MEMBER' ? 'selected' : ''}>普通成员</option>
                                        <option value="ADMIN" ${param.role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">用户类型</label>
                                    <select class="form-select" name="userType" required>
                                        <option value="STUDENT" ${param.userType == 'STUDENT' ? 'selected' : ''}>学生</option>
                                        <option value="TEACHER" ${param.userType == 'TEACHER' ? 'selected' : ''}>教师</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">手机号</label>
                                    <input type="tel" class="form-control" name="phone"
                                           value="${not empty param.phone ? param.phone : ''}"
                                           placeholder="请输入手机号" required pattern="1[3-9]\d{9}"
                                           oninvalid="this.setCustomValidity('请输入正确的11位手机号')"
                                           oninput="this.setCustomValidity('')">
                                    <div class="form-hint">11位手机号码</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">邮箱</label>
                                    <input type="email" class="form-control" name="email"
                                           value="${not empty param.email ? param.email : ''}"
                                           placeholder="选填" pattern="[\w.-]+@[\w.-]+\.\w+"
                                           oninvalid="this.setCustomValidity('请输入正确的邮箱格式')"
                                           oninput="this.setCustomValidity('')">
                                    <div class="form-hint">选填，邮箱格式</div>
                                </div>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">状态</label>
                            <select class="form-select" name="status">
                                <option value="1" ${param.status == '1' || empty param.status ? 'selected' : ''}>启用</option>
                                <option value="0" ${param.status == '0' ? 'selected' : ''}>禁用</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">保存</button>
                        </div>
                    </div>
                </form>
            </div>
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-body">
                        <h3 class="card-title">添加成员须知</h3>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item">
                                <i class="bi bi-info-circle me-2 text-info"></i>
                                学号作为用户名，不能重复，用于登录系统
                            </li>
                            <li class="list-group-item">
                                <i class="bi bi-info-circle me-2 text-info"></i>
                                密码会自动加密存储，建议设置强密码
                            </li>
                            <li class="list-group-item">
                                <i class="bi bi-info-circle me-2 text-info"></i>
                                角色选择后可以修改，管理员拥有全部权限
                            </li>
                            <li class="list-group-item">
                                <i class="bi bi-info-circle me-2 text-info"></i>
                                用户类型区分教师和学生
                            </li>
                            <li class="list-group-item">
                                <i class="bi bi-exclamation-triangle me-2 text-warning"></i>
                                <strong>注意：</strong>学号设置后无法修改
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 表单校验增强脚本 -->
<script>
document.addEventListener('DOMContentLoaded', function() {
    var form = document.getElementById('addMemberForm');

    form.addEventListener('submit', function(e) {
        var password = form.querySelector('[name="password"]');
        if (password.value.length < 6) {
            alert('密码长度不能少于6位');
            password.focus();
            e.preventDefault();
            return false;
        }

        var phone = form.querySelector('[name="phone"]');
        if (!/^1[3-9]\d{9}$/.test(phone.value)) {
            alert('请输入正确的手机号（11位数字）');
            phone.focus();
            e.preventDefault();
            return false;
        }

        var username = form.querySelector('[name="username"]');
        if (!/^[a-zA-Z0-9]{6,20}$/.test(username.value)) {
            alert('学号格式不正确，请输入6-20位字母或数字');
            username.focus();
            e.preventDefault();
            return false;
        }

        var email = form.querySelector('[name="email"]');
        if (email.value && !/^[\w.-]+@[\w.-]+\.\w+$/.test(email.value)) {
            alert('邮箱格式不正确');
            email.focus();
            e.preventDefault();
            return false;
        }
    });
});
</script>

<jsp:include page="../../common/layout_bottom.jsp" />
