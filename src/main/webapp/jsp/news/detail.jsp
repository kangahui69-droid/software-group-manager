<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="${news.title}" />
</jsp:include>

<style>
    .news-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .news-card-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 24px 32px;
        border: none;
    }
    
    .news-card-header .breadcrumb {
        margin-bottom: 12px;
    }
    
    .news-card-header .breadcrumb-item a {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        font-size: 0.88rem;
    }
    
    .news-card-header .breadcrumb-item.active {
        color: rgba(255, 255, 255, 1);
    }
    
    .news-title {
        font-family: var(--font-display);
        font-size: 2rem;
        font-weight: 600;
        color: white;
        line-height: 1.3;
        margin: 0;
    }
    
    .news-meta {
        display: flex;
        align-items: center;
        gap: 16px;
        margin-top: 16px;
        color: rgba(255, 255, 255, 0.9);
        font-size: 0.88rem;
    }
    
    .news-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
        font-size: 0.75rem;
        font-weight: 500;
    }
    
    .news-card-body {
        padding: 40px 32px;
    }
    
    .news-summary {
        font-size: 1.125rem;
        color: var(--text-secondary);
        line-height: 1.7;
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .news-content {
        font-size: 1rem;
        line-height: 1.8;
        color: var(--text-primary);
    }
    
    .news-content p {
        margin-bottom: 16px;
    }
    
    .back-btn {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        border: none;
        font-weight: 500;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    
    .back-btn:hover {
        background: var(--border-gray);
        color: var(--text-dark);
        transform: translateX(-4px);
    }
    
    .share-btn {
        background: var(--bg-light-gray);
        border: none;
        border-radius: var(--radius-standard);
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        color: var(--text-secondary);
    }
    
    .share-btn:hover {
        background: var(--brand-blue);
        color: white;
    }
    
    .page-title-back {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="row justify-content-center">
            <div class="col-lg-10">
                <div class="card news-card">
                    <div class="card-header news-card-header">
                        <ol class="breadcrumb" aria-label="breadcrumbs">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp">首页</a></li>
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/news/list">新闻动态</a></li>
                            <li class="breadcrumb-item active" aria-current="page">${news.type}</li>
                        </ol>
                        <h2 class="news-title">${news.title}</h2>
                        <div class="news-meta">
                            <span><i class="bi bi-calendar3 me-1"></i> ${news.createdAt}</span>
                            <span class="news-badge">${news.type}</span>
                        </div>
                    </div>
                    <div class="card-body news-card-body">
                        <p class="news-summary">${news.summary}</p>
                        <div class="news-content">
                            ${content}
                        </div>
                    </div>
                    <div class="card-footer" style="background: var(--bg-white); border: none; padding: 24px 32px;">
                        <div class="d-flex justify-content-between align-items-center">
                            <a href="javascript:history.back()" class="back-btn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                                返回上一页
                            </a>
                            <a href="${pageContext.request.contextPath}/news/list" class="back-btn">
                                <i class="bi bi-list me-1"></i> 返回列表
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />

<script>
function copyLink() {
    navigator.clipboard.writeText(window.location.href).then(function() {
        alert('链接已复制到剪贴板');
    }, function() {
        prompt('链接：', window.location.href);
    });
}
</script>