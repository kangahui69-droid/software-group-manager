package servlet;

import dao.RecruitApplicationDAO;
import dao.UserDAO;
import model.RecruitApplication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 招新申请Servlet
 */
public class RecruitServlet extends HttpServlet {
    private RecruitApplicationDAO recruitDAO = new RecruitApplicationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (servletPath.startsWith("/admin/recruit")) {
            handleAdminActions(request, response, pathInfo);
            return;
        }

        if (pathInfo == null || "/apply.jsp".equals(pathInfo) || "/".equals(pathInfo)) {
            request.getRequestDispatcher("/jsp/recruit/apply.jsp").forward(request, response);
        } else if ("/success".equals(pathInfo)) {
            request.getRequestDispatcher("/jsp/recruit/success.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAdminActions(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        // Default to list if pathInfo is null or /manage
        if (pathInfo == null || "/".equals(pathInfo) || "/manage".equals(pathInfo)) {
            listApplications(request, response);
        } else if ("/edit".equals(pathInfo)) {
            showEditForm(request, response);
        } else if ("/review".equals(pathInfo)) {
            handleReview(request, response);
        } else {
            listApplications(request, response);
        }
    }

    private void listApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String yearStr = request.getParameter("year");
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String roundStr = request.getParameter("round");
        
        Integer year = null;
        if (yearStr != null && !yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
            }
        }
        
        Integer round = null;
        if (roundStr != null && !roundStr.isEmpty()) {
            try {
                round = Integer.parseInt(roundStr);
            } catch (NumberFormatException e) {
            }
        }

        List<RecruitApplication> list = recruitDAO.findByConditions(keyword, year, status, round);
        List<Integer> years = recruitDAO.findAllYears();

        request.setAttribute("applicationList", list);
        request.setAttribute("years", years);
        request.setAttribute("selectedYear", year);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("round", roundStr);

        request.getRequestDispatcher("/jsp/admin/recruit/manage.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idStr);
                RecruitApplication app = recruitDAO.findById(id);
                request.setAttribute("app", app);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        request.getRequestDispatcher("/jsp/admin/recruit/edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (servletPath.startsWith("/admin/recruit")) {
            if (servletPath.endsWith("/approve") || (pathInfo != null && pathInfo.startsWith("/approve"))) {
                handleApprove(request, response);
            } else if (servletPath.endsWith("/reject") || (pathInfo != null && pathInfo.startsWith("/reject"))) {
                handleReject(request, response);
            } else {
                handleAdminPost(request, response, pathInfo);
            }
            return;
        }

        if ("/submit".equals(pathInfo) || "/recruit/submit".equals(servletPath)) {
            handlePublicSubmit(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handlePublicSubmit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RecruitApplication app = extractFromRequest(request);

        // 验证必填字段：姓名、学号、专业
        String errorMsg = validateRequiredFields(app);
        if (errorMsg != null) {
            request.setAttribute("error", errorMsg);
            request.getRequestDispatcher("/jsp/recruit/apply.jsp").forward(request, response);
            return;
        }

        app.setStatus(1); // 待处理

        if (recruitDAO.insert(app)) {
            // 成功：设置成功提示并转发到成功页面
            request.setAttribute("success", "报名提交成功！请等待审核结果。");
            request.getRequestDispatcher("/jsp/recruit/success.jsp").forward(request, response);
        } else {
            // 失败：设置错误提示并返回报名页面
            request.setAttribute("error", "提交申请失败，请稍后重试。");
            request.getRequestDispatcher("/jsp/recruit/apply.jsp").forward(request, response);
        }
    }
    
    // 验证必填字段（姓名、学号、专业、邮箱）
    private String validateRequiredFields(RecruitApplication app) {
        if (app.getName() == null || app.getName().trim().isEmpty()) {
            return "请填写姓名";
        }
        if (app.getStudentId() == null || app.getStudentId().trim().isEmpty()) {
            return "请填写学号";
        }
        if (app.getMajor() == null || app.getMajor().trim().isEmpty()) {
            return "请填写专业";
        }
        if (app.getEmail() == null || app.getEmail().trim().isEmpty()) {
            return "请填写邮箱";
        }
        return null;
    }

    private void handleAdminPost(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        String formAction = request.getParameter("action");

        if ("create".equals(formAction)) {
            try {
                RecruitApplication app = extractFromRequest(request);
                
                // 验证必填字段
                String errorMsg = validateRequiredFields(app);
                if (errorMsg != null) {
                    request.setAttribute("error", errorMsg);
                    request.getRequestDispatcher("/jsp/admin/recruit/edit.jsp").forward(request, response);
                    return;
                }
                
                String statusStr = request.getParameter("status");
                app.setStatus(statusStr != null ? Integer.parseInt(statusStr) : 1);

                // Debug logging
                System.out.println("Creating recruitment application:");
                System.out.println("  Name: " + app.getName());
                System.out.println("  StudentId: " + app.getStudentId());
                System.out.println("  Major: " + app.getMajor());
                System.out.println("  Grade: " + app.getGrade());
                System.out.println("  Phone: " + app.getPhone());
                System.out.println("  Email: " + app.getEmail());
                System.out.println("  Status: " + app.getStatus());

                if (recruitDAO.insert(app)) {
                    response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
                } else {
                    request.setAttribute("error", "添加记录失败：数据库返回失败");
                    request.getRequestDispatcher("/jsp/admin/recruit/edit.jsp").forward(request, response);
                }
            } catch (Exception e) {
                System.err.println("Error creating recruitment application: " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("error", "添加记录失败：" + e.getMessage());
                request.getRequestDispatcher("/jsp/admin/recruit/edit.jsp").forward(request, response);
            }
        } else if ("update".equals(formAction)) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            RecruitApplication app = extractFromRequest(request);
            app.setId(Integer.parseInt(idStr));
            String statusStr = request.getParameter("status");
            if (statusStr != null) {
                app.setStatus(Integer.parseInt(statusStr));
            }

            if (recruitDAO.update(app)) {
                response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
            } else {
                request.setAttribute("app", app);
                request.setAttribute("error", "更新记录失败");
                request.getRequestDispatcher("/jsp/admin/recruit/edit.jsp").forward(request, response);
            }
        } else if ("delete".equals(formAction)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                recruitDAO.delete(Integer.parseInt(idStr));
            }
            response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
        }
    }

    private void handleReview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                RecruitApplication app = recruitDAO.findById(id);
                request.setAttribute("app", app);
                request.getRequestDispatcher("/jsp/admin/recruit/review.jsp").forward(request, response);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
    }

    private void handleApprove(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        
        // Try to get from path /approve/{id}
        if (idStr == null || idStr.isEmpty()) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                idStr = pathInfo.substring(1);
            }
        }
        
        if (idStr != null && !idStr.isEmpty()) {
            Integer id = null;
            RecruitApplication app = null;
            dao.UserDAO userDAO = new dao.UserDAO();
            
            try {
                id = Integer.parseInt(idStr);
                app = recruitDAO.findById(id);
                
                if (app == null || app.getStatus() != 1) {
                    response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
                    return;
                }
                
                // Check if user already exists by student ID
                boolean usernameExists = userDAO.existsByUsername(app.getStudentId());
                
                if (usernameExists) {
                    // User already exists, update status only
                    app.setStatus(2);
                    recruitDAO.update(app);
                } else {
                    // Check if email already exists (only if email is not empty)
                    boolean emailExists = false;
                    if (app.getEmail() != null && !app.getEmail().trim().isEmpty()) {
                        emailExists = userDAO.existsByEmail(app.getEmail());
                    }

                    if (emailExists) {
                        request.setAttribute("error", "申请失败：该邮箱已被其他用户使用");
                        listApplications(request, response);
                        return;
                    }

                    // Create new user - must set name from application
                    model.User user = new model.User();
                    user.setUsername(app.getStudentId());
                    user.setPassword("123456");
                    user.setName(app.getName()); // FIX: Set name from application
                    // 如果邮箱为空，设置为 null
                    user.setEmail((app.getEmail() != null && !app.getEmail().trim().isEmpty()) ? app.getEmail() : null);
                    user.setPhone(app.getPhone());
                    user.setRole("MEMBER");
                    user.setStatus(1);
                    // 标记必须修改密码
                    user.setMustChangePassword(true);

                    boolean created = userDAO.insert(user);
                    if (created) {
                        // Create member profile
                        dao.MemberProfileDAO memberProfileDAO = new dao.MemberProfileDAO();
                        model.MemberProfile profile = new model.MemberProfile();
                        profile.setUserId(user.getId());
                        profile.setStudentId(app.getStudentId());
                        profile.setMajor(app.getMajor());
                        profile.setGrade(app.getGrade());
                        profile.setStatus(1);
                        memberProfileDAO.insert(profile);
                        
                        // Update status to approved
                        app.setStatus(2);
                        recruitDAO.update(app);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "申请失败：系统错误 - " + e.getMessage());
                listApplications(request, response);
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
    }

    private void handleReject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== handleReject called ===");
        String idStr = request.getParameter("id");
        // Try to get from path /reject/{id}
        if (idStr == null || idStr.isEmpty()) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                idStr = pathInfo.substring(1);
            }
        }
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                RecruitApplication app = recruitDAO.findById(id);
                if (app != null && app.getStatus() == 1) {
                    app.setStatus(0); // Rejected
                    recruitDAO.update(app);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/recruit/manage");
    }

    private RecruitApplication extractFromRequest(HttpServletRequest request) {
        String name = request.getParameter("name");
        String studentId = request.getParameter("studentId");
        String major = request.getParameter("major");
        String grade = request.getParameter("grade");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String reason = request.getParameter("reason");

        RecruitApplication app = new RecruitApplication();
        app.setName(name);
        app.setStudentId(studentId);
        app.setMajor(major);
        app.setGrade(grade);
        app.setPhone(phone);
        app.setEmail(email);
        app.setReason(reason);
        return app;
    }
}
