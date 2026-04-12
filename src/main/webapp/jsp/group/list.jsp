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
    <jsp:param name="title" value="群聊列表" />
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
                <h2 class="page-title">群聊列表</h2>
                <div class="text-muted">所有可用群聊</div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:choose>
            <c:when test="${empty groups}">
                <div class="card">
                    <div class="card-body text-center py-5">
                        <div class="empty-img mb-3">
                            <i class="bi bi-chat-dots text-muted" style="font-size: 64px;"></i>
                        </div>
                        <h3 class="mb-2">暂无群聊</h3>
                        <p class="text-muted mb-4">目前没有可加入的群聊。</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row row-cards">
                    <c:forEach var="group" items="${groups}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card group-card">
                                <div class="card-body">
                                    <div class="d-flex align-items-center mb-3">
                                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" class="text-decoration-none text-reset flex-fill">
                                            <div class="group-icon me-3 d-inline-flex">
                                                <i class="bi bi-people"></i>
                                            </div>
                                            <div class="d-inline-block">
                                                <h4 class="mb-1">${group.groupName}</h4>
                                            </div>
                                        </a>
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center text-muted">
                                        <span><i class="bi bi-clock me-1"></i> 
                                            <fmt:formatDate value="${group.createdAt}" pattern="MM-dd HH:mm" />
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
