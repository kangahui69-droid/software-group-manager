<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目历史 - ${project.name}" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    项目历史
                </h2>
                <div class="text-muted">${project.name}</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn btn-outline-primary">
                    返回项目
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">项目历程</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty historyList}">
                            <div class="text-center text-muted py-5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                <p class="mt-2">暂无历史记录</p>
                            </div>
                        </c:if>
                        
                        <div class="timeline">
                            <c:forEach var="history" items="${historyList}">
                            <div class="timeline-event">
                                <div class="timeline-event-icon bg-${history.operationType == 'PROJECT_APPROVE' ? 'success' : 
                                    history.operationType == 'PROJECT_REJECT' ? 'danger' : 
                                    history.operationType == 'MEMBER_APPROVE' ? 'success' :
                                    history.operationType == 'MEMBER_REJECT' ? 'danger' : 'primary'}">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-history" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                </div>
                                <div class="timeline-event-content card">
                                    <div class="card-body">
                                        <div class="row align-items-center">
                                            <div class="col">
                                                <div class="mb-1">
                                                    <c:choose>
                                                        <c:when test="${history.operationType == 'PROJECT_APPLY'}">项目申请</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_APPROVE'}">项目审批通过</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_REJECT'}">项目审批驳回</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_TRANSFER'}">项目管理员转移</c:when>
                                                        <c:when test="${history.operationType == 'MEMBER_APPLY'}">成员申请</c:when>
                                                        <c:when test="${history.operationType == 'MEMBER_APPROVE'}">成员审批通过</c:when>
                                                        <c:when test="${history.operationType == 'MEMBER_REJECT'}">成员审批驳回</c:when>
                                                        <c:when test="${history.operationType == 'MEMBER_JOIN'}">成员加入</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_LABEL_ADD'}">添加项目标签</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_LABEL_REMOVE'}">移除项目标签</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_INFO_UPDATE'}">项目信息更新</c:when>
                                                        <c:when test="${history.operationType == 'PROJECT_STATUS_CHANGE'}">项目状态变更</c:when>
                                                        <c:otherwise>${history.operationType}</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="text-muted small">${history.description}</div>
                                                <div class="text-muted small mt-1">
                                                    <span class="me-2">操作人: ${history.operatorName}</span>
                                                    <span><fmt:formatDate value="${history.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
.timeline {
    position: relative;
    padding-left: 2rem;
}
.timeline::before {
    content: '';
    position: absolute;
    left: 0.75rem;
    top: 0;
    bottom: 0;
    width: 2px;
    background: var(--tblr-border-color);
}
.timeline-event {
    position: relative;
    padding-bottom: 1.5rem;
}
.timeline-event:last-child {
    padding-bottom: 0;
}
.timeline-event-icon {
    position: absolute;
    left: -2rem;
    width: 1.5rem;
    height: 1.5rem;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}
.timeline-event-content {
    margin-left: 0.5rem;
}
</style>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />