<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项列表" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">奖项列表管理</h3>
                    <div class="card-actions">
                        <div class="btn-list">
                            <a href="${pageContext.request.contextPath}/award?action=approveList&status=ALL" class="btn btn-sm btn-link ${param.status == 'ALL' || empty param.status ? 'active' : ''}">全部</a>
                            <a href="${pageContext.request.contextPath}/award?action=approveList&status=PENDING" class="btn btn-sm btn-link ${param.status == 'PENDING' ? 'active' : ''}">待审核</a>
                            <a href="${pageContext.request.contextPath}/award?action=approveList&status=APPROVED" class="btn btn-sm btn-link ${param.status == 'APPROVED' ? 'active' : ''}">已通过</a>
                            <a href="${pageContext.request.contextPath}/award?action=approveList&status=REJECTED" class="btn btn-sm btn-link ${param.status == 'REJECTED' ? 'active' : ''}">已打回</a>
                        </div>
                    </div>
                </div>
                <div class="card-body">
                    <!-- 搜索条件 -->
                    <div class="mb-4">
                        <form method="get" action="${pageContext.request.contextPath}/award" class="row g-3">
                            <input type="hidden" name="action" value="approveList">
                            <div class="col-md-3">
                                <label class="form-label">关键词</label>
                                <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="搜索奖项名称/比赛名称">
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">奖项类型</label>
                                <select class="form-select" name="awardType">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${awardTypes}">
                                        <option value="${dict.id}" <c:if test="${param.awardType == dict.id}">selected</c:if>>${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">奖项类别</label>
                                <select class="form-select" name="awardCategory">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${awardCategories}">
                                        <option value="${dict.id}" <c:if test="${param.awardCategory == dict.id}">selected</c:if>>${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">奖项等级</label>
                                <select class="form-select" name="awardLevel">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${awardLevels}">
                                        <option value="${dict.id}" <c:if test="${param.awardLevel == dict.id}">selected</c:if>>${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">比赛等级</label>
                                <select class="form-select" name="competitionLevel">
                                    <option value="">全部</option>
                                    <c:forEach var="dict" items="${competitionLevels}">
                                        <option value="${dict.id}" <c:if test="${param.competitionLevel == dict.id}">selected</c:if>>${dict.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary w-100">搜索</button>
                            </div>
                            <div class="col-md-1 d-flex align-items-end">
                                <a href="${pageContext.request.contextPath}/award?action=approveList&status=ALL" class="btn btn-outline-secondary w-100">重置</a>
                            </div>
                        </form>
                    </div>
                    
                    <!-- 奖项列表 -->
                    <div class="table-responsive">
                        <table class="table table-vcenter card-table">
                            <thead>
                                <tr>
                                    <th>序号</th>
                                    <th>奖项名称</th>
                                    <th>比赛信息</th>
                                    <th>奖项类型</th>
                                    <th>提交时间</th>
                                    <th>状态</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="award" items="${awards}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1}</td>
                                        <td>${award.name}</td>
                                        <td>
                                            <div>${award.competition}</div>
                                            <div class="text-muted small">
                                                <fmt:formatDate value="${award.competitionTime}" pattern="yyyy-MM-dd" />
                                                <c:if test="${not empty award.competitionSession}">，${award.competitionSession}</c:if>
                                            </div>
                                        </td>
                                        <td>
                                            <div>
                                                <c:forEach var="dict" items="${awardTypes}">
                                                    <c:if test="${award.awardType eq dict.id}">
                                                        ${dict.name}
                                                    </c:if>
                                                </c:forEach>
                                            </div>
                                            <div class="text-muted small">
                                                <c:forEach var="dict" items="${awardCategories}">
                                                    <c:if test="${award.awardCategory eq dict.id}">
                                                        ${dict.name}
                                                    </c:if>
                                                </c:forEach>
                                            </div>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${award.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${award.awardStatus eq 'PENDING'}">
                                                    <span class="badge bg-yellow">待审核</span>
                                                </c:when>
                                                <c:when test="${award.awardStatus eq 'APPROVED'}">
                                                    <span class="badge bg-green">已通过</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-red">已打回</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-list">
                                                <a href="${pageContext.request.contextPath}/award?action=approveDetail&id=${award.id}" class="btn btn-sm btn-link">查看详情</a>
                                                <c:if test="${award.awardStatus eq 'PENDING'}">
                                                    <form action="${pageContext.request.contextPath}/award?action=approve" method="post" style="display: inline;">
                                                        <input type="hidden" name="id" value="${award.id}">
                                                        <button type="submit" class="btn btn-sm btn-success">通过</button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/award?action=reject" method="post" style="display: inline;">
                                                        <input type="hidden" name="id" value="${award.id}">
                                                        <button type="submit" class="btn btn-sm btn-danger">打回</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty awards}">
                                    <tr>
                                        <td colspan="7" class="text-center">
                                            <div class="empty-state">
                                                <div class="empty-state-icon">
                                                    <i class="bi bi-inbox"></i>
                                                </div>
                                                <h3>暂无数据</h3>
                                                <p>没有找到符合条件的奖项记录</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />