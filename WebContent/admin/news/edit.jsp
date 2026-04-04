<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="model.News" %>
        <%@ page import="model.User" %>
            <% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } News news=(News)
                request.getAttribute("news"); String content=(String) request.getAttribute("content"); boolean
                isEdit=(news !=null); %>
                <jsp:include page="../../jsp/common/layout_top.jsp">
                    <jsp:param name="title" value="<%= isEdit ? \" 编辑新闻\" : \"发布新闻\" %>" />
                </jsp:include>

                <!-- Quill.js Core -->
                <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
                <script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>

                <!-- Editor Initialization Logic -->
                <script
                    src="${pageContext.request.contextPath}/js/editors.js?v=<%=System.currentTimeMillis()%>"></script>

                <div class="page-header d-print-none">
                    <div class="container-xl">
                        <div class="row g-2 align-items-center">
                            <div class="col">
                                <h2 class="page-title">
                                    <%= isEdit ? "编辑新闻" : "发布新闻" %>
                                </h2>
                            </div>
                            <div class="col-auto ms-auto">
                                <a href="${pageContext.request.contextPath}/news?action=manage" class="btn btn-outline-secondary">
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
                                                                        <option value="notice" <%=(isEdit && "notice"
                                                                            .equals(news.getType())) ? "selected" : ""
                                                                            %>>通知公告</option>
                                                                        <option value="award" <%=(isEdit && "award"
                                                                            .equals(news.getType())) ? "selected" : ""
                                                                            %>>获奖新闻</option>
                                                                        <option value="activity" <%=(isEdit
                                                                            && "activity" .equals(news.getType()))
                                                                            ? "selected" : "" %>>活动新闻</option>
                                                                    </select>
                                                                </div>
                                                            </div>
                                                            <div class="col-md-6">
                                                                <div class="mb-3">
                                                                    <label class="form-label">发布状态</label>
                                                                    <select class="form-select" name="status">
                                                                        <option value="1" <%=(isEdit &&
                                                                            news.getStatus()==1) ? "selected" : "" %>>已发布
                                                                        </option>
                                                                        <option value="0" <%=(isEdit &&
                                                                            news.getStatus()==0) ? "selected" : "" %>
                                                                            >未发布</option>
                                                                    </select>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label required">摘要</label>
                                                            <textarea class="form-control" name="summary" rows="3"
                                                                placeholder="简要描述新闻内容"
                                                                required><%= isEdit ? news.getSummary() : "" %></textarea>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label required">正文内容</label>
                                                            <div id="editor-wrapper" style="height: 400px;">
                                                                <div id="editor-container"></div>
                                                            </div>
                                                            <input type="hidden" id="content-data" name="content"
                                                                required>
                                                        </div>
                                    </div>
                                    <div class="card-footer text-end">
                                        <div class="d-flex">
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

                <!-- Quill Initialization -->
                <script>
                    let initialContent = "";
                <%if (isEdit && content != null && !content.isEmpty()) {
                    // Escape content for JavaScript
                    String escapedContent = content.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "")
                            .replace("`", "\\`");
                %>
                            initialContent = "<%= escapedContent %>";
                <% } %>

                        // Initialize the editor with the data
                        document.addEventListener('DOMContentLoaded', function () {
                            initQuillEditor(initialContent);
                        });
                </script>

                <jsp:include page="../../jsp/common/layout_bottom.jsp" />