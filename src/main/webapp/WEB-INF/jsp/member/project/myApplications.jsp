<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的申请记录" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    我的申请记录
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=myProjects" class="btn btn-outline-primary">
                    返回项目列表
                </a>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty param.msg}">
<div class="container-xl mt-3">
    <div class="alert alert-info alert-dismissible" role="alert">
        <div>${param.msg}</div>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</div>
</c:if>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${empty applications}">
            <div class="text-center text-muted py-5">
                <p>暂无申请记录</p>
                <a href="${pageContext.request.contextPath}/project?action=browse" class="btn btn-primary">浏览项目</a>
            </div>
        </c:if>
        <c:if test="${not empty applications}">
        <div class="card">
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>项目名称</th>
                            <th>申请时间</th>
                            <th>状态</th>
                            <th>处理时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="app" items="${applications}">
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/project?action=detail&id=${app.projectId}">${app.projectName}</a>
                            </td>
                            <td><fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${app.status == 'PENDING'}">
                                        <span class="badge bg-warning text-dark">申请待确认</span>
                                    </c:when>
                                    <c:when test="${app.status == 'CONFIRMED'}">
                                        <span class="badge bg-success">申请成功</span>
                                    </c:when>
                                    <c:when test="${app.status == 'REJECTED'}">
                                        <span class="badge bg-danger">申请已驳回</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">${app.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty app.handledAt}">
                                        <fmt:formatDate value="${app.handledAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${app.status == 'CONFIRMED' || app.status == 'REJECTED'}">
                                    <a href="${pageContext.request.contextPath}/project?action=detail&id=${app.projectId}" class="btn btn-sm btn-primary">查看项目</a>
                                </c:if>
                            </td>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        <c:if test="${totalPages > 1}">
            <nav class="d-flex align-items-center justify-content-center gap-3 mt-3" aria-label="申请记录分页">
                <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/project?action=myApplications&page=${currentPage - 1}" class="btn btn-outline-primary btn-sm">上一页</a>
                </c:if>
                <span>第 ${currentPage} / ${totalPages} 页，共 ${totalCount} 条</span>
                <c:if test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/project?action=myApplications&page=${currentPage + 1}" class="btn btn-outline-primary btn-sm">下一页</a>
                </c:if>
            </nav>
        </c:if>
        </c:if>
    </div>
</div>

<jsp:include page="/jsp/common/layout_bottom.jsp" />
