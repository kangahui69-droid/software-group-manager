<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <jsp:include page="../common/layout_top.jsp">
        <jsp:param name="title" value="报名成功" />
    </jsp:include>

    <div class="page-body">
        <div class="container-xl d-flex flex-column justify-content-center">
            <div class="empty">
                <div class="empty-icon">
                    <i class="bi bi-check-circle text-success h1"></i>
                </div>
                <p class="empty-title">提交成功！</p>
                <p class="empty-subtitle text-muted">
                    感谢您的报名，我们的工作人员会在近期通过手机或联系方式与您取得联系。
                </p>
                <div class="empty-action">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary">
                        <i class="bi bi-house me-2"></i>返回首页
                    </a>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../common/layout_bottom.jsp" />