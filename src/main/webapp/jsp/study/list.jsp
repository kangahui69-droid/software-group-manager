<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学习记录 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/study.css">
    <style>
        body { background: #f5f5f5; }
        .list-page { padding: 20px; }
        .list-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); max-width: 900px; margin: 0 auto; }
        .list-header { padding: 20px; border-bottom: 1px solid #eee; }
        .list-header h2 { margin: 0; color: #333; font-size: 20px; }
        .filter-form { padding: 20px; background: #fafafa; }
        .filter-row { display: flex; gap: 15px; align-items: flex-end; flex-wrap: wrap; }
        .filter-item { flex: 1; min-width: 150px; }
        .filter-item label { display: block; font-size: 12px; color: #666; margin-bottom: 5px; }
        .filter-item input { width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
        .stats-summary { display: flex; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
        .stat-item { flex: 1; text-align: center; padding: 15px; }
        .stat-value { display: block; font-size: 24px; font-weight: bold; }
        .stat-label { display: block; font-size: 12px; opacity: 0.9; margin-top: 5px; }
        .session-table { width: 100%; border-collapse: collapse; }
        .session-table th, .session-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
        .session-table th { background: #f9f9f9; font-weight: 600; color: #333; font-size: 13px; }
        .session-table td { color: #666; font-size: 13px; }
        .session-table tr:hover { background: #fafafa; }
        .status-badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; }
        .status-badge.ACTIVE { background: #fff3e0; color: #ff9800; }
        .status-badge.COMPLETED { background: #e8f5e9; color: #4caf50; }
        .pagination { padding: 20px; display: flex; justify-content: center; gap: 8px; }
        .pagination a, .pagination span { display: inline-block; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; text-decoration: none; color: #666; font-size: 13px; }
        .pagination a:hover { background: #667eea; color: #fff; border-color: #667eea; }
        .pagination .current { background: #667eea; color: #fff; border-color: #667eea; }
        .quick-btn { padding: 5px 12px; font-size: 12px; border: 1px solid #11998e; border-radius: 4px; background: #fff; color: #11998e; cursor: pointer; }
        .quick-btn:hover { background: #11998e; color: #fff; }
        .back-link { padding: 15px 20px; border-top: 1px solid #eee; }
        .back-link a { color: #667eea; text-decoration: none; font-size: 14px; }
    </style>
</head>
<body>
    <div class="list-page">
        <div class="list-card">
            <div class="list-header">
                <h2>学习记录</h2>
            </div>

            <form class="filter-form" method="get" action="${ctx}/study/list">
                <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                    <button type="button" class="quick-btn" onclick="setQuickDate('today')">今天</button>
                    <button type="button" class="quick-btn" onclick="setQuickDate('week')">本周</button>
                    <button type="button" class="quick-btn" onclick="setQuickDate('month')">本月</button>
                </div>
                <div class="filter-row">
                    <div class="filter-item">
                        <label>开始日期</label>
                        <input type="date" name="startDate" value="${startDate}">
                    </div>
                    <div class="filter-item">
                        <label>结束日期</label>
                        <input type="date" name="endDate" value="${endDate}">
                    </div>
                    <button type="submit" class="btn btn-primary">查询</button>
                </div>
            </form>

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

            <div class="stats-summary">
                <div class="stat-item">
                    <span class="stat-value">${statistics.totalSessions}</span>
                    <span class="stat-label">总学习次数</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${statistics.completedSessions}</span>
                    <span class="stat-label">已完成</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">
                        ${empty statistics.totalDuration ? 0 : statistics.totalDuration}
                    </span>
                    <span class="stat-label">总时长(分钟)</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">
                        <c:choose>
                            <c:when test="${not empty statistics.avgDuration}">
                                <fmt:formatNumber value="${statistics.avgDuration}" pattern="#0"/>
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="stat-label">平均时长(分钟)</span>
                </div>
            </div>

            <table class="session-table">
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
                                <td colspan="5" style="text-align: center; color: #999; padding: 40px;">
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
                                        <c:if test="${not empty sess.duration}">
                                            ${sess.duration} 分钟
                                        </c:if>
                                        <c:if test="${empty sess.duration}">进行中</c:if>
                                    </td>
                                    <td>
                                        <span class="status-badge ${sess.status}">
                                            <c:choose>
                                                <c:when test="${sess.status == 'ACTIVE'}">进行中</c:when>
                                                <c:when test="${sess.status == 'COMPLETED'}">已完成</c:when>
                                            </c:choose>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>

            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:if test="${currentPage > 1}">
                        <a href="?page=${currentPage - 1}&startDate=${startDate}&endDate=${endDate}">上一页</a>
                    </c:if>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="current">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="?page=${i}&startDate=${startDate}&endDate=${endDate}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}">
                        <a href="?page=${currentPage + 1}&startDate=${startDate}&endDate=${endDate}">下一页</a>
                    </c:if>
                </div>
            </c:if>

            <div class="back-link">
                <a href="${ctx}/study">&larr; 返回学习中心</a>
                &nbsp;|&nbsp;
                <a href="${ctx}/index.jsp">返回首页</a>
            </div>
        </div>
    </div>
</body>
</html>
