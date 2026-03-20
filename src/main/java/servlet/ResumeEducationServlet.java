package servlet;

import dao.ResumeEducationDAO;
import dao.ResumeDAO;
import model.ResumeEducation;
import model.Resume;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * 简历-教育经历控制器
 * 映射路径: /resume/education
 */
public class ResumeEducationServlet extends HttpServlet {

    private ResumeEducationDAO educationDAO = new ResumeEducationDAO();
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

        if ("list".equals(action)) {
            listEducations(req, resp, user.getId());
        } else if ("create".equals(action)) {
            showCreateForm(req, resp, user.getId());
        } else if ("edit".equals(action)) {
            showEditForm(req, resp, user.getId());
        } else if ("delete".equals(action)) {
            deleteEducation(req, resp, user.getId());
        } else {
            listEducations(req, resp, user.getId());
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

        if ("create".equals(action)) {
            createEducation(req, resp, user.getId());
        } else if ("update".equals(action)) {
            updateEducation(req, resp, user.getId());
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=list");
        }
    }

    /**
     * 列出简历的所有教育经历
     */
    private void listEducations(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(resumeIdStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        List<ResumeEducation> educations = educationDAO.findByResumeId(resumeId);

        req.setAttribute("resume", resume);
        req.setAttribute("educations", educations);
        req.getRequestDispatcher("/member/resume-education-list.jsp").forward(req, resp);
    }

    /**
     * 显示创建表单
     */
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(resumeIdStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        req.setAttribute("resume", resume);
        req.setAttribute("action", "create");
        req.getRequestDispatcher("/member/resume-education-edit.jsp").forward(req, resp);
    }

    /**
     * 显示编辑表单
     */
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer educationId = Integer.parseInt(idStr);
        ResumeEducation education = educationDAO.findById(educationId);

        if (education == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        Resume resume = resumeDAO.findById(education.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        req.setAttribute("resume", resume);
        req.setAttribute("education", education);
        req.setAttribute("action", "edit");
        req.getRequestDispatcher("/member/resume-education-edit.jsp").forward(req, resp);
    }

    /**
     * 创建教育经历
     */
    private void createEducation(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(resumeIdStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        ResumeEducation education = new ResumeEducation();
        education.setResumeId(resumeId);
        education.setSchoolName(req.getParameter("schoolName"));
        education.setMajor(req.getParameter("major"));
        education.setDegree(req.getParameter("degree"));

        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");
        if (startDateStr != null && !startDateStr.isEmpty()) {
            education.setStartDate(Date.valueOf(startDateStr));
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            education.setEndDate(Date.valueOf(endDateStr));
        }

        String isCurrentStr = req.getParameter("isCurrent");
        education.setIsCurrent("1".equals(isCurrentStr) ? 1 : 0);
        education.setDescription(req.getParameter("description"));

        if (educationDAO.save(education)) {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=list&resumeId=" + resumeId + "&success=created");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=create&resumeId=" + resumeId + "&error=failed");
        }
    }

    /**
     * 更新教育经历
     */
    private void updateEducation(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer educationId = Integer.parseInt(idStr);
        ResumeEducation education = educationDAO.findById(educationId);

        if (education == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        Resume resume = resumeDAO.findById(education.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        education.setSchoolName(req.getParameter("schoolName"));
        education.setMajor(req.getParameter("major"));
        education.setDegree(req.getParameter("degree"));

        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");
        if (startDateStr != null && !startDateStr.isEmpty()) {
            education.setStartDate(Date.valueOf(startDateStr));
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            education.setEndDate(Date.valueOf(endDateStr));
        }

        String isCurrentStr = req.getParameter("isCurrent");
        education.setIsCurrent("1".equals(isCurrentStr) ? 1 : 0);
        education.setDescription(req.getParameter("description"));

        if (educationDAO.update(education)) {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=list&resumeId=" + education.getResumeId() + "&success=updated");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=edit&id=" + educationId + "&error=failed");
        }
    }

    /**
     * 删除教育经历
     */
    private void deleteEducation(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer educationId = Integer.parseInt(idStr);
        ResumeEducation education = educationDAO.findById(educationId);

        if (education == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        Resume resume = resumeDAO.findById(education.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        Integer resumeId = education.getResumeId();
        if (educationDAO.delete(educationId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=list&resumeId=" + resumeId + "&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/education?action=list&resumeId=" + resumeId + "&error=failed");
        }
    }
}
