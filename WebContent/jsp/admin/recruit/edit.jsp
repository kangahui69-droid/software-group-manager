<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .card-title-design { font-family: var(--font-display); font-size: 1.5rem; font-weight: 500; color: var(--text-dark); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-approved { background: #d1fae5; color: #065f46; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    ${empty app ? '添加招新记录' : '编辑招新记录'}
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <form action="manage" method="POST" class="card-design">
                    <div class="card-body p-4">
                        <input type="hidden" name="action" value="${empty app ? 'create' : 'update'}">
                        <c:if test="${not empty app}">
                            <input type="hidden" name="id" value="${app.id}">
                        </c:if>
                        
                        <h3 class="card-title-design mb-4">基本信息</h3>
                        
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                                ${error}
                            </div>
                        </c:if>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">姓名</label>
                                    <input type="text" class="form-control input-design" name="name" value="${app.name}" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">学号</label>
                                    <input type="text" class="form-control input-design" name="studentId" value="${app.studentId}" required>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">专业</label>
                                    <input type="text" class="form-control input-design" name="major" value="${app.major}" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">班级/年级</label>
                                    <input type="text" class="form-control input-design" name="grade" value="${app.grade}" required>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">手机号</label>
                                    <input type="text" class="form-control input-design" name="phone" value="${app.phone}" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label required">邮箱</label>
                                    <input type="email" class="form-control input-design" name="email" value="${app.email}" required>
                                </div>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label class="form-label required">申请理由/备注</label>
                            <textarea class="form-control input-design" name="reason" rows="5" required>${app.reason}</textarea>
                        </div>
                        <div class="mb-4">
                            <label class="form-label">处理状态</label>
                            <select name="status" class="form-select input-design">
                                <option value="1" ${app.status==1 ? 'selected' : '' }>待处理</option>
                                <option value="2" ${app.status==2 ? 'selected' : '' }>已通过</option>
                                <option value="0" ${app.status==0 ? 'selected' : '' }>已拒绝</option>
                            </select>
                        </div>
                        
                        <div class="d-flex justify-content-end" style="gap: 12px;">
                            <a href="manage" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 11px 20px;">取消</a>
                            <button type="submit" class="btn btn-brand">${empty app ? '立即添加' : '保存更改'}</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>