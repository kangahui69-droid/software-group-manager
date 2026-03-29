<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="学习记录"/>
    <jsp:param name="active" value="study"/>
</jsp:include>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            <i class="bi bi-list-ul me-2"></i>学习记录
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${ctx}/study" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>返回学习中心
                        </a>
                    </div>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <!-- 统计卡片 -->
            <div class="row row-cards mb-3">
                <div class="col-sm-6 col-lg-3">
                    <div class="card">
                        <div class="card-body text-center">
                            <div class="h1 text-blue mb-1">${statistics.totalSessions}</div>
                            <div class="text-muted">总学习次数</div>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="card">
                        <div class="card-body text-center">
                            <div class="h1 text-green mb-1">${statistics.completedSessions}</div>
                            <div class="text-muted">已完成</div>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="card">
                        <div class="card-body text-center">
                            <div class="h1 text-purple mb-1">${statistics.totalDuration}</div>
                            <div class="text-muted">总时长(分钟)</div>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3">
                    <div class="card">
                        <div class="card-body text-center">
                            <div class="h1 text-orange mb-1">
                                <c:choose>
                                    <c:when test="${not empty statistics.avgDuration}">
                                        <fmt:formatNumber value="${statistics.avgDuration}" pattern="#0"/>
                                    </c:when>
                                    <c:otherwise>0</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="text-muted">平均时长(分钟)</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 学习记录列表 -->
            <div class="card">
                <div class="table-responsive">
                    <table class="table table-vcenter card-table table-striped">
                        <thead>
                            <tr>
                                <th>日期</th>
                                <th>开始时间</th>
                                <th>结束时间</th>
                                <th>学习时长</th>
                                <th>状态</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty sessionList}">
                                    <tr>
                                        <td colspan="5" class="text-center text-muted py-4">
                                            暂无学习记录
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${sessionList}" var="sess">
                                        <tr>
                                            <td><fmt:formatDate value="${sess.sessionDate}" pattern="yyyy-MM-dd"/></td>
                                            <td><fmt:formatDate value="${sess.checkInTime}" pattern="HH:mm:ss"/></td>
                                            <td>
                                                <c:if test="${not empty sess.checkOutTime}">
                                                    <fmt:formatDate value="${sess.checkOutTime}" pattern="HH:mm:ss"/>
                                                </c:if>
                                                <c:if test="${empty sess.checkOutTime}">--</c:if>
                                            </td>
                                            <td>
                                                <c:if test="${not empty sess.duration}">${sess.duration} 分钟</c:if>
                                                <c:if test="${empty sess.duration}"><span class="text-warning">进行中</span></c:if>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sess.status == 'ACTIVE'}">
                                                        <span class="badge bg-orange">进行中</span>
                                                    </c:when>
                                                    <c:when test="${sess.status == 'COMPLETED'}">
                                                        <span class="badge bg-green">已完成</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/jsp/common/layout_bottom.jsp"/>