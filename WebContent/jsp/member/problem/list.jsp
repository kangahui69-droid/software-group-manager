<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .table-design th { font-family: var(--font-ui); font-weight: 600; color: var(--text-secondary); border-bottom: 2px solid var(--border-gray); padding: 12px 16px; }
    .table-design td { padding: 16px; vertical-align: middle; }
    .table-design tbody tr:hover { background: rgba(20, 86, 240, 0.03); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-verified { background: #fee2e2; color: #991b1b; }
    .badge-unverified { background: #fef3c7; color: #92400e; }
    .badge-invalid { background: #d1fae5; color: #065f46; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-solving { background: #dbeafe; color: #1e40af; }
    .badge-solved { background: #d1fae5; color: #065f46; }
    .badge-unsolved { background: #e5e7eb; color: #374151; }
    .modal-design { border-radius: var(--radius-generous); border: none; padding: 24px; max-width: 700px; width: 90%; box-shadow: var(--shadow-brand-purple); }
    .link-primary { color: var(--text-dark); text-decoration: none; transition: color 0.3s ease; }
    .link-primary:hover { color: var(--brand-blue); }
</style>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none mb-4">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title mb-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M10.29 3.861L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.861a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                            问题反馈
                        </h2>
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-brand" onclick="showSubmitModal()">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                            提交新反馈
                        </button>
                    </div>
                </div>
            </div>

            <c:if test="${not empty param.success}">
                <div class="alert alert-success mb-4" role="alert" style="border-radius: var(--radius-standard); background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; padding: 16px;">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                    ${param.success}
                </div>
            </c:if>

            <div class="card-design">
                <div class="table-responsive">
                    <table class="table table-design table-hover mb-0" style="border-collapse: separate; border-spacing: 0;">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>标题</th>
                                <th>分类</th>
                                <th>状态</th>
                                <th>提交时间</th>
                                <th class="text-end">操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty reports}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted py-5">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon mb-2" width="48" height="48" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round" style="color: var(--text-muted);"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"></polyline><path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"></path></svg>
                                            <br>
                                            暂无反馈记录
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="report" items="${reports}">
                                        <tr>
                                            <td>${report.id}</td>
                                            <td>
                                                <a href="javascript:void(0)" onclick="showDetail(${report.id})" class="link-primary">${report.title}</a>
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
                                                    <c:otherwise>
                                                        <span class="text-muted">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-muted">${report.createdAt}</td>
                                            <td class="text-end">
                                                <button class="btn btn-sm" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard);" onclick="showDetail(${report.id})">查看</button>
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

<dialog id="detailModal" class="modal-design">
    <div id="modalContent" style="margin-bottom: 20px;"></div>
    <div style="text-align: right;">
        <button type="button" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 10px 20px;" onclick="closeDetailModal()">关闭</button>
    </div>
</dialog>

<dialog id="submitModal" class="modal-design">
    <div style="font-family: var(--font-ui);">
        <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom" style="border-color: var(--border-light);">
            <h5 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark); margin: 0;">提交问题反馈</h5>
            <button type="button" class="btn-close" onclick="closeSubmitModal()">×</button>
        </div>
        <form id="submitForm">
            <div id="submitErrorAlert" class="alert alert-danger mb-3" style="display: none; border-radius: var(--radius-standard);"></div>
            <div id="submitSuccessAlert" class="alert alert-success mb-3" style="display: none; border-radius: var(--radius-standard);"></div>
            <div class="mb-3">
                <label class="form-label required">问题标题</label>
                <input type="text" name="title" class="form-control input-design" placeholder="请简要描述您的问题" required maxlength="200">
            </div>
            <div class="mb-3">
                <label class="form-label required">问题详情</label>
                <textarea name="content" class="form-control input-design" rows="5" placeholder="请详细描述您遇到的问题" required></textarea>
            </div>
            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 10px 20px;" onclick="closeSubmitModal()">取消</button>
                <button type="submit" class="btn btn-brand">提交</button>
            </div>
        </form>
    </div>
</dialog>

<script>
var detailModal;
var submitModal;

document.addEventListener('DOMContentLoaded', function() {
    detailModal = document.getElementById('detailModal');
    submitModal = document.getElementById('submitModal');
    
    document.getElementById('submitForm').addEventListener('submit', function(e) {
        e.preventDefault();
        submitProblem();
    });
});

function closeDetailModal() {
    detailModal.close();
}

function closeSubmitModal() {
    submitModal.close();
}

function showSubmitModal() {
    document.getElementById('submitErrorAlert').style.display = 'none';
    document.getElementById('submitSuccessAlert').style.display = 'none';
    document.getElementById('submitForm').reset();
    submitModal.showModal();
}

function submitProblem() {
    var form = document.getElementById('submitForm');
    var formData = new FormData(form);
    var params = new URLSearchParams();
    params.append('action', 'submit');
    params.append('title', formData.get('title'));
    params.append('content', formData.get('content'));

    fetch('${pageContext.request.contextPath}/member/problem', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('submitSuccessAlert').textContent = data.message;
            document.getElementById('submitSuccessAlert').style.display = 'block';
            document.getElementById('submitErrorAlert').style.display = 'none';
            setTimeout(function() {
                closeSubmitModal();
                window.location.reload();
            }, 1500);
        } else {
            document.getElementById('submitErrorAlert').textContent = data.message;
            document.getElementById('submitErrorAlert').style.display = 'block';
            document.getElementById('submitSuccessAlert').style.display = 'none';
        }
    })
    .catch(error => {
        document.getElementById('submitErrorAlert').textContent = '提交失败，请稍后重试';
        document.getElementById('submitErrorAlert').style.display = 'block';
        document.getElementById('submitSuccessAlert').style.display = 'none';
    });
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
    var categoryText = {'VERIFIED': '属实', 'UNVERIFIED': '待验证', 'INVALID': '不属实'}[report.category] || report.category;
    var statusText = {'PENDING': '待处理', 'SOLVING': '正在解决', 'SOLVED': '已解决', 'UNSOLVED': '未解决'}[report.status] || '-';
    
    var html = '<div style="font-family: var(--font-ui);">';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">问题详情：</strong><div style="border: 1px solid var(--border-gray); border-radius: var(--radius-standard); padding: 12px; margin-top: 8px; background: var(--bg-light-gray);">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row" style="margin-bottom: 16px;">';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">分类：</strong>' + categoryText + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">状态：</strong>' + statusText + '</div>';
    html += '<div class="col-4"><strong style="color: var(--text-dark);">提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    if (report.adminComment) {
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">管理员备注：</strong><div style="border: 1px solid var(--border-gray); border-radius: var(--radius-standard); padding: 12px; margin-top: 8px; background: var(--bg-light-gray);">' + escapeHtml(report.adminComment) + '</div></div>';
    }
    if (report.handlerName) {
        html += '<div style="margin-bottom: 16px;"><strong style="color: var(--text-dark);">处理人：</strong>' + escapeHtml(report.handlerName) + '</div>';
    }
    html += '</div>';
    
    document.getElementById('modalContent').innerHTML = html;
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
</script>