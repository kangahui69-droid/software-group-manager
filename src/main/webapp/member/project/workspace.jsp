<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="项目工作区 - ${project.name}" />
</jsp:include>

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .card-body-design {
        padding: 24px;
    }

    .info-item {
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .info-item:last-child {
        border-bottom: none;
    }

    .info-label {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
        margin-bottom: 4px;
    }

    .info-value {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        color: var(--text-dark);
        font-weight: 500;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-sm-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .btn-sm-outline {
        background: transparent;
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-outline:hover {
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }

    .btn-sm-danger {
        background: transparent;
        color: #ef4444;
        border: 1px solid rgba(239, 68, 68, 0.3);
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-sm-danger:hover {
        background: #ef4444;
        color: white;
    }

    .table-design {
        width: 100%;
        border-collapse: collapse;
    }

    .table-design th {
        font-family: var(--font-ui);
        font-weight: 600;
        color: var(--text-secondary);
        border-bottom: 2px solid var(--border-gray);
        padding: 12px 16px;
        text-align: left;
        font-size: 0.81rem;
    }

    .table-design td {
        padding: 12px 16px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
    }

    .table-design tbody tr:hover {
        background: rgba(20, 86, 240, 0.03);
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .badge-primary {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
    }

    .badge-secondary {
        background: rgba(139, 92, 246, 0.1);
        color: #8b5cf6;
    }

    .badge-success {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-warning {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
    }

    .input-group-design {
        display: flex;
        align-items: center;
        background: var(--bg-white);
        border: 1px solid var(--border-gray);
        border-radius: var(--radius-standard);
        overflow: hidden;
        transition: all 0.3s ease;
    }

    .input-group-design:focus-within {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
    }

    .input-group-design input,
    .input-group-design select {
        flex: 1;
        border: none;
        padding: 10px 14px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        outline: none;
        background: transparent;
    }

    .link-primary {
        color: var(--brand-blue);
        text-decoration: none;
        font-weight: 500;
    }

    .link-primary:hover {
        text-decoration: underline;
    }

    .label-tag {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 4px 12px;
        background: rgba(20, 86, 240, 0.08);
        color: var(--brand-blue);
        border-radius: var(--radius-pill);
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        margin-right: 8px;
        margin-bottom: 8px;
    }

    .plan-card {
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        padding: 16px;
        margin-bottom: 12px;
        border: 1px solid var(--border-light);
    }

    .progress-item {
        background: rgba(20, 86, 240, 0.03);
        border-radius: var(--radius-comfortable);
        padding: 16px;
        margin-bottom: 12px;
        border: 1px solid var(--border-light);
    }

    .image-thumb {
        width: 100%;
        height: 120px;
        object-fit: cover;
        border-radius: var(--radius-standard);
        margin-bottom: 8px;
    }

    .file-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .file-item:last-child {
        border-bottom: none;
    }

    .history-item {
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);
    }

    .history-item:last-child {
        border-bottom: none;
    }

    @media (max-width: 768px) {
        .member-hero {
            padding: 24px;
        }

        .member-hero-title {
            font-size: 1.5rem;
        }
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                <div>
                    <h1 class="member-hero-title">
                        <i class="bi bi-kanban me-2"></i>${project.name}
                    </h1>
                    <p class="member-hero-subtitle">
                        <span class="badge-design ${project.status == 'PENDING' ? 'badge-warning' : project.status == 'APPROVED' ? 'badge-success' : 'badge-primary'}">
                            <c:choose>
                                <c:when test="${project.status == 'PENDING'}">申请审核中</c:when>
                                <c:when test="${project.status == 'APPROVED'}">确认成立</c:when>
                                <c:when test="${project.status == 'IN_PROGRESS'}">进行中</c:when>
                                <c:when test="${project.status == 'COMPLETED'}">已完成</c:when>
                                <c:when test="${project.status == 'CANCELED'}">已取消</c:when>
                                <c:when test="${project.status == 'REJECTED'}">已驳回</c:when>
                                <c:otherwise>${project.status}</c:otherwise>
                            </c:choose>
                        </span>
                        <span class="ms-2">${project.year}年</span>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/project?action=detail&id=${project.id}" class="btn-outline-brand" style="background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.3); color: white;">
                    <i class="bi bi-eye"></i>查看公开详情
                </a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle text-brand"></i>项目信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">项目描述</div>
                                    <div class="info-value">${project.description}</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-item">
                                    <div class="info-label">项目类型</div>
                                    <div class="info-value">${project.category}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">项目预算</div>
                                    <div class="info-value">
                                        <c:if test="${project.budget > 0}">¥${project.budget}</c:if>
                                        <c:if test="${project.budget == null || project.budget == 0}">未设置</c:if>
                                    </div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">期望时间</div>
                                    <div class="info-value">
                                        <c:if test="${project.expectedStartDate != null}"><fmt:formatDate value="${project.expectedStartDate}" pattern="yyyy-MM-dd"/></c:if>
                                        <c:if test="${project.expectedEndDate != null}"> ~ <fmt:formatDate value="${project.expectedEndDate}" pattern="yyyy-MM-dd"/></c:if>
                                        <c:if test="${project.expectedStartDate == null && project.expectedEndDate == null}">未设置</c:if>
                                    </div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">项目管理员</div>
                                    <div class="info-value">${project.adminName}</div>
                                </div>
                            </div>
                        </div>
                        <c:if test="${not empty project.repoUrl}">
                            <div class="info-item">
                                <div class="info-label">代码仓库</div>
                                <div class="info-value"><a href="${project.repoUrl}" target="_blank" class="link-primary">${project.repoUrl}</a></div>
                            </div>
                        </c:if>
                        <c:if test="${not empty project.docUrl}">
                            <div class="info-item">
                                <div class="info-label">项目文档</div>
                                <div class="info-value"><a href="${project.docUrl}" target="_blank" class="link-primary">${project.docUrl}</a></div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-people text-brand"></i>项目成员
                        </h3>
                    </div>
                    <div class="card-body-design" style="padding: 0;">
                        <table class="table-design">
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
                                            <span class="badge-design ${member.role == 'ADMIN' ? 'badge-primary' : 'badge-secondary'}">
                                                ${member.role == 'ADMIN' ? '管理员' : '成员'}
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-tags text-brand"></i>项目标签
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="mb-3">
                            <c:forEach var="label" items="${labels}">
                                <span class="label-tag">
                                    <i class="bi bi-tag"></i>${label}
                                </span>
                            </c:forEach>
                            <c:if test="${empty labels}">
                                <span class="text-muted">暂无标签</span>
                            </c:if>
                        </div>
                        <c:if test="${isAdmin}">
                            <form method="post" action="${pageContext.request.contextPath}/project" class="d-inline">
                                <input type="hidden" name="action" value="addLabel">
                                <input type="hidden" name="projectId" value="${project.id}">
                                <div class="input-group-design" style="max-width: 300px;">
                                    <select name="labelCode">
                                        <option value="">添加标签</option>
                                        <c:forEach var="label" items="${allLabels}">
                                            <c:if test="${!labels.contains(label.code)}">
                                                <option value="${label.code}">${label.name}</option>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                    <button type="submit" class="btn-sm-brand" style="margin-left: 8px;">添加</button>
                                </div>
                            </form>
                        </c:if>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-list-check text-brand"></i>项目计划
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${empty plans}">
                            <p class="text-muted">暂无项目计划</p>
                        </c:if>
                        <c:forEach var="plan" items="${plans}">
                            <div class="plan-card">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <strong>${plan.title}</strong>
                                        <div class="text-muted small">
                                            <fmt:formatDate value="${plan.startDate}" pattern="yyyy-MM-dd"/> ~ <fmt:formatDate value="${plan.endDate}" pattern="yyyy-MM-dd"/>
                                        </div>
                                    </div>
                                    <c:if test="${isAdmin}">
                                        <a href="${pageContext.request.contextPath}/project?action=deletePlan&projectId=${project.id}&planId=${plan.id}" class="btn-sm-danger" onclick="return confirm('确定删除?');">
                                            <i class="bi bi-trash"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${isAdmin}">
                            <form method="post" action="${pageContext.request.contextPath}/project" class="mt-3 pt-3" style="border-top: 1px solid var(--border-light);">
                                <input type="hidden" name="action" value="addPlan">
                                <input type="hidden" name="projectId" value="${project.id}">
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <input type="text" name="title" class="form-control" placeholder="计划标题" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                    </div>
                                    <div class="col-md-3">
                                        <input type="date" name="startDate" class="form-control" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                    </div>
                                    <div class="col-md-3">
                                        <input type="date" name="endDate" class="form-control" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                    </div>
                                    <div class="col-md-2">
                                        <button type="submit" class="btn-brand w-100 justify-content-center">添加</button>
                                    </div>
                                </div>
                            </form>
                        </c:if>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-graph-up text-brand"></i>项目进度
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${empty progressList}">
                            <p class="text-muted">暂无项目进度</p>
                        </c:if>
                        <c:forEach var="progress" items="${progressList}">
                            <div class="progress-item">
                                <div class="d-flex justify-content-between mb-2">
                                    <strong>${progress.title}</strong>
                                    <span class="badge-design ${progress.completionRate >= 100 ? 'badge-success' : progress.completionRate >= 50 ? 'badge-warning' : 'badge-secondary'}">
                                        ${progress.completionRate}%
                                    </span>
                                </div>
                                <c:if test="${not empty progress.planTitle}">
                                    <div class="text-muted small">关联计划: ${progress.planTitle}</div>
                                </c:if>
                                <div class="text-muted small mt-1">录入人: ${progress.creatorName} | <fmt:formatDate value="${progress.createdAt}" pattern="yyyy-MM-dd HH:mm"/></div>
                            </div>
                        </c:forEach>
                        <form method="post" action="${pageContext.request.contextPath}/project" class="mt-3 pt-3" style="border-top: 1px solid var(--border-light);">
                            <input type="hidden" name="action" value="addProgress">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <input type="text" name="title" class="form-control" placeholder="进度标题" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                </div>
                                <div class="col-md-3">
                                    <select name="planId" class="form-control" style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                        <option value="">关联计划（可选）</option>
                                        <c:forEach var="plan" items="${plans}">
                                            <option value="${plan.id}">${plan.title}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <input type="number" name="completionRate" class="form-control" placeholder="完成%" min="0" max="100" value="0" style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                </div>
                                <div class="col-md-3">
                                    <button type="submit" class="btn-brand w-100 justify-content-center">添加进度</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-images text-brand"></i>项目图片
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row mb-3">
                            <c:forEach var="img" items="${projectImages}">
                                <div class="col-md-3 mb-3">
                                    <div class="border rounded p-2" style="border-radius: var(--radius-comfortable);">
                                        <img src="${pageContext.request.contextPath}/localstorage/projects/${project.id}/images/${img[3]}" class="image-thumb" style="border-radius: var(--radius-standard);">
                                        <div class="d-flex justify-content-between align-items-center mt-2">
                                            <small class="text-muted">${img[4]}</small>
                                            <a href="${pageContext.request.contextPath}/project?action=deleteFile&projectId=${project.id}&fileId=${img[0]}" class="btn-sm-danger" onclick="return confirm('确定删除?');">
                                                <i class="bi bi-trash"></i>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty projectImages}">
                                <div class="col-12">
                                    <p class="text-muted text-center py-3">暂无项目图片</p>
                                </div>
                            </c:if>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/project" enctype="multipart/form-data" class="pt-3" style="border-top: 1px solid var(--border-light);">
                            <input type="hidden" name="action" value="uploadImage">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row g-3">
                                <div class="col-md-8">
                                    <input type="file" name="imageFile" class="form-control" accept="image/*" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                    <small class="text-muted">支持 JPG, PNG, GIF 等图片格式，文件大小不超过 2MB</small>
                                </div>
                                <div class="col-md-4">
                                    <button type="submit" class="btn-brand w-100 justify-content-center">上传图片</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-file-earmark-text text-brand"></i>项目文件
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:forEach var="file" items="${projectFiles}">
                            <div class="file-item">
                                <div>
                                    <div style="font-weight: 500;">${file[5]}</div>
                                    <div class="text-muted small">
                                        <script>document.write((${file[6]}/1024).toFixed(1) + ' KB')</script>
                                        | <fmt:formatDate value="${file[7]}" pattern="yyyy-MM-dd HH:mm"/>
                                    </div>
                                </div>
                                <div class="d-flex gap-2">
                                    <a href="${pageContext.request.contextPath}/project?action=downloadFile&projectId=${project.id}&fileId=${file[0]}" class="btn-sm-outline">
                                        <i class="bi bi-download"></i>下载
                                    </a>
                                    <a href="${pageContext.request.contextPath}/project?action=deleteFile&projectId=${project.id}&fileId=${file[0]}" class="btn-sm-danger" onclick="return confirm('确定删除?');">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty projectFiles}">
                            <p class="text-muted text-center py-3">暂无项目文件</p>
                        </c:if>
                        <form method="post" action="${pageContext.request.contextPath}/project" enctype="multipart/form-data" class="pt-3" style="border-top: 1px solid var(--border-light);">
                            <input type="hidden" name="action" value="uploadFile">
                            <input type="hidden" name="projectId" value="${project.id}">
                            <div class="row g-3">
                                <div class="col-md-8">
                                    <input type="file" name="docFile" class="form-control" required style="border-radius: var(--radius-standard); padding: 10px 14px;">
                                    <small class="text-muted">支持 PDF, DOC, DOCX, TXT 等文档格式，文件大小不超过 50MB</small>
                                </div>
                                <div class="col-md-4">
                                    <button type="submit" class="btn-brand w-100 justify-content-center">上传文件</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design mb-4">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-lightning text-brand"></i>操作
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:if test="${isAdmin}">
                            <a href="${pageContext.request.contextPath}/project?action=memberApplications&id=${project.id}" class="btn-outline-brand w-100 mb-2 justify-content-center">
                                <i class="bi bi-people"></i>成员申请审批
                            </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/project?action=history&id=${project.id}" class="btn-outline-brand w-100 justify-content-center">
                            <i class="bi bi-clock-history"></i>项目历史
                        </a>
                    </div>
                </div>

                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-history text-brand"></i>项目历史
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <c:forEach var="history" items="${historyList}" begin="0" end="9">
                            <div class="history-item">
                                <div class="text-muted small"><fmt:formatDate value="${history.createdAt}" pattern="MM-dd HH:mm"/></div>
                                <div style="font-size: 0.875rem;">${history.description}</div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty historyList}">
                            <p class="text-muted text-center py-3">暂无历史记录</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />