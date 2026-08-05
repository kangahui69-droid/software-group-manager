package com.softwaregroup.project.service;

import com.softwaregroup.project.dao.AwardDAO;
import com.softwaregroup.project.dao.AwardImageDAO;
import com.softwaregroup.project.dao.UserDAO;
import com.softwaregroup.project.model.Award;
import com.softwaregroup.project.model.AwardImage;
import com.softwaregroup.project.model.User;
import com.softwaregroup.project.model.dto.AwardDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 奖项服务层
 */
@Service
public class AwardService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private static final int MAX_REASON_LENGTH = 500;

    @Autowired
    private AwardDAO awardDAO;

    @Autowired
    private AwardImageDAO awardImageDAO;

    @Autowired
    private UserDAO userDAO;

    public AwardService() {
    }

    public AwardService(AwardDAO awardDAO, AwardImageDAO awardImageDAO, UserDAO userDAO) {
        this.awardDAO = awardDAO;
        this.awardImageDAO = awardImageDAO;
        this.userDAO = userDAO;
    }

    public Result submitAward(AwardDTO dto, Integer userId, List<Integer> imageIds) {
        Result validationResult = validateAwardSubmission(dto, userId);
        if (validationResult != null) {
            return validationResult;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Award award = buildAwardFromDTO(dto, userId);

        boolean inserted = awardDAO.insert(award);
        if (!inserted) {
            return Result.error(500, "提交失败");
        }

        return Result.ok(award);
    }

    private Result validateAwardSubmission(AwardDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "奖项信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (dto.getCompetition() == null || dto.getCompetition().trim().isEmpty()) {
            return Result.error(400, "竞赛名称不能为空");
        }
        if (dto.getCompetitionTime() != null && !dto.getCompetitionTime().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return Result.error(400, "比赛时间格式错误");
        }
        return null;
    }

    private Award buildAwardFromDTO(AwardDTO dto, Integer userId) {
        Award award = new Award();
        award.setCompetition(dto.getCompetition());
        award.setCompetitionTime(dto.getCompetitionTime());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardType(dto.getAwardType());
        award.setAwardStatus(STATUS_PENDING);
        award.setCreatedBy(userId);
        award.setCreatedAt(new Date());
        award.setYear(new Date().getYear() + 1900);
        return award;
    }

    public Result approveAward(Integer awardId, Integer operatorId) {
        Result validationResult = validateAwardApprovalInput(awardId, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        Award award = awardDAO.findById(awardId);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "奖项非待审核状态");
        }

        Result adminCheckResult = checkAdminRole(operatorId);
        if (adminCheckResult != null) {
            return adminCheckResult;
        }

        boolean approved = awardDAO.approveAward(awardId, operatorId);
        if (!approved) {
            return Result.error(500, "审批失败");
        }

        return Result.ok();
    }

    public Result rejectAward(Integer awardId, String reason, Integer operatorId) {
        Result validationResult = validateAwardRejectionInput(awardId, reason, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        Award award = awardDAO.findById(awardId);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "奖项非待审核状态");
        }

        Result adminCheckResult = checkAdminRole(operatorId);
        if (adminCheckResult != null) {
            return adminCheckResult;
        }

        boolean rejected = awardDAO.rejectAward(awardId, operatorId);
        if (!rejected) {
            return Result.error(500, "驳回失败");
        }

        return Result.ok();
    }

    private Result validateAwardApprovalInput(Integer awardId, Integer operatorId) {
        if (awardId == null || awardId <= 0) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private Result validateAwardRejectionInput(Integer awardId, String reason, Integer operatorId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(400, "驳回原因不能为空");
        }
        return null;
    }

    private Result checkAdminRole(Integer operatorId) {
        User operator = userDAO.findById(operatorId);
        if (operator == null) {
            return Result.error(404, "操作者不存在");
        }
        if (!"ADMIN".equals(operator.getRole())) {
            return Result.error(403, "无权限");
        }
        return null;
    }

    public Result listAwards(String status, Integer page) {
        if (status == null || status.trim().isEmpty()) {
            return Result.ok(awardDAO.findAll());
        }
        return Result.ok(awardDAO.findByStatus(status));
    }

    public Result getAwardStatistics(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<Award> awards = awardDAO.findByUserId(userId);
        return Result.ok(awards);
    }

    public Result getMyAwards(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<Award> awards = awardDAO.findByUserId(userId);
        return Result.ok(awards);
    }

    public Result getAwardDetail(Integer awardId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }

        Award award = awardDAO.findById(awardId);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        return Result.ok(award);
    }

    public Result getAwardImages(Integer awardId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }

        List<AwardImage> images = awardImageDAO.findByAwardId(awardId);
        return Result.ok(images);
    }

    public Result deleteAward(Integer awardId, Integer userId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Award award = awardDAO.findById(awardId);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "只能删除待审核的奖项");
        }

        boolean isOwner = award.getCreatedBy().equals(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            return Result.error(403, "无权限");
        }

        boolean deleted = awardDAO.delete(awardId);
        if (!deleted) {
            return Result.error(500, "删除失败");
        }

        return Result.ok();
    }

    public Result updateAward(Integer awardId, AwardDTO dto, Integer userId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Award award = awardDAO.findById(awardId);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "只能修改待审核的奖项");
        }

        if (!award.getCreatedBy().equals(userId)) {
            return Result.error(403, "无权限");
        }

        award.setCompetition(dto.getCompetition());
        award.setCompetitionTime(dto.getCompetitionTime());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardType(dto.getAwardType());
        award.setUpdatedAt(new Date());

        boolean updated = awardDAO.update(award);
        if (!updated) {
            return Result.error(500, "更新失败");
        }

        return Result.ok(award);
    }
}
