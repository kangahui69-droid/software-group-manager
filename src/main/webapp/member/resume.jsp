<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的简历" />
</jsp:include>

<style>
:root {
    --brand-blue: #1456f0;
    --font-display: 'Outfit', sans-serif;
    --font-ui: 'DM Sans', sans-serif;
    --radius-generous: 16px;
    --radius-standard: 12px;
    --radius-comfortable: 10px;
    --radius-pill: 9999px;
    --shadow-brand-purple: 0 4px 20px rgba(20, 85, 240, 0.15);
    --shadow-brand-offset: 0 8px 32px rgba(20, 85, 240, 0.12);
    --shadow-standard: 0 2px 8px rgba(0, 0, 0, 0.06);
    --bg-white: #ffffff;
    --text-dark: #1a1a2e;
    --text-muted: #6b7280;
    --border-gray: #e5e7eb;
    --border-light: #f3f4f6;
    --primary-light: #eff6ff;
}
body {
    font-family: var(--font-ui);
    background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
}
.page-header { padding: 24px 0; }
.page-title {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0;
}
.card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-standard);
    box-shadow: var(--shadow-standard);
}
.card-body { padding: 32px; }
.coming-soon {
    text-align: center;
    padding: 64px 24px;
}
.coming-soon-icon {
    width: 80px;
    height: 80px;
    background: var(--primary-light);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
}
.coming-soon-icon i {
    font-size: 36px;
    color: var(--brand-blue);
}
.coming-soon h2 {
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0 0 8px;
}
.coming-soon p {
    color: var(--text-muted);
    font-size: 14px;
    margin: 0;
}
.back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: var(--radius-standard);
    background: rgba(20, 86, 240, 0.1);
    color: var(--brand-blue);
    text-decoration: none;
    font-size: 0.875rem;
    transition: all 0.2s ease;
}
.back-btn:hover {
    background: rgba(20, 86, 240, 0.2);
    color: var(--brand-blue);
}
</style>

<div class="container-xl">
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <div class="d-flex align-items-center gap-3">
                    <a href="${pageContext.request.contextPath}/member/index.jsp" class="back-btn">
                        <i class="bi bi-arrow-left"></i>返回
                    </a>
                    <h2 class="page-title">我的简历</h2>
                </div>
            </div>
        </div>
    </div>
    
    <div class="card">
        <div class="card-body">
            <div class="coming-soon">
                <div class="coming-soon-icon">
                    <i class="bi bi-file-earmark-person"></i>
                </div>
                <h2>功能开发中</h2>
                <p>简历管理功能即将上线，敬请期待...</p>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />