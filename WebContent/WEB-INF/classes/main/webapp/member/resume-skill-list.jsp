<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="技能特长管理" />
</jsp:include>

<div class="container-xl">
    <!-- 页面标题 -->
    <div class="page-header d-print-none">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    <i class="bi bi-tools me-2"></i>技能特长管理
                </h2>
                <div class="text-muted mt-1">管理 "${resume.resumeName}" 的技能特长</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/resume/skill?action=create&resumeId=${resume.id}" class="btn btn-primary me-2">
                    <i class="bi bi-plus-lg me-1"></i>添加技能
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
                <div>技能添加成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>技能更新成功！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-check-circle-fill me-2"></i></div>
                <div>技能已删除！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'failed'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>操作失败，请稍后重试！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>找不到指定的技能或简历！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>
    <c:if test="${param.error == 'invalid_id'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <div class="d-flex">
                <div><i class="bi bi-exclamation-triangle-fill me-2"></i></div>
                <div>无效的ID格式！</div>
            </div>
            <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
        </div>
    </c:if>

    <!-- 技能列表 -->
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">技能列表</h3>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty skills}">
                    <div class="empty">
                        <div class="empty-img">
                            <i class="bi bi-tools display-1 text-muted"></i>
                        </div>
                        <p class="empty-title h3">暂无技能特长</p>
                        <p class="empty-subtitle text-muted">点击上方按钮添加您的技能特长</p>
                        <div class="empty-action">
                            <a href="${pageContext.request.contextPath}/resume/skill?action=create&resumeId=${resume.id}" class="btn btn-primary">
                                <i class="bi bi-plus-lg me-1"></i>添加技能
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-vcenter table-striped">
                            <thead>
                                <tr>
                                    <th>技能名称</th>
                                    <th>分类</th>
                                    <th>熟练程度</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="skill" items="${skills}">
                                    <tr>
                                        <td>
                                            <div class="font-weight-medium">${skill.skillName}</div>
                                            <c:if test="${not empty skill.description}">
                                                <div class="text-muted text-small">${skill.description}</div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="badge bg-info">${empty skill.category ? '其他' : skill.category}</span>
                                        </td>
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <span class="me-2">${skill.proficiency}</span>
                                                <c:if test="${not empty skill.proficiencyScore}">
                                                    <span class="badge bg-primary">${skill.proficiencyScore}分</span>
                                                </c:if>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="btn-list flex-nowrap">
                                                <a href="${pageContext.request.contextPath}/resume/skill?action=edit&id=${skill.id}" class="btn btn-white btn-sm">
                                                    编辑
                                                </a>
                                                <a href="#" class="btn btn-white btn-sm text-danger" onclick="return confirmDelete('${pageContext.request.contextPath}/resume/skill?action=delete&id=${skill.id}')">
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
        if (confirm('确定要删除这条技能吗？此操作不可恢复。')) {
            window.location.href = url;
        }
        return false;
    }
</script>

<jsp:include page="../jsp/common/layout_bottom.jsp" />
