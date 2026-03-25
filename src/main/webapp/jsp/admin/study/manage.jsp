<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学习管理 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/study.css">
    <style>
        body { margin: 0; padding: 0; background: #f5f5f5; }
        .manage-container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
        .manage-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .manage-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
        .manage-header h2 { margin: 0; color: #333; font-size: 20px; }
        .total-count { color: #666; font-size: 14px; }

        /* 统计数据卡片 */
        .stats-cards { display: flex; gap: 15px; padding: 20px; background: #fafafa; flex-wrap: wrap; }
        .stat-card { flex: 1; min-width: 150px; background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); text-align: center; }
        .stat-card-value { font-size: 28px; font-weight: bold; color: #11998e; }
        .stat-card-label { font-size: 13px; color: #666; margin-top: 5px; }

        .filter-form { padding: 20px; background: #fafafa; }
        .filter-row { display: flex; gap: 15px; align-items: flex-end; flex-wrap: wrap; }
        .filter-item { flex: 1; min-width: 150px; }
        .filter-item label { display: block; font-size: 12px; color: #666; margin-bottom: 5px; }
        .filter-item input, .filter-item select { width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
        .quick-btns { display: flex; gap: 10px; margin-bottom: 10px; }
        .quick-btn { padding: 5px 12px; font-size: 12px; border: 1px solid #ddd; border-radius: 4px; background: #fff; color: #666; cursor: pointer; }
        .quick-btn:hover { background: #667eea; color: #fff; border-color: #667eea; }
        .table-wrapper { overflow-x: auto; }
        .session-table { width: 100%; border-collapse: collapse; }
        .session-table th, .session-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; white-space: nowrap; }
        .session-table th { background: #f9f9f9; font-weight: 600; color: #333; font-size: 13px; }
        .session-table td { color: #666; font-size: 13px; }
        .session-table tr:hover { background: #fafafa; }
        .status-badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; }
        .status-badge.ACTIVE { background: #fff3e0; color: #ff9800; }
        .status-badge.COMPLETED { background: #e8f5e9; color: #4caf50; }
        .pagination { padding: 20px; display: flex; justify-content: space-between; align-items: center; }
        .pagination-info { color: #666; font-size: 13px; }
        .pagination-btns { display: flex; gap: 8px; }
        .pagination a { display: inline-block; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; text-decoration: none; color: #666; font-size: 13px; }
        .pagination a:hover { background: #667eea; color: #fff; border-color: #667eea; }
        .pagination a.disabled { background: #f5f5f5; color: #999; cursor: not-allowed; }
        .back-link { padding: 15px 20px; border-top: 1px solid #eee; }
        .back-link a { color: #667eea; text-decoration: none; font-size: 14px; }
    </style>
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
</head>
<body>
    <div class="manage-container">
        <div class="manage-card">
            <div class="manage-header">
                <h2>学习管理</h2>
                <span class="total-count">共 ${empty totalCount ? 0 : totalCount} 条记录</span>
            </div>

            <!-- 统计数据卡片 -->
            <div class="stats-cards">
                <div class="stat-card">
                    <div class="stat-card-value">${statistics.totalSessions}</div>
                    <div class="stat-card-label">总学习次数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-value">${statistics.completedSessions}</div>
                    <div class="stat-card-label">已完成</div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-value">${empty statistics.totalDuration ? 0 : statistics.totalDuration}</div>
                    <div class="stat-card-label">总时长(分钟)</div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-value">
                        <c:choose>
                            <c:when test="${not empty statistics.avgDuration}">
                                <fmt:formatNumber value="${statistics.avgDuration}" pattern="#0"/>
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="stat-card-label">平均时长(分钟)</div>
                </div>
            </div>

            <form class="filter-form" method="get" action="${ctx}/study/manage">
                <div class="quick-btns">
                    <button type="button" class="quick-btn" onclick="setQuickDate('today')">今天</button>
                    <button type="button" class="quick-btn" onclick="setQuickDate('week')">本周</button>
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
                    <div class="filter-item">
                        <label>选择成员</label>
                        <select name="userId">
                            <option value="">全部成员</option>
                            <c:forEach items="${userList}" var="user">
                                <option value="${user.id}" ${selectedUserId == user.id ? 'selected' : ''}>${user.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">查询</button>
                </div>
            </form>

            <div class="table-wrapper">
                <table class="session-table">
                    <thead>
                        <tr>
                            <th>日期</th>
                            <th>用户</th>
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
                                    <td colspan="6" style="text-align: center; color: #999; padding: 40px;">
                                        暂无学习记录
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${sessionList}" var="sess">
                                    <tr>
                                        <td><fmt:formatDate value="${sess.sessionDate}" pattern="yyyy-MM-dd"/></td>
                                        <td>${sess.userName}</td>
                                        <td><fmt:formatDate value="${sess.checkInTime}" pattern="HH:mm:ss"/></td>
                                        <td>
                                            <c:if test="${not empty sess.checkOutTime}">
                                                <fmt:formatDate value="${sess.checkOutTime}" pattern="HH:mm:ss"/>
                                            </c:if>
                                            <c:if test="${empty sess.checkOutTime}">--</c:if>
                                        </td>
                                        <td>
                                            <c:if test="${not empty sess.duration}">${sess.duration} 分钟</c:if>
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
            </div>

            <div class="pagination">
                <div class="pagination-info">
                    第 ${currentPage} / ${totalPages} 页
                </div>
                <div class="pagination-btns">
                    <c:choose>
                        <c:when test="${currentPage > 1}">
                            <a href="?page=${currentPage - 1}&startDate=${startDate}&endDate=${endDate}&userId=${selectedUserId}">上一页</a>
                        </c:when>
                        <c:otherwise>
                            <a class="disabled">上一页</a>
                        </c:otherwise>
                    </c:choose>

                    <c:choose>
                        <c:when test="${currentPage < totalPages}">
                            <a href="?page=${currentPage + 1}&startDate=${startDate}&endDate=${endDate}&userId=${selectedUserId}">下一页</a>
                        </c:when>
                        <c:otherwise>
                            <a class="disabled">下一页</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="back-link">
                <a href="${ctx}/admin/dashboard">&larr; 返回管理员首页</a>
            </div>
        </div>
    </div>
</body>
</html>
