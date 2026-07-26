package service;

import dao.ProblemReportDAO;
import dao.UserDAO;
import dto.ProblemDTO;
import dto.ProblemFilterDTO;
import model.ProblemReport;
import model.User;
import util.Result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问题服务
 *
 * 服务分层与API化完整计划.md 5.2 ProblemService 问题服务
 */
public class ProblemService {

    // ==================== 常量定义 ====================
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_TITLE_LENGTH = 256;
    private static final int MAX_CONTENT_LENGTH = 5000;

    // 分类常量
    private static final String CATEGORY_VERIFIED = ProblemDTO.CATEGORY_VERIFIED;
    private static final String CATEGORY_UNVERIFIED = ProblemDTO.CATEGORY_UNVERIFIED;
    private static final String CATEGORY_INVALID = ProblemDTO.CATEGORY_INVALID;

    // 状态常量
    private static final String STATUS_PENDING = ProblemDTO.STATUS_PENDING;
    private static final String STATUS_SOLVING = ProblemDTO.STATUS_SOLVING;
    private static final String STATUS_SOLVED = ProblemDTO.STATUS_SOLVED;
    private static final String STATUS_UNSOLVED = ProblemDTO.STATUS_UNSOLVED;

    // 报告者类型常量
    private static final String REPORTER_TYPE_ADMIN = ProblemDTO.REPORTER_TYPE_ADMIN;
    private static final String REPORTER_TYPE_MEMBER = ProblemDTO.REPORTER_TYPE_MEMBER;
    private static final String REPORTER_TYPE_GUEST = ProblemDTO.REPORTER_TYPE_GUEST;

    // ==================== 依赖注入 ====================
    private final ProblemReportDAO problemReportDAO;
    private final UserDAO userDAO;

    public ProblemService() {
        this.problemReportDAO = new ProblemReportDAO();
        this.userDAO = new UserDAO();
    }

    public ProblemService(ProblemReportDAO problemReportDAO, UserDAO userDAO) {
        this.problemReportDAO = problemReportDAO;
        this.userDAO = userDAO;
    }

    // ==================== 公开业务方法 ====================

    public Result submitProblem(ProblemDTO dto, Integer userId) {
        Result validation = validateSubmitParams(dto, userId);
        if (validation != null) {
            return validation;
        }

        ProblemReport report = buildProblemFromDTO(dto, userId);
        return insertProblem(report);
    }

    public Result getProblemDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "问题ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(id);
        if (problem == null) {
            return Result.error(404, "问题不存在");
        }

        return Result.ok(problem);
    }

    public Result listProblems(ProblemFilterDTO filter, int page, int pageSize) {
        Result validation = validatePageParams(page, pageSize);
        if (validation != null) {
            return validation;
        }

        List<ProblemReport> problems;
        if (filter == null) {
            problems = problemReportDAO.findAll();
        } else if (filter.getCategory() != null) {
            problems = problemReportDAO.findByCategory(filter.getCategory());
        } else if (filter.getStatus() != null) {
            problems = problemReportDAO.findByStatus(filter.getStatus());
        } else {
            problems = problemReportDAO.findAll();
        }

        return Result.ok(problems);
    }

    public Result getMyProblems(Integer userId, int page, int pageSize) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Result validation = validatePageParams(page, pageSize);
        if (validation != null) {
            return validation;
        }

        List<ProblemReport> problems = problemReportDAO.findByUserId(userId);
        return Result.ok(problems);
    }

    public Result updateProblem(Integer id, ProblemDTO dto, Integer operatorId) {
        Result validation = validateUpdateParams(id, dto, operatorId);
        if (validation != null) {
            return validation;
        }

        ProblemReport existing = problemReportDAO.findById(id);
        if (existing == null) {
            return Result.error(404, "问题不存在");
        }

        applyDTOToProblem(existing, dto);
        return updateProblemAndReturn(existing);
    }

    public Result updateStatus(Integer id, String status, String adminComment, Integer operatorId) {
        Result validation = validateStatusParams(id, status, operatorId);
        if (validation != null) {
            return validation;
        }

        ProblemReport existing = problemReportDAO.findById(id);
        if (existing == null) {
            return Result.error(404, "问题不存在");
        }

        if (!CATEGORY_VERIFIED.equals(existing.getCategory())) {
            return Result.error(400, "只有属实的分类才能更新状态");
        }

        boolean updated = problemReportDAO.updateCategoryAndStatus(id, existing.getCategory(), status, adminComment, operatorId);
        if (!updated) {
            return Result.error(500, "更新状态失败");
        }
        return Result.ok();
    }

    public Result updateCategory(Integer id, String category, Integer operatorId) {
        Result validation = validateCategoryParams(id, category, operatorId);
        if (validation != null) {
            return validation;
        }

        ProblemReport existing = problemReportDAO.findById(id);
        if (existing == null) {
            return Result.error(404, "问题不存在");
        }

        String status = STATUS_PENDING;
        boolean updated = problemReportDAO.updateCategoryAndStatus(id, category, status, null, operatorId);
        if (!updated) {
            return Result.error(500, "更新分类失败");
        }
        return Result.ok();
    }

    public Result addComment(Integer id, String adminComment, Integer operatorId) {
        Result validation = validateIdAndOperatorId(id, operatorId);
        if (validation != null) {
            return validation;
        }

        ProblemReport existing = problemReportDAO.findById(id);
        if (existing == null) {
            return Result.error(404, "问题不存在");
        }

        boolean updated = problemReportDAO.updateAdminComment(id, adminComment);
        if (!updated) {
            return Result.error(500, "添加备注失败");
        }
        return Result.ok();
    }

    public Result deleteProblem(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperatorId(id, operatorId);
        if (validation != null) {
            return validation;
        }

        ProblemReport existing = problemReportDAO.findById(id);
        if (existing == null) {
            return Result.error(404, "问题不存在");
        }

        boolean deleted = problemReportDAO.delete(id);
        if (!deleted) {
            return Result.error(500, "删除问题失败");
        }
        return Result.ok();
    }

    public Result getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", problemReportDAO.countPending());
        stats.put("verified", problemReportDAO.countByCategory(CATEGORY_VERIFIED));
        stats.put("unverified", problemReportDAO.countByCategory(CATEGORY_UNVERIFIED));
        stats.put("invalid", problemReportDAO.countByCategory(CATEGORY_INVALID));
        stats.put("solved", problemReportDAO.countByStatus(STATUS_SOLVED));
        return Result.ok(stats);
    }

    // ==================== 验证方法 ====================

    private Result validateSubmitParams(ProblemDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "问题信息不能为空");
        }
        if (isBlank(dto.getTitle())) {
            return Result.error(400, "标题不能为空");
        }
        if (dto.getTitle().length() > MAX_TITLE_LENGTH) {
            return Result.error(400, "标题不能超过256个字符");
        }
        if (isBlank(dto.getContent())) {
            return Result.error(400, "内容不能为空");
        }
        if (dto.getContent().length() > MAX_CONTENT_LENGTH) {
            return Result.error(400, "内容不能超过5000个字符");
        }
        return null;
    }

    private Result validatePageParams(int page, int pageSize) {
        if (page <= 0) {
            return Result.error(400, "页码必须大于0");
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return Result.error(400, "每页数量必须在1-100之间");
        }
        return null;
    }

    private Result validateUpdateParams(Integer id, ProblemDTO dto, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "问题ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "问题信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (isBlank(dto.getTitle())) {
            return Result.error(400, "标题不能为空");
        }
        return null;
    }

    private Result validateStatusParams(Integer id, String status, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "问题ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (!isValidStatus(status)) {
            return Result.error(400, "无效的状态值");
        }
        return null;
    }

    private Result validateCategoryParams(Integer id, String category, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "问题ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (!isValidCategory(category)) {
            return Result.error(400, "无效的分类值");
        }
        return null;
    }

    private Result validateIdAndOperatorId(Integer id, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "问题ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    // ==================== 辅助方法 ====================

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
               STATUS_SOLVING.equals(status) ||
               STATUS_SOLVED.equals(status) ||
               STATUS_UNSOLVED.equals(status);
    }

    private boolean isValidCategory(String category) {
        return CATEGORY_VERIFIED.equals(category) ||
               CATEGORY_UNVERIFIED.equals(category) ||
               CATEGORY_INVALID.equals(category);
    }

    private ProblemReport buildProblemFromDTO(ProblemDTO dto, Integer userId) {
        ProblemReport report = new ProblemReport();
        report.setTitle(dto.getTitle().trim());
        report.setContent(dto.getContent().trim());
        report.setReporterName(trimToNull(dto.getReporterName()));
        report.setReporterContact(trimToNull(dto.getReporterContact()));
        report.setCategory(CATEGORY_UNVERIFIED);
        report.setStatus(STATUS_PENDING);

        if (userId != null) {
            report.setUserId(userId);
            User user = userDAO.findById(userId);
            if (user != null && "ADMIN".equals(user.getRole())) {
                report.setReporterType(REPORTER_TYPE_ADMIN);
            } else {
                report.setReporterType(REPORTER_TYPE_MEMBER);
            }
        } else {
            report.setReporterType(REPORTER_TYPE_GUEST);
        }

        return report;
    }

    private void applyDTOToProblem(ProblemReport problem, ProblemDTO dto) {
        problem.setTitle(dto.getTitle().trim());
    }

    private String trimToNull(String str) {
        return str != null ? str.trim() : null;
    }

    private Result insertProblem(ProblemReport report) {
        int id = problemReportDAO.insert(report);
        if (id <= 0) {
            return Result.error(500, "创建问题失败");
        }
        return Result.ok(id);
    }

    private Result updateProblemAndReturn(ProblemReport problem) {
        boolean updated = problemReportDAO.updateCategoryAndStatus(
            problem.getId(),
            problem.getCategory(),
            problem.getStatus(),
            problem.getAdminComment(),
            problem.getHandledBy()
        );
        if (!updated) {
            return Result.error(500, "更新问题失败");
        }
        return Result.ok(problem);
    }
}