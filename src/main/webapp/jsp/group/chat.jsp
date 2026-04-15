<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="${group.groupName} - 群聊" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    .chat-container {
        height: calc(100vh - 250px);
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
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
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

    .message.mine {
        flex-direction: row-reverse;
    }

    .message-avatar {
        width: 42px;
        height: 42px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 1rem;
        flex-shrink: 0;
    }

    .message.mine .message-avatar {
        background: linear-gradient(135deg, #10b981, #059669);
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
        font-size: 0.94rem;
    }

    .message.mine .message-bubble {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-600));
        color: white;
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
    }

    .chat-input {
        padding: 20px 24px;
        border-top: 1px solid var(--border-light);
        background: white;
    }

    .chat-input input {
        border-radius: var(--radius-generous);
        border: 1px solid var(--border-gray);
        padding: 12px 18px;
        font-size: 0.94rem;
        transition: all 0.3s ease;
        flex: 1;
    }

    .chat-input input:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .btn-send {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-generous);
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
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-members {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border-radius: var(--radius-generous);
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
        border-radius: var(--radius-generous);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
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
        margin-left: 8px;
    }

    .message-file {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background: white;
        border: 1px solid var(--border-light);
        border-radius: 12px;
        text-decoration: none;
        color: var(--text-dark);
        max-width: 300px;
        box-shadow: var(--shadow-standard);
    }

    .message.mine .message-file {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-600));
        border-color: var(--brand-blue);
        color: white;
    }

    .message-file:hover {
        background: var(--bg-light-gray);
        text-decoration: none;
    }

    .message.mine .message-file:hover {
        background: var(--primary-600);
        color: white;
    }

    .file-icon {
        font-size: 28px;
        margin-right: 12px;
        color: var(--brand-blue);
    }

    .message.mine .file-icon {
        color: white;
    }

    .file-info {
        flex: 1;
        min-width: 0;
    }

    .file-name {
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 0.88rem;
    }

    .file-size {
        font-size: 0.75rem;
        color: var(--text-muted);
    }

    .message.mine .file-size {
        color: rgba(255, 255, 255, 0.8);
    }

    .file-download {
        margin-left: 12px;
        font-size: 20px;
    }

    .chat-input-wrapper {
        display: flex;
        gap: 12px;
    }

    .file-upload-btn {
        position: relative;
        overflow: hidden;
        background: transparent;
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-generous);
        padding: 10px 16px;
        font-size: 0.88rem;
        transition: all 0.3s ease;
    }

    .file-upload-btn:hover {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }

    .file-upload-btn input[type=file] {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        cursor: pointer;
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

    .group-info-item {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
        font-size: 0.88rem;
    }

    .group-info-label {
        color: var(--text-muted);
    }

    .group-info-value {
        color: var(--text-dark);
        font-weight: 500;
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

    .btn-outline-danger {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border-radius: var(--radius-generous);
        padding: 6px 10px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }

    .btn-outline-danger:hover {
        background: #dc2626;
        color: white;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty param.success}">
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${param.success}
            </div>
        </c:if>

        <div class="row">
            <div class="col-md-9">
                <div class="chat-container">
                    <div class="chat-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h4 style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; margin: 0 0 4px 0;">${group.groupName}</h4>
                                <p style="font-size: 0.81rem; opacity: 0.9; margin: 0;">${members.size()} 名成员<c:if test="${isOwner}"> · 您是群主</c:if></p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button class="btn-members" data-bs-toggle="modal" data-bs-target="#membersModal">
                                    <i class="bi bi-people"></i> 成员
                                </button>
                                <a href="${pageContext.request.contextPath}/group/my-groups" class="btn-members">
                                    <i class="bi bi-arrow-left"></i> 返回
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="chat-messages" id="chatMessages">
                        <c:forEach var="msg" items="${messages}">
                            <div class="message ${msg.senderId == sessionScope.user.id ? 'mine' : ''}">
                                <div class="message-avatar">
                                    <c:choose>
                                        <c:when test="${not empty msg.senderAvatarFileId}">
                                            <img src="${pageContext.request.contextPath}/file?action=view&id=${msg.senderAvatarFileId}" 
                                                 alt="${msg.senderName}" style="width: 42px; height: 42px; object-fit: cover; border-radius: var(--radius-comfortable);">
                                        </c:when>
                                        <c:otherwise>
                                            ${not empty msg.senderName ? msg.senderName.charAt(0) : 'U'}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="message-content">
                                    <div class="message-sender">${msg.senderName}</div>
                                    <c:choose>
                                        <c:when test="${msg.messageType == 'FILE'}">
                                            <a href="${pageContext.request.contextPath}/group/downloadFile?fileId=${msg.fileId}&fileName=${msg.fileName}" class="message-file" download>
                                                <i class="bi bi-file-earmark-fill file-icon"></i>
                                                <div class="file-info">
                                                    <div class="file-name" title="${msg.fileName}">${msg.fileName}</div>
                                                    <div class="file-size">${msg.formattedFileSize}</div>
                                                </div>
                                                <i class="bi bi-download file-download"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="message-bubble">${msg.content}</div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="message-time">
                                        <fmt:formatDate value="${msg.sentAt}" pattern="HH:mm:ss" />
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <c:if test="${empty messages}">
                            <div class="empty-chat">
                                <div class="empty-chat-icon">
                                    <i class="bi bi-chat-dots"></i>
                                </div>
                                <p>暂无消息，开始聊天吧！</p>
                            </div>
                        </c:if>
                    </div>

                    <c:if test="${group.isMuted == 1}">
                        <div style="background: rgba(239, 68, 68, 0.1); border-top: 1px solid rgba(239, 68, 68, 0.2); padding: 12px 24px;">
                            <div style="color: #dc2626; font-size: 0.88rem;">
                                <i class="bi bi-exclamation-triangle me-2"></i><strong>当前群聊已被禁言</strong>
                                <c:if test="${not empty group.muteReason}"><br>禁言原因：${group.muteReason}</c:if>
                                <c:if test="${not empty group.mutedUntil}"><br>禁言解除时间：<fmt:formatDate value="${group.mutedUntil}" pattern="yyyy-MM-dd HH:mm" /></c:if>
                                <c:if test="${empty group.mutedUntil}"><br>禁言解除时间：永久禁言</c:if>
                            </div>
                        </div>
                    </c:if>

                    <div class="chat-input">
                        <form action="${pageContext.request.contextPath}/group/send" method="post" id="messageForm" class="mb-2">
                            <input type="hidden" name="groupId" value="${group.id}">
                            <div class="chat-input-wrapper">
                                <input type="text" name="content" placeholder="输入消息..." 
                                       autocomplete="off" required ${group.isMuted == 1 ? 'disabled' : ''}>
                                <button type="submit" class="btn-send" ${group.isMuted == 1 ? 'disabled' : ''}>
                                    <i class="bi bi-send"></i> 发送
                                </button>
                            </div>
                        </form>
                        <form action="${pageContext.request.contextPath}/group/sendFile" method="post" enctype="multipart/form-data" id="fileForm">
                            <input type="hidden" name="groupId" value="${group.id}">
                            <div class="chat-input-wrapper">
                                <div class="file-upload-btn" ${group.isMuted == 1 ? 'style="opacity: 0.5; pointer-events: none;"' : ''}>
                                    <i class="bi bi-paperclip"></i> 选择文件
                                    <input type="file" name="file" id="fileInput" accept="*/*" ${group.isMuted == 1 ? 'disabled' : ''}>
                                </div>
                                <span class="file-name-display" id="fileNameDisplay" style="flex: 1; color: var(--text-muted); font-size: 0.88rem;"></span>
                                <button type="submit" class="btn-send" id="uploadBtn" style="background: #10b981; display: none;">
                                    <i class="bi bi-cloud-upload"></i> 上传
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
                    <div style="padding: 20px;">
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
                        <div class="group-info-item" style="border-bottom: none;">
                            <span class="group-info-label">创建时间</span>
                            <span class="group-info-value"><fmt:formatDate value="${group.createdAt}" pattern="yyyy-MM-dd" /></span>
                        </div>
                    </div>
                    <c:if test="${isOwner}">
                        <div style="padding: 0 20px 20px;">
                            <a href="${pageContext.request.contextPath}/group/add-members?groupId=${group.id}" class="btn-send w-100" style="justify-content: center;">
                                <i class="bi bi-person-plus"></i> 邀请成员
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="membersModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light)); color: white; border: none;">
                <h5 class="modal-title">群成员 (${members.size()})</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" style="filter: brightness(0) invert(1);"></button>
            </div>
            <div class="modal-body member-list">
                <c:forEach var="member" items="${members}">
                    <div class="member-item">
                        <div class="member-avatar">
                            <c:choose>
                                <c:when test="${not empty member.avatarFileId}">
                                    <img src="${pageContext.request.contextPath}/file?action=view&id=${member.avatarFileId}" 
                                         alt="${member.name}" style="width: 38px; height: 38px; object-fit: cover; border-radius: var(--radius-generous);">
                                </c:when>
                                <c:otherwise>
                                    ${not empty member.name ? member.name.charAt(0) : (not empty member.username ? member.username.charAt(0) : 'U')}
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="member-name">
                            ${not empty member.name ? member.name : member.username}
                            <c:if test="${member.role == 'OWNER'}">
                                <span class="badge-owner">群主</span>
                            </c:if>
                        </div>
                        <c:if test="${isOwner && member.role != 'OWNER'}">
                            <form action="${pageContext.request.contextPath}/group/member/remove" method="post">
                                <input type="hidden" name="groupId" value="${group.id}">
                                <input type="hidden" name="userId" value="${member.userId}">
                                <button type="submit" class="btn-outline-danger"
                                        onclick="return confirm('确定要移除该成员吗？')">
                                    <i class="bi bi-x-lg"></i>
                                </button>
                            </form>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var messagesDiv = document.getElementById('chatMessages');
    if (messagesDiv) {
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }

    var fileInput = document.getElementById('fileInput');
    var fileNameDisplay = document.getElementById('fileNameDisplay');
    var uploadBtn = document.getElementById('uploadBtn');
    var fileForm = document.getElementById('fileForm');

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                var fileName = this.files[0].name;
                fileNameDisplay.textContent = fileName;
                uploadBtn.style.display = 'flex';
            } else {
                fileNameDisplay.textContent = '';
                uploadBtn.style.display = 'none';
            }
        });
    }

    if (fileForm) {
        fileForm.addEventListener('submit', function(e) {
            if (!fileInput.files || fileInput.files.length === 0) {
                e.preventDefault();
                return false;
            }
        });
    }
});
</script>

<jsp:include page="../common/layout_bottom.jsp" />