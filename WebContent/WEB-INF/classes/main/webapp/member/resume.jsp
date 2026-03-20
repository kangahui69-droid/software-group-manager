<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    // 防护逻辑：确保只有通过 Servlet 才能访问此页面
    // 如果 resumes 属性为 null，说明用户直接访问了 JSP 页面，重定向到 Servlet
    if (request.getAttribute("resumes") == null) {
        response.sendRedirect(request.getContextPath() + "/resume?action=list");
        return;
    }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的简历" />
</jsp:include>

<div class="container-xl">
    <!-- 面包屑导航 -->
    <nav aria-label="breadcrumb" class="mt-3">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp">首页</a></li>
            <li class="breadcrumb-item active" aria-current="page">我的简历</li>
        </ol>
    </nav>
    
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-file-earmark-person me-2"></i>我的简历
                </h2>
                <div class="text-muted mt-1">管理和完善您的个人简历</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume?action=create" class="btn btn-primary btn-hover-lift">
                    <i class="bi bi-plus-lg me-1"></i>创建新简历
                </a>
                <a href="${pageContext.request.contextPath}/resume?action=recycleBin" class="btn btn-outline-danger ms-2">
                    <i class="bi bi-trash me-1"></i>回收站
                </a>
            </div>
        </div>
    </div>

    <!-- 消息提示 -->
    <c:if test="${param.success == 'created'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历创建成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历更新成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历已删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'restored'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>简历恢复成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'defaultSet'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>默认简历设置成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>简历不存在或无权访问！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-warning alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>无效的ID格式！</div>
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

    <!-- 简历列表 -->
    <div class="row row-cards">
        <c:choose>
            <%-- 使用 empty 检查，能同时处理 null 和空列表 --%>
            <c:when test="${empty resumes}">
                <!-- 空状态 -->
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="empty">
                                <div class="empty-img">
                                    <i class="bi bi-file-earmark-text display-1 text-primary" style="font-size: 5rem; opacity: 0.8;"></i>
                                </div>
                                <p class="empty-title h3">还没有创建简历</p>
                                <p class="empty-subtitle text-muted">
                                    创建您的第一份简历，展示您的技能和经验
                                </p>
                                <div class="empty-action">
                                    <a href="${pageContext.request.contextPath}/resume?action=create" class="btn btn-primary btn-hover-lift">
                                        <i class="bi bi-plus-lg me-1"></i>创建简历
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <!-- 简历卡片列表 -->
                <c:forEach var="resume" items="${resumes}">
                    <div class="col-md-6 col-lg-4">
                        <div class="card ${resume.isDefaultResume() ? 'border-primary' : ''}">
                            <div class="card-header ${resume.isDefaultResume() ? 'bg-primary text-white' : ''}">
                                <h3 class="card-title">
                                    <c:if test="${resume.isDefaultResume()}">
                                        <i class="bi bi-star-fill me-1" title="默认简历"></i>
                                    </c:if>
                                    ${resume.resumeName}
                                </h3>
                                <div class="card-actions">
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
                            <div class="card-body">
                                <div class="mb-2">
                                    <i class="bi bi-calendar me-1 text-muted"></i>
                                    <small class="text-muted">创建于: <fmt:formatDate value="${resume.createdAt}" pattern="yyyy年MM月dd日"/></small>
                                </div>
                                <div class="mb-2">
                                    <i class="bi bi-eye me-1 text-muted"></i>
                                    <small class="text-muted">浏览: ${resume.viewCount}次</small>
                                </div>
                                <c:if test="${not empty resume.careerObjective}">
                                    <div class="mt-2">
                                        <small class="text-muted">求职意向: ${resume.careerObjective}</small>
                                    </div>
                                </c:if>
                            </div>
                            <div class="card-footer">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div class="btn-group">
                                        <a href="${pageContext.request.contextPath}/resume?action=view&id=${resume.id}"
                                           class="btn btn-outline-primary btn-sm btn-hover-lift">
                                            <i class="bi bi-eye"></i> 查看
                                        </a>
                                        <a href="${pageContext.request.contextPath}/resume?action=edit&id=${resume.id}"
                                           class="btn btn-outline-secondary btn-sm btn-hover-lift">
                                            <i class="bi bi-pencil"></i> 编辑
                                        </a>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button"
                                                data-bs-toggle="dropdown" aria-expanded="false">
                                            <i class="bi bi-three-dots-vertical"></i>
                                        </button>
                                        <ul class="dropdown-menu dropdown-menu-end">
                                            <li>
                                                <a class="dropdown-item" href="${pageContext.request.contextPath}/resume?action=preview&id=${resume.id}">
                                                    <i class="bi bi-file-earmark-pdf me-1"></i> 预览简历
                                                </a>
                                            </li>
                                            <c:if test="${!resume.isDefaultResume()}">
                                                <li>
                                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/resume?action=setDefault&id=${resume.id}">
                                                        <i class="bi bi-star me-1"></i> 设为默认
                                                    </a>
                                                </li>
                                            </c:if>
                                            <li><hr class="dropdown-divider"></li>
                                            <li>
                                                <a class="dropdown-item text-danger" href="#"
                                                   onclick="return confirmDelete('${pageContext.request.contextPath}/resume?action=delete&id=${resume.id}')">
                                                    <i class="bi bi-trash me-1"></i> 删除简历
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
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
        if (confirm('确定要删除这份简历吗？此操作不可恢复。')) {
            window.location.href = url;
        }
        return false;
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
