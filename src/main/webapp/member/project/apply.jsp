<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="申请加入项目 - ${project.name}" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    申请加入项目
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">申请信息</h3>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label class="text-muted">项目名称</label>
                            <h4>${project.name}</h4>
                        </div>
                        <div class="mb-3">
                            <label class="text-muted">项目描述</label>
                            <p>${project.description}</p>
                        </div>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">填写申请</h3>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/project" method="POST">
                            <input type="hidden" name="action" value="applyMember">
                            <input type="hidden" name="id" value="${project.id}">
                            
                            <div class="mb-3">
                                <label class="form-label required">申请理由</label>
                                <textarea class="form-control" name="reason" rows="5" placeholder="请说明您想加入该项目的原因和优势" required></textarea>
                            </div>
                            
                            <div class="mb-3">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" required>
                                    <label class="form-check-label">
                                        我已阅读并了解项目信息，愿意申请加入
                                    </label>
                                </div>
                            </div>
                            
                            <div class="text-end">
                                <a href="${pageContext.request.contextPath}/project?action=detail&id=${project.id}" class="btn btn-link">取消</a>
                                <button type="submit" class="btn btn-primary">提交申请</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">申请须知</h3>
                    </div>
                    <div class="card-body">
                        <ul class="list-unstyled text-muted">
                            <li class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon text-success" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                提交申请后需要项目管理员审批
                            </li>
                            <li class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon text-success" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                审批通过后您将成为项目成员
                            </li>
                            <li class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon text-success" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                申请被驳回后可再次申请
                            </li>
                            <li class="mb-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon text-success" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                成员可查看项目完整信息
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />