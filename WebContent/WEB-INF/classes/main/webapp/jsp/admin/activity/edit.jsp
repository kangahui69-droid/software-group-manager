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
                        <c:if test="${not empty param.error}">
                            <div class="alert alert-danger">
                                ${param.error}
                            </div>
                        </c:if>
                        <c:if test="${not empty param.success}">
                            <div class="alert alert-success">
                                ${param.success}
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
                                    <input type="datetime-local" class="form-control" name="activityStartTime" id="activityStartTime"
                                           value="<fmt:formatDate value='${activity.activityStartTime}' pattern='yyyy-MM-dd HH:mm' />"
                                           min="<%= new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new java.util.Date()) %>">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">结束时间</label>
                                    <input type="datetime-local" class="form-control" name="activityEndTime" id="activityEndTime"
                                           value="<fmt:formatDate value='${activity.activityEndTime}' pattern='yyyy-MM-dd HH:mm' />">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">报名开始时间</label>
                                    <input type="datetime-local" class="form-control" name="registrationStartTime" id="registrationStartTime"
                                           value="<fmt:formatDate value='${activity.registrationStartTime}' pattern='yyyy-MM-dd HH:mm' />"
                                           min="<%= new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new java.util.Date()) %>">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">报名截止时间</label>
                                    <input type="datetime-local" class="form-control" name="registrationEndTime" id="registrationEndTime"
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
                                <%-- 新建活动：只允许选择"即将开始"或"进行中" --%>
                                <c:if test="${empty activity}">
                                    <option value="upcoming" selected>即将开始</option>
                                    <option value="ongoing">进行中</option>
                                </c:if>
                                <%-- 编辑活动：显示所有状态 --%>
                                <c:if test="${not empty activity}">
                                    <option value="upcoming" ${activity.status eq 'upcoming' ? 'selected' : ''}>即将开始</option>
                                    <option value="ongoing" ${activity.status eq 'ongoing' ? 'selected' : ''}>进行中</option>
                                    <option value="completed" ${activity.status eq 'completed' ? 'selected' : ''}>已结束</option>
                                    <option value="canceled" ${activity.status eq 'canceled' ? 'selected' : ''}>已取消</option>
                                </c:if>
                            </select>
                            <c:if test="${empty activity}">
                                <small class="text-muted">新建活动时只能选择"即将开始"或"进行中"</small>
                            </c:if>
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

// 显示错误提示的辅助函数
function showFieldError(message) {
    // 移除已存在的错误提示
    var existingError = document.querySelector('.field-error-alert');
    if (existingError) {
        existingError.remove();
    }

    // 创建错误提示元素
    var errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger field-error-alert';
    errorDiv.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg> ' + message;

    // 插入到表单顶部
    var cardBody = document.querySelector('.card-body');
    var firstChild = cardBody.firstChild;
    cardBody.insertBefore(errorDiv, firstChild);

    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // 3秒后自动移除
    setTimeout(function() {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 5000);
}

// 日期验证逻辑
document.addEventListener('DOMContentLoaded', function() {
    var activityStartInput = document.getElementById('activityStartTime');
    var activityEndInput = document.getElementById('activityEndTime');
    var regStartInput = document.getElementById('registrationStartTime');
    var regEndInput = document.getElementById('registrationEndTime');

    // 当活动时间改变时，更新结束时间的最小值
    activityStartInput.addEventListener('change', function() {
        if (this.value) {
            activityEndInput.min = this.value;
            // 如果结束时间早于开始时间，清空结束时间
            if (activityEndInput.value && activityEndInput.value < this.value) {
                activityEndInput.value = '';
                showFieldError('报名截止时间已重置，请重新选择早于活动开始时间的时间');
            }
        } else {
            activityEndInput.removeAttribute('min');
        }
    });

    // 当报名开始时间改变时，更新报名结束时间的最小值
    regStartInput.addEventListener('change', function() {
        if (this.value) {
            regEndInput.min = this.value;
            // 如果报名结束时间早于开始时间，清空结束时间
            if (regEndInput.value && regEndInput.value < this.value) {
                regEndInput.value = '';
            }
        } else {
            regEndInput.removeAttribute('min');
        }
    });

    // 当活动时间确定后，限制报名截止时间必须早于活动开始时间
    activityStartInput.addEventListener('change', function() {
        if (this.value) {
            // 报名截止时间必须早于活动开始时间（严格小于）
            regEndInput.max = this.value;
            // 如果当前报名截止时间晚于或等于活动开始时间，清空报名截止时间
            if (regEndInput.value && regEndInput.value >= this.value) {
                regEndInput.value = '';
                showFieldError('报名截止时间已重置，请重新选择早于活动开始时间的时间');
            }
        } else {
            regEndInput.removeAttribute('max');
        }
    });

    // 表单提交验证
    document.querySelector('form').addEventListener('submit', function(e) {
        var activityStart = activityStartInput.value;
        var activityEnd = activityEndInput.value;
        var regStart = regStartInput.value;
        var regEnd = regEndInput.value;

        // 验证活动结束时间
        if (activityStart && activityEnd) {
            if (activityEnd <= activityStart) {
                showFieldError('活动结束时间必须晚于开始时间');
                e.preventDefault();
                return false;
            }
        }

        // 验证报名结束时间
        if (regStart && regEnd) {
            if (regEnd <= regStart) {
                showFieldError('报名截止时间必须晚于报名开始时间');
                e.preventDefault();
                return false;
            }
        }

        // 验证报名截止时间必须早于活动开始时间（严格小于）
        if (regEnd && activityStart) {
            if (regEnd >= activityStart) {
                showFieldError('报名截止时间必须早于活动开始时间');
                e.preventDefault();
                return false;
            }
        }
    });
});
</script>