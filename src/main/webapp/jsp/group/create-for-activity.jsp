<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="为活动创建群聊" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    为活动创建群聊
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity?action=myCreatedActivities" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible" role="alert">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card mb-3">
            <div class="card-header">
                <h3 class="card-title">活动信息</h3>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">活动名称</label>
                            <input type="text" class="form-control" value="${activity.title}" readonly>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">活动地点</label>
                            <input type="text" class="form-control" value="${activity.location}" readonly>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-3">
            <div class="card-header">
                <h3 class="card-title">已报名的参与者 (${not empty participants ? participants.size() : 0})</h3>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty participants}">
                        <p class="text-muted">暂无已报名的参与者</p>
                        <form action="${pageContext.request.contextPath}/group/create" method="POST">
                            <input type="hidden" name="activityId" value="${activity.id}">
                            <input type="hidden" name="groupName" value="${activity.title}交流群">
                            <div class="mb-3">
                                <button type="submit" class="btn btn-primary">创建群聊（无参与者）</button>
                                <a href="${pageContext.request.contextPath}/activity?action=myCreatedActivities" class="btn btn-outline-secondary">取消</a>
                            </div>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/group/create" method="POST">
                            <input type="hidden" name="activityId" value="${activity.id}">
                            <input type="hidden" name="groupName" value="${activity.title}交流群">
                            
                            <div class="mb-3">
                                <label class="form-label">选择要加入群聊的参与者（默认全部选中）</label>
                                <div class="form-selectgroup form-selectgroup-pills">
                                    <c:forEach var="p" items="${participants}">
                                        <label class="form-selectgroup-item">
                                            <input type="checkbox" name="selectedUsers" value="${p.userId}" class="form-selectgroup-input" checked>
                                            <span class="form-selectgroup-label">
                                                ${not empty p.userName ? p.userName : '用户'}${p.userId}
                                                <c:if test="${not empty p.studentId}"> (${p.studentId})</c:if>
                                                <c:if test="${p.status == 'pending'}"> [待审核]</c:if>
                                            </span>
                                        </label>
                                    </c:forEach>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <button type="submit" class="btn btn-primary">创建群聊并邀请选中成员</button>
                                <a href="${pageContext.request.contextPath}/activity?action=myCreatedActivities" class="btn btn-outline-secondary">取消</a>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />