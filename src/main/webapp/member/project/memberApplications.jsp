<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="成员申请审批 - ${project.name}" />
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
        margin-bottom: 4px;
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

    .btn-success {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }

    .btn-success:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }

    .btn-danger:hover {
        background-color: #dc2626;
        color: white;
    }

    .application-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 20px;
        margin-bottom: 16px;
        transition: all 0.3s ease;
    }

    .application-card:hover {
        box-shadow: var(--shadow-brand-offset);
    }

    .applicant-name {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .applicant-info {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-bottom: 4px;
    }

    .application-reason {
        margin-top: 12px;
        padding: 12px;
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-standard);
        font-size: 0.875rem;
        color: var(--text-secondary);
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

    .badge-confirmed {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-rejected {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .modal-content {
        border-radius: var(--radius-comfortable);
        border: none;
        box-shadow: var(--shadow-brand-purple);
    }

    .modal-header {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .modal-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
    }

    .modal-body {
        padding: 24px;
    }

    .modal-footer {
        padding: 16px 24px;
        border-top: 1px solid var(--border-light);
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
                        <i class="bi bi-people me-2"></i>成员申请审批
                    </h1>
                    <p class="member-hero-subtitle">${project.name}</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn-outline-brand" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.3); color: white;">
                    <i class="bi bi-arrow-left"></i>返回项目
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-12">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-hourglass-split text-warning"></i>待审批申请
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:set var="hasPending" value="false"/>
                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status == 'pending'}">
                                <c:set var="hasPending" value="true"/>
                            </c:if>
                        </c:forEach>

                        <c:if test="${!hasPending}">
                            <div class="text-center text-muted py-4">
                                <i class="bi bi-check-circle" style="font-size: 48px; opacity: 0.5;"></i>
                                <p class="mt-2">暂无待审批的申请</p>
                            </div>
                        </c:if>

                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status == 'pending'}">
                                <div class="application-card">
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <div class="applicant-name">${app.userName}</div>
                                            <div class="applicant-info">
                                                <span class="me-3">学号: ${app.studentId}</span>
                                                <span>专业: ${app.major}</span>
                                            </div>
                                            <div class="applicant-info">
                                                申请时间: <fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                            </div>
                                            <c:if test="${not empty app.reason}">
                                                <div class="application-reason">
                                                    <strong>申请理由:</strong> ${app.reason}
                                                </div>
                                            </c:if>
                                        </div>
                                        <div class="d-flex gap-2">
                                            <form method="post" action="${pageContext.request.contextPath}/project" class="d-inline">
                                                <input type="hidden" name="action" value="approveMember">
                                                <input type="hidden" name="applicationId" value="${app.id}">
                                                <input type="hidden" name="projectId" value="${project.id}">
                                                <button type="submit" class="btn-success">
                                                    <i class="bi bi-check-lg"></i>通过
                                                </button>
                                            </form>
                                            <button class="btn-danger" data-bs-toggle="modal" data-bs-target="#rejectModal${app.id}">
                                                <i class="bi bi-x-lg"></i>驳回
                                            </button>

                                            <div class="modal fade" id="rejectModal${app.id}" tabindex="-1">
                                                <div class="modal-dialog">
                                                    <form method="post" action="${pageContext.request.contextPath}/project" class="modal-content">
                                                        <input type="hidden" name="action" value="rejectMember">
                                                        <input type="hidden" name="applicationId" value="${app.id}">
                                                        <input type="hidden" name="projectId" value="${project.id}">
                                                        <div class="modal-header">
                                                            <h5 class="modal-title">驳回申请</h5>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="mb-3">
                                                                <label class="form-label">驳回理由</label>
                                                                <textarea name="reason" class="form-control" rows="3" placeholder="请输入驳回理由" style="border-radius: var(--radius-standard); padding: 12px 16px;"></textarea>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn-outline-brand" data-bs-dismiss="modal">取消</button>
                                                            <button type="submit" class="btn-danger">确认驳回</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-history text-brand"></i>历史申请记录
                        </h3>
                    </div>
                    <div class="card-body-design" style="padding: 0;">
                        <table class="table-design">
                            <thead>
                                <tr>
                                    <th>申请人</th>
                                    <th>学号</th>
                                    <th>专业</th>
                                    <th>申请时间</th>
                                    <th>状态</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="app" items="${applications}">
                                    <c:if test="${app.status != 'pending'}">
                                        <tr>
                                            <td>${app.userName}</td>
                                            <td>${app.studentId}</td>
                                            <td>${app.major}</td>
                                            <td><fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td>
                                                <c:if test="${app.status == 'confirmed'}">
                                                    <span class="badge-design badge-confirmed">已通过</span>
                                                </c:if>
                                                <c:if test="${app.status == 'rejected'}">
                                                    <span class="badge-design badge-rejected">已驳回</span>
                                                    <c:if test="${not empty app.reason}">
                                                        <div class="small text-muted mt-1">理由: ${app.reason}</div>
                                                    </c:if>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                        <c:set var="hasHistory" value="false"/>
                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status != 'pending'}">
                                <c:set var="hasHistory" value="true"/>
                            </c:if>
                        </c:forEach>
                        <c:if test="${!hasHistory}">
                            <p class="text-muted text-center py-4">暂无历史记录</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />