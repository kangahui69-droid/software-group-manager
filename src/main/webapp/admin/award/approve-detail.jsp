<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项详情审核" />
</jsp:include>

<%
String competitionLevelName = "";
String awardLevelName = "";
Object competitionLevelsObj = request.getAttribute("competitionLevels");
Object awardLevelsObj = request.getAttribute("awardLevels");
if (competitionLevelsObj != null) {
    for (Object obj : (java.util.List)competitionLevelsObj) {
        model.Dictionary dict = (model.Dictionary)obj;
        Object awardObj = request.getAttribute("award");
        if (awardObj != null) {
            try {
                java.lang.reflect.Method m = awardObj.getClass().getMethod("getCompetitionLevel");
                Object val = m.invoke(awardObj);
                if (val != null && val.toString().equals(dict.getId().toString())) {
                    competitionLevelName = dict.getName();
                    break;
                }
            } catch (Exception e) {}
        }
    }
}
if (awardLevelsObj != null) {
    for (Object obj : (java.util.List)awardLevelsObj) {
        model.Dictionary dict = (model.Dictionary)obj;
        Object awardObj = request.getAttribute("award");
        if (awardObj != null) {
            try {
                java.lang.reflect.Method m = awardObj.getClass().getMethod("getAwardLevel");
                Object val = m.invoke(awardObj);
                if (val != null && val.toString().equals(dict.getId().toString())) {
                    awardLevelName = dict.getName();
                    break;
                }
            } catch (Exception e) {}
        }
    }
}
pageContext.setAttribute("competitionLevelName", competitionLevelName);
pageContext.setAttribute("awardLevelName", awardLevelName);
%>

