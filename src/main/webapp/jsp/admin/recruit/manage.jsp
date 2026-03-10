<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
            <jsp:include page="../../common/layout_top.jsp">
                <jsp:param name="title" value="招新管理" />
            </jsp:include>

            <div class="page-header d-print-none">
                <div class="container-xl">
                    <div class="row g-2 align-items-center">
                        <div class="col">
                            <h2 class="page-title">
                                招新报名管理
                            </h2>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-body">
                <div class="container-xl">
                    <!-- 错误信息显示 -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                            ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>
                    <div class="card mb-3">
                        <div class="card-body">
                            <form method="get" action="${pageContext.request.contextPath}/admin/recruit/manage" class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label">关键词</label>
                                    <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索姓名/学号/手机/邮箱">
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">年份</label>
                                    <select class="form-select" name="year">
                                        <option value="">全部</option>
                                        <c:forEach var="y" items="${years}">
                                            <option value="${y}" ${selectedYear==y ? 'selected' : ''}>${y}年</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">状态</label>
                                    <select class="form-select" name="status">
                                        <option value="">全部</option>
                                        <option value="1" ${status == '1' ? 'selected' : ''}>待审核</option>
                                        <option value="2" ${status == '2' ? 'selected' : ''}>已通过</option>
                                        <option value="0" ${status == '0' ? 'selected' : ''}>已拒绝</option>
                                    </select>
                                </div>
                                <div class="col-md-3 d-flex align-items-end">
                                    <button type="submit" class="btn btn-primary w-100">搜索</button>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">报名列表</h3>
                        </div>
                        <div class="table-responsive">
                            <table class="table card-table table-vcenter text-nowrap datatable">
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
                                                        <span class="badge bg-warning text-dark">待处理</span>
                                                    </c:when>
                                                    <c:when test="${app.status == 2}">
                                                        <span class="badge bg-success">已通过</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger">已拒绝</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${app.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                            </td>
                                            <td class="text-end">
                                                <div class="btn-list flex-nowrap justify-content-end">
                                                    <a href="review?id=${app.id}" class="btn btn-white btn-sm">查看详情</a>
                                                    <form action="manage" method="POST" style="display:inline;"
                                                        onsubmit="return confirm('确定要删除吗？')">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="id" value="${app.id}">
                                                        <button type="submit" class="btn btn-danger btn-sm">删除</button>
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