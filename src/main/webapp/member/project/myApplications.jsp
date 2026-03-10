<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
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
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-outline-primary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
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
        <div class="row row-cards">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">我的项目申请</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty applications}">
                            <div class="text-center text-muted py-5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                                <p class="mt-2">暂无申请记录</p>
                                <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-primary">浏览项目</a>
                            </div>
                        </c:if>
                        <c:if test="${not empty applications}">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>项目名称</th>
                                        <th>申请时间</th>
                                        <th>状态</th>
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
                                                <c:when test="${app.status == 'pending'}">
                                                    <span class="badge bg-warning text-dark">申请待确认</span>
                                                </c:when>
                                                <c:when test="${app.status == 'confirmed'}">
                                                    <span class="badge bg-success">申请成功</span>
                                                </c:when>
                                                <c:when test="${app.status == 'rejected'}">
                                                    <span class="badge bg-danger">申请已驳回</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${app.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${app.status == 'confirmed'}">
                                                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${app.projectId}" class="btn btn-sm btn-primary">进入项目</a>
                                            </c:if>
                                            <c:if test="${app.status != 'pending'}">
                                                <a href="${pageContext.request.contextPath}/project?action=detail&id=${app.projectId}" class="btn btn-sm btn-outline-secondary">查看项目</a>
                                            </c:if>
                                        </td>
                                    </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />