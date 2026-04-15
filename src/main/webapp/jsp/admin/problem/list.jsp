<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="问题反馈管理" />
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
                <i class="bi bi-exclamation-triangle me-2"></i>问题反馈管理
            </h1>
            <p class="admin-hero-subtitle">查看和处理用户反馈的问题</p>
        </div>

        <div class="card-design mb-3">
            <div class="card-body-design">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label-design">筛选分类</label>
                        <select class="input-design" id="categoryFilter">
                            <option value="">全部</option>
                            <option value="VERIFIED">属实</option>
                            <option value="UNVERIFIED">待验证</option>
                            <option value="INVALID">不属实</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label-design">筛选状态</label>
                        <select class="input-design" id="statusFilter">
                            <option value="">全部</option>
                            <option value="PENDING">待处理</option>
                            <option value="SOLVING">正在解决</option>
                            <option value="SOLVED">已解决</option>
                            <option value="UNSOLVED">未解决</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="button" class="btn-brand w-100" onclick="loadReports()">筛选</button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="card-design">
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>标题</th>
                            <th>报告者</th>
                            <th>类型</th>
                            <th>分类</th>
                            <th>状态</th>
                            <th>提交时间</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody id="reportTableBody">
                        <c:forEach var="report" items="${reports}">
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
                                        <c:when test="${report.reporterType == 'GUEST'}">
                                            <span class="badge-design" style="background: rgba(107, 114, 128, 0.1); color: #6b7280;">游客</span>
                                        </c:when>
                                        <c:when test="${report.reporterType == 'MEMBER'}">
                                            <span class="badge-design" style="background: rgba(59, 130, 246, 0.1); color: #2563eb;">成员</span>
                                        </c:when>
                                        <c:when test="${report.reporterType == 'ADMIN'}">
                                            <span class="badge-design" style="background: rgba(139, 92, 246, 0.1); color: #7c3aed;">管理员</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${report.category == 'VERIFIED'}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">属实</span>
                                        </c:when>
                                        <c:when test="${report.category == 'UNVERIFIED'}">
                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #d97706;">待验证</span>
                                        </c:when>
                                        <c:when test="${report.category == 'INVALID'}">
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">不属实</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${report.category == 'VERIFIED'}">
                                            <c:choose>
                                                <c:when test="${report.status == 'PENDING'}">
                                                    <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #d97706;">待处理</span>
                                                </c:when>
                                                <c:when test="${report.status == 'SOLVING'}">
                                                    <span class="badge-design" style="background: rgba(59, 130, 246, 0.1); color: #2563eb;">正在解决</span>
                                                </c:when>
                                                <c:when test="${report.status == 'SOLVED'}">
                                                    <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">已解决</span>
                                                </c:when>
                                                <c:when test="${report.status == 'UNSOLVED'}">
                                                    <span class="badge-design" style="background: rgba(107, 114, 128, 0.1); color: #6b7280;">未解决</span>
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
                                <td>${report.createdAt}</td>
                                <td>
                                    <button class="btn-sm-brand" onclick="showDetail(${report.id})">查看</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<dialog id="detailModal" class="modal modal-blur fade" style="display: none;">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">问题详情</h5>
                <button type="button" class="btn-close" onclick="closeDetailModal()"></button>
            </div>
            <div class="modal-body" id="modalContent">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-outline-brand" onclick="closeDetailModal()">关闭</button>
            </div>
        </div>
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
    
    var html = '<div class="mb-3"><strong>问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div class="mb-3"><strong>问题详情：</strong><div class="border rounded p-2 mt-1">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row mb-3">';
    html += '<div class="col-4"><strong>报告者：</strong>' + escapeHtml(report.reporterName || '无') + '</div>';
    html += '<div class="col-4"><strong>联系方式：</strong>' + escapeHtml(report.reporterContact || '无') + '</div>';
    html += '<div class="col-4"><strong>报告者类型：</strong>' + reporterTypeText + '</div>';
    html += '</div>';
    html += '<div class="row mb-3">';
    html += '<div class="col-4"><strong>提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '<div class="col-4"><strong>处理人：</strong>' + (report.handlerName || '-') + '</div>';
    html += '<div class="col-4"><strong>处理时间：</strong>' + (report.handledAt ? new Date(report.handledAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    html += '<hr>';
    html += '<div class="mb-3"><strong>分类：</strong>';
    html += '<select class="input-design" id="reportCategory" style="display:inline;width:auto;">' + categoryOptions[report.category] + '</select></div>';
    html += '<div class="mb-3" id="statusSection"><strong>状态：</strong>';
    if (canChangeStatus) {
        html += '<select class="input-design" id="reportStatus" style="display:inline;width:auto;">' + statusOptions[report.status] + '</select>';
    } else {
        html += '<span class="badge-design" style="background: rgba(107, 114, 128, 0.1); color: #6b7280;">' + (report.category === 'UNVERIFIED' ? '待验证无状态' : '不属实无状态') + '</span>';
    }
    html += '</div>';
    html += '<div class="mb-3"><strong>管理员备注：</strong>';
    html += '<textarea class="input-design" id="adminComment" rows="3">' + escapeHtml(report.adminComment || '') + '</textarea></div>';
    html += '<input type="hidden" id="reportId" value="' + report.id + '">';
    
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

<jsp:include page="../../common/layout_bottom.jsp" />