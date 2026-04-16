<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="学习管理" />
    <jsp:param name="active" value="attendance" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-body-design {
        padding: 0;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-book me-2"></i>学习管理
            </h1>
            <p class="admin-hero-subtitle">查看和管理学习记录</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="row row-cards mb-3">
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="card-body-design text-center">
                        <div class="h1 text-blue mb-1">${empty sessionStats.totalSessions ? 0 : sessionStats.totalSessions}</div>
                        <div class="text-muted">总学习次数</div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="card-body-design text-center">
                        <div class="h1 text-green mb-1">${empty sessionStats.completedSessions ? 0 : sessionStats.completedSessions}</div>
                        <div class="text-muted">已完成</div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="card-body-design text-center">
                        <div class="h1 text-purple mb-1">${empty sessionStats.totalDuration ? 0 : sessionStats.totalDuration}</div>
                        <div class="text-muted">总时长(分钟)</div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="stat-card">
                    <div class="card-body-design text-center">
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

        <div class="card-design">
            <div class="card-body-design p-3">
                <form method="get" action="${ctx}/attendance/manage">
                    <div class="row g-3">
                        <div class="col-md-3">
                            <label class="form-label-design">开始日期</label>
                            <input type="date" name="startDate" class="input-design" value="${startDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label-design">结束日期</label>
                            <input type="date" name="endDate" class="input-design" value="${endDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label-design">选择成员</label>
                            <select name="userId" class="input-design">
                                <option value="">全部成员</option>
                                <c:forEach items="${memberList}" var="member">
                                    <option value="${member.id}" ${selectedUserId == member.id ? 'selected' : ''}>
                                        ${not empty member.name ? member.name : member.username}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="submit" class="btn-brand w-100">查询</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="card-design mt-3">
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>日期</th>
                            <th>成员</th>
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
                                                    <span class="badge-design" style="background: rgba(249, 115, 22, 0.1); color: #ea580c;">进行中</span>
                                                </c:when>
                                                <c:when test="${sess.status == 'COMPLETED'}">
                                                    <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">已完成</span>
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

<jsp:include page="../../common/layout_bottom.jsp" />