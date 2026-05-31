<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="申请加入项目 - ${project.name}" />
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

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
    }

    .input-group-design {
        display: flex;
        align-items: center;
        background: var(--bg-white);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        overflow: hidden;
        transition: all 0.3s ease;
    }

    .input-group-design:focus-within {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    }

    .input-group-design textarea {
        flex: 1;
        border: none;
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        outline: none;
        background: transparent;
        resize: vertical;
        min-height: 120px;
    }

    .info-card {
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        border: 1px solid rgba(20, 86, 240, 0.1);
    }

    .info-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 14px;
    }

    .info-item:last-child {
        margin-bottom: 0;
    }

    .info-icon {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: var(--brand-blue);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        flex-shrink: 0;
    }

    .info-text {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-secondary);
        line-height: 1.5;
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

    .checkbox-design {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
    }

    .checkbox-design input {
        width: 18px;
        height: 18px;
        accent-color: var(--brand-blue);
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
                <i class="bi bi-person-plus me-2"></i>申请加入项目
            </h1>
            <p class="member-hero-subtitle">填写申请信息并提交审核</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle text-brand"></i>项目信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="mb-3">
                            <div class="form-label-design">项目名称</div>
                            <div style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark);">${project.name}</div>
                        </div>
                        <div class="mb-3">
                            <div class="form-label-design">项目描述</div>
                            <div style="color: var(--text-secondary);">${project.description}</div>
                        </div>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-pencil text-brand"></i>填写申请
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form action="${pageContext.request.contextPath}/project" method="POST">
                            <input type="hidden" name="action" value="applyMember">
                            <input type="hidden" name="id" value="${project.id}">

                            <div class="mb-4">
                                <label class="form-label-design required">申请理由</label>
                                <div class="input-group-design">
                                    <textarea name="reason" rows="5" placeholder="请说明您想加入该项目的原因和优势" required></textarea>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="checkbox-design">
                                    <input type="checkbox" required>
                                    <span style="color: var(--text-secondary);">我已阅读并了解项目信息，愿意申请加入</span>
                                </label>
                            </div>

                            <div class="d-flex justify-content-end gap-3">
                                <a href="${pageContext.request.contextPath}/project?action=detail&id=${project.id}" class="btn-outline-brand">
                                    <i class="bi bi-x-lg"></i>取消
                                </a>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-send"></i>提交申请
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle text-brand"></i>申请须知
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="info-card">
                            <div class="info-item">
                                <div class="info-icon">
                                    <i class="bi bi-check"></i>
                                </div>
                                <div class="info-text">提交申请后需要项目管理员审批</div>
                            </div>
                            <div class="info-item">
                                <div class="info-icon">
                                    <i class="bi bi-check"></i>
                                </div>
                                <div class="info-text">审批通过后您将成为项目成员</div>
                            </div>
                            <div class="info-item">
                                <div class="info-icon">
                                    <i class="bi bi-check"></i>
                                </div>
                                <div class="info-text">申请被驳回后可再次申请</div>
                            </div>
                            <div class="info-item">
                                <div class="info-icon">
                                    <i class="bi bi-check"></i>
                                </div>
                                <div class="info-text">成员可查看项目完整信息</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />