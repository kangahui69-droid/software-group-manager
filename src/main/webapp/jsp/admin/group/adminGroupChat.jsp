<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="${group.groupName} - 群聊" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-body-design {
        padding: 0;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-chat-dots me-2"></i>${group.groupName}
                    </h1>
                    <p class="admin-hero-subtitle">群聊管理</p>
                </div>
                <a href="${pageContext.request.contextPath}/group/admin" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <c:if test="${not empty param.success}">
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${param.success}
            </div>
        </c:if>
        
        <div class="row">
            <div class="col-md-9">
                <div class="chat-container" style="height: calc(100vh - 300px); min-height: 450px; display: flex; flex-direction: column; background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); overflow: hidden;">
                    <div class="chat-header" style="padding: 20px 24px; background: linear-gradient(135deg, var(--brand-blue), var(--primary-light)); color: white;">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h4 style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; margin: 0 0 4px 0;">${group.groupName}</h4>
                                <p style="font-size: 0.81rem; opacity: 0.9; margin: 0;">${members.size()} 名成员 · 您是群主</p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button class="btn-members" data-bs-toggle="modal" data-bs-target="#membersModal" style="background: rgba(255, 255, 255, 0.2); color: white; border-radius: var(--radius-generous); padding: 8px 16px; font-size: 0.81rem; font-weight: 500; border: none; transition: all 0.3s ease; display: inline-flex; align-items: center; gap: 6px;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-2a4 4 0 0 0 -3 -3.85"></path></svg>
                                    成员
                                </button>
                                <a href="${pageContext.request.contextPath}/group/admin?action=addMembers&groupId=${group.id}" class="btn-members" style="text-decoration: none; background: rgba(255, 255, 255, 0.2); color: white; border-radius: var(--radius-generous); padding: 8px 16px; font-size: 0.81rem; font-weight: 500; border: none; transition: all 0.3s ease; display: inline-flex; align-items: center; gap: 6px;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                    邀请
                                </a>
                            </div>
                        </div>
                    </div>
                    
                    <div class="chat-messages" id="chatMessages" style="flex: 1; overflow-y: auto; padding: 24px; background: #f8fafc;">
                        <c:forEach var="msg" items="${messages}">
                            <div class="message ${msg.senderId == sessionScope.user.id ? 'mine' : ''}" style="margin-bottom: 20px; display: flex; animation: fadeIn 0.3s ease;">
                                <div class="message-avatar" style="width: 42px; height: 42px; border-radius: var(--radius-comfortable); background: linear-gradient(135deg, var(--brand-blue), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 1rem; flex-shrink: 0;">
                                    ${not empty msg.senderName ? msg.senderName.charAt(0) : 'U'}
                                </div>
                                <div class="message-content" style="max-width: 65%; margin: 0 12px;">
                                    <div class="message-sender" style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 4px; font-weight: 500;">${msg.senderName}</div>
                                    <div class="message-bubble" style="padding: 14px 18px; border-radius: 18px; background: white; word-wrap: break-word; box-shadow: var(--shadow-standard); line-height: 1.5;">${msg.content}</div>
                                    <div class="message-time" style="font-size: 0.69rem; color: var(--text-muted); margin-top: 4px;">
                                        <fmt:formatDate value="${msg.sentAt}" pattern="HH:mm:ss" />
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        
                        <c:if test="${empty messages}">
                            <div class="empty-chat" style="text-align: center; padding: 60px 24px; color: var(--text-muted);">
                                <div style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                                </div>
                                <p>暂无消息，开始聊天吧！</p>
                            </div>
                        </c:if>
                    </div>
                    
                    <div class="chat-input" style="padding: 20px 24px; border-top: 1px solid var(--border-light); background: white;">
                        <form action="${pageContext.request.contextPath}/group/admin" method="post">
                            <input type="hidden" name="action" value="send">
                            <input type="hidden" name="groupId" value="${group.id}">
                            <div style="display: flex; gap: 12px;">
                                <input type="text" class="chat-input-field" name="content" placeholder="输入消息..." autocomplete="off" required style="flex: 1; border-radius: var(--radius-generous); border: 1px solid var(--border-gray); padding: 12px 18px; font-size: 0.94rem; transition: all 0.3s ease;">
                                <button type="submit" class="btn-send" style="background: var(--brand-blue); color: white; border-radius: var(--radius-generous); padding: 12px 24px; font-weight: 600; border: none; transition: all 0.3s ease; display: flex; align-items: center; gap: 8px;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
                                    发送
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-3">
                <div class="group-info-card" style="background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); overflow: hidden; height: 100%;">
                    <div class="group-info-header" style="background: var(--bg-light-gray); padding: 16px 20px; border-bottom: 1px solid var(--border-light);">
                        <h5 class="group-info-title" style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark); margin: 0;">群信息</h5>
                    </div>
                    <div class="group-info-body" style="padding: 20px;">
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">群名称</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;">${group.groupName}</span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">所属活动</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;">${group.activityName}</span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">群主</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;">${group.ownerName}</span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">成员数</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500;">${members.size()}</span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">创建时间</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;"><fmt:formatDate value="${group.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">状态</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500;"><span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">已禁言</span></span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">禁言原因</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;">${group.muteReason}</span>
                        </div>
                        <div class="group-info-item" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 0; font-size: 0.88rem;">
                            <span class="group-info-label" style="color: var(--text-muted); flex-shrink: 0;">禁言截止</span>
                            <span class="group-info-value" style="color: var(--text-dark); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;"><fmt:formatDate value="${group.muteEndTime}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
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
            <div class="modal-header" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light)); color: white; border: none;">
                <h5 class="modal-title">群成员 (${members.size()})</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" style="filter: brightness(0) invert(1);"></button>
            </div>
            <div class="modal-body member-list" style="max-height: 400px; overflow-y: auto;">
                <c:forEach var="member" items="${members}">
                    <div class="member-item" style="display: flex; align-items: center; padding: 14px 20px; border-bottom: 1px solid var(--border-light); transition: background 0.2s ease;">
                        <div class="member-avatar" style="width: 38px; height: 38px; border-radius: var(--radius-generous); background: linear-gradient(135deg, var(--brand-blue), var(--primary-light)); color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 0.88rem; margin-right: 12px;">
                            ${not empty member.name ? member.name.charAt(0) : (not empty member.username ? member.username.charAt(0) : 'U')}
                        </div>
                        <div class="member-name" style="flex: 1; font-weight: 500; color: var(--text-dark); font-size: 0.88rem;">
                            ${not empty member.name ? member.name : member.username}
                            <c:if test="${member.role == 'OWNER'}">
                                <span class="badge-owner" style="background: rgba(245, 158, 11, 0.15); color: #d97706; padding: 3px 8px; border-radius: var(--radius-pill); font-size: 0.69rem; font-weight: 600; margin-left: 8px;">群主</span>
                            </c:if>
                        </div>
                        <c:if test="${isOwner && member.role != 'OWNER'}">
                            <form action="${pageContext.request.contextPath}/group/admin" method="post">
                                <input type="hidden" name="action" value="removeMember">
                                <input type="hidden" name="groupId" value="${group.id}">
                                <input type="hidden" name="userId" value="${member.userId}">
                                <button type="submit" class="btn-remove" onclick="return confirm('确定要移除该成员吗？')" style="background: rgba(239, 68, 68, 0.1); color: #dc2626; border-radius: var(--radius-generous); padding: 6px 10px; font-size: 0.75rem; font-weight: 500; border: none; transition: all 0.3s ease;">
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