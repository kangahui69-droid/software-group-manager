<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="offcanvas offcanvas-start offcanvas-sidebar" tabindex="-1" id="adminSidebar" aria-labelledby="adminSidebarLabel">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="adminSidebarLabel">
            <i class="bi bi-grid-3x3-gap me-2 text-brand"></i>管理快捷入口
        </h5>
        <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-0">
        <div class="sidebar-shortcuts">
            <div class="row g-3">
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/admin/index.jsp" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
                            <i class="bi bi-speedometer2 text-white"></i>
                        </div>
                        <div class="sidebar-card-title">控制台</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/ai?action=chat" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);">
                            <i class="bi bi-robot text-white"></i>
                        </div>
                        <div class="sidebar-card-title">AI助手</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/news?action=manage" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #06b6d4, #22d3ee);">
                            <i class="bi bi-newspaper text-white"></i>
                        </div>
                        <div class="sidebar-card-title">新闻管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/project/list" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                            <i class="bi bi-kanban text-white"></i>
                        </div>
                        <div class="sidebar-card-title">项目管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/admin/member/list" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
                            <i class="bi bi-people text-white"></i>
                        </div>
                        <div class="sidebar-card-title">成员管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/activity?action=manage" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6);">
                            <i class="bi bi-calendar-check text-white"></i>
                        </div>
                        <div class="sidebar-card-title">活动管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/award/list" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #f97316, #fb923c);">
                            <i class="bi bi-trophy text-white"></i>
                        </div>
                        <div class="sidebar-card-title">奖项管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/admin/recruit/manage" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #14b8a6, #2dd4bf);">
                            <i class="bi bi-person-plus-fill text-white"></i>
                        </div>
                        <div class="sidebar-card-title">招新管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/admin/log/list" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #64748b, #94a3b8);">
                            <i class="bi bi-journal-text text-white"></i>
                        </div>
                        <div class="sidebar-card-title">操作日志</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/attendance/manage" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #6366f1, #818cf8);">
                            <i class="bi bi-book text-white"></i>
                        </div>
                        <div class="sidebar-card-title">学习管理</div>
                    </a>
                </div>
                <div class="col-6">
                    <a href="${pageContext.request.contextPath}/admin/problem" class="sidebar-card">
                        <div class="sidebar-card-icon" style="background: linear-gradient(135deg, #ef4444, #f87171);">
                            <i class="bi bi-exclamation-triangle text-white"></i>
                        </div>
                        <div class="sidebar-card-title">问题反馈</div>
                    </a>
                </div>
            </div>
        </div>

        <div class="sidebar-faq">
            <div class="accordion accordion-design" id="aiFaqAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#aiFaq">
                            <i class="bi bi-robot me-2"></i>AI助手使用指南
                        </button>
                    </h2>
                    <div id="aiFaq" class="accordion-collapse collapse" data-bs-parent="#aiFaqAccordion">
                        <div class="accordion-body">
                            <div class="mb-3 pb-3 border-bottom">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-question-circle text-brand me-2 mt-1"></i>
                                    <div>
                                        <strong class="d-block mb-1">如何发布新闻？</strong>
                                        <span class="text-muted">进入新闻管理，点击"发布新闻"按钮，填写标题、内容、类型后保存。</span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3 pb-3 border-bottom">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-question-circle text-brand me-2 mt-1"></i>
                                    <div>
                                        <strong class="d-block mb-1">如何审核奖项申请？</strong>
                                        <span class="text-muted">进入奖项管理，点击"待审核"，查看详情后点击"通过"或"拒绝"。</span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3 pb-3 border-bottom">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-question-circle text-brand me-2 mt-1"></i>
                                    <div>
                                        <strong class="d-block mb-1">如何管理成员？</strong>
                                        <span class="text-muted">进入成员管理，可查看、编辑成员资料或重置密码。</span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3 pb-3 border-bottom">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-question-circle text-brand me-2 mt-1"></i>
                                    <div>
                                        <strong class="d-block mb-1">如何创建活动？</strong>
                                        <span class="text-muted">进入活动管理，点击"创建活动"，填写时间、地点等信息后保存。</span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="d-flex align-items-start">
                                    <i class="bi bi-question-circle text-brand me-2 mt-1"></i>
                                    <div>
                                        <strong class="d-block mb-1">如何处理招新报名？</strong>
                                        <span class="text-muted">进入招新管理，查看待处理申请，点击"同意"创建账号或"拒绝"驳回。</span>
                                    </div>
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/ai?action=chat" class="btn btn-brand w-100 mt-2">
                                <i class="bi bi-chat-dots me-2"></i>向AI助手提问
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="p-3" style="border-top: 1px solid var(--border-light);">
            <a href="${pageContext.request.contextPath}/index.jsp" class="btn-back-home">
                <i class="bi bi-house"></i>返回前台首页
            </a>
        </div>
    </div>
</div>