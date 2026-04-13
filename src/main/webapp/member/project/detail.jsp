<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目详情" />
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
    }

    .card-body-design {
        padding: 24px;
    }

    .info-item {
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .info-item:last-child {
        border-bottom: none;
    }

    .info-label {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
        margin-bottom: 4px;
    }

    .info-value {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-dark);
        font-weight: 500;
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

    .btn-secondary-brand {
        background-color: var(--bg-light-gray);
        color: var(--text-secondary);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-secondary-brand:hover {
        background-color: var(--border-gray);
        color: var(--text-dark);
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

    .badge-other {
        background: rgba(139, 92, 246, 0.1);
        color: #8b5cf6;
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
                        <i class="bi bi-kanban me-2"></i>项目详情
                    </h1>
                    <p class="member-hero-subtitle">${project.name}</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn-outline-brand" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.3); color: white;">
                    <i class="bi bi-arrow-left"></i>返回列表
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">${project.name}</h3>
                    </div>
                    <div class="card-body-design">
                        <div class="mb-4">
                            <div class="info-label">项目描述</div>
                            <div class="info-value">${project.description}</div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">项目类型</div>
                                    <div class="info-value">${project.category}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">年份</div>
                                    <div class="info-value">${project.year}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">项目预算</div>
                                    <div class="info-value">${project.budget != null ? project.budget : '未设置'}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">期望开始时间</div>
                                    <div class="info-value"><fmt:formatDate value="${project.expectedStartDate}" pattern="yyyy-MM-dd"/></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">期望结束时间</div>
                                    <div class="info-value"><fmt:formatDate value="${project.expectedEndDate}" pattern="yyyy-MM-dd"/></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">实际开始时间</div>
                                    <div class="info-value"><fmt:formatDate value="${project.actualStartDate}" pattern="yyyy-MM-dd"/></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">实际结束时间</div>
                                    <div class="info-value"><fmt:formatDate value="${project.actualEndDate}" pattern="yyyy-MM-dd"/></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">创建时间</div>
                                    <div class="info-value"><fmt:formatDate value="${project.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                                </div>
                            </div>
                        </div>
                        <c:if test="${not empty project.repoUrl}">
                            <div class="info-item mt-3">
                                <div class="info-label">仓库地址</div>
                                <div class="info-value">
                                    <a href="${project.repoUrl}" target="_blank" class="link-primary">${project.repoUrl}</a>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${not empty project.docUrl}">
                            <div class="info-item">
                                <div class="info-label">文档地址</div>
                                <div class="info-value">
                                    <a href="${project.docUrl}" target="_blank" class="link-primary">${project.docUrl}</a>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <c:if test="${isMember}">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">项目成员</h3>
                    </div>
                    <div class="card-body-design" style="padding: 0;">
                        <table class="table-design">
                            <thead>
                                <tr>
                                    <th>成员</th>
                                    <th>角色</th>
                                    <th>加入时间</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="member" items="${members}">
                                    <tr>
                                        <td>${member.operatorName}</td>
                                        <td>
                                            <span class="badge-design" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                                ${member.role == 'ADMIN' ? '管理员' : '成员'}
                                            </span>
                                        </td>
                                        <td>${member.createdAt}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                </c:if>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">项目状态</h3>
                    </div>
                    <div class="card-body-design">
                        <div class="mb-3">
                            <span class="badge-design ${project.status == 'approved' ? 'badge-approved' : project.status == 'pending' ? 'badge-pending' : 'badge-other'}">
                                ${project.status == 'approved' ? '已批准' : project.status == 'pending' ? '待审批' : project.status}
                            </span>
                        </div>
                        <c:choose>
                            <c:when test="${isMember && (project.status == 'approved' || project.status == 'in_progress' || project.status == 'completed')}">
                                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn-brand w-100 mb-2 justify-content-center">
                                    <i class="bi bi-box-arrow-in-right"></i>进入项目工作区
                                </a>
                                <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn-outline-brand w-100 justify-content-center">
                                    <i class="bi bi-clock-history"></i>查看项目历史
                                </a>
                            </c:when>
                            <c:when test="${isMember && project.status == 'pending'}">
                                <div class="alert alert-warning" style="border-radius: var(--radius-standard); padding: 12px 16px; margin-bottom: 16px;">
                                    <i class="bi bi-clock me-2"></i>项目正在审批中，审批通过后即可进入工作区
                                </div>
                                <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn-outline-brand w-100 justify-content-center">
                                    <i class="bi bi-clock-history"></i>查看项目历史
                                </a>
                            </c:when>
                            <c:when test="${hasApplication}">
                                <button class="btn-secondary-brand w-100 justify-content-center" disabled>
                                    <i class="bi bi-hourglass-split"></i>已提交申请
                                </button>
                            </c:when>
                            <c:when test="${project.status == 'approved'}">
                                <a href="${pageContext.request.contextPath}/project?action=apply&id=${project.id}" class="btn-outline-brand w-100 justify-content-center">
                                    <i class="bi bi-plus-circle"></i>申请加入项目
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button class="btn-secondary-brand w-100 justify-content-center" disabled>
                                    <i class="bi bi-clock"></i>项目待审批
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />