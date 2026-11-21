package com.softwaregroup.project.service;

import com.softwaregroup.project.dao.ProjectDAO;
import com.softwaregroup.project.dao.UserDAO;
import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.ProjectMemberApplication;
import com.softwaregroup.project.model.User;
import com.softwaregroup.project.model.dto.ProjectDTO;
import com.softwaregroup.project.model.dto.ProjectFilterDTO;
import com.softwaregroup.project.model.dto.PlanDTO;
import com.softwaregroup.project.model.dto.ProgressDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 项目服务层
 */
@Service
public class ProjectService {

    public static final String STATUS_PENDING = Project.STATUS_PENDING;
    public static final String STATUS_APPROVED = Project.STATUS_APPROVED;
    public static final String STATUS_IN_PROGRESS = Project.STATUS_IN_PROGRESS;
    public static final String STATUS_COMPLETED = Project.STATUS_COMPLETED;

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_YEARLY_PROJECTS = 3;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private UserDAO userDAO;

    public ProjectService() {
    }

    public ProjectService(ProjectDAO projectDAO, UserDAO userDAO) {
        this.projectDAO = projectDAO;
        this.userDAO = userDAO;
    }

    public String getStatusPending() { return STATUS_PENDING; }
    public String getStatusApproved() { return STATUS_APPROVED; }
    public String getStatusInProgress() { return STATUS_IN_PROGRESS; }
    public String getStatusCompleted() { return STATUS_COMPLETED; }

