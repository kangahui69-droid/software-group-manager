<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="活动审核" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    活动审核
                </h2>
                <p class="text-muted">审核学生提交的活动申请</p>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity?action=manage" class="btn btn-outline-secondary">
                    返回活动管理
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${empty pendingActivities}">
            <div class="alert alert-info">
                暂无待审核的活动申请
            </div>
        </c:if>
        
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>标题</th>
                            <th>地点</th>
                            <th>开始时间</th>
                            <th>结束时间</th>
                            <th>最大人数</th>
                            <th>申请时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="a" items="${pendingActivities}">
                            <tr>
                                <td>${a.title}</td>
                                <td class="text-muted">${a.location}</td>
                                <td class="text-muted"><fmt:formatDate value="${a.startTime}" pattern="yyyy-MM-dd HH:mm" /></td>
                                <td class="text-muted"><fmt:formatDate value="${a.endTime}" pattern="yyyy-MM-dd HH:mm" /></td>
                                <td class="text-muted">${a.maxParticipants > 0 ? a.maxParticipants : '不限'}</td>
                                <td class="text-muted"><fmt:formatDate value="${a.createdAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/activity?action=approve&id=${a.id}" class="btn btn-success btn-sm">通过</a>
                                        <a href="${pageContext.request.contextPath}/activity?action=reject&id=${a.id}" class="btn btn-danger btn-sm">拒绝</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />
