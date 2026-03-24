<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="AI助手统计" />
</jsp:include>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title"><i class="bi bi-graph-up me-2"></i>AI助手提问统计</h3>
                        <div class="card-actions">
                            <a href="${pageContext.request.contextPath}/ai?action=chat" class="btn btn-sm btn-outline-primary">
                                <i class="bi bi-chat-dots me-1"></i>返回AI助手
                            </a>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table">
                                <thead>
                                    <tr>
                                        <th class="text-center" style="width: 50px;">#</th>
                                        <th>问题内容</th>
                                        <th class="text-center">提问次数</th>
                                        <th class="text-center">最后提问时间</th>
                                        <th class="text-center">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty stats}">
                                            <tr>
                                                <td colspan="5" class="text-center text-muted py-4">
                                                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                                    暂无统计数据
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="stat" items="${stats}" varStatus="status">
                                                <tr>
                                                    <td class="text-center text-muted">${status.index + 1}</td>
                                                    <td>
                                                        <div class="text-truncate" style="max-width: 400px;">
                                                            ${stat.normalizedQuestion}
                                                        </div>
                                                    </td>
                                                    <td class="text-center">
                                                        <span class="badge bg-blue">${stat.queryCount} 次</span>
                                                    </td>
                                                    <td class="text-center text-muted">
                                                        <c:if test="${stat.lastQueryAt != null}">
                                                            <fmt:formatDate value="${stat.lastQueryAt}" pattern="yyyy-MM-dd HH:mm"/>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">
                                                        <button class="btn btn-sm btn-outline-info" 
                                                                onclick="askQuestion('${stat.normalizedQuestion}')">
                                                            <i class="bi bi-chat-dots me-1"></i>提问
                                                        </button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title"><i class="bi bi-bar-chart me-2"></i>热门问题TOP10</h3>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <c:forEach var="stat" items="${stats}" begin="0" end="9" varStatus="status">
                                <div class="col-md-6 col-lg-4 mb-3">
                                    <div class="card card-sm">
                                        <div class="card-body">
                                            <div class="d-flex align-items-center">
                                                <div class="me-3">
                                                    <span class="badge bg-${status.index < 3 ? 'yellow' : 'blue'} text-${status.index < 3 ? 'yellow-fg' : 'blue-fg'}">
                                                        ${status.index + 1}
                                                    </span>
                                                </div>
                                                <div class="flex-fill">
                                                    <div class="text-truncate fw-medium">${stat.normalizedQuestion}</div>
                                                    <div class="text-muted small">${stat.queryCount} 次提问</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function askQuestion(question) {
    window.location.href = '${pageContext.request.contextPath}/ai?action=chat&question=' + encodeURIComponent(question);
}
</script>

<jsp:include page="../common/layout_bottom.jsp" />