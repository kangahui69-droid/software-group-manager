<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="奖项详情" />
</jsp:include>

<div class="page-wrapper">
    <div class="container-xl">
        <div class="row row-cards">
            <div class="col-md-8">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-trophy" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z" /><path d="M12 17v-4" /><path d="M7 13h10" /><path d="M8 21h8" /><path d="M12 17l-3 3" /><path d="M12 17l3 3" /><path d="M6 9a6 6 0 0 1 12 0" /></svg>
                            奖项详情
                        </h3>
                        <div class="card-actions">
                            <c:if test="${award.awardStatus eq 'PENDING'}">
                                <a href="${pageContext.request.contextPath}/award?action=edit&id=${award.id}" class="btn btn-primary">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-edit" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M4 20h4l10.5 -10.5a1.5 1.5 0 0 0 4 -4l-4.5 -4.5a1.5 1.5 0 0 0 -4 4l-10.5 10.5v4" /><line x1="13.5" y1="6.5" x2="17.5" y2="10.5" /></svg>
                                    编辑
                                </a>
                            </c:if>
                            <c:if test="${award.awardStatus eq 'APPROVED'}">
                                <a href="${pageContext.request.contextPath}/award?action=addImage&id=${award.id}" class="btn btn-success">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-photo-plus" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><circle cx="12" cy="12" r="9" /><line x1="9" y1="12" x2="15" y2="12" /><line x1="12" y1="9" x2="12" y2="15" /></svg>
                                    补充图片
                                </a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/award?action=list" class="btn">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-arrow-left" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><line x1="5" y1="12" x2="19" y2="12" /><line x1="5" y1="12" x2="11" y2="18" /><line x1="5" y1="12" x2="11" y2="6" /></svg>
                                返回列表
                            </a>
                        </div>
                    </div>
                    
                    <div class="card-body">
                        <form class="form-horizontal" action="#">
                            <div class="card mb-3">
                                <div class="card-header">
                                    <h3 class="card-title">比赛信息</h3>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">比赛名称</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z"></path><path d="M12 17v-4"></path><path d="M7 13h10"></path><path d="M8 21h8"></path><path d="M12 17l-3 3"></path><path d="M12 17l3 3"></path><path d="M6 9a6 6 0 0 1 12 0"></path></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="${award.competition}">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">比赛时间</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><rect x="4" y="5" width="16" height="16" rx="2"></rect><line x1="16" y1="3" x2="16" y2="7"></line><line x1="8" y1="3" x2="8" y2="7"></line><line x1="4" y1="11" x2="20" y2="11"></line><line x1="11" y1="15" x2="12" y2="15"></line><line x1="12" y1="15" x2="12" y2="18"></line></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<fmt:formatDate value="${award.competitionTime}" pattern="yyyy-MM-dd" />">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">比赛地点</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s-8-4.5-8-11.8a8 8 0 0 1 16 0c0 7.3-8 11.8-8 11.8z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="${award.competitionLocation}">
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">比赛界别</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="${award.competitionSession}">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">比赛类型</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<c:forEach var='dict' items='${awardTypes}'><c:if test='${award.awardType eq dict.id}'>${dict.name}</c:if></c:forEach>">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">比赛等级</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 2l3 7h7l-5.5 4 2 7L12 16l-6.5 4 2-7L1.5 9h7z"></path></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<c:forEach var='dict' items='${competitionLevels}'><c:if test='${award.competitionLevel eq dict.id}'>${dict.name}</c:if></c:forEach>">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="card mb-3">
                                <div class="card-header">
                                    <h3 class="card-title">奖项信息</h3>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            
                                            <div class="mb-3">
                                                <label class="form-label">奖项等级</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z"></path><path d="M12 17v-4"></path><path d="M7 13h10"></path><path d="M8 21h8"></path><path d="M12 17l-3 3"></path><path d="M12 17l3 3"></path><path d="M6 9a6 6 0 0 1 12 0"></path></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<c:forEach var='dict' items='${awardLevels}'><c:if test='${award.awardLevel eq dict.id}'>${dict.name}</c:if></c:forEach>">
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">奖项类别</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="9"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<c:forEach var='dict' items='${awardCategories}'><c:if test='${award.awardCategory eq dict.id}'>${dict.name}</c:if></c:forEach>">
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">提交时间</label>
                                                <div class="input-group">
                                                    <span class="input-group-text">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                                    </span>
                                                    <input type="text" class="form-control" disabled value="<fmt:formatDate value="${award.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" />">
                                                </div>
                                            </div>
                                            <!-- 团队奖项才显示团队名称 -->
                                            <c:forEach var="dict" items="${awardTypes}">
                                                <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                                    <div class="mb-3">
                                                        <label class="form-label">团队名称</label>
                                                        <div class="input-group">
                                                            <span class="input-group-text">
                                                                <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path><path d="M3 21h18"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path><path d="M21 21v-4a3 3 0 0 0 -3 -3H6a3 3 0 0 0 -3 3v4"></path></svg>
                                                            </span>
                                                            <input type="text" class="form-control" disabled value="${award.teamName}">
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="card mb-3">
                                <div class="card-header">
                                    <h3 class="card-title">奖项图片</h3>
                                </div>
                                <div class="card-body">
                                    <div class="row row-cards">
                                        <c:forEach var="image" items="${awardImages}">
                                            <% long timestamp = System.currentTimeMillis(); %>
                                            <div class="col-md-4">
                                                <div class="card">
                                                    <div class="card-img-top">
                                                        <div style="height: 200px; overflow: hidden;">
                                                            <img src="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" alt="奖项图片" class="img-fluid">
                                                        </div>
                                                    </div>
                                                    <div class="card-body">
                                                        <div class="d-flex justify-content-between align-items-center">
                                                            <div>
                                                                <c:if test="${image.isMain}">
                                                                    <span class="badge bg-primary">主图</span>
                                                                </c:if>
                                                            </div>
                                                            <a href="${pageContext.request.contextPath}/file?action=view&id=${image.fileStorageId}&t=<%=timestamp%>" target="_blank" class="btn btn-sm btn-outline-primary">
                                                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-external-link" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 6h-6a2 2 0 0 0 -2 2v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-4" /><polyline points="11 13 15 13 15 17 19 17 19 13" /><line x1="15" y1="17" x2="18" y2="14" /></svg>
                                                                查看原图
                                                            </a>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                        <c:if test="${empty awardImages}">
                                            <div class="col-md-12">
                                                <div class="alert alert-info">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-info-circle" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><circle cx="12" cy="12" r="9" /><line x1="12" y1="8" x2="12.01" y2="8" /><polyline points="11 12 12 12 12 16 13 16" /></svg>
                                                    暂无图片
                                                </div>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">快捷操作</h3>
                    </div>
                    <div class="list-group list-group-flush list-group-hoverable">
                        <div class="list-group-item">
                            <div class="row align-items-center">
                                <div class="col-auto">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-trophy" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z" /><path d="M12 17v-4" /><path d="M7 13h10" /><path d="M8 21h8" /><path d="M12 17l-3 3" /><path d="M12 17l3 3" /><path d="M6 9a6 6 0 0 1 12 0" /></svg>
                                </div>
                                <div class="col text-truncate ms-2">
                                    <a href="${pageContext.request.contextPath}/award?action=list" class="text-reset d-block">查看所有奖项</a>
                                    <div class="d-block text-muted text-truncate mt-n1">浏览您提交的所有奖项</div>
                                </div>
                            </div>
                        </div>
                        <c:if test="${award.awardStatus eq 'REJECTED'}">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-trash" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M4 7l16 0" /><path d="M10 11l0 6" /><path d="M14 11l0 6" /><path d="M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12" /><path d="M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3" /></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <a href="${pageContext.request.contextPath}/award?action=delete&id=${award.id}" class="text-danger d-block" onclick="return confirm('确定要删除这个已拒绝的奖项吗？删除后将无法恢复，且会删除相关的文件和关联项。');">删除奖项</a>
                                        <div class="d-block text-muted text-truncate mt-n1">仅已拒绝的奖项可删除</div>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${award.awardStatus eq 'PENDING'}">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-edit" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M4 20h4l10.5 -10.5a1.5 1.5 0 0 0 4 -4l-4.5 -4.5a1.5 1.5 0 0 0 -4 4l-10.5 10.5v4" /><line x1="13.5" y1="6.5" x2="17.5" y2="10.5" /></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <a href="${pageContext.request.contextPath}/award?action=edit&id=${award.id}" class="text-reset d-block">编辑奖项</a>
                                        <div class="d-block text-muted text-truncate mt-n1">修改奖项信息</div>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${award.awardStatus eq 'APPROVED'}">
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-auto">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-photo-plus" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><circle cx="12" cy="12" r="9" /><line x1="9" y1="12" x2="15" y2="12" /><line x1="12" y1="9" x2="12" y2="15" /></svg>
                                    </div>
                                    <div class="col text-truncate ms-2">
                                        <a href="${pageContext.request.contextPath}/award?action=addImage&id=${award.id}" class="text-reset d-block">补充图片</a>
                                        <div class="d-block text-muted text-truncate mt-n1">为奖项添加更多图片</div>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">审核状态</h3>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${award.awardStatus eq 'PENDING'}">
                                <div class="alert alert-warning alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><circle cx="12" cy="12" r="9" /><polyline points="12 7 12 12 15 15" /></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">待审核</h4>
                                            <div class="text-muted">您的奖项正在等待审核，请耐心等待</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${award.awardStatus eq 'APPROVED'}">
                                <div class="alert alert-success alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l5 5l10 -10" /></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">已审核</h4>
                                            <div class="text-muted">恭喜！您的奖项已通过审核</div>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-danger alert-dismissible" role="alert">
                                    <div class="d-flex">
                                        <div>
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
                                        </div>
                                        <div>
                                            <h4 class="alert-title">已拒绝</h4>
                                            <div class="text-muted">很遗憾，您的奖项未通过审核</div>
                                        </div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                
                <div class="card mt-3">
                    <div class="card-header">
                        <h3 class="card-title">奖项统计</h3>
                    </div>
                    <div class="list-group list-group-flush list-group-hoverable">
                        <div class="list-group-item">
                            <div class="row align-items-center">
                                <div class="col-auto">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-image text-primary" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><rect x="4" y="4" width="16" height="16" rx="2" /><circle cx="9" cy="9" r="2" /><path d="M15 8l-5.5 5.5l-2.5 -2.5" /></svg>
                                </div>
                                <div class="col text-truncate ms-2">
                                    <span class="text-primary d-block fs-4">${not empty awardImages ? awardImages.size() : 0} 张图片</span>
                                    <div class="d-block text-muted text-truncate mt-n1">奖项相关图片数量</div>
                                </div>
                            </div>
                        </div>
                        <div class="list-group-item">
                            <div class="row align-items-center">
                                <div class="col-auto">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-trophy text-warning" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z" /><path d="M12 17v-4" /><path d="M7 13h10" /><path d="M8 21h8" /><path d="M12 17l-3 3" /><path d="M12 17l3 3" /><path d="M6 9a6 6 0 0 1 12 0" /></svg>
                                </div>
                                <div class="col text-truncate ms-2">
                                    <span class="d-block fs-4 ${award.awardLevel == 1 ? 'text-danger' : award.awardLevel == 2 ? 'text-warning' : award.awardLevel == 3 ? 'text-success' : 'text-info'}">
                                        <c:forEach var="dict" items="${awardLevels}">
                                            <c:if test="${award.awardLevel eq dict.id}">${dict.name}</c:if>
                                        </c:forEach>
                                    </span>
                                    <div class="d-block text-muted text-truncate mt-n1">奖项等级</div>
                                </div>
                            </div>
                        </div>
                        <!-- 团队成员信息 - 仅团队奖项显示 -->
                        <c:forEach var="dict" items="${awardTypes}">
                            <c:if test="${award.awardType eq dict.id and dict.code eq 'TYPE_TEAM'}">
                                <div class="list-group-item">
                                    <div class="row align-items-center">
                                        <div class="col-auto">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-users text-info" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21h18" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /><path d="M21 21v-4a3 3 0 0 0 -3 -3H6a3 3 0 0 0 -3 3v4" /></svg>
                                </div>
                                        <div class="col text-truncate ms-2">
                                            <span class="text-reset d-block">团队成员</span>
                                            <div class="d-block text-muted text-truncate mt-n1">
                                                <!-- 这里需要从数据库获取团队成员信息，暂时显示占位符 -->
                                                <c:if test="${not empty teamMembers}">
                                                    <c:forEach var="member" items="${teamMembers}">
                                                        ${member.name}<br>
                                                    </c:forEach>
                                                </c:if>
                                                <c:if test="${empty teamMembers}">
                                                    团队成员信息加载中...
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />