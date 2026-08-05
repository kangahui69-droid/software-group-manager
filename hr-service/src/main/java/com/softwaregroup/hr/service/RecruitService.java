package com.softwaregroup.hr.service;

import com.softwaregroup.hr.dao.MemberProfileDAO;
import com.softwaregroup.hr.dao.RecruitApplicationDAO;
import com.softwaregroup.hr.dao.UserDAO;
import com.softwaregroup.hr.model.entity.MemberProfile;
import com.softwaregroup.hr.model.entity.RecruitApplication;
import com.softwaregroup.hr.model.entity.User;
import com.softwaregroup.hr.model.dto.RecruitApplicationDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 招新服务层
 */
@Service
public class RecruitService {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_FORBIDDEN = 403;
    private static final int CODE_INTERNAL_ERROR = 500;

    private static final Integer STATUS_PENDING = 1;
    private static final Integer STATUS_APPROVED = 2;
    private static final Integer STATUS_REJECTED = 0;

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String ROLE_MEMBER = "MEMBER";

    private final RecruitApplicationDAO recruitDAO;
    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;

    public RecruitService(RecruitApplicationDAO recruitDAO, UserDAO userDAO, MemberProfileDAO memberProfileDAO) {
        this.recruitDAO = recruitDAO;
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
    }

    public Result submitApplication(RecruitApplicationDTO dto) {
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "验证失败");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "姓名不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "学号不能为空");
        }
        if (dto.getMajor() == null || dto.getMajor().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "专业不能为空");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "邮箱不能为空");
        }

        RecruitApplication app = buildApplicationFromDTO(dto);
        app.setStatus(STATUS_PENDING);

        boolean inserted = recruitDAO.insert(app);
        if (!inserted) {
            return Result.error(CODE_INTERNAL_ERROR, "提交申请失败");
        }
        return Result.ok();
    }

    public Result approveApplication(Integer applicationId, Integer operatorId) {
        if (applicationId == null) {
            return Result.error(CODE_BAD_REQUEST, "申请ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作者ID不能为空");
        }
        if (applicationId <= 0) {
            return Result.error(CODE_BAD_REQUEST, "申请ID必须大于0");
        }

        RecruitApplication app = recruitDAO.findById(applicationId);
        if (app == null) {
            return Result.error(CODE_NOT_FOUND, "申请不存在");
        }
        if (app.getStatus() != STATUS_PENDING) {
            return Result.error(CODE_BAD_REQUEST, "该申请已被审批，无法重复操作");
        }

        if (userDAO.existsByUsername(app.getStudentId())) {
            app.setStatus(STATUS_APPROVED);
            boolean updated = recruitDAO.update(app);
            if (!updated) {
                return Result.error(CODE_INTERNAL_ERROR, "审批失败");
            }
            return Result.ok();
        }

        if (!isBlank(app.getEmail()) && userDAO.existsByEmail(app.getEmail())) {
            return Result.error(CODE_BAD_REQUEST, "该邮箱已被其他用户使用");
        }

        User user = buildUserFromApplication(app);
        boolean userCreated = userDAO.insert(user);
        if (!userCreated) {
            return Result.error(CODE_INTERNAL_ERROR, "创建用户失败");
        }

        MemberProfile profile = buildMemberProfileFromApplication(app, user.getId());
        boolean profileCreated = memberProfileDAO.insert(profile);
        if (!profileCreated) {
            return Result.error(CODE_INTERNAL_ERROR, "创建成员档案失败");
        }

        app.setStatus(STATUS_APPROVED);
        boolean updated = recruitDAO.update(app);
        if (!updated) {
            return Result.error(CODE_INTERNAL_ERROR, "更新申请状态失败");
        }

        return Result.ok();
    }

    public Result rejectApplication(Integer applicationId, Integer operatorId) {
        if (applicationId == null) {
            return Result.error(CODE_BAD_REQUEST, "申请ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作者ID不能为空");
        }
        if (applicationId <= 0) {
            return Result.error(CODE_BAD_REQUEST, "申请ID必须大于0");
        }

        RecruitApplication app = recruitDAO.findById(applicationId);
        if (app == null) {
            return Result.error(CODE_NOT_FOUND, "申请不存在");
        }
        if (app.getStatus() != STATUS_PENDING) {
            return Result.error(CODE_BAD_REQUEST, "该申请已被审批，无法重复操作");
        }

        app.setStatus(STATUS_REJECTED);
        boolean updated = recruitDAO.update(app);
        if (!updated) {
            return Result.error(CODE_INTERNAL_ERROR, "驳回申请失败");
        }
        return Result.ok();
    }

    public Result listApplications(Integer year, String status, String keyword, Integer round) {
        List<RecruitApplication> list = recruitDAO.findByConditions(keyword, year, status, round);
        return Result.ok(list);
    }

    public Result getApplicationDetail(Integer applicationId) {
        if (applicationId == null) {
            return Result.error(CODE_BAD_REQUEST, "申请ID不能为空");
        }
        if (applicationId <= 0) {
            return Result.error(CODE_BAD_REQUEST, "申请ID必须大于0");
        }

        RecruitApplication app = recruitDAO.findById(applicationId);
        if (app == null) {
            return Result.error(CODE_NOT_FOUND, "申请不存在");
        }
        return Result.ok(app);
    }

    public Result deleteApplication(Integer applicationId) {
        if (applicationId == null) {
            return Result.error(CODE_BAD_REQUEST, "申请ID不能为空");
        }
        if (applicationId <= 0) {
            return Result.error(CODE_BAD_REQUEST, "申请ID必须大于0");
        }

        boolean deleted = recruitDAO.delete(applicationId);
        if (!deleted) {
            return Result.error(CODE_NOT_FOUND, "申请不存在");
        }
        return Result.ok();
    }

    public Result countPending() {
        int count = recruitDAO.countPending();
        return Result.ok(count);
    }

    public Result findAllYears() {
        List<Integer> years = recruitDAO.findAllYears();
        return Result.ok(years);
    }

    public Result validateApplication(RecruitApplicationDTO dto) {
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "申请信息不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "姓名不能为空");
        }
        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "学号不能为空");
        }
        if (dto.getMajor() == null || dto.getMajor().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "专业不能为空");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "邮箱不能为空");
        }
        return Result.ok();
    }

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

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
