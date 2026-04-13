<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项列表" />
</jsp:include>

<style>
    .page-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .page-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .page-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .filter-tabs {
        display: flex;
        gap: 8px;
        margin-bottom: 24px;
        flex-wrap: wrap;
    }

    .filter-tab {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        padding: 8px 16px;
        border-radius: var(--radius-pill);
        border: 1px solid var(--border-gray);
        background: var(--bg-white);
        color: var(--text-secondary);
        text-decoration: none;
        transition: all 0.3s ease;
    }

    .filter-tab:hover {
        background: var(--bg-light-gray);
        color: var(--text-dark);
    }

    .filter-tab.active {
        background: var(--brand-blue);
        color: white;
        border-color: var(--brand-blue);
    }

    .search-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        margin-bottom: 24px;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 14px;
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

    .select-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 14px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
        background: white;
    }

    .select-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .btn-search {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        font-size: 0.875rem;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
    }

    .btn-search:hover {
        background: var(--primary-600);
        color: white;
    }

    .table-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design thead {
        background: var(--bg-light-gray);
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.875rem;
        color: var(--text-secondary);
        padding: 16px 20px;
        text-align: left;
        border-bottom: 2px solid var(--border-gray);
    }

    .table-design td {
        padding: 16px 20px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-dark);
        border-bottom: 1px solid var(--border-light);
        vertical-align: middle;
    }

    .table-design tbody tr {
        transition: all 0.2s ease;
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
        display: inline-block;
    }

    .badge-yellow {
        background: rgba(245, 158, 11, 0.15);
        color: #d97706;
    }

    .badge-green {
        background: rgba(16, 185, 129, 0.15);
        color: #059669;
    }

    .badge-red {
        background: rgba(239, 68, 68, 0.15);
        color: #dc2626;
    }

    .btn-action {
        padding: 6px 14px;
        font-family: var(--font-ui);
        font-size: 0.81rem;
        font-weight: 500;
        border-radius: var(--radius-standard);
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }

    .btn-action-link {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        border: none;
    }

    .btn-action-link:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-action-approve {
        background: rgba(16, 185, 129, 0.1);
        color: #059669;
        border: 1px solid rgba(16, 185, 129, 0.2);
    }

    .btn-action-approve:hover {
        background: #059669;
        color: white;
    }

    .btn-action-reject {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .btn-action-reject:hover {
        background: #dc2626;
        color: white;
    }

    .empty-state {
        text-align: center;
        padding: 64px 24px;
    }

    .empty-state-icon {
        font-size: 64px;
        color: var(--text-muted);
        margin-bottom: 16px;
    }

    .empty-state h3 {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .empty-state p {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-muted);
    }

    @media (max-width: 768px) {
        .page-hero {
            padding: 24px;
        }

        .page-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="page-hero">
            <h1 class="page-hero-title">
                <i class="bi bi-trophy me-2"></i>奖项列表管理
            </h1>
            <p class="page-hero-subtitle">审核和管理成员的奖项申请</p>
        </div>

        <div class="filter-tabs">
            <a href="${pageContext.request.contextPath}/award?action=approveList&status=ALL" class="filter-tab ${param.status == 'ALL' || empty param.status ? 'active' : ''}">全部</a>
            <a href="${pageContext.request.contextPath}/award?action=approveList&status=PENDING" class="filter-tab ${param.status == 'PENDING' ? 'active' : ''}">待审核</a>
            <a href="${pageContext.request.contextPath}/award?action=approveList&status=APPROVED" class="filter-tab ${param.status == 'APPROVED' ? 'active' : ''}">已通过</a>
            <a href="${pageContext.request.contextPath}/award?action=approveList&status=REJECTED" class="filter-tab ${param.status == 'REJECTED' ? 'active' : ''}">已打回</a>
        </div>

        <div class="search-card">
            <form method="get" action="${pageContext.request.contextPath}/award" class="row g-3">
                <input type="hidden" name="action" value="approveList">
                <div class="col-md-3">
                    <label class="form-label-design">关键词</label>
                    <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索奖项名称/比赛名称">
                </div>
                <div class="col-md-2">
                    <label class="form-label-design">奖项类型</label>
                    <select class="select-design" name="awardType">
                        <option value="">全部</option>
                        <c:forEach var="dict" items="${awardTypes}">
                            <option value="${dict.id}" <c:if test="${param.awardType == dict.id}">selected</c:if>>${dict.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label-design">奖项类别</label>
                    <select class="select-design" name="awardCategory">
                        <option value="">全部</option>
                        <c:forEach var="dict" items="${awardCategories}">
                            <option value="${dict.id}" <c:if test="${param.awardCategory == dict.id}">selected</c:if>>${dict.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label-design">奖项等级</label>
                    <select class="select-design" name="awardLevel">
                        <option value="">全部</option>
                        <c:forEach var="dict" items="${awardLevels}">
                            <option value="${dict.id}" <c:if test="${param.awardLevel == dict.id}">selected</c:if>>${dict.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label-design">比赛等级</label>
                    <select class="select-design" name="competitionLevel">
                        <option value="">全部</option>
                        <c:forEach var="dict" items="${competitionLevels}">
                            <option value="${dict.id}" <c:if test="${param.competitionLevel == dict.id}">selected</c:if>>${dict.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-1">
                    <label class="form-label-design">&nbsp;</label>
                    <button type="submit" class="btn-search">搜索</button>
                </div>
            </form>
        </div>

        <div class="table-card">
            <table class="table-design">
                <thead>
                    <tr>
                        <th>序号</th>
                        <th>奖项名称</th>
                        <th>比赛信息</th>
                        <th>奖项类型</th>
                        <th>提交时间</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="award" items="${awards}" varStatus="status">
                        <tr>
                            <td>${status.index + 1}</td>
                            <td>${award.name}</td>
                            <td>
                                <div>${award.competition}</div>
                                <div style="font-size: 0.75rem; color: var(--text-muted);">
                                    <fmt:formatDate value="${award.competitionTime}" pattern="yyyy-MM-dd" />
                                    <c:if test="${not empty award.competitionSession}">，${award.competitionSession}</c:if>
                                </div>
                            </td>
                            <td>
                                <div>
                                    <c:forEach var="dict" items="${awardTypes}">
                                        <c:if test="${award.awardType eq dict.id}">${dict.name}</c:if>
                                    </c:forEach>
                                </div>
                                <div style="font-size: 0.75rem; color: var(--text-muted);">
                                    <c:forEach var="dict" items="${awardCategories}">
                                        <c:if test="${award.awardCategory eq dict.id}">${dict.name}</c:if>
                                    </c:forEach>
                                </div>
                            </td>
                            <td>
                                <fmt:formatDate value="${award.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${award.awardStatus eq 'PENDING'}">
                                        <span class="badge-design badge-yellow">待审核</span>
                                    </c:when>
                                    <c:when test="${award.awardStatus eq 'APPROVED'}">
                                        <span class="badge-design badge-green">已通过</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-design badge-red">已打回</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                    <a href="${pageContext.request.contextPath}/award?action=approveDetail&id=${award.id}" class="btn-action btn-action-link">查看详情</a>
                                    <c:if test="${award.awardStatus eq 'PENDING'}">
                                        <form action="${pageContext.request.contextPath}/award?action=approve" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="${award.id}">
                                            <button type="submit" class="btn-action btn-action-approve">通过</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/award?action=reject" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="${award.id}">
                                            <button type="submit" class="btn-action btn-action-reject">打回</button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty awards}">
                        <tr>
                            <td colspan="7">
                                <div class="empty-state">
                                    <div class="empty-state-icon">
                                        <i class="bi bi-inbox"></i>
                                    </div>
                                    <h3>暂无数据</h3>
                                    <p>没有找到符合条件的奖项记录</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />