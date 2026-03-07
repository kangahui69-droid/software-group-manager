<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <div class="offcanvas offcanvas-start" tabindex="-1" id="adminSidebar" aria-labelledby="adminSidebarLabel">
        <div class="offcanvas-header">
            <h5 class="offcanvas-title" id="adminSidebarLabel">系统管理快捷方式</h5>
            <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>
        <div class="offcanvas-body">
            <div class="row row-cards text-center">
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/admin/index.jsp" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-speedometer2 h2 text-primary"></i>
                        </div>
                        <div class="text-truncate">控制台</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/news?action=manage"
                        class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-newspaper h2 text-blue"></i>
                        </div>
                        <div class="text-truncate">新闻管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/project/list" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-kanban h2 text-purple"></i>
                        </div>
                        <div class="text-truncate">项目管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/admin/member/list" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-people h2 text-cyan"></i>
                        </div>
                        <div class="text-truncate">成员管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/activity/list" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-calendar-check h2 text-green"></i>
                        </div>
                        <div class="text-truncate">活动管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/award/list" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-trophy h2 text-yellow"></i>
                        </div>
                        <div class="text-truncate">奖项管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/admin/recruit/manage"
                        class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-person-plus-fill h2 text-teal"></i>
                        </div>
                        <div class="text-truncate">招新管理</div>
                    </a>
                </div>
                <div class="col-6 mb-3">
                    <a href="${pageContext.request.contextPath}/admin/log/list" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-journal-text h2 text-secondary"></i>
                        </div>
                        <div class="text-truncate">操作日志</div>
                    </a>
                </div>
            </div>
            <div class="mt-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-primary w-100">
                    <i class="bi bi-house me-2"></i>返回前台首页
                </a>
            </div>
        </div>
    </div>