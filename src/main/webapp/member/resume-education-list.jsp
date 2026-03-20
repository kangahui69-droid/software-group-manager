<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="教育经历管理" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-mortarboard me-2"></i>教育经历管理
                </h2>
                <div class="text-muted mt-1">管理 "${resume.resumeName}" 的教育经历</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/education?action=create&resumeId=${resume.id}" class="btn btn-primary me-2">
                    <i class="bi bi-plus-lg me-1"></i>添加教育经历
                </a>
                <a href="${pageContext.request.contextPath}/resume?action=edit&id=${resume.id}" class="btn btn-outline-secondary">
                    返回简历
                </a>
            </div>
        </div>
    </div>

    <!-- 消息提示 -->
    <c:if test="${param.success == 'created'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>教育经历添加成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>教育经历更新成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>教育经历已删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 教育经历列表 -->
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">教育经历列表</h3>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty educations}">
                    <div class="empty">
                        <div class="empty-img">
                            <i class="bi bi-mortarboard display-1 text-muted"></i>
                        </div>
                        <p class="empty-title h3">暂无教育经历</p>
                        <p class="empty-subtitle text-muted">点击上方按钮添加您的教育经历</p>
                        <div class="empty-action">
                            <a href="${pageContext.request.contextPath}/resume/education?action=create&resumeId=${resume.id}" class="btn btn-primary">
                                <i class="bi bi-plus-lg me-1"></i>添加教育经历
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-vcenter table-striped">
                            <thead>
                                <tr>
                                    <th>学校名称</th>
                                    <th>专业</th>
                                    <th>学历</th>
                                    <th>时间</th>
                                    <th>状态</th>
                                    <th class="w-1">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="edu" items="${educations}">
                                    <tr>
                                        <td>
                                            <div class="font-weight-medium">${edu.schoolName}</div>
                                            <c:if test="${not empty edu.description}">
                                                <div class="text-muted text-small">${edu.description}</div>
                                            </c:if>
                                        </td>
                                        <td>${empty edu.major ? '-' : edu.major}</td>
                                        <td>${empty edu.degree ? '-' : edu.degree}</td>
                                        <td>
                                            ${edu.startDate} ~
                                            <c:choose>
                                                <c:when test="${edu.currentStudy}">至今</c:when>
                                                <c:otherwise>${edu.endDate}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${edu.currentStudy}">
                                                    <span class="badge bg-green">在读</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">已毕业</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-list flex-nowrap">
                                                <a href="${pageContext.request.contextPath}/resume/education?action=edit&id=${edu.id}" class="btn btn-white btn-sm">
                                                    编辑
                                                </a>
                                                <a href="#" class="btn btn-white btn-sm text-danger" onclick="return confirmDelete('${pageContext.request.contextPath}/resume/education?action=delete&id=${edu.id}')">
                                                    删除
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
    function confirmDelete(url) {
        if (confirm('确定要删除这条教育经历吗？此操作不可恢复。')) {
            window.location.href = url;
        }
        return false;
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
