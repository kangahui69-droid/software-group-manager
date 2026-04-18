<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="offcanvas offcanvas-start" tabindex="-1" id="memberSidebar" aria-labelledby="memberSidebarLabel">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="memberSidebarLabel">快捷功能</h5>
        <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body">
        <div class="row row-cards text-center">
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/member/index.jsp" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-house h2 text-primary"></i>
                    </div>
                    <div class="text-truncate">个人中心</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/study" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-book h2 text-green"></i>
                    </div>
                    <div class="text-truncate">学习中心</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/member/profile.jsp" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-person-circle h2 text-blue"></i>
                    </div>
                    <div class="text-truncate">我的资料</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/group/my-groups" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-chat-dots h2 text-info"></i>
                    </div>
                    <div class="text-truncate">我的群聊</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/award?action=list" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-trophy h2 text-yellow"></i>
                    </div>
                    <div class="text-truncate">我的奖项</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/project?action=myApplications" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-kanban h2 text-purple"></i>
                    </div>
                    <div class="text-truncate">我的项目</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/activity?action=myCreatedActivities" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-calendar-plus h2 text-orange"></i>
                    </div>
                    <div class="text-truncate">我的活动</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/member/problem" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-exclamation-triangle h2 text-danger"></i>
                    </div>
                    <div class="text-truncate">问题反馈</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/activity?action=list" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-calendar-event h2 text-cyan"></i>
                    </div>
                    <div class="text-truncate">浏览活动</div>
                </a>
            </div>
            <div class="col-6 mb-3">
                <a href="${pageContext.request.contextPath}/news?type=notice" class="card card-link card-body p-3">
                    <div class="mb-2">
                        <i class="bi bi-megaphone h2 text-pink"></i>
                    </div>
                    <div class="text-truncate">通知公告</div>
                </a>
            </div>
        </div>
        <div class="mt-4">
            <a href="${pageContext.request.contextPath}/ai?action=chat" class="btn btn-outline-primary w-100 mb-2">
                <i class="bi bi-robot me-2"></i>AI助手
            </a>
            <a href="${pageContext.request.contextPath}/member/edit-profile.jsp" class="btn btn-outline-secondary w-100 mb-2">
                <i class="bi bi-pencil me-2"></i>编辑资料
            </a>
            <a href="${pageContext.request.contextPath}/member/password-change.jsp" class="btn btn-outline-secondary w-100">
                <i class="bi bi-lock me-2"></i>修改密码
            </a>
        </div>
    </div>
</div>