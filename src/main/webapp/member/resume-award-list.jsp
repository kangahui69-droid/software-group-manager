<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    // 修改为检查 resumeId 而不是 awards，允许 awards 为空列表
    if (request.getAttribute("resumeId") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=list");
        return;
    }
%>

<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="获奖情况管理" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-trophy me-2"></i>获奖情况管理
                </h2>
                <div class="text-muted mt-1">管理您的获奖记录，展示您的荣誉成就</div>
                <div class="mt-2">
                    <span class="badge bg-info">
                        <i class="bi bi-file-earmark-person me-1"></i>
                        当前简历ID: <strong>${resumeId}</strong>
                        <c:if test="${not empty resume}">
                            - ${resume.resumeName}
                        </c:if>
                    </span>
                    <span class="badge bg-secondary ms-2">
                        <i class="bi bi-trophy me-1"></i>
                        共 ${empty awards ? 0 : awards.size()} 条记录
                    </span>
                </div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/award?action=create&resumeId=${resumeId}" class="btn btn-primary">
                    <i class="bi bi-plus-lg me-1"></i>添加获奖记录
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
                <div>获奖记录添加成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>获奖记录更新成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>获奖记录已删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>获奖记录不存在或无权访问！</div>
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
    <%-- 添加对自定义错误信息的显示 --%>
    <c:if test="${not empty param.error && param.error != 'notfound' && param.error != 'failed'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>${param.error}</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 获奖记录列表 -->
    <div class="row row-cards">
        <c:choose>
            <c:when test="${empty awards}">
                <!-- 空状态 -->
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="empty">
                                <div class="empty-img">
                                    <i class="bi bi-trophy display-1 text-muted" style="font-size: 5rem;"></i>
                                </div>
                                <p class="empty-title h3">还没有添加获奖记录</p>
                                <p class="empty-subtitle text-muted">
                                    添加您的获奖记录，展示您的荣誉和成就
                                </p>
                                <div class="empty-action">
                                    <a href="${pageContext.request.contextPath}/resume/award?action=create&resumeId=${resumeId}" class="btn btn-primary">
                                        <i class="bi bi-plus-lg me-1"></i>添加获奖记录
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <!-- 获奖记录卡片列表 -->
                <c:forEach var="award" items="${awards}" varStatus="status">
                    <div class="col-md-6 col-lg-4">
                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">
                                    <i class="bi bi-trophy me-2 text-warning"></i>${award.awardName}
                                </h3>
                                <c:if test="${not empty award.awardLevel}">
                                    <span class="badge bg-success">${award.awardLevel}</span>
                                </c:if>
                            </div>
                            <div class="card-body">
                                <c:if test="${not empty award.competitionName}">
                                    <div class="mb-2">
                                        <i class="bi bi-flag me-1 text-muted"></i>
                                        <span class="text-muted">比赛/活动:</span>
                                        <span class="fw-bold">${award.competitionName}</span>
                                    </div>
                                </c:if>
                                <c:if test="${not empty award.awardOrg}">
                                    <div class="mb-2">
                                        <i class="bi bi-building me-1 text-muted"></i>
                                        <span class="text-muted">颁奖机构:</span>
                                        <span class="fw-bold">${award.awardOrg}</span>
                                    </div>
                                </c:if>
                                <div class="mb-2">
                                    <i class="bi bi-calendar me-1 text-muted"></i>
                                    <span class="text-muted">获奖时间:</span>
                                    <span class="fw-bold">
                                        <fmt:formatDate value="${award.awardDate}" pattern="yyyy年MM月"/>
                                    </span>
                                </div>
                                <c:if test="${not empty award.description}">
                                    <div class="mt-3">
                                        <span class="text-muted small">获奖描述:</span>
                                        <p class="mb-0 mt-1 text-muted small">${award.description}</p>
                                    </div>
                                </c:if>
                            </div>
                            <div class="card-footer">
                                <div class="d-flex justify-content-between">
                                    <a href="${pageContext.request.contextPath}/resume/award?action=edit&resumeId=${resumeId}&id=${award.id}"
                                       class="btn btn-outline-primary btn-sm">
                                        <i class="bi bi-pencil me-1"></i>编辑
                                    </a>
                                    <button type="button" class="btn btn-outline-danger btn-sm"
                                            onclick="confirmDelete('${pageContext.request.contextPath}/resume/award?action=delete&resumeId=${resumeId}&id=${award.id}')">
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
        if (confirm('确定要删除这条获奖记录吗？此操作不可恢复。')) {
            window.location.href = url;
        }
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
