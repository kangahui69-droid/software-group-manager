<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="${empty resume ? '创建简历' : '编辑简历'}" />
</jsp:include>
<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    // 获取转发信息（当 Servlet forward 到 JSP 时会设置这些属性）
    String forwardRequestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
    String forwardServletPath = (String) request.getAttribute("javax.servlet.forward.servlet_path");

    // 如果没有转发信息，说明是直接访问 JSP
    if (forwardRequestUri == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=list");
        return;
    }
%>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-file-earmark-person me-2"></i>
                    ${empty resume ? '创建简历' : '编辑简历'}
                </h2>
                <div class="text-muted mt-1">
                    ${empty resume ? '填写基本信息创建您的第一份简历' : '完善您的简历信息'}
                </div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume?action=list" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 消息提示 -->
    <c:if test="${param.success == 'created'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历创建成功！请继续完善详细信息。</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 编辑表单 -->
    <div class="card">
        <div class="card-header">
            <ul class="nav nav-tabs card-header-tabs" id="resumeTab" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="basic-tab" data-bs-toggle="tab" data-bs-target="#basic"
                            type="button" role="tab" aria-controls="basic" aria-selected="true">
                        <i class="bi bi-person me-1"></i>基本信息
                    </button>
                </li>
                <c:if test="${not empty resume}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="education-tab" data-bs-toggle="tab" data-bs-target="#education"
                                type="button" role="tab" aria-controls="education" aria-selected="false">
                            <i class="bi bi-mortarboard me-1"></i>教育经历
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="skill-tab" data-bs-toggle="tab" data-bs-target="#skill"
                                type="button" role="tab" aria-controls="skill" aria-selected="false">
                            <i class="bi bi-tools me-1"></i>技能特长
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="project-tab" data-bs-toggle="tab" data-bs-target="#project"
                                type="button" role="tab" aria-controls="project" aria-selected="false">
                            <i class="bi bi-kanban me-1"></i>项目经历
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="award-tab" data-bs-toggle="tab" data-bs-target="#award"
                                type="button" role="tab" aria-controls="award" aria-selected="false">
                            <i class="bi bi-trophy me-1"></i>获奖情况
                        </button>
                    </li>
                </c:if>
            </ul>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/resume" method="post"
                  id="resumeForm" class="needs-validation" novalidate>
                <input type="hidden" name="action" value="${empty resume ? 'create' : 'update'}">
                <c:if test="${not empty resume}">
                    <input type="hidden" name="id" value="${resume.id}">
                </c:if>

                <div class="tab-content" id="resumeTabContent">
                    <!-- 基本信息 -->
                    <div class="tab-pane fade show active" id="basic" role="tabpanel" aria-labelledby="basic-tab">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="resumeName" class="form-label">简历名称 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="resumeName" name="resumeName"
                                       value="${resume.resumeName}" required
                                       placeholder="例如：我的简历、Java开发工程师简历">
                                <div class="invalid-feedback">请输入简历名称</div>
                            </div>

                            <div class="col-md-6 mb-3">
                                <label for="templateStyle" class="form-label">模板风格</label>
                                <select class="form-select" id="templateStyle" name="templateStyle">
                                    <option value="default" ${resume.templateStyle == 'default' ? 'selected' : ''}>默认风格</option>
                                    <option value="minimal" ${resume.templateStyle == 'minimal' ? 'selected' : ''}>简约风格</option>
                                    <option value="academic" ${resume.templateStyle == 'academic' ? 'selected' : ''}>学术风格</option>
                                    <option value="creative" ${resume.templateStyle == 'creative' ? 'selected' : ''}>创意风格</option>
                                </select>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="phone" class="form-label">联系电话</label>
                                <input type="tel" class="form-control" id="phone" name="phone"
                                       value="${resume.phone}" placeholder="您的手机号码">
                            </div>

                            <div class="col-md-6 mb-3">
                                <label for="email" class="form-label">电子邮箱</label>
                                <input type="email" class="form-control" id="email" name="email"
                                       value="${resume.email}" placeholder="您的邮箱地址">
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="wechat" class="form-label">微信号</label>
                                <input type="text" class="form-control" id="wechat" name="wechat"
                                       value="${resume.wechat}" placeholder="您的微信号">
                            </div>

                            <div class="col-md-6 mb-3">
                                <label for="githubUrl" class="form-label">GitHub地址</label>
                                <input type="url" class="form-control" id="githubUrl" name="githubUrl"
                                       value="${resume.githubUrl}" placeholder="https://github.com/username">
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="blogUrl" class="form-label">技术博客</label>
                            <input type="url" class="form-control" id="blogUrl" name="blogUrl"
                                   value="${resume.blogUrl}" placeholder="您的技术博客地址">
                        </div>

                        <div class="mb-3">
                            <label for="careerObjective" class="form-label">求职意向</label>
                            <input type="text" class="form-control" id="careerObjective" name="careerObjective"
                                   value="${resume.careerObjective}" placeholder="例如：Java开发工程师、前端工程师">
                        </div>

                        <div class="mb-3">
                            <label for="summary" class="form-label">个人简介</label>
                            <textarea class="form-control" id="summary" name="summary" rows="4"
                                      placeholder="简要介绍您的专业背景、核心技能和职业目标">${resume.summary}</textarea>
                        </div>

                        <c:if test="${not empty resume}">
                            <div class="mb-3">
                                <label class="form-label">简历状态</label>
                                <div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="status" id="status0" value="0" ${resume.status == 0 ? 'checked' : ''}>
                                        <label class="form-check-label" for="status0">草稿</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="status" id="status1" value="1" ${resume.status == 1 ? 'checked' : ''}>
                                        <label class="form-check-label" for="status1">已发布</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="status" id="status2" value="2" ${resume.status == 2 ? 'checked' : ''}>
                                        <label class="form-check-label" for="status2">已隐藏</label>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <c:if test="${not empty resume}">
                        <!-- 教育经历Tab -->
                        <div class="tab-pane fade" id="education" role="tabpanel" aria-labelledby="education-tab">
                            <div class="text-center py-5">
                                <i class="bi bi-mortarboard display-4 text-primary"></i>
                                <h4 class="mt-3 mb-3">教育经历管理</h4>
                                <p class="text-muted mb-4">添加您的学校、专业、学历等教育背景信息</p>
                                <a href="${pageContext.request.contextPath}/resume/education?action=list&resumeId=${resume.id}"
                                   class="btn btn-primary btn-lg">
                                    <i class="bi bi-mortarboard me-2"></i>管理教育经历
                                </a>
                            </div>
                        </div>

                        <!-- 技能特长Tab -->
                        <div class="tab-pane fade" id="skill" role="tabpanel" aria-labelledby="skill-tab">
                            <div class="text-center py-5">
                                <i class="bi bi-tools display-4 text-success"></i>
                                <h4 class="mt-3 mb-3">技能特长管理</h4>
                                <p class="text-muted mb-4">添加您的专业技能、语言能力等特长信息</p>
                                <a href="${pageContext.request.contextPath}/resume/skill?action=list&resumeId=${resume.id}"
                                   class="btn btn-success btn-lg">
                                    <i class="bi bi-tools me-2"></i>管理技能特长
                                </a>
                            </div>
                        </div>

                        <!-- 项目经历Tab -->
                        <div class="tab-pane fade" id="project" role="tabpanel" aria-labelledby="project-tab">
                            <div class="text-center py-5">
                                <i class="bi bi-kanban display-4 text-primary"></i>
                                <h4 class="mt-3 mb-3">项目经历管理</h4>
                                <p class="text-muted mb-4">添加和管理您的项目经历，展示您的实战能力</p>
                                <a href="${pageContext.request.contextPath}/resume/project?action=list&resumeId=${resume.id}"
                                   class="btn btn-primary btn-lg">
                                    <i class="bi bi-kanban me-2"></i>管理项目经历
                                </a>
                            </div>
                        </div>

                        <!-- 获奖情况Tab -->
                        <div class="tab-pane fade" id="award" role="tabpanel" aria-labelledby="award-tab">
                            <div class="text-center py-5">
                                <i class="bi bi-trophy display-4 text-warning"></i>
                                <h4 class="mt-3 mb-3">获奖情况管理</h4>
                                <p class="text-muted mb-4">添加和管理您的获奖记录，展示您的荣誉成就</p>
                                <a href="${pageContext.request.contextPath}/resume/award?action=list&resumeId=${resume.id}"
                                   class="btn btn-warning btn-lg">
                                    <i class="bi bi-trophy me-2"></i>管理获奖情况
                                </a>
                            </div>
                        </div>
                    </c:if>
                </div>

                <!-- 表单提交按钮 -->
                <div class="card-footer bg-transparent border-top-0">
                    <div class="d-flex justify-content-between align-items-center">
                        <a href="${pageContext.request.contextPath}/resume?action=list" class="btn btn-outline-secondary">
                            取消
                        </a>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-lg me-1"></i>
                            ${empty resume ? '创建简历' : '保存修改'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // 表单验证
    (function () {
        'use strict'
        var forms = document.querySelectorAll('.needs-validation')
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                    form.classList.add('was-validated')
                }, false)
            })
    })()
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
