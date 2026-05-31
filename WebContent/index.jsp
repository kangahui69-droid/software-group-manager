<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="jsp/common/layout_top.jsp">
    <jsp:param name="title" value="欢迎" />
    <jsp:param name="active" value="home" />
</jsp:include>

<style>
    .hero-section {
        padding: 80px 0;
        text-align: center;
        background: linear-gradient(180deg, var(--bg-white) 0%, #f8fafc 100%);
    }

    .hero-title {
        font-family: var(--font-display);
        font-size: 3.5rem;
        font-weight: 600;
        line-height: 1.10;
        color: var(--text-dark);
        margin-bottom: 24px;
    }

    .hero-subtitle {
        font-family: var(--font-ui);
        font-size: 1.25rem;
        color: var(--text-secondary);
        line-height: 1.6;
        max-width: 600px;
        margin: 0 auto 40px;
    }

    .hero-buttons {
        display: flex;
        gap: 16px;
        justify-content: center;
        flex-wrap: wrap;
    }

    .btn-hero-primary {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 14px 32px;
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.94rem;
        text-decoration: none;
        transition: all 0.3s ease;
        box-shadow: 0 4px 14px rgba(20, 86, 240, 0.35);
    }

    .btn-hero-primary:hover {
        background: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(20, 86, 240, 0.4);
    }

    .btn-hero-secondary {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 14px 32px;
        background: var(--bg-white);
        color: var(--text-dark);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.94rem;
        text-decoration: none;
        transition: all 0.3s ease;
    }

    .btn-hero-secondary:hover {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
        background: rgba(20, 86, 240, 0.03);
    }

    .section-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
        text-align: center;
        margin-bottom: 48px;
    }

    .news-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
        transition: all 0.3s ease;
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    .news-card:hover {
        transform: translateY(-6px);
        box-shadow: var(--shadow-brand-purple);
        border-color: transparent;
    }

    .news-card-body {
        padding: 24px;
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .news-card-icon {
        width: 48px;
        height: 48px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        margin-bottom: 16px;
    }

    .news-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 12px;
    }

    .news-card-text {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        color: var(--text-secondary);
        line-height: 1.6;
        flex: 1;
    }

    .news-card-meta {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid var(--border-light);
    }

    .quick-links-section {
        padding: 64px 0;
        background: var(--bg-white);
    }

    .quick-link-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        text-decoration: none;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 16px;
        border: 1px solid var(--border-light);
        margin-bottom: 12px;
    }

    .quick-link-card:hover {
        transform: translateX(8px);
        border-color: var(--brand-blue);
        box-shadow: var(--shadow-standard);
    }

    .quick-link-icon {
        width: 44px;
        height: 44px;
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
        font-weight: 600;
        font-size: 0.94rem;
        color: var(--text-dark);
        margin-bottom: 2px;
    }

    .quick-link-desc {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
    }

    .quick-link-arrow {
        color: var(--text-muted);
        transition: transform 0.3s ease;
    }

    .quick-link-card:hover .quick-link-arrow {
        transform: translateX(4px);
        color: var(--brand-blue);
    }

    .features-section {
        padding: 80px 0;
        background: linear-gradient(180deg, #f8fafc 0%, var(--bg-white) 100%);
    }

    .feature-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        padding: 32px;
        text-align: center;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        transition: all 0.3s ease;
        height: 100%;
    }

    .feature-card:hover {
        transform: translateY(-6px);
        box-shadow: var(--shadow-brand-purple);
        border-color: transparent;
    }

    .feature-icon {
        width: 72px;
        height: 72px;
        border-radius: var(--radius-generous);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2rem;
        margin: 0 auto 20px;
    }

    .feature-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 12px;
    }

    .feature-desc {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        color: var(--text-secondary);
        line-height: 1.6;
    }

    .welcome-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: rgba(20, 86, 240, 0.08);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        font-family: var(--font-ui);
        font-size: 0.81rem;
        font-weight: 500;
        margin-bottom: 20px;
    }

    .view-more-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        color: var(--brand-blue);
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.88rem;
        text-decoration: none;
        transition: gap 0.3s ease;
    }

    .view-more-link:hover {
        gap: 10px;
        color: var(--primary-600);
    }

    @media (max-width: 768px) {
        .hero-title {
            font-size: 2.25rem;
        }

        .hero-subtitle {
            font-size: 1rem;
        }

        .section-title {
            font-size: 1.5rem;
            margin-bottom: 32px;
        }

        .hero-buttons {
            flex-direction: column;
            align-items: center;
        }

        .btn-hero-primary,
        .btn-hero-secondary {
            width: 100%;
            justify-content: center;
        }
    }
</style>

<div class="page-wrapper">
    <div class="page-body">
        <section class="hero-section">
            <div class="container-xl">
                <span class="welcome-badge animate-float">
                    <i class="bi bi-stars"></i>
                    欢迎来到软件小组
                </span>
                <h1 class="hero-title">信息工程学院<br>软件兴趣小组</h1>
                <p class="hero-subtitle">
                    这里是一个专注于软件技术学习与交流的平台。<br>
                    加入我们，一起探索技术的乐趣！
                </p>
                <div class="hero-buttons">
                    <c:choose>
                        <c:when test="${empty sessionScope.user}">
                            <a href="${pageContext.request.contextPath}/login.jsp" class="btn-hero-primary">
                                <i class="bi bi-box-arrow-in-right"></i>
                                立即登录
                            </a>
                            <a href="${pageContext.request.contextPath}/recruit/apply.jsp" class="btn-hero-secondary">
                                <i class="bi bi-person-plus"></i>
                                加入我们
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/member/index.jsp" class="btn-hero-primary">
                                <i class="bi bi-grid-1x2"></i>
                                个人中心
                            </a>
                            <a href="${pageContext.request.contextPath}/ai?action=chat" class="btn-hero-secondary">
                                <i class="bi bi-robot"></i>
                                AI助手
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <section class="features-section">
            <div class="container-xl">
                <h2 class="section-title">平台功能</h2>
                <div class="row g-4">
                    <div class="col-md-6 col-lg-3">
                        <div class="feature-card">
                            <div class="feature-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
                                <i class="bi bi-newspaper text-white"></i>
                            </div>
                            <h3 class="feature-title">新闻管理</h3>
                            <p class="feature-desc">查看通知公告、活动新闻、获奖信息等内容</p>
                        </div>
                    </div>
                    <div class="col-md-6 col-lg-3">
                        <div class="feature-card">
                            <div class="feature-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);">
                                <i class="bi bi-robot text-white"></i>
                            </div>
                            <h3 class="feature-title">AI助手</h3>
                            <p class="feature-desc">智能问答助手，帮助解决学习和项目中的问题</p>
                        </div>
                    </div>
                    <div class="col-md-6 col-lg-3">
                        <div class="feature-card">
                            <div class="feature-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                                <i class="bi bi-kanban text-white"></i>
                            </div>
                            <h3 class="feature-title">项目管理</h3>
                            <p class="feature-desc">创建、管理和参与项目，锻炼团队协作能力</p>
                        </div>
                    </div>
                    <div class="col-md-6 col-lg-3">
                        <div class="feature-card">
                            <div class="feature-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
                                <i class="bi bi-calendar-check text-white"></i>
                            </div>
                            <h3 class="feature-title">活动管理</h3>
                            <p class="feature-desc">组织和参与各类技术分享和兴趣活动</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section class="quick-links-section">
            <div class="container-xl">
                <h2 class="section-title">快速链接</h2>
                <div class="row g-4">
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/news?type=notice" class="quick-link-card">
                            <div class="quick-link-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                                <i class="bi bi-megaphone"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">通知公告</div>
                                <div class="quick-link-desc">查看小组最新通知</div>
                            </div>
                            <i class="bi bi-arrow-right quick-link-arrow"></i>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/news?type=award" class="quick-link-card">
                            <div class="quick-link-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                                <i class="bi bi-trophy"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">获奖新闻</div>
                                <div class="quick-link-desc">了解成员荣誉成就</div>
                            </div>
                            <i class="bi bi-arrow-right quick-link-arrow"></i>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/news?type=activity" class="quick-link-card">
                            <div class="quick-link-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                <i class="bi bi-calendar-event"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">活动新闻</div>
                                <div class="quick-link-desc">回顾精彩活动瞬间</div>
                            </div>
                            <i class="bi bi-arrow-right quick-link-arrow"></i>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/news?type=tech" class="quick-link-card">
                            <div class="quick-link-icon" style="background: rgba(99, 102, 241, 0.1); color: #6366f1;">
                                <i class="bi bi-lightbulb"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">技术分享</div>
                                <div class="quick-link-desc">阅读技术干货文章</div>
                            </div>
                            <i class="bi bi-arrow-right quick-link-arrow"></i>
                        </a>
                    </div>
                </div>

                <c:if test="${empty sessionScope.user}">
                    <div class="text-center mt-5">
                        <a href="${pageContext.request.contextPath}/recruit/apply.jsp" class="btn-hero-primary">
                            <i class="bi bi-person-plus"></i>
                            申请加入小组
                        </a>
                    </div>
                </c:if>
            </div>
        </section>
    </div>
</div>

<jsp:include page="jsp/common/layout_bottom.jsp" />