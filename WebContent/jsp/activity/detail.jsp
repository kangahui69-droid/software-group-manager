<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动详情" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    ${activity.title}
                </h2>
            </div>
            <div class="col-auto">
                <c:choose>
                    <c:when test="${param.fromMyActivities == 'true'}">
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="btn btn-outline-secondary">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                            返回我的活动
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-outline-secondary">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                            返回列表
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-calendar-event" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line><path d="M8 14h.01"></path><path d="M12 14h.01"></path><path d="M16 14h.01"></path><path d="M8 18h.01"></path><path d="M12 18h.01"></path><path d="M16 18h.01"></path></svg>
                            活动详情
                        </h3>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">活动类型</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="<c:choose><c:when test='${activity.activityType == \"LECTURE\"}'>讲座</c:when><c:when test='${activity.activityType == \"SEMINAR\"}'>讨论会</c:when><c:when test='${activity.activityType == \"TEA_PARTY\"}'>茶话会</c:when><c:when test='${activity.activityType == \"PROJECT_INTRO\"}'>项目介绍</c:when><c:when test='${activity.activityType == \"WORKSHOP\"}'>工作坊</c:when><c:when test='${activity.activityType == \"COMPETITION\"}'>竞赛</c:when><c:when test='${activity.activityType == \"TRAINING\"}'>培训</c:when><c:when test='${activity.activityType == \"TEAM_BUILDING\"}'>团建</c:when><c:otherwise>其他</c:otherwise></c:choose>">
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">活动地点</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s-8-4.5-8-11.8a8 8 0 0 1 16 0c0 7.3-8 11.8-8 11.8z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="${activity.location}">
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">活动时间</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="5" width="16" height="16" rx="2"></rect><line x1="16" y1="3" x2="16" y2="7"></line><line x1="8" y1="3" x2="8" y2="7"></line><line x1="4" y1="11" x2="20" y2="11"></line><line x1="11" y1="15" x2="12" y2="15"></line><line x1="12" y1="15" x2="12" y2="18"></line></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="<fmt:formatDate value="${activity.activityStartTime}" pattern="yyyy-MM-dd HH:mm" /> ~ <fmt:formatDate value="${activity.activityEndTime}" pattern="MM-dd HH:mm" />">
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">报名时间</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="<fmt:formatDate value="${activity.registrationStartTime}" pattern="yyyy-MM-dd HH:mm" /> ~ <fmt:formatDate value="${activity.registrationEndTime}" pattern="MM-dd HH:mm" />">
                                </div>
                            </div>
                            <c:if test="${not empty activity.organizers}">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">组织人</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="9" cy="7" r="4"></circle><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="${activity.organizers}">
                                </div>
                            </div>
                            </c:if>
                            <c:if test="${not empty activity.contactInfo}">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">联系方式</label>
                                <div class="input-group">
                                    <span class="input-group-text">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                    </span>
                                    <input type="text" class="form-control" disabled value="${activity.contactInfo}">
                                </div>
                            </div>
                            </c:if>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">活动介绍</label>
                            <div class="input-group">
                                <span class="input-group-text" style="align-self: flex-start;">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>
                                </span>
                                <textarea class="form-control" rows="8" disabled>${activity.description}</textarea>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">报名信息</h3>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                                <div class="alert alert-secondary alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">报名已结束</h4>
                                            <div class="text-muted">该活动已结束</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${activity.inRegistrationPeriod}">
                                <div class="alert alert-success alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">报名进行中</h4>
                                            <div class="text-muted">立即报名参加此活动</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${activity.registrationEnded}">
                                <div class="alert alert-secondary alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">报名已结束</h4>
                                            <div class="text-muted">该活动报名已截止</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${!activity.registrationStarted}">
                                <div class="alert alert-warning alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">报名未开始</h4>
                                            <div class="text-muted">报名将于 <fmt:formatDate value="${activity.registrationStartTime}" pattern="MM-dd HH:mm" /> 开始</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                        </c:choose>
                        
                        <div class="list-group list-group-flush list-group-hoverable">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-users text-primary" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <span class="text-primary d-block fs-4">${activity.currentParticipants}</span>
                                        <div class="d-block text-muted text-truncate mt-n1">已报名人数</div>
                                    </div>
                                </div>
                            </div>
                            <c:choose>
                                <c:when test="${activity.maxParticipants > 0}">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-user-plus text-success" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <span class="text-success d-block fs-4">${activity.maxParticipants - activity.currentParticipants}</span>
                                        <div class="d-block text-muted text-truncate mt-n1">剩余名额</div>
                                    </div>
                                </div>
                            </div>
                                </c:when>
                                <c:otherwise>
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-users text-success" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <span class="text-success d-block fs-4">不限</span>
                                        <div class="d-block text-muted text-truncate mt-n1">名额限制</div>
                                    </div>
                                </div>
                            </div>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${activity.inRegistrationPeriod}">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-clock text-warning" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <span class="text-warning d-block fs-4" id="countdown">加载中...</span>
                                        <div class="d-block text-muted text-truncate mt-n1">报名截止倒计时</div>
                                    </div>
                                </div>
                            </div>
                            </c:if>
                        </div>
                        
                        <c:choose>
                            <c:when test="${isRegistered}">
                                <div class="mt-3">
                                    <button class="btn btn-success w-100" disabled>
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                        已报名
                                    </button>
                                </div>
                            </c:when>
                            <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                                <div class="mt-3">
                                    <button class="btn btn-secondary w-100" disabled>
                                        活动已结束
                                    </button>
                                </div>
                            </c:when>
                            <c:when test="${activity.inRegistrationPeriod}">
                                <form action="${pageContext.request.contextPath}/activity" method="post" class="mt-3">
                                    <input type="hidden" name="action" value="register">
                                    <input type="hidden" name="activityId" value="${activity.id}">
                                    <button type="submit" class="btn btn-primary w-100" onclick="return confirm('确定要报名此活动吗？');">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                        立即报名
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <div class="mt-3">
                                    <button class="btn btn-secondary w-100" disabled>
                                        暂不可报名
                                    </button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<c:if test="${activity.inRegistrationPeriod && activity.registrationEndTime != null}">
<script>
(function() {
    var endTime = new Date("<fmt:formatDate value="${activity.registrationEndTime}" pattern="yyyy/MM/dd HH:mm:ss" />").getTime();
    
    function updateCountdown() {
        var now = new Date().getTime();
        var distance = endTime - now;
        
        if (distance < 0) {
            document.getElementById("countdown").textContent = "已截止";
            return;
        }
        
        var days = Math.floor(distance / (1000 * 60 * 60 * 24));
        var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = Math.floor((distance % (1000 * 60)) / 1000);
        
        var text = "";
        if (days > 0) text += days + "天 ";
        if (hours > 0) text += hours + "时 ";
        if (minutes > 0) text += minutes + "分 ";
        text += seconds + "秒";
        
        document.getElementById("countdown").textContent = text;
    }
    
    updateCountdown();
    setInterval(updateCountdown, 1000);
})();
</script>
</c:if>

<jsp:include page="../common/layout_bottom.jsp" />
