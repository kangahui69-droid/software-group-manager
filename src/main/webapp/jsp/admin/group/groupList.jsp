<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊管理" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    群聊管理
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
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
                            <th>群主</th>
                            <th>成员数</th>
                            <th>状态</th>
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
                                <td>${group.ownerName}</td>
                                <td>${group.memberCount}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${group.isMuted == 1}">
                                            <span class="badge bg-danger">已禁言</span>
                                            <c:if test="${group.mutedUntil != null}">
                                                <small class="text-muted d-block">至 ${group.mutedUntil}</small>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success">正常</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${group.createdAt}</td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" 
                                           class="btn btn-white btn-sm">详情</a>
                                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" 
                                           class="btn btn-outline-primary btn-sm">进入群聊</a>
                                        <button type="button" class="btn btn-danger btn-sm" 
                                                onclick="confirmDelete(${group.id}, '${group.groupName}')">删除</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty groups}">
                            <tr>
                                <td colspan="8" class="text-center text-muted">暂无群聊记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function confirmDelete(groupId, groupName) {
    if (confirm('确定要删除群聊"' + groupName + '"吗？此操作不可恢复！')) {
        window.location.href = '${pageContext.request.contextPath}/group/admin?action=delete&groupId=' + groupId;
    }
}
</script>

<jsp:include page="../../common/layout_bottom.jsp" />