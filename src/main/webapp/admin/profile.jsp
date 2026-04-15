<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dao.AdminProfileDAO" %>
<%@ page import="model.AdminProfile" %>
<%
    model.User user = (model.User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    AdminProfile adminProfile = (AdminProfile) session.getAttribute("adminProfile");
    if (adminProfile == null && user != null) {
        AdminProfileDAO adminProfileDAO = new AdminProfileDAO();
        adminProfile = adminProfileDAO.findByUserId(user.getId());
        if (adminProfile != null) {
            session.setAttribute("adminProfile", adminProfile);
        }
    }
%>
<jsp:include page="../jsp/common/layout_top.jsp">
    <jsp:param name="title" value="管理员个人中心" />
</jsp:include>

<div class="page-body">
    <div class="container-xl">
        <div class="admin-hero">
            <h1 class="admin-hero-title">
                <i class="bi bi-person-circle me-2"></i>个人信息
            </h1>
            <p class="admin-hero-subtitle">管理您的个人资料和账户信息</p>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design" style="background: var(--bg-white); border-radius: var(--radius-generous); padding: 24px; box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; margin-bottom: 24px;">
                    <h3 class="card-title-design" style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                        <i class="bi bi-person text-brand"></i>基本信息
                    </h3>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                                    <i class="bi bi-person"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">用户名</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${user.username}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                    <i class="bi bi-shield-check"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">角色</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${user.role}'/></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-design" style="background: var(--bg-white); border-radius: var(--radius-generous); padding: 24px; box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; margin-bottom: 24px;">
                    <h3 class="card-title-design" style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                        <i class="bi bi-card-text text-brand"></i>个人信息
                    </h3>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(20, 184, 166, 0.1); color: #14b8a6;">
                                    <i class="bi bi-person-badge"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">姓名</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${user.name}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(236, 72, 153, 0.1); color: #ec4899;">
                                    <i class="bi bi-telephone"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">电话</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${user.phone}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(249, 115, 22, 0.1); color: #f97316;">
                                    <i class="bi bi-envelope"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">邮箱</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${user.email}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">
                                    <i class="bi bi-briefcase"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">职称</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${adminProfile.title}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(99, 102, 241, 0.1); color: #6366f1;">
                                    <i class="bi bi-building"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">部门</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${adminProfile.department}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(168, 85, 247, 0.1); color: #a855f7;">
                                    <i class="bi bi-mortarboard"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">学历</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${adminProfile.education}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-12">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-light);">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(20, 86, 240, 0.1); color: var(--brand-blue);">
                                    <i class="bi bi-lightbulb"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">研究领域</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark);"><c:out value='${adminProfile.researchArea}'/></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-12">
                            <div class="info-item" style="display: flex; align-items: flex-start; gap: 12px; padding: 12px 0;">
                                <div class="info-icon" style="width: 40px; height: 40px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 1rem; background: rgba(100, 116, 139, 0.1); color: #64748b;">
                                    <i class="bi bi-file-text"></i>
                                </div>
                                <div class="info-content" style="flex: 1;">
                                    <div class="info-label" style="font-family: var(--font-ui); font-size: 0.75rem; font-weight: 500; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">个人简介</div>
                                    <div class="info-value" style="font-family: var(--font-ui); font-size: 0.94rem; font-weight: 500; color: var(--text-dark); white-space: pre-wrap;"><c:out value='${adminProfile.bio}'/></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="d-flex gap-3">
                    <a href="${pageContext.request.contextPath}/admin/edit-profile.jsp" class="btn btn-brand">
                        <i class="bi bi-pencil me-1"></i>编辑资料
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/password-change.jsp" class="btn btn-outline-brand">
                        <i class="bi bi-lock me-1"></i>修改密码
                    </a>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design" style="background: var(--bg-white); border-radius: var(--radius-generous); padding: 24px; box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; margin-bottom: 24px;">
                    <h3 class="card-title-design" style="font-family: var(--font-display); font-size: 1.25rem; font-weight: 600; color: var(--text-dark); margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                        <i class="bi bi-grid-3x3-gap text-brand"></i>系统管理
                    </h3>
                    <div class="row g-3">
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/news?action=manage" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #3b82f6, #60a5fa); color: white;">
                                    <i class="bi bi-newspaper"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">新闻管理</div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/project/list" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #10b981, #34d399); color: white;">
                                    <i class="bi bi-kanban"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">项目管理</div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #f59e0b, #fbbf24); color: white;">
                                    <i class="bi bi-people"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">成员管理</div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/activity/list" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #ec4899, #f472b6); color: white;">
                                    <i class="bi bi-calendar-check"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">活动管理</div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/award/list" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #f97316, #fb923c); color: white;">
                                    <i class="bi bi-trophy"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">奖项管理</div>
                            </a>
                        </div>
                        <div class="col-6">
                            <a href="${pageContext.request.contextPath}/admin/log/list" class="quick-link-card" style="background: var(--bg-white); border-radius: var(--radius-comfortable); padding: 16px; box-shadow: var(--shadow-standard); border: 1px solid var(--border-light); transition: all 0.3s ease; text-decoration: none; display: flex; flex-direction: column; align-items: center; gap: 12px;">
                                <div class="quick-link-icon" style="width: 48px; height: 48px; border-radius: var(--radius-standard); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: linear-gradient(135deg, #64748b, #94a3b8); color: white;">
                                    <i class="bi bi-journal-text"></i>
                                </div>
                                <div class="quick-link-title" style="font-family: var(--font-ui); font-weight: 500; font-size: 0.875rem; color: var(--text-dark); text-align: center;">操作日志</div>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../jsp/common/layout_bottom.jsp" />