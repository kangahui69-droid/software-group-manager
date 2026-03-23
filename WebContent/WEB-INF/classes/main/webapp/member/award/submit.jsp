<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="提交奖项" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">提交奖项</h2>
            </div>
            <div class="col-auto ms-auto">
                <a href="${pageContext.request.contextPath}/award?action=list" class="btn btn-outline-secondary">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="12" x2="11" y2="18"></line><line x1="5" y1="12" x2="11" y2="6"></line></svg>
                    返回列表
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <div class="row">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">奖项信息</h3>
                    </div>
                    <div class="card-body">
                        <%-- 错误提示 --%>
                        <% if (request.getAttribute("error") != null) { %>
                            <div class="alert alert-danger alert-dismissible" role="alert">
                                <%= request.getAttribute("error") %>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        <% } %>

                        <form id="awardForm" action="${pageContext.request.contextPath}/award?action=submit" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="_csrf" value="<%= session.getAttribute("csrfToken") != null ? session.getAttribute("csrfToken") : "" %>">

                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">比赛名称</label>
                                        <input type="text" class="form-control" name="competition" id="competition" placeholder="请输入比赛名称" required>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label required">比赛时间</label>
                                        <input type="date" class="form-control" name="competitionTime" id="competitionTime" required>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">比赛地点</label>
                                        <input type="text" class="form-control" name="competitionLocation" placeholder="请输入比赛地点">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label">比赛界别</label>
                                        <input type="text" class="form-control" name="competitionSession" placeholder="如：第12届">
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label required">比赛等级</label>
                                        <select class="form-select" name="competitionLevel" id="competitionLevel" required>
                                            <option value="">请选择比赛等级</option>
                                            <c:forEach var="dict" items="${competitionLevels}">
                                                <option value="${dict.id}">${dict.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label required">奖项类型</label>
                                        <select class="form-select" name="awardType" id="awardType" required>
                                            <option value="">请选择奖项类型</option>
                                            <c:forEach var="dict" items="${awardTypes}">
                                                <option value="${dict.id}">${dict.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-6">
                                    <div class="mb-3">
                                        <label class="form-label required">奖项类别</label>
                                        <select class="form-select" name="awardCategory" id="awardCategory" required>
                                            <option value="">请选择奖项类别</option>
                                            <c:forEach var="dict" items="${awardCategories}">
                                                <option value="${dict.id}">${dict.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label required">奖项等级</label>
                                        <select class="form-select" name="awardLevel" id="awardLevel" required>
                                            <option value="">请选择奖项等级</option>
                                            <c:forEach var="dict" items="${awardLevels}">
                                                <option value="${dict.id}">${dict.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-3" id="teamNameDiv" style="display: none;">
                                        <label class="form-label">团队名称</label>
                                        <input type="text" class="form-control" name="teamName" id="teamName" placeholder="团队奖项请填写团队名称">
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label required">奖项证书图片</label>
                                        <input type="file" class="form-control" name="awardImages" id="awardImages" multiple accept="image/*" required>
                                        <small class="form-hint">可上传多张图片，支持JPG、PNG等格式</small>
                                    </div>
                                </div>
                            </div>

                            <div class="card-footer">
                                <button type="submit" class="btn btn-primary">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 2L11 13"></path><path d="M22 2l-7 20-4-9-9-4 20-7z"></path></svg>
                                    提交审核
                                </button>
                                <a href="${pageContext.request.contextPath}/award?action=list" class="btn btn-link">取消</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// 监听奖项类型变化，动态显示或隐藏团队名称框
document.addEventListener('DOMContentLoaded', function() {
    var awardTypeSelect = document.getElementById('awardType');
    var teamNameDiv = document.getElementById('teamNameDiv');
    var teamNameInput = document.getElementById('teamName');

    // 初始检查
    updateTeamNameVisibility();

    // 添加事件监听器
    awardTypeSelect.addEventListener('change', updateTeamNameVisibility);

    function updateTeamNameVisibility() {
        var selectedText = awardTypeSelect.options[awardTypeSelect.selectedIndex].text;
        if (selectedText.includes('个人')) {
            teamNameDiv.style.display = 'none';
            teamNameInput.value = '';
        } else if (selectedText.includes('团队')) {
            teamNameDiv.style.display = 'block';
        } else {
            teamNameDiv.style.display = 'block';
        }
    }

    // 表单提交前验证
    document.getElementById('awardForm').addEventListener('submit', function(e) {
        var competition = document.getElementById('competition').value.trim();
        var competitionTime = document.getElementById('competitionTime').value;
        var competitionLevel = document.getElementById('competitionLevel').value;
        var awardType = document.getElementById('awardType').value;
        var awardCategory = document.getElementById('awardCategory').value;
        var awardLevel = document.getElementById('awardLevel').value;
        var awardImages = document.getElementById('awardImages').files;

        // 检查必填项
        if (!competition) {
            alert('请输入比赛名称');
            e.preventDefault();
            return false;
        }

        if (!competitionTime) {
            alert('请选择比赛时间');
            e.preventDefault();
            return false;
        }

        if (!competitionLevel) {
            alert('请选择比赛等级');
            e.preventDefault();
            return false;
        }

        if (!awardType) {
            alert('请选择奖项类型');
            e.preventDefault();
            return false;
        }

        if (!awardCategory) {
            alert('请选择奖项类别');
            e.preventDefault();
            return false;
        }

        if (!awardLevel) {
            alert('请选择奖项等级');
            e.preventDefault();
            return false;
        }

        if (awardImages.length === 0) {
            alert('请上传奖项证书图片');
            e.preventDefault();
            return false;
        }

        // 检查图片大小（单个文件不超过5MB）
        for (var i = 0; i < awardImages.length; i++) {
            if (awardImages[i].size > 5 * 1024 * 1024) {
                alert('图片文件大小不能超过5MB');
                e.preventDefault();
                return false;
            }
        }

        return true;
    });
});
</script>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />