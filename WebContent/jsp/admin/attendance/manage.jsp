<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="考勤管理" />
    <jsp:param name="active" value="attendance" />
</jsp:include>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            <i class="bi bi-clock-history me-2"></i>考勤管理
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${ctx}/admin/dashboard" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>返回首页
                        </a>
                    </div>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <!-- 标签页导航 -->
            <ul class="nav nav-tabs mb-3" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link ${activeTab == 'attendance' || empty activeTab ? 'active' : ''}" 
                            data-bs-toggle="tab" data-bs-target="#attendance-tab" type="button" role="tab">
                        <i class="bi bi-calendar-check me-1"></i>签到记录
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link ${activeTab == 'study' ? 'active' : ''}" 
                            data-bs-toggle="tab" data-bs-target="#study-tab" type="button" role="tab">
                        <i class="bi bi-book me-1"></i>学习记录
                    </button>
                </li>
            </ul>

            <div class="tab-content">
                <!-- 签到记录 -->
                <div class="tab-pane fade ${activeTab == 'attendance' || empty activeTab ? 'show active' : ''}" 
                     id="attendance-tab" role="tabpanel">
                    <div class="card">
                        <div class="card-body">
                            <form method="get" action="${ctx}/attendance/manage">
                                <input type="hidden" name="tab" value="attendance"/>
                                <div class="row g-3">
                                    <div class="col-md-3">
                                        <label class="form-label">开始日期</label>
                                        <input type="date" name="startDate" class="form-control" value="${startDate}">
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">结束日期</label>
                                        <input type="date" name="endDate" class="form-control" value="${endDate}">
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">用户ID</label>
                                        <input type="number" name="userId" class="form-control" placeholder="输入用户ID" value="${selectedUserId}">
                                    </div>
                                    <div class="col-md-3 d-flex align-items-end">
                                        <button type="submit" class="btn btn-primary me-2">查询</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="card mt-3">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table table-striped">
                                <thead>
                                    <tr>
                                        <th>日期</th>
                                        <th>用户</th>
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
                                                <td colspan="7" class="text-center text-muted py-4">
                                                    暂无签到记录
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
                                                        <c:choose>
                                                            <c:when test="${att.checkInStatus == 'NORMAL'}"><span class="badge bg-green">正常</span></c:when>
                                                            <c:when test="${att.checkInStatus == 'LATE'}"><span class="badge bg-yellow">迟到</span></c:when>
                                                            <c:when test="${att.checkInStatus == 'LEAVE'}"><span class="badge bg-blue">请假</span></c:when>
                                                            <c:when test="${att.checkInStatus == 'MISSING'}"><span class="badge bg-secondary">未签退</span></c:when>
                                                            <c:otherwise><span class="badge bg-secondary">未签到</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${att.checkOutStatus == 'NORMAL'}"><span class="badge bg-green">正常</span></c:when>
                                                            <c:when test="${att.checkOutStatus == 'EARLY'}"><span class="badge bg-orange">早退</span></c:when>
                                                            <c:when test="${att.checkOutStatus == 'LEAVE'}"><span class="badge bg-blue">请假</span></c:when>
                                                            <c:otherwise><span class="badge bg-secondary">未签退</span></c:otherwise>
                                                        </c:choose>
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
                        </div>
                    </div>
                </div>

                <!-- 学习记录 -->
                <div class="tab-pane fade ${activeTab == 'study' ? 'show active' : ''}" 
                     id="study-tab" role="tabpanel">
                    <!-- 统计卡片 -->
                    <div class="row row-cards mb-3">
                        <div class="col-sm-6 col-lg-3">
                            <div class="card">
                                <div class="card-body text-center">
                                    <div class="h1 text-blue mb-1">${empty sessionStats.totalSessions ? 0 : sessionStats.totalSessions}</div>
                                    <div class="text-muted">总学习次数</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="card">
                                <div class="card-body text-center">
                                    <div class="h1 text-green mb-1">${empty sessionStats.completedSessions ? 0 : sessionStats.completedSessions}</div>
                                    <div class="text-muted">已完成</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="card">
                                <div class="card-body text-center">
                                    <div class="h1 text-purple mb-1">${empty sessionStats.totalDuration ? 0 : sessionStats.totalDuration}</div>
                                    <div class="text-muted">总时长(分钟)</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="card">
                                <div class="card-body text-center">
                                    <div class="h1 text-orange mb-1">
                                        <c:choose>
                                            <c:when test="${not empty sessionStats.avgDuration}">
                                                <fmt:formatNumber value="${sessionStats.avgDuration}" pattern="#0"/>
                                            </c:when>
                                            <c:otherwise>0</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="text-muted">平均时长(分钟)</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-body">
                            <form method="get" action="${ctx}/attendance/manage">
                                <input type="hidden" name="tab" value="study"/>
                                <div class="row g-3">
                                    <div class="col-md-3">
                                        <label class="form-label">开始日期</label>
                                        <input type="date" name="startDate" class="form-control" value="${startDate}">
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">结束日期</label>
                                        <input type="date" name="endDate" class="form-control" value="${endDate}">
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">用户ID</label>
                                        <input type="number" name="userId" class="form-control" placeholder="输入用户ID" value="${selectedUserId}">
                                    </div>
                                    <div class="col-md-3 d-flex align-items-end">
                                        <button type="submit" class="btn btn-primary me-2">查询</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="card mt-3">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table table-striped">
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
                                                <td colspan="6" class="text-center text-muted py-4">
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
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />