<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目详情" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    项目详情
                </h2>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=list" class="btn btn-link">返回列表</a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">${project.name}</h3>
                        <span class="badge bg-${project.status == 'approved' ? 'green' : project.status == 'pending' ? 'yellow' : 'secondary'}">
                            ${project.status == 'approved' ? '已批准' : project.status == 'pending' ? '待审批' : project.status}
                        </span>
                    </div>
                    <div class="card-body">
                        <h4 class="mb-3">项目描述</h4>
                        <p class="text-muted">${project.description}</p>
                        
                        <h4 class="mt-4 mb-3">项目信息</h4>
                        <div class="row">
                            <div class="col-md-6">
                                <p><strong>项目类型：</strong> ${project.category}</p>
                                <p><strong>年份：</strong> ${project.year}</p>
                                <p><strong>项目预算：</strong> ${project.budget != null ? project.budget : '未设置'}</p>
                                <p><strong>期望开始时间：</strong> <fmt:formatDate value="${project.expectedStartDate}" pattern="yyyy-MM-dd"/></p>
                                <p><strong>期望结束时间：</strong> <fmt:formatDate value="${project.expectedEndDate}" pattern="yyyy-MM-dd"/></p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>实际开始时间：</strong> <fmt:formatDate value="${project.actualStartDate}" pattern="yyyy-MM-dd"/></p>
                                <p><strong>实际结束时间：</strong> <fmt:formatDate value="${project.actualEndDate}" pattern="yyyy-MM-dd"/></p>
                                <p><strong>创建时间：</strong> <fmt:formatDate value="${project.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
                            </div>
                        </div>
                        
                        <c:if test="${not empty project.repoUrl}">
                        <h4 class="mt-4 mb-3">项目链接</h4>
                        <p><strong>仓库地址：</strong> <a href="${project.repoUrl}" target="_blank">${project.repoUrl}</a></p>
                        </c:if>
                        
                        <c:if test="${not empty project.docUrl}">
                        <p><strong>文档地址：</strong> <a href="${project.docUrl}" target="_blank">${project.docUrl}</a></p>
                        </c:if>
                    </div>
                </div>
                
                <c:if test="${isMember}">
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">项目成员</h3>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-vcenter">
                                <thead>
                                    <tr>
                                        <th>成员</th>
                                        <th>角色</th>
                                        <th>加入时间</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="member" items="${members}">
                                    <tr>
                                        <td>${member.operatorName}</td>
                                        <td>
                                            <span class="badge bg-${member.role == 'ADMIN' ? 'blue' : 'gray'}">
                                                ${member.role == 'ADMIN' ? '管理员' : '成员'}
                                            </span>
                                        </td>
                                        <td>${member.createdAt}</td>
                                    </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                </c:if>
            </div>
            
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${isMember && (project.status == 'approved' || project.status == 'in_progress' || project.status == 'completed')}">
                                <a href="${pageContext.request.contextPath}/project?action=workspace&id=${project.id}" class="btn btn-primary w-100 mb-2">
                                    进入项目工作区
                                </a>
                                <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn btn-outline-secondary w-100">
                                    查看项目历史
                                </a>
                            </c:when>
                            <c:when test="${isMember && project.status == 'pending'}">
                                <div class="alert alert-info mb-2">
                                    项目正在审批中，审批通过后即可进入工作区
                                </div>
                                <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn btn-outline-secondary w-100">
                                    查看项目历史
                                </a>
                            </c:when>
                            <c:when test="${hasApplication}">
                                <button class="btn btn-secondary w-100" disabled>
                                    已提交申请
                                </button>
                            </c:when>
                            <c:when test="${project.status == 'approved'}">
                                <a href="${pageContext.request.contextPath}/project?action=apply&id=${project.id}" class="btn btn-outline-primary w-100">
                                    申请加入项目
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button class="btn btn-secondary w-100" disabled>
                                    项目待审批
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />
