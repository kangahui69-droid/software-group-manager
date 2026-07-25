package service;

import dao.MemberProfileDAO;
import dao.RecruitApplicationDAO;
import dao.UserDAO;
import dto.RecruitApplicationDTO;
import model.MemberProfile;
import model.RecruitApplication;
import model.User;
import util.Result;

import java.util.List;
import java.util.function.Supplier;

/**
 * 招新服务层
 *
 * 服务分层与API化重构计划.md 4.3 RecruitService 招新服务
 */
public class RecruitService {

    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_APPROVED = 2;
    public static final Integer STATUS_REJECTED = 0;

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String ROLE_MEMBER = "MEMBER";

    private final RecruitApplicationDAO recruitDAO;
    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;

    public RecruitService() {
        this.recruitDAO = new RecruitApplicationDAO();
        this.userDAO = new UserDAO();
        this.memberProfileDAO = new MemberProfileDAO();
    }

    public RecruitService(RecruitApplicationDAO recruitDAO, UserDAO userDAO, MemberProfileDAO memberProfileDAO) {
        this.recruitDAO = recruitDAO;
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
    }

    // ==================== 公开业务方法 ====================

    public Result submitApplication(RecruitApplicationDTO dto) {
        Result validation = validateApplication(dto);
        if (validation == null) {
            return Result.error(400, "验证失败");
        }
        if (!validation.isSuccess()) {
            return validation;
        }

        return execute(() -> {
            RecruitApplication app = buildApplicationFromDTO(dto);
            app.setStatus(STATUS_PENDING);
            boolean inserted = recruitDAO.insert(app);
            if (!inserted) {
                return Result.error(500, "提交申请失败");
            }
            return Result.ok();
        });
    }

    public Result approveApplication(Integer applicationId, Integer operatorId) {
        Result validation = validateIdAndOperatorId(applicationId, operatorId);
        if (validation != null) {
            return validation;
        }

        return execute(() -> {
            RecruitApplication app = findApplicationOrFail(applicationId);
            ensurePendingStatus(app);

            if (userDAO.existsByUsername(app.getStudentId())) {
                return approveExistingUser(app);
            }

            checkEmailAvailability(app);
            return createUserAndProfile(app);
        });
    }

    public Result rejectApplication(Integer applicationId, Integer operatorId) {
        Result validation = validateIdAndOperatorId(applicationId, operatorId);
        if (validation != null) {
            return validation;
        }

        return execute(() -> {
            RecruitApplication app = findApplicationOrFail(applicationId);
            ensurePendingStatus(app);

            app.setStatus(STATUS_REJECTED);
            boolean updated = recruitDAO.update(app);
            if (!updated) {
                return Result.error(500, "驳回申请失败");
            }
            return Result.ok();
        });
    }

    public Result listApplications(Integer year, String status, String keyword, Integer round) {
        return execute(() -> {
            List<RecruitApplication> list = recruitDAO.findByConditions(keyword, year, status, round);
            return Result.ok(list);
        });
    }

    public Result getApplicationDetail(Integer applicationId) {
        Result validation = validateApplicationId(applicationId);
        if (validation != null) {
            return validation;
        }

        return execute(() -> {
            RecruitApplication app = recruitDAO.findById(applicationId);
            if (app == null) {
                return Result.error(404, "申请不存在");
            }
            return Result.ok(app);
        });
    }

    public Result deleteApplication(Integer applicationId) {
        Result validation = validateApplicationId(applicationId);
        if (validation != null) {
            return validation;
        }

        return execute(() -> {
            boolean deleted = recruitDAO.delete(applicationId);
            if (!deleted) {
                return Result.error(404, "申请不存在");
            }
            return Result.ok();
        });
    }

    public Result countPending() {
        return execute(() -> {
            int count = recruitDAO.countPending();
            return Result.ok(count);
        });
    }

    public Result findAllYears() {
        return execute(() -> {
            List<Integer> years = recruitDAO.findAllYears();
            return Result.ok(years);
        });
    }

    public Result validateApplication(RecruitApplicationDTO dto) {
        if (dto == null) {
            return Result.error(400, "申请信息不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error(400, "姓名不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            return Result.error(400, "学号不能为空");
        }
        if (dto.getMajor() == null || dto.getMajor().trim().isEmpty()) {
            return Result.error(400, "专业不能为空");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        return Result.ok();
    }

    // ==================== 内部业务方法 ====================

    /**
     * 学号已存在时，仅更新申请状态为已通过
     */
    private Result approveExistingUser(RecruitApplication app) {
        app.setStatus(STATUS_APPROVED);
        boolean updated = recruitDAO.update(app);
        if (!updated) {
            return Result.error(500, "审批失败");
        }
        return Result.ok();
    }

    /**
     * 学号不存在时，检查邮箱并创建用户和成员档案
     */
    private Result createUserAndProfile(RecruitApplication app) {
        User user = buildUserFromApplication(app);
        boolean userCreated = userDAO.insert(user);
        if (!userCreated) {
            return Result.error(500, "创建用户失败");
        }

        MemberProfile profile = buildMemberProfileFromApplication(app, user.getId());
        boolean profileCreated = memberProfileDAO.insert(profile);
        if (!profileCreated) {
            return Result.error(500, "创建成员档案失败");
        }

        app.setStatus(STATUS_APPROVED);
        boolean updated = recruitDAO.update(app);
        if (!updated) {
            return Result.error(500, "更新申请状态失败");
        }

        return Result.ok();
    }

    /**
     * 检查邮箱是否已被使用
     */
    private void checkEmailAvailability(RecruitApplication app) {
        String email = app.getEmail();
        boolean emailExists = (email != null && !email.trim().isEmpty())
                ? userDAO.existsByEmail(email)
                : userDAO.existsByEmail("");
        if (emailExists) {
            throw new BadRequestException("该邮箱已被其他用户使用");
        }
    }

    // ==================== 验证方法 ====================

    private Result validateIdAndOperatorId(Integer id, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "申请ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (id <= 0) {
            return Result.error(400, "申请ID必须大于0");
        }
        return null;
    }

    private Result validateApplicationId(Integer applicationId) {
        if (applicationId == null) {
            return Result.error(400, "申请ID不能为空");
        }
        if (applicationId <= 0) {
            return Result.error(400, "申请ID必须大于0");
        }
        return null;
    }

    // ==================== 构建方法 ====================

    private RecruitApplication buildApplicationFromDTO(RecruitApplicationDTO dto) {
        RecruitApplication app = new RecruitApplication();
        app.setName(dto.getName());
        app.setStudentId(dto.getStudentId());
        app.setMajor(dto.getMajor());
        app.setGrade(dto.getGrade());
        app.setPhone(dto.getPhone());
        app.setEmail(dto.getEmail());
        app.setReason(dto.getReason());
        return app;
    }

    private User buildUserFromApplication(RecruitApplication app) {
        User user = new User();
        user.setUsername(app.getStudentId());
        user.setPassword(DEFAULT_PASSWORD);
        user.setName(app.getName());
        user.setEmail(isBlank(app.getEmail()) ? null : app.getEmail());
        user.setPhone(app.getPhone());
        user.setRole(ROLE_MEMBER);
        user.setStatus(1);
        user.setMustChangePassword(true);
        return user;
    }

    private MemberProfile buildMemberProfileFromApplication(RecruitApplication app, Integer userId) {
        MemberProfile profile = new MemberProfile();
        profile.setUserId(userId);
        profile.setStudentId(app.getStudentId());
        profile.setMajor(app.getMajor());
        profile.setGrade(app.getGrade());
        profile.setStatus(1);
        return profile;
    }

    // ==================== 辅助方法 ====================

    private RecruitApplication findApplicationOrFail(Integer applicationId) {
        RecruitApplication app = recruitDAO.findById(applicationId);
        if (app == null) {
            throw new ApplicationNotFoundException("申请不存在");
        }
        return app;
    }

    private void ensurePendingStatus(RecruitApplication app) {
        if (app.getStatus() != STATUS_PENDING) {
            throw new BadRequestException("该申请已被审批，无法重复操作");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 统一事务执行入口：捕获RuntimeException返回500错误
     */
    private Result execute(Supplier<Result> action) {
        try {
            return action.get();
        } catch (BadRequestException e) {
            return Result.error(400, e.getMessage());
        } catch (ApplicationNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }

    // ==================== 异常类 ====================

    private static class ApplicationNotFoundException extends RuntimeException {
        ApplicationNotFoundException(String message) {
            super(message);
        }
    }

    private static class BadRequestException extends RuntimeException {
        BadRequestException(String message) {
            super(message);
        }
    }
}
