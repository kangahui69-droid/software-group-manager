<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的项目" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    我的项目
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=create" class="btn btn-primary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    添加项目
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>标题</th>
                            <th>分类</th>
                            <th>状态</th>
                            <th>年份</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${projects}">
                            <tr>
                                <td>${p.name}</td>
                                <td class="text-muted">${p.category}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'pending'}">
                                            <span class="badge bg-warning">待审批</span>
                                        </c:when>
                                        <c:when test="${p.status == 'approved'}">
                                            <span class="badge bg-success">已批准</span>
                                        </c:when>
                                        <c:when test="${p.status == 'in_progress'}">
                                            <span class="badge bg-primary">进行中</span>
                                        </c:when>
                                        <c:when test="${p.status == 'completed'}">
                                            <span class="badge bg-success">已完成</span>
                                        </c:when>
                                        <c:when test="${p.status == 'canceled'}">
                                            <span class="badge bg-danger">已取消</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${p.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${p.year}</td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/project?action=detail&id=${p.id}" 
                                           class="btn btn-white btn-sm">详情</a>
                                        <c:if test="${p.leaderId == sessionScope.user.id || sessionScope.user.role == 'ADMIN'}">
                                            <a href="${pageContext.request.contextPath}/project?action=edit&id=${p.id}" 
                                               class="btn btn-white btn-sm">编辑</a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty projects}">
                            <tr>
                                <td colspan="5" class="text-center text-muted">暂无项目记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />