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
                    <a href="${pageContext.request.contextPath}/ai?action=chat" class="card card-link card-body p-3">
                        <div class="mb-2">
                            <i class="bi bi-robot h2 text-info"></i>
                        </div>
                        <div class="text-truncate">AI助手</div>
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
            <div class="accordion mt-4" id="aiFaqAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header">
                        <button class="accordion-button collapsed" type="button"
                                data-bs-toggle="collapse" data-bs-target="#aiFaq">
                            <i class="bi bi-robot me-2"></i>AI助手使用指南
                        </button>
                    </h2>
                    <div id="aiFaq" class="accordion-collapse collapse"
                         data-bs-parent="#aiFaqAccordion">
                        <div class="accordion-body">
                            <div class="d-flex align-items-start mb-2">
                                <i class="bi bi-question-circle text-primary me-2 mt-1"></i>
                                <div>
                                    <strong>如何发布新闻？</strong>
                                    <p class="text-muted small mb-0">答案：进入新闻管理，点击"发布新闻"按钮，填写标题、内容、类型后保存。</p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start mb-2">
                                <i class="bi bi-question-circle text-primary me-2 mt-1"></i>
                                <div>
                                    <strong>如何审核奖项申请？</strong>
                                    <p class="text-muted small mb-0">答案：进入奖项管理，点击"待审核"，查看详情后点击"通过"或"拒绝"。</p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start mb-2">
                                <i class="bi bi-question-circle text-primary me-2 mt-1"></i>
                                <div>
                                    <strong>如何管理成员？</strong>
                                    <p class="text-muted small mb-0">答案：进入成员管理，可查看、编辑成员资料或重置密码。</p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start mb-2">
                                <i class="bi bi-question-circle text-primary me-2 mt-1"></i>
                                <div>
                                    <strong>如何创建活动？</strong>
                                    <p class="text-muted small mb-0">答案：进入活动管理，点击"创建活动"，填写时间、地点等信息后保存。</p>
                                </div>
                            </div>
                            <div class="d-flex align-items-start mb-2">
                                <i class="bi bi-question-circle text-primary me-2 mt-1"></i>
                                <div>
                                    <strong>如何处理招新报名？</strong>
                                    <p class="text-muted small mb-0">答案：进入招新管理，查看待处理申请，点击"同意"创建账号或"拒绝"驳回。</p>
                                </div>
                            </div>
                            <hr>
                            <a href="${pageContext.request.contextPath}/ai?action=chat" class="btn btn-outline-info btn-sm w-100">
                                <i class="bi bi-chat-dots me-1"></i>向AI助手提问
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="mt-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-primary w-100">
                    <i class="bi bi-house me-2"></i>返回前台首页
                </a>
            </div>
        </div>
    </div>