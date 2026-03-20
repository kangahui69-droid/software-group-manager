<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../common/layout_top.jsp">
            <jsp:param name="title" value="成员管理" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            成员管理
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${pageContext.request.contextPath}/admin/member/add" class="btn btn-primary">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                            添加成员
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="card">
                    <div class="table-responsive">
                        <table class="table table-vcenter card-table table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>用户名</th>
                                    <th>角色</th>
                                    <th>状态</th>
                                    <th>创建时间</th>
                                    <th class="w-1">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="u" items="${userList}">
                                    <tr>
                                        <td>${u.id}</td>
                                        <td>${u.username}</td>
                                        <td class="text-muted">
                                            <span
                                                class="badge bg-blue-lt text-capitalize">${u.role.toLowerCase()}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${u.status == 1}">
                                                    <span class="badge bg-success">启用</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">禁用</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-muted">${u.createdAt}</td>
                                        <td>
                                            <div class="btn-list flex-nowrap">
                                                <a href="${pageContext.request.contextPath}/admin/member/edit?id=${u.id}" 
                                                   class="btn btn-white btn-sm">编辑</a>
                                                <a href="${pageContext.request.contextPath}/admin/member/toggleStatus?id=${u.id}" 
                                                   class="btn btn-white btn-sm">
                                                    ${u.status == 1 ? '禁用' : '启用'}
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/member/delete?id=${u.id}" 
                                                   class="btn btn-white btn-sm" onclick="return confirm('确定要删除这个成员吗？');">删除</a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty userList}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted">暂无成员记录</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../common/layout_bottom.jsp" />