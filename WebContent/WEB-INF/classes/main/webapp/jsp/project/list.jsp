<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../common/layout_top.jsp">
            <jsp:param name="title" value="项目管理" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            项目管理
                        </h2>
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
                                    <th>负责人</th>
                                    <th>状态</th>
                                    <th>年份</th>
                                    <th class="w-1"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${projects}">
                                    <tr>
                                        <td>${p.name}</td>
                                        <td class="text-muted">
                                            ${p.leaderId}
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status == 'pending'}">
                                                    <span class="badge bg-warning text-warning-fg">待审批</span>
                                                </c:when>
                                                <c:when test="${p.status == 'approved'}">
                                                    <span class="badge bg-success text-success-fg">已批准</span>
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
                                            </c:choose>
                                        </td>
                                        <td class="text-muted">
                                            ${p.year}
                                        </td>
                                        <td>
                                            <c:if test="${p.status == 'pending'}">
                                                <div class="btn-list flex-nowrap">
                                                    <form action="${pageContext.request.contextPath}/project"
                                                        method="post" class="d-inline">
                                                        <input type="hidden" name="action" value="approve">
                                                        <input type="hidden" name="id" value="${p.id}">
                                                        <button type="submit" class="btn btn-sm btn-success">批准</button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/project"
                                                        method="post" class="d-inline">
                                                        <input type="hidden" name="action" value="reject">
                                                        <input type="hidden" name="id" value="${p.id}">
                                                        <button type="submit" class="btn btn-sm btn-danger">拒绝</button>
                                                    </form>
                                                </div>
                                            </c:if>
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

        <jsp:include page="../common/layout_bottom.jsp" />