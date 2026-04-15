<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项列表" />
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

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
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

    .card-body-design {
        padding: 24px;
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
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover, .btn-outline-brand.active {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
        background: rgba(20, 86, 240, 0.05);
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
                <i class="bi bi-trophy me-2"></i>奖项列表
            </h1>
            <p class="admin-hero-subtitle">管理系统内的所有奖项</p>
        </div>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-list-ul"></i>奖项列表管理
                </h3>
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/award?action=approveList&status=ALL" class="btn-outline-brand ${param.status == 'ALL' || empty param.status ? 'active' : ''}">全部</a>
                    <a href="${pageContext.request.contextPath}/award?action=approveList&status=PENDING" class="btn-outline-brand ${param.status == 'PENDING' ? 'active' : ''}">待审核</a>
                    <a href="${pageContext.request.contextPath}/award?action=approveList&status=APPROVED" class="btn-outline-brand ${param.status == 'APPROVED' ? 'active' : ''}">已通过</a>
                    <a href="${pageContext.request.contextPath}/award?action=approveList&status=REJECTED" class="btn-outline-brand ${param.status == 'REJECTED' ? 'active' : ''}">已打回</a>
                </div>
            </div>
            <div class="card-body-design">
                <div class="mb-4">
                    <form method="get" action="${pageContext.request.contextPath}/award" class="row g-3">
                        <input type="hidden" name="action" value="approveList">
                        <div class="col-md-3">
                            <label class="form-label-design">关键词</label>
                            <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索奖项名称/比赛名称">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">奖项类型</label>
                            <select class="input-design" name="awardType">
                                <option value="">全部</option>
                                <c:forEach var="dict" items="${awardTypes}">
                                    <option value="${dict.id}" <c:if test="${param.awardType == dict.id}">selected</c:if>>${dict.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">奖项类别</label>
                            <select class="input-design" name="awardCategory">
                                <option value="">全部</option>
                                <c:forEach var="dict" items="${awardCategories}">
                                    <option value="${dict.id}" <c:if test="${param.awardCategory == dict.id}">selected</c:if>>${dict.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">奖项等级</label>
                            <select class="input-design" name="awardLevel">
                                <option value="">全部</option>
                                <c:forEach var="dict" items="${awardLevels}">
                                    <option value="${dict.id}" <c:if test="${param.awardLevel == dict.id}">selected</c:if>>${dict.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">比赛等级</label>
                            <select class="input-design" name="competitionLevel">
                                <option value="">全部</option>
                                <c:forEach var="dict" items="${competitionLevels}">
                                    <option value="${dict.id}" <c:if test="${param.competitionLevel == dict.id}">selected</c:if>>${dict.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-1 d-flex align-items-end">
                            <button type="submit" class="btn-brand w-100" style="justify-content: center;">
                                <i class="bi bi-search"></i>
                            </button>
                        </div>
                    </form>
                </div>
                
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
                                <td style="color: var(--text-muted);">${status.index + 1}</td>
                                <td style="font-weight: 500; color: var(--text-dark);">${award.name}</td>
                                <td>
                                    <div style="color: var(--text-dark);">${award.competition}</div>
                                    <div style="color: var(--text-muted); font-size: 0.75rem;">
                                        <fmt:formatDate value="${award.competitionTime}" pattern="yyyy-MM-dd" />
                                        <c:if test="${not empty award.competitionSession}">，${award.competitionSession}</c:if>
                                    </div>
                                </td>
                                <td>
                                    <div style="color: var(--text-dark);">
                                        <c:forEach var="dict" items="${awardTypes}">
                                            <c:if test="${award.awardType eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                    <div style="color: var(--text-muted); font-size: 0.75rem;">
                                        <c:forEach var="dict" items="${awardCategories}">
                                            <c:if test="${award.awardCategory eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                </td>
                                <td style="color: var(--text-secondary);">
                                    <fmt:formatDate value="${award.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${award.awardStatus eq 'PENDING'}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待审核</span>
                                        </c:when>
                                        <c:when test="${award.awardStatus eq 'APPROVED'}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已通过</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已打回</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/award?action=approveDetail&id=${award.id}" class="btn-sm-brand">
                                            <i class="bi bi-eye"></i>查看
                                        </a>
                                        <c:if test="${award.awardStatus eq 'PENDING'}">
                                            <form action="${pageContext.request.contextPath}/award?action=approve" method="post" style="display: inline;">
                                                <input type="hidden" name="id" value="${award.id}">
                                                <button type="submit" class="btn-success-design">
                                                    <i class="bi bi-check-lg"></i>通过
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/award?action=reject" method="post" style="display: inline;">
                                                <input type="hidden" name="id" value="${award.id}">
                                                <button type="submit" class="btn-danger-design">
                                                    <i class="bi bi-x-lg"></i>打回
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty awards}">
                            <tr>
                                <td colspan="7" class="text-center" style="padding: 48px;">
                                    <div style="font-size: 48px; color: var(--text-muted); margin-bottom: 16px;">
                                        <i class="bi bi-inbox"></i>
                                    </div>
                                    <h3 style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); margin: 0 0 8px;">暂无数据</h3>
                                    <p style="color: var(--text-muted); margin: 0;">没有找到符合条件的奖项记录</p>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />