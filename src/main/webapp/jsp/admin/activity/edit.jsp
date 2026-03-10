<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="${empty activity ? '添加活动' : '编辑活动'}" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    ${empty activity ? '添加活动' : '编辑活动'}
                </h2>
            </div>
            <div class="col-auto ms-auto">
                <a href="${pageContext.request.contextPath}/activity?action=list" class="btn btn-outline-secondary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回列表
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/activity" method="POST" class="card">
                    <div class="card-header">
                        <h3 class="card-title">活动信息</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>
                        
                        <input type="hidden" name="action" value="${empty activity ? 'create' : 'update'}">
                        <c:if test="${not empty activity}">
                            <input type="hidden" name="id" value="${activity.id}">
                        </c:if>
                        
                        <div class="mb-3">
                            <label class="form-label required">活动标题</label>
                            <input type="text" class="form-control" name="title" 
                                   value="${activity.title}" placeholder="请输入活动标题" required>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">活动描述</label>
                            <textarea class="form-control" name="description" rows="4" 
                                      placeholder="请输入活动描述">${activity.description}</textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">活动分类</label>
                            <select class="form-select" name="activityType">
                                <option value="">请选择活动分类</option>
                                <c:forEach var="type" items="${activityTypes}">
                                    <option value="${type.code}" ${activity.activityType eq type.code ? 'selected' : ''}>${type.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">最大参与人数</label>
                                    <input type="number" class="form-control" name="maxParticipants" 
                                           value="${activity.maxParticipants > 0 ? activity.maxParticipants : ''}" placeholder="不限制请留空" min="0">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">活动地点</label>
                                    <input type="text" class="form-control" name="location" 
                                           value="${activity.location}" placeholder="请输入活动地点">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">开始时间</label>
                                    <input type="datetime-local" class="form-control" name="activityStartTime" 
                                           value="<fmt:formatDate value='${activity.activityStartTime}' pattern='yyyy-MM-dd HH:mm' />">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">结束时间</label>
                                    <input type="datetime-local" class="form-control" name="activityEndTime" 
                                           value="<fmt:formatDate value='${activity.activityEndTime}' pattern='yyyy-MM-dd HH:mm' />">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">报名开始时间</label>
                                    <input type="datetime-local" class="form-control" name="registrationStartTime" 
                                           value="<fmt:formatDate value='${activity.registrationStartTime}' pattern='yyyy-MM-dd HH:mm' />">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">报名截止时间</label>
                                    <input type="datetime-local" class="form-control" name="registrationEndTime" 
                                           value="<fmt:formatDate value='${activity.registrationEndTime}' pattern='yyyy-MM-dd HH:mm' />">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">组织人</label>
                                    <input type="text" class="form-control" name="organizers" 
                                           value="${activity.organizers}" placeholder="多个组织人用逗号分隔">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">联系方式</label>
                                    <input type="text" class="form-control" name="contactInfo" 
                                           value="${activity.contactInfo}" placeholder="邮箱或电话">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label required">活动状态</label>
                            <select class="form-select" name="status" required>
                                <option value="upcoming" ${empty activity or activity.status eq 'upcoming' ? 'selected' : ''}>即将开始</option>
                                <option value="ongoing" ${activity.status eq 'ongoing' ? 'selected' : ''}>进行中</option>
                                <option value="completed" ${activity.status eq 'completed' ? 'selected' : ''}>已结束</option>
                                <option value="canceled" ${activity.status eq 'canceled' ? 'selected' : ''}>已取消</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/activity/list" class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">
                                ${empty activity ? '添加活动' : '保存更改'}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />

<script>
(function() {
    // 仅在新建活动时设置默认值（activity为空）
    if (!${not empty activity}) {
        // 计算下一个周六
        var now = new Date();
        var dayOfWeek = now.getDay();
        var daysToSaturday = 6 - dayOfWeek;
        if (daysToSaturday <= 0) daysToSaturday += 7;
        
        var saturday = new Date(now);
        saturday.setDate(now.getDate() + daysToSaturday);
        saturday.setHours(9, 0, 0, 0);
        
        var sunday = new Date(saturday);
        sunday.setHours(11, 0, 0, 0);
        
        var regStart = new Date(now);
        
        var regEnd = new Date(saturday);
        regEnd.setDate(saturday.getDate() - 1);
        regEnd.setHours(23, 59, 0, 0);
        
        function formatDateTime(d) {
            var year = d.getFullYear();
            var month = String(d.getMonth() + 1).padStart(2, '0');
            var day = String(d.getDate()).padStart(2, '0');
            var hours = String(d.getHours()).padStart(2, '0');
            var minutes = String(d.getMinutes()).padStart(2, '0');
            return year + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
        }
        
        var activityStartInput = document.querySelector('input[name="activityStartTime"]');
        var activityEndInput = document.querySelector('input[name="activityEndTime"]');
        var regStartInput = document.querySelector('input[name="registrationStartTime"]');
        var regEndInput = document.querySelector('input[name="registrationEndTime"]');
        
        if (activityStartInput && !activityStartInput.value) {
            activityStartInput.value = formatDateTime(saturday);
        }
        if (activityEndInput && !activityEndInput.value) {
            activityEndInput.value = formatDateTime(sunday);
        }
        if (regStartInput && !regStartInput.value) {
            regStartInput.value = formatDateTime(regStart);
        }
        if (regEndInput && !regEndInput.value) {
            regEndInput.value = formatDateTime(regEnd);
        }
    }
})();
</script>