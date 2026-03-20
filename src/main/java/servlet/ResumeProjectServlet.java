package servlet;

import dao.ResumeDAO;
import dao.ResumeProjectDAO;
import model.Resume;
import model.ResumeProject;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * 简历项目经历管理控制器
 * 映射路径: /resume/project
 */
@WebServlet(name = "ResumeProjectServlet", urlPatterns = {"/resume/project"})
public class ResumeProjectServlet extends HttpServlet {

    private ResumeProjectDAO projectDAO = new ResumeProjectDAO();
    private ResumeDAO resumeDAO = new ResumeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 检查登录状态
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        Integer resumeId = parseInt(req.getParameter("resumeId"));

        if (resumeId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        // 验证简历属于当前用户
        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        if ("list".equals(action) || action == null) {
            listProjects(req, resp, resumeId, resume);
        } else if ("create".equals(action)) {
            showCreateForm(req, resp, resumeId, resume);
        } else if ("edit".equals(action)) {
            showEditForm(req, resp, resumeId, resume);
        } else if ("delete".equals(action)) {
            deleteProject(req, resp, resumeId);
        } else {
            listProjects(req, resp, resumeId, resume);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // 检查登录状态
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        Integer resumeId = parseInt(req.getParameter("resumeId"));

        if (resumeId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        // 验证简历属于当前用户
        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        if ("create".equals(action)) {
            createProject(req, resp, resumeId);
        } else if ("update".equals(action)) {
            updateProject(req, resp, resumeId);
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId);
        }
    }

    // ========== 私有方法 ==========

    private void listProjects(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        List<ResumeProject> projects = projectDAO.findByResumeId(resumeId);
        req.setAttribute("projects", projects);
        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-project-list.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-project-edit.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        Integer projectId = parseInt(req.getParameter("id"));
        if (projectId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId);
            return;
        }

        ResumeProject project = projectDAO.findById(projectId);
        if (project == null || !project.getResumeId().equals(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&error=notfound");
            return;
        }

        req.setAttribute("project", project);
        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-project-edit.jsp").forward(req, resp);
    }

    private void createProject(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        ResumeProject project = buildProjectFromRequest(req, resumeId);

        if (projectDAO.save(project)) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&success=created");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=create&resumeId=" + resumeId + "&error=failed");
        }
    }

    private void updateProject(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        Integer projectId = parseInt(req.getParameter("id"));
        if (projectId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId);
            return;
        }

        ResumeProject project = buildProjectFromRequest(req, resumeId);
        project.setId(projectId);

        if (projectDAO.update(project)) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&success=updated");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=edit&resumeId=" + resumeId + "&id=" + projectId + "&error=failed");
        }
    }

    private void deleteProject(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        Integer projectId = parseInt(req.getParameter("id"));
        if (projectId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId);
            return;
        }

        // 验证项目属于该简历
        ResumeProject project = projectDAO.findById(projectId);
        if (project == null || !project.getResumeId().equals(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&error=notfound");
            return;
        }

        if (projectDAO.delete(projectId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/project?action=list&resumeId=" + resumeId + "&error=failed");
        }
    }

    private ResumeProject buildProjectFromRequest(HttpServletRequest req, Integer resumeId) {
        ResumeProject project = new ResumeProject();
        project.setResumeId(resumeId);
        project.setProjectName(req.getParameter("projectName"));
        project.setRole(req.getParameter("role"));
        project.setTeamSize(parseInt(req.getParameter("teamSize")));
        project.setStartDate(parseDate(req.getParameter("startDate")));
        project.setEndDate(parseDate(req.getParameter("endDate")));
        project.setIsCurrent("1".equals(req.getParameter("isCurrent")) ? 1 : 0);
        project.setDescription(req.getParameter("description"));
        project.setResponsibilities(req.getParameter("responsibilities"));
        project.setTechnologies(req.getParameter("technologies"));
        project.setProjectUrl(req.getParameter("projectUrl"));
        project.setAchievements(req.getParameter("achievements"));
        project.setDisplayOrder(parseInt(req.getParameter("displayOrder")));
        return project;
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.sql.Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
