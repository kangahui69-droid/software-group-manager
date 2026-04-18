<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="问题反馈" />
    <jsp:param name="active" value="problem" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
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
        cursor: pointer;
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
        cursor: pointer;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
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

    .back-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: var(--radius-standard);
        background: rgba(255, 255, 255, 0.2);
        color: white;
        text-decoration: none;
        font-size: 0.875rem;
        transition: all 0.2s ease;
    }
    .back-btn:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <a href="${pageContext.request.contextPath}/member/index.jsp" class="back-btn">
                    <i class="bi bi-arrow-left"></i>返回
                </a>
            </div>
            <h1 class="member-hero-title">
                <i class="bi bi-exclamation-triangle me-2"></i>问题反馈
            </h1>
            <p class="member-hero-subtitle">提交和查看您的问题反馈</p>
        </div>

        <div class="d-flex justify-content-end mb-4">
            <button class="btn-brand" onclick="showSubmitModal()">
                <i class="bi bi-plus-circle"></i>提交新反馈
            </button>
        </div>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible">
                <i class="bi bi-check-circle me-2"></i>${param.success}
            </div>
        </c:if>

        <div class="card-design">
            <div class="card-body-design">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>标题</th>
                            <th>分类</th>
                            <th>状态</th>
                            <th>提交时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty reports}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted" style="padding: 48px;">
                                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                        暂无反馈记录
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="report" items="${reports}">
                                    <tr>
                                        <td style="color: var(--text-muted);">${report.id}</td>
                                        <td>
                                            <a href="javascript:void(0)" onclick="showDetail(${report.id})" class="text-decoration-none" style="color: var(--text-dark); font-weight: 500;">${report.title}</a>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.category == 'VERIFIED'}">
                                                    <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">属实</span>
                                                </c:when>
                                                <c:when test="${report.category == 'UNVERIFIED'}">
                                                    <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待验证</span>
                                                </c:when>
                                                <c:when test="${report.category == 'INVALID'}">
                                                    <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">不属实</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${report.category == 'VERIFIED'}">
                                                    <c:choose>
                                                        <c:when test="${report.status == 'PENDING'}">
                                                            <span class="badge-design" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">待处理</span>
                                                        </c:when>
                                                        <c:when test="${report.status == 'SOLVING'}">
                                                            <span class="badge-design" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">正在解决</span>
                                                        </c:when>
                                                        <c:when test="${report.status == 'SOLVED'}">
                                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">已解决</span>
                                                        </c:when>
                                                        <c:when test="${report.status == 'UNSOLVED'}">
                                                            <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">未解决</span>
                                                        </c:when>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: var(--text-muted);">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="color: var(--text-secondary);">${report.createdAt}</td>
                                        <td>
                                            <button class="btn-sm-brand" onclick="showDetail(${report.id})">
                                                <i class="bi bi-eye"></i>查看
                                            </button>
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

<dialog id="detailModal" style="border:1px solid #ddd;border-radius:8px;padding:0;max-width:700px;width:90%;">
    <div class="modal-content" style="border:none;">
        <div class="modal-header" style="display:flex;justify-content:space-between;align-items:center;padding:15px;border-bottom:1px solid #eee;">
            <h5 class="modal-title">问题详情</h5>
            <button type="button" class="btn-close" onclick="closeDetailModal()">×</button>
        </div>
        <div class="modal-body" id="modalContent" style="padding:15px;"></div>
        <div class="modal-footer" style="padding:15px;border-top:1px solid #eee;">
            <button type="button" class="btn btn-secondary" onclick="closeDetailModal()">关闭</button>
        </div>
    </div>
</dialog>

<dialog id="submitModal" style="border:1px solid #ddd;border-radius:8px;padding:0;max-width:500px;width:90%;">
    <div class="modal-content" style="border:none;">
        <div class="modal-header" style="display:flex;justify-content:space-between;align-items:center;padding:15px;border-bottom:1px solid #eee;">
            <h5 class="modal-title">提交问题反馈</h5>
            <button type="button" class="btn-close" onclick="closeSubmitModal()">×</button>
        </div>
        <form id="submitForm">
            <div class="modal-body" style="padding:15px;">
                <div id="submitErrorAlert" class="alert alert-danger" style="display: none;"></div>
                <div id="submitSuccessAlert" class="alert alert-success" style="display: none;"></div>
                <div class="mb-3">
                    <label class="form-label required">问题标题</label>
                    <input type="text" name="title" class="form-control" placeholder="请简要描述您的问题" required maxlength="200">
                </div>
                <div class="mb-3">
                    <label class="form-label required">问题详情</label>
                    <textarea name="content" class="form-control" rows="5" placeholder="请详细描述您遇到的问题" required></textarea>
                </div>
            </div>
            <div class="modal-footer" style="padding:15px;border-top:1px solid #eee;">
                <button type="button" class="btn btn-secondary" onclick="closeSubmitModal()">取消</button>
                <button type="submit" class="btn btn-primary">提交</button>
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