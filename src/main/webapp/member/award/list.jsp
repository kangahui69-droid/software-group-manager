<!--
 * @Author: error: error: git config user.name & please set dead value or install git && error: git config user.email & please set dead value or install git & please set dead value or install git
 * @Date: 2026-01-12 20:16:37
 * @LastEditors: error: error: git config user.name & please set dead value or install git && error: git config user.email & please set dead value or install git & please set dead value or install git
 * @LastEditTime: 2026-01-13 14:17:51
 * @FilePath: \Software_group_v2\WebContent\member\award\list.jsp
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 检查awards属性是否存在，如果不存在则重定向到servlet --%>
<% if (request.getAttribute("awards") == null) {
    response.sendRedirect(request.getContextPath() + "/award?action=list");
    return;
} %>
    <%@ page import="java.util.List" %>
        <%@ page import="model.Award" %>
            <jsp:include page="../../jsp/common/layout_top.jsp">
                <jsp:param name="title" value="我的奖项" />
            </jsp:include>
            <div class="container-xl">
                <%-- 成功提示 --%>
                <c:if test="${not empty param.success}">
                    <div class="alert alert-success alert-dismissible" role="alert">
                        ${param.success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                        
                        <div class="row row-cards">
                            <!-- 左侧：奖项列表 -->
                            <div class="col-md-8">
                                <a href="${pageContext.request.contextPath}/award?action=submit" class="btn btn-primary mb-3">提交新奖项</a>
                                
                                <!-- 筛选表单 -->
                                <div class="card mb-3">
                                    <div class="card-body">
                                        <form id="filterForm" class="row g-3">
                                            <div class="col-md-3">
                                                <label class="form-label">比赛名称</label>
                                                <input type="text" name="competition" class="form-control" placeholder="输入比赛名称">
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label">比赛等级</label>
                                                <select name="competitionLevel" class="form-select">
                                                    <option value="">全部</option>
                                                    <c:forEach var="dict" items="${competitionLevels}">
                                                        <option value="${dict.id}">${dict.name}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label">奖项等级</label>
                                                <select name="awardLevel" class="form-select">
                                                    <option value="">全部</option>
                                                    <c:forEach var="dict" items="${awardLevels}">
                                                        <option value="${dict.id}">${dict.name}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label">状态</label>
                                                <select name="status" class="form-select">
                                                    <option value="">全部</option>
                                                    <option value="PENDING">审核中</option>
                                                    <option value="APPROVED">已通过</option>
                                                    <option value="REJECTED">已拒绝</option>
                                                </select>
                                            </div>
                                            <div class="col-md-3 align-self-end">
                                                <button type="button" id="filterBtn" class="btn btn-primary">筛选</button>
                                                <button type="button" id="resetBtn" class="btn btn-outline-secondary ms-2">重置</button>
                                            </div>
                                        </form>
                                        
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
                                                    // 重置表单
                                                    filterForm.reset();
                                                    // 重新加载所有奖项
                                                    filterAwards();
                                                });
                                                
                                                function filterAwards() {
                                                    const formData = new FormData(filterForm);
                                                    const params = new URLSearchParams();
                                                    
                                                    formData.forEach((value, key) => {
                                                        params.append(key, value);
                                                    });
                                                    
                                                    // 打印发送的筛选参数
                                                    console.log('发送的筛选参数:', Object.fromEntries(params.entries()));
                                                    
                                                    // 显示加载状态
                                                    awardTableBody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">加载中...</span></div></td></tr>';
                                                    
                                                    // 构建完整的请求URL
                                                    const requestUrl = '${pageContext.request.contextPath}/award?action=filter&' + params.toString();
                                                    console.log('发送筛选请求到:', requestUrl);
                                                    
                                                    // 发送AJAX请求
                                                    fetch(requestUrl, {
                                                        method: 'GET',
                                                        headers: {
                                                            'Accept': 'application/json',
                                                            'Content-Type': 'application/json'
                                                        },
                                                        credentials: 'include' // 包含cookie，确保会话保持
                                                    })
                                                    .then(response => {
                                                        // 打印响应状态
                                                        console.log('响应状态:', response.status, response.statusText);
                                                        // 打印响应头
                                                        console.log('响应头:', Object.fromEntries(response.headers.entries()));
                                                        // 检查响应状态
                                                        if (!response.ok) {
                                                            throw new Error('HTTP error! status: ' + response.status);
                                                        }
                                                        // 读取响应文本，先检查是否为空
                                                        return response.text().then(text => {
                                                            console.log('响应文本:', text);
                                                            if (!text) {
                                                                throw new Error('空响应');
                                                            }
                                                            return JSON.parse(text);
                                                        });
                                                    })
                                                    .then(data => {
                                                        // 打印接收到的数据
                                                        console.log('接收到的筛选结果:', data);
                                                        // 更新表格内容
                                                        updateAwardTable(data);
                                                    })
                                                    .catch(error => {
                                                        console.error('筛选失败:', error);
                                                        console.error('错误详情:', error.stack);
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
                                                            statusBadge = '<span class="badge bg-warning">审核中</span>';
                                                        } else if (award.awardStatus === 'APPROVED') {
                                                            statusBadge = '<span class="badge bg-success">已通过</span>';
                                                        } else if (award.awardStatus === 'REJECTED') {
                                                            statusBadge = '<span class="badge bg-danger">已拒绝</span>';
                                                        }
                                                        
                                                        let actionButtons = '';
                                                        if (award.awardStatus === 'PENDING') {
                                                            actionButtons = '<a href="' + contextPath + '/award?action=edit&id=' + award.id + '" class="btn btn-sm btn-warning">编辑</a> <a href="' + contextPath + '/award?action=delete&id=' + award.id + '" class="btn btn-sm btn-danger" onclick="return confirm(\'确定要删除这个奖项吗？\')">删除</a>';
                                                        }
                                                        
                                                        html += `
                                                        <tr>
                                                            <td>${award.competition}</td>
                                                            <td>${award.competitionLevelName || ''}</td>
                                                            <td>${award.awardLevelName || ''}</td>
                                                            <td>${award.competitionLocation || ''}</td>
                                                            <td>${award.competitionTime || ''}</td>
                                                            <td>${statusBadge}</td>
                                                            <td>
                                                                <a href="${contextPath}/award?action=detail&id=${award.id}" class="btn btn-sm btn-info">详情</a>
                                                                ${actionButtons}
                                                            </td>
                                                        </tr>
                                                        `;
                                                    });
                                                    
                                                    awardTableBody.innerHTML = html;
                                                }
                                            });
                                        </script>
                                    </div>
                                </div>
                                
                                <div class="card">
                                    <div class="card-body">
                                        <table class="table table-striped">
                                            <thead>
                                                <tr>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competition" class="text-reset">比赛名称</a></th>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionLevel" class="text-reset">比赛等级</a></th>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=awardLevel" class="text-reset">奖项等级</a></th>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionLocation" class="text-reset">比赛地点</a></th>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=competitionTime" class="text-reset">比赛时间</a></th>
                                                    <th><a href="${pageContext.request.contextPath}/award?action=list&sort=awardStatus" class="text-reset">状态</a></th>
                                                    <th>操作</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <%-- 使用JSTL来显示奖项列表 --%>
                                                <c:forEach var="award" items="${awards}">
                                                        <tr>
                                                            <td>
                                                                ${award.competition}
                                                            </td>
                                                            <td>
                                                                <c:forEach var="dict" items="${competitionLevels}">
                                                                    <c:if test="${award.competitionLevel eq dict.id}">${dict.name}</c:if>
                                                                </c:forEach>
                                                            </td>
                                                            <td>
                                                                <%-- 显示奖项等级的中文名称 --%>
                                                                <c:forEach var="dict" items="${awardLevels}">
                                                                    <c:if test="${award.awardLevel eq dict.id}">${dict.name}</c:if>
                                                                </c:forEach>
                                                            </td>
                                                            <td>
                                                                ${award.competitionLocation}
                                                            </td>
                                                            <td>
                                                                ${award.competitionTime}
                                                            </td>
                                                            <td>
                                                                <%-- 显示奖项状态的中文名称 --%>
                                                                <c:choose>
                                                                    <c:when test="${award.awardStatus eq 'PENDING'}">
                                                                        <span class="badge bg-warning">审核中</span>
                                                                    </c:when>
                                                                    <c:when test="${award.awardStatus eq 'APPROVED'}">
                                                                        <span class="badge bg-success">已通过</span>
                                                                    </c:when>
                                                                    <c:when test="${award.awardStatus eq 'REJECTED'}">
                                                                        <span class="badge bg-danger">已拒绝</span>
                                                                    </c:when>
                                                                    <c:otherwise>${award.awardStatus}</c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>
                                                                <a href="${pageContext.request.contextPath}/award?action=detail&id=${award.id}"
                                                                    class="btn btn-sm btn-info">详情</a>
                                                                <c:if test="${award.awardStatus eq 'PENDING'}">
                                                                <a href="${pageContext.request.contextPath}/award?action=edit&id=${award.id}"
                                                                    class="btn btn-sm btn-warning">编辑</a>
                                                                <a href="${pageContext.request.contextPath}/award?action=delete&id=${award.id}"
                                                                    class="btn btn-sm btn-danger" onclick="return confirm('确定要删除这个奖项吗？')">删除</a>
                                                                </c:if>
                                                            </td>
                                                        </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 右侧：统计信息 -->
                            <div class="col-md-4">
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title">奖项统计</h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="row row-cards">
                                            <!-- 个人获奖总数 -->
                                            <div class="col-md-6 col-lg-12">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-primary text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 21h8a2 2 0 0 0 2 -2v-9a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2z" /><path d="M12 17v-4" /><path d="M7 13h10" /><path d="M8 21h8" /><path d="M12 17l-3 3" /><path d="M12 17l3 3" /><path d="M6 9a6 6 0 0 1 12 0" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">个人获奖总数</h4>
                                                                <div class="text-primary fs-4">
                                                                    <c:out value="${awardStats.totalPersonalAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <!-- 个人奖项数量 -->
                                            <div class="col-md-6 col-lg-6">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-success text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 7a4 4 0 1 0 8 0a4 4 0 0 0 -8 0" /><path d="M6 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">个人奖项</h4>
                                                                <div class="text-success fs-4">
                                                                    <c:out value="${awardStats.personalAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <!-- 团体奖项数量 -->
                                            <div class="col-md-6 col-lg-6">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-info text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21h18" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /><path d="M21 21v-4a3 3 0 0 0 -3 -3H6a3 3 0 0 0 -3 3v4" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">团体奖项</h4>
                                                                <div class="text-info fs-4">
                                                                    <c:out value="${awardStats.teamAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <!-- 国家级奖项数量 -->
                                            <div class="col-md-6 col-lg-6">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-warning text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 5a5 5 0 0 1 7 0a5 5 0 0 0 7 0v14a1 1 0 0 1 -1 1h-12a1 1 0 0 1 -1 -1v-14z" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">国家级奖项</h4>
                                                                <div class="text-warning fs-4">
                                                                    <c:out value="${awardStats.nationalAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <!-- 省级及地区级奖项数量 -->
                                            <div class="col-md-6 col-lg-6">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-danger text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><circle cx="12" cy="11" r="3" /><path d="M17.657 16.657l-4.243 4.243a2 2 0 0 1 -2.827 0l-4.244 -4.243a8 8 0 1 1 11.314 0z" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">省级奖项</h4>
                                                                <div class="text-danger fs-4">
                                                                    <c:out value="${awardStats.provincialAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <!-- 当前年度获奖总数 -->
                                            <div class="col-md-6 col-lg-12">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <div class="d-flex align-items-center">
                                                            <div class="flex-shrink-0">
                                                                <span class="bg-purple text-white avatar">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><rect x="4" y="5" width="16" height="16" rx="2" /><path d="M8 9h8" /><path d="M16 12h2" /><path d="M16 16h2" /><path d="M8 12h2" /><path d="M8 16h2" /></svg>
                                                                </span>
                                                            </div>
                                                            <div class="flex-grow-1 ms-3">
                                                                <h4 class="card-title">当前年度获奖总数</h4>
                                                                <div class="text-purple fs-4">
                                                                    <c:out value="${awardStats.currentYearAwards}" default="0" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
            </div>
            <jsp:include page="../../jsp/common/layout_bottom.jsp" />