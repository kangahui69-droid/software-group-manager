<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊管理" />
</jsp:include>

<style>
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .group-table-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }
    
    .table-design {
        width: 100%;
        border-collapse: collapse;
    }
    
    .table-design thead {
        background: var(--bg-light-gray);
    }
    
    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        padding: 16px 20px;
        text-align: left;
        font-size: 0.81rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        border-bottom: 2px solid var(--border-gray);
    }
    
    .table-design td {
        padding: 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
    }
    
    .table-design tbody tr {
        transition: background 0.2s ease;
    }
    
    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }
    
    .group-name {
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .badge-status {
        display: inline-flex;
        align-items: center;
        padding: 6px 12px;
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
    
    .btn-detail {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-detail:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }
    
    .btn-chat {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-chat:hover {
        background: var(--primary-600);
        color: white;
    }
    
    .btn-delete {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-delete:hover {
        background: #dc2626;
        color: white;
    }
    
    .alert-success {
        background: rgba(16, 185, 129, 0.1);
        border: 1px solid rgba(16, 185, 129, 0.2);
        color: #059669;
        border-radius: var(--radius-standard);
        padding: 16px 20px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    
    .alert-error {
        background: rgba(239, 68, 68, 0.1);
        border: 1px solid rgba(239, 68, 68, 0.2);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 16px 20px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        justify-content: space-between;
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
                <h2 class="page-title">群聊管理</h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <c:if test="${not empty success}">
            <div class="alert-success" role="alert">
                <span>${success}</span>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert-error" role="alert">
                <span>${error}</span>
            </div>
        </c:if>
        
        <div class="card group-table-card">
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>群聊名称</th>
                            <th>所属活动</th>
                            <th>群主</th>
                            <th>成员数</th>
                            <th>状态</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="group" items="${groups}">
                            <tr>
                                <td>${group.id}</td>
                                <td class="group-name">${group.groupName}</td>
                                <td>${group.activityName}</td>
                                <td>${group.ownerName}</td>
                                <td>${group.memberCount}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${group.isMuted == 1}">
                                            <span class="badge-status badge-danger">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
                                                已禁言
                                            </span>
                                            <c:if test="${group.mutedUntil != null}">
                                                <small class="d-block" style="color: var(--text-muted); font-size: 0.75rem; margin-top: 4px;">至 ${group.mutedUntil}</small>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-status badge-success">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                                正常
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="color: var(--text-secondary); font-size: 0.88rem;">${group.createdAt}</td>
                                <td>
                                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" class="btn-detail">详情</a>
                                        <a href="${pageContext.request.contextPath}/group/chat/${group.id}" class="btn-chat">进入群聊</a>
                                        <button type="button" class="btn-delete" onclick="confirmDelete(${group.id}, '${group.groupName}')">删除</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty groups}">
                            <tr>
                                <td colspan="8" class="empty-state">暂无群聊记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function confirmDelete(groupId, groupName) {
    if (confirm('确定要删除群聊"' + groupName + '"吗？此操作不可恢复！')) {
        window.location.href = '${pageContext.request.contextPath}/group/admin?action=delete&groupId=' + groupId;
    }
}
</script>

<jsp:include page="../../common/layout_bottom.jsp" />