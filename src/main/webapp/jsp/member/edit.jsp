<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="编辑成员" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    编辑成员信息
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/admin/member/edit" method="POST" class="card">
                    <div class="card-header">
                        <h3 class="card-title">成员信息</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>
                        <input type="hidden" name="id" value="${user.id}">
                        <div class="mb-3">
                            <label class="form-label required">用户名</label>
                            <input type="text" class="form-control" name="username" value="${user.username}" placeholder="请输入用户名" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label required">角色</label>
                            <select class="form-select" name="role" required>
                                <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                                <option value="MEMBER" ${user.role == 'MEMBER' ? 'selected' : ''}>成员</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">保存更改</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />