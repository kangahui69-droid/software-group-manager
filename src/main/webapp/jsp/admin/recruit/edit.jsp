<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="${empty app ? '添加招新记录' : '编辑招新记录'}" />
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
<style>
    .admin-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .admin-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .admin-hero-subtitle {
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

    .card-body-design {
        padding: 0;
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

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
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

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-success-design {
        background-color: #10b981;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-success-design:hover {
        background-color: #059669;
        color: white;
    }

    .btn-danger-design {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 6px 12px;
        font-size: 0.75rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-danger-design:hover {
        background-color: #dc2626;
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
        padding: 14px 20px;
        text-align: left;
        font-size: 0.81rem;
        background: rgba(20, 86, 240, 0.03);
    }

    .table-design td {
        padding: 16px 20px;
        vertical-align: middle;
        border-bottom: 1px solid var(--border-light);
        font-family: var(--font-ui);
        font-size: 0.875rem;
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

    .input-design {
        border-radius: var(--radius-standard);
        border: 1px solid var(--border-gray);
        padding: 10px 16px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        transition: all 0.3s ease;
        width: 100%;
    }

    .input-design:focus {
        border-color: var(--brand-blue);
        box-shadow: 0 0 0 3px rgba(20, 86, 240, 0.1);
        outline: none;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
        font-size: 0.875rem;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 class="admin-hero-title">
                        <i class="bi bi-pencil-square me-2"></i>${empty app ? '添加招新记录' : '编辑招新记录'}
                    </h1>
                    <p class="admin-hero-subtitle">${empty app ? '创建新的招新记录' : '修改招新记录信息'}</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/recruit/manage" class="btn btn-outline-brand" style="color: white; border-color: white;">
                    <i class="bi bi-arrow-left me-1"></i>返回
                </a>
            </div>
        </div>

        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">姓名</label>
                                    <input type="text" class="input-design" name="name" value="${app.name}"
                                        required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">学号</label>
                                    <input type="text" class="input-design" name="studentId"
                                        value="${app.studentId}" required>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">专业</label>
                                    <input type="text" class="input-design" name="major" value="${app.major}"
                                        required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">班级/年级</label>
                                    <input type="text" class="input-design" name="grade" value="${app.grade}"
                                        required>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">手机号</label>
                                    <input type="text" class="input-design" name="phone" value="${app.phone}"
                                        required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label-design required">邮箱</label>
                                    <input type="email" class="input-design" name="email" value="${app.email}"
                                        required>
                                </div>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label-design required">申请理由/备注</label>
                            <textarea class="input-design" name="reason" rows="5"
                                required>${app.reason}</textarea>
                        </div>
                        <div class="mb-3">
                            <label class="form-label-design">处理状态</label>
                            <select name="status" class="input-design">
                                <option value="1" ${app.status==1 ? 'selected' : '' }>待处理</option>
                                <option value="2" ${app.status==2 ? 'selected' : '' }>已通过</option>
                                <option value="0" ${app.status==0 ? 'selected' : '' }>已拒绝</option>
                            </select>
                        </div>
                    </div>
                    <div class="card-footer-design text-end">
                        <div class="d-flex">
                            <a href="manage" class="btn-outline-brand">取消</a>
                            <button type="submit" class="btn-brand ms-auto">${empty app ? '立即添加' :
                                '保存更改'}</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/layout_bottom.jsp" />