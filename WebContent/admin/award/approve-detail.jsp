<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项详情审核" />
</jsp:include>

<%
    String competitionLevelName = "";
    String awardLevelName = "";
    Object competitionLevelsObj = request.getAttribute("competitionLevels");
    Object awardLevelsObj = request.getAttribute("awardLevels");
    if (competitionLevelsObj != null) {
        for (Object obj : (java.util.List)competitionLevelsObj) {
            model.Dictionary dict = (model.Dictionary)obj;
            Object awardObj = request.getAttribute("award");
            if (awardObj != null) {
                try {
                    java.lang.reflect.Method m = awardObj.getClass().getMethod("getCompetitionLevel");
                    Object val = m.invoke(awardObj);
                    if (val != null && val.toString().equals(dict.getId().toString())) {
                        competitionLevelName = dict.getName();
                        break;
                    }
                } catch (Exception e) {}
            }
        }
    }
    if (awardLevelsObj != null) {
        for (Object obj : (java.util.List)awardLevelsObj) {
            model.Dictionary dict = (model.Dictionary)obj;
            Object awardObj = request.getAttribute("award");
            if (awardObj != null) {
                try {
                    java.lang.reflect.Method m = awardObj.getClass().getMethod("getAwardLevel");
                    Object val = m.invoke(awardObj);
                    if (val != null && val.toString().equals(dict.getId().toString())) {
                        awardLevelName = dict.getName();
                        break;
                    }
                } catch (Exception e) {}
            }
        }
    }
    pageContext.setAttribute("competitionLevelName", competitionLevelName);
    pageContext.setAttribute("awardLevelName", awardLevelName);
%>

