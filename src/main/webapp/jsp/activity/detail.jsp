<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动详情" />
</jsp:include>

<style>
    .activity-detail-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .activity-detail-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 32px;
        color: white;
    }
    
    .activity-detail-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        color: white;
        margin: 0 0 8px 0;
    }
    
    .back-btn {
        background: rgba(255, 255, 255, 0.15);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        border: none;
        font-weight: 500;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        text-decoration: none;
    }
    
    .back-btn:hover {
        background: rgba(255, 255, 255, 0.25);
        color: white;
    }
    
    .info-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 24px;
        height: 100%;
    }
    
    .info-card-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    
    .info-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }
    
    .info-item:last-child {
        border-bottom: none;
    }
    
    .info-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-standard);
        background: rgba(20, 86, 240, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--brand-blue);
        flex-shrink: 0;
    }
    
    .info-label {
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-bottom: 4px;
    }
    
    .info-value {
        font-size: 0.94rem;
        color: var(--text-dark);
        font-weight: 500;
    }
    
    .register-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        padding: 24px;
    }
    
    .register-status {
        padding: 16px;
        border-radius: var(--radius-standard);
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .register-status.success {
        background: rgba(16, 185, 129, 0.1);
        color: #059669;
    }
    
    .register-status.warning {
        background: rgba(245, 158, 11, 0.1);
        color: #d97706;
    }
    
    .register-status.secondary {
        background: var(--bg-light-gray);
        color: var(--text-secondary);
    }
    
    .register-stat {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        background: var(--bg-light-gray);
        border-radius: var(--radius-standard);
        margin-bottom: 16px;
    }
    
    .register-stat-icon {
        width: 48px;
        height: 48px;
        border-radius: var(--radius-standard);
        background: var(--brand-blue);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .register-stat-value {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .register-stat-label {
        font-size: 0.81rem;
        color: var(--text-muted);
    }
    
    .btn-register {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 14px 24px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
    }
    
    .btn-register:hover {
        background: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }
    
    .btn-disabled {
        background: var(--bg-light-gray);
        color: var(--text-muted);
        border-radius: var(--radius-standard);
        padding: 14px 24px;
        font-weight: 500;
        border: none;
        width: 100%;
    }
    
    .btn-success {
        background: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 14px 24px;
        font-weight: 500;
        border: none;
        width: 100%;
    }
    
    .description-section {
        padding: 24px;
        background: var(--bg-light-gray);
        border-radius: var(--radius-standard);
        margin-top: 24px;
    }
    
    .description-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 12px;
    }
    
    .description-text {
        color: var(--text-secondary);
        line-height: 1.7;
        white-space: pre-wrap;
    }
    
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">${activity.title}</h2>
            </div>
            <div class="col-auto">
                <c:choose>
                    <c:when test="${param.fromMyActivities == 'true'}">
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="back-btn">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                            返回我的活动
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/activity?action=list" class="back-btn">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
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
        <div class="row row-cards g-4">
            <div class="col-md-8">
                <div class="activity-detail-card">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">活动类型</div>
                                        <div class="info-value">
                                            <c:choose>
                                                <c:when test='${activity.activityType == "LECTURE"}'>讲座</c:when>
                                                <c:when test='${activity.activityType == "SEMINAR"}'>讨论会</c:when>
                                                <c:when test='${activity.activityType == "TEA_PARTY"}'>茶话会</c:when>
                                                <c:when test='${activity.activityType == "PROJECT_INTRO"}'>项目介绍</c:when>
                                                <c:when test='${activity.activityType == "WORKSHOP"}'>工作坊</c:when>
                                                <c:when test='${activity.activityType == "COMPETITION"}'>竞赛</c:when>
                                                <c:when test='${activity.activityType == "TRAINING"}'>培训</c:when>
                                                <c:when test='${activity.activityType == "TEAM_BUILDING"}'>团建</c:when>
                                                <c:otherwise>其他</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s-8-4.5-8-11.8a8 8 0 0 1 16 0c0 7.3-8 11.8-8 11.8z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">活动地点</div>
                                        <div class="info-value">${activity.location}</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="5" width="16" height="16" rx="2"></rect><line x1="16" y1="3" x2="16" y2="7"></line><line x1="8" y1="3" x2="8" y2="7"></line><line x1="4" y1="11" x2="20" y2="11"></line><line x1="11" y1="15" x2="12" y2="15"></line><line x1="12" y1="15" x2="12" y2="18"></line></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">活动时间</div>
                                        <div class="info-value"><fmt:formatDate value="${activity.activityStartTime}" pattern="yyyy-MM-dd HH:mm" /> ~ <fmt:formatDate value="${activity.activityEndTime}" pattern="MM-dd HH:mm" /></div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">报名时间</div>
                                        <div class="info-value"><fmt:formatDate value="${activity.registrationStartTime}" pattern="yyyy-MM-dd HH:mm" /> ~ <fmt:formatDate value="${activity.registrationEndTime}" pattern="MM-dd HH:mm" /></div>
                                    </div>
                                </div>
                            </div>
                            <c:if test="${not empty activity.organizers}">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="9" cy="7" r="4"></circle><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">组织人</div>
                                        <div class="info-value">${activity.organizers}</div>
                                    </div>
                                </div>
                            </div>
                            </c:if>
                            <c:if test="${not empty activity.contactInfo}">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-icon">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                    </div>
                                    <div>
                                        <div class="info-label">联系方式</div>
                                        <div class="info-value">${activity.contactInfo}</div>
                                    </div>
                                </div>
                            </div>
                            </c:if>
                        </div>
                        
                        <div class="description-section">
                            <div class="description-title">活动介绍</div>
                            <div class="description-text">${activity.description}</div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="register-card">
                    <c:choose>
                        <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                            <div class="register-status secondary">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                <div>
                                    <div style="font-weight: 600;">报名已结束</div>
                                    <div style="font-size: 0.81rem; opacity: 0.8;">该活动已结束</div>
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${activity.inRegistrationPeriod}">
                            <div class="register-status success">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                <div>
                                    <div style="font-weight: 600;">报名进行中</div>
                                    <div style="font-size: 0.81rem; opacity: 0.8;">立即报名参加此活动</div>
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${activity.registrationEnded}">
                            <div class="register-status secondary">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                <div>
                                    <div style="font-weight: 600;">报名已结束</div>
                                    <div style="font-size: 0.81rem; opacity: 0.8;">该活动报名已截止</div>
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${!activity.registrationStarted}">
                            <div class="register-status warning">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                <div>
                                    <div style="font-weight: 600;">报名未开始</div>
                                    <div style="font-size: 0.81rem; opacity: 0.8;">报名将于 <fmt:formatDate value="${activity.registrationStartTime}" pattern="MM-dd HH:mm" /> 开始</div>
                                </div>
                            </div>
                        </c:when>
                    </c:choose>
                    
                    <div class="register-stat">
                        <div class="register-stat-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                        </div>
                        <div>
                            <div class="register-stat-value">${activity.currentParticipants}</div>
                            <div class="register-stat-label">已报名人数</div>
                        </div>
                    </div>
                    
                    <div class="register-stat">
                        <div class="register-stat-icon" style="background: #10b981;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${activity.maxParticipants > 0}">
                                    <div class="register-stat-value">${activity.maxParticipants - activity.currentParticipants}</div>
                                    <div class="register-stat-label">剩余名额</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="register-stat-value">不限</div>
                                    <div class="register-stat-label">名额限制</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                    <c:if test="${activity.inRegistrationPeriod}">
                    <div class="register-stat" style="background: rgba(245, 158, 11, 0.1);">
                        <div class="register-stat-icon" style="background: #f59e0b;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                        </div>
                        <div>
                            <div class="register-stat-value" id="countdown" style="color: #d97706;">加载中...</div>
                            <div class="register-stat-label">报名截止倒计时</div>
                        </div>
                    </div>
                    </c:if>
                    
                    <c:choose>
                        <c:when test="${isRegistered}">
                            <button class="btn-success" disabled>
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                已报名
                            </button>
                        </c:when>
                        <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                            <button class="btn-disabled" disabled>活动已结束</button>
                        </c:when>
                        <c:when test="${activity.inRegistrationPeriod}">
                            <form action="${pageContext.request.contextPath}/activity" method="post">
                                <input type="hidden" name="action" value="register">
                                <input type="hidden" name="activityId" value="${activity.id}">
                                <button type="submit" class="btn-register" onclick="return confirm('确定要报名此活动吗？');">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                    立即报名
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <button class="btn-disabled" disabled>暂不可报名</button>
                        </c:otherwise>
                    </c:choose>
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