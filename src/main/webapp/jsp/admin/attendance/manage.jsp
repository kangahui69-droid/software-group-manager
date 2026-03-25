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
    <title>考勤管理 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/attendance.css">
    <style>
        body {
            margin: 0;
            padding: 0;
            background: #f5f5f5;
        }
        .manage-container {
            max-width: 1200px;
            margin: 20px auto;
            padding: 0 20px;
        }
        .manage-card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
        }
        .manage-header {
            padding: 20px;
            border-bottom: 1px solid #eee;
        }
        .manage-header h2 {
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
        .filter-item input,
        .filter-item select {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }
        .table-wrapper {
            overflow-x: auto;
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
            white-space: nowrap;
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
    </style>
</head>
<body>
    <div class="manage-container">
        <div class="manage-card">
            <div class="manage-header">
                <h2>考勤管理</h2>
            </div>

            <form class="filter-form" method="get" action="${ctx}/attendance/manage">
                <div class="filter-row">
                    <div class="filter-item">
                        <label>开始日期</label>
                        <input type="date" name="startDate" value="${startDate}">
                    </div>
                    <div class="filter-item">
                        <label>结束日期</label>
                        <input type="date" name="endDate" value="${endDate}">
                    </div>
                    <div class="filter-item">
                        <label>用户ID</label>
                        <input type="number" name="userId" placeholder="输入用户ID" value="${selectedUserId}">
                    </div>
                    <button type="submit" class="btn btn-primary">查询</button>
                </div>
            </form>

            <div class="table-wrapper">
                <table class="attendance-table">
                    <thead>
                        <tr>
                            <th>日期</th>
                            <th>用户</th>
                            <th>签到时间</th>
                            <th>签退时间</th>
                            <th>签到状态</th>
                            <th>签退状态</th>
                            <th>工作时长</th>
                            <th>备注</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty attendanceList}">
                                <tr>
                                    <td colspan="8" style="text-align: center; color: #999; padding: 40px;">
                                        暂无考勤记录
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${attendanceList}" var="att">
                                    <tr>
                                        <td><fmt:formatDate value="${att.attendanceDate}" pattern="yyyy-MM-dd"/></td>
                                        <td>${att.userName}</td>
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
                                        <td>${att.remark}</td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <div class="pagination">
                <c:if test="${currentPage > 1}">
                    <a href="?page=${currentPage - 1}&startDate=${startDate}&endDate=${endDate}&userId=${selectedUserId}">上一页</a>
                </c:if>
                <c:if test="${currentPage < totalPages}">
                    <a href="?page=${currentPage + 1}&startDate=${startDate}&endDate=${endDate}&userId=${selectedUserId}">下一页</a>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
