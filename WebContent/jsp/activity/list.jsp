<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动列表" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    活动列表
                </h2>
                <div class="text-muted mt-1">
                    <c:choose>
                        <c:when test="${viewMode == 'inPeriod'}">
                            显示正在报名中的活动
                        </c:when>
                        <c:otherwise>
                            显示所有活动（包括已过报名时间的）
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-auto btn-list">
                <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="btn btn-outline-primary">
                    我的活动
                </a>
                <c:choose>
                    <c:when test="${viewMode == 'inPeriod'}">
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-outline-secondary">
                            查看全部
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/activity?action=list&viewMode=inPeriod" class="btn btn-primary">
                            仅看可报名
                        </a>
                    </c:otherwise>
                </c:choose>
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
                    <input type="hidden" name="action" value="list">
                    <input type="hidden" name="viewMode" value="${viewMode}">
                    <div class="col-md-4">
                        <input type="text" name="keyword" class="form-control" placeholder="搜索活动名称、描述、地点" value="${keyword}">
                    </div>
                    <div class="col-md-3">
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
                        <button type="submit" class="btn btn-primary w-100">搜索</button>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-outline-secondary w-100">重置</a>
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
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            <c:forEach var="activity" items="${activities}">
                <div class="col">
                    <div class="card h-100">
                        <div class="card-status-top 
                            <c:choose>
                                <c:when test="${activity.inRegistrationPeriod}">bg-success</c:when>
                                <c:when test="${activity.registrationEnded}">bg-secondary</c:when>
                                <c:otherwise>bg-warning</c:otherwise>
                            </c:choose>">
                        </div>
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <span class="badge bg-blue-lt me-2">
                                    <c:choose>
                                        <c:when test="${activity.activityType == 'LECTURE'}">讲座</c:when>
                                        <c:when test="${activity.activityType == 'SEMINAR'}">讨论会</c:when>
                                        <c:when test="${activity.activityType == 'TEA_PARTY'}">茶话会</c:when>
                                        <c:when test="${activity.activityType == 'PROJECT_INTRO'}">项目介绍</c:when>
                                        <c:when test="${activity.activityType == 'WORKSHOP'}">工作坊</c:when>
                                        <c:when test="${activity.activityType == 'COMPETITION'}">竞赛</c:when>
                                        <c:when test="${activity.activityType == 'TRAINING'}">培训</c:when>
                                        <c:when test="${activity.activityType == 'TEAM_BUILDING'}">团建</c:when>
                                        <c:otherwise>其他</c:otherwise>
                                    </c:choose>
                                </span>
                                <c:choose>
                                    <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                                        <span class="badge bg-secondary text-white">报名已结束</span>
                                    </c:when>
                                    <c:when test="${activity.inRegistrationPeriod}">
                                        <span class="badge bg-success text-white">报名进行中</span>
                                    </c:when>
                                    <c:when test="${activity.registrationEnded}">
                                        <span class="badge bg-secondary text-white">报名已结束</span>
                                    </c:when>
                                    <c:when test="${!activity.registrationStarted}">
                                        <span class="badge bg-warning text-dark">报名未开始</span>
                                    </c:when>
                                </c:choose>
                                <c:if test="${activity.registeredByCurrentUser}">
                                    <span class="badge bg-info text-white ms-1">已报名</span>
                                </c:if>
                            </div>
                            
                            <h3 class="card-title">${activity.title}</h3>
                            <p class="text-muted text-truncate-2">${activity.description}</p>
                            
                            <div class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1 text-muted" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                    <rect x="4" y="5" width="16" height="16" rx="2"></rect>
                                    <line x1="16" y1="3" x2="16" y2="7"></line>
                                    <line x1="8" y1="3" x2="8" y2="7"></line>
                                    <line x1="4" y1="11" x2="20" y2="11"></line>
                                    <line x1="11" y1="15" x2="12" y2="15"></line>
                                    <line x1="12" y1="15" x2="12" y2="18"></line>
                                </svg>
                                <span class="text-muted">活动时间：</span>
                                <fmt:formatDate value="${activity.activityStartTime}" pattern="MM-dd HH:mm" /> ~ 
                                <fmt:formatDate value="${activity.activityEndTime}" pattern="MM-dd HH:mm" />
                            </div>
                            
                            <div class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1 text-muted" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                    <path d="M12 22s-8-4.5-8-11.8a8 8 0 0 1 16 0c0 7.3-8 11.8-8 11.8z"></path>
                                    <circle cx="12" cy="10" r="3"></circle>
                                </svg>
                                <span class="text-muted">地点：</span>${activity.location}
                            </div>
                            
                            <div class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1 text-muted" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                    <circle cx="12" cy="12" r="9"></circle>
                                    <polyline points="12 6 12 12 16 14"></polyline>
                                </svg>
                                <span class="text-muted">报名截止：</span>
                                <fmt:formatDate value="${activity.registrationEndTime}" pattern="MM-dd HH:mm" />
                            </div>
                            
                            <c:if test="${not empty activity.organizers}">
                                <div class="mb-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1 text-muted" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <circle cx="9" cy="7" r="4"></circle>
                                        <path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path>
                                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                        <path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path>
                                    </svg>
                                    <span class="text-muted">组织人：</span>${activity.organizers}
                                </div>
                            </c:if>
                            
                            <c:if test="${activity.maxParticipants > 0}">
                                <div class="mb-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1 text-muted" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                        <circle cx="9" cy="7" r="4"></circle>
                                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                    </svg>
                                    <span class="text-muted">名额：</span>${activity.maxParticipants}人
                                </div>
                            </c:if>
                        </div>
                        
                        <div class="card-footer">
                            <a href="${pageContext.request.contextPath}/activity?action=detail&id=${activity.id}" class="btn btn-outline-primary w-100">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                                查看详情
                            </a>
                        </div>
                    </div>
                </div>
            </c:forEach>
            
            <c:if test="${empty activities}">
                <div class="col-12">
                    <div class="empty">
                        <div class="empty-img">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-calendar-off" width="128" height="128" viewBox="0 0 24 24" stroke-width="1" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M9 5h9a2 2 0 0 1 2 2v9m-.292 3.706a2 2 0 0 1 -1.708 .294h-12a2 2 0 0 1 -2 -2v-12c0 -.768 .294 -1.46 .768 -1.986"></path>
                                <path d="M13 13h3v3h-3z"></path>
                                <path d="M3 3l18 18"></path>
                            </svg>
                        </div>
                        <p class="empty-title">暂无活动</p>
                        <p class="empty-subtitle text-muted">
                            <c:choose>
                                <c:when test="${viewMode == 'all'}">当前没有活动记录</c:when>
                                <c:otherwise>当前没有正在报名中的活动</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
