<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="${empty activity ? '添加活动' : '编辑活动'}" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-body-design {
        padding: 0;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-calendar-plus me-2"></i>${empty activity ? '添加活动' : '编辑活动'}
            </h1>
            <p class="admin-hero-subtitle">${empty activity ? '创建新的活动' : '修改活动信息'}</p>
        </div>

        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/activity" method="POST" class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">活动信息</h3>
                        <a href="${returnUrl != null ? returnUrl : pageContext.request.contextPath.concat('/activity?action=manage')}" class="btn btn-outline-brand">
                            <i class="bi bi-arrow-left me-1"></i>返回列表
                        </a>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: var(--radius-standard); color: #dc2626; padding: 12px 16px;">
                                ${error}
                            </div>
                        </c:if>
                        
                        <input type="hidden" name="action" value="${empty activity ? 'create' : 'update'}">
                        <c:if test="${not empty activity}">
                            <input type="hidden" name="id" value="${activity.id}">
                        </c:if>
                        
                        <div class="mb-3">
                            <label class="form-label-design required">活动标题</label>
                            <input type="text" class="input-design" name="title" 
                                   value="${activity.title}" placeholder="请输入活动标题" required>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label-design">活动描述</label>
                            <textarea class="input-design" name="description" rows="4" 
                                      placeholder="请输入活动描述">${activity.description}</textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label-design">活动分类</label>
                            <select class="input-design" name="activityType">
                                <option value="">请选择活动分类</option>
                                <c:forEach var="type" items="${activityTypes}">
                                    <option value="${type.code}" ${activity.activityType eq type.code ? 'selected' : ''}>${type.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">最大参与人数</label>
                                    <input type="number" class="input-design" name="maxParticipants" 
                                           value="${activity.maxParticipants > 0 ? activity.maxParticipants : ''}" placeholder="不限制请留空" min="0">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">活动地点</label>
                                    <input type="text" class="input-design" name="location" 
                                           value="${activity.location}" placeholder="请输入活动地点">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">开始时间</label>
                                    <input type="datetime-local" class="input-design" name="activityStartTime" 
                                           value="<fmt:formatDate value='${activity.activityStartTime}' pattern='yyyy-MM-dd\'T\'HH:mm' />">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">结束时间</label>
                                    <input type="datetime-local" class="input-design" name="activityEndTime" 
                                           value="<fmt:formatDate value='${activity.activityEndTime}' pattern='yyyy-MM-dd\'T\'HH:mm' />">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">报名开始时间</label>
                                    <input type="datetime-local" class="input-design" name="registrationStartTime" 
                                           value="<fmt:formatDate value='${activity.registrationStartTime}' pattern='yyyy-MM-dd\'T\'HH:mm' />">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">报名截止时间</label>
                                    <input type="datetime-local" class="input-design" name="registrationEndTime" 
                                           value="<fmt:formatDate value='${activity.registrationEndTime}' pattern='yyyy-MM-dd\'T\'HH:mm' />">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">组织人</label>
                                    <input type="text" class="input-design" name="organizers" 
                                           value="${activity.organizers}" placeholder="多个组织人用逗号分隔">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">联系方式</label>
                                    <input type="text" class="input-design" name="contactInfo" 
                                           value="${activity.contactInfo}" placeholder="邮箱或电话">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label-design required">活动状态</label>
                            <select class="input-design" name="status" required>
                                <option value="upcoming" ${empty activity or activity.status eq 'upcoming' ? 'selected' : ''}>即将开始</option>
                                <option value="ongoing" ${activity.status eq 'ongoing' ? 'selected' : ''}>进行中</option>
                                <option value="completed" ${activity.status eq 'completed' ? 'selected' : ''}>已结束</option>
                                <option value="canceled" ${activity.status eq 'canceled' ? 'selected' : ''}>已取消</option>
                            </select>
                        </div>
                        
                        <c:if test="${empty activity}">
                        <div class="mb-3">
                            <label class="form-label-design">
                                <input type="checkbox" name="createGroupChat" value="true" checked>
                                同时创建活动群聊
                            </label>
                            <div class="text-muted small">创建后可在"我的群聊"中管理群聊并添加成员</div>
                        </div>
                        </c:if>
                    </div>
                    <div class="card-footer-design text-end">
                        <div class="d-flex">
                            <a href="${returnUrl != null ? returnUrl : pageContext.request.contextPath.concat('/activity?action=manage')}" class="btn btn-outline-brand">取消</a>
                            <button type="submit" class="btn btn-brand ms-auto">
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
    if (!${not empty activity}) {
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