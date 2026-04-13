<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊消息" />
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
    
    .messages-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .messages-card-header {
        background: linear-gradient(135deg, var(--brand-blue) 0%, var(--primary-500) 100%);
        padding: 20px 24px;
    }
    
    .messages-card-title {
        font-family: var(--font-display);
        font-size: 1.125rem;
        font-weight: 600;
        color: white;
        margin: 0;
    }
    
    .table-design {
        width: 100%;
        border-collapse: collapse;
    }
    
    .table-design thead {
        background: var(--bg-light-gray);
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
    
    .pagination-container {
        padding: 20px 24px;
        border-top: 1px solid var(--border-light);
        display: flex;
        justify-content: center;
    }
    
    .pagination {
        display: flex;
        gap: 8px;
        list-style: none;
        padding: 0;
        margin: 0;
    }
    
    .pagination-item {
        border-radius: var(--radius-standard);
        overflow: hidden;
    }
    
    .pagination-item a,
    .pagination-item span {
        display: block;
        padding: 8px 14px;
        font-size: 0.88rem;
        font-weight: 500;
        color: var(--text-dark);
        text-decoration: none;
        transition: all 0.2s ease;
    }
    
    .pagination-item a:hover {
        background: var(--brand-blue);
        color: white;
    }
    
    .pagination-item.active span {
        background: var(--brand-blue);
        color: white;
    }
    
    .empty-state {
        text-align: center;
        padding: 60px 24px;
        color: var(--text-muted);
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">群聊消息 - ${group.groupName}</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" class="btn-back">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                        返回详情
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty success}">
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${success}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${error}
            </div>
        </c:if>
        
        <div class="card messages-card">
            <div class="card-header messages-card-header">
                <h3 class="messages-card-title">消息记录（共${totalMessages}条）</h3>
            </div>
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>发送者</th>
                            <th>内容</th>
                            <th>发送时间</th>
                            <th style="width: 80px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="msg" items="${messages}">
                            <tr>
                                <td>${msg.id}</td>
                                <td style="font-weight: 500;">${msg.senderName}</td>
                                <td style="max-width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${msg.content}</td>
                                <td style="color: var(--text-muted);">${msg.sentAt}</td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/group/admin" method="POST" style="display: inline;" onsubmit="return confirm('确定要删除该消息吗？')">
                                        <input type="hidden" name="action" value="deleteMessage">
                                        <input type="hidden" name="messageId" value="${msg.id}">
                                        <input type="hidden" name="groupId" value="${group.id}">
                                        <button type="submit" class="btn-delete-msg">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty messages}">
                            <tr>
                                <td colspan="5" class="empty-state">暂无消息记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <c:if test="${totalPages > 1}">
                <div class="pagination-container">
                    <nav>
                        <ul class="pagination">
                            <c:if test="${currentPage > 1}">
                                <li class="pagination-item">
                                    <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage - 1}">上一页</a>
                                </li>
                            </c:if>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:if test="${i == currentPage}">
                                    <li class="pagination-item active">
                                        <span>${i}</span>
                                    </li>
                                </c:if>
                                <c:if test="${i != currentPage}">
                                    <c:if test="${i <= 3 || i >= totalPages - 2 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                        <li class="pagination-item">
                                            <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${i}">${i}</a>
                                        </li>
                                    </c:if>
                                </c:if>
                            </c:forEach>
                            
                            <c:if test="${currentPage < totalPages}">
                                <li class="pagination-item">
                                    <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage + 1}">下一页</a>
                                </li>
                            </c:if>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />