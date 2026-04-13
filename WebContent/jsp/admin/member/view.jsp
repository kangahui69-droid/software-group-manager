<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; background: var(--bg-light-gray); }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .section-heading { font-family: var(--font-display); font-size: 1.75rem; font-weight: 500; color: var(--text-dark); margin-bottom: 20px; }
    .info-card { background: linear-gradient(135deg, rgba(20, 86, 240, 0.05), rgba(234, 94, 193, 0.05)); border-radius: var(--radius-comfortable); padding: 24px; border: 1px solid var(--border-light); }
    .info-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border-light); }
    .info-item:last-child { border-bottom: none; }
    .info-icon { width: 40px; height: 40px; background: var(--brand-blue); border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; margin-right: 16px; }
    .info-icon svg { color: white; }
    .info-text { font-size: 0.94rem; color: var(--text-secondary); }
    .view-field { margin-bottom: 20px; }
    .view-field label { font-size: 0.88rem; color: var(--text-muted); margin-bottom: 4px; display: block; }
    .input-group-design { display: flex; align-items: center; background: var(--bg-light-gray); border-radius: var(--radius-standard); padding: 12px 16px; }
    .input-group-design .icon-wrapper { width: 40px; height: 40px; background: var(--brand-blue); border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; margin-right: 16px; flex-shrink: 0; }
    .input-group-design .icon-wrapper svg { color: white; }
    .input-group-design input { border: none; background: transparent; flex: 1; font-size: 1rem; color: var(--text-dark); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    查看成员
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/admin/member/list" class="btn btn-outline-brand">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回列表
                </a>
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
                        <div class="view-field">
                            <label>用户名</label>
                            <div class="input-group-design">
                                <div class="icon-wrapper">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                </div>
                                <input type="text" value="${user.username}" readonly>
                            </div>
                        </div>
                        
                        <div class="view-field">
                            <label>角色</label>
                            <div class="input-group-design">
                                <div class="icon-wrapper">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                </div>
                                <input type="text" value="${user.role == 'ADMIN' ? '管理员' : '普通成员'}" readonly>
                            </div>
                        </div>
                        
                        <div class="view-field">
                            <label>用户类型</label>
                            <div class="input-group-design">
                                <div class="icon-wrapper">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                </div>
                                <input type="text" value="${user.userType == 'TEACHER' ? '教师' : '学生'}" readonly>
                            </div>
                        </div>
                        
                        <div class="view-field mb-0">
                            <label>状态</label>
                            <div class="input-group-design">
                                <div class="icon-wrapper" style="background: ${user.status == 1 ? '#10b981' : '#ef4444'};">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 3l18 9v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V12z"></path></svg>
                                </div>
                                <input type="text" value="${user.status == 1 ? '启用' : '禁用'}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="info-card">
                    <h3 class="section-heading">查看成员须知</h3>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                        </div>
                        <span class="info-text">此页面仅用于查看成员信息</span>
                    </div>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                        </div>
                        <span class="info-text">如需修改信息，请返回列表页操作</span>
                    </div>
                    <div class="info-item">
                        <div class="info-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                        </div>
                        <span class="info-text">密码信息不显示</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>