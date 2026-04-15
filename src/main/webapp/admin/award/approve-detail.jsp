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
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-trophy me-2"></i>奖项详情审核
            </h1>
            <p class="admin-hero-subtitle">审核成员提交的奖项申请</p>
        </div>

        <div class="card-design">
            <div class="card-header-design">
                <h3 class="card-title-design">奖项信息</h3>
                <a href="${pageContext.request.contextPath}/award?action=approveList" class="btn btn-outline-brand">
                    <i class="bi bi-arrow-left me-1"></i>返回列表
                </a>
            </div>
            <div class="card-body-design">
                <div class="alert alert-primary mb-4" role="alert" style="background: rgba(20, 86, 240, 0.1); border: 1px solid rgba(20, 86, 240, 0.2); border-radius: var(--radius-standard); padding: 16px 20px;">
                    <div class="d-flex align-items-center">
                        <i class="bi bi-star me-3" style="font-size: 1.5rem; color: var(--brand-blue);"></i>
                        <div>
                            <h4 class="alert-title mb-1" style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark);">奖项等级</h4>
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
                    <h4 style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark); margin: 0 0 16px;">比赛信息</h4>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label-design">比赛名称</label>
                                <input type="text" class="input-design" value="${award.competition}" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">比赛等级</label>
                                <input type="text" class="input-design" value="${competitionLevelName}" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">比赛时间</label>
                                <input type="text" class="input-design" value="<fmt:formatDate value='${award.competitionTime}' pattern='yyyy-MM-dd' />" readonly>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label-design">比赛地点</label>
                                <input type="text" class="input-design" value="${award.competitionLocation}" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">比赛界别</label>
                                <input type="text" class="input-design" value="${award.competitionSession}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="mb-6">
                    <h4 style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark); margin: 0 0 16px;">奖项信息</h4>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label-design">奖项名称</label>
                                <input type="text" class="input-design" value="${award.name}" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">奖项等级</label>
                                <input type="text" class="input-design" value="${awardLevelName}" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">奖项类型</label>
                                <input type="text" class="input-design" value="<c:forEach var='dict' items='${awardTypes}'><c:if test='${award.awardType eq dict.id}'>${dict.name}</c:if></c:forEach>" readonly>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label-design">奖项类别</label>
                                <input type="text" class="input-design" value="<c:forEach var='dict' items='${awardCategories}'><c:if test='${award.awardCategory eq dict.id}'>${dict.name}</c:if></c:forEach>" readonly>
                            </div>
                            <c:forEach var="dict" items="${awardTypes}">
                                <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                    <div class="mb-3">
                                        <label class="form-label-design">团队名称</label>
                                        <input type="text" class="input-design" value="${award.teamName}" readonly>
                                    </div>
                                </c:if>
                            </c:forEach>
                            <div class="mb-3">
                                <label class="form-label-design">审核状态</label>
                                <input type="text" class="input-design" value="<c:choose><c:when test='${award.awardStatus eq \"PENDING\"}'>待审核</c:when><c:when test='${award.awardStatus eq \"APPROVED\"}'>已通过</c:when><c:otherwise>已打回</c:otherwise></c:choose>" readonly>
                            </div>
                            <div class="mb-3">
                                <label class="form-label-design">提交时间</label>
                                <input type="text" class="input-design" value="<fmt:formatDate value='${award.createdAt}' pattern='yyyy-MM-dd HH:mm:ss' />" readonly>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="mb-6">
                    <h4 style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark); margin: 0 0 16px;">奖项图片</h4>
                    <div class="row row-cards">
                        <c:forEach var="image" items="${awardImages}">
                            <% long timestamp = System.currentTimeMillis(); %>
                            <div class="col-md-4">
                                <div class="card-design">
                                    <div class="card-img" style="border-radius: var(--radius-standard); overflow: hidden;">
                                        <div style="height: 200px; overflow: hidden;">
                                            <img src="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" alt="图片" class="w-100 h-auto">
                                        </div>
                                    </div>
                                    <div class="card-body-design">
                                        <p class="card-text">
                                            <c:if test="${image.isMain}">
                                                <span class="badge-design" style="background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">主图</span>
                                            </c:if>
                                        </p>
                                        <a href="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" target="_blank" class="btn btn-sm-brand">查看原图</a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty awardImages}">
                            <div class="col-md-12">
                                <div class="alert alert-info" style="background: var(--primary-light); border: 1px solid rgba(20, 86, 240, 0.2); border-radius: var(--radius-standard); color: var(--brand-blue); padding: 12px 16px;">暂无图片</div>
                            </div>
                        </c:if>
                    </div>
                </div>
                
                <div class="mb-6">
                    <h4 style="font-family: var(--font-display); font-size: 1rem; font-weight: 600; color: var(--text-dark); margin: 0 0 16px;">审核操作</h4>
                    <div class="row">
                        <div class="col-md-12">
                            <c:if test="${award.awardStatus eq 'PENDING'}">
                                <div class="btn-list">
                                    <form action="${pageContext.request.contextPath}/award?action=approve" method="post" style="display: inline;">
                                        <input type="hidden" name="id" value="${award.id}">
                                        <button type="submit" class="btn btn-success-design">通过</button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/award?action=reject" method="post" style="display: inline;">
                                        <input type="hidden" name="id" value="${award.id}">
                                        <button type="submit" class="btn btn-danger-design">打回</button>
                                    </form>
                                </div>
                            </c:if>
                            <c:if test="${award.awardStatus eq 'APPROVED'}">
                                <div class="alert alert-success" style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: var(--radius-standard); color: #059669; padding: 12px 16px;">此奖项已通过审核</div>
                            </c:if>
                            <c:if test="${award.awardStatus eq 'REJECTED'}">
                                <div class="alert alert-danger" style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: var(--radius-standard); color: #dc2626; padding: 12px 16px;">此奖项已被打回</div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />