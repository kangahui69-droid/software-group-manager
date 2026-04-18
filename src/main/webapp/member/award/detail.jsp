<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<% 
    if (request.getAttribute("award") == null) {
        Integer awardId = null;
        try {
            awardId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list.jsp?error=invalid_id");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/award?action=detail&id=" + awardId);
        return;
    }
%>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项详情" />
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

    .info-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
    }

    .info-card-header {
        padding: 16px 20px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .info-card-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }

    .info-card-body {
        padding: 20px;
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

    .btn-success-brand {
        background-color: #10b981;
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

    .btn-success-brand:hover {
        background-color: #059669;
        color: white;
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

    .badge-rejected {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .alert-design {
        padding: 16px 20px;
        border-radius: var(--radius-comfortable);
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .alert-pending {
        background: rgba(245, 158, 11, 0.1);
        border: 1px solid rgba(245, 158, 11, 0.2);
    }

    .alert-approved {
        background: rgba(16, 185, 129, 0.1);
        border: 1px solid rgba(16, 185, 129, 0.2);
    }

    .alert-rejected {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .image-gallery {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 16px;
    }

    .image-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        overflow: hidden;
        border: 1px solid var(--border-light);
    }

    .image-preview {
        width: 100%;
        height: 160px;
        object-fit: cover;
    }

    .image-card-body {
        padding: 12px;
    }

    .quick-link {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 14px;
        border-radius: var(--radius-comfortable);
        text-decoration: none;
        transition: all 0.3s ease;
        border: 1px solid var(--border-light);
        margin-bottom: 8px;
    }

    .quick-link:hover {
        background: rgba(20, 86, 240, 0.05);
        border-color: var(--brand-blue);
    }

    .quick-link:last-child {
        margin-bottom: 0;
    }

    .quick-link-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.25rem;
        flex-shrink: 0;
    }

    .quick-link-content {
        flex: 1;
    }

    .quick-link-title {
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--text-dark);
    }

    .quick-link-desc {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }

    .back-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: var(--radius-standard);
        background: rgba(255, 255, 255, 0.2);
        color: white;
        text-decoration: none;
        font-size: 0.875rem;
        transition: all 0.2s ease;
    }
    .back-btn:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <a href="${pageContext.request.contextPath}/award?action=list" class="back-btn">
                    <i class="bi bi-arrow-left"></i>返回
                </a>
            </div>
            <h1 class="member-hero-title">
                <i class="bi bi-trophy me-2"></i>奖项详情
            </h1>
            <p class="member-hero-subtitle">查看您的获奖信息</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle text-brand"></i>比赛信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛名称</div>
                                    <div class="info-value">${award.competition}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛时间</div>
                                    <div class="info-value"><fmt:formatDate value="${award.competitionTime}" pattern="yyyy-MM-dd" /></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛地点</div>
                                    <div class="info-value">${award.competitionLocation}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛界别</div>
                                    <div class="info-value">${award.competitionSession}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛类型</div>
                                    <div class="info-value">
                                        <c:forEach var="dict" items="${awardTypes}">
                                            <c:if test="${award.awardType eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">比赛等级</div>
                                    <div class="info-value">
                                        <c:forEach var="dict" items="${competitionLevels}">
                                            <c:if test="${award.competitionLevel eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-award text-brand"></i>奖项信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">奖项等级</div>
                                    <div class="info-value">
                                        <c:forEach var="dict" items="${awardLevels}">
                                            <c:if test="${award.awardLevel eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">奖项类别</div>
                                    <div class="info-value">
                                        <c:forEach var="dict" items="${awardCategories}">
                                            <c:if test="${award.awardCategory eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">提交时间</div>
                                    <div class="info-value"><fmt:formatDate value="${award.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                                </div>
                            </div>
                            <c:forEach var="dict" items="${awardTypes}">
                                <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <div class="info-label">团队名称</div>
                                            <div class="info-value">${award.teamName}</div>
                                        </div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-images text-brand"></i>奖项图片
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="image-gallery">
                            <c:forEach var="image" items="${awardImages}">
                                <% long timestamp = System.currentTimeMillis(); %>
                                <div class="image-card">
                                    <img src="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" alt="奖项图片" class="image-preview">
                                    <div class="image-card-body">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div>
                                                <c:if test="${image.isMain}">
                                                    <span class="badge-design badge-approved">主图</span>
                                                </c:if>
                                            </div>
                                            <a href="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" target="_blank" class="btn-outline-brand" style="padding: 6px 12px; font-size: 0.75rem;">
                                                <i class="bi bi-eye"></i>查看
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty awardImages}">
                                <div class="col-12">
                                    <div class="text-center text-muted py-5">
                                        <i class="bi bi-image" style="font-size: 48px; opacity: 0.5;"></i>
                                        <p class="mt-2">暂无图片</p>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-lightning text-brand"></i>快捷操作
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <a href="${pageContext.request.contextPath}/award?action=list" class="quick-link">
                            <div class="quick-link-icon" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                <i class="bi bi-list-ul"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">查看所有奖项</div>
                                <div class="quick-link-desc">浏览您提交的所有奖项</div>
                            </div>
                        </a>
                        <c:if test="${award.awardStatus eq 'REJECTED'}">
                            <a href="${pageContext.request.contextPath}/award?action=delete&id=${award.id}" class="quick-link" style="border-color: rgba(239, 68, 68, 0.3);" onclick="return confirm('确定要删除这个已拒绝的奖项吗？删除后将无法恢复，且会删除相关的文件和关联项。');">
                                <div class="quick-link-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                                    <i class="bi bi-trash"></i>
                                </div>
                                <div class="quick-link-content">
                                    <div class="quick-link-title" style="color: #ef4444;">删除奖项</div>
                                    <div class="quick-link-desc">仅已拒绝的奖项可删除</div>
                                </div>
                            </a>
                        </c:if>
                        <c:if test="${award.awardStatus eq 'PENDING'}">
                            <a href="${pageContext.request.contextPath}/award?action=edit&id=${award.id}" class="quick-link">
                                <div class="quick-link-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                                    <i class="bi bi-pencil"></i>
                                </div>
                                <div class="quick-link-content">
                                    <div class="quick-link-title">编辑奖项</div>
                                    <div class="quick-link-desc">修改奖项信息</div>
                                </div>
                            </a>
                        </c:if>
                        <c:if test="${award.awardStatus eq 'APPROVED'}">
                            <a href="${pageContext.request.contextPath}/award?action=addImage&id=${award.id}" class="quick-link">
                                <div class="quick-link-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                    <i class="bi bi-plus-circle"></i>
                                </div>
                                <div class="quick-link-content">
                                    <div class="quick-link-title">补充图片</div>
                                    <div class="quick-link-desc">为奖项添加更多图片</div>
                                </div>
                            </a>
                        </c:if>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-check-circle text-brand"></i>审核状态
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:choose>
                            <c:when test="${award.awardStatus eq 'PENDING'}">
                                <div class="alert-design alert-pending">
                                    <i class="bi bi-clock" style="font-size: 24px; color: #f59e0b;"></i>
                                    <div>
                                        <div style="font-weight: 600; color: #f59e0b;">待审核</div>
                                        <div style="font-size: 0.81rem; color: var(--text-muted);">您的奖项正在等待审核，请耐心等待</div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${award.awardStatus eq 'APPROVED'}">
                                <div class="alert-design alert-approved">
                                    <i class="bi bi-check-circle-fill" style="font-size: 24px; color: #10b981;"></i>
                                    <div>
                                        <div style="font-weight: 600; color: #10b981;">已审核</div>
                                        <div style="font-size: 0.81rem; color: var(--text-muted);">恭喜！您的奖项已通过审核</div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert-design alert-rejected">
                                    <i class="bi bi-x-circle" style="font-size: 24px; color: #ef4444;"></i>
                                    <div>
                                        <div style="font-weight: 600; color: #ef4444;">已拒绝</div>
                                        <div style="font-size: 0.81rem; color: var(--text-muted);">很遗憾，您的奖项未通过审核</div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-bar-chart text-brand"></i>奖项统计
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="info-item">
                            <div class="info-label">图片数量</div>
                            <div class="info-value">${not empty awardImages ? awardImages.size() : 0} 张图片</div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">奖项等级</div>
                            <div class="info-value">
                                <c:forEach var="dict" items="${awardLevels}">
                                    <c:if test="${award.awardLevel eq dict.id}">${dict.name}</c:if>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />