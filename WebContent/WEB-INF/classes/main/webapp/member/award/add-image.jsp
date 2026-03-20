<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
        // 重定向到正确的入口
        response.sendRedirect(request.getContextPath() + "/award?action=addImage&id=" + awardId);
        return;
    }
%>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="补充奖项图片" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">补充奖项图片</h3>
                    <div class="card-actions">
                        <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}" class="btn btn-sm btn-link">返回详情</a>
                    </div>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/award?action=addImage" method="post" enctype="multipart/form-data">
                        <input type="hidden" name="id" value="${award.id}">
                        
                        <div class="mb-6">
                            <h4>奖项基本信息</h4>
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label">奖项名称</label>
                                        <div class="form-control-plaintext">${award.title}</div>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">比赛名称</label>
                                        <div class="form-control-plaintext">${award.competition}</div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label">奖项类型</label>
                                        <div class="form-control-plaintext">
                                            <c:choose>
                                                <c:when test="${award.awardType eq 'TYPE_INDIVIDUAL'}">个人</c:when>
                                                <c:otherwise>团队</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">审核状态</label>
                                        <div class="form-control-plaintext">
                                            <span class="badge bg-green">已审核</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-6">
                            <h4>上传补充图片</h4>
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="mb-3">
                                        <label class="form-label required">奖项图片</label>
                                        <input type="file" class="form-control" name="awardImages" multiple accept="image/*" required>
                                        <small class="form-hint">可上传多张图片，支持JPG、PNG等格式</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="card-footer">
                            <button type="submit" class="btn btn-primary">上传图片</button>
                            <a href="${pageContext.request.contextPath}/member/award/detail.jsp?id=${award.id}" class="btn btn-link">取消</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />