<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="回收站 - 已删除的简历" />
</jsp:include>
<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    if (request.getAttribute("deletedResumes") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=recycleBin");
        return;
    }
%>
<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    if (request.getAttribute("deletedResumes") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=recycleBin");
        return;
    }
%>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-trash me-2"></i>回收站
                </h2>
                <div class="text-muted mt-1">管理已删除的简历，可以恢复或彻底删除</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume?action=list" class="btn btn-outline-primary">
                    <i class="bi bi-arrow-left me-1"></i>返回简历列表
                </a>
            </div>
        </div>
    </div>

    <!-- 消息提示 -->
    <c:if test="${param.success == 'restored'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历恢复成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历已永久删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>简历不存在或无权操作！</div>
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

    <!-- 已删除的简历列表 -->
    <div class="row row-cards">
        <c:choose>
            <c:when test="${empty deletedResumes}">
                <!-- 空状态 -->
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="empty">
                                <div class="empty-img">
                                    <i class="bi bi-trash display-1 text-muted"></i>
                                </div>
                                <p class="empty-title h3">回收站是空的</p>
                                <p class="empty-subtitle text-muted">
                                    您没有已删除的简历，删除的简历会在这里显示
                                </p>
                                <div class="empty-action">
                                    <a href="${pageContext.request.contextPath}/resume?action=list" class="btn btn-primary">
                                        <i class="bi bi-arrow-left me-1"></i>返回简历列表
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <!-- 已删除的简历卡片列表 -->
                <c:forEach var="resume" items="${deletedResumes}">
                    <div class="col-md-6 col-lg-4">
                        <div class="card border-danger">
                            <div class="card-header bg-danger text-white">
                                <h3 class="card-title">
                                    <i class="bi bi-trash me-2"></i>${resume.resumeName}
                                </h3>
                            </div>
                            <div class="card-body">
                                <div class="mb-2">
                                    <i class="bi bi-calendar me-1 text-muted"></i>
                                    <small class="text-muted">创建于: ${resume.createdAt}</small>
                                </div>
                                <div class="mb-2">
                                    <i class="bi bi-trash me-1 text-muted"></i>
                                    <small class="text-muted">删除于: ${resume.updatedAt}</small>
                                </div>
                                <c:if test="${not empty resume.careerObjective}">
                                    <div class="mt-2">
                                        <small class="text-muted">求职意向: ${resume.careerObjective}</small>
                                    </div>
                                </c:if>
                            </div>
                            <div class="card-footer">
                                <div class="d-flex justify-content-between align-items-center">
                                    <a href="${pageContext.request.contextPath}/resume?action=restore&id=${resume.id}"
                                       class="btn btn-success"
                                       onclick="return confirmRestore()">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>恢复简历
                                    </a>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/resume?action=hardDelete&id=${resume.id}"
                                           class="btn btn-outline-danger btn-sm"
                                           onclick="return confirmHardDelete()"
                                           title="永久删除后无法恢复">
                                            <i class="bi bi-trash-fill me-1"></i>永久删除
                                        </a>
                                    </div>
                                </div>
                                <div class="text-center mt-2">
                                    <span class="text-muted small">
                                        <i class="bi bi-info-circle me-1"></i>30天后自动永久删除
                                    </span>
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
    function confirmRestore() {
        return confirm('确定要恢复这份简历吗？恢复后将在简历列表中显示。');
    }

    function confirmHardDelete() {
        return confirm('警告：永久删除后无法恢复！\n\n确定要永久删除这份简历吗？');
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
