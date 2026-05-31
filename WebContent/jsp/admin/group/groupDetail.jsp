<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊详情" />
</jsp:include>

<style>
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .btn-back {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    
    .btn-back:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }
    
    .info-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
        height: 100%;
    }
    
    .info-card-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 20px 24px;
    }
    
    .info-card-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: white;
        margin: 0;
    }
    
    .info-card-body {
        padding: 24px;
    }
    
    .info-list {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    
    .info-list-item {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }
    
    .info-list-item:last-child {
        border-bottom: none;
    }
    
    .info-label {
        color: var(--text-muted);
        font-size: 0.88rem;
    }
    
    .info-value {
        color: var(--text-dark);
        font-weight: 500;
        font-size: 0.88rem;
    }
    
    .badge-status {
        display: inline-flex;
        align-items: center;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
        font-size: 0.75rem;
        font-weight: 500;
    }
    
    .badge-success {
        background: rgba(16, 185, 129, 0.1);
        color: #059669;
    }
    
    .badge-danger {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
    }
    
    .btn-primary-custom {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
        text-decoration: none;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
    }
    
    .btn-primary-custom:hover {
        background: var(--primary-600);
        color: white;
    }
    
    .btn-success-custom {
        background: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
    }
    
    .btn-success-custom:hover {
        background: #059669;
        color: white;
    }
    
    .btn-danger-custom {
        background: #dc2626;
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        width: 100%;
    }
    
    .btn-danger-custom:hover {
        background: #b91c1c;
        color: white;
    }
    
    .messages-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .messages-card-header {
        background: var(--bg-white);
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .messages-card-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }
    
    .btn-view-all {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-view-all:hover {
        background: var(--brand-blue);
        color: white;
    }
    
    .table-design {
        width: 100%;
        border-collapse: collapse;
    }
    
    .table-design th {
        font-weight: 600;
        color: var(--text-secondary);
        padding: 14px 16px;
        text-align: left;
        font-size: 0.81rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        border-bottom: 2px solid var(--border-gray);
        background: var(--bg-light-gray);
    }
    
    .table-design td {
        padding: 16px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-size: 0.88rem;
    }
    
    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }
    
    .btn-delete-msg {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-delete-msg:hover {
        background: #dc2626;
        color: white;
    }
    
    .form-select {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-size: 0.88rem;
        transition: all 0.3s ease;
    }
    
    .form-select:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }
    
    .form-control {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-size: 0.88rem;
        transition: all 0.3s ease;
    }
    
    .form-control:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }
    
    .empty-state {
        text-align: center;
        padding: 40px;
        color: var(--text-muted);
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">群聊详情</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=list" class="btn-back">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                        返回列表
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${success}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${error}
            </div>
        </c:if>
        
        <div class="row g-3">
            <div class="col-md-4">
                <div class="info-card">
                    <div class="card-header info-card-header">
                        <h3 class="info-card-title">群聊信息</h3>
                    </div>
                    <div class="card-body info-card-body">
                        <ul class="info-list">
                            <li class="info-list-item">
                                <span class="info-label">群聊名称</span>
                                <span class="info-value">${group.groupName}</span>
                            </li>
                            <li class="info-list-item">
                                <span class="info-label">所属活动</span>
                                <span class="info-value">${group.activityName}</span>
                            </li>
                            <li class="info-list-item">
                                <span class="info-label">群主</span>
                                <span class="info-value">${group.ownerName}</span>
                            </li>
                            <li class="info-list-item">
                                <span class="info-label">成员数</span>
                                <span class="info-value">${group.memberCount}</span>
                            </li>
                            <li class="info-list-item">
                                <span class="info-label">创建时间</span>
                                <span class="info-value">${group.createdAt}</span>
                            </li>
                            <li class="info-list-item">
                                <span class="info-label">状态</span>
                                <span class="info-value">
                                    <c:choose>
                                        <c:when test="${group.isMuted == 1}">
                                            <span class="badge-status badge-danger">已禁言</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-status badge-success">正常</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </li>
                            <c:if test="${group.isMuted == 1}">
                                <li class="info-list-item">
                                    <span class="info-label">禁言原因</span>
                                    <span class="info-value">${group.muteReason}</span>
                                </li>
                                <li class="info-list-item">
                                    <span class="info-label">禁言截止</span>
                                    <span class="info-value">${group.mutedUntil}</span>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </div>
                
                <div class="card info-card" style="margin-top: 16px;">
                    <div class="card-body" style="padding: 20px;">
                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" class="btn-primary-custom">
                            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                            进入群聊
                        </a>
                    </div>
                </div>
                
                <div class="card info-card" style="margin-top: 16px;">
                    <div class="card-header info-card-header">
                        <h3 class="info-card-title">禁言管理</h3>
                    </div>
                    <div class="card-body" style="padding: 20px;">
                        <c:choose>
                            <c:when test="${group.isMuted == 1}">
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="unmute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <button type="submit" class="btn-success-custom" onclick="return confirm('确定要解除禁言吗？')">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                                        解除禁言
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="mute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <div class="mb-3">
                                        <label class="form-label" style="font-weight: 500; font-size: 0.88rem;">禁言时长</label>
                                        <select class="form-select" name="duration" required style="width: 100%;">
                                            <option value="1">1小时</option>
                                            <option value="6">6小时</option>
                                            <option value="12">12小时</option>
                                            <option value="24">24小时</option>
                                            <option value="72">3天</option>
                                            <option value="168">7天</option>
                                            <option value="0">永久</option>
                                        </select>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label" style="font-weight: 500; font-size: 0.88rem;">禁言原因</label>
                                        <input type="text" class="form-control" name="reason" placeholder="请输入禁言原因" required>
                                    </div>
                                    <button type="submit" class="btn-danger-custom">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 8px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
                                        禁言该群
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <div class="col-md-8">
                <div class="messages-card">
                    <div class="messages-card-header">
                        <h3 class="messages-card-title">最近消息（${totalMessages}条）</h3>
                        <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}" class="btn-view-all">查看全部</a>
                    </div>
                    <div class="table-responsive">
                        <table class="table-design">
                            <thead>
                                <tr>
                                    <th>发送者</th>
                                    <th>内容</th>
                                    <th>时间</th>
                                    <th style="width: 80px;">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="msg" items="${recentMessages}">
                                    <tr>
                                        <td style="font-weight: 500;">${msg.senderName}</td>
                                        <td style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${msg.content}</td>
                                        <td style="color: var(--text-muted);">${msg.sentAt}</td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/group/admin" method="POST" style="display: inline;">
                                                <input type="hidden" name="action" value="deleteMessage">
                                                <input type="hidden" name="messageId" value="${msg.id}">
                                                <input type="hidden" name="groupId" value="${group.id}">
                                                <button type="submit" class="btn-delete-msg" onclick="return confirm('确定要删除该消息吗？')">删除</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty recentMessages}">
                                    <tr>
                                        <td colspan="4" class="empty-state">暂无消息记录</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />