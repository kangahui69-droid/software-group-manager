<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    if (request.getAttribute("projects") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=list");
        return;
    }
%>

<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目经历管理" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-kanban me-2"></i>项目经历管理
                </h2>
                <div class="text-muted mt-1">管理您的项目经历，展示您的实战能力</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/project?action=create&resumeId=${resumeId}" class="btn btn-primary">
                    <i class="bi bi-plus-lg me-1"></i>添加项目经历
                </a>
                <a href="${pageContext.request.contextPath}/resume?action=edit&id=${resumeId}" class="btn btn-outline-secondary ms-2">
                    <i class="bi bi-arrow-left me-1"></i>返回简历
                </a>
            </div>
        </div>
    </div>

    <!-- 消息提示 -->
    <c:if test="${param.success == 'created'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>项目经历添加成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>项目经历更新成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>项目经历已删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>项目经历不存在或无权访问！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'failed'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-x-circle-fill me-2"></i></div>
                <div>操作失败，请重试！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 项目经历列表 -->
    <div class="row row-cards">
        <c:choose>
            <c:when test="${empty projects}">
                <!-- 空状态 -->
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="empty">
                                <div class="empty-img">
                                    <i class="bi bi-kanban display-1 text-muted" style="font-size: 5rem;"></i>
                                </div>
                                <p class="empty-title h3">还没有添加项目经历</p>
                                <p class="empty-subtitle text-muted">
                                    添加您的项目经历，展示您的实战能力和技术栈
                                </p>
                                <div class="empty-action">
                                    <a href="${pageContext.request.contextPath}/resume/project?action=create&resumeId=${resumeId}" class="btn btn-primary">
                                        <i class="bi bi-plus-lg me-1"></i>添加项目经历
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <!-- 项目经历卡片列表 -->
                <c:forEach var="project" items="${projects}" varStatus="status">
                    <div class="col-md-6 col-lg-4">
                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">
                                    <i class="bi bi-kanban me-2 text-primary"></i>${project.projectName}
                                </h3>
                                <c:if test="${project.isCurrentProject()}">
                                    <span class="badge bg-success">进行中</span>
                                </c:if>
                            </div>
                            <div class="card-body">
                                <div class="mb-2">
                                    <i class="bi bi-person-badge me-1 text-muted"></i>
                                    <span class="text-muted">担任角色:</span>
                                    <span class="fw-bold">${project.role}</span>
                                </div>
                                <c:if test="${not empty project.teamSize}">
                                    <div class="mb-2">
                                        <i class="bi bi-people me-1 text-muted"></i>
                                        <span class="text-muted">团队规模:</span>
                                        <span class="fw-bold">${project.teamSize}人</span>
                                    </div>
                                </c:if>
                                <div class="mb-2">
                                    <i class="bi bi-calendar me-1 text-muted"></i>
                                    <span class="text-muted">项目时间:</span>
                                    <span class="fw-bold">
                                        <fmt:formatDate value="${project.startDate}" pattern="yyyy.MM"/>
                                        -
                                        <c:choose>
                                            <c:when test="${project.isCurrentProject()}">至今</c:when>
                                            <c:otherwise><fmt:formatDate value="${project.endDate}" pattern="yyyy.MM"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <c:if test="${not empty project.technologies}">
                                    <div class="mt-3">
                                        <span class="text-muted small">技术栈:</span>
                                        <div class="mt-1">
                                            <c:forEach var="tech" items="${fn:split(project.technologies, ',')}" varStatus="techStatus">
                                                <span class="badge bg-primary me-1">${fn:trim(tech)}</span>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                                <c:if test="${not empty project.description}">
                                    <div class="mt-3">
                                        <span class="text-muted small">项目描述:</span>
                                        <p class="mb-0 mt-1 text-muted small">${fn:substring(project.description, 0, 100)}${fn:length(project.description) > 100 ? '...' : ''}</p>
                                    </div>
                                </c:if>
                            </div>
                            <div class="card-footer">
                                <div class="d-flex justify-content-between">
                                    <a href="${pageContext.request.contextPath}/resume/project?action=edit&resumeId=${resumeId}&id=${project.id}"
                                       class="btn btn-outline-primary btn-sm">
                                        <i class="bi bi-pencil me-1"></i>编辑
                                    </a>
                                    <button type="button" class="btn btn-outline-danger btn-sm"
                                            onclick="confirmDelete('${pageContext.request.contextPath}/resume/project?action=delete&resumeId=${resumeId}&id=${project.id}')">
                                        <i class="bi bi-trash me-1"></i>删除
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
    function confirmDelete(url) {
        if (confirm('确定要删除这个项目经历吗？此操作不可恢复。')) {
            window.location.href = url;
        }
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
