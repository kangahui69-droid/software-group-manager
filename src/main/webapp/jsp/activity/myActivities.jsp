<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="我的活动" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    我的活动
                </h2>
                <p class="text-muted">查看我的活动报名记录及状态</p>
            </div>
            <div class="col-auto btn-list">
                <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-primary">
                    查看最新活动
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <!-- 状态筛选 -->
        <div class="card mb-3">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/activity" method="get" class="row g-3">
                    <input type="hidden" name="action" value="myActivities">
                    <div class="col-md-4">
                        <select name="status" class="form-select">
                            <option value="">全部状态</option>
                            <option value="pending" ${statusFilter == 'pending' ? 'selected' : ''}>待审核</option>
                            <option value="confirmed" ${statusFilter == 'confirmed' ? 'selected' : ''}>已确认</option>
                            <option value="rejected" ${statusFilter == 'rejected' ? 'selected' : ''}>已驳回</option>
                            <option value="expired" ${statusFilter == 'expired' ? 'selected' : ''}>已过期</option>
                            <option value="activityEnded" ${statusFilter == 'activityEnded' ? 'selected' : ''}>活动已结束</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">筛选</button>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="btn btn-outline-secondary w-100">重置</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- 提示消息 -->
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

        <!-- 报名列表 -->
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>活动地点</th>
                            <th>活动时间</th>
                            <th>报名时间</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="reg" items="${registrations}">
                            <tr>
                                <td>
                                    <strong>${reg.activityName}</strong>
                                </td>
                                <td class="text-muted">${reg.location}</td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${reg.activityStartTime}" pattern="MM-dd HH:mm" /> ~ 
                                    <fmt:formatDate value="${reg.activityEndTime}" pattern="MM-dd HH:mm" />
                                </td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${reg.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${reg.displayStatus == 'pending'}">
                                            <span class="badge bg-warning text-dark">待审核</span>
                                        </c:when>
                                        <c:when test="${reg.displayStatus == 'confirmed'}">
                                            <span class="badge bg-success text-white">已确认</span>
                                        </c:when>
                                        <c:when test="${reg.displayStatus == 'rejected'}">
                                            <span class="badge bg-danger text-white">已驳回</span>
                                            <c:if test="${not empty reg.notes}">
                                                <br><small class="text-muted">原因: ${reg.notes}</small>
                                            </c:if>
                                        </c:when>
                                        <c:when test="${reg.displayStatus == 'expired'}">
                                            <span class="badge bg-secondary text-white">已过期</span>
                                        </c:when>
                                        <c:when test="${reg.displayStatus == 'activityEnded'}">
                                            <span class="badge bg-secondary text-white">活动已结束</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary text-white">未知</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${reg.status == 'pending' && not reg.registrationClosed}">
                                        <a href="${pageContext.request.contextPath}/activity?action=cancel&activityId=${reg.activityId}" 
                                           class="btn btn-outline-danger btn-sm"
                                           onclick="return confirm('确定要取消报名吗？')">
                                            取消报名
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/activity?action=detail&id=${reg.activityId}&fromMyActivities=true" class="btn btn-outline-primary btn-sm">
                                        查看详情
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty registrations}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">
                                    <div class="empty">
                                        <p class="empty-title">暂无报名记录</p>
                                        <p class="empty-subtitle text-muted">快去报名感兴趣的活动吧！</p>
                                        <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-primary">查看活动列表</a>
                                    </div>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- 状态说明 -->
        <div class="card mt-3">
            <div class="card-header">
                <h3 class="card-title">状态说明</h3>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3">
                        <span class="badge bg-warning text-dark me-1">待审核</span>
                        <span class="text-muted">报名已提交，等待管理员审核</span>
                    </div>
                    <div class="col-md-3">
                        <span class="badge bg-success text-white me-1">已确认</span>
                        <span class="text-muted">报名审核通过</span>
                    </div>
                    <div class="col-md-3">
                        <span class="badge bg-danger text-white me-1">已驳回</span>
                        <span class="text-muted">报名未通过审核</span>
                    </div>
                    <div class="col-md-3">
                        <span class="badge bg-secondary text-white me-1">已过期</span>
                        <span class="text-muted">报名已截止且未处理</span>
                    </div>
                    <div class="col-md-3">
                        <span class="badge bg-secondary text-white me-1">活动已结束</span>
                        <span class="text-muted">活动已结束/已取消/进行中</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
