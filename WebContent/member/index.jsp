<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<% User user=(User) session.getAttribute("user"); if (user==null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp" ); return; } %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="个人中心" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    个人中心
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-md-6 col-lg-4">
                <div class="card">
                    <div class="card-body p-4 text-center">
                        <span class="avatar avatar-xl mb-3 avatar-rounded bg-blue-lt">
                            <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName().substring(0,1) : user.getUsername().substring(0,1).toUpperCase() %>
                        </span>
                        <h3 class="m-0 mb-1">
                            <%= (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getUsername() %>
                        </h3>
                        <div class="text-muted text-capitalize">
                            <%= user.getRole().toLowerCase() %>
                        </div>
                        <div class="mt-3">
                            <span class="badge bg-green-lt">正式成员</span>
                        </div>
                    </div>
                    <div class="d-flex">
                        <a href="profile.jsp" class="card-btn">查看个人资料</a>
                    </div>
                </div>
                
                <!-- 快速链接 -->
                <div class="card mt-4">
                    <div class="card-header">
                        <h3 class="card-title">快速链接</h3>
                    </div>
                    <div class="list-group list-group-flush">
                        <a href="profile.jsp" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-person-circle me-3 text-blue"></i> 个人中心
                        </a>
                        <a href="${pageContext.request.contextPath}/activity?action=myActivities" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-calendar-check me-3 text-green"></i> 我的活动
                        </a>
                        <a href="${pageContext.request.contextPath}/project?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-briefcase me-3 text-orange"></i> 我的项目
                        </a>
                        <a href="${pageContext.request.contextPath}/award?action=list" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="bi bi-award me-3 text-yellow"></i> 我的奖项
                        </a>
                        <a href="resume.jsp" class="list-group-item list-group-item-action d-flex align-items-center border-bottom-0">
                            <i class="bi bi-file-earmark-person me-3 text-purple"></i> 我的简历
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-md-6 col-lg-8">
                <div class="row row-cards">
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-blue text-white avatar"><i class="bi bi-briefcase"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium">我的项目</div>
                                        <div class="text-muted"><a href="${pageContext.request.contextPath}/project?action=list" class="text-reset">查看 & 管理申请</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-green text-white avatar"><i class="bi bi-calendar-check"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium">我的活动</div>
                                        <div class="text-muted"><a href="${pageContext.request.contextPath}/activity?action=myActivities" class="text-reset">查看我的报名活动</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-yellow text-white avatar"><i class="bi bi-award"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium">我的奖项</div>
                                        <div class="text-muted"><a href="${pageContext.request.contextPath}/award?action=list" class="text-reset">荣誉证书管理</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="card card-sm">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <span class="bg-azure text-white avatar"><i class="bi bi-file-earmark-person"></i></span>
                                    </div>
                                    <div class="col">
                                        <div class="font-weight-medium">我的简历</div>
                                        <div class="text-muted"><a href="resume.jsp" class="text-reset">在线简历维护</a></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />