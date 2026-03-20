<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../../common/layout_top.jsp">
    <jsp:param name="title" value="报名管理" />
</jsp:include>

<div class="page-header d-print-none">
    <div class="container-xl">
        <div class="row g-2 align-items-center">
            <div class="col">
                <h2 class="page-title">
                    报名管理 - ${activity.title}
                </h2>
                <p class="text-muted">
                    <fmt:formatDate value="${activity.activityStartTime}" pattern="yyyy-MM-dd HH:mm" /> | 
                    地点: ${activity.location} |
                    <c:choose>
                        <c:when test="${activity.registrationEnded}">
                            <span class="text-danger">报名已截止</span>
                        </c:when>
                        <c:otherwise>
                            <span class="text-success">报名进行中</span>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="col-auto">
                <a href="${pageContext.request.contextPath}/activity?action=manage" class="btn btn-outline-secondary">
                    返回活动管理
                </a>
            </div>
        </div>
    </div>
</div>

<div class="page-body">
    <div class="container-xl">
        <!-- 提示消息 -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible" role="alert">
                ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible" role="alert">
                ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 统计信息 -->
        <div class="row row-cols-1 row-cols-md-4 g-3 mb-3">
            <div class="col">
                <div class="card bg-primary text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="subtile">总报名人数</div>
                                <div class="h1 m-0">${registrations.size()}</div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                <circle cx="9" cy="7" r="4"></circle>
                                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card bg-warning">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="subtile">待审核</div>
                                <div class="h1 m-0">
                                    <c:set var="pendingCount" value="0" />
                                    <c:forEach var="r" items="${registrations}">
                                        <c:if test="${r.status == 'pending'}"><c:set var="pendingCount" value="${pendingCount + 1}" /></c:if>
                                    </c:forEach>
                                    ${pendingCount}
                                </div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <circle cx="12" cy="12" r="10"></circle>
                                <polyline points="12 6 12 12 16 14"></polyline>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card bg-success text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="subtile">已确认</div>
                                <div class="h1 m-0">
                                    <c:set var="confirmedCount" value="0" />
                                    <c:forEach var="r" items="${registrations}">
                                        <c:if test="${r.status == 'confirmed'}"><c:set var="confirmedCount" value="${confirmedCount + 1}" /></c:if>
                                    </c:forEach>
                                    ${confirmedCount}
                                </div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card bg-danger text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="subtile">已驳回</div>
                                <div class="h1 m-0">
                                    <c:set var="rejectedCount" value="0" />
                                    <c:forEach var="r" items="${registrations}">
                                        <c:if test="${r.status == 'rejected'}"><c:set var="rejectedCount" value="${rejectedCount + 1}" /></c:if>
                                    </c:forEach>
                                    ${rejectedCount}
                                </div>
                            </div>
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon icon-lg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="15" y1="9" x2="9" y2="15"></line>
                                <line x1="9" y1="9" x2="15" y2="15"></line>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${empty registrations}">
            <div class="alert alert-info">
                暂无报名人员
            </div>
        </c:if>
        
        <!-- 已过期提示 -->
        <c:if test="${activity.registrationEnded}">
            <div class="alert alert-warning">
                <strong>注意：</strong>此活动的报名已截止，仅可查看报名记录，无法进行审核操作。
            </div>
        </c:if>
        
        <form id="participantForm" method="POST" action="${pageContext.request.contextPath}/activity">
            <input type="hidden" name="activityId" value="${activity.id}">
            
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h3 class="card-title">报名人员列表</h3>
                        <c:if test="${not activity.registrationEnded}">
                            <div>
                                <label class="form-check">
                                    <input type="checkbox" class="form-check-input" id="selectAll">
                                    <span class="form-check-label">全选</span>
                                </label>
                            </div>
                        </c:if>
                    </div>
                </div>
                <c:if test="${not activity.registrationEnded}">
                    <div class="card-body">
                        <div class="mb-3">
                            <button type="submit" name="action" value="batchApprove" class="btn btn-success" onclick="return confirm('确定要批量通过选中的报名吗？')">
                                批量通过
                            </button>
                            <button type="submit" name="action" value="batchReject" class="btn btn-danger" onclick="return confirm('确定要批量拒绝选中的报名吗？')">
                                批量拒绝
                            </button>
                        </div>
                    </div>
                </c:if>
                <div class="table-responsive">
                    <table class="table table-vcenter card-table table-striped">
                        <thead>
                            <tr>
                                <th class="w-1"></th>
                                <th>姓名</th>
                                <th>学号</th>
                                <th>专业</th>
                                <th>报名时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${registrations}">
                                <tr>
                                    <td>
                                        <c:if test="${not activity.registrationEnded}">
                                            <input type="checkbox" name="userIds" value="${r.userId}" class="participant-checkbox">
                                        </c:if>
                                    </td>
                                    <td>${r.userName}</td>
                                    <td class="text-muted">${r.studentId}</td>
                                    <td class="text-muted">${r.major}</td>
                                    <td class="text-muted">
                                        <fmt:formatDate value="${r.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.displayStatus == 'confirmed'}">
                                                <span class="badge bg-success">已确认</span>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'pending'}">
                                                <span class="badge bg-warning text-dark">待审核</span>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'rejected'}">
                                                <span class="badge bg-danger">已驳回</span>
                                                <c:if test="${not empty r.notes}">
                                                    <br><small class="text-muted">原因: ${r.notes}</small>
                                                </c:if>
                                            </c:when>
                                            <c:when test="${r.displayStatus == 'expired'}">
                                                <span class="badge bg-secondary text-white">已过期</span>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="btn-list flex-nowrap">
                                            <c:if test="${not activity.registrationEnded}">
                                                <c:if test="${r.status == 'pending'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=approve&activityId=${activity.id}&userId=${r.userId}" class="btn btn-success btn-sm">通过</a>
                                                </c:if>
                                                <c:if test="${r.status == 'pending'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=reject&activityId=${activity.id}&userId=${r.userId}" class="btn btn-danger btn-sm">拒绝</a>
                                                </c:if>
                                                <c:if test="${r.status == 'rejected'}">
                                                    <a href="${pageContext.request.contextPath}/activity?action=deleteRegistration&activityId=${activity.id}&userId=${r.userId}" class="btn btn-outline-danger btn-sm" onclick="return confirm('确定要删除该报名记录吗？用户可以重新报名。')">删除</a>
                                                </c:if>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty registrations}">
                                <tr>
                                    <td colspan="7" class="text-center text-muted">暂无报名人员</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    document.getElementById('selectAll').addEventListener('change', function() {
        const checkboxes = document.querySelectorAll('.participant-checkbox');
        checkboxes.forEach(function(checkbox) {
            checkbox.checked = document.getElementById('selectAll').checked;
        });
    });
    
    document.querySelectorAll('.participant-checkbox').forEach(function(checkbox) {
        checkbox.addEventListener('change', function() {
            const allCheckboxes = document.querySelectorAll('.participant-checkbox');
            const checkedCheckboxes = document.querySelectorAll('.participant-checkbox:checked');
            document.getElementById('selectAll').checked = allCheckboxes.length === checkedCheckboxes.length;
        });
    });
</script>

<jsp:include page="../../common/layout_bottom.jsp" />
