<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的项目" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
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
        padding: 0;
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

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
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

    .badge-pending {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
    }

    .badge-approved {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-in-progress {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
    }

    .badge-completed {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-canceled {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
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

    .btn-sm-outline {
        background: transparent;
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
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

    .btn-sm-outline:hover {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-kanban me-2"></i>我的项目
            </h1>
            <p class="member-hero-subtitle">查看和管理您的项目</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/project?action=create" class="btn-brand">
                <i class="bi bi-plus-lg"></i>添加项目
            </a>
        </div>

        <div class="card-design">
            <div class="card-body-design">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>标题</th>
                            <th>分类</th>
                            <th>状态</th>
                            <th>年份</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${projects}">
                            <tr>
                                <td>
                                    <a href="${pageContext.request.contextPath}/project?action=detail&id=${p.id}" class="text-decoration-none" style="color: var(--text-dark); font-weight: 500;">
                                        ${p.name}
                                    </a>
                                </td>
                                <td style="color: var(--text-secondary);">${p.category}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'pending'}">
                                            <span class="badge-design badge-pending">待审批</span>
                                        </c:when>
                                        <c:when test="${p.status == 'approved'}">
                                            <span class="badge-design badge-approved">已批准</span>
                                        </c:when>
                                        <c:when test="${p.status == 'in_progress'}">
                                            <span class="badge-design badge-in-progress">进行中</span>
                                        </c:when>
                                        <c:when test="${p.status == 'completed'}">
                                            <span class="badge-design badge-completed">已完成</span>
                                        </c:when>
                                        <c:when test="${p.status == 'canceled'}">
                                            <span class="badge-design badge-canceled">已取消</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design badge-pending">${p.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${p.year}</td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/project?action=detail&id=${p.id}" class="btn-sm-brand">
                                            <i class="bi bi-eye"></i>详情
                                        </a>
                                        <c:if test="${p.leaderId == sessionScope.user.id || sessionScope.user.role == 'ADMIN'}">
                                            <a href="${pageContext.request.contextPath}/project?action=edit&id=${p.id}" class="btn-sm-outline">
                                                <i class="bi bi-pencil"></i>编辑
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty projects}">
                            <tr>
                                <td colspan="5" class="text-center text-muted" style="padding: 48px;">暂无项目记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />