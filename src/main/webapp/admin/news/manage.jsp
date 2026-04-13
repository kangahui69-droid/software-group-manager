<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="新闻管理" />
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
.page-header {
    background: transparent;
    padding: 24px 0;
}
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
.card-body {
    padding: 20px;
}
.form-label {
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 6px;
    font-size: 14px;
}
.form-control, .form-select {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 10px 14px;
    font-size: 14px;
    transition: all 0.2s ease;
}
.form-control:focus, .form-select:focus {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 85, 240, 0.1);
}
.btn-primary {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 10px 20px;
    font-weight: 600;
    font-size: 14px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-primary:hover {
    transform: translateY(-1px);
    box-shadow: var(--shadow-brand-purple);
}
.btn-white {
    background: var(--bg-white);
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 6px 12px;
    font-weight: 500;
    font-size: 13px;
    color: var(--text-dark);
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-white:hover {
    border-color: var(--brand-blue);
    color: var(--brand-blue);
}
.btn-outline-danger {
    background: transparent;
    border: 1px solid #ef4444;
    border-radius: var(--radius-comfortable);
    padding: 6px 12px;
    font-weight: 500;
    font-size: 13px;
    color: #ef4444;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-outline-danger:hover {
    background: #fef2f2;
}
.table {
    font-family: var(--font-ui);
}
.table th {
    font-weight: 600;
    color: var(--text-dark);
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    border-bottom: 2px solid var(--border-gray);
    padding: 12px 16px;
    background: var(--primary-light);
}
.table td {
    vertical-align: middle;
    padding: 14px 16px;
    font-size: 14px;
    color: var(--text-dark);
}
.table tbody tr {
    transition: background 0.15s ease;
}
.table tbody tr:hover {
    background: var(--primary-light);
}
.badge {
    font-weight: 500;
    font-size: 12px;
    padding: 4px 10px;
    border-radius: var(--radius-pill);
}
.bg-blue {
    background: var(--primary-light);
    color: var(--brand-blue);
}
.bg-success {
    background: #ecfdf5;
    color: #065f46;
}
.bg-danger {
    background: #fef2f2;
    color: #991b1b;
}
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    新闻管理
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/news?action=edit"
                        class="btn btn-primary d-none d-sm-inline-block">
                        <i class="bi bi-plus-lg me-2"></i>发布新闻
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card mb-3">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/news" class="row g-3">
                    <input type="hidden" name="action" value="manage">
                    <div class="col-md-4">
                        <label class="form-label">关键词</label>
                        <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索标题/摘要/内容">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">类型</label>
                        <select class="form-select" name="type">
                            <option value="">全部</option>
                            <option value="notice" ${type == 'notice' ? 'selected' : ''}>通知公告</option>
                            <option value="activity" ${type == 'activity' ? 'selected' : ''}>活动新闻</option>
                            <option value="award" ${type == 'award' ? 'selected' : ''}>奖项新闻</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">状态</label>
                        <select class="form-select" name="status">
                            <option value="">全部</option>
                            <option value="1" ${status == '1' ? 'selected' : ''}>正常</option>
                            <option value="0" ${status == '0' ? 'selected' : ''}>已删除</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">搜索</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>标题</th>
                            <th>类型</th>
                            <th>摘要</th>
                            <th>时间</th>
                            <th>状态</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% List<News> newsList = (List<News>) request.getAttribute("newsList");
                                if (newsList != null && !newsList.isEmpty()) {
                                for (News news : newsList) {
                                %>
                                <tr>
                                    <td>
                                        <%= news.getId() %>
                                                            </td>
                                                            <td class="font-weight-medium">
                                                                <%= news.getTitle() %>
                                                            </td>
                                                            <td><span class="badge bg-blue">
                                                                    <%= news.getType() %>
                                                                </span></td>
                                                            <td class="text-muted"><small>
                                                                    <%= news.getSummary() !=null ?
                                                                        (news.getSummary().length()> 20 ?
                                                                        news.getSummary().substring(0, 20) + "..." :
                                                                        news.getSummary()) : "无" %>
                                                                </small></td>
                                                            <td class="text-muted">
                                                                <%= news.getCreatedAt() %>
                                                            </td>
                                                            <td>
                                                                <% if (news.getStatus()==1) { %>
                                                                    <span class="badge bg-success">正常</span>
                                                                    <% } else { %>
                                                                        <span class="badge bg-danger">已删除</span>
                                                                        <% } %>
                                                            </td>
                                                            <td>
                                                                <div class="btn-list flex-nowrap">
                                                                    <a href="${pageContext.request.contextPath}/news?action=edit&id=<%= news.getId() %>"
                                                                        class="btn btn-white btn-sm">编辑</a>
                                                                    <% if (news.getStatus()==1) { %>
                                                                        <form
                                                                            action="${pageContext.request.contextPath}/news?action=delete&id=<%= news.getId() %>"
                                                                            method="POST" class="d-inline"
                                                                            onsubmit="return confirm('确定要删除这条新闻吗？');">
                                                                            <button type="submit"
                                                                                class="btn btn-outline-danger btn-sm">删除</button>
                                                                        </form>
                                                                        <% } %>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                        <% } } else { %>
                                                            <tr>
                                                                <td colspan="7" class="text-center text-muted">暂无新闻记录</td>
                                                            </tr>
                                                            <% } %>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <jsp:include page="../../jsp/common/layout_bottom.jsp" />