<style>
:root {
    --brand-blue: #1456f0;
    --font-display: 'Outfit', sans-serif;
    --font-ui: 'DM Sans', sans-serif;
    --radius-generous: 16px;
    --radius-standard: 12px;
    --radius-comfortable: 10px;
    --radius-pill: 9999px;
    --shadow-brand-purple: 0 4px 20px rgba(20, 85, 240, 0.15);
    --shadow-brand-offset: 0 8px 32px rgba(20, 85, 240, 0.12);
    --shadow-standard: 0 2px 8px rgba(0, 0, 0, 0.06);
    --bg-white: #ffffff;
    --text-dark: #1a1a2e;
    --text-muted: #6b7280;
    --border-gray: #e5e7eb;
    --border-light: #f3f4f6;
    --primary-light: #eff6ff;
}
body {
    font-family: var(--font-ui);
    background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
}
.card {
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-standard);
    box-shadow: var(--shadow-standard);
}
.card-header {
    background: transparent;
    border-bottom: 1px solid var(--border-light);
    padding: 20px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.card-body {
    padding: 24px;
}
.card-title {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0;
}
.card-img {
    border-radius: var(--radius-standard);
    overflow: hidden;
}
.card-img img {
    width: 100%;
    height: auto;
    object-fit: cover;
}
.alert-primary {
    background: var(--primary-light);
    border: 1px solid rgba(20, 85, 240, 0.2);
    border-radius: var(--radius-standard);
    padding: 16px 20px;
}
.alert-success {
    background: #ecfdf5;
    border: 1px solid #d1fae5;
    border-radius: var(--radius-standard);
    color: #065f46;
    padding: 12px 16px;
}
.alert-danger {
    background: #fef2f2;
    border: 1px solid #fee2e2;
    border-radius: var(--radius-standard);
    color: #991b1b;
    padding: 12px 16px;
}
.alert-info {
    background: var(--primary-light);
    border: 1px solid rgba(20, 85, 240, 0.2);
    border-radius: var(--radius-standard);
    color: var(--brand-blue);
    padding: 12px 16px;
}
.form-label {
    font-weight: 500;
    color: var(--text-dark);
    margin-bottom: 8px;
    font-size: 14px;
}
.input-group {
    border-radius: var(--radius-comfortable);
    overflow: hidden;
}
.input-group-text {
    background: var(--primary-light);
    border: 1px solid var(--border-gray);
    border-right: none;
    padding: 10px 14px;
}
.input-group-text svg {
    color: var(--brand-blue);
}
.form-control {
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 10px 14px;
    font-size: 14px;
    background: #f9fafb;
}
.form-control[readonly] {
    background: var(--primary-light);
    color: var(--text-dark);
}
.btn-outline-secondary {
    background: var(--bg-white);
    border: 1px solid var(--border-gray);
    border-radius: var(--radius-comfortable);
    padding: 10px 20px;
    font-weight: 500;
    font-size: 14px;
    color: var(--text-muted);
    cursor: pointer;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    text-decoration: none;
}
.btn-outline-secondary:hover {
    border-color: var(--brand-blue);
    color: var(--brand-blue);
}
.btn-success {
    background: #10b981;
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 8px 16px;
    font-weight: 500;
    font-size: 13px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-success:hover {
    background: #059669;
}
.btn-danger {
    background: #ef4444;
    border: none;
    border-radius: var(--radius-comfortable);
    padding: 8px 16px;
    font-weight: 500;
    font-size: 13px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-danger:hover {
    background: #dc2626;
}
.btn-link {
    color: var(--brand-blue);
    text-decoration: none;
    font-weight: 500;
    font-size: 13px;
}
.btn-link:hover {
    text-decoration: underline;
}
.badge {
    font-weight: 500;
    font-size: 12px;
    padding: 4px 10px;
    border-radius: var(--radius-pill);
}
.bg-primary {
    background: var(--primary-light);
    color: var(--brand-blue);
}
h4 {
    font-family: var(--font-display);
    font-size: 16px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0 0 16px;
}
.mb-6 {
    margin-bottom: 32px;
}
</style>

<div class="container-xl">
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">奖项详情审核</h3>
                    <a href="${pageContext.request.contextPath}/award?action=approveList" class="btn btn-outline-secondary">
                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                        返回列表
                    </a>
                </div>
                <div class="card-body">
                    <div class="alert alert-primary mb-4" role="alert">
                        <div class="d-flex align-items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-3" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 2l3.09 6.26L22 9.27l-5 4.87l1.18 6.88L12 17.77l-6.18 3.25L7 14.14l-5-4.87l6.91-1.01L12 2z"></path></svg>
                            <div>
                                <h4 class="alert-title mb-1">奖项等级</h4>
                                <div class="text-secondary">
                                    <c:choose>
                                        <c:when test="${not empty competitionLevelName}">${competitionLevelName}</c:when>
                                    </c:choose>
                                    <c:choose>
                                        <c:when test="${not empty awardLevelName}">${awardLevelName}</c:when>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="mb-6">
                        <h4>比赛信息</h4>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">比赛名称</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="14.31" y1="8" x2="20.05" y2="17.94"></line><line x1="9.69" y1="8" x2="21.17" y2="8"></line><line x1="7.38" y1="12" x2="13.12" y2="2.06"></line><line x1="9.69" y1="16" x2="3.95" y2="6.06"></line><line x1="14.31" y1="16" x2="2.83" y2="16"></line><line x1="16.62" y1="12" x2="10.88" y2="21.94"></line></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${award.competition}" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">比赛等级</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 2l3.09 6.26L22 9.27l-5 4.87l1.18 6.88L12 17.77l-6.18 3.25L7 14.14l-5-4.87l6.91-1.01L12 2z"></path></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${competitionLevelName}" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">比赛时间</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                        </span>
                                        <input type="text" class="form-control" value="<fmt:formatDate value='${award.competitionTime}' pattern='yyyy-MM-dd' />" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">比赛地点</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${award.competitionLocation}" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">比赛界别</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="12" y1="2" x2="12" y2="22"></line><line x1="2" y1="12" x2="22" y2="12"></line><circle cx="12" cy="12" r="10"></circle></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${award.competitionSession}" readonly>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="mb-6">
                        <h4>奖项信息</h4>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">奖项名称</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${award.name}" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">奖项等级</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 2l3.09 6.26L22 9.27l-5 4.87l1.18 6.88L12 17.77l-6.18 3.25L7 14.14l-5-4.87l6.91-1.01L12 2z"></path></svg>
                                        </span>
                                        <input type="text" class="form-control" value="${awardLevelName}" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">奖项类型</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                                        </span>
                                        <input type="text" class="form-control" value="<c:forEach var='dict' items='${awardTypes}'><c:if test='${award.awardType eq dict.id}'>${dict.name}</c:if></c:forEach>" readonly>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">奖项类别</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>
                                        </span>
                                        <input type="text" class="form-control" value="<c:forEach var='dict' items='${awardCategories}'><c:if test='${award.awardCategory eq dict.id}'>${dict.name}</c:if></c:forEach>" readonly>
                                    </div>
                                </div>
                                <c:forEach var="dict" items="${awardTypes}">
                                    <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                        <div class="mb-3">
                                            <label class="form-label">团队名称</label>
                                            <div class="input-group">
                                                <span class="input-group-text">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                                                </span>
                                                <input type="text" class="form-control" value="${award.teamName}" readonly>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                                <div class="mb-3">
                                    <label class="form-label">审核状态</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <c:choose>
                                                <c:when test="${award.awardStatus eq 'PENDING'}">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                                </c:when>
                                                <c:when test="${award.awardStatus eq 'APPROVED'}">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M5 12l5 5l10 -10"></path></svg>
                                                </c:when>
                                                <c:otherwise>
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <input type="text" class="form-control" value="<c:choose><c:when test='${award.awardStatus eq \"PENDING\"}'>待审核</c:when><c:when test='${award.awardStatus eq \"APPROVED\"}'>已通过</c:when><c:otherwise>已打回</c:otherwise></c:choose>" readonly>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">提交时间</label>
                                    <div class="input-group">
                                        <span class="input-group-text">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                        </span>
                                        <input type="text" class="form-control" value="<fmt:formatDate value='${award.createdAt}' pattern='yyyy-MM-dd HH:mm:ss' />" readonly>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="mb-6">
                        <h4>奖项图片</h4>
                        <div class="row row-cards">
                            <c:forEach var="image" items="${awardImages}">
                                <% long timestamp = System.currentTimeMillis(); %>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-img">
                                            <div style="height: 200px; overflow: hidden;">
                                                <img src="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" alt="图片" class="w-100 h-auto">
                                            </div>
                                        </div>
                                        <div class="card-body">
                                            <p class="card-text">
                                                <c:if test="${image.isMain}">
                                                    <span class="badge bg-primary">主图</span>
                                                </c:if>
                                            </p>
                                            <a href="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" target="_blank" class="btn btn-link">查看原图</a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty awardImages}">
                                <div class="col-md-12">
                                    <div class="alert alert-info">暂无图片</div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                    
                    <div class="mb-6">
                        <h4>审核操作</h4>
                        <div class="row">
                            <div class="col-md-12">
                                <c:if test="${award.awardStatus eq 'PENDING'}">
                                    <div class="btn-list">
                                        <form action="${pageContext.request.contextPath}/award?action=approve" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="${award.id}">
                                            <button type="submit" class="btn btn-success">通过</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/award?action=reject" method="post" style="display: inline;">
                                            <input type="hidden" name="id" value="${award.id}">
                                            <button type="submit" class="btn btn-danger">打回</button>
                                        </form>
                                    </div>
                                </c:if>
                                <c:if test="${award.awardStatus eq 'APPROVED'}">
                                    <div class="alert alert-success">此奖项已通过审核</div>
                                </c:if>
                                <c:if test="${award.awardStatus eq 'REJECTED'}">
                                    <div class="alert alert-danger">此奖项已被打回</div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />