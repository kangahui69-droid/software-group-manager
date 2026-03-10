<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <jsp:include page="../common/layout_top.jsp">
        <jsp:param name="title" value="招新报名" />
        <jsp:param name="active" value="recruit" />
    </jsp:include>

    <div class="page-header d-print-none">
        <div class="container-xl">
            <div class="row g-2 align-items-center">
                <div class="col">
                    <h2 class="page-title">
                        招新报名
                    </h2>
                </div>
            </div>
        </div>
    </div>

    <div class="page-body">
        <div class="container-xl">
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <form action="${pageContext.request.contextPath}/recruit/submit" method="POST" class="card">
                        <div class="card-header">
                            <h3 class="card-title">填写个人信息</h3>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label required">真实姓名</label>
                                <input type="text" class="form-control" name="name" placeholder="请输入姓名" required>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">学号</label>
                                        <input type="text" class="form-control" name="studentId" placeholder="请输入学号"
                                            required>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">专业</label>
                                        <input type="text" class="form-control" name="major" placeholder="请输入专业"
                                            required>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">年级</label>
                                        <input type="text" class="form-control" name="grade" placeholder="请输入年级"
                                            required>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label">邮箱</label>
                                        <input type="email" class="form-control" name="email" placeholder="可选填">
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">手机号</label>
                                        <input type="tel" class="form-control" name="phone" placeholder="请输入手机号"
                                            required>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label required">个人简介 & 加入理由</label>
                                <textarea class="form-control" name="reason" rows="6"
                                    placeholder="请简单介绍一下自己，以及为什么想加入我们小组。" required></textarea>
                            </div>
                        </div>
                        <div class="card-footer text-end">
                            <div class="d-flex">
                                <button type="reset" class="btn btn-link">重置</button>
                                <button type="submit" class="btn btn-primary ms-auto">提交报名</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../common/layout_bottom.jsp" />