<style>
    .page-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
        display: flex;
        align-items: center;
        justify-content: space-between;
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

    .btn-back {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        font-size: 0.88rem;
        border: 1px solid rgba(255, 255, 255, 0.3);
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-back:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }

    .detail-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        margin-bottom: 24px;
        overflow: hidden;
    }

    .detail-card-header {
        padding: 24px 32px;
        border-bottom: 1px solid var(--border-light);
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .detail-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .detail-card-body {
        padding: 32px;
    }

    .info-section {
        margin-bottom: 32px;
    }

    .info-section:last-child {
        margin-bottom: 0;
    }

    .info-section-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 24px;
    }

    .info-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
    }

    .info-icon {
        width: 44px;
        height: 44px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        font-size: 1rem;
    }

    .info-content {
        flex: 1;
    }

    .info-label {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        color: var(--text-muted);
        margin-bottom: 4px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .info-value {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        font-weight: 500;
        color: var(--text-dark);
    }

    .level-badge {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        border: 1px solid #fcd34d;
        border-radius: var(--radius-standard);
        padding: 12px 20px;
        margin-bottom: 24px;
    }

    .level-badge-icon {
        width: 32px;
        height: 32px;
        border-radius: var(--radius-standard);
        background: rgba(245, 158, 11, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #d97706;
        font-size: 1rem;
    }

    .level-badge-content {
        flex: 1;
    }

    .level-badge-title {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        color: #92400e;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .level-badge-value {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: #78350f;
    }

    .image-gallery {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;
    }

    .image-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
        transition: all 0.3s ease;
    }

    .image-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-purple);
    }

    .image-card img {
        width: 100%;
        height: 200px;
        object-fit: cover;
    }

    .image-card-body {
        padding: 12px;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .badge-main {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 10px;
        border-radius: var(--radius-pill);
        background: var(--brand-blue);
        color: white;
    }

    .btn-view {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        color: var(--brand-blue);
        text-decoration: none;
        transition: color 0.3s ease;
    }

    .btn-view:hover {
        color: var(--primary-600);
        text-decoration: underline;
    }

    .action-section {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
    }

    .btn-action {
        padding: 10px 20px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 600;
        border-radius: var(--radius-standard);
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-approve {
        background: #059669;
        color: white;
        border: none;
    }

    .btn-approve:hover {
        background: #047857;
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-reject {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .btn-reject:hover {
        background: #dc2626;
        color: white;
    }

    .status-message {
        border-radius: var(--radius-standard);
        padding: 16px 20px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .status-success {
        background: rgba(16, 185, 129, 0.1);
        border: 1px solid rgba(16, 185, 129, 0.2);
        color: #059669;
    }

    .status-danger {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
        color: #dc2626;
    }

    @media (max-width: 768px) {
        .page-hero {
            flex-direction: column;
            align-items: flex-start;
            gap: 16px;
            padding: 24px;
        }

        .page-hero-title {
            font-size: 1.5rem;
        }

        .info-grid {
            grid-template-columns: 1fr;
        }

        .image-gallery {
            grid-template-columns: 1fr;
        }

        .detail-card-body {
            padding: 24px;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="page-hero">
            <div>
                <h1 class="page-hero-title">
                    <i class="bi bi-award me-2"></i>奖项详情审核
                </h1>
                <p class="page-hero-subtitle">查看并审核成员的奖项申请详情</p>
            </div>
            <a href="${pageContext.request.contextPath}/award?action=approveList" class="btn-back">
                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                返回列表
            </a>
        </div>

        <div class="detail-card">
            <div class="detail-card-header">
                <h3 class="detail-card-title">
                    <i class="bi bi-file-text text-brand"></i>奖项详情
                </h3>
            </div>
            <div class="detail-card-body">
                <c:if test="${not empty competitionLevelName or not empty awardLevelName}">
                    <div class="level-badge">
                        <div class="level-badge-icon">
                            <i class="bi bi-star-fill"></i>
                        </div>
                        <div class="level-badge-content">
                            <div class="level-badge-title">奖项等级</div>
                            <div class="level-badge-value">
                                <c:if test="${not empty competitionLevelName}">${competitionLevelName}</c:if>
                                <c:if test="${not empty awardLevelName}"> / ${awardLevelName}</c:if>
                            </div>
                        </div>
                    </div>
                </c:if>

                <div class="info-section">
                    <h4 class="info-section-title">
                        <i class="bi bi-trophy text-brand"></i>比赛信息
                    </h4>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                                <i class="bi bi-trophy"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">比赛名称</div>
                                <div class="info-value">${award.competition}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(249, 115, 22, 0.1); color: #f97316;">
                                <i class="bi bi-bar-chart"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">比赛等级</div>
                                <div class="info-value">${competitionLevelName}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                <i class="bi bi-calendar-event"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">比赛时间</div>
                                <div class="info-value"><fmt:formatDate value='${award.competitionTime}' pattern='yyyy-MM-dd' /></div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(20, 184, 166, 0.1); color: #14b8a6;">
                                <i class="bi bi-geo-alt"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">比赛地点</div>
                                <div class="info-value">${award.competitionLocation}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(236, 72, 153, 0.1); color: #ec4899;">
                                <i class="bi bi-circle-half"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">比赛界别</div>
                                <div class="info-value">${award.competitionSession}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="info-section">
                    <h4 class="info-section-title">
                        <i class="bi bi-award text-brand"></i>奖项信息
                    </h4>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                <i class="bi bi-check-circle"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">奖项名称</div>
                                <div class="info-value">${award.name}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                                <i class="bi bi-star"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">奖项等级</div>
                                <div class="info-value">${awardLevelName}</div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(99, 102, 241, 0.1); color: #6366f1;">
                                <i class="bi bi-tag"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">奖项类型</div>
                                <div class="info-value">
                                    <c:forEach var="dict" items="${awardTypes}">
                                        <c:if test="${award.awardType eq dict.id}">${dict.name}</c:if>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(168, 85, 247, 0.1); color: #a855f7;">
                                <i class="bi bi-grid-3x3-gap"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">奖项类别</div>
                                <div class="info-value">
                                    <c:forEach var="dict" items="${awardCategories}">
                                        <c:if test="${award.awardCategory eq dict.id}">${dict.name}</c:if>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                        <c:forEach var="dict" items="${awardTypes}">
                            <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                <div class="info-item">
                                    <div class="info-icon" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                        <i class="bi bi-people"></i>
                                    </div>
                                    <div class="info-content">
                                        <div class="info-label">团队名称</div>
                                        <div class="info-value">${award.teamName}</div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">
                                <i class="bi bi-clock-history"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">审核状态</div>
                                <div class="info-value">
                                    <c:choose>
                                        <c:when test="${award.awardStatus eq 'PENDING'}">待审核</c:when>
                                        <c:when test="${award.awardStatus eq 'APPROVED'}">已通过</c:when>
                                        <c:otherwise>已打回</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-icon" style="background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">
                                <i class="bi bi-calendar-plus"></i>
                            </div>
                            <div class="info-content">
                                <div class="info-label">提交时间</div>
                                <div class="info-value"><fmt:formatDate value='${award.createdAt}' pattern='yyyy-MM-dd HH:mm:ss' /></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="info-section">
                    <h4 class="info-section-title">
                        <i class="bi bi-image text-brand"></i>奖项图片
                    </h4>
                    <c:choose>
                        <c:when test="${not empty awardImages}">
                            <% long timestamp = System.currentTimeMillis(); %>
                            <div class="image-gallery">
                                <c:forEach var="image" items="${awardImages}">
                                    <div class="image-card">
                                        <img src="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" alt="奖项图片">
                                        <div class="image-card-body">
                                            <c:if test="${image.isMain}">
                                                <span class="badge-main">主图</span>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" target="_blank" class="btn-view">查看原图</a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="text-align: center; padding: 48px 24px; color: var(--text-muted);">
                                <i class="bi bi-image" style="font-size: 48px; margin-bottom: 16px;"></i>
                                <p>暂无图片</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="info-section">
                    <h4 class="info-section-title">
                        <i class="bi bi-gear text-brand"></i>审核操作
                    </h4>
                    <c:choose>
                        <c:when test="${award.awardStatus eq 'PENDING'}">
                            <div class="action-section">
                                <form action="${pageContext.request.contextPath}/award?action=approve" method="post">
                                    <input type="hidden" name="id" value="${award.id}">
                                    <button type="submit" class="btn-action btn-approve">
                                        <i class="bi bi-check-lg"></i>通过
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/award?action=reject" method="post">
                                    <input type="hidden" name="id" value="${award.id}">
                                    <button type="submit" class="btn-action btn-reject">
                                        <i class="bi bi-x-lg"></i>打回
                                    </button>
                                </form>
                            </div>
                        </c:when>
                        <c:when test="${award.awardStatus eq 'APPROVED'}">
                            <div class="status-message status-success">
                                <i class="bi bi-check-circle-fill"></i>
                                <span>此奖项已通过审核</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="status-message status-danger">
                                <i class="bi bi-x-circle-fill"></i>
                                <span>此奖项已被打回</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />