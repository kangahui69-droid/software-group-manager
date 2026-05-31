<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的简历" />
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
        padding: 48px 24px;
        text-align: center;
    }

    .empty-state {
        padding: 48px 24px;
    }

    .empty-state-icon {
        font-size: 64px;
        color: var(--text-muted);
        margin-bottom: 16px;
        opacity: 0.5;
    }

    .empty-state-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .empty-state-desc {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        color: var(--text-muted);
        margin-bottom: 24px;
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
                <i class="bi bi-file-earmark-person me-2"></i>我的简历
            </h1>
            <p class="member-hero-subtitle">在线简历维护和管理</p>
        </div>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-file-earmark-text text-brand"></i>简历功能
                </h3>
            </div>
            <div class="card-body-design">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="bi bi-tools"></i>
                    </div>
                    <h3 class="empty-state-title">功能开发中</h3>
                    <p class="empty-state-desc">简历功能正在紧张开发中，请耐心等待...</p>
                    <a href="${pageContext.request.contextPath}/member/index.jsp" class="btn-brand">
                        <i class="bi bi-house"></i>返回个人中心
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />