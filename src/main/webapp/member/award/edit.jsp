<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<% 
    if (request.getAttribute("award") == null) {
        Integer awardId = null;
        try {
            awardId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list.jsp?error=invalid_id");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/award?action=edit&id=" + awardId);
        return;
    }
%>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="编辑奖项" />
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

    .quick-link {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 14px;
        border-radius: var(--radius-comfortable);
        text-decoration: none;
        transition: all 0.3s ease;
        border: 1px solid var(--border-light);
        margin-bottom: 8px;
    }

    .quick-link:hover {
        background: rgba(20, 86, 240, 0.05);
        border-color: var(--brand-blue);
    }

    .quick-link:last-child {
        margin-bottom: 0;
    }

    .quick-link-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.25rem;
        flex-shrink: 0;
    }

    .quick-link-content {
        flex: 1;
    }

    .quick-link-title {
        font-family: var(--font-ui);
        font-weight: 500;
        font-size: 0.875rem;
        color: var(--text-dark);
    }

    .quick-link-desc {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        color: var(--text-muted);
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
                <i class="bi bi-pencil me-2"></i>编辑奖项
            </h1>
            <p class="member-hero-subtitle">修改您的获奖信息</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-pencil-square text-brand"></i>编辑奖项信息
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <form action="${pageContext.request.contextPath}/award?action=update" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="id" value="${award.id}">

                            <div class="row g-4">
                                <div class="col-md-6">
                                    <div class="mb-4">
                                        <label class="form-label-design required">比赛名称</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-trophy"></i>
                                            </div>
                                            <input type="text" name="competition" value="${award.competition}" placeholder="请输入比赛名称" required>
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <label class="form-label-design required">比赛时间</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-calendar"></i>
                                            </div>
                                            <input type="date" name="competitionTime" value="<fmt:formatDate value='${award.competitionTime}' pattern='yyyy-MM-dd' />" required>
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <label class="form-label-design">比赛地点</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-geo-alt"></i>
                                            </div>
                                            <input type="text" name="competitionLocation" value="${award.competitionLocation}" placeholder="请输入比赛地点">
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-4">
                                        <label class="form-label-design">比赛界别</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-hash"></i>
                                            </div>
                                            <input type="text" name="competitionSession" value="${award.competitionSession}" placeholder="如：第12届">
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <label class="form-label-design required">比赛等级</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-bar-chart"></i>
                                            </div>
                                            <select name="competitionLevel" required>
                                                <option value="">请选择</option>
                                                <c:forEach var="dict" items="${competitionLevels}">
                                                    <option value="${dict.id}" <c:if test="${award.competitionLevel eq dict.id}">selected</c:if>>${dict.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <label class="form-label-design required">奖项类型</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-people"></i>
                                            </div>
                                            <select name="awardType" required>
                                                <option value="">请选择</option>
                                                <c:forEach var="dict" items="${awardTypes}">
                                                    <option value="${dict.id}" <c:if test="${award.awardType eq dict.id}">selected</c:if>>${dict.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="mb-4">
                                        <label class="form-label-design required">奖项类别</label>
                                        <div class="input-group-design">
                                            <div class="input-group-icon">
                                                <i class="bi bi-tag"></i>
                                            </div>
                                            <select name="awardCategory" required>
                                                <option value="">请选择</option>
                                                <c:forEach var="dict" items="${awardCategories}">
                                                    <option value="${dict.id}" <c:if test="${award.awardCategory eq dict.id}">selected</c:if>>${dict.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design">团队名称</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-collection"></i>
                                    </div>
                                    <input type="text" name="teamName" value="${award.teamName}" placeholder="团队奖项请填写团队名称">
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design required">奖项等级</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-award"></i>
                                    </div>
                                    <select name="awardLevel" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardLevels}">
                                            <option value="${dict.id}" <c:if test="${award.awardLevel eq dict.id}">selected</c:if>>${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label-design">补充奖项图片</label>
                                <input type="file" class="form-control" name="awardImages" multiple accept="image/*" style="border-radius: var(--radius-standard); padding: 12px 16px; border: 1px solid var(--border-gray);">
                                <div style="font-size: 0.81rem; color: var(--text-muted); margin-top: 8px;">可上传多张图片，支持JPG、PNG等格式</div>
                            </div>

                            <div class="d-flex justify-content-end gap-3">
                                <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="btn-outline-brand">
                                    <i class="bi bi-x-lg"></i>取消
                                </a>
                                <button type="submit" class="btn-brand">
                                    <i class="bi bi-check-lg"></i>保存修改
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-lightning text-brand"></i>快捷操作
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <a href="${pageContext.request.contextPath}/award?action=list" class="quick-link">
                            <div class="quick-link-icon" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                <i class="bi bi-list-ul"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">查看所有奖项</div>
                                <div class="quick-link-desc">浏览您提交的所有奖项</div>
                            </div>
                        </a>
                        <a href="javascript:void(0);" onclick="document.querySelector('form').submit();" class="quick-link">
                            <div class="quick-link-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                <i class="bi bi-save"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">保存修改</div>
                                <div class="quick-link-desc">保存当前编辑的奖项信息</div>
                            </div>
                        </a>
                        <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="quick-link">
                            <div class="quick-link-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                <i class="bi bi-arrow-left"></i>
                            </div>
                            <div class="quick-link-content">
                                <div class="quick-link-title">返回当前奖项</div>
                                <div class="quick-link-desc">查看当前奖项的详细信息</div>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />