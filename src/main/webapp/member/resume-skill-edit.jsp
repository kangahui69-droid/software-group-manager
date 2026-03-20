<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="${action == 'create' ? '添加技能特长' : '编辑技能特长'}" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-tools me-2"></i>${action == 'create' ? '添加技能特长' : '编辑技能特长'}
                </h2>
                <div class="text-muted mt-1">简历: ${resume.resumeName}</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/skill?action=list&resumeId=${resume.id}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 错误消息 -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>${error}</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 验证错误列表 -->
    <c:if test="${not empty errors}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-circle-fill me-2"></i></div>
                <div>
                    <strong>请修正以下错误：</strong>
                    <ul class="mb-0 mt-2">
                        <c:forEach var="err" items="${errors}">
                            <li>${err}</li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 编辑表单 -->
    <div class="card">
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/resume/skill" method="post"
                  id="skillForm" class="needs-validation" novalidate>

                <input type="hidden" name="action" value="${action}">
                <input type="hidden" name="resumeId" value="${resume.id}">
                <c:if test="${action == 'update'}">
                    <input type="hidden" name="id" value="${skill.id}">
                </c:if>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="skillName" class="form-label">技能名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="skillName" name="skillName"
                               value="${skill.skillName}" required maxlength="100"
                               placeholder="例如：Java, MySQL, React">
                        <div class="invalid-feedback">请输入技能名称（最多100个字符）</div>
                        <div class="form-text">请输入具体的技能名称</div>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label for="category" class="form-label">技能分类</label>
                        <select class="form-select" id="category" name="category">
                            <option value="" ${empty skill.category ? 'selected' : ''}>请选择</option>
                            <option value="编程语言" ${skill.category == '编程语言' ? 'selected' : ''}>编程语言</option>
                            <option value="开发框架" ${skill.category == '开发框架' ? 'selected' : ''}>开发框架</option>
                            <option value="数据库" ${skill.category == '数据库' ? 'selected' : ''}>数据库</option>
                            <option value="工具" ${skill.category == '工具' ? 'selected' : ''}>工具</option>
                            <option value="语言" ${skill.category == '语言' ? 'selected' : ''}>语言</option>
                            <option value="其他" ${skill.category == '其他' ? 'selected' : ''}>其他</option>
                        </select>
                        <div class="form-text">选择技能所属的分类</div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="proficiency" class="form-label">熟练程度 <span class="text-danger">*</span></label>
                        <select class="form-select" id="proficiency" name="proficiency" required>
                            <option value="" ${empty skill.proficiency ? 'selected' : ''}>请选择熟练程度</option>
                            <option value="beginner" ${skill.proficiency == 'beginner' ? 'selected' : ''}>入门 (Beginner)</option>
                            <option value="elementary" ${skill.proficiency == 'elementary' ? 'selected' : ''}>初级 (Elementary)</option>
                            <option value="intermediate" ${skill.proficiency == 'intermediate' ? 'selected' : ''}>中级 (Intermediate)</option>
                            <option value="advanced" ${skill.proficiency == 'advanced' ? 'selected' : ''}>高级 (Advanced)</option>
                            <option value="expert" ${skill.proficiency == 'expert' ? 'selected' : ''}>专家 (Expert)</option>
                        </select>
                        <div class="invalid-feedback">请选择熟练程度</div>
                        <div class="form-text">
                            <small class="text-muted">
                                <strong>熟练度说明:</strong><br>
                                • 入门: 了解基本概念，能完成简单任务<br>
                                • 初级: 掌握基础知识，能独立完成部分工作<br>
                                • 中级: 熟练掌握，能解决常见问题<br>
                                • 高级: 深入理解，能解决复杂问题，指导他人<br>
                                • 专家: 精通该领域，能进行创新和改进
                            </small>
                        </div>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label for="proficiencyScore" class="form-label">熟练度分数 (可选)</label>
                        <div class="d-flex align-items-center">
                            <input type="range" class="form-range" id="proficiencyScore" name="proficiencyScore"
                                   min="1" max="100" value="${not empty skill.proficiencyScore ? skill.proficiencyScore : 50}"
                                   oninput="updateScoreValue(this.value)">
                            <span class="ms-3 badge bg-primary" id="scoreValue">
                                ${not empty skill.proficiencyScore ? skill.proficiencyScore : 50}分
                            </span>
                        </div>
                        <div class="form-text">1-100分，拖动滑块调整分数（可选）</div>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="description" class="form-label">技能描述</label>
                    <textarea class="form-control" id="description" name="description" rows="3" maxlength="500"
                              placeholder="描述您对该技能的掌握情况、应用经验、项目经验等">${skill.description}</textarea>
                    <div class="form-text">最多500个字符，描述您对该技能的具体掌握情况和应用经验</div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="displayOrder" class="form-label">显示顺序</label>
                        <input type="number" class="form-control" id="displayOrder" name="displayOrder"
                               value="${not empty skill.displayOrder ? skill.displayOrder : 0}" min="0"
                               placeholder="数字越小显示越靠前">
                        <div class="form-text">设置技能在列表中的显示顺序，数字越小显示越靠前</div>
                    </div>
                </div>

                <div class="card-footer bg-transparent border-top-0">
                    <div class="d-flex justify-content-between align-items-center">
                        <a href="${pageContext.request.contextPath}/resume/skill?action=list&resumeId=${resume.id}" class="btn btn-outline-secondary">
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
                    // 验证技能名称长度
                    var skillNameInput = document.getElementById('skillName');
                    if (skillNameInput.value.trim().length > 100) {
                        skillNameInput.setCustomValidity('技能名称不能超过100个字符');
                        skillNameInput.reportValidity();
                        event.preventDefault();
                        event.stopPropagation();
                        return;
                    } else {
                        skillNameInput.setCustomValidity('');
                    }

                    // 验证描述长度
                    var descriptionInput = document.getElementById('description');
                    if (descriptionInput.value.length > 500) {
                        descriptionInput.setCustomValidity('技能描述不能超过500个字符');
                        descriptionInput.reportValidity();
                        event.preventDefault();
                        event.stopPropagation();
                        return;
                    } else {
                        descriptionInput.setCustomValidity('');
                    }

                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false)
            })
    })()

    // 更新分数显示
    function updateScoreValue(value) {
        document.getElementById('scoreValue').textContent = value + '分';
    }

    // 页面加载时初始化
    document.addEventListener('DOMContentLoaded', function() {
        // 确保熟练度分数滑块显示正确值
        var scoreSlider = document.getElementById('proficiencyScore');
        if (scoreSlider) {
            updateScoreValue(scoreSlider.value);
        }
    });
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />