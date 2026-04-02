<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="活动管理" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    活动管理
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity?action=create" class="btn btn-primary">
                    创建活动
                </a>
            </div>
        </div>
    </div>
</div>

<!-- 搜索栏 -->
<div class="page-body">
    <div class="container-xl">
        <div class="card mb-3">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/activity" method="get" class="row g-3">
                    <input type="hidden" name="action" value="manage">
                    <div class="col-md-3">
                        <input type="text" name="keyword" class="form-control" placeholder="搜索活动名称、描述、地点" value="${keyword}">
                    </div>
                    <div class="col-md-2">
                        <select name="activityType" class="form-select">
                            <option value="">所有类型</option>
                            <option value="LECTURE" ${activityType == 'LECTURE' ? 'selected' : ''}>讲座</option>
                            <option value="SEMINAR" ${activityType == 'SEMINAR' ? 'selected' : ''}>讨论会</option>
                            <option value="TEA_PARTY" ${activityType == 'TEA_PARTY' ? 'selected' : ''}>茶话会</option>
                            <option value="PROJECT_INTRO" ${activityType == 'PROJECT_INTRO' ? 'selected' : ''}>项目介绍</option>
                            <option value="WORKSHOP" ${activityType == 'WORKSHOP' ? 'selected' : ''}>工作坊</option>
                            <option value="COMPETITION" ${activityType == 'COMPETITION' ? 'selected' : ''}>竞赛</option>
                            <option value="TRAINING" ${activityType == 'TRAINING' ? 'selected' : ''}>培训</option>
                            <option value="TEAM_BUILDING" ${activityType == 'TEAM_BUILDING' ? 'selected' : ''}>团建</option>
                            <option value="OTHER" ${activityType == 'OTHER' ? 'selected' : ''}>其他</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <select name="status" class="form-select">
                            <option value="">所有状态</option>
                            <option value="upcoming" ${status == 'upcoming' ? 'selected' : ''}>即将开始</option>
                            <option value="ongoing" ${status == 'ongoing' ? 'selected' : ''}>进行中</option>
                            <option value="completed" ${status == 'completed' ? 'selected' : ''}>已结束</option>
                            <option value="canceled" ${status == 'canceled' ? 'selected' : ''}>已取消</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <select name="approvalStatus" class="form-select">
                            <option value="">所有审批</option>
                            <option value="pending" ${approvalStatus == 'pending' ? 'selected' : ''}>待审核</option>
                            <option value="approved" ${approvalStatus == 'approved' ? 'selected' : ''}>已批准</option>
                            <option value="rejected" ${approvalStatus == 'rejected' ? 'selected' : ''}>已拒绝</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">搜索</button>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/activity?action=manage" class="btn btn-outline-secondary w-100">重置</a>
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

        <!-- 活动列表 -->
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>类型</th>
                            <th>地点</th>
                            <th>审批状态</th>
                            <th>报名状态</th>
                            <th>活动时间</th>
                            <th>报名时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${activities}">
                            <tr>
                                <td>
                                    <strong>${a.title}</strong>
                                    <c:if test="${a.maxParticipants > 0}">
                                        <br><small class="text-muted">名额: ${a.maxParticipants}人</small>
                                    </c:if>
                                </td>
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
                                <td>
                                    <c:choose>
                                        <c:when test="${a.inRegistrationPeriod}">
                                            <span class="badge bg-success">报名中</span>
                                        </c:when>
                                        <c:when test="${a.registrationEnded}">
                                            <span class="badge bg-secondary text-white">已结束</span>
                                        </c:when>
                                        <c:when test="${!a.registrationStarted}">
                                            <span class="badge bg-warning text-dark">未开始</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${a.activityStartTime}" pattern="MM-dd HH:mm" />
                                </td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${a.registrationStartTime}" pattern="MM-dd HH:mm" /> ~ 
                                    <fmt:formatDate value="${a.registrationEndTime}" pattern="MM-dd HH:mm" />
                                </td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <c:choose>
                                            <c:when test="${a.approvalStatus == 'pending'}">
                                                <a href="${pageContext.request.contextPath}/activity?action=approveActivity&id=${a.id}" class="btn btn-success btn-sm" onclick="return confirm('确定要批准此活动吗？')">批准</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=rejectActivity&id=${a.id}" class="btn btn-danger btn-sm" onclick="return confirm('确定要拒绝此活动吗？')">拒绝</a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/activity?action=participants&id=${a.id}" class="btn btn-outline-primary btn-sm">报名管理</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=edit&id=${a.id}" class="btn btn-white btn-sm">编辑</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=delete&id=${a.id}" class="btn btn-outline-danger btn-sm" onclick="return confirm('确定要删除此活动吗？')">删除</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty activities}">
                            <tr>
                                <td colspan="8" class="text-center text-muted">暂无活动记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />
