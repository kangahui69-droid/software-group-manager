<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤记录 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/attendance.css">
    <style>
        .list-page {
            background: #f5f5f5;
            min-height: 100vh;
            padding: 20px;
        }
        .list-card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
            max-width: 900px;
            margin: 0 auto;
        }
        .list-header {
            padding: 20px;
            border-bottom: 1px solid #eee;
        }
        .list-header h2 {
            margin: 0;
            color: #333;
            font-size: 20px;
        }
        .filter-form {
            padding: 20px;
            background: #fafafa;
        }
        .filter-row {
            display: flex;
            gap: 15px;
            align-items: flex-end;
            flex-wrap: wrap;
        }
        .filter-item {
            flex: 1;
            min-width: 150px;
        }
        .filter-item label {
            display: block;
            font-size: 12px;
            color: #666;
            margin-bottom: 5px;
        }
        .filter-item input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }
        .stats-summary {
            display: flex;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
        }
        .stat-item {
            flex: 1;
            text-align: center;
            padding: 10px;
        }
        .stat-value {
            display: block;
            font-size: 24px;
            font-weight: bold;
        }
        .stat-label {
            display: block;
            font-size: 12px;
            opacity: 0.9;
            margin-top: 5px;
        }
        .attendance-table {
            width: 100%;
            border-collapse: collapse;
        }
        .attendance-table th,
        .attendance-table td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        .attendance-table th {
            background: #f9f9f9;
            font-weight: 600;
            color: #333;
            font-size: 13px;
        }
        .attendance-table td {
            color: #666;
            font-size: 13px;
        }
        .attendance-table tr:hover {
            background: #fafafa;
        }
        .status-badge {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 12px;
        }
        .status-badge.NORMAL { background: #e8f5e9; color: #4caf50; }
        .status-badge.LATE { background: #fff3e0; color: #ff9800; }
        .status-badge.EARLY { background: #fff3e0; color: #ff9800; }
        .status-badge.LEAVE { background: #e3f2fd; color: #2196f3; }
        .status-badge.NONE, .status-badge.MISSING { background: #f5f5f5; color: #999; }
        .pagination {
            padding: 20px;
            display: flex;
            justify-content: center;
            gap: 8px;
        }
        .pagination a,
        .pagination span {
            display: inline-block;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            text-decoration: none;
            color: #666;
            font-size: 13px;
        }
        .pagination a:hover {
            background: #667eea;
            color: #fff;
            border-color: #667eea;
        }
        .pagination .current {
            background: #667eea;
            color: #fff;
            border-color: #667eea;
        }
        .back-link {
            padding: 15px 20px;
            border-top: 1px solid #eee;
        }
        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-size: 14px;
        }
        .back-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="list-page">
        <div class="list-card">
            <div class="list-header">
                <h2>考勤记录</h2>
            </div>

            <form class="filter-form" method="get" action="${ctx}/attendance/list">
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

            <div class="stats-summary">
                <div class="stat-item">
                    <span class="stat-value">${statistics.totalDays}</span>
                    <span class="stat-label">总天数</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${statistics.normalDays}</span>
                    <span class="stat-label">正常</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${statistics.lateDays}</span>
                    <span class="stat-label">迟到</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${statistics.leaveDays}</span>
                    <span class="stat-label">请假</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">
                        <c:choose>
                            <c:when test="${not empty statistics.totalWorkDuration}">
                                <fmt:formatNumber value="${statistics.totalWorkDuration / 60}" pattern="#0.0"/>
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="stat-label">总时长(小时)</span>
                </div>
            </div>

            <table class="attendance-table">
                <thead>
                    <tr>
                        <th>日期</th>
                        <th>签到时间</th>
                        <th>签退时间</th>
                        <th>签到状态</th>
                        <th>签退状态</th>
                        <th>工作时长</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty attendanceList}">
                            <tr>
                                <td colspan="6" style="text-align: center; color: #999; padding: 40px;">
                                    暂无考勤记录
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${attendanceList}" var="att">
                                <tr>
                                    <td><fmt:formatDate value="${att.attendanceDate}" pattern="yyyy-MM-dd"/></td>
                                    <td>
                                        <c:if test="${not empty att.checkInTime}">
                                            <fmt:formatDate value="${att.checkInTime}" pattern="HH:mm:ss"/>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:if test="${not empty att.checkOutTime}">
                                            <fmt:formatDate value="${att.checkOutTime}" pattern="HH:mm:ss"/>
                                        </c:if>
                                    </td>
                                    <td>
                                        <span class="status-badge ${att.checkInStatus}">
                                            <c:choose>
                                                <c:when test="${att.checkInStatus == 'NORMAL'}">正常</c:when>
                                                <c:when test="${att.checkInStatus == 'LATE'}">迟到</c:when>
                                                <c:when test="${att.checkInStatus == 'LEAVE'}">请假</c:when>
                                                <c:when test="${att.checkInStatus == 'MISSING'}">未签退</c:when>
                                                <c:otherwise>未签到</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="status-badge ${att.checkOutStatus}">
                                            <c:choose>
                                                <c:when test="${att.checkOutStatus == 'NORMAL'}">正常</c:when>
                                                <c:when test="${att.checkOutStatus == 'EARLY'}">早退</c:when>
                                                <c:when test="${att.checkOutStatus == 'LEAVE'}">请假</c:when>
                                                <c:otherwise>未签退</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td>
                                        <c:if test="${not empty att.workDuration}">
                                            ${att.workDuration} 分钟
                                        </c:if>
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
                <a href="${ctx}/attendance">&larr; 返回签到首页</a>
            </div>
        </div>
    </div>
</body>
</html>
