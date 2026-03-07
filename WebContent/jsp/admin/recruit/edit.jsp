<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../../common/layout_top.jsp">
            <jsp:param name="title" value="${empty app ? '添加招新记录' : '编辑招新记录'}" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            ${empty app ? '添加招新记录' : '编辑招新记录'}
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="row justify-content-center">
                    <div class="col-lg-8">
                        <form action="manage" method="POST" class="card">
                            <input type="hidden" name="action" value="${empty app ? 'create' : 'update'}">
                            <c:if test="${not empty app}">
                                <input type="hidden" name="id" value="${app.id}">
                            </c:if>
                            <div class="card-header">
                                <h3 class="card-title">基本信息</h3>
                            </div>
                            <div class="card-body">
                                <c:if test="${not empty error}">
                                    <div class="alert alert-danger">${error}</div>
                                </c:if>
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">姓名</label>
                                            <input type="text" class="form-control" name="name" value="${app.name}"
                                                required>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">学号</label>
                                            <input type="text" class="form-control" name="studentId"
                                                value="${app.studentId}" required>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">专业</label>
                                            <input type="text" class="form-control" name="major" value="${app.major}"
                                                required>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">班级/年级</label>
                                            <input type="text" class="form-control" name="grade" value="${app.grade}"
                                                required>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">手机号</label>
                                            <input type="text" class="form-control" name="phone" value="${app.phone}"
                                                required>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label required">邮箱</label>
                                            <input type="email" class="form-control" name="email" value="${app.email}"
                                                required>
                                        </div>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">申请理由/备注</label>
                                    <textarea class="form-control" name="reason" rows="5"
                                        required>${app.reason}</textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">处理状态</label>
                                    <select name="status" class="form-select">
                                        <option value="1" ${app.status==1 ? 'selected' : '' }>待处理</option>
                                        <option value="2" ${app.status==2 ? 'selected' : '' }>已通过</option>
                                        <option value="0" ${app.status==0 ? 'selected' : '' }>已拒绝</option>
                                    </select>
                                </div>
                            </div>
                            <div class="card-footer text-end">
                                <div class="d-flex">
                                    <a href="manage" class="btn btn-link">取消</a>
                                    <button type="submit" class="btn btn-primary ms-auto">${empty app ? '立即添加' :
                                        '保存更改'}</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../../common/layout_bottom.jsp" />