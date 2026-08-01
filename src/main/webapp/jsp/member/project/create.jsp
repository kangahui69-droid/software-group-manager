<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="创建项目" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-plus-circle me-2"></i>创建新项目
                    </h1>
                    <p class="admin-hero-subtitle">填写项目信息提交审核</p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=myApplications" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/project" method="POST" class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">项目基本信息</h3>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger">
                                ${error}
                            </div>
                        </c:if>

                        <input type="hidden" name="action" value="create">

                        <div class="mb-3">
                            <label class="form-label-design required">项目名称</label>
                            <input type="text" class="input-design" name="name" value="${project.name}" placeholder="请输入项目名称" required maxlength="100">
                        </div>

                        <div class="mb-3">
                            <label class="form-label-design">项目描述</label>
                            <textarea class="input-design" name="description" rows="4" placeholder="请输入项目描述">${project.description}</textarea>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">项目类别</label>
                                    <select class="input-design" name="category">
                                        <option value="">请选择</option>
                                        <option value="科研" ${project.category == '科研' ? 'selected' : ''}>科研</option>
                                        <option value="竞赛" ${project.category == '竞赛' ? 'selected' : ''}>竞赛</option>
                                        <option value="开发" ${project.category == '开发' ? 'selected' : ''}>开发</option>
                                        <option value="培训" ${project.category == '培训' ? 'selected' : ''}>培训</option>
                                        <option value="其他" ${project.category == '其他' ? 'selected' : ''}>其他</option>
                                    </select>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">年份</label>
                                    <input type="number" class="input-design" name="year" value="${project.year != null ? project.year : 2026}" placeholder="如：2026" min="2000" max="2100">
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">期望开始时间</label>
                                    <input type="date" class="input-design" name="expectedStartDate" value="${project.expectedStartDate}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design">期望结束时间</label>
                                    <input type="date" class="input-design" name="expectedEndDate" value="${project.expectedEndDate}">
                                </div>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label-design">预算（元）</label>
                            <input type="number" class="input-design" name="budget" value="${project.budget}" placeholder="请输入预算金额" min="0">
                        </div>

                        <div class="mb-3">
                            <label class="form-label-design">仓库地址</label>
                            <input type="text" class="input-design" name="repoUrl" value="${project.repoUrl}" placeholder="如：https://github.com/xxx">
                        </div>

                        <div class="mb-3">
                            <label class="form-label-design">文档地址</label>
                            <input type="text" class="input-design" name="docUrl" value="${project.docUrl}" placeholder="如：https://xxx.com/doc">
                        </div>
                    </div>
                    <div class="card-footer-design text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/project?action=myApplications" class="btn-outline-brand">取消</a>
                            <button type="submit" class="btn-brand ms-auto">提交创建</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />
