package com.softwaregroup.monitor.service;

import org.springframework.stereotype.Service;
import com.softwaregroup.monitor.dao.ProblemReportDAO;
import com.softwaregroup.monitor.dao.UserDAO;
import com.softwaregroup.monitor.model.ProblemReport;
import com.softwaregroup.monitor.model.User;
import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.model.dto.ProblemFilterDTO;
import com.softwaregroup.common.util.Result;

import java.util.*;

/**
 * 问题反馈服务层
 */
@Service
public class ProblemService {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_INTERNAL_ERROR = 500;

    private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList(
            ProblemReport.CATEGORY_VERIFIED,
            ProblemReport.CATEGORY_UNVERIFIED,
            ProblemReport.CATEGORY_INVALID
    ));

    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList(
            ProblemReport.STATUS_PENDING,
            ProblemReport.STATUS_SOLVING,
            ProblemReport.STATUS_SOLVED,
            ProblemReport.STATUS_UNSOLVED
    ));

    private static final int MAX_TITLE_LENGTH = 256;
    private static final int MAX_CONTENT_LENGTH = 5000;

    private final ProblemReportDAO problemReportDAO;
    private final UserDAO userDAO;

    public ProblemService(ProblemReportDAO problemReportDAO, UserDAO userDAO) {
        this.problemReportDAO = problemReportDAO;
        this.userDAO = userDAO;
    }

    public Result submitProblem(ProblemDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "问题信息不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }
        if (dto.getTitle().length() > MAX_TITLE_LENGTH) {
            return Result.error(CODE_BAD_REQUEST, "标题不能超过256个字符");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "内容不能为空");
        }
        if (dto.getContent().length() > MAX_CONTENT_LENGTH) {
            return Result.error(CODE_BAD_REQUEST, "内容不能超过5000个字符");
        }

        ProblemReport problem = new ProblemReport();
        problem.setTitle(dto.getTitle().trim());
        problem.setContent(dto.getContent().trim());
        problem.setReporterName(dto.getReporterName());
        problem.setReporterContact(dto.getReporterContact());
        problem.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
        problem.setStatus(ProblemReport.STATUS_PENDING);

        if (userId != null) {
            User user = userDAO.findById(userId);
            if (user != null) {
                if ("ADMIN".equals(user.getRole())) {
                    problem.setReporterType(ProblemReport.REPORTER_TYPE_ADMIN);
                } else {
                    problem.setReporterType(ProblemReport.REPORTER_TYPE_MEMBER);
                }
                problem.setUserId(userId);
            } else {
                problem.setReporterType(ProblemReport.REPORTER_TYPE_GUEST);
            }
        } else {
            problem.setReporterType(ProblemReport.REPORTER_TYPE_GUEST);
        }

        int inserted = problemReportDAO.insert(problem);
        if (inserted > 0) {
            return Result.ok();
        }
        return Result.error(CODE_INTERNAL_ERROR, "提交问题失败");
    }

    public Result getProblemDetail(Integer problemId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(problemId);
        if (problem == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }
        return Result.ok(problem);
    }

    public Result listProblems(ProblemFilterDTO filter, Integer page, Integer pageSize) {
        if (page == null || page <= 0) {
            return Result.error(CODE_BAD_REQUEST, "页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            return Result.error(CODE_BAD_REQUEST, "每页数量必须在1-100之间");
        }

        List<ProblemReport> problems;
        if (filter == null || (filter.getCategory() == null && filter.getStatus() == null)) {
            problems = problemReportDAO.findAll();
        } else if (filter.getCategory() != null) {
            problems = problemReportDAO.findByCategory(filter.getCategory());
        } else {
            problems = problemReportDAO.findByStatus(filter.getStatus());
        }

        return Result.ok(problems);
    }

    public Result getMyProblems(Integer userId, Integer page, Integer pageSize) {
        if (userId == null) {
            return Result.error(CODE_BAD_REQUEST, "用户ID不能为空");
        }

        List<ProblemReport> problems = problemReportDAO.findByUserId(userId);
        return Result.ok(problems);
    }

    public Result updateProblem(Integer problemId, ProblemDTO dto, Integer operatorId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "问题信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作者ID不能为空");
        }

        ProblemReport existing = problemReportDAO.findById(problemId);
        if (existing == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }

        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }

        return Result.ok();
    }

    public Result updateStatus(Integer problemId, String status, String comment, Integer operatorId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作者ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(problemId);
        if (problem == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }

        if (!ProblemReport.CATEGORY_VERIFIED.equals(problem.getCategory())) {
            return Result.error(CODE_BAD_REQUEST, "只有属实的分类才能更新状态");
        }

        if (status != null && !VALID_STATUSES.contains(status)) {
            return Result.error(CODE_BAD_REQUEST, "无效的状态值");
        }

        boolean updated = problemReportDAO.updateCategoryAndStatus(
                problemId, problem.getCategory(), status, comment, operatorId);
        if (updated) {
            return Result.ok();
        }
        return Result.error(CODE_INTERNAL_ERROR, "更新状态失败");
    }

    public Result updateCategory(Integer problemId, String category, Integer operatorId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(problemId);
        if (problem == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }

        if (category == null || !VALID_CATEGORIES.contains(category)) {
            return Result.error(CODE_BAD_REQUEST, "无效的分类值");
        }

        boolean updated = problemReportDAO.updateCategoryAndStatus(
                problemId, category, ProblemReport.STATUS_PENDING, null, operatorId);
        if (updated) {
            return Result.ok();
        }
        return Result.error(CODE_INTERNAL_ERROR, "更新分类失败");
    }

    public Result addComment(Integer problemId, String comment, Integer operatorId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(problemId);
        if (problem == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }

        boolean updated = problemReportDAO.updateAdminComment(problemId, comment);
        if (updated) {
            return Result.ok();
        }
        return Result.error(CODE_INTERNAL_ERROR, "添加备注失败");
    }

    public Result deleteProblem(Integer problemId, Integer operatorId) {
        if (problemId == null) {
            return Result.error(CODE_BAD_REQUEST, "问题ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作者ID不能为空");
        }

        ProblemReport problem = problemReportDAO.findById(problemId);
        if (problem == null) {
            return Result.error(CODE_NOT_FOUND, "问题不存在");
        }

        boolean deleted = problemReportDAO.delete(problemId);
        if (deleted) {
            return Result.ok();
        }
        return Result.error(CODE_INTERNAL_ERROR, "删除问题失败");
    }

    public Result getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", problemReportDAO.countPending());
        stats.put("verified", problemReportDAO.countByCategory(ProblemReport.CATEGORY_VERIFIED));
        stats.put("unverified", problemReportDAO.countByCategory(ProblemReport.CATEGORY_UNVERIFIED));
        stats.put("invalid", problemReportDAO.countByCategory(ProblemReport.CATEGORY_INVALID));
        stats.put("solved", problemReportDAO.countByStatus(ProblemReport.STATUS_SOLVED));
        return Result.ok(stats);
    }
}
