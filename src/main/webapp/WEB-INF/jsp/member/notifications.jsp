<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="../../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的通知" />
</jsp:include>

<div class="page-body">
    <div class="container-xl">
        <div class="page-header d-print-none mb-4">
            <h1 class="page-title">我的通知</h1>
            <p class="text-muted mb-0">当前有 <c:out value="${unreadCount}" /> 条未读通知。打开详情后提示会消失。</p>
        </div>
        <div class="card">
            <div class="list-group list-group-flush">
                <c:choose>
                    <c:when test="${empty notifications}">
                        <div class="list-group-item text-muted py-5 text-center">暂无通知</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${notifications}" var="item">
                            <a class="list-group-item list-group-item-action py-3"
                               href="${pageContext.request.contextPath}${fn:escapeXml(item.targetUrl)}">
                                <div class="d-flex justify-content-between gap-3">
                                    <div>
                                        <c:if test="${item.unread}"><span class="badge publication-notification-unread me-2" style="background-color: #f4cbd1 !important; color: #923446 !important; border: 1px solid #e3a7b1;">未读</span></c:if>
                                        <span class="badge bg-blue-lt me-2">${item.sourceType eq 'NEWS' ? '新闻公告' : '活动'}</span>
                                        <strong><c:out value="${item.title}" /></strong>
                                    </div>
                                    <small class="text-muted text-nowrap"><fmt:formatDate value="${item.createdAt}" pattern="yyyy-MM-dd HH:mm" /></small>
                                </div>
                            </a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
<jsp:include page="../../../jsp/common/layout_bottom.jsp" />
