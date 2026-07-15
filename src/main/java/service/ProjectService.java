package service;

import dao.DictionaryDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dto.PlanDTO;
import dto.ProgressDTO;
import dto.ProjectDTO;
import dto.ProjectFilterDTO;
import model.Project;
import model.ProjectMemberApplication;
import model.ProjectPlan;
import model.ProjectProgress;
import model.User;
import util.Result;
import util.TransactionTemplate;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectService {

    // ==================== 响应码 ====================
    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_FORBIDDEN = 403;
    private static final int CODE_INTERNAL_ERROR = 500;

    // ==================== 项目状态 ====================
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_IN_PROGRESS = "in_progress";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";
    private static final String STATUS_REJECTED = "rejected";

    // ==================== 申请状态 ====================
    private static final String APP_STATUS_PENDING = "PENDING";

    // ==================== 成员角色 ====================
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // ==================== 文件类型 ====================
    private static final String FILE_TYPE_DOC = "DOC";
    private static final String FILE_TYPE_IMAGE = "IMAGE";
    private static final String FILE_TYPE_CODE = "CODE";

    // ==================== 历史操作类型 ====================
    private static final String OP_PROJECT_APPLY = "PROJECT_APPLY";
    private static final String OP_PROJECT_UPDATE = "PROJECT_UPDATE";
    private static final String OP_PROJECT_APPROVE = "PROJECT_APPROVE";
    private static final String OP_PROJECT_REJECT = "PROJECT_REJECT";
    private static final String OP_MEMBER_APPLY = "MEMBER_APPLY";
    private static final String OP_MEMBER_APPROVE = "MEMBER_APPROVE";
    private static final String OP_MEMBER_REJECT = "MEMBER_REJECT";
    private static final String OP_INFO_UPDATE = "PROJECT_INFO_UPDATE";
    private static final String OP_TRANSFER = "PROJECT_TRANSFER";
    private static final String OP_LABEL_ADD = "PROJECT_LABEL_ADD";
    private static final String OP_LABEL_REMOVE = "PROJECT_LABEL_REMOVE";
    private static final String OP_IMAGE_ADD = "PROJECT_IMAGE_ADD";
    private static final String OP_FILE_DELETE = "PROJECT_FILE_DELETE";

    // ==================== 限制常量 ====================
    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_REJECT_REASON_LENGTH = 500;
    private static final int MAX_PLAN_TITLE_LENGTH = 200;
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_PROJECTS_PER_YEAR = 3;

    // ==================== DAO ====================
    private ProjectDAO projectDAO = new ProjectDAO();
    private UserDAO userDAO = new UserDAO();
    private FileService fileService;
    private DictionaryDAO dictionaryDAO = new DictionaryDAO();

    // ==================== Setter 注入（Mockito @InjectMocks 用）====================

    public void setProjectDAO(ProjectDAO projectDAO) { this.projectDAO = projectDAO; }
    public void setUserDAO(UserDAO userDAO) { this.userDAO = userDAO; }
    public void setFileService(FileService fileService) { this.fileService = fileService; }
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO) { this.dictionaryDAO = dictionaryDAO; }

    // ==================== 通用工具方法 ====================

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Result badRequest(String message) {
        return Result.error(CODE_BAD_REQUEST, message);
    }

    private Result notFound(String message) {
        return Result.error(CODE_NOT_FOUND, message);
    }

    private Result forbidden(String message) {
        return Result.error(CODE_FORBIDDEN, message);
    }

    private Result serverError(String action) {
        return Result.error(CODE_INTERNAL_ERROR, action + "失败");
    }

    private Result serverError(String action, RuntimeException e) {
        return Result.error(CODE_INTERNAL_ERROR, action + "失败: " + e.getMessage());
    }

    private int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private boolean isValidHttpUrl(String url) {
        if (url == null || url.isEmpty()) return true;
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private int[] normalizePageParams(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
        if (pageSize > MAX_PAGE_SIZE) pageSize = MAX_PAGE_SIZE;
        return new int[]{page, pageSize};
    }

    /**
     * 执行事务操作，统一异常处理为 serverError。
     * lambda 内可直接调用 DAO 的 Connection 重载方法。
     */
    private Result executeInTransaction(String actionName, Function<Connection, Result> action) {
        try {
            return TransactionTemplate.executeWithConnection(action::apply);
        } catch (RuntimeException e) {
            return serverError(actionName, e);
        }
    }

    // ==================== 权限辅助方法 ====================

    private boolean isGlobalAdmin(Integer userId) {
        if (userId == null) return false;
        User u = userDAO.findById(userId);
        return u != null && ROLE_ADMIN.equals(u.getRole());
    }

    private boolean isProjectAdmin(Project p, Integer userId) {
        return userId != null
                && (userId.equals(p.getLeaderId()) || userId.equals(p.getAdminId()));
    }

    private boolean canManageProject(Project p, Integer userId) {
        return isProjectAdmin(p, userId) || isGlobalAdmin(userId);
    }

    private boolean isMemberOrAdmin(Integer projectId, Project p, Integer userId) {
        if (userId == null) return false;
        return projectDAO.isMember(projectId, userId) || canManageProject(p, userId);
    }

    private String resolveOperatorName(Integer userId) {
        if (userId == null) return "";
        User u = userDAO.findById(userId);
        return u != null && u.getName() != null ? u.getName() : "";
    }

    // ==================== 文件上传反射工具 ====================

    private static class PartInfo {
        final long size;
        final String contentType;
        final String fileName;

        PartInfo(long size, String contentType, String fileName) {
            this.size = size;
            this.contentType = contentType;
            this.fileName = fileName;
        }

        String fileNameOrEmpty() {
            return fileName != null ? fileName : "";
        }
    }

    private PartInfo extractPartInfo(Object part) {
        if (part == null) return null;
        try {
            long size = (long) invokeNoArg(part, "getSize");
            String contentType = (String) invokeNoArg(part, "getContentType");
            String fileName = (String) invokeNoArg(part, "getSubmittedFileName");
            return new PartInfo(size, contentType, fileName);
        } catch (Exception e) {
            return null;
        }
    }

    private Object invokeNoArg(Object target, String methodName) throws Exception {
        Method m = target.getClass().getMethod(methodName);
        return m.invoke(target);
    }

    // ==================== 历史记录辅助 ====================

    /**
     * 将操作历史写入当前事务连接（lambda 内调用）。
     */
    private void addHistory(Connection conn, Integer projectId, String opType,
                            Integer operatorId, String description,
                            String oldValue, String newValue) {
        projectDAO.addHistory(projectId, opType, operatorId,
                resolveOperatorName(operatorId),
                description != null ? description : "",
                oldValue, newValue, conn);
    }

    // ==================== 项目加载辅助 ====================

    private Result requireProjectId(Integer projectId) {
        return projectId == null ? badRequest("项目ID不能为空") : null;
    }

    private Project loadProject(Integer projectId) {
        return projectDAO.findById(projectId);
    }

    private Result requireFileServiceAvailable() {
        return fileService == null ? serverError("文件服务不可用") : null;
    }

    // ==================== createProject ====================

    public Result createProject(ProjectDTO dto, Integer userId) {
        Result validation = validateCreateProjectParams(dto, userId);
        if (validation != null) return validation;

        User user = userDAO.findById(userId);
        if (user == null) return notFound("用户不存在");

        int year = dto.getYear() != null ? dto.getYear() : currentYear();
        int existingCount = projectDAO.countProjectsByMemberAndYear(userId, year);
        if (existingCount >= MAX_PROJECTS_PER_YEAR) {
            return badRequest("每年最多参与" + MAX_PROJECTS_PER_YEAR + "个项目");
        }

        Project project = buildProjectFromDTO(dto, userId, year);

        try {
            if (!projectDAO.insert(project)) return serverError("创建项目");
        } catch (RuntimeException e) {
            return serverError("创建项目", e);
        }

        initializeProjectCreator(project.getId(), userId, project.getName());
        return Result.ok(project.getId());
    }

    private Result validateCreateProjectParams(ProjectDTO dto, Integer userId) {
        if (dto == null) return badRequest("请求参数不能为空");
        if (userId == null) return badRequest("用户ID不能为空");
        if (isBlank(dto.getName())) return badRequest("项目名称不能为空");
        if (dto.getName().length() > MAX_NAME_LENGTH)
            return badRequest("项目名称不能超过" + MAX_NAME_LENGTH + "字符");
        if (dto.getDescription() != null && dto.getDescription().length() > MAX_DESCRIPTION_LENGTH)
            return badRequest("项目描述不能超过" + MAX_DESCRIPTION_LENGTH + "字符");
        if (isBlank(dto.getCategory())) return badRequest("项目分类不能为空");
        Date start = dto.getExpectedStartDate();
        Date end = dto.getExpectedEndDate();
        if (start != null && end != null && start.after(end))
            return badRequest("预计开始时间不能晚于结束时间");
        if (dto.getBudget() != null && dto.getBudget().signum() < 0)
            return badRequest("项目预算不能为负数");
        if (!isValidHttpUrl(dto.getRepoUrl()))
            return badRequest("仓库地址格式不正确");
        return null;
    }

    private Project buildProjectFromDTO(ProjectDTO dto, Integer userId, int year) {
        Project project = new Project();
        project.setName(dto.getName().trim());
        project.setDescription(dto.getDescription());
        project.setCategory(dto.getCategory());
        project.setYear(year);
        project.setExpectedStartDate(dto.getExpectedStartDate());
        project.setExpectedEndDate(dto.getExpectedEndDate());
        project.setBudget(dto.getBudget());
        project.setRepoUrl(dto.getRepoUrl());
        project.setDocUrl(dto.getDocUrl());
        project.setStatus(STATUS_PENDING);
        project.setLeaderId(userId);
        project.setAdminId(userId);
        project.setDeleted(0);
        return project;
    }

    private void initializeProjectCreator(Integer projectId, Integer userId, String projectName) {
        try {
            TransactionTemplate.executeWithConnection(conn -> {
                projectDAO.addMember(projectId, userId, ROLE_ADMIN, conn);
                addHistory(conn, projectId, OP_PROJECT_APPLY, userId, "创建项目", null, projectName);
                return null;
            });
        } catch (RuntimeException ignored) {
        }
    }

    // ==================== updateProject ====================

    public Result updateProject(Integer id, ProjectDTO dto, Integer userId) {
        if (id == null) return badRequest("项目ID不能为空");
        if (dto == null) return badRequest("请求参数不能为空");

        Project project = loadProject(id);
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, userId)) return forbidden("无权修改此项目");

        Result statusCheck = validateProjectEditable(project.getStatus());
        if (statusCheck != null) return statusCheck;
        if (isBlank(dto.getName())) return badRequest("项目名称不能为空");

        String oldName = project.getName();
        applyProjectUpdates(project, dto);

        return executeInTransaction("更新项目", conn -> {
            if (!projectDAO.update(project, conn)) return serverError("更新项目");
            addHistory(conn, id, OP_PROJECT_UPDATE, userId, "更新项目信息", oldName, project.getName());
            return Result.ok(id);
        });
    }

    private Result validateProjectEditable(String status) {
        if (STATUS_COMPLETED.equals(status)) return badRequest("项目已完成，无法修改");
        if (STATUS_IN_PROGRESS.equals(status)) return badRequest("项目进行中，无法修改");
        if (STATUS_CANCELED.equals(status)) return badRequest("项目已取消，无法修改");
        if (STATUS_REJECTED.equals(status)) return badRequest("项目已驳回，无法修改");
        return null;
    }

    private void applyProjectUpdates(Project project, ProjectDTO dto) {
        project.setName(dto.getName().trim());
        project.setDescription(dto.getDescription());
        if (dto.getCategory() != null) project.setCategory(dto.getCategory());
        if (dto.getYear() != null) project.setYear(dto.getYear());
        if (dto.getExpectedStartDate() != null) project.setExpectedStartDate(dto.getExpectedStartDate());
        if (dto.getExpectedEndDate() != null) project.setExpectedEndDate(dto.getExpectedEndDate());
        if (dto.getBudget() != null) project.setBudget(dto.getBudget());
        if (dto.getRepoUrl() != null) project.setRepoUrl(dto.getRepoUrl());
        if (dto.getDocUrl() != null) project.setDocUrl(dto.getDocUrl());
    }

    // ==================== deleteProject ====================

    public Result deleteProject(Integer id, Integer userId) {
        if (id == null) return badRequest("项目ID不能为空");

        Project project = loadProject(id);
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, userId)) return forbidden("无权删除此项目");

        if (STATUS_COMPLETED.equals(project.getStatus()))
            return badRequest("项目已完成，无法删除");
        if (STATUS_IN_PROGRESS.equals(project.getStatus()))
            return badRequest("项目进行中，无法删除");

        return executeInTransaction("删除项目", conn ->
                projectDAO.delete(id, conn) ? Result.ok() : serverError("删除项目")
        );
    }

    // ==================== approveProject ====================

    public Result approveProject(Integer projectId, Integer operatorId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!STATUS_PENDING.equals(project.getStatus()))
            return badRequest("该项目已审核");
        if (!isGlobalAdmin(operatorId))
            return forbidden("需要管理员权限");

        return executeInTransaction("审核", conn -> {
            if (!projectDAO.approve(projectId, operatorId, conn)) return serverError("审核");
            addHistory(conn, projectId, OP_PROJECT_APPROVE, operatorId, "审核通过项目", STATUS_PENDING, STATUS_APPROVED);
            return Result.ok();
        });
    }

    // ==================== rejectProject ====================

    public Result rejectProject(Integer projectId, String reason, Integer operatorId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");

        if (isBlank(reason)) return badRequest("驳回原因不能为空");
        if (reason.length() > MAX_REJECT_REASON_LENGTH)
            return badRequest("驳回原因不能超过" + MAX_REJECT_REASON_LENGTH + "字符");

        if (!STATUS_PENDING.equals(project.getStatus()))
            return badRequest("该项目已审核");
        if (!isGlobalAdmin(operatorId))
            return forbidden("需要管理员权限");

        return executeInTransaction("驳回", conn -> {
            if (!projectDAO.reject(projectId, operatorId, conn)) return serverError("驳回");
            addHistory(conn, projectId, OP_PROJECT_REJECT, operatorId, "驳回项目: " + reason, STATUS_PENDING, STATUS_REJECTED);
            return Result.ok();
        });
    }

    // ==================== applyMember ====================

    public Result applyMember(Integer projectId, Integer userId, String reason) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");

        if (!STATUS_APPROVED.equals(project.getStatus()))
            return badRequest("项目未通过审核，无法申请加入");
        if (userId.equals(project.getLeaderId()))
            return badRequest("项目负责人不能申请加入自己的项目");
        if (projectDAO.isMember(projectId, userId))
            return badRequest("您已是成员");
        if (projectDAO.hasPendingApplication(projectId, userId))
            return badRequest("您已提交申请，请等待审核");

        String applyReason = isBlank(reason) ? "申请加入项目" : reason;

        return executeInTransaction("申请", conn -> {
            if (!projectDAO.applyMember(projectId, userId, applyReason, conn))
                return serverError("申请");
            addHistory(conn, projectId, OP_MEMBER_APPLY, userId, "申请加入项目", null, applyReason);
            return Result.ok();
        });
    }

    // ==================== approveMember ====================

    public Result approveMember(Integer applicationId, Integer operatorId) {
        if (applicationId == null) return badRequest("申请ID不能为空");

        ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
        if (app == null) return notFound("申请不存在");
        if (!APP_STATUS_PENDING.equals(app.getStatus()))
            return badRequest("该申请非待审核状态");

        Project project = loadProject(app.getProjectId());
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, operatorId)) return forbidden("无权审批此申请");

        return executeInTransaction("审批", conn -> {
            if (!projectDAO.approveMemberApplication(applicationId, operatorId, conn))
                return serverError("审批");
            addHistory(conn, app.getProjectId(), OP_MEMBER_APPROVE, operatorId, "审批通过成员申请", null, app.getUserName());
            return Result.ok();
        });
    }

    // ==================== rejectMember ====================

    public Result rejectMember(Integer applicationId, String reason, Integer operatorId) {
        if (applicationId == null) return badRequest("申请ID不能为空");

        ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
        if (app == null) return notFound("申请不存在");
        if (!APP_STATUS_PENDING.equals(app.getStatus()))
            return badRequest("该申请非待审核状态");

        Project project = loadProject(app.getProjectId());
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, operatorId)) return forbidden("无权审批此申请");

        String rejectReason = isBlank(reason) ? "不符合项目要求" : reason;

        return executeInTransaction("驳回", conn -> {
            if (!projectDAO.rejectMemberApplication(applicationId, operatorId, rejectReason, conn))
                return serverError("驳回");
            addHistory(conn, app.getProjectId(), OP_MEMBER_REJECT, operatorId, "驳回成员申请: " + rejectReason, null, app.getUserName());
            return Result.ok();
        });
    }

    // ==================== addPlan ====================

    public Result addPlan(Integer projectId, PlanDTO dto, Integer userId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;
        if (dto == null) return badRequest("请求参数不能为空");

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");

        if (STATUS_PENDING.equals(project.getStatus())
                || STATUS_CANCELED.equals(project.getStatus())
                || STATUS_REJECTED.equals(project.getStatus()))
            return badRequest("项目当前状态不允许添加计划");
        if (!isMemberOrAdmin(projectId, project, userId))
            return forbidden("无权添加计划");

        Result validation = validatePlanDTO(dto);
        if (validation != null) return validation;

        ProjectPlan plan = buildPlan(projectId, dto);

        return executeInTransaction("添加计划", conn -> {
            if (!projectDAO.addPlan(plan, conn)) return serverError("添加计划");
            addHistory(conn, projectId, OP_INFO_UPDATE, userId, "添加项目计划: " + plan.getTitle(), null, plan.getTitle());
            return Result.ok();
        });
    }

    private Result validatePlanDTO(PlanDTO dto) {
        if (isBlank(dto.getTitle())) return badRequest("计划标题不能为空");
        if (dto.getTitle().length() > MAX_PLAN_TITLE_LENGTH)
            return badRequest("计划标题不能超过" + MAX_PLAN_TITLE_LENGTH + "字符");
        Date start = dto.getStartDate();
        Date end = dto.getEndDate();
        if (start != null && end != null && start.after(end))
            return badRequest("计划开始时间不能晚于结束时间");
        return null;
    }

    private ProjectPlan buildPlan(Integer projectId, PlanDTO dto) {
        ProjectPlan plan = new ProjectPlan();
        plan.setProjectId(projectId);
        plan.setTitle(dto.getTitle().trim());
        plan.setDescription(dto.getDescription());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setOrderIndex(dto.getOrderIndex());
        return plan;
    }

    // ==================== addProgress ====================

    public Result addProgress(Integer projectId, ProgressDTO dto, Integer userId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;
        if (dto == null) return badRequest("请求参数不能为空");

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!isMemberOrAdmin(projectId, project, userId))
            return forbidden("无权添加进度");

        Result validation = validateProgressDTO(dto);
        if (validation != null) return validation;

        ProjectProgress progress = buildProgress(projectId, userId, dto);
        int rate = dto.getCompletionRate();

        return executeInTransaction("添加进度", conn -> {
            if (!projectDAO.addProgress(progress, conn)) return serverError("添加进度");
            addHistory(conn, projectId, OP_INFO_UPDATE, userId,
                    "添加项目进度: " + progress.getTitle() + " (" + rate + "%)", null, progress.getTitle());
            return Result.ok();
        });
    }

    private Result validateProgressDTO(ProgressDTO dto) {
        if (isBlank(dto.getTitle())) return badRequest("进度标题不能为空");
        Integer rate = dto.getCompletionRate();
        if (rate == null || rate < 0 || rate > 100)
            return badRequest("完成率必须在0-100之间");
        return null;
    }

    private ProjectProgress buildProgress(Integer projectId, Integer userId, ProgressDTO dto) {
        ProjectProgress progress = new ProjectProgress();
        progress.setProjectId(projectId);
        progress.setPlanId(dto.getPlanId());
        progress.setTitle(dto.getTitle().trim());
        progress.setDescription(dto.getDescription());
        progress.setCompletionRate(dto.getCompletionRate());
        progress.setCreatedBy(userId);
        return progress;
    }

    // ==================== transferAdmin ====================

    public Result transferAdmin(Integer projectId, Integer newAdminId, Integer operatorId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, operatorId))
            return forbidden("无权转让此项目");
        if (STATUS_COMPLETED.equals(project.getStatus()))
            return badRequest("项目已完成，无法转让");
        if (newAdminId.equals(operatorId))
            return badRequest("不能转让给自己");
        if (!projectDAO.isMember(projectId, newAdminId))
            return badRequest("指定用户不是项目成员");

        String oldLeaderName = resolveOperatorName(operatorId);
        String newLeaderName = resolveOperatorName(newAdminId);

        return executeInTransaction("转让", conn -> {
            if (!projectDAO.transferAdmin(projectId, newAdminId, conn)) return serverError("转让");
            addHistory(conn, projectId, OP_TRANSFER, operatorId, "转让项目负责人", oldLeaderName, newLeaderName);
            return Result.ok();
        });
    }

    // ==================== addLabel ====================

    public Result addLabel(Integer projectId, String labelCode, Integer userId) {
        if (isBlank(labelCode)) return badRequest("标签代码不能为空");
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, userId)) return forbidden("无权管理标签");

        List<String> labels = projectDAO.getLabels(projectId);
        if (labels != null && labels.contains(labelCode))
            return badRequest("标签已存在");

        return executeInTransaction("添加标签", conn -> {
            if (!projectDAO.addLabel(projectId, labelCode, conn)) return serverError("添加标签");
            addHistory(conn, projectId, OP_LABEL_ADD, userId, "添加标签: " + labelCode, null, labelCode);
            return Result.ok();
        });
    }

    // ==================== removeLabel ====================

    public Result removeLabel(Integer projectId, String labelCode, Integer userId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!canManageProject(project, userId)) return forbidden("无权管理标签");

        List<String> labels = projectDAO.getLabels(projectId);
        if (labels == null || !labels.contains(labelCode))
            return badRequest("标签不存在");

        return executeInTransaction("移除标签", conn -> {
            if (!projectDAO.removeLabel(projectId, labelCode, conn)) return serverError("移除标签");
            addHistory(conn, projectId, OP_LABEL_REMOVE, userId, "移除标签: " + labelCode, labelCode, null);
            return Result.ok();
        });
    }

    // ==================== uploadProjectImage ====================

    public Result uploadProjectImage(Integer projectId, Object part, Integer userId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!isMemberOrAdmin(projectId, project, userId)) return forbidden("无权上传图片");

        PartInfo info = extractPartInfo(part);
        if (info == null) return badRequest("文件信息无效");
        if (info.size <= 0) return badRequest("文件不能为空");
        if (info.size > MAX_IMAGE_SIZE) return badRequest("图片大小不能超过5MB");
        if (info.contentType == null || !info.contentType.startsWith("image/"))
            return badRequest("只能上传图片文件");

        Result fsCheck = requireFileServiceAvailable();
        if (fsCheck != null) return fsCheck;

        Result uploadResult = fileService.uploadFile(part, "images/projects", userId);
        if (!uploadResult.isSuccess()) return uploadResult;

        recordImageUploadHistory(projectId, userId, info.fileNameOrEmpty());
        return Result.ok(uploadResult.getData());
    }

    private void recordImageUploadHistory(Integer projectId, Integer userId, String fileName) {
        try {
            TransactionTemplate.executeWithConnection(conn -> {
                addHistory(conn, projectId, OP_IMAGE_ADD, userId, "上传项目图片: " + fileName, null, fileName);
                return null;
            });
        } catch (RuntimeException ignored) {
        }
    }

    // ==================== uploadProjectFile ====================

    public Result uploadProjectFile(Integer projectId, Object part, String fileType, Integer userId) {
        if (isBlank(fileType)) return badRequest("文件类型不能为空");
        if (!isAllowedFileType(fileType)) return badRequest("不支持的文件类型");

        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!isMemberOrAdmin(projectId, project, userId)) return forbidden("无权上传文件");

        PartInfo info = extractPartInfo(part);
        if (info != null && info.size > MAX_FILE_SIZE)
            return badRequest("文件大小不能超过20MB");

        Result fsCheck = requireFileServiceAvailable();
        if (fsCheck != null) return fsCheck;

        Result uploadResult = fileService.uploadFile(part, "files/projects", userId);
        return uploadResult.isSuccess() ? Result.ok(uploadResult.getData()) : uploadResult;
    }

    private boolean isAllowedFileType(String fileType) {
        return FILE_TYPE_DOC.equals(fileType)
                || FILE_TYPE_IMAGE.equals(fileType)
                || FILE_TYPE_CODE.equals(fileType);
    }

    // ==================== deleteProjectFile ====================

    public Result deleteProjectFile(Integer projectId, Integer fileId, Integer userId) {
        Result idCheck = requireProjectId(projectId);
        if (idCheck != null) return idCheck;

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (!isMemberOrAdmin(projectId, project, userId)) return forbidden("无权删除文件");

        Result fsCheck = requireFileServiceAvailable();
        if (fsCheck != null) return fsCheck;

        Result delResult = fileService.deleteFile(fileId, userId);
        if (!delResult.isSuccess()) return delResult;

        recordFileDeleteHistory(projectId, userId, fileId);
        return Result.ok();
    }

    private void recordFileDeleteHistory(Integer projectId, Integer userId, Integer fileId) {
        try {
            TransactionTemplate.executeWithConnection(conn -> {
                addHistory(conn, projectId, OP_FILE_DELETE, userId, "删除项目文件", "fileId:" + fileId, null);
                return null;
            });
        } catch (RuntimeException ignored) {
        }
    }

    // ==================== listProjects ====================

    public Result listProjects(ProjectFilterDTO filter, int page, int pageSize) {
        normalizePageParams(page, pageSize);

        String keyword = filter != null ? filter.getKeyword() : null;
        String status = filter != null ? filter.getStatus() : null;
        String category = filter != null ? filter.getCategory() : null;
        Integer year = filter != null ? filter.getYear() : null;

        List<Project> projects = projectDAO.findByConditions(keyword, status, year);

        if (category != null && !category.isEmpty() && projects != null) {
            projects = projects.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }

        return Result.ok(projects != null ? projects : Collections.emptyList());
    }

    // ==================== getProjectDetail ====================

    public Result getProjectDetail(Integer projectId, Integer userId) {
        if (projectId == null || projectId <= 0) return badRequest("项目ID无效");
        if (userId != null && userId <= 0) return badRequest("用户ID无效");

        Project project = loadProject(projectId);
        if (project == null) return notFound("项目不存在");
        if (userId == null && !STATUS_APPROVED.equals(project.getStatus()))
            return forbidden("无权查看此项目");

        return Result.ok(buildProjectDetail(project, userId));
    }

    private java.util.Map<String, Object> buildProjectDetail(Project project, Integer userId) {
        project.setMembers(projectDAO.getProjectMembers(project.getId()));
        project.setLabels(projectDAO.getLabels(project.getId()));

        java.util.Map<String, Object> detail = new java.util.HashMap<>();
        detail.put("project", project);
        detail.put("members", project.getMembers());
        detail.put("labels", project.getLabels());
        detail.put("plans", projectDAO.getPlans(project.getId()));
        detail.put("progress", projectDAO.getProgressList(project.getId()));
        detail.put("history", projectDAO.getHistory(project.getId()));

        if (userId != null) {
            boolean member = projectDAO.isMember(project.getId(), userId);
            detail.put("isMember", member);
            detail.put("currentUserRole", resolveUserRole(project, userId, member));
        }

        return detail;
    }

    private String resolveUserRole(Project project, Integer userId, boolean isMember) {
        if (userId.equals(project.getLeaderId()) || userId.equals(project.getAdminId())) {
            return ROLE_ADMIN;
        }
        return isMember ? ROLE_MEMBER : null;
    }

    // ==================== getMyProjects ====================

    public Result getMyProjects(Integer userId, int page, int pageSize) {
        if (userId == null) return badRequest("用户ID不能为空");
        normalizePageParams(page, pageSize);

        List<Project> list = projectDAO.findProjectsByUserId(userId);
        return Result.ok(list != null ? list : Collections.emptyList());
    }
}
