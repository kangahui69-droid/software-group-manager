<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊管理" />
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
            <h1 class="admin-hero-title">
                <i class="bi bi-chat-dots me-2"></i>群聊管理
            </h1>
            <p class="admin-hero-subtitle">管理系统群聊</p>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-success mb-4" role="alert" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-generous); padding: 16px 20px;">
                <span>${success}</span>
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger mb-4" role="alert" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-generous); padding: 16px 20px;">
                <span>${error}</span>
            </div>
        </c:if>
        
        <div class="card-design">
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
                                <td style="font-weight: 600; color: var(--text-dark);">${group.groupName}</td>
                                <td>${group.activityName}</td>
                                <td>${group.ownerName}</td>
                                <td>${group.memberCount}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${group.isMuted == 1}">
                                            <span class="badge-design" style="background: rgba(239, 68, 68, 0.1); color: #dc2626;">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
                                                已禁言
                                            </span>
                                            <c:if test="${group.mutedUntil != null}">
                                                <small class="d-block" style="color: var(--text-muted); font-size: 0.75rem; margin-top: 4px;">至 ${group.mutedUntil}</small>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #059669;">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px;"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                                正常
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="color: var(--text-secondary); font-size: 0.88rem;">${group.createdAt}</td>
                                <td>
                                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" class="btn-sm-brand">详情</a>
                                        <c:choose>
                                            <c:when test="${memberStatus[group.id]}">
                                                <a href="${pageContext.request.contextPath}/group/chat/${group.id}" class="btn-brand">进入群聊</a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/group/admin?action=joinAsAdmin&groupId=${group.id}" class="btn-success-design">加入群聊</a>
                                            </c:otherwise>
                                        </c:choose>
                                        <button type="button" class="btn-danger-design btn-sm" onclick="confirmDelete(${group.id}, '${group.groupName}')">删除</button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty groups}">
                            <tr>
                                <td colspan="8" class="text-center text-muted py-4">暂无群聊记录</td>
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