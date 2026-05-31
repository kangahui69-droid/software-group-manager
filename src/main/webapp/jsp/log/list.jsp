<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
                <div class="col-md-4">
                    <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索用户名/描述/IP" style="border-radius: 10px; padding: 12px 16px;">
                </div>
                <div class="col-md-2">
                    <select class="form-select" name="operation" style="border-radius: 10px; padding: 12px;">
                        <option value="">全部</option>
                        <option value="POST" ${operation == 'POST' ? 'selected' : ''}>POST</option>
                        <option value="GET" ${operation == 'GET' ? 'selected' : ''}>GET</option>
                        <option value="DELETE" ${operation == 'DELETE' ? 'selected' : ''}>DELETE</option>
                        <option value="PUT" ${operation == 'PUT' ? 'selected' : ''}>PUT</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <select class="form-select" name="module" style="border-radius: 10px; padding: 12px;">
                        <option value="">全部</option>
                        <option value="用户" ${module == '用户' ? 'selected' : ''}>用户</option>
                        <option value="新闻" ${module == '新闻' ? 'selected' : ''}>新闻</option>
                        <option value="项目" ${module == '项目' ? 'selected' : ''}>项目</option>
                        <option value="活动" ${module == '活动' ? 'selected' : ''}>活动</option>
                        <option value="奖项" ${module == '奖项' ? 'selected' : ''}>奖项</option>
                        <option value="其他" ${module == '其他' ? 'selected' : ''}>其他</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex align-items-end">
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
                            <th>IP地址</th>
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
                                <td colspan="7" class="text-center text-muted py-8">暂无日志记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            <c:if test="${totalPages > 0}">
                <div class="card-footer d-flex align-items-center flex-wrap" style="background: #f8fafc; border-top: none; padding: 16px 20px;">
                    <div class="d-flex align-items-center me-3">
                        <span class="text-muted me-2">每页</span>
                        <select class="form-select form-select-sm" style="width: auto; border-radius: 8px;" onchange="window.location.href='?page=1&pageSize='+this.value">
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