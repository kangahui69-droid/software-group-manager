<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; }
    .input-design:focus { border-color: var(--brand-blue); box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1); outline: none; }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); box-shadow: var(--shadow-standard); }
    .btn-secondary-light { background: var(--bg-light-gray); color: #333333; border-radius: var(--radius-standard); padding: 11px 20px; border: none; transition: all 0.3s ease; }
    .btn-secondary-light:hover { background: #e5e5e5; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .section-heading { font-family: var(--font-display); font-size: 1.75rem; font-weight: 500; color: var(--text-dark); margin-bottom: 20px; }
    .info-card { background: linear-gradient(135deg, rgba(20, 86, 240, 0.05), rgba(234, 94, 193, 0.05)); border-radius: var(--radius-comfortable); padding: 24px; border: 1px solid var(--border-light); }
    .info-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border-light); }
    .info-item:last-child { border-bottom: none; }
    .info-icon { width: 40px; height: 40px; background: var(--brand-blue); border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; margin-right: 16px; }
    .info-icon svg { color: white; }
    .info-text { font-size: 0.94rem; color: var(--text-secondary); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    添加成员
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-body p-4">
                        <form method="post" action="${pageContext.request.contextPath}/admin/member/">
                            <input type="hidden" name="action" value="add">
                            
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca;">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><line x1="11" y1="12" x2="12" y2="12"></line><line x1="11" y1="16" x2="12" y2="16"></line></svg>
                                    ${error}
                                </div>
                            </c:if>
                            
                            <div class="mb-4">
                                <label class="form-label required">用户名</label>
                                <input type="text" class="form-control input-design" name="username" placeholder="请输入用户名（登录用）" required>
                            </div>
                            
                            <div class="mb-4">
                                <label class="form-label required">姓名</label>
                                <input type="text" class="form-control input-design" name="name" placeholder="请输入真实姓名" required>
                            </div>
                            
                            <div class="mb-4">
                                <label class="form-label required">密码</label>
                                <input type="password" class="form-control input-design" name="password" placeholder="请输入密码" required>
                            </div>
                            
                            <div class="mb-4">
                                <label class="form-label required">角色</label>
                                <select class="form-select input-design" name="role" required>
                                    <option value="MEMBER">普通成员</option>
                                    <option value="ADMIN">管理员</option>
                                </select>
                            </div>
                            
                            <div class="mb-4">
                                <label class="form-label required">用户类型</label>
                                <select class="form-select input-design" name="userType" required>
                                    <option value="STUDENT">学生</option>
                                    <option value="TEACHER">教师</option>
                                </select>
                            </div>
                            
                            <div class="mb-4">
                                <label class="form-label">状态</label>
                                <select class="form-select input-design" name="status">
                                    <option value="1">启用</option>
                                    <option value="0">禁用</option>
                                </select>
                            </div>
                            
                            <div class="d-flex justify-content-end" style="gap: 12px;">
                                <a href="${pageContext.request.contextPath}/admin/member/list" class="btn btn-secondary-light">取消</a>
                                <button type="submit" class="btn btn-brand">保存</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="info-card">
                    <h3 class="section-heading">添加成员须知</h3>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                        </div>
                        <span class="info-text">用户名不能重复</span>
                    </div>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                        </div>
                        <span class="info-text">密码会自动加密存储</span>
                    </div>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        </div>
                        <span class="info-text">角色选择后可以修改</span>
                    </div>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                        </div>
                        <span class="info-text">用户类型区分教师和学生</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>