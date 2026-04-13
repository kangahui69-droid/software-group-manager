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
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    编辑成员信息
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/admin/member/edit" method="POST" class="card-design">
                    <div class="card-body p-4">
                        <h3 class="card-title-design mb-4">成员信息</h3>
                        
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                                ${error}
                            </div>
                        </c:if>
                        
                        <input type="hidden" name="id" value="${user.id}">
                        <div class="mb-4">
                            <label class="form-label required">用户名</label>
                            <input type="text" class="form-control input-design" name="username" value="${user.username}" placeholder="请输入用户名" required>
                        </div>
                        <div class="mb-4">
                            <label class="form-label required">角色</label>
                            <select class="form-select input-design" name="role" required>
                                <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                                <option value="MEMBER" ${user.role == 'MEMBER' ? 'selected' : ''}>成员</option>
                            </select>
                        </div>
                        
                        <div class="d-flex justify-content-end" style="gap: 12px;">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 11px 20px;">取消</a>
                            <button type="submit" class="btn btn-brand">保存更改</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>