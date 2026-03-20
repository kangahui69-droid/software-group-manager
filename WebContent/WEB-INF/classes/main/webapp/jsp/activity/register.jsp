<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="活动报名" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    活动报名
                </h2>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-12">
                <!-- 错误提示 -->
                <c:if test="${not empty error}">
                    <div class="row">
                        <div class="col-12">
                            <div class="alert alert-danger" role="alert">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-alert-circle" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                    <circle cx="12" cy="12" r="9"></circle>
                                    <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                    <line x1="11" y1="12" x2="12" y2="12"></line>
                                    <line x1="11" y1="16" x2="12" y2="16"></line>
                                </svg>
                                ${error}
                            </div>
                        </div>
                    </div>
                </c:if>
                <!-- 成功提示 -->
                <c:if test="${not empty success}">
                    <div class="row">
                        <div class="col-12">
                            <div class="alert alert-success" role="alert">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-check" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                    <path d="M5 12l5 5l10 -10"></path>
                                </svg>
                                ${success}
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-body">
                        <form id="registerForm" method="post" action="${pageContext.request.contextPath}/activity/register" enctype="multipart/form-data">
                            <input type="hidden" name="activityId" value="${activity.id}">
                            
                            <!-- 活动信息 -->
                            <div class="mb-4">
                                <h3 class="mb-3">活动信息</h3>
                                <div class="card bg-light">
                                    <div class="card-body">
                                        <h4>${activity.title}</h4>
                                        <p class="text-muted">${activity.description}</p>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <p><strong>开始时间：</strong>${activity.startTime}</p>
                                                <p><strong>结束时间：</strong>${activity.endTime}</p>
                                            </div>
                                            <div class="col-md-6">
                                                <p><strong>地点：</strong>${activity.location}</p>
                                                <p><strong>分类：</strong>${activity.category}</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 参与者信息 -->
                            <div class="mb-4">
                                <h3 class="mb-3">参与者信息</h3>
                                <div class="mb-3">
                                    <label class="form-label required">姓名</label>
                                    <input type="text" class="form-control" name="name" placeholder="请输入姓名" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">电话</label>
                                    <input type="tel" class="form-control" name="phone" placeholder="请输入联系电话" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">邮箱</label>
                                    <input type="email" class="form-control" name="email" placeholder="请输入邮箱地址">
                                </div>
                            </div>

                            <!-- 多图片上传 -->
                            <div class="mb-4">
                                <h3 class="mb-3">上传材料</h3>
                                <div class="mb-3">
                                    <label class="form-label">活动相关材料（最多上传3张图片）</label>
                                    <input type="file" class="form-control" name="files" multiple accept="image/jpeg,image/jpg,image/png,image/gif,image/webp">
                                    <p class="text-muted mt-2">支持JPG、PNG、GIF格式，每张图片大小不超过1MB</p>
                                </div>
                            </div>

                            <!-- 成员关联 -->
                            <div class="mb-4">
                                <h3 class="mb-3">团队成员</h3>
                                <div class="mb-3">
                                    <label class="form-label">添加团队成员（可选）</label>
                                    <select class="form-select" name="members" multiple>
                                        <c:forEach var="member" items="${availableMembers}">
                                            <option value="${member.id}">${member.username}</option>
                                        </c:forEach>
                                    </select>
                                    <p class="text-muted mt-2">按住Ctrl键可多选</p>
                                </div>
                            </div>

                            <!-- 备注信息 -->
                            <div class="mb-4">
                                <h3 class="mb-3">备注信息</h3>
                                <div class="mb-3">
                                    <label class="form-label">备注</label>
                                    <textarea class="form-control" name="remark" rows="3" placeholder="请输入备注信息"></textarea>
                                </div>
                            </div>

                            <!-- 操作按钮 -->
                            <div class="d-flex justify-content-end">
                                <a href="${pageContext.request.contextPath}/activity/list" class="btn btn-secondary me-2">取消</a>
                                <button type="submit" class="btn btn-primary">提交报名</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-body">
                        <h3 class="card-title">报名须知</h3>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2 text-info" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                请确保填写的信息真实有效
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2 text-info" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                上传的图片大小不能超过1MB
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2 text-info" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                报名成功后将收到确认通知
                            </li>
                            <li class="list-group-item">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2 text-info" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12.01" y2="8"></line><polyline points="11 12 12 12 12 16 13 16"></polyline></svg>
                                如需取消报名，请提前联系组织者
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />
<script>
    // 表单提交验证
    document.getElementById('registerForm').addEventListener('submit', function(e) {
        const name = this.name.value;
        const phone = this.phone.value;
        const files = this.files.files;
        
        if (!name) {
            alert('请输入姓名');
            e.preventDefault();
            return;
        }
        
        if (!phone) {
            alert('请输入联系电话');
            e.preventDefault();
            return;
        }
        
        // 检查文件数量
        if (files.length > 3) {
            alert('最多只能上传3张图片');
            e.preventDefault();
            return;
        }
        
        // 检查文件大小
        for (let i = 0; i < files.length; i++) {
            if (files[i].size > 1 * 1024 * 1024) { // 1MB
                alert('每张图片大小不能超过1MB');
                e.preventDefault();
                return;
            }
        }
    });
</script>