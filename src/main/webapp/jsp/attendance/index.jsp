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
    <title>每日签到 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/attendance.css">
</head>
<body>
    <div class="attendance-page">
        <!-- 顶部提示 -->
        <c:if test="${not empty warning}">
            <div class="alert alert-warning">
                <i class="icon">&#9888;</i> ${warning}
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <i class="icon">&#10060;</i> ${error}
            </div>
        </c:if>

        <div class="attendance-card">
            <!-- 日期显示 -->
            <div class="date-header">
                <div class="date-main">
                    <span class="day"><fmt:formatDate value="${nowDate}" pattern="dd"/></span>
                    <div class="date-info">
                        <span class="year-month"><fmt:formatDate value="${nowDate}" pattern="yyyy年MM月"/></span>
                        <span class="weekday"><fmt:formatDate value="${nowDate}" pattern="EEEE"/></span>
                    </div>
                </div>
            </div>

            <!-- 用户信息 -->
            <div class="user-section">
                <div class="user-avatar">
                    <img src="${ctx}/file/avatar/${currentUser.id}"
                         onerror="this.src='${ctx}/images/avatar/default-avatar.svg'"
                         alt="头像">
                </div>
                <div class="user-details">
                    <span class="user-name">${currentUser.name}</span>
                    <span class="user-role">${currentUser.role == 'MEMBER' ? '成员' : '管理员'}</span>
                </div>
            </div>

            <!-- 签到状态 -->
            <div class="status-grid">
                <div class="status-box check-in ${todayAttendance.checkInTime != null ? 'success' : ''}">
                    <div class="status-icon">
                        <c:choose>
                            <c:when test="${todayAttendance.checkInTime != null}">&#10003;</c:when>
                            <c:otherwise>&#9675;</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="status-info">
                        <span class="status-label">签到</span>
                        <span class="status-time" id="checkInTimeDisplay">
                            <c:choose>
                                <c:when test="${todayAttendance.checkInTime != null}">
                                    <fmt:formatDate value="${todayAttendance.checkInTime}" pattern="HH:mm"/>
                                </c:when>
                                <c:otherwise>--:--</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="status-tag ${todayAttendance.checkInStatus}">
                        <c:choose>
                            <c:when test="${todayAttendance.checkInStatus == 'NORMAL'}">正常</c:when>
                            <c:when test="${todayAttendance.checkInStatus == 'LATE'}">迟到</c:when>
                            <c:when test="${todayAttendance.checkInStatus == 'LEAVE'}">请假</c:when>
                            <c:otherwise>未签到</c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="status-box check-out ${todayAttendance.checkOutTime != null ? 'success' : ''}">
                    <div class="status-icon">
                        <c:choose>
                            <c:when test="${todayAttendance.checkOutTime != null}">&#10003;</c:when>
                            <c:otherwise>&#9675;</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="status-info">
                        <span class="status-label">签退</span>
                        <span class="status-time" id="checkOutTimeDisplay">
                            <c:choose>
                                <c:when test="${todayAttendance.checkOutTime != null}">
                                    <fmt:formatDate value="${todayAttendance.checkOutTime}" pattern="HH:mm"/>
                                </c:when>
                                <c:otherwise>--:--</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="status-tag ${todayAttendance.checkOutStatus}">
                        <c:choose>
                            <c:when test="${todayAttendance.checkOutStatus == 'NORMAL'}">正常</c:when>
                            <c:when test="${todayAttendance.checkOutStatus == 'EARLY'}">早退</c:when>
                            <c:when test="${todayAttendance.checkOutStatus == 'LEAVE'}">请假</c:when>
                            <c:otherwise>未签退</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- 工作时长（如果已签退） -->
            <c:if test="${todayAttendance.workDuration != null}">
                <div class="work-duration">
                    <span class="label">今日工作时长</span>
                    <span class="value">${todayAttendance.workDuration} 分钟</span>
                </div>
            </c:if>

            <!-- 操作按钮 -->
            <div class="action-section">
                <button class="btn btn-attendance btn-check-in" id="btnCheckIn"
                    <c:if test="${todayAttendance.checkInTime != null}">disabled</c:if>>
                    <span class="btn-icon">&#128205;</span>
                    <span class="btn-text">签到</span>
                </button>

                <button class="btn btn-attendance btn-check-out" id="btnCheckOut"
                    <c:if test="${todayAttendance.checkInTime == null || todayAttendance.checkOutTime != null}">disabled</c:if>>
                    <span class="btn-icon">&#127987;</span>
                    <span class="btn-text">签退</span>
                </button>
            </div>

            <!-- 提醒信息 -->
            <div class="info-section">
                <div class="info-item">
                    <span class="info-label">工作时间</span>
                    <span class="info-value">09:00 - 18:00</span>
                </div>
                <div class="info-item">
                    <span class="info-label">迟到认定</span>
                    <span class="info-value">09:30 后</span>
                </div>
                <div class="info-item">
                    <span class="info-label">早退认定</span>
                    <span class="info-value">17:30 前</span>
                </div>
            </div>

            <!-- 底部链接 -->
            <div class="footer-links">
                <a href="${ctx}/attendance/list" class="link-item">
                    <span class="link-icon">&#128203;</span>
                    <span>考勤记录</span>
                </a>
                <a href="${ctx}/index.jsp" class="link-item">
                    <span class="link-icon">&#127968;</span>
                    <span>返回首页</span>
                </a>
            </div>
        </div>
    </div>

    <script>
        var ctx = '${ctx}';
    </script>
    <script src="${ctx}/js/attendance.js"></script>
</body>
</html>
