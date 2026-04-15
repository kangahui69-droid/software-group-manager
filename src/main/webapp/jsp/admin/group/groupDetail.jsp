<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊详情" />
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
                        <i class="bi bi-chat-dots me-2"></i>群聊详情
                    </h1>
                    <p class="admin-hero-subtitle">查看群聊详细信息</p>
                </div>
                <a href="${pageContext.request.contextPath}/group/admin" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${success}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${error}
            </div>
        </c:if>
        
        <div class="row g-3">
            <div class="col-md-4">
                <div class="card-design">
                    <div class="card-header-design" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));">
                        <h3 class="card-title-design" style="color: white;">群聊信息</h3>
                    </div>
                    <div class="card-body-design">
                        <ul style="list-style: none; padding: 0; margin: 0;">
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">群聊名称</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.groupName}</span>
                            </li>
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">所属活动</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.activityName}</span>
                            </li>
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">群主</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.ownerName}</span>
                            </li>
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">成员数</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.memberCount}</span>
                            </li>
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">创建时间</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.createdAt}</span>
                            </li>
                            <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <span style="color: var(--text-muted); font-size: 0.88rem;">状态</span>
                                <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">
                                    <c:choose>
                                        <c:when test="${group.isMuted == 1}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">已禁言</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">正常</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </li>
                            <c:if test="${group.isMuted == 1}">
                                <li style="display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                    <span style="color: var(--text-muted); font-size: 0.88rem;">禁言原因</span>
                                    <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.muteReason}</span>
                                </li>
                                <li style="display: flex; justify-content: space-between; padding: 12px 0;">
                                    <span style="color: var(--text-muted); font-size: 0.88rem;">禁言截止</span>
                                    <span style="color: var(--text-dark); font-weight: 500; font-size: 0.88rem;">${group.mutedUntil}</span>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </div>
                
                <div class="card-design" style="margin-top: 16px;">
                    <div class="card-body-design" style="padding: 20px;">
                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" class="btn-brand" style="width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                            进入群聊
                        </a>
                    </div>
                </div>
                
                <div class="card-design" style="margin-top: 16px;">
                    <div class="card-header-design" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));">
                        <h3 class="card-title-design" style="color: white;">禁言管理</h3>
                    </div>
                    <div class="card-body-design" style="padding: 20px;">
                        <c:choose>
                            <c:when test="${group.isMuted == 1}">
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="unmute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <button type="submit" class="btn-success-design" style="width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;" onclick="return confirm('确定要解除禁言吗？')">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                                        解除禁言
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="mute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <div class="mb-3">
                                        <label class="form-label-design" style="font-weight: 500; font-size: 0.88rem;">禁言时长</label>
                                        <select class="input-design" name="duration" required style="width: 100%;">
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
                                        <label class="form-label-design" style="font-weight: 500; font-size: 0.88rem;">禁言原因</label>
                                        <input type="text" class="input-design" name="reason" placeholder="请输入禁言原因" required>
                                    </div>
                                    <button type="submit" class="btn-danger-design" style="width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
                                        禁言该群
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <div class="col-md-8">
                <div class="card-design">
                    <div class="card-header-design" style="background: var(--bg-white); border-bottom: 1px solid var(--border-light);">
                        <h3 class="card-title-design">最近消息（${totalMessages}条）</h3>
                        <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}" class="btn-sm-brand">查看全部</a>
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
                                                <button type="submit" class="btn-outline-danger btn-sm" onclick="return confirm('确定要删除该消息吗？')">删除</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty recentMessages}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">暂无消息记录</td>
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