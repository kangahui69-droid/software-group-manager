<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="成员申请审批 - ${project.name}" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    成员申请审批
                </h2>
                <div class="text-muted">${project.name}</div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn btn-outline-primary">
                    返回项目
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">待审批申请</h3>
                    </div>
                    <div class="card-body">
                        <c:set var="hasPending" value="false"/>
                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status == 'pending'}">
                                <c:set var="hasPending" value="true"/>
                            </c:if>
                        </c:forEach>
                        
                        <c:if test="${!hasPending}">
                            <div class="text-center text-muted py-5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                <p class="mt-2">暂无待审批的申请</p>
                            </div>
                        </c:if>
                        
                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status == 'pending'}">
                            <div class="border rounded p-3 mb-3">
                                <div class="row align-items-center">
                                    <div class="col-md-6">
                                        <h5 class="mb-1">${app.userName}</h5>
                                        <div class="text-muted small">
                                            <span class="me-3">学号: ${app.studentId}</span>
                                            <span class="me-3">专业: ${app.major}</span>
                                        </div>
                                        <div class="mt-2 text-muted small">
                                            申请时间: <fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                        </div>
                                        <c:if test="${not empty app.reason}">
                                        <div class="mt-2">
                                            <strong>申请理由:</strong>
                                            <p class="mb-0 text-muted">${app.reason}</p>
                                        </div>
                                        </c:if>
                                    </div>
                                    <div class="col-md-6 text-md-end">
                                        <form method="post" action="${pageContext.request.contextPath}/project" class="d-inline">
                                            <input type="hidden" name="action" value="approveMember">
                                            <input type="hidden" name="applicationId" value="${app.id}">
                                            <input type="hidden" name="projectId" value="${project.id}">
                                            <button type="submit" class="btn btn-success">通过</button>
                                        </form>
                                        <button class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#rejectModal${app.id}">驳回</button>
                                        
                                        <div class="modal fade" id="rejectModal${app.id}" tabindex="-1">
                                            <div class="modal-dialog">
                                                <form method="post" action="${pageContext.request.contextPath}/project" class="modal-content">
                                                    <input type="hidden" name="action" value="rejectMember">
                                                    <input type="hidden" name="applicationId" value="${app.id}">
                                                    <input type="hidden" name="projectId" value="${project.id}">
                                                    <div class="modal-header">
                                                        <h5 class="modal-title">驳回申请</h5>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label class="form-label">驳回理由</label>
                                                            <textarea name="reason" class="form-control" rows="3" placeholder="请输入驳回理由"></textarea>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-link" data-bs-dismiss="modal">取消</button>
                                                        <button type="submit" class="btn btn-danger">确认驳回</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">历史申请记录</h3>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>申请人</th>
                                        <th>学号</th>
                                        <th>专业</th>
                                        <th>申请时间</th>
                                        <th>状态</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="app" items="${applications}">
                                    <c:if test="${app.status != 'PENDING'}">
                                    <tr>
                                        <td>${app.userName}</td>
                                        <td>${app.studentId}</td>
                                        <td>${app.major}</td>
                                        <td><fmt:formatDate value="${app.appliedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td>
                                            <c:if test="${app.status == 'CONFIRMED'}">
                                                <span class="badge bg-success">已通过</span>
                                            </c:if>
                                            <c:if test="${app.status == 'REJECTED'}">
                                                <span class="badge bg-danger">已驳回</span>
                                                <c:if test="${not empty app.reason}">
                                                <div class="small text-muted">理由: ${app.reason}</div>
                                                </c:if>
                                            </c:if>
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <c:set var="hasHistory" value="false"/>
                        <c:forEach var="app" items="${applications}">
                            <c:if test="${app.status != 'PENDING'}">
                                <c:set var="hasHistory" value="true"/>
                            </c:if>
                        </c:forEach>
                        <c:if test="${!hasHistory}">
                            <p class="text-muted text-center py-3">暂无历史记录</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />