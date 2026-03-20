<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="操作日志" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    操作日志
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card mb-3">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/admin/log/list" class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">关键词</label>
                        <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索用户名/描述/IP">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">操作类型</label>
                        <select class="form-select" name="operation">
                            <option value="">全部</option>
                            <option value="POST" ${operation == 'POST' ? 'selected' : ''}>POST</option>
                            <option value="GET" ${operation == 'GET' ? 'selected' : ''}>GET</option>
                            <option value="DELETE" ${operation == 'DELETE' ? 'selected' : ''}>DELETE</option>
                            <option value="PUT" ${operation == 'PUT' ? 'selected' : ''}>PUT</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">模块</label>
                        <select class="form-select" name="module">
                            <option value="">全部</option>
                            <option value="用户" ${module == '用户' ? 'selected' : ''}>用户</option>
                            <option value="新闻" ${module == '新闻' ? 'selected' : ''}>新闻</option>
                            <option value="项目" ${module == '项目' ? 'selected' : ''}>项目</option>
                            <option value="活动" ${module == '活动' ? 'selected' : ''}>活动</option>
                            <option value="奖项" ${module == '奖项' ? 'selected' : ''}>奖项</option>
                            <option value="其他" ${module == '其他' ? 'selected' : ''}>其他</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">时间范围</label>
                        <select class="form-select" name="dateRange">
                            <option value="">全部</option>
                            <option value="1" ${dateRange == '1' ? 'selected' : ''}>最近1天</option>
                            <option value="7" ${dateRange == '7' ? 'selected' : ''}>最近7天</option>
                            <option value="30" ${dateRange == '30' ? 'selected' : ''}>最近30天</option>
                            <option value="90" ${dateRange == '90' ? 'selected' : ''}>最近90天</option>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex align-items-end">
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
                            <th>ID</th>
                            <th>用户名</th>
                            <th>操作</th>
                            <th>模块</th>
                            <th>描述</th>
                            <th>IP地址</th>
                            <th>时间</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td>${log.id}</td>
                                <td>${log.username}</td>
                                <td><span class="badge bg-blue-lt">${log.operation}</span></td>
                                <td>${log.module}</td>
                                <td class="text-muted"><small>${log.description}</small></td>
                                <td class="text-muted">${log.ipAddress}</td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${log.createdAt}"
                                        pattern="yyyy-MM-dd HH:mm:ss" />
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="7" class="text-center text-muted">暂无日志记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
                    <c:if test="${totalPages > 0}">
                        <div class="card-footer d-flex align-items-center flex-wrap">
                            <div class="d-flex align-items-center me-3">
                                <span class="text-muted me-2">每页</span>
                                <select class="form-select form-select-sm" style="width: auto;" onchange="window.location.href='?page=1&pageSize='+this.value">
                                    <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                                    <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                                    <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                                    <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
                                </select>
                                <span class="text-muted ms-2">条，共 ${totalLogs} 条</span>
                            </div>
                            <ul class="pagination m-0 ms-auto">
                                <c:if test="${currentPage > 1}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}">上一页</a>
                                    </li>
                                </c:if>
                                <c:choose>
                                    <c:when test="${totalPages <= 10}">
                                        <c:forEach var="i" begin="1" end="${totalPages}">
                                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="?page=${i}&pageSize=${pageSize}">${i}</a>
                                            </li>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="startPage" value="${currentPage - 2}"/>
                                        <c:set var="endPage" value="${currentPage + 2}"/>
                                        <c:if test="${startPage < 2}">
                                            <c:set var="startPage" value="2"/>
                                            <c:set var="endPage" value="5"/>
                                        </c:if>
                                        <c:if test="${endPage > totalPages - 1}">
                                            <c:set var="endPage" value="${totalPages - 1}"/>
                                            <c:set var="startPage" value="${totalPages - 4}"/>
                                        </c:if>
                                        <c:if test="${startPage < 2}">
                                            <c:set var="startPage" value="2"/>
                                        </c:if>
                                        <li class="page-item ${currentPage == 1 ? 'active' : ''}">
                                            <a class="page-link" href="?page=1&pageSize=${pageSize}">1</a>
                                        </li>
                                        <c:if test="${startPage > 2}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:if>
                                        <c:forEach var="i" begin="${startPage}" end="${endPage}">
                                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="?page=${i}&pageSize=${pageSize}">${i}</a>
                                            </li>
                                        </c:forEach>
                                        <c:if test="${endPage < totalPages - 1}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:if>
                                        <li class="page-item ${currentPage == totalPages ? 'active' : ''}">
                                            <a class="page-link" href="?page=${totalPages}&pageSize=${pageSize}">${totalPages}</a>
                                        </li>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${currentPage < totalPages}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}">下一页</a>
                                    </li>
                                </c:if>
                            </ul>
                        </div>
                    </c:if>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />