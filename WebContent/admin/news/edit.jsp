<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.News" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) { response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } News news=(News) request.getAttribute("news"); String content=(String) request.getAttribute("content"); boolean isEdit=(news !=null); %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="<%= isEdit ? \" 编辑新闻\" : \"发布新闻\" %>" />
</jsp:include>

<link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
<script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>
<script src="${pageContext.request.contextPath}/js/editors.js?v=<%=System.currentTimeMillis()%>"></script>

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

    .btn-back {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        font-size: 0.88rem;
        border: 1px solid rgba(255, 255, 255, 0.3);
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-back:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }

    .form-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
    }

    .form-card-header {
        padding: 24px 32px;
        border-bottom: 1px solid var(--border-light);
    }

    .form-card-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .form-card-body {
        padding: 32px;
    }

    .form-card-footer {
        padding: 24px 32px;
        border-top: 1px solid var(--border-light);
        display: flex;
        justify-content: space-between;
        align-items: center;
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
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
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
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        transition: all 0.3s ease;
        width: 100%;
        background: white;
    }

    .select-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    textarea.input-design {
        min-height: 100px;
        resize: vertical;
    }

    .editor-wrapper {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        overflow: hidden;
    }

    .editor-wrapper:focus-within {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    }

    #editor-container {
        height: 400px;
    }

    .btn-primary-design {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 600;
        font-size: 0.94rem;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-primary-design:hover {
        background: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-cancel {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 500;
        font-size: 0.94rem;
        border: 1px solid var(--border-gray);
        transition: all 0.3s ease;
        text-decoration: none;
    }

    .btn-cancel:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }

    .alert-design {
        border-radius: var(--radius-standard);
        padding: 16px 20px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .alert-danger-design {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
        color: #dc2626;
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

        .form-card-body {
            padding: 24px;
        }

        .form-card-footer {
            flex-direction: column;
            gap: 12px;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="page-hero">
            <div>
                <h1 class="page-hero-title">
                    <i class="bi bi-pencil-square me-2"></i><%= isEdit ? "编辑新闻" : "发布新闻" %>
                </h1>
                <p class="page-hero-subtitle"><%= isEdit ? "修改新闻内容" : "创建新的新闻内容" %></p>
            </div>
            <a href="${pageContext.request.contextPath}/news?action=manage" class="btn-back">
                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                返回列表
            </a>
        </div>

        <div class="form-card">
            <div class="form-card-header">
                <h3 class="form-card-title">
                    <i class="bi bi-file-text text-brand"></i><%= isEdit ? "编辑新闻" : "发布新闻" %>
                </h3>
            </div>
            <div class="form-card-body">
                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert-design alert-danger-design" role="alert">
                        <i class="bi bi-exclamation-circle-fill"></i>
                        <span><%= request.getAttribute("error") %></span>
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/news" method="POST" class="row g-4">
                    <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">
                    <% if (isEdit) { %>
                        <input type="hidden" name="id" value="<%= news.getId() %>">
                    <% } %>

                    <div class="col-12">
                        <label class="form-label-design">新闻标题 <span style="color: #ef4444;">*</span></label>
                        <input type="text" class="input-design" name="title" value="<%= isEdit ? news.getTitle() : "" %>" placeholder="请输入新闻标题" required>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label-design">新闻类型 <span style="color: #ef4444;">*</span></label>
                        <select class="select-design" name="type" required>
                            <option value="notice" <%= (isEdit && "notice".equals(news.getType())) ? "selected" : "" %>>通知公告</option>
                            <option value="award" <%= (isEdit && "award".equals(news.getType())) ? "selected" : "" %>>获奖新闻</option>
                            <option value="activity" <%= (isEdit && "activity".equals(news.getType())) ? "selected" : "" %>>活动新闻</option>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label-design">状态</label>
                        <select class="select-design" name="status">
                            <option value="1" <%= (isEdit && news.getStatus() == 1) ? "selected" : "" %>>正常</option>
                            <option value="0" <%= (isEdit && news.getStatus() == 0) ? "selected" : "" %>>禁用/删除</option>
                        </select>
                    </div>

                    <div class="col-12">
                        <label class="form-label-design">摘要 <span style="color: #ef4444;">*</span></label>
                        <textarea class="input-design" name="summary" rows="3" placeholder="简要描述新闻内容" required><%= isEdit ? news.getSummary() : "" %></textarea>
                    </div>

                    <div class="col-12">
                        <label class="form-label-design">正文内容 <span style="color: #ef4444;">*</span></label>
                        <div id="editor-wrapper" class="editor-wrapper">
                            <div id="editor-container"></div>
                        </div>
                        <input type="hidden" id="content-data" name="content" required>
                    </div>

                    <div class="col-12">
                        <div class="d-flex justify-content-end gap-3">
                            <a href="${pageContext.request.contextPath}/news?action=manage" class="btn-cancel">取消</a>
                            <button type="submit" class="btn-primary-design">
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