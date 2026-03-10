<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="项目管理" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    项目管理
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/project?action=create" class="btn btn-primary d-none d-sm-inline-block">
                        <i class="bi bi-plus-lg me-2"></i>添加项目
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card mb-3">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/project" class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label">关键词</label>
                        <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索标题/描述/团队名">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">状态</label>
                        <select class="form-select" name="status">
                            <option value="">全部</option>
                            <option value="pending" ${status == 'pending' ? 'selected' : ''}>待审核</option>
                            <option value="approved" ${status == 'approved' ? 'selected' : ''}>已通过</option>
                            <option value="in_progress" ${status == 'in_progress' ? 'selected' : ''}>进行中</option>
                            <option value="completed" ${status == 'completed' ? 'selected' : ''}>已完成</option>
                            <option value="canceled" ${status == 'canceled' ? 'selected' : ''}>已取消</option>
                            <option value="rejected" ${status == 'rejected' ? 'selected' : ''}>已拒绝</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">年份</label>
                        <select class="form-select" name="year">
                            <option value="">全部</option>
                            <option value="2026" ${year == '2026' ? 'selected' : ''}>2026</option>
                            <option value="2025" ${year == '2025' ? 'selected' : ''}>2025</option>
                            <option value="2024" ${year == '2024' ? 'selected' : ''}>2024</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">搜索</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>标题</th>
                            <th>负责人</th>
                            <th>状态</th>
                            <th>年份</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${projects}">
                            <tr>
                                <td>${p.name}</td>
                                <td class="text-muted">${p.leaderName}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status == 'pending'}">
                                            <span class="badge bg-warning text-dark">待审核</span>
                                        </c:when>
                                        <c:when test="${p.status == 'approved'}">
                                            <span class="badge bg-success">已通过</span>
                                        </c:when>
                                        <c:when test="${p.status == 'in_progress'}">
                                            <span class="badge bg-primary">进行中</span>
                                        </c:when>
                                        <c:when test="${p.status == 'completed'}">
                                            <span class="badge bg-success">已完成</span>
                                        </c:when>
                                        <c:when test="${p.status == 'canceled'}">
                                            <span class="badge bg-danger">已取消</span>
                                        </c:when>
                                        <c:when test="${p.status == 'rejected'}">
                                            <span class="badge bg-danger">已拒绝</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary text-white">${p.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${p.year}</td>
                                <td>
                                    <div class="btn-list flex-nowrap">
                                        <a href="${pageContext.request.contextPath}/project?action=edit&id=${p.id}" 
                                           class="btn btn-white btn-sm">编辑</a>
                                        <form action="${pageContext.request.contextPath}/project" method="POST"
                                            onsubmit="return confirm('确定要删除吗？')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit"
                                                class="btn btn-outline-danger btn-sm">删除</button>
                                        </form>
                                        <c:if test="${p.status == 'pending'}">
                                            <form action="${pageContext.request.contextPath}/project"
                                                method="POST" class="ms-1">
                                                <input type="hidden" name="action" value="approve">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <button type="submit" class="btn btn-success btn-sm">批准</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty projects}">
                            <tr>
                                <td colspan="5" class="text-center text-muted">暂无项目记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />
