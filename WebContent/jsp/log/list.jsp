<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .search-card { background: var(--bg-white); border-radius: var(--radius-comfortable); box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); padding: 20px; }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-blue-lt { background: #dbeafe; color: #1e40af; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .pagination-design { display: flex; list-style: none; padding: 0; margin: 0; gap: 4px; align-items: center; }
    .pagination-design li a, .pagination-design li span { display: flex; align-items: center; justify-content: center; padding: 8px 12px; border-radius: var(--radius-standard); transition: all 0.2s ease; color: var(--text-dark); text-decoration: none; font-weight: 500; }
    .pagination-design li a:hover { background: var(--bg-light-gray); }
    .pagination-design li.active span { background: var(--brand-blue); color: white; }
    .pagination-design li.disabled span { color: var(--text-muted); cursor: not-allowed; }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    操作日志
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="search-card mb-4">
            <form method="get" action="${pageContext.request.contextPath}/admin/log/list" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">关键词</label>
                    <input type="text" class="form-control input-design" name="keyword" value="${keyword}" placeholder="搜索用户名/描述/IP">
                </div>
                <div class="col-md-2">
                    <label class="form-label">操作类型</label>
                    <select class="form-select input-design" name="operation">
                        <option value="">全部</option>
                        <option value="POST" ${operation == 'POST' ? 'selected' : ''}>POST</option>
                        <option value="GET" ${operation == 'GET' ? 'selected' : ''}>GET</option>
                        <option value="DELETE" ${operation == 'DELETE' ? 'selected' : ''}>DELETE</option>
                        <option value="PUT" ${operation == 'PUT' ? 'selected' : ''}>PUT</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">模块</label>
                    <select class="form-select input-design" name="module">
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
                    <select class="form-select input-design" name="dateRange">
                        <option value="">全部</option>
                        <option value="1" ${dateRange == '1' ? 'selected' : ''}>最近1天</option>
                        <option value="7" ${dateRange == '7' ? 'selected' : ''}>最近7天</option>
                        <option value="30" ${dateRange == '30' ? 'selected' : ''}>最近30天</option>
                        <option value="90" ${dateRange == '90' ? 'selected' : ''}>最近90天</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-brand w-100">搜索</button>
                </div>
            </form>
        </div>
        
        <div class="card-design">
            <div class="table-responsive">
                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
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
                                <td><span class="badge-design badge-blue-lt">${log.operation}</span></td>
                                <td>${log.module}</td>
                                <td class="text-muted"><small>${log.description}</small></td>
                                <td class="text-muted">${log.ipAddress}</td>
                                <td class="text-muted">
                                    <fmt:formatDate value="${log.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" />
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-5">暂无日志记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            <c:if test="${totalPages > 0}">
                <div class="d-flex align-items-center flex-wrap p-4 border-top" style="border-color: var(--border-light);">
                    <div class="d-flex align-items-center me-3">
                        <span class="text-muted me-2">每页</span>
                        <select class="form-select input-design" style="width: auto; padding: 6px 12px;" onchange="window.location.href='?page=1&pageSize='+this.value">
                            <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                            <option value="100" ${pageSize == 100 ? 'selected' : ''}>100</option>
                        </select>
                        <span class="text-muted ms-2">条，共 ${totalLogs} 条</span>
                    </div>
                    <ul class="pagination-design m-0 ms-auto">
                        <c:if test="${currentPage > 1}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}">上一页</a>
                            </li>
                        </c:if>
                        <c:choose>
                            <c:when test="${totalPages <= 10}">
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="${i == currentPage ? 'active' : ''}">
                                        <span ${i == currentPage ? '' : 'href="?page='.concat(i).concat('&pageSize=').concat(pageSize).concat('"')}>${i}</span>
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
                                <li class="${currentPage == 1 ? 'active' : ''}">
                                    <span ${currentPage == 1 ? '' : 'href="?page=1&pageSize='.concat(pageSize).concat('"')}>1</span>
                                </li>
                                <c:if test="${startPage > 2}">
                                    <li class="disabled"><span>...</span></li>
                                </c:if>
                                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                                    <li class="${i == currentPage ? 'active' : ''}">
                                        <span ${i == currentPage ? '' : 'href="?page='.concat(i).concat('&pageSize=').concat(pageSize).concat('"')}>${i}</span>
                                    </li>
                                </c:forEach>
                                <c:if test="${endPage < totalPages - 1}">
                                    <li class="disabled"><span>...</span></li>
                                </c:if>
                                <li class="${currentPage == totalPages ? 'active' : ''}">
                                    <span ${currentPage == totalPages ? '' : 'href="?page='.concat(totalPages).concat('&pageSize=').concat(pageSize).concat('"')}>${totalPages}</span>
                                </li>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${currentPage < totalPages}">
                            <li>
                                <a href="?page=${currentPage + 1}&pageSize=${pageSize}">下一页</a>
                            </li>
                        </c:if>
                    </ul>
                </div>
            </c:if>
        </div>
    </div>
</div>