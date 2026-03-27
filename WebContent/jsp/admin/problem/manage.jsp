<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="问题管理" />
    <jsp:param name="active" value="problem" />
</jsp:include>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            <i class="bi bi-gear me-2"></i>问题管理
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-primary">
                            <i class="bi bi-house me-1"></i>返回首页
                        </a>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <ul class="nav nav-tabs nav-fill" role="tablist">
                        <li class="nav-item">
                            <a class="nav-link active" data-bs-toggle="tab" href="#tab-verified" role="tab">
                                <i class="bi bi-check-circle me-1"></i>属实
                                <span class="badge bg-danger ms-1">${verifiedCount}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-bs-toggle="tab" href="#tab-invalid" role="tab">
                                <i class="bi bi-x-circle me-1"></i>不属实
                                <span class="badge bg-success ms-1">${invalidCount}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-bs-toggle="tab" href="#tab-unverified" role="tab">
                                <i class="bi bi-question-circle me-1"></i>待确认
                                <span class="badge bg-warning ms-1">${unverifiedCount}</span>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="card-body">
                    <div class="tab-content">
                        <!-- 属实 Tab -->
                        <div class="tab-pane fade show active" id="tab-verified" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-vcenter card-table table-striped">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>问题标题</th>
                                            <th>报告者</th>
                                            <th>处理状态</th>
                                            <th>提交时间</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty verifiedList}">
                                                <tr>
                                                    <td colspan="6" class="text-center text-muted py-4">暂无属实问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${verifiedList}">
                                                    <tr id="row-${report.id}">
                                                        <td>${report.id}</td>
                                                        <td>
                                                            <a href="javascript:void(0)" onclick="showDetail(${report.id})">${report.title}</a>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${report.reporterType == 'GUEST'}">
                                                                    ${report.reporterName != null ? report.reporterName : '游客'}(游客)
                                                                </c:when>
                                                                <c:when test="${report.reporterType == 'MEMBER'}">
                                                                    ${report.userName != null ? report.userName : report.reporterName}(成员)
                                                                </c:when>
                                                                <c:when test="${report.reporterType == 'ADMIN'}">
                                                                    ${report.userName != null ? report.userName : report.reporterName}(管理员)
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${report.status == 'SOLVED'}">
                                                                    <span class="badge bg-success">已修改</span>
                                                                </c:when>
                                                                <c:when test="${report.status == 'SOLVING'}">
                                                                    <span class="badge bg-info">正在修改</span>
                                                                </c:when>
                                                                <c:when test="${report.status == 'PENDING'}">
                                                                    <span class="badge bg-warning">待修改</span>
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <button class="btn btn-sm btn-primary" onclick="showDetail(${report.id})">查看</button>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- 不属实 Tab -->
                        <div class="tab-pane fade" id="tab-invalid" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-vcenter card-table table-striped">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>问题标题</th>
                                            <th>报告者</th>
                                            <th>提交时间</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty invalidList}">
                                                <tr>
                                                    <td colspan="5" class="text-center text-muted py-4">暂无不属实问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${invalidList}">
                                                    <tr>
                                                        <td>${report.id}</td>
                                                        <td>
                                                            <a href="javascript:void(0)" onclick="showDetail(${report.id})">${report.title}</a>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${report.reporterType == 'GUEST'}">
                                                                    ${report.reporterName != null ? report.reporterName : '游客'}(游客)
                                                                </c:when>
                                                                <c:when test="${report.reporterType == 'MEMBER'}">
                                                                    ${report.userName != null ? report.userName : report.reporterName}(成员)
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <button class="btn btn-sm btn-primary" onclick="showDetail(${report.id})">查看</button>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- 待确认 Tab -->
                        <div class="tab-pane fade" id="tab-unverified" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-vcenter card-table table-striped">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>问题标题</th>
                                            <th>报告者</th>
                                            <th>提交时间</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty unverifiedList}">
                                                <tr>
                                                    <td colspan="5" class="text-center text-muted py-4">暂无待确认问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${unverifiedList}">
                                                    <tr>
                                                        <td>${report.id}</td>
                                                        <td>
                                                            <a href="javascript:void(0)" onclick="showDetail(${report.id})">${report.title}</a>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${report.reporterType == 'GUEST'}">
                                                                    ${report.reporterName != null ? report.reporterName : '游客'}(游客)
                                                                </c:when>
                                                                <c:when test="${report.reporterType == 'MEMBER'}">
                                                                    ${report.userName != null ? report.userName : report.reporterName}(成员)
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <button class="btn btn-sm btn-primary" onclick="showDetail(${report.id})">查看</button>
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
</div>

<!-- 详情模态框 -->
<dialog id="detailModal" style="border:1px solid #ccc;border-radius:8px;padding:20px;min-width:400px;max-width:600px;">
    <div id="modalContent" style="margin-bottom:15px;"></div>
    <div style="text-align:right;">
        <button class="btn btn-secondary" onclick="document.getElementById('detailModal').close()">关闭</button>
    </div>
</dialog>

<script>
function showDetail(id) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '${pageContext.request.contextPath}/admin/problem/detail?id=' + id, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            console.log('xhr status:', xhr.status);
            console.log('xhr response length:', xhr.responseText ? xhr.responseText.length : 0);
            if (xhr.status === 200) {
                try {
                    var responseText = xhr.responseText;
                    if (responseText.charCodeAt(0) === 0xFEFF) {
                        responseText = responseText.substring(1);
                    }
                    var data = JSON.parse(responseText);
                    if (data.success && data.data) {
                        displayDetail(data.data);
                        document.getElementById('detailModal').showModal();
                    } else {
                        alert('获取详情失败: ' + (data.message || '未知错误'));
                    }
                } catch (e) {
                    alert('获取详情失败: JSON解析错误\n错误: ' + e.message + '\n响应长度: ' + (xhr.responseText ? xhr.responseText.length : 0) + '\n响应内容: ' + (xhr.responseText || 'empty').substring(0, 300));
                }
            } else {
                alert('获取详情失败: HTTP ' + xhr.status);
            }
        }
    };
    xhr.send();
}

function displayDetail(report) {
    var categoryOptions = {
        'UNVERIFIED': '<option value="UNVERIFIED" selected>待确认</option><option value="VERIFIED">属实</option><option value="INVALID">不属实</option>',
        'VERIFIED': '<option value="UNVERIFIED">待确认</option><option value="VERIFIED" selected>属实</option><option value="INVALID">不属实</option>',
        'INVALID': '<option value="UNVERIFIED">待确认</option><option value="VERIFIED">属实</option><option value="INVALID" selected>不属实</option>'
    };
    
    var statusOptions = {
        'PENDING': '<option value="PENDING" selected>待修改</option><option value="SOLVING">正在修改</option><option value="SOLVED">已修改</option>',
        'SOLVING': '<option value="PENDING">待修改</option><option value="SOLVING" selected>正在修改</option><option value="SOLVED">已修改</option>',
        'SOLVED': '<option value="PENDING">待修改</option><option value="SOLVING">正在修改</option><option value="SOLVED" selected>已修改</option>'
    };
    
    var categoryText = {'VERIFIED': '属实', 'UNVERIFIED': '待确认', 'INVALID': '不属实'}[report.category] || report.category;
    var statusText = {'PENDING': '待修改', 'SOLVING': '正在修改', 'SOLVED': '已修改'}[report.status] || '-';
    var reporterTypeText = {'GUEST': '游客', 'MEMBER': '成员', 'ADMIN': '管理员'}[report.reporterType] || report.reporterType;
    
    var html = '<div class="mb-3"><strong>问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div class="mb-3"><strong>问题详情：</strong><div class="border rounded p-2 mt-1">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row mb-3">';
    html += '<div class="col-4"><strong>报告者：</strong>' + escapeHtml(report.reporterName || '无') + '</div>';
    html += '<div class="col-4"><strong>联系方式：</strong>' + escapeHtml(report.reporterContact || '无') + '</div>';
    html += '<div class="col-4"><strong>报告者类型：</strong>' + reporterTypeText + '</div>';
    html += '</div>';
    html += '<div class="row mb-3">';
    html += '<div class="col-4"><strong>分类：</strong>' + categoryText + '</div>';
    html += '<div class="col-4"><strong>状态：</strong>' + (report.category === 'VERIFIED' ? statusText : '-') + '</div>';
    html += '<div class="col-4"><strong>提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    
    if (report.category === 'VERIFIED') {
        html += '<hr><h6>处理操作</h6>';
        html += '<div class="mb-3"><strong>更新分类：</strong>';
        html += '<select class="form-select" id="reportCategory" style="display:inline;width:auto;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div class="mb-3"><strong>更新状态：</strong>';
        html += '<select class="form-select" id="reportStatus" style="display:inline;width:auto;">' + statusOptions[report.status || 'PENDING'] + '</select></div>';
        html += '<div class="mb-3"><strong>管理员备注：</strong>';
        html += '<textarea class="form-control" id="adminComment" rows="3">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-primary" onclick="saveChanges(' + report.id + ')">保存</button>';
    } else if (report.category === 'UNVERIFIED') {
        html += '<hr><h6>处理操作</h6>';
        html += '<div class="mb-3"><strong>更新分类：</strong>';
        html += '<select class="form-select" id="reportCategory" style="display:inline;width:auto;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div class="mb-3"><strong>管理员备注：</strong>';
        html += '<textarea class="form-control" id="adminComment" rows="3">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-primary" onclick="saveChanges(' + report.id + ')">保存</button>';
    } else {
        html += '<hr><h6>处理操作</h6>';
        html += '<div class="mb-3"><strong>更新分类：</strong>';
        html += '<select class="form-select" id="reportCategory" style="display:inline;width:auto;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div class="mb-3"><strong>管理员备注：</strong>';
        html += '<textarea class="form-control" id="adminComment" rows="3">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-primary" onclick="saveChanges(' + report.id + ')">保存</button>';
    }
    
    document.getElementById('modalContent').innerHTML = html;
}

function saveChanges(id) {
    var category = document.getElementById('reportCategory').value;
    var status = document.getElementById('reportStatus') ? document.getElementById('reportStatus').value : '';
    var adminComment = document.getElementById('adminComment').value;
    
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/admin/problem', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                alert(data.message);
                if (data.success) {
                    document.getElementById('detailModal').close();
                    window.location.reload();
                }
            } catch (e) {
                alert('保存失败: ' + e.message);
            }
        }
    };
    
    var params = 'action=update&id=' + id + '&category=' + encodeURIComponent(category);
    if (status) {
        params += '&status=' + encodeURIComponent(status);
    }
    params += '&adminComment=' + encodeURIComponent(adminComment);
    xhr.send(params);
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
</script>

<jsp:include page="/jsp/common/layout_bottom.jsp" />