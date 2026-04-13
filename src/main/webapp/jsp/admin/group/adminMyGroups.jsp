<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="我的群聊" />
</jsp:include>

<style>
    .page-title {
        font-family: var(--font-display);
        font-size: 1.94rem;
        font-weight: 600;
        color: var(--text-dark);
    }
    
    .btn-manage {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        padding: 10px 20px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    
    .btn-manage:hover {
        background: var(--brand-blue);
        color: white;
    }
    
    .groups-card {
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
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    
    .btn-chat:hover {
        background: var(--primary-600);
        color: white;
    }
    
    .btn-add {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
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
    
    .btn-add:hover {
        background: var(--brand-blue);
        color: white;
    }
    
    .btn-delete-group {
        background: rgba(239, 68, 68, 0.1);
        color: #dc2626;
        border-radius: var(--radius-standard);
        padding: 8px 16px;
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-delete-group:hover {
        background: #dc2626;
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
                <h2 class="page-title">我的群聊</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=list" class="btn-manage">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        群聊管理
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
        
        <c:if test="${not empty param.error}">
            <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-standard); padding: 16px; margin-bottom: 24px;">
                ${param.error}
            </div>
        </c:if>
         
        <div class="card groups-card">
            <div class="table-responsive">
                <table class="table-design">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>群聊名称</th>
                            <th>所属活动</th>
                            <th>成员数</th>
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
                                <td>${group.memberCount}</td>
                                <td style="color: var(--text-secondary); font-size: 0.88rem;">${group.createdAt}</td>
                                <td>
                                    <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: center;">
                                        <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn-chat">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 15a2 2 0 0 1 -2 2H7l-4 4V5a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10z"></path></svg>
                                            进入群聊
                                        </a>
                                        <a href="${pageContext.request.contextPath}/group/admin?action=addMembers&groupId=${group.id}" class="btn-add">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0 -4 -4H5a4 4 0 0 0 -4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                            添加成员
                                        </a>
                                        <form action="${pageContext.request.contextPath}/group/admin" method="POST" style="display: inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="groupId" value="${group.id}">
                                            <input type="hidden" name="from" value="my">
                                            <button type="submit" class="btn-delete-group" onclick="return confirm('确定要删除该群聊吗？此操作不可恢复！')">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                                                删除
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty groups}">
                            <tr>
                                <td colspan="6" class="empty-state">您还没有创建任何群聊</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />