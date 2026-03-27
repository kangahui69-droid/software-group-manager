<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="问题反馈" />
    <jsp:param name="active" value="problem" />
</jsp:include>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            <i class="bi bi-exclamation-triangle me-2"></i>问题反馈
                        </h2>
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-primary" onclick="showSubmitModal()">
                            <i class="bi bi-plus-circle me-2"></i>提交新反馈
                        </button>
                    </div>
                </div>
            </div>

            <c:if test="${not empty param.success}">
                <div class="alert alert-success alert-dismissible">
                    <i class="bi bi-check-circle me-2"></i>${param.success}
                </div>
            </c:if>

            <div class="row row-cards">
                <div class="col-12">
                    <div class="card">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table table-striped">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>标题</th>
                                        <th>分类</th>
                                        <th>状态</th>
                                        <th>提交时间</th>
                                        <th class="w-1">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty reports}">
                                            <tr>
                                                <td colspan="6" class="text-center text-muted py-4">
                                                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                                    暂无反馈记录
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="report" items="${reports}">
                                                <tr>
                                                    <td>${report.id}</td>
                                                    <td>
                                                        <a href="javascript:void(0)" onclick="showDetail(${report.id})">${report.title}</a>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${report.category == 'VERIFIED'}">
                                                                <span class="badge bg-danger">属实</span>
                                                            </c:when>
                                                            <c:when test="${report.category == 'UNVERIFIED'}">
                                                                <span class="badge bg-warning">待验证</span>
                                                            </c:when>
                                                            <c:when test="${report.category == 'INVALID'}">
                                                                <span class="badge bg-success">不属实</span>
                                                            </c:when>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${report.category == 'VERIFIED'}">
                                                                <c:choose>
                                                                    <c:when test="${report.status == 'PENDING'}">
                                                                        <span class="badge bg-warning">待处理</span>
                                                                    </c:when>
                                                                    <c:when test="${report.status == 'SOLVING'}">
                                                                        <span class="badge bg-info">正在解决</span>
                                                                    </c:when>
                                                                    <c:when test="${report.status == 'SOLVED'}">
                                                                        <span class="badge bg-success">已解决</span>
                                                                    </c:when>
                                                                    <c:when test="${report.status == 'UNSOLVED'}">
                                                                        <span class="badge bg-secondary">未解决</span>
                                                                    </c:when>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">-</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>${report.createdAt}</td>
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

<dialog id="detailModal" class="modal modal-blur fade">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">问题详情</h5>
                <button type="button" class="btn-close" onclick="closeDetailModal()"></button>
            </div>
            <div class="modal-body" id="modalContent"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeDetailModal()">关闭</button>
            </div>
        </div>
    </div>
</dialog>

<dialog id="submitModal" class="modal modal-blur fade">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">提交问题反馈</h5>
                <button type="button" class="btn-close" onclick="closeSubmitModal()"></button>
            </div>
            <form id="submitForm">
                <div class="modal-body">
                    <div id="submitErrorAlert" class="alert alert-danger" style="display: none;" role="alert"></div>
                    <div id="submitSuccessAlert" class="alert alert-success" style="display: none;" role="alert"></div>
                    <div class="mb-3">
                        <label class="form-label required">问题标题</label>
                        <input type="text" name="title" class="form-control" placeholder="请简要描述您的问题" required maxlength="200">
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">问题详情</label>
                        <textarea name="content" class="form-control" rows="5" placeholder="请详细描述您遇到的问题" required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeSubmitModal()">取消</button>
                    <button type="submit" class="btn btn-primary">提交</button>
                </div>
            </form>
        </div>
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
    
    var html = '<div class="mb-3"><strong>问题标题：</strong>' + escapeHtml(report.title) + '</div>';
    html += '<div class="mb-3"><strong>问题详情：</strong><div class="border rounded p-2 mt-1">' + escapeHtml(report.content) + '</div></div>';
    html += '<div class="row mb-3">';
    html += '<div class="col-4"><strong>分类：</strong>' + categoryText + '</div>';
    html += '<div class="col-4"><strong>状态：</strong>' + statusText + '</div>';
    html += '<div class="col-4"><strong>提交时间：</strong>' + (report.createdAt ? new Date(report.createdAt).toLocaleString() : '-') + '</div>';
    html += '</div>';
    if (report.adminComment) {
        html += '<div class="mb-3"><strong>管理员备注：</strong><div class="border rounded p-2 mt-1">' + escapeHtml(report.adminComment) + '</div></div>';
    }
    if (report.handlerName) {
        html += '<div class="mb-3"><strong>处理人：</strong>' + escapeHtml(report.handlerName) + '</div>';
    }
    
    document.getElementById('modalContent').innerHTML = html;
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
</script>

<jsp:include page="/jsp/common/layout_bottom.jsp" />