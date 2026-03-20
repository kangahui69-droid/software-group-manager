<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="${empty project ? '添加项目经历' : '编辑项目经历'}" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-kanban me-2"></i>
                    ${empty project ? '添加项目经历' : '编辑项目经历'}
                </h2>
                <div class="text-muted mt-1">
                    ${empty project ? '添加新的项目经历，展示您的实战能力' : '修改项目经历信息'}
                </div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/project?action=list&resumeId=${resumeId}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 编辑表单 -->
    <div class="card">
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/resume/project" method="post" id="projectForm" class="needs-validation" novalidate>
                <input type="hidden" name="action" value="${empty project ? 'create' : 'update'}">
                <input type="hidden" name="resumeId" value="${resumeId}">
                <c:if test="${not empty project}">
                    <input type="hidden" name="id" value="${project.id}">
                </c:if>

                <div class="row">
                    <!-- 项目名称 -->
                    <div class="col-md-8 mb-3">
                        <label for="projectName" class="form-label">项目名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="projectName" name="projectName"
                               value="${project.projectName}" required
                               placeholder="例如：电商平台开发、移动APP设计">
                        <div class="invalid-feedback">请输入项目名称</div>
                    </div>

                    <!-- 担任角色 -->
                    <div class="col-md-4 mb-3">
                        <label for="role" class="form-label">担任角色 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="role" name="role"
                               value="${project.role}" required
                               placeholder="例如：项目负责人、后端开发">
                        <div class="invalid-feedback">请输入担任角色</div>
                    </div>
                </div>

                <div class="row">
                    <!-- 团队规模 -->
                    <div class="col-md-4 mb-3">
                        <label for="teamSize" class="form-label">团队规模</label>
                        <input type="number" class="form-control" id="teamSize" name="teamSize"
                               value="${project.teamSize}" min="1"
                               placeholder="例如：5">
                    </div>

                    <!-- 开始时间 -->
                    <div class="col-md-4 mb-3">
                        <label for="startDate" class="form-label">开始时间 <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="startDate" name="startDate"
                               value="${project.startDate}" required>
                        <div class="invalid-feedback">请选择开始时间</div>
                    </div>

                    <!-- 结束时间 -->
                    <div class="col-md-4 mb-3">
                        <label for="endDate" class="form-label">结束时间</label>
                        <input type="date" class="form-control" id="endDate" name="endDate"
                               value="${project.endDate}">
                        <div class="form-check mt-2">
                            <input class="form-check-input" type="checkbox" id="isCurrent" name="isCurrent" value="1"
                                   ${project.isCurrent == 1 ? 'checked' : ''}>
                            <label class="form-check-label" for="isCurrent">
                                项目进行中
                            </label>
                        </div>
                    </div>
                </div>

                <!-- 使用技术 -->
                <div class="mb-3">
                    <label for="technologies" class="form-label">使用技术</label>
                    <input type="text" class="form-control" id="technologies" name="technologies"
                           value="${project.technologies}"
                           placeholder="例如：Java, Spring Boot, MySQL, Redis（使用逗号分隔）">
                </div>

                <!-- 项目描述 -->
                <div class="mb-3">
                    <label for="description" class="form-label">项目描述</label>
                    <textarea class="form-control" id="description" name="description" rows="4"
                              placeholder="描述项目背景、目标、规模等信息">${project.description}</textarea>
                </div>

                <!-- 个人职责 -->
                <div class="mb-3">
                    <label for="responsibilities" class="form-label">个人职责</label>
                    <textarea class="form-control" id="responsibilities" name="responsibilities" rows="4"
                              placeholder="描述您在项目中承担的具体工作和职责">${project.responsibilities}</textarea>
                </div>

                <!-- 项目成果 -->
                <div class="mb-3">
                    <label for="achievements" class="form-label">项目成果</label>
                    <textarea class="form-control" id="achievements" name="achievements" rows="3"
                              placeholder="描述项目取得的成果、数据指标、获得的认可等">${project.achievements}</textarea>
                </div>

                <!-- 项目链接 -->
                <div class="mb-3">
                    <label for="projectUrl" class="form-label">项目链接</label>
                    <input type="url" class="form-control" id="projectUrl" name="projectUrl"
                           value="${project.projectUrl}"
                           placeholder="例如：https://github.com/username/project">
                </div>

                <!-- 表单按钮 -->
                <div class="d-flex justify-content-between mt-4">
                    <a href="${pageContext.request.contextPath}/resume/project?action=list&resumeId=${resumeId}" class="btn btn-outline-secondary">
                        取消
                    </a>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-lg me-1"></i>
                        ${empty project ? '保存项目经历' : '更新项目经历'}
                    </button>
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

    // 进行中复选框控制结束日期
    document.getElementById('isCurrent').addEventListener('change', function() {
        var endDateInput = document.getElementById('endDate');
        if (this.checked) {
            endDateInput.value = '';
            endDateInput.disabled = true;
        } else {
            endDateInput.disabled = false;
        }
    });

    // 页面加载时检查进行中状态
    window.addEventListener('load', function() {
        var isCurrent = document.getElementById('isCurrent');
        if (isCurrent && isCurrent.checked) {
            document.getElementById('endDate').disabled = true;
        }
    });
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
