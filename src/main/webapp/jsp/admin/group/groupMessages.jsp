<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="群聊消息" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    群聊消息 - ${group.groupName}
                </h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
                <div class="btn-list">
                    <a href="${pageContext.request.contextPath}/group/admin?action=detail&id=${group.id}" 
                       class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>返回详情
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
        
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">消息记录（共${totalMessages}条）</h3>
            </div>
            <div class="table-responsive">
                <table class="table table-vcenter card-table table-striped">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>发送者</th>
                            <th>内容</th>
                            <th>发送时间</th>
                            <th class="w-1">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="msg" items="${messages}">
                            <tr>
                                <td>${msg.id}</td>
                                <td>${msg.senderName}</td>
                                <td class="text-truncate" style="max-width: 400px;">${msg.content}</td>
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
                        <c:if test="${empty messages}">
                            <tr>
                                <td colspan="5" class="text-center text-muted">暂无消息记录</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <c:if test="${totalPages > 1}">
                <div class="card-footer d-flex justify-content-center">
                    <nav>
                        <ul class="pagination mb-0">
                            <c:if test="${currentPage > 1}">
                                <li class="page-item">
                                    <a class="page-link" href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage - 1}">
                                        上一页
                                    </a>
                                </li>
                            </c:if>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:if test="${i == currentPage}">
                                    <li class="page-item active">
                                        <span class="page-link">${i}</span>
                                    </li>
                                </c:if>
                                <c:if test="${i != currentPage}">
                                    <c:if test="${i <= 3 || i >= totalPages - 2 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                        <li class="page-item">
                                            <a class="page-link" href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${i}">
                                                ${i}
                                            </a>
                                        </li>
                                    </c:if>
                                </c:if>
                            </c:forEach>
                            
                            <c:if test="${currentPage < totalPages}">
                                <li class="page-item">
                                    <a class="page-link" href="${pageContext.request.contextPath}/group/admin?action=messages&id=${group.id}&page=${currentPage + 1}">
                                        下一页
                                    </a>
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