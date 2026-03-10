<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="提交奖项" />
</jsp:include>

<div class="container-xl">
    <div class="row">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">提交奖项信息</h3>
                </div>
                <div class="card-body">
                    <% if (request.getAttribute("error") != null) { %>
                        <div class="alert alert-danger" role="alert">
                            <%= request.getAttribute("error") %>
                        </div>
                    <% } %>
                    <form action="${pageContext.request.contextPath}/award?action=submit" method="post" enctype="multipart/form-data">
                    <script>
                    
                    // 监听奖项类型变化，动态显示或隐藏团队名称框
                    document.addEventListener('DOMContentLoaded', function() {
                        var awardTypeSelect = document.querySelector('select[name="awardType"]');
                        var teamNameInput = document.querySelector('input[name="teamName"]');
                        var teamNameDiv = teamNameInput ? teamNameInput.closest('div.mb-3') : null;
                        
                        // 初始检查
                        updateTeamNameVisibility();
                        
                        // 添加事件监听器
                        awardTypeSelect.addEventListener('change', updateTeamNameVisibility);
                        
                        function updateTeamNameVisibility() {
                            var selectedValue = awardTypeSelect.value;
                            // 根据奖项类型判断是否显示团队名称框
                            // 假设奖项类型中，个人奖项的名称包含"个人"，团队奖项的名称包含"团队"
                            var selectedText = awardTypeSelect.options[awardTypeSelect.selectedIndex].text;
                            if (selectedText.includes('个人')) {
                                teamNameDiv.style.display = 'none';
                                // 清空团队名称值
                                teamNameInput.value = '';
                            } else if (selectedText.includes('团队')) {
                                teamNameDiv.style.display = 'block';
                            } else {
                                // 默认显示
                                teamNameDiv.style.display = 'block';
                            }
                        }
                    });
                    </script>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label required">比赛名称</label>
                                    <input type="text" class="form-control" name="competition" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">比赛时间</label>
                                    <input type="date" class="form-control" name="competitionTime" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">比赛地点</label>
                                    <input type="text" class="form-control" name="competitionLocation">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">比赛界别</label>
                                    <input type="text" class="form-control" name="competitionSession" placeholder="如：第12届">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">比赛等级</label>
                                    <select class="form-control" name="competitionLevel" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${competitionLevels}">
                                            <option value="${dict.id}">${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项类型</label>
                                    <select class="form-control" name="awardType" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardTypes}">
                                            <option value="${dict.id}">${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项类别</label>
                                    <select class="form-control" name="awardCategory" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardCategories}">
                                            <option value="${dict.id}">${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-12">
                                <div class="mb-3">
                                    <label class="form-label">团队名称</label>
                                    <input type="text" class="form-control" name="teamName" placeholder="团队奖项请填写团队名称">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项等级</label>
                                    <select class="form-control" name="awardLevel" required>
                                        <option value="">请选择</option>
                                        <c:forEach var="dict" items="${awardLevels}">
                                            <option value="${dict.id}">${dict.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label required">奖项图片</label>
                                    <input type="file" class="form-control" name="awardImages" multiple accept="image/*" required>
                                    <small class="form-hint">可上传多张图片，支持JPG、PNG等格式</small>
                                </div>
                            </div>
                        </div>
                        
                        <div class="card-footer">
                            <button type="submit" class="btn btn-primary">提交审核</button>
                            <a href="${pageContext.request.contextPath}/member/award/list.jsp" class="btn btn-link">取消</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../jsp/common/layout_bottom.jsp" />