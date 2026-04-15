<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/layout_top.jsp">
    <jsp:param name="title" value="招新报名" />
    <jsp:param name="active" value="recruit" />
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
        font-weight: 500;
        color: var(--text-dark);
        margin-bottom: 8px;
        display: block;
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

    textarea.input-design {
        resize: vertical;
        min-height: 120px;
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
        color: var(--text-secondary);
        border: 1px solid var(--border-gray);
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
        border-color: var(--brand-blue);
        color: var(--brand-blue);
    }

    .required::after {
        content: " *";
        color: #ef4444;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <h1 class="member-hero-title">
                <i class="bi bi-person-plus me-2"></i>招新报名
            </h1>
            <p class="member-hero-subtitle">填写个人信息加入我们</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-pencil-square"></i>填写个人信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form action="${pageContext.request.contextPath}/recruit/submit" method="POST">
                            <div class="mb-4">
                                <label class="form-label-design required">真实姓名</label>
                                <input type="text" class="input-design" name="name" placeholder="请输入姓名" required>
                            </div>
                            <div class="row mb-4">
                                <div class="col-md-6">
                                    <label class="form-label-design required">学号</label>
                                    <input type="text" class="input-design" name="studentId" placeholder="请输入学号" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design required">专业</label>
                                    <input type="text" class="input-design" name="major" placeholder="请输入专业" required>
                                </div>
                            </div>
                            <div class="row mb-4">
                                <div class="col-md-6">
                                    <label class="form-label-design required">年级</label>
                                    <input type="text" class="input-design" name="grade" placeholder="请输入年级" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label-design required">邮箱</label>
                                    <input type="email" class="input-design" name="email" placeholder="请输入邮箱" required>
                                </div>
                            </div>
                            <div class="mb-4">
                                <label class="form-label-design required">手机号</label>
                                <input type="tel" class="input-design" name="phone" placeholder="请输入手机号" required>
                            </div>
                            <div class="mb-4">
                                <label class="form-label-design required">个人简介 & 加入理由</label>
                                <textarea class="input-design" name="reason" rows="6" placeholder="请简单介绍一下自己，以及为什么想加入我们小组。" required></textarea>
                            </div>
                            <div class="d-flex gap-3 justify-content-end">
                                <button type="reset" class="btn-outline-brand">
                                    <i class="bi bi-arrow-counterclockwise"></i>重置
                                </button>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-check-lg"></i>提交报名
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/layout_bottom.jsp" />