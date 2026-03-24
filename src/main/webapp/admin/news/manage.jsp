<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="model.News" %>
            <%@ page import="model.User" %>
                <% User user=(User) session.getAttribute("user"); if (user==null || !"ADMIN".equals(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } %>
                    <jsp:include page="../../jsp/common/layout_top.jsp">
                        <jsp:param name="title" value="新闻管理" />
                    </jsp:include>

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
                                        <div class="col-md-2 d-flex align-items-end">
                                            <a href="${pageContext.request.contextPath}/news?action=manage" class="btn btn-outline-secondary w-100">重置</a>
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