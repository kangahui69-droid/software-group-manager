<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="项目管理" />
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
                <i class="bi bi-folder-check me-2"></i>项目管理
            </h1>
            <p class="admin-hero-subtitle">管理系统项目</p>
        </div>

        <div class="card-design mb-4" style="padding: 20px 24px;">
            <form method="get" action="${pageContext.request.contextPath}/project" class="row g-3">
                <div class="col-md-4">
                    <label class="form-label-design">关键词</label>
                    <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索标题/描述/团队名">
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">状态</label>
                    <select class="input-design" name="status">
                        <option value="">全部</option>
                        <option value="pending" ${status == 'pending' ? 'selected' : ''}>待审核</option>
                        <option value="approved" ${status == 'approved' ? 'selected' : ''}>已通过</option>
                        <option value="rejected" ${status == 'rejected' ? 'selected' : ''}>已拒绝</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">年份</label>
                    <select class="input-design" name="year">
                        <option value="">全部</option>
                        <option value="2026" ${year == '2026' ? 'selected' : ''}>2026</option>
                        <option value="2025" ${year == '2025' ? 'selected' : ''}>2025</option>
                        <option value="2024" ${year == '2024' ? 'selected' : ''}>2024</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn-brand w-100" style="justify-content: center;">
                        <i class="bi bi-search"></i>搜索
                    </button>
                </div>
            </form>
        </div>

        <div class="card-design">
            <table class="table-design">
                <thead>
                    <tr>
                        <th>标题</th>
                        <th>负责人</th>
                        <th>状态</th>
                        <th>年份</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${projects}">
                        <tr>
                            <td style="font-weight: 500; color: var(--text-dark);">${p.name}</td>
                            <td style="color: var(--text-secondary);">${p.leaderName}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.status == 'pending'}">
                                        <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #d97706;">待审核</span>
                                    </c:when>
                                    <c:when test="${p.status == 'approved'}">
                                        <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">已通过</span>
                                    </c:when>
                                    <c:when test="${p.status == 'in_progress'}">
                                        <span class="badge-design" style="background: rgba(59, 130, 246, 0.1); color: #2563eb;">进行中</span>
                                    </c:when>
                                    <c:when test="${p.status == 'completed'}">
                                        <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">已完成</span>
                                    </c:when>
                                    <c:when test="${p.status == 'canceled'}">
                                        <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">已取消</span>
                                    </c:when>
                                    <c:when test="${p.status == 'rejected'}">
                                        <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">已拒绝</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-design" style="background: rgba(107, 114, 128, 0.1); color: #6b7280;">${p.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="color: var(--text-secondary);">${p.year}</td>
                            <td>
                                <div class="d-flex gap-2">
                                    <a href="${pageContext.request.contextPath}/project?action=edit&id=${p.id}" class="btn-sm-brand">
                                        <i class="bi bi-pencil"></i>编辑
                                    </a>
                                    <form action="${pageContext.request.contextPath}/project" method="POST" onsubmit="return confirm('确定要删除吗？')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <button type="submit" class="btn-danger-design">
                                            <i class="bi bi-trash"></i>删除
                                        </button>
                                    </form>
                                    <c:if test="${p.status == 'pending'}">
                                        <form action="${pageContext.request.contextPath}/project" method="POST">
                                            <input type="hidden" name="action" value="approve">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit" class="btn-success-design">
                                                <i class="bi bi-check-lg"></i>批准
                                            </button>
                                        </form>
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

<jsp:include page="../../common/layout_bottom.jsp" />