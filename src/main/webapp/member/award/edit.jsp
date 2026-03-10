<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%-- 添加数据检查，确保只有通过正确的入口访问才能显示页面 --%>
<% 
    if (request.getAttribute("award") == null) {
        // 如果没有award对象，说明是直接访问JSP页面，重定向到正确的入口
        Integer awardId = null;
        try {
            awardId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            // 如果id参数无效，重定向到列表页面
            response.sendRedirect(request.getContextPath() + "/member/award/list.jsp?error=invalid_id");
            return;
        }
        // 重定向到正确的编辑入口
        response.sendRedirect(request.getContextPath() + "/award?action=edit&id=" + awardId);
        return;
    }
%>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="编辑奖项" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">编辑奖项信息</h3>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/award?action=update" method="post" enctype="multipart/form-data">
                        <input type="hidden" name="id" value="${award.id}">
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">比赛名称</label>
                                    <input type="text" class="form-control" name="competition" value="${award.competition}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">比赛时间</label>
                                    <input type="date" class="form-control" name="competitionTime" 
                                           value="<fmt:formatDate value='${award.competitionTime}' pattern='yyyy-MM-dd' />" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">比赛地点</label>
                                    <input type="text" class="form-control" name="competitionLocation" value="${award.competitionLocation}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">比赛界别</label>
                                    <input type="text" class="form-control" name="competitionSession" 
                                           value="${award.competitionSession}" placeholder="如：第12届">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">比赛等级</label>
                                    <select class="form-control" name="competitionLevel" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${competitionLevels}">
                                            <option value="${dict.id}" <c:if test="${award.competitionLevel eq dict.id}">selected</c:if>>${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项类型</label>
                                    <select class="form-control" name="awardType" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardTypes}">
                                            <option value="${dict.id}" <c:if test="${award.awardType eq dict.id}">selected</c:if>>${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项类别</label>
                                    <select class="form-control" name="awardCategory" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardCategories}">
                                            <option value="${dict.id}" <c:if test="${award.awardCategory eq dict.id}">selected</c:if>>${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-12">
                                <div class="mb-3">
                                    <label class="form-label">团队名称</label>
                                    <input type="text" class="form-control" name="teamName" 
                                           value="${award.teamName}" placeholder="团队奖项请填写团队名称">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项等级</label>
                                    <select class="form-control" name="awardLevel" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardLevels}">
                                            <option value="${dict.id}" <c:if test="${award.awardLevel eq dict.id}">selected</c:if>>${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">补充奖项图片</label>
                                    <input type="file" class="form-control" name="awardImages" multiple accept="image/*">
                                    <small class="form-hint">可上传多张图片，支持JPG、PNG等格式</small>
                                </div>
                            </div>
                        </div>
                        
                        <div class="card-footer">
                            <button type="submit" class="btn btn-primary">保存修改</button>
                            <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="btn btn-link">取消</a>
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
                    <div class="list-group-item">
                        <div class="row align-items-center">
                            <div class="col-auto">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-save" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 10l-6 6m0 -6l6 6m6 -6l-6 6" /><path d="M12 3a9 9 0 0 1 0 18a9 9 0 0 1 0 -18" /></svg>
                            </div>
                            <div class="col text-truncate ms-2">
                                <button type="button" onclick="document.querySelector('form').submit();" class="text-reset d-block btn btn-link p-0">保存修改</button>
                                <div class="d-block text-muted text-truncate mt-n1">保存当前编辑的奖项信息</div>
                            </div>
                        </div>
                    </div>
                    <div class="list-group-item">
                        <div class="row align-items-center">
                            <div class="col-auto">
                                <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-tabler icon-tabler-arrow-left" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><line x1="5" y1="12" x2="19" y2="12" /><line x1="5" y1="12" x2="11" y2="18" /><line x1="5" y1="12" x2="11" y2="6" /></svg>
                            </div>
                            <div class="col text-truncate ms-2">
                                <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="text-reset d-block">返回当前奖项页</a>
                                <div class="d-block text-muted text-truncate mt-n1">查看当前奖项的详细信息</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />