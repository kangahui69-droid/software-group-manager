<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="报名管理" />
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
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-people me-2"></i>报名管理 - ${activity.title}
                    </h1>
                    <p class="admin-hero-subtitle">
                        <fmt:formatDate value="${activity.activityStartTime}" pattern="yyyy-MM-dd HH:mm" /> | 
                        地点: ${activity.location} |
                        <c:choose>
                            <c:when test="${activity.status == 'completed' || activity.status == 'canceled' || activity.status == 'ongoing'}">
                                <span style="color: #ef4444;">报名已结束</span>
                            </c:when>
                            <c:when test="${activity.registrationEnded}">
                                <span style="color: #ef4444;">报名已截止</span>
                            </c:when>
                            <c:otherwise>
                                <span style="color: #10b981;">报名进行中</span>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/activity?action=manage" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success" role="alert" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: var(--radius-standard); color: #059669; padding: 12px 16px; margin-bottom: 16px;">
                ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" role="alert" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: var(--radius-standard); color: #dc2626; padding: 12px 16px; margin-bottom: 16px;">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row row-cols-1 row-cols-md-4 g-3 mb-4">
            <div class="col">
                <div class="stat-card" style="background: linear-gradient(135deg, var(--brand-blue), #3b82f6); color: white;">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="subtile" style="font-size: 0.875rem; opacity: 0.9;">总报名人数</div>
                            <div class="h1 m-0" style="font-size: 2rem; font-weight: 600;">${registrations.size()}</div>
                        </div>
                        <div style="width: 52px; height: 52px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; flex-shrink: 0; background: rgba(255,255,255,0.2); color: white;">
                            <i class="bi bi-people"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="stat-card" style="background: linear-gradient(135deg, #f59e0b, #fbbf24); color: white;">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="subtile" style="font-size: 0.875rem; opacity: 0.9;">待审核</div>
                            <div class="h1 m-0" style="font-size: 2rem; font-weight: 600;">
                                <c:set var="pendingCount" value="0" />
                                <c:forEach var="r" items="${registrations}">
                                    <c:if test="${r.status == 'pending'}"><c:set var="pendingCount" value="${pendingCount + 1}" /></c:if>
                                </c:forEach>
                                ${pendingCount}
                            </div>
                        </div>
                        <div style="width: 52px; height: 52px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; flex-shrink: 0; background: rgba(255,255,255,0.2); color: white;">
                            <i class="bi bi-clock"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="stat-card" style="background: linear-gradient(135deg, #10b981, #34d399); color: white;">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="subtile" style="font-size: 0.875rem; opacity: 0.9;">已确认</div>
                            <div class="h1 m-0" style="font-size: 2rem; font-weight: 600;">
                                <c:set var="confirmedCount" value="0" />
                                <c:forEach var="r" items="${registrations}">
                                    <c:if test="${r.status == 'confirmed'}"><c:set var="confirmedCount" value="${confirmedCount + 1}" /></c:if>
                                </c:forEach>
                                ${confirmedCount}
                            </div>
                        </div>
                        <div style="width: 52px; height: 52px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; flex-shrink: 0; background: rgba(255,255,255,0.2); color: white;">
                            <i class="bi bi-check-circle"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="stat-card" style="background: linear-gradient(135deg, #ef4444, #f87171); color: white;">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="subtile" style="font-size: 0.875rem; opacity: 0.9;">已驳回</div>
                            <div class="h1 m-0" style="font-size: 2rem; font-weight: 600;">
                                <c:set var="rejectedCount" value="0" />
                                <c:forEach var="r" items="${registrations}">
                                    <c:if test="${r.status == 'rejected'}"><c:set var="rejectedCount" value="${rejectedCount + 1}" /></c:if>
                                </c:forEach>
                                ${rejectedCount}
                            </div>
                        </div>
                        <div style="width: 52px; height: 52px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; flex-shrink: 0; background: rgba(255,255,255,0.2); color: white;">
                            <i class="bi bi-x-circle"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${empty registrations}">
            <div class="alert alert-info" role="alert" style="background: var(--primary-light); border: 1px solid rgba(20, 86, 240, 0.2); border-radius: var(--radius-standard); color: var(--brand-blue); padding: 12px 16px;">
                暂无报名人员
            </div>
        </c:if>
        
        <c:if test="${activity.registrationClosed}">
            <div class="alert alert-warning" role="alert" style="background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.2); border-radius: var(--radius-standard); color: #92400e; padding: 12px 16px; margin-bottom: 16px;">
                <strong>注意：</strong>此活动的报名已结束，仅可查看报名记录，无法进行审核操作。
            </div>
        </c:if>
        
        <form id="participantForm" method="POST" action="${pageContext.request.contextPath}/activity">
            <input type="hidden" name="activityId" value="${activity.id}">
            
            <div class="card-design">
                <div class="card-header-design">
                    <div class="d-flex justify-content-between align-items-center">
                        <h3 class="card-title-design">报名人员列表</h3>
                        <c:if test="${not activity.registrationClosed}">
                            <div>
                                <label class="form-check">
                                    <input type="checkbox" class="form-check-input" id="selectAll">
                                    <span class="form-check-label">全选</span>
                                </label>
                            </div>
                        </c:if>
                    </div>
                </div>
                <c:if test="${not activity.registrationClosed}">
                    <div class="card-body-design">
                        <div class="mb-3">
                            <button type="submit" name="action" value="batchApprove" class="btn btn-success-design" onclick="return confirm('确定要批量通过选中的报名吗？')">
                                批量通过
                            </button>
                            <button type="submit" name="action" value="batchReject" class="btn btn-danger-design" onclick="return confirm('确定要批量拒绝选中的报名吗？')">
                                批量拒绝
                            </button>
                        </div>
                    </div>
                </c:if>
                <div class="table-responsive">
                    <table class="table-design">
                        <thead>
                            <tr>
                                <th class="w-1"></th>
                                <th>姓名</th>
                                <th>学号</th>
                                <th>专业</th>
                                <th>报名时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${registrations}">
                                <tr>
                                    <td>
                                        <c:if test="${not activity.registrationClosed}">
                                            <input type="checkbox" name="userIds" value="${r.userId}" class="participant-checkbox">
                                        </c:if>
                                    </td>
                                    <td>${r.userName}</td>
                                    <td class="text-muted">${r.studentId}</td>
                                    <td class="text-muted">${r.major}</td>
                                    <td class="text-muted">
                                        <fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.displayStatus == 'confirmed'}">
                                                <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已确认</span>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'pending'}">
                                                <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待审核</span>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'rejected'}">
                                                <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已驳回</span>
                                                <c:if test="${not empty r.notes}">
                                                    <br><small class="text-muted">原因: ${r.notes}</small>
                                                </c:if>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'expired'}">
                                                <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">已过期</span>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="btn-list flex-nowrap">
                                            <c:if test="${not activity.registrationClosed}">
                                                <c:if test="${r.status == 'pending'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=approve&activityId=${activity.id}&userId=${r.userId}" class="btn btn-sm btn-success-design">通过</a>
                                                </c:if>
                                                <c:if test="${r.status == 'pending'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=reject&activityId=${activity.id}&userId=${r.userId}" class="btn btn-sm btn-danger-design">拒绝</a>
                                                </c:if>
                                                <c:if test="${r.status == 'rejected'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=deleteRegistration&activityId=${activity.id}&userId=${r.userId}" class="btn btn-sm btn-outline-danger-design" onclick="return confirm('确定要删除该报名记录吗？用户可以重新报名。')">删除</a>
                                                </c:if>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty registrations}">
                                <tr>
                                    <td colspan="7" class="text-center text-muted">暂无报名人员</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    document.getElementById('selectAll').addEventListener('change', function() {
        const checkboxes = document.querySelectorAll('.participant-checkbox');
        checkboxes.forEach(function(checkbox) {
            checkbox.checked = document.getElementById('selectAll').checked;
        });
    });
    
    document.querySelectorAll('.participant-checkbox').forEach(function(checkbox) {
        checkbox.addEventListener('change', function() {
            const allCheckboxes = document.querySelectorAll('.participant-checkbox');
            const checkedCheckboxes = document.querySelectorAll('.participant-checkbox:checked');
            document.getElementById('selectAll').checked = allCheckboxes.length === checkedCheckboxes.length;
        });
    });
</script>

<jsp:include page="../../common/layout_bottom.jsp" />