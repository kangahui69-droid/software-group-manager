<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="活动管理" />
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
                <i class="bi bi-calendar-check me-2"></i>活动管理
            </h1>
            <p class="admin-hero-subtitle">管理小组活动，创建和审核活动申请</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/activity?action=createForm" class="btn-brand">
                <i class="bi bi-plus-lg"></i>发布活动
            </a>
        </div>

        <div class="card-design mb-3">
            <div class="card-body-design">
                <form action="${pageContext.request.contextPath}/activity" method="get" class="row g-3">
                    <input type="hidden" name="action" value="manage">
                    <div class="col-md-3">
                        <input type="text" name="keyword" class="input-design" placeholder="搜索活动名称、描述、地点" value="${keyword}">
                    </div>
                    <div class="col-md-2">
                        <select name="activityType" class="input-design">
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
                        <select name="status" class="input-design">
                            <option value="">所有状态</option>
                            <option value="upcoming" ${status == 'upcoming' ? 'selected' : ''}>即将开始</option>
                            <option value="ongoing" ${status == 'ongoing' ? 'selected' : ''}>进行中</option>
                            <option value="completed" ${status == 'completed' ? 'selected' : ''}>已结束</option>
                            <option value="canceled" ${status == 'canceled' ? 'selected' : ''}>已取消</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <select name="approvalStatus" class="input-design">
                            <option value="">所有审批</option>
                            <option value="pending" ${approvalStatus == 'pending' ? 'selected' : ''}>待审核</option>
                            <option value="approved" ${approvalStatus == 'approved' ? 'selected' : ''}>已批准</option>
                            <option value="rejected" ${approvalStatus == 'rejected' ? 'selected' : ''}>已拒绝</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-brand w-100">搜索</button>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/activity?action=manage" class="btn btn-outline-brand w-100">重置</a>
                    </div>
                </form>
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

        <div class="card-design">
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>类型</th>
                            <th>地点</th>
                            <th>活动状态</th>
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
                                        <c:when test="${a.computedStatus == 'upcoming'}">
                                            <span class="badge-design" style="background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">即将开始</span>
                                        </c:when>
                                        <c:when test="${a.computedStatus == 'ongoing'}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">进行中</span>
                                        </c:when>
                                        <c:when test="${a.computedStatus == 'completed'}">
                                            <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">已结束</span>
                                        </c:when>
                                        <c:when test="${a.computedStatus == 'canceled'}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已取消</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.approvalStatus == 'approved'}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已批准</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'pending'}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待审核</span>
                                        </c:when>
                                        <c:when test="${a.approvalStatus == 'rejected'}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已拒绝</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.status == 'completed' || a.status == 'canceled' || a.status == 'ongoing'}">
                                            <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">已结束</span>
                                        </c:when>
                                        <c:when test="${a.inRegistrationPeriod}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">报名中</span>
                                        </c:when>
                                        <c:when test="${a.registrationEnded}">
                                            <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">已结束</span>
                                        </c:when>
                                        <c:when test="${!a.registrationStarted}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">未开始</span>
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
                                                <a href="${pageContext.request.contextPath}/activity?action=approveActivity&id=${a.id}" class="btn btn-sm btn-success-design" onclick="return confirm('确定要批准此活动吗？')">批准</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=rejectActivity&id=${a.id}" class="btn btn-sm btn-danger-design" onclick="return confirm('确定要拒绝此活动吗？')">拒绝</a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/activity?action=participants&id=${a.id}" class="btn btn-sm btn-brand">报名管理</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=edit&id=${a.id}" class="btn btn-sm btn-outline-brand">编辑</a>
                                                <a href="${pageContext.request.contextPath}/activity?action=delete&id=${a.id}" class="btn btn-sm btn-outline-danger-design" onclick="return confirm('确定要删除此活动吗？')">删除</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty activities}">
                            <tr>
                                <td colspan="9" class="text-center text-muted">暂无活动记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />