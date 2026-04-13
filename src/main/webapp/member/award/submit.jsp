<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="提交奖项" />
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

    .file-upload-area {
        border: 2px dashed var(--border-gray);
        border-radius: var(--radius-comfortable);
        padding: 32px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .file-upload-area:hover {
        border-color: var(--brand-blue);
        background: rgba(20, 86, 240, 0.02);
    }

    .file-upload-icon {
        font-size: 48px;
        color: var(--text-muted);
        margin-bottom: 12px;
    }

    .file-upload-text {
        font-family: var(--font-ui);
        color: var(--text-secondary);
        font-size: 0.94rem;
    }

    .file-upload-hint {
        font-family: var(--font-ui);
        color: var(--text-muted);
        font-size: 0.81rem;
        margin-top: 8px;
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
                <i class="bi bi-trophy me-2"></i>提交奖项
            </h1>
            <p class="member-hero-subtitle">记录您的获奖经历和荣誉证书</p>
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
                    <i class="bi bi-file-earmark-plus text-brand"></i>奖项信息
                </h3>
            </div>
            <div class="card-body-design">
                <form action="${pageContext.request.contextPath}/award?action=submit" method="post" enctype="multipart/form-data">
                    <script>
                        document.addEventListener('DOMContentLoaded', function() {
                            var awardTypeSelect = document.querySelector('select[name="awardType"]');
                            var teamNameInput = document.querySelector('input[name="teamName"]');
                            var teamNameDiv = teamNameInput ? teamNameInput.closest('div.mb-4') : null;

                            updateTeamNameVisibility();

                            awardTypeSelect.addEventListener('change', updateTeamNameVisibility);

                            function updateTeamNameVisibility() {
                                var selectedValue = awardTypeSelect.value;
                                var selectedText = awardTypeSelect.options[awardTypeSelect.selectedIndex].text;
                                if (selectedText.includes('个人')) {
                                    teamNameDiv.style.display = 'none';
                                    if (teamNameInput) teamNameInput.value = '';
                                } else if (selectedText.includes('团队')) {
                                    teamNameDiv.style.display = 'block';
                                } else {
                                    teamNameDiv.style.display = 'block';
                                }
                            }
                        });
                    </script>

                    <div class="row g-4">
                        <div class="col-md-6">
                            <div class="mb-4">
                                <label class="form-label-design required">比赛名称</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-trophy"></i>
                                    </div>
                                    <input type="text" name="competition" placeholder="请输入比赛名称" required>
                                </div>
                            </div>
                            <div class="mb-4">
                                <label class="form-label-design required">比赛时间</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-calendar"></i>
                                    </div>
                                    <input type="date" name="competitionTime" required>
                                </div>
                            </div>
                            <div class="mb-4">
                                <label class="form-label-design">比赛地点</label>
                                <div class="input-group-design">
                                    <div class="input-group-icon">
                                        <i class="bi bi-geo-alt"></i>
                                    </div>
                                    <input type="text" name="competitionLocation" placeholder="请输入比赛地点">
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
                                    <input type="text" name="competitionSession" placeholder="如：第12届">
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
                                            <option value="${dict.id}">${dict.name}</option>
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
                                            <option value="${dict.id}">${dict.name}</option>
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
                                            <option value="${dict.id}">${dict.name}</option>
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
                            <input type="text" name="teamName" placeholder="团队奖项请填写团队名称">
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
                                    <option value="${dict.id}">${dict.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label-design required">奖项图片</label>
                        <input type="file" class="form-control input-design" name="awardImages" multiple accept="image/*" required style="border-radius: var(--radius-standard); padding: 12px 16px; border: 1px solid var(--border-gray);">
                        <div class="file-upload-hint mt-2">可上传多张图片，支持JPG、PNG等格式</div>
                    </div>

                    <div class="d-flex justify-content-end gap-3">
                        <a href="${pageContext.request.contextPath}/member/award/list.jsp" class="btn-outline-brand">
                            <i class="bi bi-x-lg"></i>取消
                        </a>
                        <button type="submit" class="btn-brand">
                            <i class="bi bi-check-lg"></i>提交审核
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />