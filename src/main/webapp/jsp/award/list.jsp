<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <jsp:include page="../common/layout_top.jsp">
            <jsp:param name="title" value="奖项管理" />
        </jsp:include>

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title">
                            奖项管理
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="card">
                    <div class="table-responsive">
                        <table class="table table-vcenter card-table table-striped">
                            <thead>
                                <tr>
                                    <th>标题</th>
                                    <th>比赛</th>
                                    <th>级别</th>
                                    <th>年份</th>
                                    <th>状态</th>
                                    <th class="w-1">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="a" items="${awards}">
                                    <tr>
                                        <td>${a.title}</td>
                                        <td class="text-muted">${a.competition}</td>
                                        <td class="text-muted">${a.level}</td>
                                        <td class="text-muted">${a.year}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${a.status == 'RAW'}">
                                                    <span class="badge bg-warning text-dark">原始</span>
                                                </c:when>
                                                <c:when test="${a.status == 'CLEANED'}">
                                                    <span class="badge bg-info text-white">已清洗</span>
                                                </c:when>
                                                <c:when test="${a.status == 'PUBLISHED'}">
                                                    <span class="badge bg-success">已发布</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-list flex-nowrap">
                                                <button class="btn btn-white btn-sm">清洗</button>
                                                <button class="btn btn-success btn-sm text-white">发布</button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty awards}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted">暂无奖项记录</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <jsp:include page="../common/layout_bottom.jsp" />