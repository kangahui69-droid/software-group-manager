package servlet;

import dao.ResumeAwardDAO;
import dao.ResumeDAO;
import model.Resume;
import model.ResumeAward;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 简历获奖情况管理控制器
 * 映射路径: /resume/award
 */
@WebServlet(name = "ResumeAwardServlet", urlPatterns = {"/resume/award"})
public class ResumeAwardServlet extends HttpServlet {

    private ResumeAwardDAO awardDAO = new ResumeAwardDAO();
    private ResumeDAO resumeDAO = new ResumeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 禁用缓存，防止resumeId被缓存
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        String resumeIdParam = req.getParameter("resumeId");
        System.out.println("[DEBUG] doGet: action=" + action + ", resumeIdParam=" + resumeIdParam);
        Integer resumeId = parseInt(resumeIdParam);

        if (resumeId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        if ("list".equals(action) || action == null) {
            listAwards(req, resp, resumeId, resume);
        } else if ("create".equals(action)) {
            showCreateForm(req, resp, resumeId, resume);
        } else if ("edit".equals(action)) {
            showEditForm(req, resp, resumeId, resume);
        } else if ("delete".equals(action)) {
            deleteAward(req, resp, resumeId);
        } else {
            listAwards(req, resp, resumeId, resume);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        String resumeIdParam = req.getParameter("resumeId");
        System.out.println("[DEBUG] doPost: action=" + action + ", resumeIdParam=" + resumeIdParam);
        Integer resumeId = parseInt(resumeIdParam);

        if (resumeId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(user.getId())) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        if ("create".equals(action)) {
            createAward(req, resp, resumeId);
        } else if ("update".equals(action)) {
            updateAward(req, resp, resumeId);
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId);
        }
    }

    private void listAwards(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        List<ResumeAward> awards = awardDAO.findByResumeId(resumeId);
        req.setAttribute("awards", awards);
        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-award-list.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        // 强制刷新，防止resumeId被缓存
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        System.out.println("[DEBUG] showCreateForm: resumeId=" + resumeId + ", resumeName=" + (resume != null ? resume.getResumeName() : "null"));
        req.getRequestDispatcher("/member/resume-award-edit.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, Integer resumeId, Resume resume)
            throws ServletException, IOException {
        Integer awardId = parseInt(req.getParameter("id"));
        if (awardId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId);
            return;
        }

        ResumeAward award = awardDAO.findById(awardId);
        if (award == null || !award.getResumeId().equals(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&error=notfound");
            return;
        }

        req.setAttribute("award", award);
        req.setAttribute("resumeId", resumeId);
        req.setAttribute("resume", resume);
        req.getRequestDispatcher("/member/resume-award-edit.jsp").forward(req, resp);
    }

    private void createAward(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        try {
            ResumeAward award = buildAwardFromRequest(req, resumeId);

            if (awardDAO.save(award)) {
                resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&success=created");
            } else {
                resp.sendRedirect(req.getContextPath() + "/resume/award?action=create&resumeId=" + resumeId + "&error=failed");
            }
        } catch (IllegalArgumentException e) {
            // 参数验证失败，返回错误信息
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=create&resumeId=" + resumeId + "&error=" + e.getMessage());
        }
    }

    private void updateAward(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        Integer awardId = parseInt(req.getParameter("id"));
        if (awardId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId);
            return;
        }

        try {
            // 先查询原有记录，保留 award_id 和 is_from_system
            ResumeAward existingAward = awardDAO.findById(awardId);
            if (existingAward == null) {
                resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&error=notfound");
                return;
            }

            ResumeAward award = buildAwardFromRequest(req, resumeId);
            award.setId(awardId);
            // 保留原有的 award_id 和 is_from_system 值
            award.setAwardId(existingAward.getAwardId());
            award.setIsFromSystem(existingAward.getIsFromSystem());

            if (awardDAO.update(award)) {
                resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&success=updated");
            } else {
                resp.sendRedirect(req.getContextPath() + "/resume/award?action=edit&resumeId=" + resumeId + "&id=" + awardId + "&error=failed");
            }
        } catch (IllegalArgumentException e) {
            // 参数验证失败，返回错误信息
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=edit&resumeId=" + resumeId + "&id=" + awardId + "&error=" + e.getMessage());
        }
    }

    private void deleteAward(HttpServletRequest req, HttpServletResponse resp, Integer resumeId)
            throws ServletException, IOException {
        Integer awardId = parseInt(req.getParameter("id"));
        if (awardId == null) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId);
            return;
        }

        ResumeAward award = awardDAO.findById(awardId);
        if (award == null || !award.getResumeId().equals(resumeId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&error=notfound");
            return;
        }

        if (awardDAO.delete(awardId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/award?action=list&resumeId=" + resumeId + "&error=failed");
        }
    }

    private ResumeAward buildAwardFromRequest(HttpServletRequest req, Integer resumeId) {
        ResumeAward award = new ResumeAward();
        award.setResumeId(resumeId);

        // 必填字段：奖项名称
        String awardName = req.getParameter("awardName");
        if (awardName == null || awardName.trim().isEmpty()) {
            throw new IllegalArgumentException("奖项名称不能为空");
        }
        award.setAwardName(awardName.trim());

        // 可选字段
        String competitionName = req.getParameter("competitionName");
        award.setCompetitionName(competitionName != null ? competitionName.trim() : null);

        String awardLevel = req.getParameter("awardLevel");
        award.setAwardLevel(awardLevel != null && !awardLevel.isEmpty() ? awardLevel : null);

        // 必填字段：获奖时间
        String awardDateStr = req.getParameter("awardDate");
        if (awardDateStr == null || awardDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("获奖时间不能为空");
        }
        java.sql.Date awardDate = parseDate(awardDateStr);
        if (awardDate == null) {
            throw new IllegalArgumentException("获奖时间格式错误，请使用YYYY-MM-DD格式");
        }
        award.setAwardDate(awardDate);

        String awardOrg = req.getParameter("awardOrg");
        award.setAwardOrg(awardOrg != null ? awardOrg.trim() : null);

        String description = req.getParameter("description");
        award.setDescription(description != null ? description.trim() : null);

        // 显示顺序，默认为0
        Integer displayOrder = parseInt(req.getParameter("displayOrder"));
        award.setDisplayOrder(displayOrder != null ? displayOrder : 0);

        // 设置默认值
        award.setIsFromSystem(0);
        award.setAwardId(null);

        return award;
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
            System.err.println("[ResumeAwardServlet.parseDate] 日期解析失败: value=" + value + ", error=" + e.getMessage());
            return null;
        }
    }
}
