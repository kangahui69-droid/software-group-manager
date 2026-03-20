package servlet;

import dao.ResumeDAO;
import dao.ResumeEducationDAO;
import dao.ResumeSkillDAO;
import dao.ResumeProjectDAO;
import dao.ResumeAwardDAO;
import model.Resume;
import model.ResumeEducation;
import model.ResumeSkill;
import model.ResumeProject;
import model.ResumeAward;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 简历功能控制器
 * 映射路径: /resume
 */
public class ResumeServlet extends HttpServlet {

    private ResumeDAO resumeDAO = new ResumeDAO();
    private ResumeEducationDAO educationDAO = new ResumeEducationDAO();
    private ResumeSkillDAO skillDAO = new ResumeSkillDAO();
    private ResumeProjectDAO projectDAO = new ResumeProjectDAO();
    private ResumeAwardDAO awardDAO = new ResumeAwardDAO();

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

        if ("list".equals(action) || action == null) {
            listResumes(req, resp, user.getId());
        } else if ("view".equals(action)) {
            viewResume(req, resp, user.getId());
        } else if ("create".equals(action)) {
            showCreateForm(req, resp);
        } else if ("edit".equals(action)) {
            showEditForm(req, resp, user.getId());
        } else if ("delete".equals(action)) {
            deleteResume(req, resp, user.getId());
        } else if ("setDefault".equals(action)) {
            setDefaultResume(req, resp, user.getId());
        } else if ("preview".equals(action)) {
            previewResume(req, resp, user.getId());
        } else if ("recycleBin".equals(action)) {
            showRecycleBin(req, resp, user.getId());
        } else if ("restore".equals(action)) {
            restoreResume(req, resp, user.getId());
        } else if ("hardDelete".equals(action)) {
            hardDeleteResume(req, resp, user.getId());
        } else {
            listResumes(req, resp, user.getId());
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
            createResume(req, resp, user.getId());
        } else if ("update".equals(action)) {
            updateResume(req, resp, user.getId());
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
        }
    }

    // ========== 私有方法 ==========

    /**
     * 列出用户的所有简历
     */
    private void listResumes(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        List<Resume> resumes = resumeDAO.findByUserId(userId);
        req.setAttribute("resumes", resumes);
        req.getRequestDispatcher("/member/resume.jsp").forward(req, resp);
    }

    /**
     * 查看简历详情
     */
    private void viewResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        // 加载关联数据
        loadResumeDetails(resume);

        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-view.jsp").forward(req, resp);
    }

    /**
     * 显示创建表单
     */
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/member/resume-edit.jsp").forward(req, resp);
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

        Integer resumeId = Integer.parseInt(idStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        // 加载关联数据
        loadResumeDetails(resume);

        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-edit.jsp").forward(req, resp);
    }

    /**
     * 创建简历
     */
    private void createResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setResumeName(req.getParameter("resumeName"));
        resume.setTemplateStyle(req.getParameter("templateStyle"));
        resume.setSummary(req.getParameter("summary"));
        resume.setCareerObjective(req.getParameter("careerObjective"));
        resume.setPhone(req.getParameter("phone"));
        resume.setEmail(req.getParameter("email"));
        resume.setWechat(req.getParameter("wechat"));
        resume.setGithubUrl(req.getParameter("githubUrl"));
        resume.setBlogUrl(req.getParameter("blogUrl"));
        resume.setStatus(1); // 默认已发布

        // 如果是第一个简历，设为默认
        List<Resume> existingResumes = resumeDAO.findByUserId(userId);
        if (existingResumes.isEmpty()) {
            resume.setIsDefault(1);
        } else {
            resume.setIsDefault(0);
        }

        if (resumeDAO.save(resume)) {
            // 保存成功后，resume对象已被赋予ID
            Integer newResumeId = resume.getId();
            System.out.println("[ResumeServlet] 简历创建成功，ID=" + newResumeId);
            resp.sendRedirect(req.getContextPath() + "/resume?action=edit&id=" + newResumeId + "&success=created");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=create&error=failed");
        }
    }

    /**
     * 更新简历
     */
    private void updateResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        // 更新字段
        resume.setResumeName(req.getParameter("resumeName"));
        resume.setTemplateStyle(req.getParameter("templateStyle"));
        resume.setSummary(req.getParameter("summary"));
        resume.setCareerObjective(req.getParameter("careerObjective"));
        resume.setPhone(req.getParameter("phone"));
        resume.setEmail(req.getParameter("email"));
        resume.setWechat(req.getParameter("wechat"));
        resume.setGithubUrl(req.getParameter("githubUrl"));
        resume.setBlogUrl(req.getParameter("blogUrl"));
        resume.setPhotoUrl(req.getParameter("photoUrl"));

        if (resumeDAO.update(resume)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=edit&id=" + resumeId + "&success=updated");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=edit&id=" + resumeId + "&error=failed");
        }
    }

    /**
     * 删除简历
     */
    private void deleteResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);
        Resume resume = resumeDAO.findById(resumeId);

        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        if (resumeDAO.softDelete(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=failed");
        }
    }

    /**
     * 设置默认简历
     */
    private void setDefaultResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);

        if (resumeDAO.setDefaultResume(resumeId, userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&success=defaultSet");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=failed");
        }
    }

    /**
     * 预览简历（公开访问）
     */
    private void previewResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);
        Resume resume = resumeDAO.findById(resumeId);

        // 检查权限：只能预览自己的简历
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        // 加载关联数据
        loadResumeDetails(resume);

        // 增加浏览次数
        resumeDAO.incrementViewCount(resumeId);

        req.setAttribute("resume", resume);
        req.setAttribute("isPreview", true);
        req.getRequestDispatcher("/member/resume-preview.jsp").forward(req, resp);
    }

    /**
     * 显示回收站（被软删除的简历）
     */
    private void showRecycleBin(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        List<Resume> deletedResumes = resumeDAO.findDeletedByUserId(userId);
        req.setAttribute("deletedResumes", deletedResumes);
        req.getRequestDispatcher("/member/resume-recyclebin.jsp").forward(req, resp);
    }

    /**
     * 恢复软删除的简历
     */
    private void restoreResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);

        // 验证简历属于当前用户且是被软删除的
        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId) || resume.getDeleted() != 1) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin&error=notfound");
            return;
        }

        if (resumeDAO.restore(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&success=restored");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin&error=failed");
        }
    }

    /**
     * 永久删除简历（硬删除）
     */
    private void hardDeleteResume(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin");
            return;
        }

        Integer resumeId = Integer.parseInt(idStr);

        // 验证简历属于当前用户且是被软删除的
        Resume resume = resumeDAO.findById(resumeId, true); // 包含已删除的
        if (resume == null || !resume.getUserId().equals(userId) || resume.getDeleted() != 1) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin&error=notfound");
            return;
        }

        if (resumeDAO.hardDelete(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume?action=recycleBin&error=failed");
        }
    }

    /**
     * 加载简历的详细信息（关联数据）
     */
    private void loadResumeDetails(Resume resume) {
        if (resume == null || resume.getId() == null) {
            return;
        }

        Integer resumeId = resume.getId();

        // 加载教育经历
        List<ResumeEducation> educations = educationDAO.findByResumeId(resumeId);
        resume.setEducations(educations);

        // 加载技能
        List<ResumeSkill> skills = skillDAO.findByResumeId(resumeId);
        resume.setSkills(skills);

        // 加载项目经历
        List<ResumeProject> projects = projectDAO.findByResumeId(resumeId);
        resume.setProjects(projects);

        // 加载获奖情况
        List<ResumeAward> awards = awardDAO.findByResumeId(resumeId);
        resume.setAwards(awards);
    }
}
