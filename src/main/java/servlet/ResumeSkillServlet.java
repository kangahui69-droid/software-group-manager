package servlet;

import dao.ResumeSkillDAO;
import dao.ResumeDAO;
import model.ResumeSkill;
import model.Resume;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 简历-技能特长控制器
 * 映射路径: /resume/skill
 */
@WebServlet(name = "ResumeSkillServlet", urlPatterns = {"/resume/skill"})
public class ResumeSkillServlet extends HttpServlet {

    private ResumeSkillDAO skillDAO = new ResumeSkillDAO();
    private ResumeDAO resumeDAO = new ResumeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        if ("list".equals(action)) {
            listSkills(req, resp, user.getId());
        } else if ("create".equals(action)) {
            showCreateForm(req, resp, user.getId());
        } else if ("edit".equals(action)) {
            showEditForm(req, resp, user.getId());
        } else if ("delete".equals(action)) {
            deleteSkill(req, resp, user.getId());
        } else {
            listSkills(req, resp, user.getId());
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
        if ("create".equals(action)) {
            createSkill(req, resp, user.getId());
        } else if ("update".equals(action)) {
            updateSkill(req, resp, user.getId());
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/skill?action=list");
        }
    }

    private void listSkills(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId;
        try {
            resumeId = Integer.parseInt(resumeIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=invalid_id");
            return;
        }
        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        List<ResumeSkill> skills = skillDAO.findByResumeId(resumeId);
        req.setAttribute("resume", resume);
        req.setAttribute("skills", skills);
        req.getRequestDispatcher("/member/resume-skill-list.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId;
        try {
            resumeId = Integer.parseInt(resumeIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=invalid_id");
            return;
        }

        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        req.setAttribute("resume", resume);
        req.setAttribute("action", "create");
        req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer skillId;
        try {
            skillId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=invalid_id");
            return;
        }

        ResumeSkill skill = skillDAO.findById(skillId);
        if (skill == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        Resume resume = resumeDAO.findById(skill.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        req.setAttribute("resume", resume);
        req.setAttribute("skill", skill);
        req.setAttribute("action", "edit");
        req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
    }

    private void createSkill(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String resumeIdStr = req.getParameter("resumeId");
        if (resumeIdStr == null || resumeIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer resumeId;
        try {
            resumeId = Integer.parseInt(resumeIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=invalid_id");
            return;
        }
        Resume resume = resumeDAO.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        ResumeSkill skill = new ResumeSkill();
        skill.setResumeId(resumeId);
        skill.setSkillName(req.getParameter("skillName"));
        skill.setCategory(req.getParameter("category"));
        skill.setProficiency(req.getParameter("proficiency"));

        String proficiencyScoreStr = req.getParameter("proficiencyScore");
        if (proficiencyScoreStr != null && !proficiencyScoreStr.isEmpty()) {
            try {
                skill.setProficiencyScore(Integer.parseInt(proficiencyScoreStr));
            } catch (NumberFormatException e) {
                // 如果分数格式无效，设为null
                skill.setProficiencyScore(null);
            }
        }

        skill.setDescription(req.getParameter("description"));

        // 处理显示顺序
        String displayOrderStr = req.getParameter("displayOrder");
        if (displayOrderStr != null && !displayOrderStr.isEmpty()) {
            try {
                skill.setDisplayOrder(Integer.parseInt(displayOrderStr));
            } catch (NumberFormatException e) {
                // 如果转换失败，使用默认值0
                skill.setDisplayOrder(0);
            }
        } else {
            skill.setDisplayOrder(0);
        }

        // 验证技能数据
        List<String> errors = new ArrayList<>();
        if (!validateSkill(skill, errors)) {
            // 验证失败，返回编辑页面并显示错误信息
            req.setAttribute("resume", resume);
            req.setAttribute("skill", skill);
            req.setAttribute("action", "create");
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
            return;
        }

        try {
            if (skillDAO.save(skill)) {
                resp.sendRedirect(req.getContextPath() + "/resume/skill?action=list&resumeId=" + resumeId + "&success=created");
            } else {
                req.setAttribute("resume", resume);
                req.setAttribute("skill", skill);
                req.setAttribute("action", "create");
                req.setAttribute("error", "保存失败，请稍后重试");
                req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            // 处理数据库异常
            req.setAttribute("resume", resume);
            req.setAttribute("skill", skill);
            req.setAttribute("action", "create");
            req.setAttribute("error", "系统错误: " + e.getMessage());
            req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
        }
    }

    private void updateSkill(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }
        Integer skillId = Integer.parseInt(idStr);
        ResumeSkill skill = skillDAO.findById(skillId);
        if (skill == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        Resume resume = resumeDAO.findById(skill.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }

        skill.setSkillName(req.getParameter("skillName"));
        skill.setCategory(req.getParameter("category"));
        skill.setProficiency(req.getParameter("proficiency"));

        String proficiencyScoreStr = req.getParameter("proficiencyScore");
        if (proficiencyScoreStr != null && !proficiencyScoreStr.isEmpty()) {
            try {
                skill.setProficiencyScore(Integer.parseInt(proficiencyScoreStr));
            } catch (NumberFormatException e) {
                // 如果分数格式无效，设为null
                skill.setProficiencyScore(null);
            }
        }

        skill.setDescription(req.getParameter("description"));

        // 处理显示顺序
        String displayOrderStr = req.getParameter("displayOrder");
        if (displayOrderStr != null && !displayOrderStr.isEmpty()) {
            try {
                skill.setDisplayOrder(Integer.parseInt(displayOrderStr));
            } catch (NumberFormatException e) {
                // 如果转换失败，使用当前值
                skill.setDisplayOrder(skill.getDisplayOrder() != null ? skill.getDisplayOrder() : 0);
            }
        }

        // 验证技能数据
        List<String> errors = new ArrayList<>();
        if (!validateSkill(skill, errors)) {
            // 验证失败，返回编辑页面并显示错误信息
            req.setAttribute("resume", resume);
            req.setAttribute("skill", skill);
            req.setAttribute("action", "edit");
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
            return;
        }

        try {
            if (skillDAO.update(skill)) {
                resp.sendRedirect(req.getContextPath() + "/resume/skill?action=list&resumeId=" + skill.getResumeId() + "&success=updated");
            } else {
                req.setAttribute("resume", resume);
                req.setAttribute("skill", skill);
                req.setAttribute("action", "edit");
                req.setAttribute("error", "更新失败，请稍后重试");
                req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            // 处理数据库异常
            req.setAttribute("resume", resume);
            req.setAttribute("skill", skill);
            req.setAttribute("action", "edit");
            req.setAttribute("error", "系统错误: " + e.getMessage());
            req.getRequestDispatcher("/member/resume-skill-edit.jsp").forward(req, resp);
        }
    }

    /**
     * 验证技能数据
     * @param skill 技能对象
     * @param errors 错误列表，用于收集错误信息
     * @return 验证是否通过
     */
    private boolean validateSkill(ResumeSkill skill, List<String> errors) {
        boolean isValid = true;

        // 验证技能名称
        String skillName = skill.getSkillName();
        if (skillName == null || skillName.trim().isEmpty()) {
            errors.add("技能名称不能为空");
            isValid = false;
        } else if (skillName.length() > 100) {
            errors.add("技能名称不能超过100个字符");
            isValid = false;
        }

        // 验证熟练程度
        String proficiency = skill.getProficiency();
        Set<String> validProficiencies = new HashSet<>();
        validProficiencies.add("beginner");
        validProficiencies.add("elementary");
        validProficiencies.add("intermediate");
        validProficiencies.add("advanced");
        validProficiencies.add("expert");

        if (proficiency == null || proficiency.trim().isEmpty()) {
            errors.add("请选择熟练程度");
            isValid = false;
        } else if (!validProficiencies.contains(proficiency)) {
            errors.add("无效的熟练程度等级");
            isValid = false;
        }

        // 验证熟练度分数（如果提供）
        Integer proficiencyScore = skill.getProficiencyScore();
        if (proficiencyScore != null) {
            if (proficiencyScore < 1 || proficiencyScore > 100) {
                errors.add("熟练度分数必须在1-100之间");
                isValid = false;
            }
        }

        // 验证技能描述长度
        String description = skill.getDescription();
        if (description != null && description.length() > 500) {
            errors.add("技能描述不能超过500个字符");
            isValid = false;
        }

        return isValid;
    }

    private void deleteSkill(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list");
            return;
        }

        Integer skillId;
        try {
            skillId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=invalid_id");
            return;
        }

        ResumeSkill skill = skillDAO.findById(skillId);
        if (skill == null) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        Resume resume = resumeDAO.findById(skill.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId)) {
            resp.sendRedirect(req.getContextPath() + "/resume?action=list&error=notfound");
            return;
        }
        Integer resumeId = skill.getResumeId();
        if (skillDAO.delete(skillId)) {
            resp.sendRedirect(req.getContextPath() + "/resume/skill?action=list&resumeId=" + resumeId + "&success=deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/resume/skill?action=list&resumeId=" + resumeId + "&error=failed");
        }
    }
}
