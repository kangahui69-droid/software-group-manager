<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="创建项目" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

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

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
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
    .input-group-design select,
    .input-group-design textarea {
        flex: 1;
        border: none;
        padding: 12px 16px;
        font-family: var(--font-ui);
        font-size: 0.94rem;
        outline: none;
        background: transparent;
    }

    .input-group-design textarea {
        resize: vertical;
        min-height: 100px;
    }

    .input-group-icon {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(20, 86, 240, 0.05);
        color: var(--brand-blue);
        flex-shrink: 0;
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 12px 24px;
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
        padding: 12px 24px;
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
            <h1 class="member-hero-title">
                <i class="bi bi-plus-circle me-2"></i>创建项目
            </h1>
            <p class="member-hero-subtitle">创建一个新的项目并提交申请</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible mb-4" style="border-radius: var(--radius-standard);" role="alert">
                <i class="bi bi-exclamation-circle me-2"></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">
                    <i class="bi bi-kanban text-brand"></i>项目信息
                </h3>
            </div>
            <div class="card-body-design">
                <form action="${pageContext.request.contextPath}/project?action=create" method="POST">
                    <input type="hidden" name="action" value="create">

                    <div class="mb-4">
                        <label class="form-label-design required">项目名称</label>
                        <div class="input-group-design">
                            <div class="input-group-icon">
                                <i class="bi bi-folder"></i>
                            </div>
                            <input type="text" name="name" placeholder="请输入项目名称" required>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label-design required">项目描述</label>
                        <div class="input-group-design">
                            <div class="input-group-icon" style="align-self: flex-start; padding-top: 12px;">
                                <i class="bi bi-file-text"></i>
                            </div>
                            <textarea name="description" rows="4" placeholder="请输入项目描述" required></textarea>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label-design required">项目类型</label>
                        <div class="input-group-design">
                            <div class="input-group-icon">
                                <i class="bi bi-grid-3x3-gap"></i>
                            </div>
                            <select name="category" required>
                                <option value="">请选择项目类型</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.code}">${cat.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-md-6">
                            <div class="mb-4">
                                <label class="form-label-design required">年份</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-calendar"></i>
                                    </div>
                                    <input type="number" name="year" value="${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}" required>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-4">
                                <label class="form-label-design">项目预算（元）</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-currency-yen"></i>
                                    </div>
                                    <input type="number" name="budget" placeholder="0.00" step="0.01">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-md-6">
                            <div class="mb-4">
                                <label class="form-label-design">期望开始时间</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-calendar-event"></i>
                                    </div>
                                    <input type="date" name="expectedStartDate">
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-4">
                                <label class="form-label-design">期望结束时间</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-calendar-check"></i>
                                    </div>
                                    <input type="date" name="expectedEndDate">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label-design">项目代码仓库地址</label>
                        <div class="input-group-design">
                            <div class="input-group-icon">
                                <i class="bi bi-github"></i>
                            </div>
                            <input type="url" name="repoUrl" placeholder="https://github.com/...">
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label-design">项目说明文档地址</label>
                        <div class="input-group-design">
                            <div class="input-group-icon">
                                <i class="bi bi-file-earmark-text"></i>
                            </div>
                            <input type="url" name="docUrl" placeholder="https://...">
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-3">
                        <a href="${pageContext.request.contextPath}/project?action=list" class="btn-outline-brand">
                            <i class="bi bi-x-lg"></i>取消
                        </a>
                        <button type="submit" class="btn-brand">
                            <i class="bi bi-check-lg"></i>提交申请
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />