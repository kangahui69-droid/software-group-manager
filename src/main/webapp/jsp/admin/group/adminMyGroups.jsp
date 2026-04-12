<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="我的群聊" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    我的群聊
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=list" class="btn btn-outline-secondary">
                        <i class="bi bi-shield me-2"></i>群聊管理
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
         
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>群聊名称</th>
                            <th>所属活动</th>
                            <th>成员数</th>
                            <th>创建时间</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="group" items="${groups}">
                            <tr>
                                <td>${group.id}</td>
                                <td>${group.groupName}</td>
                                <td>${group.activityName}</td>
                                <td>${group.memberCount}</td>
                                <td>${group.createdAt}</td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" 
                                           class="btn btn-primary btn-sm">
                                            <i class="bi bi-chat-dots me-1"></i>进入群聊
                                        </a>
                                        <a href="${pageContext.request.contextPath}/group/admin?action=addMembers&groupId=${group.id}" 
                                           class="btn btn-outline-primary btn-sm">
                                            <i class="bi bi-person-plus me-1"></i>添加成员
                                        </a>
                                        <form action="${pageContext.request.contextPath}/group/admin" method="POST" class="d-inline">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="groupId" value="${group.id}">
                                            <input type="hidden" name="from" value="my">
                                            <button type="submit" class="btn btn-outline-danger btn-sm" onclick="return confirm('确定要删除该群聊吗？此操作不可恢复！')">
                                                <i class="bi bi-trash me-1"></i>删除
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty groups}">
                            <tr>
                                <td colspan="6" class="text-center text-muted">您还没有创建任何群聊</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />