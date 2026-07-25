package service;

import dao.ResumeAwardDAO;
import dao.ResumeDAO;
import dao.ResumeEducationDAO;
import dao.ResumeProjectDAO;
import dao.ResumeSkillDAO;
import dto.ResumeAwardDTO;
import dto.ResumeDTO;
import dto.ResumeEducationDTO;
import dto.ResumeProjectDTO;
import dto.ResumeSkillDTO;
import model.Resume;
import model.ResumeAward;
import model.ResumeEducation;
import model.ResumeProject;
import model.ResumeSkill;
import util.Result;

import java.util.Arrays;
import java.util.List;

/**
 * 简历服务层
 *
 * 服务分层与API化完整计划.md 4.4 ResumeService 简历服务
 */
public class ResumeService {

    // ==================== 状态常量 ====================

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_HIDDEN = 2;

    public static final int DELETED_NO = 0;
    public static final int DELETED_YES = 1;

    public static final int DEFAULT_YES = 1;
    public static final int DEFAULT_NO = 0;

    public static final String PROFICIENCY_BEGINNER = "beginner";
    public static final String PROFICIENCY_ELEMENTARY = "elementary";
    public static final String PROFICIENCY_INTERMEDIATE = "intermediate";
    public static final String PROFICIENCY_ADVANCED = "advanced";
    public static final String PROFICIENCY_EXPERT = "expert";

    private static final List<String> VALID_PROFICIENCIES = Arrays.asList(
            PROFICIENCY_BEGINNER, PROFICIENCY_ELEMENTARY,
            PROFICIENCY_INTERMEDIATE, PROFICIENCY_ADVANCED, PROFICIENCY_EXPERT
    );

    // ==================== 依赖注入 ====================

    private final ResumeDAO resumeDAO;
    private final ResumeEducationDAO educationDAO;
    private final ResumeSkillDAO skillDAO;
    private final ResumeProjectDAO projectDAO;
    private final ResumeAwardDAO awardDAO;

    public ResumeService() {
        this.resumeDAO = new ResumeDAO();
        this.educationDAO = new ResumeEducationDAO();
        this.skillDAO = new ResumeSkillDAO();
        this.projectDAO = new ResumeProjectDAO();
        this.awardDAO = new ResumeAwardDAO();
    }

    public ResumeService(ResumeDAO resumeDAO, ResumeEducationDAO educationDAO,
                        ResumeSkillDAO skillDAO, ResumeProjectDAO projectDAO,
                        ResumeAwardDAO awardDAO) {
        this.resumeDAO = resumeDAO;
        this.educationDAO = educationDAO;
        this.skillDAO = skillDAO;
        this.projectDAO = projectDAO;
        this.awardDAO = awardDAO;
    }

    // ==================== 简历主表操作 ====================

    public Result createResume(ResumeDTO dto, Integer userId) {
        Result validation = validateCreateResumeParams(dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            List<Resume> existingList = resumeDAO.findByUserId(userId);
            boolean isFirst = existingList == null || existingList.isEmpty();

            Resume resume = buildResumeFromDTO(dto, userId);
            resume.setIsDefault(isFirst ? DEFAULT_YES : DEFAULT_NO);
            resume.setStatus(STATUS_PUBLISHED);

            if (!resumeDAO.save(resume)) {
                return Result.error(500, "创建简历失败");
            }
            return Result.ok(resume);
        } catch (Exception e) {
            return Result.error(500, "创建简历失败: " + e.getMessage());
        }
    }

    public Result updateResume(Integer resumeId, ResumeDTO dto, Integer userId) {
        Result validation = validateUpdateResumeParams(resumeId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (isDeleted(resume)) {
                return Result.error(400, "简历已删除");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限更新此简历");
            }

            applyResumeUpdates(resume, dto);

            if (!resumeDAO.update(resume)) {
                return Result.error(500, "更新简历失败");
            }
            return Result.ok(resume);
        } catch (Exception e) {
            return Result.error(500, "更新简历失败: " + e.getMessage());
        }
    }

    public Result deleteResume(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (isDeleted(resume)) {
                return Result.error(400, "简历已删除");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限删除此简历");
            }

            if (!resumeDAO.softDelete(resumeId)) {
                return Result.error(500, "删除简历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "删除简历失败: " + e.getMessage());
        }
    }

    public Result setDefaultResume(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (isDeleted(resume)) {
                return Result.error(400, "简历已删除");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限设置此简历");
            }

            if (!resumeDAO.setDefaultResume(resumeId, userId)) {
                return Result.error(500, "设置默认简历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "设置默认简历失败: " + e.getMessage());
        }
    }

    public Result getResumeDetail(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (isDeleted(resume)) {
                return Result.error(400, "简历已删除");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限查看此简历");
            }

            resume.setEducations(educationDAO.findByResumeId(resumeId));
            resume.setSkills(skillDAO.findByResumeId(resumeId));
            resume.setProjects(projectDAO.findByResumeId(resumeId));
            resume.setAwards(awardDAO.findByResumeId(resumeId));

            return Result.ok(resume);
        } catch (Exception e) {
            return Result.error(500, "获取简历详情失败: " + e.getMessage());
        }
    }

    public Result listResumes(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        try {
            List<Resume> resumes = resumeDAO.findByUserId(userId);
            return Result.ok(resumes);
        } catch (Exception e) {
            return Result.error(500, "获取简历列表失败: " + e.getMessage());
        }
    }

    public Result getRecycleBin(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        try {
            List<Resume> resumes = resumeDAO.findDeletedByUserId(userId);
            return Result.ok(resumes);
        } catch (Exception e) {
            return Result.error(500, "获取回收站失败: " + e.getMessage());
        }
    }

    public Result restoreResume(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId, true);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!isDeleted(resume)) {
                return Result.error(400, "只能恢复已删除的简历");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限恢复此简历");
            }

            if (!resumeDAO.restore(resumeId)) {
                return Result.error(500, "恢复简历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "恢复简历失败: " + e.getMessage());
        }
    }

    public Result permanentDelete(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId, true);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!isDeleted(resume)) {
                return Result.error(400, "只能永久删除已回收的简历");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限删除此简历");
            }

            if (!resumeDAO.hardDelete(resumeId)) {
                return Result.error(500, "永久删除简历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "永久删除简历失败: " + e.getMessage());
        }
    }

    // ==================== 教育经历操作 ====================

    public Result addEducation(Integer resumeId, ResumeEducationDTO dto, Integer userId) {
        Result validation = validateEducationParams(resumeId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限添加教育经历");
            }

            ResumeEducation education = buildEducationFromDTO(dto);
            education.setResumeId(resumeId);

            if (!educationDAO.save(education)) {
                return Result.error(500, "添加教育经历失败");
            }
            return Result.ok(education);
        } catch (Exception e) {
            return Result.error(500, "添加教育经历失败: " + e.getMessage());
        }
    }

    public Result updateEducation(Integer educationId, ResumeEducationDTO dto, Integer userId) {
        Result validation = validateSubItemIdAndUserId(educationId, userId, "教育经历");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(educationId, userId, "教育经历",
                    () -> educationDAO.findById(educationId),
                    item -> ((ResumeEducation) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            ResumeEducation education = educationDAO.findById(educationId);
            applyEducationUpdates(education, dto);

            if (!educationDAO.update(education)) {
                return Result.error(500, "更新教育经历失败");
            }
            return Result.ok(education);
        } catch (Exception e) {
            return Result.error(500, "更新教育经历失败: " + e.getMessage());
        }
    }

    public Result deleteEducation(Integer educationId, Integer userId) {
        Result validation = validateSubItemIdAndUserId(educationId, userId, "教育经历");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(educationId, userId, "教育经历",
                    () -> educationDAO.findById(educationId),
                    item -> ((ResumeEducation) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            if (!educationDAO.delete(educationId)) {
                return Result.error(500, "删除教育经历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "删除教育经历失败: " + e.getMessage());
        }
    }

    // ==================== 技能操作 ====================

    public Result addSkill(Integer resumeId, ResumeSkillDTO dto, Integer userId) {
        Result validation = validateSkillParams(resumeId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限添加技能");
            }

            ResumeSkill skill = buildSkillFromDTO(dto);
            skill.setResumeId(resumeId);

            if (!skillDAO.save(skill)) {
                return Result.error(500, "添加技能失败");
            }
            return Result.ok(skill);
        } catch (Exception e) {
            return Result.error(500, "添加技能失败: " + e.getMessage());
        }
    }

    public Result updateSkill(Integer skillId, ResumeSkillDTO dto, Integer userId) {
        Result validation = validateSubItemIdAndUserId(skillId, userId, "技能");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(skillId, userId, "技能",
                    () -> skillDAO.findById(skillId),
                    item -> ((ResumeSkill) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            ResumeSkill skill = skillDAO.findById(skillId);
            applySkillUpdates(skill, dto);

            if (!skillDAO.update(skill)) {
                return Result.error(500, "更新技能失败");
            }
            return Result.ok(skill);
        } catch (Exception e) {
            return Result.error(500, "更新技能失败: " + e.getMessage());
        }
    }

    public Result deleteSkill(Integer skillId, Integer userId) {
        Result validation = validateSubItemIdAndUserId(skillId, userId, "技能");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(skillId, userId, "技能",
                    () -> skillDAO.findById(skillId),
                    item -> ((ResumeSkill) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            if (!skillDAO.delete(skillId)) {
                return Result.error(500, "删除技能失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "删除技能失败: " + e.getMessage());
        }
    }

    // ==================== 项目经历操作 ====================

    public Result addProject(Integer resumeId, ResumeProjectDTO dto, Integer userId) {
        Result validation = validateProjectParams(resumeId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限添加项目经历");
            }

            ResumeProject project = buildProjectFromDTO(dto);
            project.setResumeId(resumeId);

            if (!projectDAO.save(project)) {
                return Result.error(500, "添加项目经历失败");
            }
            return Result.ok(project);
        } catch (Exception e) {
            return Result.error(500, "添加项目经历失败: " + e.getMessage());
        }
    }

    public Result updateProject(Integer projectId, ResumeProjectDTO dto, Integer userId) {
        Result validation = validateSubItemIdAndUserId(projectId, userId, "项目经历");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(projectId, userId, "项目经历",
                    () -> projectDAO.findById(projectId),
                    item -> ((ResumeProject) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            ResumeProject project = projectDAO.findById(projectId);
            applyProjectUpdates(project, dto);

            if (!projectDAO.update(project)) {
                return Result.error(500, "更新项目经历失败");
            }
            return Result.ok(project);
        } catch (Exception e) {
            return Result.error(500, "更新项目经历失败: " + e.getMessage());
        }
    }

    public Result deleteProject(Integer projectId, Integer userId) {
        Result validation = validateSubItemIdAndUserId(projectId, userId, "项目经历");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(projectId, userId, "项目经历",
                    () -> projectDAO.findById(projectId),
                    item -> ((ResumeProject) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            if (!projectDAO.delete(projectId)) {
                return Result.error(500, "删除项目经历失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "删除项目经历失败: " + e.getMessage());
        }
    }

    // ==================== 获奖情况操作 ====================

    public Result addAward(Integer resumeId, ResumeAwardDTO dto, Integer userId) {
        Result validation = validateAwardParams(resumeId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            Resume resume = resumeDAO.findById(resumeId);
            if (resume == null) {
                return Result.error(404, "简历不存在");
            }
            if (!resume.getUserId().equals(userId)) {
                return Result.error(403, "无权限添加获奖情况");
            }

            ResumeAward award = buildAwardFromDTO(dto);
            award.setResumeId(resumeId);

            if (!awardDAO.save(award)) {
                return Result.error(500, "添加获奖情况失败");
            }
            return Result.ok(award);
        } catch (Exception e) {
            return Result.error(500, "添加获奖情况失败: " + e.getMessage());
        }
    }

    public Result updateAward(Integer awardId, ResumeAwardDTO dto, Integer userId) {
        Result validation = validateSubItemIdAndUserId(awardId, userId, "获奖情况");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(awardId, userId, "获奖情况",
                    () -> awardDAO.findById(awardId),
                    item -> ((ResumeAward) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            ResumeAward award = awardDAO.findById(awardId);
            applyAwardUpdates(award, dto);

            if (!awardDAO.update(award)) {
                return Result.error(500, "更新获奖情况失败");
            }
            return Result.ok(award);
        } catch (Exception e) {
            return Result.error(500, "更新获奖情况失败: " + e.getMessage());
        }
    }

    public Result deleteAward(Integer awardId, Integer userId) {
        Result validation = validateSubItemIdAndUserId(awardId, userId, "获奖情况");
        if (validation != null) {
            return validation;
        }

        try {
            Result ownershipCheck = validateSubItemOwnership(awardId, userId, "获奖情况",
                    () -> awardDAO.findById(awardId),
                    item -> ((ResumeAward) item).getResumeId());
            if (ownershipCheck != null) {
                return ownershipCheck;
            }

            if (!awardDAO.delete(awardId)) {
                return Result.error(500, "删除获奖情况失败");
            }
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "删除获奖情况失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    private boolean isDeleted(Resume resume) {
        return resume.getDeleted() != null && resume.getDeleted() == DELETED_YES;
    }

    private Result validateSubItemOwnership(Integer itemId, Integer userId, String itemName,
            java.util.function.Supplier<Object> itemFetcher,
            java.util.function.Function<Object, Integer> resumeIdGetter) {
        Object item = itemFetcher.get();
        if (item == null) {
            return Result.error(404, itemName + "不存在");
        }
        Resume resume = resumeDAO.findById(resumeIdGetter.apply(item));
        if (resume == null || !resume.getUserId().equals(userId)) {
            return Result.error(403, "无权限操作此" + itemName);
        }
        return null;
    }

    private void applyResumeUpdates(Resume resume, ResumeDTO dto) {
        resume.setResumeName(dto.getResumeName());
        resume.setTemplateStyle(dto.getTemplateStyle());
        resume.setSummary(dto.getSummary());
        resume.setCareerObjective(dto.getCareerObjective());
        resume.setPhone(dto.getPhone());
        resume.setEmail(dto.getEmail());
        resume.setWechat(dto.getWechat());
        resume.setGithubUrl(dto.getGithubUrl());
        resume.setBlogUrl(dto.getBlogUrl());
    }

    private void applyEducationUpdates(ResumeEducation education, ResumeEducationDTO dto) {
        education.setSchoolName(dto.getSchoolName());
        education.setMajor(dto.getMajor());
        education.setDegree(dto.getDegree());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setIsCurrent(dto.getIsCurrent());
        education.setDescription(dto.getDescription());
    }

    private void applySkillUpdates(ResumeSkill skill, ResumeSkillDTO dto) {
        skill.setSkillName(dto.getSkillName());
        skill.setProficiency(dto.getProficiency());
        skill.setProficiencyScore(dto.getProficiencyScore());
        skill.setCategory(dto.getCategory());
        skill.setDescription(dto.getDescription());
    }

    private void applyProjectUpdates(ResumeProject project, ResumeProjectDTO dto) {
        project.setProjectName(dto.getProjectName());
        project.setRole(dto.getRole());
        project.setTeamSize(dto.getTeamSize());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setIsCurrent(dto.getIsCurrent());
        project.setDescription(dto.getDescription());
        project.setResponsibilities(dto.getResponsibilities());
        project.setTechnologies(dto.getTechnologies());
        project.setProjectUrl(dto.getProjectUrl());
        project.setAchievements(dto.getAchievements());
    }

    private void applyAwardUpdates(ResumeAward award, ResumeAwardDTO dto) {
        award.setAwardName(dto.getAwardName());
        award.setCompetitionName(dto.getCompetitionName());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardDate(dto.getAwardDate());
        award.setAwardOrg(dto.getAwardOrg());
        award.setDescription(dto.getDescription());
    }

    // ==================== 验证方法 ====================

    private Result validateCreateResumeParams(ResumeDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "简历信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(dto.getResumeName())) {
            return Result.error(400, "简历名称不能为空");
        }
        return null;
    }

    private Result validateUpdateResumeParams(Integer resumeId, ResumeDTO dto, Integer userId) {
        if (resumeId == null) {
            return Result.error(400, "简历ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "简历信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private Result validateIdAndUserId(Integer id, Integer userId) {
        if (id == null) {
            return Result.error(400, "ID不能为空");
        }
        if (id <= 0) {
            return Result.error(400, "ID必须大于0");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private Result validateSubItemIdAndUserId(Integer itemId, Integer userId, String itemName) {
        Result result = validateIdAndUserId(itemId, userId);
        if (result != null) {
            return Result.error(result.getCode(), itemName + result.getMessage().substring(2));
        }
        return null;
    }

    private Result validateEducationParams(Integer resumeId, ResumeEducationDTO dto, Integer userId) {
        if (resumeId == null) {
            return Result.error(400, "简历ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "教育经历信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(dto.getSchoolName())) {
            return Result.error(400, "学校名称不能为空");
        }
        return null;
    }

    private Result validateSkillParams(Integer resumeId, ResumeSkillDTO dto, Integer userId) {
        if (resumeId == null) {
            return Result.error(400, "简历ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "技能信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(dto.getSkillName())) {
            return Result.error(400, "技能名称不能为空");
        }
        if (isBlank(dto.getProficiency())) {
            return Result.error(400, "熟练程度不能为空");
        }
        if (!VALID_PROFICIENCIES.contains(dto.getProficiency())) {
            return Result.error(400, "熟练程度枚举值无效");
        }
        if (dto.getProficiencyScore() != null &&
            (dto.getProficiencyScore() < 0 || dto.getProficiencyScore() > 100)) {
            return Result.error(400, "熟练度分数必须在0-100之间");
        }
        return null;
    }

    private Result validateProjectParams(Integer resumeId, ResumeProjectDTO dto, Integer userId) {
        if (resumeId == null) {
            return Result.error(400, "简历ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "项目经历信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(dto.getProjectName())) {
            return Result.error(400, "项目名称不能为空");
        }
        return null;
    }

    private Result validateAwardParams(Integer resumeId, ResumeAwardDTO dto, Integer userId) {
        if (resumeId == null) {
            return Result.error(400, "简历ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "获奖信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(dto.getAwardName())) {
            return Result.error(400, "奖项名称不能为空");
        }
        return null;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // ==================== 构建方法 ====================

    private Resume buildResumeFromDTO(ResumeDTO dto, Integer userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setResumeName(dto.getResumeName());
        resume.setTemplateStyle(dto.getTemplateStyle() != null ? dto.getTemplateStyle() : "default");
        resume.setSummary(dto.getSummary());
        resume.setCareerObjective(dto.getCareerObjective());
        resume.setPhone(dto.getPhone());
        resume.setEmail(dto.getEmail());
        resume.setWechat(dto.getWechat());
        resume.setGithubUrl(dto.getGithubUrl());
        resume.setBlogUrl(dto.getBlogUrl());
        return resume;
    }

    private ResumeEducation buildEducationFromDTO(ResumeEducationDTO dto) {
        ResumeEducation education = new ResumeEducation();
        education.setSchoolName(dto.getSchoolName());
        education.setMajor(dto.getMajor());
        education.setDegree(dto.getDegree());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : 0);
        education.setDescription(dto.getDescription());
        education.setDisplayOrder(0);
        return education;
    }

    private ResumeSkill buildSkillFromDTO(ResumeSkillDTO dto) {
        ResumeSkill skill = new ResumeSkill();
        skill.setSkillName(dto.getSkillName());
        skill.setProficiency(dto.getProficiency());
        skill.setProficiencyScore(dto.getProficiencyScore() != null ? dto.getProficiencyScore() : 50);
        skill.setCategory(dto.getCategory());
        skill.setDescription(dto.getDescription());
        skill.setDisplayOrder(0);
        return skill;
    }

    private ResumeProject buildProjectFromDTO(ResumeProjectDTO dto) {
        ResumeProject project = new ResumeProject();
        project.setProjectName(dto.getProjectName());
        project.setRole(dto.getRole());
        project.setTeamSize(dto.getTeamSize());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setIsCurrent(dto.getIsCurrent() != null ? dto.getIsCurrent() : 0);
        project.setDescription(dto.getDescription());
        project.setResponsibilities(dto.getResponsibilities());
        project.setTechnologies(dto.getTechnologies());
        project.setProjectUrl(dto.getProjectUrl());
        project.setAchievements(dto.getAchievements());
        project.setDisplayOrder(0);
        project.setIsFromSystem(0);
        return project;
    }

    private ResumeAward buildAwardFromDTO(ResumeAwardDTO dto) {
        ResumeAward award = new ResumeAward();
        award.setAwardName(dto.getAwardName());
        award.setCompetitionName(dto.getCompetitionName());
        award.setAwardLevel(dto.getAwardLevel());
        award.setAwardDate(dto.getAwardDate());
        award.setAwardOrg(dto.getAwardOrg());
        award.setDescription(dto.getDescription());
        award.setIsFromSystem(0);
        award.setDisplayOrder(0);
        return award;
    }

}
