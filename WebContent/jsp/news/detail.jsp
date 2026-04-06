<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../common/layout_top.jsp">
            <jsp:param name="title" value="${news.title}" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <div class="mb-3">
                            <ol class="breadcrumb" aria-label="breadcrumbs">
                                <li class="breadcrumb-item"><a
                                        href="${pageContext.request.contextPath}/index.jsp">首页</a></li>
                                <li class="breadcrumb-item"><a
                                        href="${pageContext.request.contextPath}/news/list">新闻动态</a></li>
                                <li class="breadcrumb-item active" aria-current="page">${news.type}</li>
                            </ol>
                        </div>
                        <h2 class="page-title">${news.title}</h2>
                    </div>
                    <div class="col-auto ms-auto">
                        <a href="${pageContext.request.contextPath}/news/list" class="btn btn-outline-secondary">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                            返回列表
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="row justify-content-center">
                    <div class="col-lg-10">
                        <div class="card card-lg">
                            <div class="card-body">
                                <div class="d-flex align-items-center mb-4">
                                    <div class="text-muted"><i class="bi bi-calendar3 me-1"></i> 发布时间：${news.createdAt}
                                    </div>
                                    <div class="ms-auto">
                                        <span class="badge bg-blue-lt">${news.type}</span>
                                    </div>
                                </div>
                                <div class="markdown">
                                    <p class="lead">${news.summary}</p>
                                    <hr>
                                    <div class="news-content">
                                        ${content}
                                    </div>
                                </div>
                            </div>
                            <div class="card-footer">
                                <div class="row align-items-center">
                                    <div class="col">
                                        <a href="javascript:history.back()" class="btn btn-link">返回上一页</a>
                                    </div>
                                    <div class="col-auto">
                                        <div class="btn-list">
                                            <button class="btn btn-icon btn-white" aria-label="Share" onclick="copyLink()">
                                                <i class="bi bi-share"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../common/layout_bottom.jsp" />

        <script>
        function copyLink() {
            navigator.clipboard.writeText(window.location.href).then(function() {
                alert('链接已复制到剪贴板');
            }, function() {
                prompt('链接：', window.location.href);
            });
        }
        </script>