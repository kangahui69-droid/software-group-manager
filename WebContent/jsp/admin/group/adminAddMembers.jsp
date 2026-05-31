<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="添加群聊成员" />
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
        margin-bottom: 24px;
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
    
    .form-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 24px;
    }
    
    .form-item {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    
    .form-label {
        font-weight: 500;
        color: var(--text-dark);
        font-size: 0.88rem;
    }
    
    .form-control-plaintext {
        font-size: 0.94rem;
        color: var(--text-dark);
        padding: 0;
    }
    
    .participants-card {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
        margin-bottom: 24px;
    }
    
    .participants-card-header {
        background: var(--bg-light-gray);
        padding: 16px 24px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .participants-card-title {
        font-family: var(--font-display);
        font-size: 1rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }
    
    .participants-card-body {
        padding: 24px;
    }
    
    .participant-checkbox-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background: var(--bg-light-gray);
        border-radius: var(--radius-standard);
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.2s ease;
        border: 2px solid transparent;
    }
    
    .participant-checkbox-item:hover {
        background: rgba(20, 86, 240, 0.05);
        border-color: rgba(20, 86, 240, 0.2);
    }
    
    .participant-checkbox-item input[type="checkbox"] {
        width: 18px;
        height: 18px;
        margin-right: 12px;
        accent-color: var(--brand-blue);
    }
    
    .participant-checkbox-item label {
        flex: 1;
        cursor: pointer;
        font-size: 0.88rem;
        color: var(--text-dark);
        margin: 0;
    }
    
    .participant-name {
        font-weight: 500;
    }
    
    .participant-info {
        color: var(--text-muted);
        font-size: 0.81rem;
    }
    
    .participant-pending {
        color: #d97706;
        font-size: 0.75rem;
        margin-left: 8px;
    }
    
    .btn-submit {
        background: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 28px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
    }
    
    .btn-submit:hover {
        background: var(--primary-600);
        color: white;
    }
    
    .btn-cancel {
        background: var(--bg-light-gray);
        color: var(--text-dark);
        border-radius: var(--radius-standard);
        padding: 12px 24px;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
    }
    
    .btn-cancel:hover {
        background: var(--border-gray);
        color: var(--text-dark);
    }
    
    .existing-members-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        overflow: hidden;
    }
    
    .existing-members-header {
        background: var(--bg-light-gray);
        padding: 14px 20px;
        border-bottom: 1px solid var(--border-light);
    }
    
    .existing-members-title {
        font-family: var(--font-display);
        font-size: 0.94rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
    }
    
    .existing-members-body {
        padding: 16px 20px;
    }
    
    .existing-members-list {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    
    .existing-members-list li {
        padding: 8px 0;
        color: var(--text-secondary);
        font-size: 0.88rem;
        border-bottom: 1px solid var(--border-light);
    }
    
    .existing-members-list li:last-child {
        border-bottom: none;
    }
    
    .empty-state {
        text-align: center;
        padding: 40px 24px;
        color: var(--text-muted);
        font-size: 0.88rem;
    }
</style>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">添加群聊成员 - ${group.groupName}</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn-back">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回群聊
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="card info-card">
            <div class="card-header info-card-header">
                <h3 class="info-card-title">群信息</h3>
            </div>
            <div class="card-body info-card-body">
                <div class="form-grid">
                    <div class="form-item">
                        <label class="form-label">群聊名称</label>
                        <p class="form-control-plaintext">${group.groupName}</p>
                    </div>
                    <div class="form-item">
                        <label class="form-label">所属活动</label>
                        <p class="form-control-plaintext">${group.activityName}</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card participants-card">
            <div class="card-header participants-card-header">
                <h3 class="participants-card-title">
                    <c:choose>
                        <c:when test="${not empty participants}">
                            已报名的参与者 (${not empty participants ? participants.size() : 0})
                        </c:when>
                        <c:otherwise>
                            暂无参与者可添加
                        </c:otherwise>
                    </c:choose>
                </h3>
            </div>
            <div class="card-body participants-card-body">
                <c:choose>
                    <c:when test="${empty participants}">
                        <div class="empty-state">
                            <p>该群聊没有关联的活动，或活动暂无报名参与者。</p>
                            <p>请通过其他方式邀请成员。</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                            <input type="hidden" name="action" value="addMultipleMembers">
                            <input type="hidden" name="groupId" value="${group.id}">
                            
                            <div class="mb-3">
                                <label class="form-label" style="margin-bottom: 16px;">选择要加入群聊的参与者（默认全部选中）</label>
                                <div>
                                    <c:forEach var="p" items="${participants}">
                                        <c:if test="${!existingMemberIds.contains(p.userId)}">
                                            <div class="participant-checkbox-item">
                                                <input type="checkbox" name="selectedUsers" value="${p.userId}" id="user_${p.userId}" checked>
                                                <label for="user_${p.userId}">
                                                    <span class="participant-name">${not empty p.userName ? p.userName : '用户'}${p.userId}</span>
                                                    <c:if test="${not empty p.studentId}">
                                                        <span class="participant-info"> (${p.studentId})</span>
                                                    </c:if>
                                                    <c:if test="${p.status == 'pending'}">
                                                        <span class="participant-pending">[待审核]</span>
                                                    </c:if>
                                                </label>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            
                            <div style="display: flex; gap: 12px; margin-top: 24px;">
                                <button type="submit" class="btn-submit">添加选中成员</button>
                                <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn-cancel">取消</a>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <c:if test="${not empty existingMemberIds}">
        <div class="card existing-members-card">
            <div class="existing-members-header">
                <h3 class="existing-members-title">已在群中的成员</h3>
            </div>
            <div class="existing-members-body">
                <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 12px;">以下成员已在群中：</p>
                <ul class="existing-members-list">
                    <c:forEach var="member" items="${participants}">
                        <c:if test="${existingMemberIds.contains(member.userId)}">
                            <li>${not empty member.userName ? member.userName : '用户'}${member.userId}</li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        </c:if>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />