<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="申请活动" />
</jsp:include>

<style>
    .apply-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .apply-card-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 24px 32px;
        border: none;
    }
    
    .apply-card-title {
        font-family: var(--font-display);
        font-size: 1.5rem;
        font-weight: 600;
        color: white;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .apply-card-body {
        padding: 32px;
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
    
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .page-subtitle {
        color: var(--text-muted);
        font-size: 0.94rem;
        margin-top: 8px;
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">申请活动</h2>
                <p class="page-subtitle">填写活动信息提交申请，等待管理员审核</p>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/activity" method="POST" class="card apply-card">
                    <div class="card-header apply-card-header">
                        <h3 class="apply-card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="5" width="16" height="16" rx="2"></rect><line x1="16" y1="3" x2="16" y2="7"></line><line x1="8" y1="3" x2="8" y2="7"></line><line x1="4" y1="11" x2="20" y2="11"></line></svg>
                            活动信息
                        </h3>
                    </div>
                    <div class="card-body apply-card-body">
                        <div class="mb-4">
                            <label class="form-label required">活动标题</label>
                            <input type="text" class="form-control" name="title" placeholder="请输入活动标题" required>
                        </div>
                        
                        <div class="mb-4">
                            <label class="form-label">活动描述</label>
                            <textarea class="form-control" name="description" rows="4" placeholder="请输入活动描述"></textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">活动地点</label>
                                    <input type="text" class="form-control" name="location" placeholder="请输入活动地点">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">最大参与人数</label>
                                    <input type="number" class="form-control" name="maxParticipants" placeholder="不限制请留空" min="0">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">开始时间</label>
                                    <input type="datetime-local" class="form-control" name="startTime">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label class="form-label">结束时间</label>
                                    <input type="datetime-local" class="form-control" name="endTime">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer text-end" style="background: var(--bg-white); border: none; padding: 24px 32px;">
                        <div class="d-flex justify-content-end gap-3">
                            <a href="${pageContext.request.contextPath}/activity/list" class="btn-cancel">取消</a>
                            <button type="submit" class="btn-submit">提交申请</button>
                        </div>
                    </div>
                    <input type="hidden" name="action" value="apply">
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />