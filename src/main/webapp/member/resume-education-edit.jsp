<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="${action == 'create' ? '添加教育经历' : '编辑教育经历'}" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-mortarboard me-2"></i>${action == 'create' ? '添加教育经历' : '编辑教育经历'}
                </h2>
                <div class="text-muted mt-1">简历: ${resume.resumeName}</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/education?action=list&resumeId=${resume.id}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 编辑表单 -->
    <div class="card">
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/resume/education" method="post"
                  id="educationForm" class="needs-validation" novalidate>
                <input type="hidden" name="action" value="${action}">
                <input type="hidden" name="resumeId" value="${resume.id}">
                <c:if test="${action == 'update'}">
                    <input type="hidden" name="id" value="${education.id}">
                </c:if>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="schoolName" class="form-label">学校名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="schoolName" name="schoolName"
                               value="${education.schoolName}" required
                               placeholder="例如：某某大学">
                        <div class="invalid-feedback">请输入学校名称</div>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label for="major" class="form-label">专业</label>
                        <input type="text" class="form-control" id="major" name="major"
                               value="${education.major}"
                               placeholder="例如：计算机科学与技术">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="degree" class="form-label">学历/学位</label>
                        <select class="form-select" id="degree" name="degree">
                            <option value="" ${empty education.degree ? 'selected' : ''}>请选择</option>
                            <option value="高中" ${education.degree == '高中' ? 'selected' : ''}>高中</option>
                            <option value="大专" ${education.degree == '大专' ? 'selected' : ''}>大专</option>
                            <option value="本科" ${education.degree == '本科' ? 'selected' : ''}>本科</option>
                            <option value="硕士" ${education.degree == '硕士' ? 'selected' : ''}>硕士</option>
                            <option value="博士" ${education.degree == '博士' ? 'selected' : ''}>博士</option>
                        </select>
                    </div>

                    <div class="col-md-6 mb-3">
                        <div class="form-check mt-4">
                            <input class="form-check-input" type="checkbox" id="isCurrent" name="isCurrent" value="1"
                                   ${education.isCurrentStudy() ? 'checked' : ''}>
                            <label class="form-check-label" for="isCurrent">
                                目前在读
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="startDate" class="form-label">入学时间</label>
                        <input type="date" class="form-control" id="startDate" name="startDate"
                               value="${education.startDate}">
                    </div>

                    <div class="col-md-6 mb-3">
                        <label for="endDate" class="form-label">毕业时间</label>
                        <input type="date" class="form-control" id="endDate" name="endDate"
                               value="${education.endDate}"
                               ${education.isCurrentStudy() ? 'disabled' : ''}>
                        <div class="form-text" id="endDateHelp">${education.isCurrentStudy() ? '目前在读，无需填写毕业时间' : ''}</div>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="description" class="form-label">在校经历描述</label>
                    <textarea class="form-control" id="description" name="description" rows="4"
                              placeholder="描述您的在校经历、主修课程、获奖情况等">${education.description}</textarea>
                </div>

                <div class="card-footer bg-transparent border-top-0">
                    <div class="d-flex justify-content-between align-items-center">
                        <a href="${pageContext.request.contextPath}/resume/education?action=list&resumeId=${resume.id}" class="btn btn-outline-secondary">
                            取消
                        </a>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-lg me-1"></i>
                            ${action == 'create' ? '保存' : '更新'}
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

    // 处理"目前在读"复选框
    document.getElementById('isCurrent').addEventListener('change', function() {
        var endDateInput = document.getElementById('endDate');
        var endDateHelp = document.getElementById('endDateHelp');

        if (this.checked) {
            endDateInput.disabled = true;
            endDateInput.value = '';
            endDateHelp.textContent = '目前在读，无需填写毕业时间';
        } else {
            endDateInput.disabled = false;
            endDateHelp.textContent = '';
        }
    });
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
