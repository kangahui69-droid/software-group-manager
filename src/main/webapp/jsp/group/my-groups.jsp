<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="我的群聊" />
</jsp:include>

<style>
.group-card {
    transition: all 0.3s ease;
}
.group-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.group-icon {
    width: 50px;
    height: 50px;
    border-radius: 10px;
    background: linear-gradient(135deg, #206bc4, #329BEF);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
}
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">我的群聊</h2>
                <div class="text-muted">您加入的所有群聊</div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:choose>
            <c:when test="${empty userGroups}">
                <div class="card">
                    <div class="card-body text-center py-5">
                        <div class="empty-img mb-3">
                            <i class="bi bi-chat-dots text-muted" style="font-size: 64px;"></i>
                        </div>
                        <h3 class="mb-2">暂无群聊</h3>
                        <p class="text-muted mb-4">您还没有加入任何群聊。请先发起活动，活动通过审核后可创建群聊。</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row row-cards">
                    <c:forEach var="ug" items="${userGroups}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card group-card">
                                <div class="card-body">
                                    <div class="d-flex align-items-center mb-3">
                                        <a href="${pageContext.request.contextPath}/group/chat/${ug.groupId}" class="text-decoration-none text-reset flex-fill">
                                            <div class="group-icon me-3 d-inline-flex">
                                                <i class="bi bi-people"></i>
                                            </div>
                                            <div class="d-inline-block">
                                                <h4 class="mb-1">${ug.groupName}</h4>
                                                <c:if test="${not empty ug.activityName}">
                                                    <small class="text-muted">${ug.activityName}</small>
                                                </c:if>
                                            </div>
                                        </a>
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center text-muted">
                                        <span><i class="bi bi-people me-1"></i> ${ug.memberCount} 人</span>
                                        <span><i class="bi bi-clock me-1"></i> 
                                            <fmt:formatDate value="${ug.joinedAt}" pattern="MM-dd HH:mm" />
                                        </span>
                                    </div>
                                    <c:if test="${sessionScope.user.id == ug.ownerId}">
                                        <div class="mt-3">
                                            <button type="button" class="btn btn-danger btn-sm w-100" onclick="confirmDisband(${ug.groupId}, '${ug.groupName}')">
                                                <i class="bi bi-trash me-1"></i>解散群聊
                                            </button>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
function confirmDisband(groupId, groupName) {
    if (confirm('确定要解散群聊"' + groupName + '"吗？此操作不可恢复！')) {
        window.location.href = '${pageContext.request.contextPath}/group?action=deleteGroup&groupId=' + groupId;
    }
}
</script>

<jsp:include page="../common/layout_bottom.jsp" />
