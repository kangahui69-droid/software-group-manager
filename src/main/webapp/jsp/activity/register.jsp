<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动报名" />
</jsp:include>

<style>
    .register-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .register-card-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 24px 32px;
    }
    
    .register-card-title {
        font-family: var(--font-display);
        font-size: 1.5rem;
        font-weight: 600;
        color: white;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .register-card-body {
        padding: 32px;
    }
    
    .activity-info-card {
        background: var(--bg-light-gray);
        border-radius: var(--radius-comfortable);
        padding: 24px;
        margin-bottom: 32px;
    }
    
    .activity-info-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 12px;
    }
    
    .activity-info-text {
        color: var(--text-secondary);
        line-height: 1.6;
        margin-bottom: 16px;
    }
    
    .activity-info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
    }
    
    .activity-info-item {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }
    
    .activity-info-label {
        font-size: 0.81rem;
        color: var(--text-muted);
    }
    
    .activity-info-value {
        font-weight: 500;
        color: var(--text-dark);
    }
    
    .form-section {
        margin-bottom: 32px;
    }
    
    .form-section-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 2px solid var(--border-light);
    }
    
    .form-label {
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        font-size: 0.94rem;
    }
    
    .form-control {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 12px 16px;
        font-size: 0.94rem;
        transition: all 0.3s ease;
    }
    
    .form-control:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }
    
    .form-select {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 12px 16px;
        font-size: 0.94rem;
        transition: all 0.3s ease;
    }
    
    .form-select:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }
    
    .form-hint {
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-top: 6px;
    }
    
    .sidebar-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
    }
    
    .sidebar-card-header {
        background: var(--bg-light-gray);
        padding: 16px 20px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .sidebar-card-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }
    
    .sidebar-card-body {
        padding: 0;
    }
    
    .notice-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px 20px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .notice-item:last-child {
        border-bottom: none;
    }
    
    .notice-icon {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        margin-top: 2px;
    }
    
    .notice-text {
        font-size: 0.88rem;
        color: var(--text-secondary);
        line-height: 1.5;
    }
    
    .btn-submit {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 28px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-submit:hover {
        background: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }
    
    .btn-cancel {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-cancel:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }
    
    .alert-success {
        background: rgba(16, 185, 129, 0.1);
        border: 1px solid rgba(16, 185, 129, 0.2);
        color: #059669;
        border-radius: var(--radius-standard);
        padding: 16px;
        margin-bottom: 24px;
    }
    
    .alert-error {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 16px;
        margin-bottom: 24px;
    }
    
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">活动报名</h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity/detail/${activity.id}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-12">
                <c:if test="${not empty error}">
                    <div class="alert-error" role="alert">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><line x1="11" y1="12" x2="12" y2="12"></line><line x1="11" y1="16" x2="12" y2="16"></line></svg>
                        ${error}
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert-success" role="alert">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                        ${success}
                    </div>
                </c:if>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-8">
                <div class="card register-card">
                    <div class="card-header register-card-header">
                        <h3 class="register-card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                            活动报名
                        </h3>
                    </div>
                    <div class="card-body register-card-body">
                        <form id="registerForm" method="post" action="${pageContext.request.contextPath}/activity/register" enctype="multipart/form-data">
                            <input type="hidden" name="activityId" value="${activity.id}">
                            
                            <div class="form-section">
                                <h4 class="form-section-title">活动信息</h4>
                                <div class="activity-info-card">
                                    <h5 class="activity-info-title">${activity.title}</h5>
                                    <p class="activity-info-text">${activity.description}</p>
                                    <div class="activity-info-grid">
                                        <div class="activity-info-item">
                                            <span class="activity-info-label">开始时间</span>
                                            <span class="activity-info-value">${activity.startTime}</span>
                                        </div>
                                        <div class="activity-info-item">
                                            <span class="activity-info-label">结束时间</span>
                                            <span class="activity-info-value">${activity.endTime}</span>
                                        </div>
                                        <div class="activity-info-item">
                                            <span class="activity-info-label">地点</span>
                                            <span class="activity-info-value">${activity.location}</span>
                                        </div>
                                        <div class="activity-info-item">
                                            <span class="activity-info-label">分类</span>
                                            <span class="activity-info-value">${activity.category}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-section">
                                <h4 class="form-section-title">参与者信息</h4>
                                <div class="mb-3">
                                    <label class="form-label required">姓名</label>
                                    <input type="text" class="form-control" name="name" placeholder="请输入姓名" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">电话</label>
                                    <input type="tel" class="form-control" name="phone" placeholder="请输入联系电话" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">邮箱</label>
                                    <input type="email" class="form-control" name="email" placeholder="请输入邮箱地址">
                                </div>
                            </div>

                            <div class="form-section">
                                <h4 class="form-section-title">上传材料</h4>
                                <div class="mb-3">
                                    <label class="form-label">活动相关材料（最多上传3张图片）</label>
                                    <input type="file" class="form-control" name="files" multiple accept="image/jpeg,image/jpg,image/png,image/gif,image/webp">
                                    <p class="form-hint">支持JPG、PNG、GIF格式，每张图片大小不超过1MB</p>
                                </div>
                            </div>

                            <div class="form-section">
                                <h4 class="form-section-title">团队成员</h4>
                                <div class="mb-3">
                                    <label class="form-label">添加团队成员（可选）</label>
                                    <select class="form-select" name="members" multiple style="min-height: 120px;">
                                        <c:forEach var="member" items="${availableMembers}">
                                            <option value="${member.id}">${member.username}</option>
                                        </c:forEach>
                                    </select>
                                    <p class="form-hint">按住Ctrl键可多选</p>
                                </div>
                            </div>

                            <div class="form-section" style="margin-bottom: 0;">
                                <h4 class="form-section-title">备注信息</h4>
                                <div class="mb-3">
                                    <label class="form-label">备注</label>
                                    <textarea class="form-control" name="remark" rows="3" placeholder="请输入备注信息"></textarea>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end" style="margin-top: 32px;">
                                <a href="${pageContext.request.contextPath}/activity/list" class="btn-cancel me-3">取消</a>
                                <button type="submit" class="btn-submit">提交报名</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="sidebar-card">
                    <div class="sidebar-card-header">
                        <h3 class="sidebar-card-title">报名须知</h3>
                    </div>
                    <div class="sidebar-card-body">
                        <div class="notice-item">
                            <div class="notice-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            </div>
                            <span class="notice-text">请确保填写的信息真实有效</span>
                        </div>
                        <div class="notice-item">
                            <div class="notice-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            </div>
                            <span class="notice-text">上传的图片大小不能超过1MB</span>
                        </div>
                        <div class="notice-item">
                            <div class="notice-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            </div>
                            <span class="notice-text">报名成功后将收到确认通知</span>
                        </div>
                        <div class="notice-item">
                            <div class="notice-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            </div>
                            <span class="notice-text">如需取消报名，请提前联系组织者</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
<script>
    document.getElementById('registerForm').addEventListener('submit', function(e) {
        const name = this.name.value;
        const phone = this.phone.value;
        const files = this.files.files;
        
        if (!name) {
            alert('请输入姓名');
            e.preventDefault();
            return;
        }
        
        if (!phone) {
            alert('请输入联系电话');
            e.preventDefault();
            return;
        }
        
        if (files.length > 3) {
            alert('最多只能上传3张图片');
            e.preventDefault();
            return;
        }
        
        for (let i = 0; i < files.length; i++) {
            if (files[i].size > 1 * 1024 * 1024) {
                alert('每张图片大小不能超过1MB');
                e.preventDefault();
                return;
            }
        }
    });
</script>