<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目工作区 - ${project.name}" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    ${project.name}
                </h2>
                <div class="text-muted">
                    <c:choose>
                        <c:when test="${project.status == 'PENDING'}"><span class="badge bg-warning">申请审核中</span></c:when>
                        <c:when test="${project.status == 'APPROVED'}"><span class="badge bg-success">确认成立</span></c:when>
                        <c:when test="${project.status == 'IN_PROGRESS'}"><span class="badge bg-primary">进行中</span></c:when>
                        <c:when test="${project.status == 'COMPLETED'}"><span class="badge bg-info">已完成</span></c:when>
                        <c:when test="${project.status == 'CANCELED'}"><span class="badge bg-danger">已取消</span></c:when>
                        <c:when test="${project.status == 'REJECTED'}"><span class="badge bg-secondary">已驳回</span></c:when>
                        <c:when test="${project.status == 'PAUSED'}"><span class="badge bg-warning text-dark">项目暂停</span></c:when>
                    </c:choose>
                    <span class="ms-2">${project.year}年</span>
                </div>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/project?action=detail&id=${project.id}" class="btn btn-outline-primary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                    查看公开详情
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-lg-8">
                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目信息</h3>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label class="text-muted">项目描述</label>
                            <p>${project.description}</p>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-2">
                                <label class="text-muted">项目类型</label>
                                <p>${project.category}</p>
                            </div>
                            <div class="col-md-6 mb-2">
                                <label class="text-muted">项目预算</label>
                                <p><c:if test="${project.budget > 0}">¥${project.budget}</c:if><c:if test="${project.budget == null || project.budget == 0}">未设置</c:if></p>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-2">
                                <label class="text-muted">期望时间</label>
                                <p>
                                    <c:if test="${project.expectedStartDate != null}"><fmt:formatDate value="${project.expectedStartDate}" pattern="yyyy-MM-dd"/></c:if>
                                    <c:if test="${project.expectedEndDate != null}"> ~ <fmt:formatDate value="${project.expectedEndDate}" pattern="yyyy-MM-dd"/></c:if>
                                    <c:if test="${project.expectedStartDate == null && project.expectedEndDate == null}">未设置</c:if>
                                </p>
                            </div>
                            <div class="col-md-6 mb-2">
                                <label class="text-muted">项目管理员</label>
                                <p>${project.adminName}</p>
                            </div>
                        </div>
                        <c:if test="${not empty project.repoUrl}">
                        <div class="mb-2">
                            <label class="text-muted">代码仓库</label>
                            <p><a href="${project.repoUrl}" target="_blank">${project.repoUrl}</a></p>
                        </div>
                        </c:if>
                        <c:if test="${not empty project.docUrl}">
                        <div class="mb-2">
                            <label class="text-muted">项目文档</label>
                            <p><a href="${project.docUrl}" target="_blank">${project.docUrl}</a></p>
                        </div>
                        </c:if>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目成员</h3>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>姓名</th>
                                        <th>学号</th>
                                        <th>专业</th>
                                        <th>角色</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="member" items="${members}">
                                    <tr>
                                        <td>${member.userName}</td>
                                        <td>${member.studentId}</td>
                                        <td>${member.major}</td>
                                        <td>
                                            <c:if test="${member.role == 'ADMIN'}"><span class="badge bg-primary">管理员</span></c:if>
                                            <c:if test="${member.role == 'MEMBER'}"><span class="badge bg-secondary">成员</span></c:if>
                                        </td>
                                    </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目标签</h3>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <c:forEach var="label" items="${labels}">
                                <span class="badge bg-info me-1">${label}</span>
                            </c:forEach>
                            <c:if test="${empty labels}">暂无标签</c:if>
                        </div>
                        <div class="mb-2">
                            <form method="post" action="${pageContext.request.contextPath}/project" class="d-inline">
                                <input type="hidden" name="action" value="addLabel">
                                <input type="hidden" name="projectId" value="${project.id}">
                                <select name="labelCode" class="form-select form-select-sm d-inline-block" style="width: auto;">
                                    <option value="">添加标签</option>
                                    <c:forEach var="label" items="${allLabels}">
                                        <c:if test="${!labels.contains(label.code)}">
                                        <option value="${label.code}">${label.name}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="btn btn-sm btn-outline-primary">添加</button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目计划</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty plans}">
                            <p class="text-muted">暂无项目计划</p>
                        </c:if>
                        <c:forEach var="plan" items="${plans}">
                        <div class="mb-2 p-2 border rounded">
                            <strong>${plan.title}</strong>
                            <div class="text-muted small">
                                <fmt:formatDate value="${plan.startDate}" pattern="yyyy-MM-dd"/> ~ <fmt:formatDate value="${plan.endDate}" pattern="yyyy-MM-dd"/>
                            </div>
                            <c:if test="${isAdmin}">
                            <a href="${pageContext.request.contextPath}/project?action=deletePlan&projectId=${project.id}&planId=${plan.id}" class="btn btn-sm btn-outline-danger">删除</a>
                            </c:if>
                        </div>
                        </c:forEach>
                        <c:if test="${isAdmin}">
                        <form method="post" action="${pageContext.request.contextPath}/project" class="mt-3 border-top pt-3">
                            <input type="hidden" name="action" value="addPlan">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row">
                                <div class="col-md-4 mb-2">
                                    <input type="text" name="title" class="form-control" placeholder="计划标题" required>
                                </div>
                                <div class="col-md-3 mb-2">
                                    <input type="date" name="startDate" class="form-control" required>
                                </div>
                                <div class="col-md-3 mb-2">
                                    <input type="date" name="endDate" class="form-control" required>
                                </div>
                                <div class="col-md-2 mb-2">
                                    <button type="submit" class="btn btn-primary w-100">添加</button>
                                </div>
                            </div>
                        </form>
                        </c:if>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目进度</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${empty progressList}">
                            <p class="text-muted">暂无项目进度</p>
                        </c:if>
                        <c:forEach var="progress" items="${progressList}">
                        <div class="mb-2 p-2 border rounded">
                            <div class="d-flex justify-content-between">
                                <strong>${progress.title}</strong>
                                <span class="badge bg-${progress.completionRate >= 100 ? 'success' : progress.completionRate >= 50 ? 'warning' : 'secondary'}">${progress.completionRate}%</span>
                            </div>
                            <c:if test="${not empty progress.planTitle}">
                            <div class="text-muted small">关联计划: ${progress.planTitle}</div>
                            </c:if>
                            <div class="text-muted small">录入人: ${progress.creatorName} | <fmt:formatDate value="${progress.createdAt}" pattern="yyyy-MM-dd HH:mm"/></div>
                        </div>
                        </c:forEach>
                        <form method="post" action="${pageContext.request.contextPath}/project" class="mt-3 border-top pt-3">
                            <input type="hidden" name="action" value="addProgress">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row">
                                <div class="col-md-4 mb-2">
                                    <input type="text" name="title" class="form-control" placeholder="进度标题" required>
                                </div>
                                <div class="col-md-3 mb-2">
                                    <select name="planId" class="form-select">
                                        <option value="">关联计划（可选）</option>
                                        <c:forEach var="plan" items="${plans}">
                                        <option value="${plan.id}">${plan.title}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2 mb-2">
                                    <input type="number" name="completionRate" class="form-control" placeholder="完成%" min="0" max="100" value="0">
                                </div>
                                <div class="col-md-3 mb-2">
                                    <button type="submit" class="btn btn-primary w-100">添加进度</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目图片</h3>
                    </div>
                    <div class="card-body">
                        <div class="row mb-3">
                            <c:forEach var="img" items="${projectImages}">
                            <div class="col-md-3 mb-2">
                                <div class="border rounded p-2">
                                    <img src="${pageContext.request.contextPath}/localstorage/projects/${project.id}/images/${img[3]}" class="img-fluid mb-2" style="max-height: 150px; object-fit: cover;">
                                    <div class="d-flex justify-content-between">
                                        <small class="text-muted">${img[4]}</small>
                                        <a href="${pageContext.request.contextPath}/project?action=deleteFile&projectId=${project.id}&fileId=${img[0]}" class="btn btn-sm btn-outline-danger" onclick="return confirm('确定删除?');">删除</a>
                                    </div>
                                </div>
                            </div>
                            </c:forEach>
                            <c:if test="${empty projectImages}">
                                <p class="text-muted">暂无项目图片</p>
                            </c:if>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/project" enctype="multipart/form-data" class="border-top pt-3">
                            <input type="hidden" name="action" value="uploadImage">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row">
                                <div class="col-md-8 mb-2">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*" required>
                                    <small class="text-muted">支持 JPG, PNG, GIF 等图片格式，文件大小不超过 2MB</small>
                                </div>
                                <div class="col-md-4 mb-2">
                                    <button type="submit" class="btn btn-primary w-100">上传图片</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">项目文件</h3>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive mb-3">
                            <table class="table table-sm">
                                <thead>
                                    <tr>
                                        <th>文件名</th>
                                        <th>大小</th>
                                        <th>上传时间</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="file" items="${projectFiles}">
                                    <tr>
                                        <td>${file[5]}</td>
                                        <td><script>document.write((${file[6]}/1024).toFixed(1) + ' KB')</script></td>
                                        <td><fmt:formatDate value="${file[7]}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/project?action=downloadFile&projectId=${project.id}&fileId=${file[0]}" class="btn btn-sm btn-outline-primary">下载</a>
                                            <a href="${pageContext.request.contextPath}/project?action=deleteFile&projectId=${project.id}&fileId=${file[0]}" class="btn btn-sm btn-outline-danger" onclick="return confirm('确定删除?');">删除</a>
                                        </td>
                                    </tr>
                                    </c:forEach>
                                    <c:if test="${empty projectFiles}">
                                    <tr>
                                        <td colspan="4" class="text-muted text-center">暂无项目文件</td>
                                    </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/project" enctype="multipart/form-data" class="border-top pt-3">
                            <input type="hidden" name="action" value="uploadFile">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row">
                                <div class="col-md-8 mb-2">
                                    <input type="file" name="docFile" class="form-control" required>
                                    <small class="text-muted">支持 PDF, DOC, DOCX, TXT 等文档格式，文件大小不超过 50MB，不允许上传可执行文件</small>
                                </div>
                                <div class="col-md-4 mb-2">
                                    <button type="submit" class="btn btn-primary w-100">上传文件</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card mb-3">
                    <div class="card-header">
                        <h3 class="card-title">操作</h3>
                    </div>
                    <div class="card-body">
                        <c:if test="${isAdmin}">
                        <a href="${pageContext.request.contextPath}/project?action=memberApplications&id=${project.id}" class="btn btn-outline-primary w-100 mb-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                            成员申请审批
                        </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn btn-outline-secondary w-100 mb-2">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                            项目历史
                        </a>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">项目历史</h3>
                    </div>
                    <div class="card-body">
                        <c:forEach var="history" items="${historyList}" begin="0" end="9">
                        <div class="mb-2 border-bottom pb-2">
                            <div class="small text-muted"><fmt:formatDate value="${history.createdAt}" pattern="MM-dd HH:mm"/></div>
                            <div>${history.description}</div>
                        </div>
                        </c:forEach>
                        <c:if test="${empty historyList}">
                            <p class="text-muted">暂无历史记录</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />