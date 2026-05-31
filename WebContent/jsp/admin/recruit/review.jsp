<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .input-design { border-radius: var(--radius-standard); border: 1px solid var(--border-gray); padding: 10px 16px; font-family: var(--font-ui); transition: all 0.3s ease; background: var(--bg-light-gray); }
    .btn-brand { background-color: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-brand:hover { background-color: var(--primary-600); color: white; transform: translateY(-2px); }
    .btn-success { background-color: #10b981; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-success:hover { background-color: #059669; color: white; }
    .btn-danger { background-color: #ef4444; color: white; border-radius: var(--radius-standard); padding: 11px 20px; font-weight: 600; border: none; transition: all 0.3s ease; }
    .btn-danger:hover { background-color: #dc2626; color: white; }
    .btn-outline-brand { background: transparent; color: var(--brand-blue); border: 2px solid var(--brand-blue); border-radius: var(--radius-standard); padding: 9px 18px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; }
    .btn-outline-brand:hover { background: var(--brand-blue); color: white; }
    .form-label { font-family: var(--font-ui); font-weight: 500; color: var(--text-dark); margin-bottom: 8px; }
    .card-title-design { font-family: var(--font-display); font-size: 1.5rem; font-weight: 500; color: var(--text-dark); }
    .badge-design { font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; padding: 4px 12px; border-radius: var(--radius-pill); display: inline-block; }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-approved { background: #d1fae5; color: #065f46; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
    .badge-member { background: var(--primary-500); color: white; }
    .view-field { margin-bottom: 20px; }
    .view-field label { font-size: 0.88rem; color: var(--text-muted); margin-bottom: 4px; display: block; }
    .input-group-design { display: flex; align-items: center; background: var(--bg-light-gray); border-radius: var(--radius-standard); padding: 12px 16px; }
    .input-group-design .icon-wrapper { width: 40px; height: 40px; background: var(--brand-blue); border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; margin-right: 16px; flex-shrink: 0; }
    .input-group-design .icon-wrapper svg { color: white; }
    .input-group-design textarea { border: none; background: transparent; flex: 1; font-size: 1rem; color: var(--text-dark); resize: none; }
    .input-group-design input { border: none; background: transparent; flex: 1; font-size: 1rem; color: var(--text-dark); }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center mb-4">
            <div class="col">
                <h2 class="page-title mb-0">
                    审核申请
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/admin/recruit/manage" class="btn btn-outline-brand">
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
            <div class="col-12">
                <div class="card-design">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center mb-4 pb-4 border-bottom" style="border-color: var(--border-light);">
                            <h3 class="card-title-design mb-0">申请详情</h3>
                            <span class="badge-design badge-${app.status == 0 ? 'pending' : (app.status == 1 ? 'pending' : (app.status == 2 ? 'rejected' : 'approved'))}">
                                <c:choose>
                                    <c:when test="${app.status == 1}">待处理</c:when>
                                    <c:when test="${app.status == 2}">再接再厉</c:when>
                                    <c:when test="${app.status == 3}">正式成员</c:when>
                                    <c:otherwise>未知</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>姓名</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="8" r="4"></circle><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2"></path></svg>
                                        </div>
                                        <input type="text" value="${app.name}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>学号</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="4" width="16" height="16" rx="2"></rect><path d="M9 9h6v6H9z"></path></svg>
                                        </div>
                                        <input type="text" value="${app.studentId}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>专业</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
                                        </div>
                                        <input type="text" value="${app.major}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>年级</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                        </div>
                                        <input type="text" value="${app.grade}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>手机号</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 4h4l2 5l-2.5 1.5a11 11 0 0 0 5 5l1.5 -2.5l5 2v4a2 2 0 0 1 -2 2a16 16 0 0 1 -15 -15a2 2 0 0 1 2 -2"></path></svg>
                                        </div>
                                        <input type="text" value="${app.phone}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="view-field">
                                    <label>邮箱</label>
                                    <div class="input-group-design">
                                        <div class="icon-wrapper">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2H5a2 2 0 0 1 -2 -2V7z"></path><polyline points="3 7 12 13 21 7"></polyline></svg>
                                        </div>
                                        <input type="text" value="${app.email}" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="view-field mb-0">
                                    <label>申请理由</label>
                                    <div class="input-group-design" style="align-items: flex-start;">
                                        <div class="icon-wrapper" style="align-self: flex-start; margin-top: 2px;">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>
                                        </div>
                                        <textarea class="form-control" rows="4" readonly style="border: none; background: transparent; flex: 1; font-size: 1rem; color: var(--text-dark); resize: none;">${app.reason}</textarea>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="d-flex justify-content-end mt-4 pt-4 border-top" style="border-color: var(--border-light); gap: 12px;">
                            <c:if test="${app.status == 1}">
                                <form action="${pageContext.request.contextPath}/admin/recruit/reject" method="POST" style="display: inline;">
                                    <input type="hidden" name="id" value="${app.id}">
                                    <button type="submit" class="btn btn-danger" onclick="return confirm('确定要打回该申请吗？状态将变为再接再厉。')">打回 (再接再厉)</button>
                                </form>
                                <form action="${pageContext.request.contextPath}/admin/recruit/approve" method="POST" style="display: inline;">
                                    <input type="hidden" name="id" value="${app.id}">
                                    <button type="submit" class="btn btn-success" onclick="return confirm('确定要通过该申请吗？将自动创建账号。')">通过 (正式成员)</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>