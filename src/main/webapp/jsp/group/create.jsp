<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="创建群聊" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">创建群聊</h2>
                <div class="text-muted">创建一个新的群聊</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/group/my-groups" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">群聊信息</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger alert-dismissible">
                                <i class="bi bi-exclamation-triangle me-2"></i>${error}
                                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                            </div>
                        </c:if>
                        
                        <form action="${pageContext.request.contextPath}/group" method="post">
                            <div class="mb-3">
                                <label class="form-label">群名称 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="groupName" 
                                       placeholder="请输入群名称" required maxlength="100">
                                <div class="form-text">群名称最多100个字符</div>
                            </div>
                            
                            <div class="alert alert-info">
                                <p class="mb-0"><i class="bi bi-info-circle me-1"></i>如果您想创建与活动关联的群聊，请从"我发起的活动"页面点击"创建群聊"按钮。</p>
                            </div>
                            
                            <div class="d-flex justify-content-between">
                                <a href="${pageContext.request.contextPath}/group/my-groups" class="btn btn-outline-secondary">
                                    <i class="bi bi-arrow-left me-1"></i>取消
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-lg me-1"></i>创建群聊
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
