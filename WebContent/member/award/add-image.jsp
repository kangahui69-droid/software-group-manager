<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<% 
    if (request.getAttribute("award") == null) {
        Integer awardId = null;
        try {
            awardId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list.jsp?error=invalid_id");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/award?action=addImage&id=" + awardId);
        return;
    }
%>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="补充奖项图片" />
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
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        border: 1px solid rgba(20, 86, 240, 0.1);
    }

    .info-item {
        margin-bottom: 12px;
    }

    .info-item:last-child {
        margin-bottom: 0;
    }

    .info-label {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
        margin-bottom: 2px;
    }

    .info-value {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-dark);
        font-weight: 500;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
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
        padding: 12px 24px;
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

    .file-upload-area {
        border: 2px dashed var(--border-gray);
        border-radius: var(--radius-comfortable);
        padding: 32px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .file-upload-area:hover {
        border-color: var(--brand-blue);
        background: rgba(20, 86, 240, 0.02);
    }

    .file-upload-icon {
        font-size: 48px;
        color: var(--text-muted);
        margin-bottom: 12px;
    }

    .file-upload-text {
        font-family: var(--font-ui);
        color: var(--text-secondary);
        font-size: 0.94rem;
    }

    .file-upload-hint {
        font-family: var(--font-ui);
        color: var(--text-muted);
        font-size: 0.81rem;
        margin-top: 8px;
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
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
                <i class="bi bi-images me-2"></i>补充奖项图片
            </h1>
            <p class="member-hero-subtitle">为您的奖项添加更多证明图片</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-upload text-brand"></i>上传图片
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form action="${pageContext.request.contextPath}/award?action=addImage" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="id" value="${award.id}">

                            <div class="info-card mb-4">
                                <h4 style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; margin-bottom: 16px;">奖项基本信息</h4>
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <div class="info-label">比赛名称</div>
                                            <div class="info-value">${award.competition}</div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <div class="info-label">奖项类型</div>
                                            <div class="info-value">
                                                <c:choose>
                                                    <c:when test="${award.awardType eq 'TYPE_INDIVIDUAL'}">个人</c:when>
                                                    <c:otherwise>团队</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <div class="info-label">审核状态</div>
                                            <div class="info-value">
                                                <span class="badge-design">已审核</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design required">奖项图片</label>
                                <input type="file" class="form-control" name="awardImages" multiple accept="image/*" required style="border-radius: var(--radius-standard); padding: 12px 16px; border: 1px solid var(--border-gray);">
                                <div class="file-upload-hint mt-2">可上传多张图片，支持JPG、PNG等格式</div>
                            </div>

                            <div class="d-flex justify-content-end gap-3">
                                <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="btn-outline-brand">
                                    <i class="bi bi-x-lg"></i>取消
                                </a>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-upload"></i>上传图片
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />