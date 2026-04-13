<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .btn-outline-danger { color: #ef4444; border: 1px solid #ef4444; background: transparent; border-radius: var(--radius-standard); padding: 8px 16px; transition: all 0.3s ease; }
    .btn-outline-danger:hover { background: #ef4444; color: white; }
    .btn-sm { padding: 6px 12px; font-size: 0.875rem; }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-verified { background: #fee2e2; color: #991b1b; }
    .badge-invalid { background: #d1fae5; color: #065f46; }
    .badge-unverified { background: #fef3c7; color: #92400e; }
    .badge-solved { background: #d1fae5; color: #065f46; }
    .badge-solving { background: #dbeafe; color: #1e40af; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .nav-pill { border-radius: var(--radius-pill); padding: 8px 16px; transition: all 0.3s ease; font-weight: 500; }
    .nav-pill:hover { background: rgba(20, 86, 240, 0.1); }
    .nav-pill.active { background: var(--brand-blue); color: white; }
    .modal-design { border-radius: var(--radius-generous); border: none; padding: 24px; max-width: 600px; width: 90%; box-shadow: var(--shadow-brand-purple); }
</style>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none mb-4">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title mb-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                            问题管理
                        </h2>
                    </div>
                    <div class="col-auto">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline-brand">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                            返回首页
                        </a>
                    </div>
                </div>
            </div>

            <div class="card-design">
                <div class="card-body p-0">
                    <ul class="nav nav-tabs nav-fill" role="tablist" style="border-bottom: 1px solid var(--border-gray);">
                        <li class="nav-item">
                            <a class="nav-link active" data-bs-toggle="tab" href="#tab-verified" role="tab" style="border-radius: var(--radius-standard) var(--radius-standard) 0 0; padding: 16px;">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                                属实
                                <span class="badge-design badge-verified ms-2">${verifiedCount}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-bs-toggle="tab" href="#tab-invalid" role="tab" style="border-radius: var(--radius-standard) var(--radius-standard) 0 0; padding: 16px;">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
                                不属实
                                <span class="badge-design badge-invalid ms-2">${invalidCount}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-bs-toggle="tab" href="#tab-unverified" role="tab" style="border-radius: var(--radius-standard) var(--radius-standard) 0 0; padding: 16px;">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                                待确认
                                <span class="badge-design badge-unverified ms-2">${unverifiedCount}</span>
                            </a>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane fade show active" id="tab-verified" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
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
                                                    <td colspan="6" class="text-center text-muted py-5">暂无属实问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${verifiedList}">
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
                                                                <c:when test="${report.status == 'SOLVED'}">
                                                                    <span class="badge-design badge-solved">已修改</span>
                                                                </c:when>
                                                                <c:when test="${report.status == 'SOLVING'}">
                                                                    <span class="badge-design badge-solving">正在修改</span>
                                                                </c:when>
                                                                <c:when test="${report.status == 'PENDING'}">
                                                                    <span class="badge-design badge-pending">待修改</span>
                                                                </c:when>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-muted"><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <div class="btn-group" style="gap: 8px;">
                                                                <button class="btn btn-sm" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard);" onclick="showDetail(${report.id})">查看</button>
                                                                <button class="btn btn-sm btn-outline-danger" onclick="deleteReport(${report.id})">删除</button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="tab-invalid" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
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
                                                    <td colspan="5" class="text-center text-muted py-5">暂无不属实问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${invalidList}">
                                                    <tr>
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
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-muted"><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <div class="btn-group" style="gap: 8px;">
                                                                <button class="btn btn-sm" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard);" onclick="showDetail(${report.id})">查看</button>
                                                                <button class="btn btn-sm btn-outline-danger" onclick="deleteReport(${report.id})">删除</button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="tab-unverified" role="tabpanel">
                            <div class="table-responsive">
                                <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
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
                                                    <td colspan="5" class="text-center text-muted py-5">暂无待确认问题</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="report" items="${unverifiedList}">
                                                    <tr>
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
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-muted"><fmt:formatDate value="${report.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                                        <td>
                                                            <div class="btn-group" style="gap: 8px;">
                                                                <button class="btn btn-sm" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard);" onclick="showDetail(${report.id})">查看</button>
                                                                <button class="btn btn-sm btn-outline-danger" onclick="deleteReport(${report.id})">删除</button>
                                                            </div>
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

<dialog id="detailModal" class="modal-design">
    <div id="modalContent" style="margin-bottom: 20px;"></div>
    <div style="text-align: right;">
        <button class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 10px 20px;" onclick="document.getElementById('detailModal').close()">关闭</button>
    </div>
</dialog>

<script>
function showDetail(id) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '${pageContext.request.contextPath}/admin/problem/detail?id=' + id, true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
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
                    alert('获取详情失败: JSON解析错误');
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
    
    var html = '<div style="font-family: var(--font-ui);">';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题详情：</strong><div style="border: 1px solid var(--border-gray); border-radius: var(--radius-standard); padding: 12px; margin-top: 8px; background: var(--bg-light-gray);">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row" style="margin-bottom: 16px;">';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">报告者：</strong>' + escapeHtml(report.reporterName || '无') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">联系方式：</strong>' + escapeHtml(report.reporterContact || '无') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">报告者类型：</strong>' + reporterTypeText + '</div>';
    html += '</div>';
    html += '<div class="row" style="margin-bottom: 16px;">';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">分类：</strong>' + categoryText + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">状态：</strong>' + (report.category === 'VERIFIED' ? statusText : '-') + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    
    if (report.category === 'VERIFIED') {
        html += '<hr style="border: none; border-top: 1px solid var(--border-gray); margin: 20px 0;"><h6 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark); margin-bottom: 16px;">处理操作</h6>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">更新分类：</strong>';
        html += '<select class="form-select input-design" id="reportCategory" style="display:inline;width:auto;margin-left:8px;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">更新状态：</strong>';
        html += '<select class="form-select input-design" id="reportStatus" style="display:inline;width:auto;margin-left:8px;">' + statusOptions[report.status || 'PENDING'] + '</select></div>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">管理员备注：</strong>';
        html += '<textarea class="form-control input-design" id="adminComment" rows="3" style="margin-top:8px;">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-brand" onclick="saveChanges(' + report.id + ')">保存</button>';
    } else if (report.category === 'UNVERIFIED') {
        html += '<hr style="border: none; border-top: 1px solid var(--border-gray); margin: 20px 0;"><h6 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark); margin-bottom: 16px;">处理操作</h6>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">更新分类：</strong>';
        html += '<select class="form-select input-design" id="reportCategory" style="display:inline;width:auto;margin-left:8px;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">管理员备注：</strong>';
        html += '<textarea class="form-control input-design" id="adminComment" rows="3" style="margin-top:8px;">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-brand" onclick="saveChanges(' + report.id + ')">保存</button>';
    } else {
        html += '<hr style="border: none; border-top: 1px solid var(--border-gray); margin: 20px 0;"><h6 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark); margin-bottom: 16px;">处理操作</h6>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">更新分类：</strong>';
        html += '<select class="form-select input-design" id="reportCategory" style="display:inline;width:auto;margin-left:8px;">' + categoryOptions[report.category] + '</select></div>';
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">管理员备注：</strong>';
        html += '<textarea class="form-control input-design" id="adminComment" rows="3" style="margin-top:8px;">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
        html += '<button type="button" class="btn btn-brand" onclick="saveChanges(' + report.id + ')">保存</button>';
    }
    
    html += '</div>';
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

function deleteReport(id) {
    if (!confirm('确定要删除这条问题反馈吗？此操作不可恢复。')) {
        return;
    }
    
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/admin/problem', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try {
                var data = JSON.parse(xhr.responseText);
                alert(data.message);
                if (data.success) {
                    window.location.reload();
                }
            } catch (e) {
                alert('删除失败: ' + e.message);
            }
        }
    };
    xhr.send('action=delete&id=' + id);
}
</script>