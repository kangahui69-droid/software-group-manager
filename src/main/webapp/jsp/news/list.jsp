<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../common/layout_top.jsp">
            <jsp:param name="title" value="新闻动态" />
            <jsp:param name="active" value="notice" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            新闻动态
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="row row-cards">
                    <c:forEach var="n" items="${newsList}">
                        <div class="col-md-6 col-lg-4">
                            <div class="card card-stacked">
                                <div class="card-body">
                                    <div class="subheader text-capitalize mb-2">${n.type}</div>
                                    <h3 class="card-title">${n.title}</h3>
                                    <p class="text-muted">${n.summary}</p>
                                </div>
                                <div class="card-footer">
                                    <div class="d-flex align-items-center">
                                        <div class="text-muted"><i class="bi bi-clock me-1"></i> ${n.createdAt}</div>
                                        <div class="ms-auto">
                                            <a href="news?action=detail&id=${n.id}"
                                                class="btn btn-primary btn-sm">阅读全文</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty newsList}">
                        <div class="col-12 text-center py-5">
                            <div class="empty">
                                <div class="empty-icon">
                                    <i class="bi bi-info-circle h1"></i>
                                </div>
                                <p class="empty-title">暂无新闻</p>
                                <p class="empty-subtitle text-muted">目前还没有发布任何新闻动态。</p>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <jsp:include page="../common/layout_bottom.jsp" />