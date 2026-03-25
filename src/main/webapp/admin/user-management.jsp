<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="jsp/common/layout_top.jsp">
    <jsp:param name="title" value="用户管理" />
    <jsp:param name="active" value="user" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">用户管理</h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card">
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty users}">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table table-striped">
                                <thead>
                                    <tr>
                                        <th>用户名</th>
                                        <th>姓名</th>
                                        <th>角色</th>
                                        <th>状态</th>
                                        <th>注册时间</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="user" items="${users}">
                                        <tr>
                                            <td>${user.username}</td>
                                            <td>${user.name}</td>
                                            <td>
                                                <span class="badge bg-${user.role == 'ADMIN' ? 'red' : 'blue'}">
                                                    ${user.role}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge bg-${user.status == 'ACTIVE' ? 'success' : 'secondary'}">
                                                    ${user.status}
                                                </span>
                                            </td>
                                            <td>${user.createdAt}</td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/member/view?id=${user.id}" 
                                                   class="btn btn-sm btn-primary">查看</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty">
                            <p class="empty-title">暂无用户</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="jsp/common/layout_bottom.jsp" />
