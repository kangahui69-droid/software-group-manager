<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="添加群成员" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">添加群成员</h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/group/chat/${groupId}" class="btn btn-outline-secondary">返回群聊</a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${empty group.activityId}">
            <div class="alert alert-warning">
                <p><strong>提示：</strong>此群聊未关联活动，无法通过报名成员添加。</p>
                <p>群信息：群ID=${groupId}, activityId=${group.activityId}</p>
            </div>
        </c:if>
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">选择要添加的成员</h3>
                <p class="text-muted">以下用户已报名此活动，可添加到群聊</p>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty participants}">
                        <div class="text-center text-muted py-4">
                            <p>暂无已报名该活动的用户</p>
                            <p><small>请确认：1. 活动已开始报名 2. 有用户报名此活动 3. 报名状态不是"已拒绝"</small></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/group/addMultiple" method="post">
                            <input type="hidden" name="groupId" value="${groupId}">
                            <div class="mb-3">
                                <label class="form-label">选择成员（${participants.size()}人，已加入${not empty existingMemberIds ? existingMemberIds.size() : 0}人）</label>
                                <div class="form-selectgroup form-selectgroup-pills">
                                    <c:forEach var="p" items="${participants}">
                                        <c:set var="alreadyInGroup" value="false"/>
                                        <c:forEach var="mid" items="${existingMemberIds}">
                                            <c:if test="${p.userId == mid}">
                                                <c:set var="alreadyInGroup" value="true"/>
                                            </c:if>
                                        </c:forEach>
                                        <label class="form-selectgroup-item">
                                            <input type="checkbox" name="selectedUsers" value="${p.userId}" class="form-selectgroup-input" ${alreadyInGroup ? 'disabled' : ''} ${alreadyInGroup ? '' : 'checked'}>
                                            <span class="form-selectgroup-label ${alreadyInGroup ? 'text-muted' : ''}">
                                                ${not empty p.userName ? p.userName : '用户'}${p.userId}
                                                <c:if test="${not empty p.studentId}"> (${p.studentId})</c:if>
                                                <c:if test="${p.status == 'pending'}"> [待审核]</c:if>
                                                <c:if test="${alreadyInGroup}"> ✓ 已加入</c:if>
                                            </span>
                                        </label>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="mt-3">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-plus-lg me-1"></i>添加选中成员
                                </button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />