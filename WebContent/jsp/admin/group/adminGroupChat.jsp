<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="${group.groupName} - 群聊" />
</jsp:include>

<style>
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
    
    .chat-container {
        height: calc(100vh - 300px);
        min-height: 450px;
        display: flex;
        flex-direction: column;
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        overflow: hidden;
    }
    
    .chat-header {
        padding: 20px 24px;
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        color: white;
    }
    
    .chat-header-title {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        margin: 0 0 4px 0;
    }
    
    .chat-header-subtitle {
        font-size: 0.81rem;
        opacity: 0.9;
        margin: 0;
    }
    
    .btn-members {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    
    .btn-members:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }
    
    .btn-invite {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    
    .btn-invite:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }
    
    .chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 24px;
        background: #f8fafc;
    }
    
    .message {
        margin-bottom: 20px;
        display: flex;
        animation: fadeIn 0.3s ease;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .message.mine {
        flex-direction: row-reverse;
    }
    
    .message-avatar {
        width: 42px;
        height: 42px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-500));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 1rem;
        flex-shrink: 0;
    }
    
    .message.mine .message-avatar {
        background: linear-gradient(135deg, #10b981, #34d399);
    }
    
    .message-content {
        max-width: 65%;
        margin: 0 12px;
    }
    
    .message-bubble {
        padding: 14px 18px;
        border-radius: 18px;
        background: white;
        word-wrap: break-word;
        box-shadow: var(--shadow-standard);
        line-height: 1.5;
    }
    
    .message.mine .message-bubble {
        background: var(--brand-blue);
        color: white;
        box-shadow: none;
    }
    
    .message-sender {
        font-size: 0.75rem;
        color: var(--text-muted);
        margin-bottom: 4px;
        font-weight: 500;
    }
    
    .message.mine .message-sender {
        text-align: right;
        color: #10b981;
    }
    
    .message-time {
        font-size: 0.69rem;
        color: var(--text-muted);
        margin-top: 4px;
    }
    
    .message.mine .message-time {
        text-align: right;
        color: rgba(255, 255, 255, 0.7);
    }
    
    .chat-input {
        padding: 20px 24px;
        border-top: 1px solid var(--border-light);
        background: white;
    }
    
    .input-group-chat {
        display: flex;
        gap: 12px;
    }
    
    .chat-input-field {
        flex: 1;
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 12px 18px;
        font-size: 0.94rem;
        transition: all 0.3s ease;
    }
    
    .chat-input-field:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }
    
    .btn-send {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    
    .btn-send:hover {
        background: var(--primary-600);
        color: white;
    }
    
    .group-info-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        overflow: hidden;
        height: 100%;
    }
    
    .group-info-header {
        background: var(--bg-light-gray);
        padding: 16px 20px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .group-info-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }
    
    .group-info-body {
        padding: 20px;
    }
    
    .group-info-item {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid var(--border-light);
        font-size: 0.88rem;
    }
    
    .group-info-item:last-child {
        border-bottom: none;
    }
    
    .group-info-label {
        color: var(--text-muted);
    }
    
    .group-info-value {
        color: var(--text-dark);
        font-weight: 500;
    }
    
    .member-list {
        max-height: 400px;
        overflow-y: auto;
    }
    
    .member-item {
        display: flex;
        align-items: center;
        padding: 14px 20px;
        border-bottom: 1px solid var(--border-light);
        transition: background 0.2s ease;
    }
    
    .member-item:hover {
        background: rgba(20, 86, 240, 0.03);
    }
    
    .member-item:last-child {
        border-bottom: none;
    }
    
    .member-avatar {
        width: 38px;
        height: 38px;
        border-radius: var(--radius-standard);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-500));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 0.88rem;
        margin-right: 12px;
    }
    
    .member-name {
        flex: 1;
        font-weight: 500;
        color: var(--text-dark);
        font-size: 0.88rem;
    }
    
    .badge-owner {
        background: rgba(245, 158, 11, 0.15);
        color: #d97706;
        padding: 3px 8px;
        border-radius: var(--radius-pill);
        font-size: 0.69rem;
        font-weight: 600;
    }
    
    .btn-remove {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 6px 10px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-remove:hover {
        background: #dc2626;
        color: white;
    }
    
    .modal-content {
        border-radius: var(--radius-generous);
        border: none;
        overflow: hidden;
    }
    
    .modal-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        color: white;
        border: none;
        padding: 20px 24px;
    }
    
    .modal-title {
        font-family: var(--font-display);
        font-weight: 600;
    }
    
    .btn-close {
        filter: brightness(0) invert(1);
    }
    
    .modal-body {
        padding: 0;
    }
    
    .empty-chat {
        text-align: center;
        padding: 60px 24px;
        color: var(--text-muted);
    }
    
    .empty-chat-icon {
        font-size: 48px;
        margin-bottom: 16px;
        opacity: 0.5;
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title" style="font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark);">${group.groupName}</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=myGroups" class="btn-back">
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
        <c:if test="${not empty param.success}">
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${param.success}
            </div>
        </c:if>
        
        <div class="row">
            <div class="col-md-9">
                <div class="chat-container">
                    <div class="chat-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h4 class="chat-header-title">${group.groupName}</h4>
                                <p class="chat-header-subtitle">${members.size()} 名成员 · 您是群主</p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button class="btn-members" data-bs-toggle="modal" data-bs-target="#membersModal">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    成员
                                </button>
                                <a href="${pageContext.request.contextPath}/group/admin?action=addMembers&groupId=${group.id}" class="btn-invite">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                    邀请
                                </a>
                            </div>
                        </div>
                    </div>
                    
                    <div class="chat-messages" id="chatMessages">
                        <c:forEach var="msg" items="${messages}">
                            <div class="message ${msg.senderId == sessionScope.user.id ? 'mine' : ''}">
                                <div class="message-avatar">
                                    ${not empty msg.senderName ? msg.senderName.charAt(0) : 'U'}
                                </div>
                                <div class="message-content">
                                    <div class="message-sender">${msg.senderName}</div>
                                    <div class="message-bubble">${msg.content}</div>
                                    <div class="message-time">
                                        <fmt:formatDate value="${msg.sentAt}" pattern="HH:mm:ss" />
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        
                        <c:if test="${empty messages}">
                            <div class="empty-chat">
                                <div class="empty-chat-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                                </div>
                                <p>暂无消息，开始聊天吧！</p>
                            </div>
                        </c:if>
                    </div>
                    
                    <div class="chat-input">
                        <form action="${pageContext.request.contextPath}/group/admin" method="post">
                            <input type="hidden" name="action" value="send">
                            <input type="hidden" name="groupId" value="${group.id}">
                            <div class="input-group-chat">
                                <input type="text" class="chat-input-field" name="content" placeholder="输入消息..." autocomplete="off" required>
                                <button type="submit" class="btn-send">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
                                    发送
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-3">
                <div class="group-info-card">
                    <div class="group-info-header">
                        <h5 class="group-info-title">群信息</h5>
                    </div>
                    <div class="group-info-body">
                        <div class="group-info-item">
                            <span class="group-info-label">群名称</span>
                            <span class="group-info-value">${group.groupName}</span>
                        </div>
                        <div class="group-info-item">
                            <span class="group-info-label">群主</span>
                            <span class="group-info-value">${group.ownerName}</span>
                        </div>
                        <c:if test="${not empty group.activityName}">
                            <div class="group-info-item">
                                <span class="group-info-label">所属活动</span>
                                <span class="group-info-value">${group.activityName}</span>
                            </div>
                        </c:if>
                        <div class="group-info-item">
                            <span class="group-info-label">成员数</span>
                            <span class="group-info-value">${members.size()}</span>
                        </div>
                        <div class="group-info-item">
                            <span class="group-info-label">创建时间</span>
                            <span class="group-info-value"><fmt:formatDate value="${group.createdAt}" pattern="yyyy-MM-dd" /></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="membersModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">群成员 (${members.size()})</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body member-list">
                <c:forEach var="member" items="${members}">
                    <div class="member-item">
                        <div class="member-avatar">
                            ${not empty member.name ? member.name.charAt(0) : (not empty member.username ? member.username.charAt(0) : 'U')}
                        </div>
                        <div class="member-name">
                            ${not empty member.name ? member.name : member.username}
                            <c:if test="${member.role == 'OWNER'}">
                                <span class="badge-owner">群主</span>
                            </c:if>
                        </div>
                        <c:if test="${isOwner && member.role != 'OWNER'}">
                            <form action="${pageContext.request.contextPath}/group/admin" method="post">
                                <input type="hidden" name="action" value="removeMember">
                                <input type="hidden" name="groupId" value="${group.id}">
                                <input type="hidden" name="userId" value="${member.userId}">
                                <button type="submit" class="btn-remove" onclick="return confirm('确定要移除该成员吗？')">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                                </button>
                            </form>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />

<script>
document.addEventListener('DOMContentLoaded', function() {
    var messagesDiv = document.getElementById('chatMessages');
    if (messagesDiv) {
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
});
</script>