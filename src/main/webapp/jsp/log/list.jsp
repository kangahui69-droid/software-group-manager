<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="操作日志" />
</jsp:include>

<style>
    .header-gradient {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border-radius: 16px;
        padding: 32px 24px;
        margin-bottom: 24px;
        color: white;
        box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
    }
    
    .header-title {
        font-size: 24px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 4px;
    }
    
    .header-subtitle {
        font-size: 14px;
        opacity: 0.85;
    }
    
    .search-card {
        background: white;
        border-radius: 14px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        margin-bottom: 20px;
    }
    
    .list-card {
        background: white;
        border-radius: 14px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        overflow: hidden;
    }
    
    .list-header {
        padding: 16px 20px;
        border-bottom: 1px solid #f1f5f9;
        font-weight: 600;
        color: #1e293b;
    }
    
    .status-badge {
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
    }
    
    .status-success {
        background: #dcfce7;
        color: #166534;
    }
    
    .status-primary {
        background: #dbeafe;
        color: #1e40af;
    }
    
    .btn-search {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        border: none;
        border-radius: 10px;
        padding: 12px 24px;
        color: white;
        font-weight: 500;
        transition: all 0.2s ease;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
    }
    
    .btn-search:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
    }
    
    .table > :not(caption) > * > * {
        padding: 14px 16px;
    }
    
    .table thead th {
        background: #f8fafc;
        border-bottom: none;
        font-weight: 600;
        color: #64748b;
        font-size: 13px;
    }
    
    .table tbody tr {
        border-bottom: 1px solid #f1f5f9;
    }
    
    .table tbody tr:hover {
        background: #f8fafc;
    }
    
    .table tbody tr:last-child {
        border-bottom: none;
    }
    
    .pagination .page-link {
        border-radius: 8px;
        margin: 0 2px;
        border: none;
        color: #64748b;
        padding: 8px 14px;
    }
    
    .pagination .page-link:hover {
        background: #e2e8f0;
        color: #3b82f6;
    }
    
    .pagination .page-item.active .page-link {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="header-gradient">
            <div class="header-title">
                <i class="bi bi-journal-text"></i>
                <span>操作日志管理</span>
            </div>
            <div class="header-subtitle">系统操作日志记录</div>
        </div>
        
        <div class="search-card">
            <form method="get" action="${pageContext.request.contextPath}/admin/log/list" class="row g-3">
                <div class="col-lg-3 col-md-4">
                    <input type="text" class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="搜索用户名/描述/IP/请求ID" style="border-radius: 10px; padding: 12px 16px;">
                </div>
                <div class="col-lg-2 col-md-4">
                    <select class="form-select" name="operation" style="border-radius: 10px; padding: 12px;">
                        <option value="">全部</option>
                        <c:forEach var="item" items="${operationOptions}">
                            <option value="${item}" ${operation == item ? 'selected' : ''}>${item}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-lg-2 col-md-4">
                    <select class="form-select" name="module" style="border-radius: 10px; padding: 12px;">
                        <option value="">全部</option>
                        <c:forEach var="item" items="${moduleOptions}">
                            <option value="${item.value}" ${module == item.value ? 'selected' : ''}>${item.value}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-lg-2 col-md-4">
                    <label class="form-label" for="logStartTime">开始（北京时间，含）</label>
                    <input id="logStartTime" type="datetime-local" class="form-control" name="startTime" value="<c:out value='${startTime}'/>" aria-label="开始时间（北京时间，含）">
                </div>
                <div class="col-lg-2 col-md-4">
                    <label class="form-label" for="logEndTime">结束（北京时间，不含）</label>
                    <input id="logEndTime" type="datetime-local" class="form-control" name="endTime" value="<c:out value='${endTime}'/>" aria-label="截止时间（北京时间，不含）">
                </div>
                <div class="col-lg-1 col-md-4 d-flex align-items-end">
                    <button type="submit" class="btn-search w-100">搜索</button>
                </div>
            </form>
        </div>
        
        <div class="list-card">
            <div class="list-header">日志列表</div>
            <div class="table-responsive">
                <table class="table table-vcenter card-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>用户名</th>
                            <th>操作</th>
                            <th>模块</th>
                            <th>描述</th>
                            <th>结果 / 状态</th>
                            <th>耗时</th>
                            <th>请求与目标</th>
                            <th>客户端 / 代理 IP</th>
                            <th>时间</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td>${log.id}</td>
                                <td>${log.username}</td>
                                <td><span class="status-badge status-primary">${log.operation}</span></td>
                                <td>${log.module}</td>
                                <td class="text-muted"><small><c:out value="${log.description}" /></small></td>
                                <td>
                                    <c:out value="${empty log.result ? '—' : log.result}" />
                                    <c:if test="${not empty log.httpStatus}"><br><small>HTTP <c:out value="${log.httpStatus}" /></small></c:if>
                                    <c:if test="${not empty log.errorCode}"><br><small><c:out value="${log.errorCode}" /></small></c:if>
                                </td>
                                <td><c:if test="${not empty log.durationMs}"><c:out value="${log.durationMs}" /> ms</c:if></td>
                                <td class="text-muted">
                                    <c:if test="${not empty log.requestId}"><small>请求 <code><c:out value="${log.requestId}" /></code></small></c:if>
                                    <c:if test="${not empty log.targetType}"><br><small>请求目标 <c:out value="${log.targetType}" /><c:if test="${not empty log.targetId}"> #<c:out value="${log.targetId}" /></c:if></small></c:if>
                                </td>
                                <td class="text-muted">
                                    <c:out value="${log.ipAddress}" />
                                    <c:if test="${not empty log.proxyIp}"><br><small>代理 <c:out value="${log.proxyIp}" /></small></c:if>
                                </td>
                                <td class="text-muted">
                                    <c:out value="${log.createdAtBeijing}" />
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="10" class="text-center text-muted py-8">暂无日志记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            <c:if test="${totalPages > 0}">
                <div class="card-footer d-flex align-items-center flex-wrap" style="background: #f8fafc; border-top: none; padding: 16px 20px;">
                    <div class="d-flex align-items-center me-3">
                        <span class="text-muted me-2">每页</span>
                        <select class="form-select form-select-sm" style="width: auto; border-radius: 8px;" onchange="return navigateToLogPage(1, this.value)">
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
                                <a class="page-link" href="?page=${currentPage - 1}&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(${currentPage - 1}, ${pageSize})">上一页</a>
                            </li>
                        </c:if>
                        <c:choose>
                            <c:when test="${totalPages <= 10}">
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(${i}, ${pageSize})">${i}</a>
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
                                    <a class="page-link" href="?page=1&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(1, ${pageSize})">1</a>
                                </li>
                                <c:if test="${startPage > 2}">
                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                </c:if>
                                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(${i}, ${pageSize})">${i}</a>
                                    </li>
                                </c:forEach>
                                <c:if test="${endPage < totalPages - 1}">
                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                </c:if>
                                <li class="page-item ${currentPage == totalPages ? 'active' : ''}">
                                    <a class="page-link" href="?page=${totalPages}&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(${totalPages}, ${pageSize})">${totalPages}</a>
                                </li>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage + 1}&amp;pageSize=${pageSize}" onclick="return navigateToLogPage(${currentPage + 1}, ${pageSize})">下一页</a>
                            </li>
                        </c:if>
                    </ul>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script>
function navigateToLogPage(page, pageSize) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    url.searchParams.set('pageSize', pageSize);
    window.location.assign(url.toString());
    return false;
}
</script>
<jsp:include page="../../jsp/common/layout_bottom.jsp" />
