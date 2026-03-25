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
                        <!-- 页面标题 -->
                        <div class="page-header d-print-none">
                            <div class="row align-items-center">
                                <div class="col">
                                    <h2 class="page-title">
                                        <i class="bi bi-list-ul me-2"></i>学习记录
                                    </h2>
                                </div>
                            </div>
                        </div>

                        <!-- 筛选表单 -->
                        <div class="card mb-3">
                            <div class="card-body">
                                <form method="get" action="${ctx}/study/list">
                                    <div class="row align-items-end">
                                        <div class="col-auto">
                                            <button type="button" class="btn btn-outline-secondary btn-sm" onclick="setQuickDate('today')">今天</button>
                                            <button type="button" class="btn btn-outline-secondary btn-sm" onclick="setQuickDate('week')">本周</button>
                                            <button type="button" class="btn btn-outline-secondary btn-sm" onclick="setQuickDate('month')">本月</button>
                                        </div>
                                        <div class="col-md-3">
                                            <label class="form-label">开始日期</label>
                                            <input type="date" class="form-control" name="startDate" value="${startDate}">
                                        </div>
                                        <div class="col-md-3">
                                            <label class="form-label">结束日期</label>
                                            <input type="date" class="form-control" name="endDate" value="${endDate}">
                                        </div>
                                        <div class="col-auto">
                                            <button type="submit" class="btn btn-primary">
                                                <i class="bi bi-search me-2"></i>查询
                                            </button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <script>
                            function setQuickDate(type) {
                                var today = new Date();
                                var startDate, endDate;

                                if (type === 'today') {
                                    startDate = formatDate(today);
                                    endDate = formatDate(today);
                                } else if (type === 'week') {
                                    var dayOfWeek = today.getDay();
                                    var start = new Date(today);
                                    start.setDate(today.getDate() - dayOfWeek);
                                    startDate = formatDate(start);
                                    endDate = formatDate(today);
                                } else if (type === 'month') {
                                    var firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
                                    startDate = formatDate(firstDay);
                                    endDate = formatDate(today);
                                }

                                document.querySelector('input[name="startDate"]').value = startDate;
                                document.querySelector('input[name="endDate"]').value = endDate;
                            }

                            function formatDate(date) {
                                var year = date.getFullYear();
                                var month = String(date.getMonth() + 1).padStart(2, '0');
                                var day = String(date.getDate()).padStart(2, '0');
                                return year + '-' + month + '-' + day;
                            }
                        </script>

                        <!-- 统计概览 -->
                        <div class="row mb-3">
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
                                        <div class="h1 text-purple mb-1">
                                            ${empty statistics.totalDuration ? 0 : statistics.totalDuration}
                                        </div>
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

                        <!-- 记录列表 -->
                        <div class="card">
                            <div class="table-responsive">
                                <table class="table table-vcenter card-table">
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
                                                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
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
                                                            <c:if test="${empty sess.checkOutTime}">
                                                                <span class="text-muted">--</span>
                                                            </c:if>
                                                        </td>
                                                        <td>
                                                            <c:if test="${not empty sess.duration}">
                                                                ${sess.duration} 分钟
                                                            </c:if>
                                                            <c:if test="${empty sess.duration}">
                                                                <span class="badge bg-orange">进行中</span>
                                                            </c:if>
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

                            <!-- 分页 -->
                            <c:if test="${totalPages > 1}">
                                <div class="card-footer d-flex align-items-center">
                                    <ul class="pagination m-0 ms-auto">
                                        <c:if test="${currentPage > 1}">
                                            <li class="page-item">
                                                <a class="page-link" href="?page=${currentPage - 1}&startDate=${startDate}&endDate=${endDate}">
                                                    <i class="bi bi-chevron-left"></i>
                                                </a>
                                            </li>
                                        </c:if>
                                        <c:forEach begin="1" end="${totalPages}" var="i">
                                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="?page=${i}&startDate=${startDate}&endDate=${endDate}">${i}</a>
                                            </li>
                                        </c:forEach>
                                        <c:if test="${currentPage < totalPages}">
                                            <li class="page-item">
                                                <a class="page-link" href="?page=${currentPage + 1}&startDate=${startDate}&endDate=${endDate}">
                                                    <i class="bi bi-chevron-right"></i>
                                                </a>
                                            </li>
                                        </c:if>
                                    </ul>
                                </div>
                            </c:if>
                        </div>

                        <!-- 返回链接 -->
                        <div class="mt-3">
                            <a href="${ctx}/study" class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-left me-2"></i>返回学习中心
                            </a>
                        </div>
                    </div>
                </div>
            </div>

<jsp:include page="/jsp/common/layout_bottom.jsp"/>
