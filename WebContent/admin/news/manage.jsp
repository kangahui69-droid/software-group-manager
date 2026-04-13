<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="新闻管理" />
</jsp:include>

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

    .btn-primary-design {
        background: white;
        color: var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 11px 20px;
        font-weight: 600;
        font-size: 0.88rem;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-primary-design:hover {
        background: var(--bg-light-gray);
        color: var(--brand-blue);
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .search-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        margin-bottom: 24px;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 14px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .select-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 14px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
        background: white;
    }

    .select-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .btn-search {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        font-size: 0.875rem;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
    }

    .btn-search:hover {
        background: var(--primary-600);
        color: white;
    }

    .table-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design thead {
        background: var(--bg-light-gray);
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        font-size: 0.875rem;
        color: var(--text-secondary);
        padding: 16px 20px;
        text-align: left;
        border-bottom: 2px solid var(--border-gray);
    }

    .table-design td {
        padding: 16px 20px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-dark);
        border-bottom: 1px solid var(--border-light);
        vertical-align: middle;
    }

    .table-design tbody tr {
        transition: all 0.2s ease;
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
        display: inline-block;
    }

    .badge-blue {
        background: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
    }

    .badge-success {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-danger {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .btn-action {
        padding: 6px 14px;
        font-family: var(--font-ui);
        font-size: 0.81rem;
        font-weight: 500;
        border-radius: var(--radius-standard);
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }

    .btn-action-edit {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border: 1px solid var(--border-gray);
    }

    .btn-action-edit:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }

    .btn-action-delete {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .btn-action-delete:hover {
        background: #ef4444;
        color: white;
    }

    .empty-state {
        text-align: center;
        padding: 64px 24px;
    }

    .empty-state-icon {
        font-size: 64px;
        color: var(--text-muted);
        margin-bottom: 16px;
    }

    .empty-state h3 {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 8px;
    }

    .empty-state p {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-muted);
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
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="page-hero">
            <div>
                <h1 class="page-hero-title">
                    <i class="bi bi-newspaper me-2"></i>新闻管理
                </h1>
                <p class="page-hero-subtitle">管理所有新闻内容，发布通知公告、活动和获奖新闻</p>
            </div>
            <a href="${pageContext.request.contextPath}/news?action=edit" class="btn-primary-design">
                <i class="bi bi-plus-lg"></i>发布新闻
            </a>
        </div>

        <div class="search-card">
            <form method="get" action="${pageContext.request.contextPath}/news" class="row g-3">
                <input type="hidden" name="action" value="manage">
                <div class="col-md-4">
                    <label class="form-label-design">关键词</label>
                    <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索标题/摘要/内容">
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">类型</label>
                    <select class="select-design" name="type">
                        <option value="">全部</option>
                        <option value="notice" ${type == 'notice' ? 'selected' : ''}>通知公告</option>
                        <option value="activity" ${type == 'activity' ? 'selected' : ''}>活动新闻</option>
                        <option value="award" ${type == 'award' ? 'selected' : ''}>奖项新闻</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">状态</label>
                    <select class="select-design" name="status">
                        <option value="">全部</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>正常</option>
                        <option value="0" ${status == '0' ? 'selected' : ''}>已删除</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label-design">&nbsp;</label>
                    <button type="submit" class="btn-search">搜索</button>
                </div>
            </form>
        </div>

        <div class="table-card">
            <table class="table-design">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>标题</th>
                        <th>类型</th>
                        <th>摘要</th>
                        <th>时间</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <% List<News> newsList = (List<News>) request.getAttribute("newsList");
                    if (newsList != null && !newsList.isEmpty()) {
                        for (News news : newsList) {
                    %>
                    <tr>
                        <td><%= news.getId() %></td>
                        <td><%= news.getTitle() %></td>
                        <td><span class="badge-design badge-blue"><%= news.getType() %></span></td>
                        <td class="text-muted"><small><%= news.getSummary() != null ? (news.getSummary().length() > 20 ? news.getSummary().substring(0, 20) + "..." : news.getSummary()) : "无" %></small></td>
                        <td class="text-muted"><%= news.getCreatedAt() %></td>
                        <td>
                            <% if (news.getStatus() == 1) { %>
                                <span class="badge-design badge-success">正常</span>
                            <% } else { %>
                                <span class="badge-design badge-danger">已删除</span>
                            <% } %>
                        </td>
                        <td>
                            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                <a href="${pageContext.request.contextPath}/news?action=edit&id=<%= news.getId() %>" class="btn-action btn-action-edit">编辑</a>
                                <% if (news.getStatus() == 1) { %>
                                    <form action="${pageContext.request.contextPath}/news?action=delete&id=<%= news.getId() %>" method="POST" style="display: inline;" onsubmit="return confirm('确定要删除这条新闻吗？');">
                                        <button type="submit" class="btn-action btn-action-delete">删除</button>
                                    </form>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <% } } else { %>
                    <tr>
                        <td colspan="7">
                            <div class="empty-state">
                                <div class="empty-state-icon">
                                    <i class="bi bi-inbox"></i>
                                </div>
                                <h3>暂无新闻记录</h3>
                                <p>点击"发布新闻"按钮创建第一条新闻</p>
                            </div>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />