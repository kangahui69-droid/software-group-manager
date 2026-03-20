<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    // 如果 resume 属性为 null，说明用户直接访问了 JSP 页面，重定向到列表页
    if (request.getAttribute("resume") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=list");
        return;
    }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="查看简历" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-file-earmark-person me-2"></i>查看简历
                </h2>
                <div class="text-muted mt-1">查看简历详细信息</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume?action=edit&id=${resume.id}" class="btn btn-primary me-2">
                    <i class="bi bi-pencil me-1"></i>编辑简历
                </a>
                <a href="${pageContext.request.contextPath}/resume?action=list" class="btn btn-outline-secondary">
                    返回列表
                </a>
            </div>
        </div>
    </div>

    <!-- 简历内容 -->
    <div class="row">
        <div class="col-lg-8">
            <!-- 基本信息卡片 -->
            <div class="card mb-3">
                <div class="card-header">
                    <h3 class="card-title">
                        <i class="bi bi-person me-2"></i>基本信息
                    </h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label text-muted">简历名称</label>
                            <div class="fw-bold">${resume.resumeName}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label text-muted">求职意向</label>
                            <div class="fw-bold">${empty resume.careerObjective ? '未填写' : resume.careerObjective}</div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label text-muted">联系电话</label>
                            <div class="fw-bold">${empty resume.phone ? '未填写' : resume.phone}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label text-muted">电子邮箱</label>
                            <div class="fw-bold">${empty resume.email ? '未填写' : resume.email}</div>
                        </div>
                    </div>
                    <c:if test="${not empty resume.summary}">
                        <div class="mb-3">
                            <label class="form-label text-muted">个人简介</label>
                            <div class="p-3 bg-light rounded">${resume.summary}</div>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- 教育经历占位 -->
            <div class="card mb-3">
                <div class="card-header">
                    <h3 class="card-title">
                        <i class="bi bi-mortarboard me-2"></i>教育经历
                    </h3>
                </div>
                <div class="card-body">
                    <div class="text-center py-4 text-muted">
                        <i class="bi bi-plus-circle display-4"></i>
                        <p class="mt-3">添加您的教育经历</p>
                        <a href="#" class="btn btn-outline-primary btn-sm" onclick="alert('功能开发中'); return false;">
                            添加教育经历
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- 侧边栏 -->
        <div class="col-lg-4">
            <!-- 简历状态卡片 -->
            <div class="card mb-3">
                <div class="card-header">
                    <h3 class="card-title">简历状态</h3>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label text-muted">当前状态</label>
                        <div>
                            <c:choose>
                                <c:when test="${resume.status == 0}">
                                    <span class="badge bg-secondary">草稿</span>
                                </c:when>
                                <c:when test="${resume.status == 1}">
                                    <span class="badge bg-success">已发布</span>
                                </c:when>
                                <c:when test="${resume.status == 2}">
                                    <span class="badge bg-warning">已隐藏</span>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-muted">浏览次数</label>
                        <div class="fw-bold">${resume.viewCount} 次</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-muted">创建时间</label>
                        <div class="fw-bold">${resume.createdAt}</div>
                    </div>
                    <div class="d-grid">
                        <a href="${pageContext.request.contextPath}/resume?action=preview&id=${resume.id}"
                           class="btn btn-outline-primary" target="_blank">
                            <i class="bi bi-eye me-1"></i>预览简历
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
