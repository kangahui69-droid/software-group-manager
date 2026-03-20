<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%
    String contextPath = request.getContextPath();
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    // 从request获取统计数据
    Integer newsCount = (Integer) request.getAttribute("newsCount");
    Integer projectCount = (Integer) request.getAttribute("projectCount");
    Integer userCount = (Integer) request.getAttribute("userCount");
    Integer logCount = (Integer) request.getAttribute("logCount");
    if (newsCount == null) newsCount = 0;
    if (projectCount == null) projectCount = 0;
    if (userCount == null) userCount = 0;
    if (logCount == null) logCount = 0;
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="管理中心" />
</jsp:include>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/news?action=manage" class="card card-link">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="subheader">新闻管理</div>
                            <div class="ms-auto lh-1">
                                <span class="badge bg-blue text-blue-fg"><%= newsCount %></span>
                            </div>
                        </div>
                        <div class="mt-4 d-flex align-items-center">
                            <div class="me-3">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg text-blue" width="48" height="48" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                                    <path d="M19 4a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V6a2 2 0 0 1 2 -2h14z" />
                                    <line x1="16" y1="10" x2="8" y2="10" />
                                    <line x1="16" y1="14" x2="8" y2="14" />
                                    <line x1="10" y1="18" x2="14" y2="18" />
                                </svg>
                            </div>
                            <div>
                                <h3 class="h2">新闻</h3>
                                <p class="text-muted">发布和管理新闻通知</p>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/project/list" class="card card-link">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="subheader">项目管理</div>
                            <div class="ms-auto lh-1">
                                <span class="badge bg-green text-green-fg"><%= projectCount %></span>
                            </div>
                        </div>
                        <div class="mt-4 d-flex align-items-center">
                            <div class="me-3">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg text-green" width="48" height="48" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                                    <path d="M14.5 2H6a2 2 0 0 0 -2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2 -2V7.5L14.5 2z" />
                                    <polyline points="14 2 14 8 20 8" />
                                    <line x1="16" y1="13" x2="8" y2="13" />
                                    <line x1="16" y1="17" x2="8" y2="17" />
                                    <polyline points="10 9 9 9 8 9" />
                                </svg>
                            </div>
                            <div>
                                <h3 class="h2">项目</h3>
                                <p class="text-muted">审批和管理项目申请</p>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/admin/member/list" class="card card-link">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="subheader">成员管理</div>
                            <div class="ms-auto lh-1">
                                <span class="badge bg-cyan text-cyan-fg"><%= userCount %></span>
                            </div>
                        </div>
                        <div class="mt-4 d-flex align-items-center">
                            <div class="me-3">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg text-cyan" width="48" height="48" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                                    <path d="M12 2a5 5 0 1 0 10 0a5 5 0 0 0 -10 0" />
                                    <path d="M12 14a7 7 0 0 0 -7 7h14a7 7 0 0 0 -7 -7" />
                                    <line x1="12" y1="8" x2="12" y2="12" />
                                    <line x1="12" y1="16" x2="12.01" y2="16" />
                                </svg>
                            </div>
                            <div>
                                <h3 class="h2">成员</h3>
                                <p class="text-muted">查看和管理成员信息</p>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
            <div class="col-sm-6 col-lg-3">
                <a href="<%= contextPath %>/admin/log/list" class="card card-link">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="subheader">操作日志</div>
                            <div class="ms-auto lh-1">
                                <span class="badge bg-yellow text-yellow-fg"><%= logCount %></span>
                            </div>
                        </div>
                        <div class="mt-4 d-flex align-items-center">
                            <div class="me-3">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg text-yellow" width="48" height="48" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                                    <circle cx="12" cy="12" r="9" />
                                    <polyline points="12 6 12 12 16 14" />
                                </svg>
                            </div>
                            <div>
                                <h3 class="h2">日志</h3>
                                <p class="text-muted">查看系统重要操作记录</p>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