    @Transactional
    public Result createProject(ProjectDTO dto, Integer userId) {
        Result validationResult = validateCreateProjectInput(dto, userId);
        if (validationResult != null) {
            return validationResult;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        int year = dto.getYear() != null ? dto.getYear() : Calendar.getInstance().get(Calendar.YEAR);
        int projectCount = projectDAO.countProjectsByMemberAndYear(userId, year);
        if (projectCount >= MAX_YEARLY_PROJECTS) {
            return Result.error(400, "每年最多参与" + MAX_YEARLY_PROJECTS + "个项目");
        }

        Project project = buildProjectFromDTO(dto, userId, year);
        boolean inserted = projectDAO.insert(project);
        if (!inserted) {
            return Result.error(500, "创建项目失败");
        }

        if (project.getId() == null) {
            return Result.error(500, "创建项目未返回ID");
        }
        projectDAO.addMember(project.getId(), userId, "LEADER", userId);
        projectDAO.addHistory(project.getId(), "CREATE", userId, "创建项目", "", null, null);

        return Result.ok(project);
    }

    private Result validateCreateProjectInput(ProjectDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "请求参数不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error(400, "项目名称不能为空");
        }
        if (dto.getName().length() > MAX_NAME_LENGTH) {
            return Result.error(400, "项目名称不能超过" + MAX_NAME_LENGTH + "字符");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return Result.error(400, "项目描述不能超过" + MAX_DESCRIPTION_LENGTH + "字符");
        }
        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            return Result.error(400, "项目分类不能为空");
        }
        if (dto.getCategory().length() > 50) {
            return Result.error(400, "项目分类不能超过50字符");
        }
        if (dto.getExpectedStartDate() != null && dto.getExpectedEndDate() != null
                && dto.getExpectedStartDate().after(dto.getExpectedEndDate())) {
            return Result.error(400, "开始时间不能晚于结束时间");
        }
        if (dto.getBudget() != null && dto.getBudget().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error(400, "预算不能为负数");
        }
        if (dto.getRepoUrl() != null && !dto.getRepoUrl().trim().isEmpty()
                && !isValidRepoUrl(dto.getRepoUrl())) {
            return Result.error(400, "仓库地址格式不正确");
        }
        return null;
    }

    private Project buildProjectFromDTO(ProjectDTO dto, Integer userId, int year) {
        Project project = new Project();
        project.setName(dto.getName().trim());
        project.setDescription(dto.getDescription());
        project.setCategory(dto.getCategory());
        project.setYear(year);
        project.setStatus(STATUS_PENDING);
        project.setLeaderId(userId);
        project.setAdminId(userId);
        project.setBudget(dto.getBudget());
        project.setRepoUrl(dto.getRepoUrl());
        project.setExpectedStartDate(dto.getExpectedStartDate());
        project.setExpectedEndDate(dto.getExpectedEndDate());
        project.setCreatedAt(new Date());
        project.setDeleted(0);
        return project;
    }

    public Result updateProject(Integer projectId, ProjectDTO dto, Integer userId) {
        Result validationResult = validateUpdateProjectInput(projectId, dto, userId);
        if (validationResult != null) {
            return validationResult;
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error(400, "项目名称不能为空");
        }
        if (STATUS_COMPLETED.equals(project.getStatus())) {
            return Result.error(400, "已完成项目无法修改");
        }
        if (STATUS_IN_PROGRESS.equals(project.getStatus())) {
            return Result.error(400, "进行中项目无法修改");
        }

        if (!project.getLeaderId().equals(userId) && !project.getAdminId().equals(userId)) {
            return Result.error(403, "无权限");
        }

        applyDTOToProject(project, dto);

        boolean updated = projectDAO.update(project, userId);
        if (!updated) {
            return Result.error(500, "更新项目失败");
        }

        return Result.ok(project);
    }

    private Result validateUpdateProjectInput(Integer projectId, ProjectDTO dto, Integer userId) {
        if (projectId == null) {
            return Result.error(400, "项目ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "请求参数不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return validateCreateProjectInput(dto, userId);
    }

    private void applyDTOToProject(Project project, ProjectDTO dto) {
        project.setName(dto.getName().trim());
        project.setDescription(dto.getDescription());
        project.setCategory(dto.getCategory());
        project.setBudget(dto.getBudget());
        project.setRepoUrl(dto.getRepoUrl());
        project.setExpectedStartDate(dto.getExpectedStartDate());
        project.setExpectedEndDate(dto.getExpectedEndDate());
        project.setUpdatedAt(new Date());
    }

    public Result deleteProject(Integer projectId, Integer userId) {
        if (projectId == null) {
            return Result.error(400, "项目ID不能为空");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (STATUS_COMPLETED.equals(project.getStatus())) {
            return Result.error(400, "已完成项目无法删除");
        }
        if (STATUS_IN_PROGRESS.equals(project.getStatus())) {
            return Result.error(400, "进行中项目无法删除");
        }

        if (!project.getLeaderId().equals(userId) && !project.getAdminId().equals(userId)) {
            return Result.error(403, "无权限");
        }

        boolean deleted = projectDAO.delete(projectId, userId);
        if (!deleted) {
            return Result.error(500, "删除项目失败");
        }

        return Result.ok();
    }

    public Result approveProject(Integer projectId, Integer operatorId) {
        Result validationResult = validateProjectApprovalInput(projectId, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (!STATUS_PENDING.equals(project.getStatus())) {
            return Result.error(400, "项目已审核");
        }

        Result adminCheckResult = checkAdminPermission(operatorId);
        if (adminCheckResult != null) {
            return adminCheckResult;
        }

        boolean approved = projectDAO.approve(projectId, operatorId, null);
        if (!approved) {
            return Result.error(500, "审批失败");
        }

        return Result.ok();
    }

    public Result rejectProject(Integer projectId, String reason, Integer operatorId) {
        Result validationResult = validateProjectRejectionInput(projectId, reason, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (!STATUS_PENDING.equals(project.getStatus())) {
            return Result.error(400, "项目已审核");
        }

        Result adminCheckResult = checkAdminPermission(operatorId);
        if (adminCheckResult != null) {
            return adminCheckResult;
        }

        boolean rejected = projectDAO.reject(projectId, operatorId, null);
        if (!rejected) {
            return Result.error(500, "驳回失败");
        }

        return Result.ok();
    }

    private Result validateProjectApprovalInput(Integer projectId, Integer operatorId) {
        if (projectId == null) {
            return Result.error(400, "项目ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private Result validateProjectRejectionInput(Integer projectId, String reason, Integer operatorId) {
        if (projectId == null) {
            return Result.error(400, "项目ID不能为空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(400, "驳回原因不能为空");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            return Result.error(400, "驳回原因不能超过" + MAX_REASON_LENGTH + "字符");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private Result checkAdminPermission(Integer operatorId) {
        User operator = userDAO.findById(operatorId);
        if (operator == null) {
            return Result.error(404, "用户不存在");
        }
        if (!"ADMIN".equals(operator.getRole())) {
            return Result.error(403, "无权限");
        }
        return null;
    }

    public Result applyMember(Integer projectId, Integer userId, String reason) {
        if (projectId == null) {
            return Result.error(400, "项目ID不能为空");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (!STATUS_APPROVED.equals(project.getStatus())) {
            return Result.error(400, "项目未通过审核");
        }
        if (project.getLeaderId().equals(userId)) {
            return Result.error(400, "负责人不能申请加入自己的项目");
        }
        if (projectDAO.isMember(projectId, userId)) {
            return Result.error(400, "已是成员");
        }
        if (projectDAO.hasPendingApplication(projectId, userId)) {
            return Result.error(400, "已提交申请");
        }

        boolean applied = projectDAO.applyMember(projectId, userId, reason, project.getLeaderId());
        if (!applied) {
            return Result.error(500, "申请失败");
        }

        return Result.ok();
    }

    public Result approveMember(Integer applicationId, Integer operatorId) {
        if (applicationId == null) {
            return Result.error(400, "申请ID不能为空");
        }

        ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
        if (app == null) {
            return Result.error(404, "申请不存在");
        }
        if (!"PENDING".equals(app.getStatus())) {
            return Result.error(400, "申请非待审核状态");
        }

        Project project = projectDAO.findById(app.getProjectId());
        if (project == null) {
            return Result.error(404, "项目不存在");
        }

        if (operatorId == null || (!operatorId.equals(project.getLeaderId())
                && !operatorId.equals(project.getAdminId()))) {
            return Result.error(403, "无权审批此项目的成员申请");
        }

        boolean approved = projectDAO.approveMemberApplication(applicationId, operatorId, null);
        if (!approved) {
            return Result.error(500, "审批失败");
        }

        return Result.ok();
    }

    public Result rejectMember(Integer applicationId, String reason, Integer operatorId) {
        if (applicationId == null) {
            return Result.error(400, "申请ID不能为空");
        }

        ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
        if (app == null) {
            return Result.error(404, "申请不存在");
        }

        Project project = projectDAO.findById(app.getProjectId());
        if (project == null) {
            return Result.error(404, "项目不存在");
        }

        if (operatorId == null || (!operatorId.equals(project.getLeaderId())
                && !operatorId.equals(project.getAdminId()))) {
            return Result.error(403, "无权审批此项目的成员申请");
        }

        boolean rejected = projectDAO.rejectMemberApplication(applicationId, operatorId, reason, null);
        if (!rejected) {
            return Result.error(500, "驳回失败");
        }

        return Result.ok();
    }

    public Result addPlan(Integer projectId, PlanDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "计划信息不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(400, "计划标题不能为空");
        }
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return Result.error(400, "计划开始和结束时间不能为空");
        }
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getStartDate().after(dto.getEndDate())) {
            return Result.error(400, "计划开始时间不能晚于结束时间");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (!STATUS_APPROVED.equals(project.getStatus())) {
            return Result.error(400, "当前状态不允许添加计划");
        }
        if (!projectDAO.isMember(projectId, userId)) {
            return Result.error(403, "无权限");
        }

        boolean added = projectDAO.addPlan(projectId, dto);
        if (!added) {
            return Result.error(500, "添加计划失败");
        }

        return Result.ok();
    }

    public Result addProgress(Integer projectId, ProgressDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "进度信息不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(400, "进度标题不能为空");
        }
        if (dto.getCompletionRate() == null) {
            return Result.error(400, "完成率不能为空");
        }
        if (dto.getCompletionRate() < 0 || dto.getCompletionRate() > 100) {
            return Result.error(400, "完成率必须在0-100之间");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }
        if (!projectDAO.isMember(projectId, userId)) {
            return Result.error(403, "无权限");
        }

        boolean added = projectDAO.addProgress(projectId, dto, userId);
        if (!added) {
            return Result.error(500, "添加进度失败");
        }

        return Result.ok();
    }

    public Result listProjects(ProjectFilterDTO filter, int page, int pageSize,
                               com.softwaregroup.common.security.CurrentUser user) {
        String keyword = filter != null ? filter.getKeyword() : null;
        String status = filter != null && filter.getStatus() != null
                ? filter.getStatus().trim().toLowerCase(java.util.Locale.ROOT) : null;
        Integer year = filter != null ? filter.getYear() : null;

        int size = normalizePageSize(pageSize);
        boolean publicOnly = user == null || !"ADMIN".equals(user.role());
        try {
            int total = projectDAO.countByConditions(keyword, status, year, publicOnly);
            int currentPage = clampPage(page, size, total);
            List<Project> projects = total == 0 ? List.of()
                    : projectDAO.findPageByConditions(keyword, status, year, publicOnly,
                    size, (currentPage - 1) * size);
            return Result.ok(projectPage(projects, total, currentPage, size));
        } catch (RuntimeException e) {
            return Result.error(500, "查询项目失败");
        }
    }

    private int normalizePageSize(int pageSize) {
        return pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private int clampPage(int page, int pageSize, int total) {
        int pages = total == 0 ? 0 : (total - 1) / pageSize + 1;
        return pages == 0 ? 1 : Math.min(Math.max(page, 1), pages);
    }

    private Map<String, Object> projectPage(List<Project> list, int total, int page, int pageSize) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("totalPages", total == 0 ? 0 : (total - 1) / pageSize + 1);
        return data;
    }

    public Result getProjectDetail(Integer projectId, Integer userId, boolean isAdmin) {
        if (projectId == null || projectId <= 0) {
            return Result.error(400, "项目ID不能为空");
        }

        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return Result.error(404, "项目不存在");
        }

        if (!isAdmin && !STATUS_APPROVED.equals(project.getStatus()) && !STATUS_IN_PROGRESS.equals(project.getStatus())
                && !STATUS_COMPLETED.equals(project.getStatus())
                && !java.util.Objects.equals(project.getLeaderId(), userId)
                && !java.util.Objects.equals(project.getAdminId(), userId)) {
            return Result.error(403, "无权限");
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("project", project);
        detail.put("members", projectDAO.getProjectMembers(projectId));
        detail.put("labels", projectDAO.getLabels(projectId));
        detail.put("plans", projectDAO.getPlans(projectId));
        detail.put("progress", projectDAO.getProgressList(projectId));
        detail.put("history", projectDAO.getHistory(projectId));
        if (userId != null) {
            boolean member = projectDAO.isMember(projectId, userId);
            detail.put("isMember", member);
            detail.put("currentUserRole", isAdmin || java.util.Objects.equals(project.getLeaderId(), userId)
                    || java.util.Objects.equals(project.getAdminId(), userId) ? "ADMIN" : member ? "MEMBER" : null);
        }

        return Result.ok(detail);
    }

    public Result getMyProjects(Integer userId, int page, int pageSize) {
        if (userId == null || userId <= 0) {
            return Result.error(400, "用户ID不能为空");
        }
        int size = normalizePageSize(pageSize);
        try {
            int total = projectDAO.countProjectsByUserId(userId);
            int currentPage = clampPage(page, size, total);
            List<Project> projects = total == 0 ? List.of()
                    : projectDAO.findPageByUserId(userId, size, (currentPage - 1) * size);
            return Result.ok(projectPage(projects, total, currentPage, size));
        } catch (RuntimeException e) {
            return Result.error(500, "查询我的项目失败");
        }
    }

    public Result getCreatedByMeProjects(Integer userId, int page, int pageSize) {
        if (userId == null || userId <= 0) {
            return Result.error(400, "用户ID不能为空");
        }
        int size = normalizePageSize(pageSize);
        try {
            int total = projectDAO.countProjectsByLeaderId(userId);
            int currentPage = clampPage(page, size, total);
            List<Project> projects = total == 0 ? List.of()
                    : projectDAO.findPageByLeaderId(userId, size, (currentPage - 1) * size);
            return Result.ok(projectPage(projects, total, currentPage, size));
        } catch (RuntimeException e) {
            return Result.error(500, "查询我负责的项目失败");
        }
    }

    private boolean isValidRepoUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true;
        }
        return Pattern.matches("^https?://.*", url);
    }
}
