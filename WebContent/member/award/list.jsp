<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<% if (request.getAttribute("awards") == null) {
    response.sendRedirect(request.getContextPath() + "/award?action=list");
    return;
} %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="我的奖项" />
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

    .stat-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 20px;
        transition: all 0.3s ease;
    }

    .stat-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-offset);
    }

    .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: var(--radius-standard);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        margin-bottom: 12px;
    }

    .stat-title {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
        margin-bottom: 4px;
    }

    .stat-value {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        color: var(--text-dark);
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
        font-size: 0.81rem;
        font-weight: 500;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }

    .btn-sm-brand:hover {
        background-color: var(--primary-600);
        color: white;
    }

    .filter-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        padding: 20px;
        margin-bottom: 16px;
    }

    .form-label-design {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        font-weight: 500;
        color: var(--text-secondary);
        margin-bottom: 6px;
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
    .input-group-design select {
        flex: 1;
        border: none;
        padding: 10px 14px;
        font-family: var(--font-ui);
        font-size: 0.875rem;
        outline: none;
        background: transparent;
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
        padding: 16px;
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

    .badge-pending {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
    }

    .badge-approved {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .badge-rejected {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .action-btns {
        display: flex;
        gap: 8px;
    }

    .btn-action {
        padding: 6px 12px;
        font-size: 0.75rem;
        border-radius: var(--radius-standard);
        border: none;
        cursor: pointer;
        transition: all 0.2s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 4px;
    }

    .btn-action-detail {
        background: rgba(20, 86, 240, 0.1);
        color: var(--brand-blue);
    }

    .btn-action-detail:hover {
        background: var(--brand-blue);
        color: white;
    }

    .btn-action-edit {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
    }

    .btn-action-edit:hover {
        background: #f59e0b;
        color: white;
    }

    .btn-action-delete {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .btn-action-delete:hover {
        background: #ef4444;
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
                <i class="bi bi-trophy me-2"></i>我的奖项
            </h1>
            <p class="member-hero-subtitle">管理您的获奖记录和荣誉证书</p>
        </div>

        <c:if test="${param.message == 'submit_success'}">
            <div class="alert alert-success alert-dismissible mb-4" style="border-radius: var(--radius-standard);" role="alert">
                <i class="bi bi-check-circle me-2"></i>提交成功！
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="row g-4">
            <div class="col-lg-8">
                <a href="${pageContext.request.contextPath}/award?action=submit" class="btn-brand mb-3">
                    <i class="bi bi-plus-lg"></i>提交新奖项
                </a>

                <div class="filter-card">
                    <form id="filterForm" class="row g-3">
                        <div class="col-md-3">
                            <label class="form-label-design">比赛名称</label>
                            <div class="input-group-design">
                                <input type="text" name="competition" placeholder="输入比赛名称">
                            </div>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">比赛等级</label>
                            <div class="input-group-design">
                                <select name="competitionLevel">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${competitionLevels}">
                                        <option value="${dict.id}">${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">奖项等级</label>
                            <div class="input-group-design">
                                <select name="awardLevel">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${awardLevels}">
                                        <option value="${dict.id}">${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label-design">状态</label>
                            <div class="input-group-design">
                                <select name="status">
                                    <option value="">全部</option>
                                    <option value="PENDING">审核中</option>
                                    <option value="APPROVED">已通过</option>
                                    <option value="REJECTED">已拒绝</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-3 align-self-end">
                            <button type="button" id="filterBtn" class="btn-brand">筛选</button>
                            <button type="button" id="resetBtn" class="btn-outline-brand ms-2">重置</button>
                        </div>
                    </form>
                </div>

                <div class="card-design">
                    <div class="card-body-design" style="padding: 0;">
                        <table class="table-design">
                            <thead>
                                <tr>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competition" class="text-reset text-decoration-none">比赛名称</a></th>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionLevel" class="text-reset text-decoration-none">比赛等级</a></th>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=awardLevel" class="text-reset text-decoration-none">奖项等级</a></th>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionLocation" class="text-reset text-decoration-none">比赛地点</a></th>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionTime" class="text-reset text-decoration-none">比赛时间</a></th>
                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=awardStatus" class="text-reset text-decoration-none">状态</a></th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="award" items="${awards}">
                                    <tr>
                                        <td>${award.competition}</td>
                                        <td>
                                            <c:forEach var="dict" items="${competitionLevels}">
                                                <c:if test="${award.competitionLevel eq dict.id}">${dict.name}</c:if>
                                            </c:forEach>
                                        </td>
                                        <td>
                                            <c:forEach var="dict" items="${awardLevels}">
                                                <c:if test="${award.awardLevel eq dict.id}">${dict.name}</c:if>
                                            </c:forEach>
                                        </td>
                                        <td>${award.competitionLocation}</td>
                                        <td>${award.competitionTime}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${award.awardStatus eq 'PENDING'}">
                                                    <span class="badge-design badge-pending">审核中</span>
                                                </c:when>
                                                <c:when test="${award.awardStatus eq 'APPROVED'}">
                                                    <span class="badge-design badge-approved">已通过</span>
                                                </c:when>
                                                <c:when test="${award.awardStatus eq 'REJECTED'}">
                                                    <span class="badge-design badge-rejected">已拒绝</span>
                                                </c:when>
                                                <c:otherwise>${award.awardStatus}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="action-btns">
                                                <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="btn-action btn-action-detail">
                                                    <i class="bi bi-eye"></i>详情
                                                </a>
                                                <c:if test="${award.awardStatus eq 'PENDING'}">
                                                    <a href="${pageContext.request.contextPath}/award?action=edit&id=${award.id}" class="btn-action btn-action-edit">
                                                        <i class="bi bi-pencil"></i>编辑
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/award?action=delete&id=${award.id}" class="btn-action btn-action-delete" onclick="return confirm('确定要删除这个奖项吗？')">
                                                        <i class="bi bi-trash"></i>删除
                                                    </a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-bar-chart text-brand"></i>奖项统计
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row g-3">
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #1456f0, #60a5fa);">
                                        <i class="bi bi-trophy text-white"></i>
                                    </div>
                                    <div class="stat-title">个人获奖总数</div>
                                    <div class="stat-value text-brand"><c:out value="${awardStats.totalPersonalAwards}" default="0" /></div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
                                        <i class="bi bi-person text-white"></i>
                                    </div>
                                    <div class="stat-title">个人奖项</div>
                                    <div class="stat-value" style="color: #10b981;"><c:out value="${awardStats.personalAwards}" default="0" /></div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);">
                                        <i class="bi bi-people text-white"></i>
                                    </div>
                                    <div class="stat-title">团体奖项</div>
                                    <div class="stat-value" style="color: #8b5cf6;"><c:out value="${awardStats.teamAwards}" default="0" /></div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
                                        <i class="bi bi-award text-white"></i>
                                    </div>
                                    <div class="stat-title">国家级奖项</div>
                                    <div class="stat-value" style="color: #f59e0b;"><c:out value="${awardStats.nationalAwards}" default="0" /></div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444, #f87171);">
                                        <i class="bi bi-geo-alt text-white"></i>
                                    </div>
                                    <div class="stat-title">省级奖项</div>
                                    <div class="stat-value" style="color: #ef4444;"><c:out value="${awardStats.provincialAwards}" default="0" /></div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="stat-card">
                                    <div class="stat-icon" style="background: linear-gradient(135deg, #ec4899, #f472b6);">
                                        <i class="bi bi-calendar text-white"></i>
                                    </div>
                                    <div class="stat-title">当前年度获奖</div>
                                    <div class="stat-value" style="color: #ec4899;"><c:out value="${awardStats.currentYearAwards}" default="0" /></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const filterForm = document.getElementById('filterForm');
        const filterBtn = document.getElementById('filterBtn');
        const resetBtn = document.getElementById('resetBtn');
        const awardTableBody = document.querySelector('table tbody');

        filterBtn.addEventListener('click', function() {
            filterAwards();
        });

        resetBtn.addEventListener('click', function() {
            filterForm.reset();
            filterAwards();
        });

        function filterAwards() {
            const formData = new FormData(filterForm);
            const params = new URLSearchParams();

            formData.forEach((value, key) => {
                params.append(key, value);
            });

            awardTableBody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">加载中...</span></div></td></tr>';

            const requestUrl = '${pageContext.request.contextPath}/award?action=filter&' + params.toString();

            fetch(requestUrl, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            })
            .then(response => response.text())
            .then(text => {
                if (!text) throw new Error('空响应');
                return JSON.parse(text);
            })
            .then(data => {
                updateAwardTable(data);
            })
            .catch(error => {
                console.error('筛选失败:', error);
                awardTableBody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">筛选失败，请重试</td></tr>';
            });
        }

        function updateAwardTable(awards) {
            if (awards.length === 0) {
                awardTableBody.innerHTML = '<tr><td colspan="7" class="text-center">暂无符合条件的奖项</td></tr>';
                return;
            }

            let html = '';
            const contextPath = '${pageContext.request.contextPath}';
            awards.forEach(award => {
                let statusBadge = '';
                if (award.awardStatus === 'PENDING') {
                    statusBadge = '<span class="badge-design badge-pending">审核中</span>';
                } else if (award.awardStatus === 'APPROVED') {
                    statusBadge = '<span class="badge-design badge-approved">已通过</span>';
                } else if (award.awardStatus === 'REJECTED') {
                    statusBadge = '<span class="badge-design badge-rejected">已拒绝</span>';
                }

                let actionButtons = '';
                if (award.awardStatus === 'PENDING') {
                    actionButtons = '<a href="' + contextPath + '/award?action=edit&id=' + award.id + '" class="btn-action btn-action-edit"><i class="bi bi-pencil"></i>编辑</a> <a href="' + contextPath + '/award?action=delete&id=' + award.id + '" class="btn-action btn-action-delete" onclick="return confirm(\'确定要删除这个奖项吗？\')"><i class="bi bi-trash"></i>删除</a>';
                }

                html += '<tr>' +
                    '<td>' + award.competition + '</td>' +
                    '<td>' + (award.competitionLevelName || '') + '</td>' +
                    '<td>' + (award.awardLevelName || '') + '</td>' +
                    '<td>' + (award.competitionLocation || '') + '</td>' +
                    '<td>' + (award.competitionTime || '') + '</td>' +
                    '<td>' + statusBadge + '</td>' +
                    '<td><a href="' + contextPath + '/award?action=detail&id=' + award.id + '" class="btn-action btn-action-detail"><i class="bi bi-eye"></i>详情</a> ' + actionButtons + '</td>' +
                '</tr>';
            });

            awardTableBody.innerHTML = html;
        }
    });
</script>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />