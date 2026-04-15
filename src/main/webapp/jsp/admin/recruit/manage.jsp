<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="招新管理" />
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
                <i class="bi bi-clipboard-check me-2"></i>招新报名管理
            </h1>
            <p class="admin-hero-subtitle">管理招新申请</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <div class="card-design mb-3">
            <div class="card-body-design">
                <form method="get" action="${pageContext.request.contextPath}/admin/recruit/manage" class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label-design">关键词</label>
                        <input type="text" class="input-design" name="keyword" value="${keyword}" placeholder="搜索姓名/学号/手机/邮箱">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label-design">年份</label>
                        <select class="input-design" name="year">
                            <option value="">全部</option>
                            <c:forEach var="y" items="${years}">
                                <option value="${y}" ${selectedYear==y ? 'selected' : ''}>${y}年</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label-design">状态</label>
                        <select class="input-design" name="status">
                            <option value="">全部</option>
                            <option value="1" ${status == '1' ? 'selected' : ''}>待审核</option>
                            <option value="2" ${status == '2' ? 'selected' : ''}>已通过</option>
                            <option value="0" ${status == '0' ? 'selected' : ''}>已拒绝</option>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex align-items-end">
                        <button type="submit" class="btn-brand w-100">搜索</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">报名列表</h3>
            </div>
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>姓名</th>
                            <th>学号</th>
                            <th>专业班级</th>
                            <th>手机号</th>
                            <th>状态</th>
                            <th>提交时间</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="app" items="${applicationList}">
                            <tr>
                                <td>${app.name}</td>
                                <td>${app.studentId}</td>
                                <td>${app.major} / ${app.grade}</td>
                                <td>${app.phone}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${app.status == 1}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #d97706;">待处理</span>
                                        </c:when>
                                        <c:when test="${app.status == 2}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">已通过</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">已拒绝</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <fmt:formatDate value="${app.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                </td>
                                <td class="text-end">
                                    <div class="btn-list flex-nowrap justify-content-end">
                                        <a href="review?id=${app.id}" class="btn-sm-brand">查看详情</a>
                                        <form action="manage" method="POST" style="display:inline;"
                                            onsubmit="return confirm('确定要删除吗？')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${app.id}">
                                            <button type="submit" class="btn-danger-design btn-sm">删除</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty applicationList}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">暂无申请记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />