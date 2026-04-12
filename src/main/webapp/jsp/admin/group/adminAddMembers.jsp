<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="添加群聊成员" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    添加群聊成员 - ${group.groupName}
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-2"></i>返回群聊
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card mb-3">
            <div class="card-header">
                <h3 class="card-title">群信息</h3>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">群聊名称</label>
                            <input type="text" class="form-control" value="${group.groupName}" readonly>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">所属活动</label>
                            <input type="text" class="form-control" value="${group.activityName}" readonly>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-3">
            <div class="card-header">
                <h3 class="card-title">
                    <c:choose>
                        <c:when test="${not empty participants}">
                            已报名的参与者 (${not empty participants ? participants.size() : 0})
                        </c:when>
                        <c:otherwise>
                            暂无参与者可添加
                        </c:otherwise>
                    </c:choose>
                </h3>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty participants}">
                        <p class="text-muted">该群聊没有关联的活动，或活动暂无报名参与者。</p>
                        <p class="text-muted">请通过其他方式邀请成员。</p>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                            <input type="hidden" name="action" value="addMultipleMembers">
                            <input type="hidden" name="groupId" value="${group.id}">
                            
                            <div class="mb-3">
                                <label class="form-label">选择要加入群聊的参与者（默认全部选中）</label>
                                <div class="form-selectgroup form-selectgroup-pills">
                                    <c:forEach var="p" items="${participants}">
                                        <c:if test="${!existingMemberIds.contains(p.userId)}">
                                            <label class="form-selectgroup-item">
                                                <input type="checkbox" name="selectedUsers" value="${p.userId}" class="form-selectgroup-input" checked>
                                                <span class="form-selectgroup-label">
                                                    ${not empty p.userName ? p.userName : '用户'}${p.userId}
                                                    <c:if test="${not empty p.studentId}"> (${p.studentId})</c:if>
                                                    <c:if test="${p.status == 'pending'}"> [待审核]</c:if>
                                                </span>
                                            </label>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <button type="submit" class="btn btn-primary">添加选中成员</button>
                                <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn btn-outline-secondary">取消</a>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <c:if test="${not empty existingMemberIds}">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">已在群中的成员</h3>
            </div>
            <div class="card-body">
                <p class="text-muted">以下成员已在群中：</p>
                <ul>
                    <c:forEach var="member" items="${participants}">
                        <c:if test="${existingMemberIds.contains(member.userId)}">
                            <li>${not empty member.userName ? member.userName : '用户'}${member.userId}</li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        </c:if>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />