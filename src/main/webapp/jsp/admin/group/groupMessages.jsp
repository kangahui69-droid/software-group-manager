<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊消息" />
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
                        <i class="bi bi-chat-dots me-2"></i>群聊消息 - ${group.groupName}
                    </h1>
                    <p class="admin-hero-subtitle">查看群聊消息记录</p>
                </div>
                <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <c:if test="${not empty success}">
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); color: #059669; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${success}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); color: #dc2626; border-radius: var(--radius-generous); padding: 16px; margin-bottom: 24px;">
                ${error}
            </div>
        </c:if>
        
        <div class="card-design">
            <div class="card-header-design" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));">
                <h3 class="card-title-design" style="color: white;">消息记录（共${totalMessages}条）</h3>
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
                                        <button type="submit" class="btn-outline-danger btn-sm">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty messages}">
                            <tr>
                                <td colspan="5" class="text-center text-muted py-4">暂无消息记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <c:if test="${totalPages > 1}">
                <div style="padding: 20px 24px; border-top: 1px solid var(--border-light); display: flex; justify-content: center;">
                    <nav>
                        <ul style="display: flex; gap: 8px; list-style: none; padding: 0; margin: 0;">
                            <c:if test="${currentPage > 1}">
                                <li style="border-radius: var(--radius-generous); overflow: hidden;">
                                    <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage - 1}" style="display: block; padding: 8px 14px; font-size: 0.88rem; font-weight: 500; color: var(--text-dark); text-decoration: none; transition: all 0.2s ease;">上一页</a>
                                </li>
                            </c:if>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:if test="${i == currentPage}">
                                    <li style="border-radius: var(--radius-generous); overflow: hidden;">
                                        <span style="display: block; padding: 8px 14px; font-size: 0.88rem; font-weight: 500; background: var(--brand-blue); color: white;">${i}</span>
                                    </li>
                                </c:if>
                                <c:if test="${i != currentPage}">
                                    <c:if test="${i <= 3 || i >= totalPages - 2 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                        <li style="border-radius: var(--radius-generous); overflow: hidden;">
                                            <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${i}" style="display: block; padding: 8px 14px; font-size: 0.88rem; font-weight: 500; color: var(--text-dark); text-decoration: none; transition: all 0.2s ease;">${i}</a>
                                        </li>
                                    </c:if>
                                </c:if>
                            </c:forEach>
                            
                            <c:if test="${currentPage < totalPages}">
                                <li style="border-radius: var(--radius-generous); overflow: hidden;">
                                    <a href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage + 1}" style="display: block; padding: 8px 14px; font-size: 0.88rem; font-weight: 500; color: var(--text-dark); text-decoration: none; transition: all 0.2s ease;">下一页</a>
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