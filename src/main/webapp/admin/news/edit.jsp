<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } News news=(News) request.getAttribute("news"); String content=(String) request.getAttribute("content"); boolean isEdit=(news !=null); %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value='<%= isEdit ? "编辑新闻" : "发布新闻" %>' />
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

    .card-footer-design {
        padding: 16px 24px;
        border-top: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
        display: flex;
        justify-content: flex-end;
        gap: 12px;
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
        cursor: pointer;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
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
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
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

    textarea.input-design {
        resize: vertical;
        min-height: 100px;
    }

    .required::after {
        content: " *";
        color: #ef4444;
    }

    #editor-wrapper {
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        overflow: hidden;
    }

    #editor-container {
        height: 350px;
    }
</style>

<link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
<script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>
<script src="${pageContext.request.contextPath}/js/editors.js?v=<%=System.currentTimeMillis()%>"></script>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-newspaper me-2"></i><%= isEdit ? "编辑新闻" : "发布新闻" %>
            </h1>
            <p class="admin-hero-subtitle"><%= isEdit ? "修改已有新闻内容" : "创建新的新闻内容" %></p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <a href="${pageContext.request.contextPath}/news?action=manage" class="btn-outline-brand">
                <i class="bi bi-arrow-left"></i>返回列表
            </a>
        </div>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-pencil-square"></i><%= isEdit ? "编辑新闻" : "发布新闻" %>
                </h3>
            </div>
            <form action="${pageContext.request.contextPath}/news" method="POST">
                <input type="hidden" name="action" value='<%= isEdit ? "update" : "create" %>'>
                <div class="card-body-design">
                    <% if (request.getAttribute("error") != null) { %>
                        <div class="alert alert-danger" style="background: #fef2f2; border: 1px solid #fee2e2; color: #991b1b; border-radius: var(--radius-standard); padding: 12px 16px; margin-bottom: 20px;">
                            <%= request.getAttribute("error") %>
                        </div>
                    <% } %>
                    <% if (isEdit) { %>
                        <input type="hidden" name="id" value="<%= news.getId() %>">
                    <% } %>
                    <div class="mb-4">
                        <label class="form-label-design required">新闻标题</label>
                        <input type="text" class="input-design" name="title" value='<%= isEdit ? news.getTitle() : "" %>' placeholder="请输入新闻标题" required>
                    </div>
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <label class="form-label-design required">新闻类型</label>
                            <select class="input-design" name="type" required>
                                <option value="notice" <%=(isEdit && "notice".equals(news.getType())) ? "selected" : "" %>>通知公告</option>
                                <option value="award" <%=(isEdit && "award".equals(news.getType())) ? "selected" : "" %>>获奖新闻</option>
                                <option value="activity" <%=(isEdit && "activity".equals(news.getType())) ? "selected" : "" %>>活动新闻</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label-design">状态</label>
                            <select class="input-design" name="status">
                                <option value="1" <%=(isEdit && news.getStatus()==1) ? "selected" : "" %>>正常</option>
                                <option value="0" <%=(isEdit && news.getStatus()==0) ? "selected" : "" %>>禁用/删除</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-4">
                        <label class="form-label-design required">摘要</label>
                        <textarea class="input-design" name="summary" rows="3" placeholder="简要描述新闻内容" required><%= isEdit ? news.getSummary() : "" %></textarea>
                    </div>
                    <div class="mb-4">
                        <label class="form-label-design required">正文内容</label>
                        <div id="editor-wrapper">
                            <div id="editor-container"></div>
                        </div>
                        <input type="hidden" id="content-data" name="content" required>
                    </div>
                </div>
                <div class="card-footer-design">
                    <a href="${pageContext.request.contextPath}/news?action=manage" class="btn-outline-brand">取消</a>
                    <button type="submit" class="btn-brand">
                        <i class="bi bi-check-lg"></i><%= isEdit ? "保存更改" : "立即发布" %>
                    </button>
                </div>
            </form>
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