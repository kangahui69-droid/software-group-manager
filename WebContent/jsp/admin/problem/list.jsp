<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
    .badge-secondary { background: #9ca3af; color: white; }
    .badge-guest { background: #6b7280; color: white; }
    .badge-member { background: var(--primary-500); color: white; }
    .badge-admin { background: var(--brand-blue); color: white; }
    .badge-verified { background: #fee2e2; color: #991b1b; }
    .badge-unverified { background: #fef3c7; color: #92400e; }
    .badge-invalid { background: #d1fae5; color: #065f46; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-solving { background: #dbeafe; color: #1e40af; }
    .badge-solved { background: #d1fae5; color: #065f46; }
    .badge-unsolved { background: #e5e7eb; color: #374151; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .modal-design { border-radius: var(--radius-generous); border: none; padding: 24px; max-width: 700px; width: 90%; box-shadow: var(--shadow-brand-purple); }
    .link-primary { color: var(--text-dark); text-decoration: none; transition: color 0.3s ease; }
    .link-primary:hover { color: var(--brand-blue); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    问题反馈管理
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="search-card mb-4">
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">筛选分类</label>
                    <select class="form-select input-design" id="categoryFilter">
                        <option value="">全部</option>
                        <option value="VERIFIED">属实</option>
                        <option value="UNVERIFIED">待验证</option>
                        <option value="INVALID">不属实</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">筛选状态</label>
                    <select class="form-select input-design" id="statusFilter">
                        <option value="">全部</option>
                        <option value="PENDING">待处理</option>
                        <option value="SOLVING">正在解决</option>
                        <option value="SOLVED">已解决</option>
                        <option value="UNSOLVED">未解决</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="button" class="btn btn-brand w-100" onclick="loadReports()">筛选</button>
                </div>
            </div>
        </div>
        
        <div class="card-design">
            <div class="table-responsive">
                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>标题</th>
                            <th>报告者</th>
                            <th>类型</th>
                            <th>分类</th>
                            <th>状态</th>
                            <th>提交时间</th>
                            <th class="text-end">操作</th>
                        </tr>
                    </thead>
                    <tbody id="reportTableBody">
                        <c:forEach var="report" items="${reports}">
                            <tr id="row-${report.id}">
                                <td>${report.id}</td>
                                <td>
                                    <a href="javascript:void(0)" onclick="showDetail(${report.id})" class="link-primary">${report.title}</a>
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
                                        <c:when test="${report.reporterType == 'GUEST'}">
                                            <span class="badge-design badge-guest">游客</span>
                                        </c:when>
                                        <c:when test="${report.reporterType == 'MEMBER'}">
                                            <span class="badge-design badge-member">成员</span>
                                        </c:when>
                                        <c:when test="${report.reporterType == 'ADMIN'}">
                                            <span class="badge-design badge-admin">管理员</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${report.category == 'VERIFIED'}">
                                            <span class="badge-design badge-verified">属实</span>
                                        </c:when>
                                        <c:when test="${report.category == 'UNVERIFIED'}">
                                            <span class="badge-design badge-unverified">待验证</span>
                                        </c:when>
                                        <c:when test="${report.category == 'INVALID'}">
                                            <span class="badge-design badge-invalid">不属实</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${report.category == 'VERIFIED'}">
                                            <c:choose>
                                                <c:when test="${report.status == 'PENDING'}">
                                                    <span class="badge-design badge-pending">待处理</span>
                                                </c:when>
                                                <c:when test="${report.status == 'SOLVING'}">
                                                    <span class="badge-design badge-solving">正在解决</span>
                                                </c:when>
                                                <c:when test="${report.status == 'SOLVED'}">
                                                    <span class="badge-design badge-solved">已解决</span>
                                                </c:when>
                                                <c:when test="${report.status == 'UNSOLVED'}">
                                                    <span class="badge-design badge-unsolved">未解决</span>
                                                </c:when>
                                            </c:choose>
                                        </c:when>
                                        <c:when test="${report.category == 'UNVERIFIED'}">
                                            <span class="text-muted">-</span>
                                        </c:when>
                                        <c:when test="${report.category == 'INVALID'}">
                                            <span class="text-muted">-</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td class="text-muted">${report.createdAt}</td>
                                <td class="text-end">
                                    <button class="btn btn-sm" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard);" onclick="showDetail(${report.id})">查看</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<dialog id="detailModal" class="modal-design">
    <div id="modalContent" style="margin-bottom: 20px;"></div>
    <div style="text-align: right;">
        <button class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 10px 20px;" onclick="closeDetailModal()">关闭</button>
    </div>
</dialog>

<script>
var detailModal;

document.addEventListener('DOMContentLoaded', function() {
    detailModal = document.getElementById('detailModal');
});

function closeDetailModal() {
    detailModal.close();
}

function loadReports() {
    var category = document.getElementById('categoryFilter').value;
    var status = document.getElementById('statusFilter').value;
    var url = '${pageContext.request.contextPath}/problem?action=list';
    if (category || status) {
        url += '&category=' + category + '&status=' + status;
    }
    window.location.href = url;
}

function showDetail(id) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '${pageContext.request.contextPath}/problem?action=detail&id=' + id, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (data.success && data.data) {
                        displayDetail(data.data);
                        detailModal.showModal();
                    } else {
                        alert(data.message || '获取详情失败');
                    }
                } catch (e) {
                    alert('获取详情失败');
                }
            } else {
                alert('获取详情失败');
            }
        }
    };
    xhr.send();
}

function displayDetail(report) {
    var categoryOptions = {
        'UNVERIFIED': '<option value="UNVERIFIED" selected>待验证</option><option value="VERIFIED">属实</option><option value="INVALID">不属实</option>',
        'VERIFIED': '<option value="UNVERIFIED">待验证</option><option value="VERIFIED" selected>属实</option><option value="INVALID">不属实</option>',
        'INVALID': '<option value="UNVERIFIED">待验证</option><option value="VERIFIED">属实</option><option value="INVALID" selected>不属实</option>'
    };
    
    var statusOptions = {
        'PENDING': '<option value="PENDING" selected>待处理</option><option value="SOLVING">正在解决</option><option value="SOLVED">已解决</option><option value="UNSOLVED">未解决</option>',
        'SOLVING': '<option value="PENDING">待处理</option><option value="SOLVING" selected>正在解决</option><option value="SOLVED">已解决</option><option value="UNSOLVED">未解决</option>',
        'SOLVED': '<option value="PENDING">待处理</option><option value="SOLVING">正在解决</option><option value="SOLVED" selected>已解决</option><option value="UNSOLVED">未解决</option>',
        'UNSOLVED': '<option value="PENDING">待处理</option><option value="SOLVING">正在解决</option><option value="SOLVED">已解决</option><option value="UNSOLVED" selected>未解决</option>'
    };
    
    var reporterTypeText = {'GUEST': '游客', 'MEMBER': '成员', 'ADMIN': '管理员'}[report.reporterType] || report.reporterType;
    var categoryText = {'VERIFIED': '属实', 'UNVERIFIED': '待验证', 'INVALID': '不属实'}[report.category] || report.category;
    var statusText = {'PENDING': '待处理', 'SOLVING': '正在解决', 'SOLVED': '已解决', 'UNSOLVED': '未解决'}[report.status] || report.status;
    
    var canChangeStatus = report.category === 'VERIFIED';
    
    var html = '<div style="font-family: var(--font-ui);">';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题详情：</strong><div style="border: 1px solid var(--border-gray); border-radius: var(--radius-standard); padding: 12px; margin-top: 8px; background: var(--bg-light-gray);">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row" style="margin-bottom: 16px;">';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">报告者：</strong>' + escapeHtml(report.reporterName || '无') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">联系方式：</strong>' + escapeHtml(report.reporterContact || '无') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">报告者类型：</strong>' + reporterTypeText + '</div>';
    html += '</div>';
    html += '<div class="row" style="margin-bottom: 16px;">';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">处理人：</strong>' + (report.handlerName || '-') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">处理时间：</strong>' + (report.handledAt ? new Date(report.handledAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    html += '<hr style="border: none; border-top: 1px solid var(--border-gray); margin: 20px 0;">';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">分类：</strong>';
    html += '<select class="form-select input-design" id="reportCategory" style="display:inline;width:auto;margin-left:8px;">' + categoryOptions[report.category] + '</select></div>';
    html += '<div style="margin-bottom: 16px;" id="statusSection"><strong style="color: var(--text-dark);">状态：</strong>';
    if (canChangeStatus) {
        html += '<select class="form-select input-design" id="reportStatus" style="display:inline;width:auto;margin-left:8px;">' + statusOptions[report.status] + '</select>';
    } else {
        html += '<span class="badge-design badge-secondary" style="margin-left: 8px;">' + (report.category === 'UNVERIFIED' ? '待验证无状态' : '不属实无状态') + '</span>';
    }
    html += '</div>';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">管理员备注：</strong>';
    html += '<textarea class="form-control input-design" id="adminComment" rows="3" style="margin-top:8px;">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
    html += '<input type="hidden" id="reportId" value="' + report.id + '">';
    html += '</div>';
    
    document.getElementById('modalContent').innerHTML = html;
}

function saveChanges() {
    var id = document.getElementById('reportId').value;
    var category = document.getElementById('reportCategory').value;
    var status = document.getElementById('reportStatus') ? document.getElementById('reportStatus').value : '';
    var adminComment = document.getElementById('adminComment').value;
    
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/problem', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                alert(data.message);
                if (data.success) {
                    detailModal.close();
                    window.location.reload();
                }
            } catch (e) {
                alert('保存失败');
            }
        }
    };
    
    var params = 'action=updateCategory&id=' + id + '&category=' + encodeURIComponent(category) + '&adminComment=' + encodeURIComponent(adminComment);
    if (status) {
        params += '&status=' + encodeURIComponent(status);
    }
    xhr.send(params);
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
</script>