<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="申请活动" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    申请活动
                </h2>
                <p class="text-muted">填写活动信息提交申请，等待管理员审核</p>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-12">
                <form action="${pageContext.request.contextPath}/activity" method="POST" class="card">
                    <div class="card-header">
                        <h3 class="card-title">活动信息</h3>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label class="form-label required">活动标题</label>
                            <input type="text" class="form-control" name="title" placeholder="请输入活动标题" required>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">活动描述</label>
                            <textarea class="form-control" name="description" rows="4" placeholder="请输入活动描述"></textarea>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">活动地点</label>
                                    <input type="text" class="form-control" name="location" placeholder="请输入活动地点">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">最大参与人数</label>
                                    <input type="number" class="form-control" name="maxParticipants" placeholder="不限制请留空" min="0">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">开始时间</label>
                                    <input type="datetime-local" class="form-control" name="startTime">
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">结束时间</label>
                                    <input type="datetime-local" class="form-control" name="endTime">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer text-end">
                        <div class="d-flex">
                            <a href="${pageContext.request.contextPath}/activity/list" class="btn btn-link">取消</a>
                            <button type="submit" class="btn btn-primary ms-auto">提交申请</button>
                        </div>
                    </div>
                    <input type="hidden" name="action" value="apply">
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
