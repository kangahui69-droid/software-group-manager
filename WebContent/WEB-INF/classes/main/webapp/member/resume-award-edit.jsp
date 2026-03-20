<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="${empty award ? '添加获奖记录' : '编辑获奖记录'}" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-trophy me-2"></i>
                    ${empty award ? '添加获奖记录' : '编辑获奖记录'}
                </h2>
                <div class="text-muted mt-1">
                    ${empty award ? '添加新的获奖记录，展示您的荣誉和成就' : '修改获奖记录信息'}
                </div>
                <div class="mt-2">
                    <span class="badge bg-info">
                        <i class="bi bi-file-earmark-person me-1"></i>
                        当前简历：${resume.resumeName}
                    </span>
                </div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/award?action=list&resumeId=${resumeId}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 编辑表单 -->
    <div class="card">
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/resume/award" method="post" id="awardForm" class="needs-validation" novalidate>
                <input type="hidden" name="action" value="${empty award ? 'create' : 'update'}">
                <input type="hidden" name="resumeId" value="${resumeId}">
                <c:if test="${not empty award}">
                    <input type="hidden" name="id" value="${award.id}">
                </c:if>
                <div class="row">
                    <!-- 奖项名称 -->
                    <div class="col-md-8 mb-3">
                        <label for="awardName" class="form-label">奖项名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="awardName" name="awardName"
                               value="${award.awardName}" required
                               placeholder="例如：全国大学生数学建模竞赛一等奖">
                        <div class="invalid-feedback">请输入奖项名称</div>
                    </div>

                    <!-- 奖项等级 -->
                    <div class="col-md-4 mb-3">
                        <label for="awardLevel" class="form-label">奖项等级</label>
                        <select class="form-select" id="awardLevel" name="awardLevel">
                            <option value="">-- 请选择 --</option>
                            <optgroup label="国家级">
                                <option value="国家级一等奖" ${award.awardLevel == '国家级一等奖' ? 'selected' : ''}>国家级一等奖</option>
                                <option value="国家级二等奖" ${award.awardLevel == '国家级二等奖' ? 'selected' : ''}>国家级二等奖</option>
                                <option value="国家级三等奖" ${award.awardLevel == '国家级三等奖' ? 'selected' : ''}>国家级三等奖</option>
                            </optgroup>
                            <optgroup label="省级">
                                <option value="省级一等奖" ${award.awardLevel == '省级一等奖' ? 'selected' : ''}>省级一等奖</option>
                                <option value="省级二等奖" ${award.awardLevel == '省级二等奖' ? 'selected' : ''}>省级二等奖</option>
                                <option value="省级三等奖" ${award.awardLevel == '省级三等奖' ? 'selected' : ''}>省级三等奖</option>
                            </optgroup>
                            <optgroup label="校级">
                                <option value="校级一等奖" ${award.awardLevel == '校级一等奖' ? 'selected' : ''}>校级一等奖</option>
                                <option value="校级二等奖" ${award.awardLevel == '校级二等奖' ? 'selected' : ''}>校级二等奖</option>
                                <option value="校级三等奖" ${award.awardLevel == '校级三等奖' ? 'selected' : ''}>校级三等奖</option>
                            </optgroup>
                            <optgroup label="其他">
                                <option value="特等奖" ${award.awardLevel == '特等奖' ? 'selected' : ''}>特等奖</option>
                                <option value="优秀奖" ${award.awardLevel == '优秀奖' ? 'selected' : ''}>优秀奖</option>
                                <option value="其他" ${award.awardLevel == '其他' ? 'selected' : ''}>其他</option>
                            </optgroup>
                        </select>
                    </div>
                </div>

                <div class="row">
                    <!-- 比赛/活动名称 -->
                    <div class="col-md-6 mb-3">
                        <label for="competitionName" class="form-label">比赛/活动名称</label>
                        <input type="text" class="form-control" id="competitionName" name="competitionName"
                               value="${award.competitionName}"
                               placeholder="例如：全国大学生数学建模竞赛">
                    </div>

                    <!-- 颁奖机构 -->
                    <div class="col-md-6 mb-3">
                        <label for="awardOrg" class="form-label">颁奖机构</label>
                        <input type="text" class="form-control" id="awardOrg" name="awardOrg"
                               value="${award.awardOrg}"
                               placeholder="例如：中国工业与应用数学学会">
                    </div>
                </div>

                <div class="row">
                    <!-- 获奖时间 -->
                    <div class="col-md-4 mb-3">
                        <label for="awardDate" class="form-label">获奖时间 <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="awardDate" name="awardDate"
                               value="${award.awardDate}" required placeholder="YYYY-MM-DD">
                        <div class="form-text text-muted">格式：YYYY-MM-DD</div>
                        <div class="invalid-feedback">请选择获奖时间</div>
                    </div>

                    <!-- 显示顺序 -->
                    <div class="col-md-4 mb-3">
                        <label for="displayOrder" class="form-label">显示顺序</label>
                        <input type="number" class="form-control" id="displayOrder" name="displayOrder"
                               value="${award.displayOrder != null ? award.displayOrder : '0'}" min="0"
                               placeholder="数字越小越靠前">
                    </div>
                </div>

                <!-- 获奖描述 -->
                <div class="mb-3">
                    <label for="description" class="form-label">获奖描述</label>
                    <textarea class="form-control" id="description" name="description" rows="4"
                              placeholder="描述比赛内容、获奖过程、作品特点等">${award.description}</textarea>
                </div>

                <!-- 表单按钮 -->
                <div class="d-flex justify-content-between mt-4">
                    <a href="${pageContext.request.contextPath}/resume/award?action=list&resumeId=${resumeId}" class="btn btn-outline-secondary">
                        取消
                    </a>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-lg me-1"></i>
                        ${empty award ? '保存获奖记录' : '更新获奖记录'}
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
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
