<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } News news=(News) request.getAttribute("news"); String content=(String) request.getAttribute("content"); boolean isEdit=(news !=null); %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="<%= isEdit ? " 编辑新闻" : "发布新闻" %>" />
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
.card-header {
    background: transparent;
    border-bottom: 1px solid var(--border-light);
    padding: 20px 24px;
}
.card-body {
    padding: 24px;
}
.card-footer {
    background: var(--primary-light);
    border-top: 1px solid var(--border-light);
    padding: 16px 24px;
}
.card-title {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
}
.form-label {
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 8px;
}
.form-control, .form-select {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 12px 16px;
    font-size: 14px;
    transition: all 0.2s ease;
}
.form-control:focus, .form-select:focus {
    border-color: var(--brand-blue);
    box-shadow: 0 0 0 3px rgba(20, 85, 240, 0.1);
}
textarea.form-control {
    resize: vertical;
    min-height: 100px;
}
.btn-primary {
    background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 12px 24px;
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
.btn-link {
    color: var(--text-muted);
    text-decoration: none;
    font-weight: 500;
    padding: 12px 16px;
    border-radius: var(--radius-comfortable);
    transition: all 0.2s ease;
}
.btn-link:hover {
    color: var(--brand-blue);
    background: var(--primary-light);
}
.alert-danger {
    background: #fef2f2;
    border: 1px solid #fee2e2;
    color: #991b1b;
    border-radius: var(--radius-comfortable);
    padding: 12px 16px;
}
#editor-wrapper {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    overflow: hidden;
}
#editor-container {
    height: 350px;
}
</style>

<link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
<script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>
<script src="${pageContext.request.contextPath}/js/editors.js?v=<%=System.currentTimeMillis()%>"></script>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <%= isEdit ? "编辑新闻" : "发布新闻" %>
                </h2>
            </div>
            <div class="col-auto ms-auto">
                <a href="${pageContext.request.contextPath}/news?action=manage" class="btn btn-link">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回列表
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/news" method="POST" class="card">
                    <input type="hidden" name="action" value="<%= isEdit ? " update" : "create" %>">
                    <div class="card-header">
                        <h3 class="card-title">
                            <%= isEdit ? "编辑新闻" : "发布新闻" %>
                        </h3>
                    </div>
                    <div class="card-body">
                        <% if (request.getAttribute("error") !=null) { %>
                            <div class="alert alert-danger">
                                <%= request.getAttribute("error") %>
                            </div>
                            <% } %>
                                <% if (isEdit) { %>
                                    <input type="hidden" name="id" value="<%= news.getId() %>">
                                    <% } %>
                                        <div class="mb-3">
                                            <label class="form-label required">新闻标题</label>
                                            <input type="text" class="form-control" name="title"
                                                value="<%= isEdit ? news.getTitle() : "" %>"
                                                placeholder="请输入新闻标题" required>
                                        </div>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <div class="mb-3">
                                                    <label class="form-label required">新闻类型</label>
                                                    <select class="form-select" name="type" required>
                                                        <option value="notice" <%=(isEdit && "notice" .equals(news.getType())) ? "selected" : "" %>>通知公告</option>
                                                        <option value="award" <%=(isEdit && "award" .equals(news.getType())) ? "selected" : "" %>>获奖新闻</option>
                                                        <option value="activity" <%=(isEdit && "activity" .equals(news.getType())) ? "selected" : "" %>>活动新闻</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="mb-3">
                                                    <label class="form-label">状态</label>
                                                    <select class="form-select" name="status">
                                                        <option value="1" <%=(isEdit && news.getStatus()==1) ? "selected" : "" %>>正常</option>
                                                        <option value="0" <%=(isEdit && news.getStatus()==0) ? "selected" : "" %>>禁用/删除</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label required">摘要</label>
                                            <textarea class="form-control" name="summary" rows="3"
                                                placeholder="简要描述新闻内容" required><%= isEdit ? news.getSummary() : "" %></textarea>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label required">正文内容</label>
                                            <div id="editor-wrapper" style="height: 400px;">
                                                <div id="editor-container"></div>
                                            </div>
                                            <input type="hidden" id="content-data" name="content" required>
                                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex justify-content-end">
                            <a href="${pageContext.request.contextPath}/news?action=manage"
                                class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">
                                <%= isEdit ? "保存更改" : "立即发布" %>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
let initialContent = "";
<%if (isEdit && content != null && !content.isEmpty()) {
    String escapedContent = content.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("`", "\\`");
%>
    initialContent = "<%= escapedContent %>";
<% } %>

document.addEventListener('DOMContentLoaded', function () {
    initQuillEditor(initialContent);
});
</script>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />