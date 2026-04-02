<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="我发起的活动" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    我发起的活动
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity?action=createForm" class="btn btn-primary">
                    发起新活动
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible" role="alert">
                ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible" role="alert">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>类型</th>
                            <th>地点</th>
                            <th>审批状态</th>
                            <th>活动时间</th>
                            <th>报名人数</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${createdActivities}">
                            <tr>
                                <td><strong>${a.title}</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.activityType == 'LECTURE'}">讲座</c:when>
                                        <c:when test="${a.activityType == 'SEMINAR'}">讨论会</c:when>
                                        <c:when test="${a.activityType == 'TEA_PARTY'}">茶话会</c:when>
                                        <c:when test="${a.activityType == 'PROJECT_INTRO'}">项目介绍</c:when>
                                        <c:when test="${a.activityType == 'WORKSHOP'}">工作坊</c:when>
                                        <c:when test="${a.activityType == 'COMPETITION'}">竞赛</c:when>
                                        <c:when test="${a.activityType == 'TRAINING'}">培训</c:when>
                                        <c:when test="${a.activityType == 'TEAM_BUILDING'}">团建</c:when>
                                        <c:otherwise>其他</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-muted">${a.location}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.approvalStatus == 'approved'}">
                                            <span class="badge bg-success">已批准</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'pending'}">
                                            <span class="badge bg-warning text-dark">待审核</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'rejected'}">
                                            <span class="badge bg-danger">已拒绝</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${a.activityStartTime}" pattern="MM-dd HH:mm" />
                                </td>
                                <td class="text-muted">
                                    ${a.currentParticipants > 0 ? a.currentParticipants : 0} / ${a.maxParticipants > 0 ? a.maxParticipants : '不限'}
                                </td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/activity?action=detail&id=${a.id}" class="btn btn-outline-primary btn-sm">查看</a>
                                        <c:if test="${a.approvalStatus == 'approved'}">
                                            <a href="${pageContext.request.contextPath}/group?action=createForActivity&activityId=${a.id}" class="btn btn-primary btn-sm">创建群聊</a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty createdActivities}">
                            <tr>
                                <td colspan="7" class="text-center text-muted">您还没有发起过活动</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />