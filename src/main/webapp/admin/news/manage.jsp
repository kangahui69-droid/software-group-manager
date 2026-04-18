<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="新闻管理" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
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

    .card-body-design {
        padding: 0;
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

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
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

    .btn-danger-outline {
        background: transparent;
        color: #ef4444;
        border: 1px solid #ef4444;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
    }

    .btn-danger-outline:hover {
        background: #fef2f2;
        color: #dc2626;
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
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
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
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
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

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
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
        <div class="admin-hero">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <a href="${pageContext.request.contextPath}/admin/index.jsp" class="back-btn">
                    <i class="bi bi-arrow-left"></i>返回
                </a>
            </div>
            <h1 class="admin-hero-title">
                <i class="bi bi-newspaper me-2"></i>新闻管理
            </h1>
            <p class="admin-hero-subtitle">管理系统内的所有新闻</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/news?action=edit" class="btn-brand">
                <i class="bi bi-plus-lg"></i>发布新闻
            </a>
        </div>

        <div class="card-design mb-4" style="padding: 20px 24px;">
            <form method="get" action="${pageContext.request.contextPath}/news" class="row g-3">
                <input type="hidden" name="action" value="manage">
                <div class="col-md-4">
                    <label class="form-label-design">关键词</label>
                    <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索标题/摘要/内容">
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">类型</label>
                    <select class="input-design" name="type">
                        <option value="">全部</option>
                        <option value="notice" ${type == 'notice' ? 'selected' : ''}>通知公告</option>
                        <option value="activity" ${type == 'activity' ? 'selected' : ''}>活动新闻</option>
                        <option value="award" ${type == 'award' ? 'selected' : ''}>奖项新闻</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label-design">状态</label>
                    <select class="input-design" name="status">
                        <option value="">全部</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>正常</option>
                        <option value="0" ${status == '0' ? 'selected' : ''}>已删除</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn-brand w-100" style="justify-content: center;">
                        <i class="bi bi-search"></i>搜索
                    </button>
                </div>
            </form>
        </div>

        <div class="card-design">
            <div class="card-body-design">
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
                                    <td style="color: var(--text-muted);"><%= news.getId() %></td>
                                    <td style="font-weight: 500; color: var(--text-dark);"><%= news.getTitle() %></td>
                                    <td><span class="badge-design" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);"><%= news.getType() %></span></td>
                                    <td style="color: var(--text-secondary);"><small><%= news.getSummary() != null ? (news.getSummary().length() > 20 ? news.getSummary().substring(0, 20) + "..." : news.getSummary()) : "无" %></small></td>
                                    <td style="color: var(--text-secondary);"><%= news.getCreatedAt() %></td>
                                    <td>
                                        <% if (news.getStatus() == 1) { %>
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">正常</span>
                                        <% } else { %>
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">已删除</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <div class="d-flex gap-2">
                                            <a href="${pageContext.request.contextPath}/news?action=edit&id=<%= news.getId() %>" class="btn-sm-brand">
                                                <i class="bi bi-pencil"></i>编辑
                                            </a>
                                            <% if (news.getStatus() == 1) { %>
                                                <form action="${pageContext.request.contextPath}/news?action=delete&id=<%= news.getId() %>" method="POST" class="d-inline" onsubmit="return confirm('确定要删除这条新闻吗？');">
                                                    <button type="submit" class="btn-danger-outline">
                                                        <i class="bi bi-trash"></i>删除
                                                    </button>
                                                </form>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                                <% } } else { %>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted" style="padding: 48px;">暂无新闻记录</td>
                                    </tr>
                                <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />