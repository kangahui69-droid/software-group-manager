<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的申请记录" />
</jsp:include>

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
        padding: 12px 16px;
        text-align: left;
        font-size: 0.81rem;
    }

    .table-design td {
        padding: 14px 16px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
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

    .badge-confirmed {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-rejected {
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

    .link-primary {
        color: var(--brand-blue);
        text-decoration: none;
        font-weight: 500;
    }

    .link-primary:hover {
        text-decoration: underline;
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
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h1 class="member-hero-title">
                        <i class="bi bi-list-check me-2"></i>我的申请记录
                    </h1>
                    <p class="member-hero-subtitle">查看您提交的项目申请</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn-outline-brand" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.3); color: white;">
                    <i class="bi bi-arrow-left"></i>返回项目列表
                </a>
            </div>
        </div>

        <c:if test="${not empty param.msg}">
            <div class="alert alert-info alert-dismissible mb-4" style="border-radius: var(--radius-standard);" role="alert">
                <i class="bi bi-info-circle me-2"></i>${param.msg}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-file-earmark-text text-brand"></i>我的项目申请
                </h3>
            </div>
            <div class="card-body-design" style="padding: 0;">
                <c:if test="${empty applications}">
                    <div class="text-center text-muted py-5">
                        <i class="bi bi-inbox" style="font-size: 48px; opacity: 0.5;"></i>
                        <p class="mt-2">暂无申请记录</p>
                        <a href="${pageContext.request.contextPath}/project?action=list" class="btn-brand">
                            <i class="bi bi-folder"></i>浏览项目
                        </a>
                    </div>
                </c:if>
                <c:if test="${not empty applications}">
                    <table class="table-design">
                        <thead>
                            <tr>
                                <th>项目名称</th>
                                <th>申请时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="app" items="${applications}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/project?action=detail&id=${app.projectId}" class="link-primary">${app.projectName}</a>
                                    </td>
                                    <td><fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${app.status == 'pending'}">
                                                <span class="badge-design badge-pending">申请待确认</span>
                                            </c:when>
                                            <c:when test="${app.status == 'confirmed'}">
                                                <span class="badge-design badge-confirmed">申请成功</span>
                                            </c:when>
                                            <c:when test="${app.status == 'rejected'}">
                                                <span class="badge-design badge-rejected">申请已驳回</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-design badge-pending">${app.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${app.status == 'confirmed'}">
                                            <a href="${pageContext.request.contextPath}/project?action=workspace&id=${app.projectId}" class="btn-sm-brand">
                                                <i class="bi bi-box-arrow-in-right"></i>进入项目
                                            </a>
                                        </c:if>
                                        <c:if test="${app.status != 'pending'}">
                                            <a href="${pageContext.request.contextPath}/project?action=detail&id=${app.projectId}" class="btn-sm-outline">
                                                <i class="bi bi-eye"></i>查看项目
                                            </a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />