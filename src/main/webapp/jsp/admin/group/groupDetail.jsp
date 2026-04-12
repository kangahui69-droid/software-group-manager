<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊详情" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    群聊详情
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=list" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>返回列表
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <div class="row g-3">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">群聊信息</h3>
                    </div>
                    <div class="card-body">
                        <dl class="mb-0">
                            <dt>群聊名称</dt>
                            <dd>${group.groupName}</dd>
                            <dt>所属活动</dt>
                            <dd>${group.activityName}</dd>
                            <dt>群主</dt>
                            <dd>${group.ownerName}</dd>
                            <dt>成员数</dt>
                            <dd>${group.memberCount}</dd>
                            <dt>创建时间</dt>
                            <dd>${group.createdAt}</dd>
                            <dt>状态</dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${group.isMuted == 1}">
                                        <span class="badge bg-danger">已禁言</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-success">正常</span>
                                    </c:otherwise>
                                </c:choose>
                            </dd>
                            <c:if test="${group.isMuted == 1}">
                                <dt>禁言原因</dt>
                                <dd>${group.muteReason}</dd>
                                <dt>禁言截止</dt>
                                <dd>${group.mutedUntil}</dd>
                            </c:if>
                        </dl>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">群聊操作</h3>
                    </div>
                    <div class="card-body">
                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" 
                           class="btn btn-primary w-100 mb-2">
                            <i class="bi bi-chat-dots me-2"></i>进入群聊
                        </a>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">禁言管理</h3>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${group.isMuted == 1}">
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="unmute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <button type="submit" class="btn btn-success w-100" 
                                            onclick="return confirm('确定要解除禁言吗？')">
                                        <i class="bi bi-chat-square-text me-2"></i>解除禁言
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                                    <input type="hidden" name="action" value="mute">
                                    <input type="hidden" name="groupId" value="${group.id}">
                                    <div class="mb-3">
                                        <label class="form-label">禁言时长</label>
                                        <select class="form-select" name="duration" required>
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
                                        <label class="form-label">禁言原因</label>
                                        <input type="text" class="form-control" name="reason" 
                                               placeholder="请输入禁言原因" required>
                                    </div>
                                    <button type="submit" class="btn btn-danger w-100">
                                        <i class="bi bi-chat-square-dash me-2"></i>禁言该群
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">最近消息（${totalMessages}条）</h3>
                        <div class="card-actions">
                            <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}" 
                               class="btn btn-outline-primary btn-sm">查看全部</a>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table table-striped">
                                <thead>
                                    <tr>
                                        <th>发送者</th>
                                        <th>内容</th>
                                        <th>时间</th>
                                        <th class="w-1">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="msg" items="${recentMessages}">
                                        <tr>
                                            <td>${msg.senderName}</td>
                                            <td class="text-truncate" style="max-width: 300px;">${msg.content}</td>
                                            <td>${msg.sentAt}</td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/group/admin" method="POST" 
                                                      onsubmit="return confirm('确定要删除该消息吗？')">
                                                    <input type="hidden" name="action" value="deleteMessage">
                                                    <input type="hidden" name="messageId" value="${msg.id}">
                                                    <input type="hidden" name="groupId" value="${group.id}">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm">删除</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty recentMessages}">
                                        <tr>
                                            <td colspan="4" class="text-center text-muted">暂无消息记录</td>
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
</div>

<jsp:include page="../../common/layout_bottom.jsp" />