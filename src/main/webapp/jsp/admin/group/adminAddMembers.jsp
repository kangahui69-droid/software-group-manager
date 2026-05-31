<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="添加群聊成员" />
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
                        <i class="bi bi-person-plus me-2"></i>添加群聊成员 - ${group.groupName}
                    </h1>
                    <p class="admin-hero-subtitle">为群聊添加新成员</p>
                </div>
                <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${groupId}" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <div class="card-design">
            <div class="card-header-design" style="background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));">
                <h3 class="card-title-design" style="color: white;">群信息</h3>
            </div>
            <div class="card-body-design">
                <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px;">
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <label class="form-label-design">群聊名称</label>
                        <p style="font-size: 0.94rem; color: var(--text-dark); padding: 0;">${group.groupName}</p>
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <label class="form-label-design">所属活动</label>
                        <p style="font-size: 0.94rem; color: var(--text-dark); padding: 0;">${group.activityName}</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card-design" style="margin-top: 24px;">
            <div class="card-header-design" style="background: var(--bg-light-gray); border-bottom: 1px solid var(--border-light);">
                <h3 class="card-title-design">
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
            <div class="card-body-design">
                <c:choose>
                    <c:when test="${empty participants}">
                        <div style="text-align: center; padding: 40px 24px; color: var(--text-muted); font-size: 0.88rem;">
                            <p>该群聊没有关联的活动，或活动暂无报名参与者。</p>
                            <p>请通过其他方式邀请成员。</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/group/admin" method="POST">
                            <input type="hidden" name="action" value="addMultipleMembers">
                            <input type="hidden" name="groupId" value="${group.id}">
                            
                            <div class="mb-3">
                                <label class="form-label-design" style="margin-bottom: 16px;">选择要加入群聊的参与者（默认全部选中）</label>
                                <div>
                                    <c:forEach var="p" items="${participants}">
                                        <c:if test="${!existingMemberIds.contains(p.userId)}">
                                            <div class="participant-checkbox-item" style="display: flex; align-items: center; padding: 12px 16px; background: var(--bg-light-gray); border-radius: var(--radius-generous); margin-bottom: 12px; cursor: pointer; transition: all 0.2s ease; border: 2px solid transparent;">
                                                <input type="checkbox" name="selectedUsers" value="${p.userId}" id="user_${p.userId}" checked style="width: 18px; height: 18px; margin-right: 12px; accent-color: var(--brand-blue);">
                                                <label for="user_${p.userId}" style="flex: 1; cursor: pointer; font-size: 0.88rem; color: var(--text-dark); margin: 0;">
                                                    <span style="font-weight: 500;">${not empty p.userName ? p.userName : '用户'}${p.userId}</span>
                                                    <c:if test="${not empty p.studentId}">
                                                        <span style="color: var(--text-muted); font-size: 0.81rem;"> (${p.studentId})</span>
                                                    </c:if>
                                                    <c:if test="${p.status == 'pending'}">
                                                        <span style="color: #d97706; font-size: 0.75rem; margin-left: 8px;">[待审核]</span>
                                                    </c:if>
                                                </label>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            
                            <div style="display: flex; gap: 12px; margin-top: 24px;">
                                <button type="submit" class="btn-brand">添加选中成员</button>
                                <a href="${pageContext.request.contextPath}/group/admin?action=chat&id=${group.id}" class="btn-outline-brand">取消</a>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        
        <c:if test="${not empty existingMemberIds}">
        <div class="card-design" style="margin-top: 24px;">
            <div class="card-header-design" style="background: var(--bg-light-gray); border-bottom: 1px solid var(--border-light);">
                <h3 class="card-title-design">已在群中的成员</h3>
            </div>
            <div class="card-body-design">
                <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 12px;">以下成员已在群中：</p>
                <ul style="list-style: none; padding: 0; margin: 0;">
                    <c:forEach var="member" items="${participants}">
                        <c:if test="${existingMemberIds.contains(member.userId)}">
                            <li style="padding: 8px 0; color: var(--text-secondary); font-size: 0.88rem; border-bottom: 1px solid var(--border-light);">${not empty member.userName ? member.userName : '用户'}${member.userId}</li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        </c:if>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />