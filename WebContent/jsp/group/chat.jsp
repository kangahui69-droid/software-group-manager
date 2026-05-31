<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${group.groupName} - 群聊 - 软件小组管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .chat-container {
            height: calc(100vh - 250px);
            min-height: 400px;
            display: flex;
            flex-direction: column;
        }
        .chat-header {
            padding: 15px;
            border-bottom: 1px solid #e0e0e0;
            background: #f8f9fa;
        }
        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 15px;
            background: #f5f5f5;
        }
        .message {
            margin-bottom: 15px;
            display: flex;
        }
        .message.mine {
            flex-direction: row-reverse;
        }
        .message-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #206bc4;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            flex-shrink: 0;
        }
        .message.mine .message-avatar {
            background: #2bb257;
        }
        .message-content {
            max-width: 70%;
            margin: 0 10px;
        }
        .message-bubble {
            padding: 10px 15px;
            border-radius: 10px;
            background: white;
            word-wrap: break-word;
        }
        .message.mine .message-bubble {
            background: #206bc4;
            color: white;
        }
        .message-sender {
            font-size: 12px;
            color: #666;
            margin-bottom: 3px;
        }
        .message.mine .message-sender {
            text-align: right;
            color: #2bb257;
        }
        .message-time {
            font-size: 11px;
            color: #999;
            margin-top: 3px;
        }
        .message.mine .message-time {
            text-align: right;
        }
        .chat-input {
            padding: 15px;
            border-top: 1px solid #e0e0e0;
            background: white;
        }
        .chat-input form {
            display: flex;
            gap: 10px;
        }
        .chat-input input {
            flex: 1;
        }
        .member-list {
            max-height: 300px;
            overflow-y: auto;
        }
        .member-item {
            display: flex;
            align-items: center;
            padding: 10px;
            border-bottom: 1px solid #f0f0f0;
        }
        .member-item:last-child {
            border-bottom: none;
        }
        .member-item .avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: #206bc4;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            margin-right: 10px;
        }
        .member-item .name {
            flex: 1;
        }
        .member-item .badge-owner {
            background: #ffc107;
            color: #000;
        }
    </style>
</head>
<body>
    <c:if test="${not empty param.success}">
        <div class="alert alert-success alert-dismissible mx-3 mt-3" role="alert">
            ${param.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <div class="container-fluid py-3">
        <div class="row">
            <div class="col-md-9">
                <div class="card chat-container">
                    <div class="chat-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h4 class="mb-1">${group.groupName}</h4>
                                <small class="text-muted">
                                    ${members.size()} 名成员
                                    <c:if test="${isOwner}"> · 您是群主</c:if>
                                </small>
                            </div>
                            <div>
                                <button class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#membersModal">
                                    <i class="bi bi-people"></i> 成员
                                </button>
                                <a href="${pageContext.request.contextPath}/group/my-groups" class="btn btn-outline-secondary btn-sm">
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
                                                 alt="${msg.senderName}" class="avatar rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">
                                        </c:when>
                                        <c:otherwise>
                                            ${not empty msg.senderName ? msg.senderName.charAt(0) : 'U'}
                                        </c:otherwise>
                                    </c:choose>
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
                            <div class="text-center text-muted py-5">
                                <i class="bi bi-chat-dots" style="font-size: 48px;"></i>
                                <p class="mt-2">暂无消息，开始聊天吧！</p>
                            </div>
                        </c:if>
                    </div>
                    
                    <div class="chat-input">
                        <form action="${pageContext.request.contextPath}/group/send" method="post" id="messageForm">
                            <input type="hidden" name="groupId" value="${group.id}">
                            <input type="text" class="form-control" name="content" placeholder="输入消息..." 
                                   autocomplete="off" required>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-send"></i> 发送
                            </button>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-3">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title mb-0">群信息</h5>
                    </div>
                    <div class="card-body">
                        <p><strong>群名称：</strong>${group.groupName}</p>
                        <p><strong>群主：</strong>${group.ownerName}</p>
                        <c:if test="${not empty group.activityName}">
                            <p><strong>所属活动：</strong>${group.activityName}</p>
                        </c:if>
                        <p><strong>成员数：</strong>${members.size()}</p>
                        <p><strong>创建时间：</strong>
                            <fmt:formatDate value="${group.createdAt}" pattern="yyyy-MM-dd" />
                        </p>
                    </div>
                    <c:if test="${isOwner}">
                        <div class="card-footer">
                            <a href="${pageContext.request.contextPath}/group/add-members?groupId=${group.id}" class="btn btn-outline-primary btn-sm w-100 mb-2">
                                <i class="bi bi-person-plus"></i> 邀请成员
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- 成员列表 Modal -->
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
                            <div class="avatar">
                                <c:choose>
                                    <c:when test="${not empty member.avatarFileId}">
                                        <img src="${pageContext.request.contextPath}/file?action=view&id=${member.avatarFileId}" 
                                             alt="${member.name}" class="rounded-circle" style="width: 36px; height: 36px; object-fit: cover;">
                                    </c:when>
                                    <c:otherwise>
                                        ${not empty member.name ? member.name.charAt(0) : (not empty member.username ? member.username.charAt(0) : 'U')}
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="name">
                                ${not empty member.name ? member.name : member.username}
                                <c:if test="${member.role == 'OWNER'}">
                                    <span class="badge badge-owner ms-1">群主</span>
                                </c:if>
                            </div>
                            <c:if test="${isOwner && member.role != 'OWNER'}">
                                <form action="${pageContext.request.contextPath}/group/member/remove" method="post">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <input type="hidden" name="userId" value="${member.userId}">
                                    <button type="submit" class="btn btn-outline-danger btn-sm"
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

    <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            var messagesDiv = document.getElementById('chatMessages');
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        });
    </script>
</body>
</html>
