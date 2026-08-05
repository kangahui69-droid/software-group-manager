package com.softwaregroup.hr.service;

import com.softwaregroup.hr.dao.*;
import com.softwaregroup.hr.model.entity.*;
import com.softwaregroup.hr.model.dto.*;
import com.softwaregroup.common.util.Result;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 简历服务层
 */
@Service
public class ResumeService {

    // ==================== 状态常量 ====================
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_HIDDEN = 2;

    public static final int DELETED_NO = 0;
    public static final int DELETED_YES = 1;

    public static final int DEFAULT_YES = 1;
    public static final int DEFAULT_NO = 0;

    // ==================== 熟练度常量 ====================
    public static final String PROFICIENCY_BEGINNER = "beginner";
    public static final String PROFICIENCY_ELEMENTARY = "elementary";
    public static final String PROFICIENCY_INTERMEDIATE = "intermediate";
    public static final String PROFICIENCY_ADVANCED = "advanced";
    public static final String PROFICIENCY_EXPERT = "expert";

    private static final List<String> VALID_PROFICIENCIES = Arrays.asList(
            PROFICIENCY_BEGINNER, PROFICIENCY_ELEMENTARY,
            PROFICIENCY_INTERMEDIATE, PROFICIENCY_ADVANCED, PROFICIENCY_EXPERT
    );

    // ==================== DAO依赖 ====================
    private final ResumeDAO resumeDAO;
    private final ResumeEducationDAO educationDAO;
    private final ResumeSkillDAO skillDAO;
    private final ResumeProjectDAO projectDAO;
    private final ResumeAwardDAO awardDAO;

    public ResumeService(ResumeDAO resumeDAO, ResumeEducationDAO educationDAO,
                        ResumeSkillDAO skillDAO, ResumeProjectDAO projectDAO,
                        ResumeAwardDAO awardDAO) {
        this.resumeDAO = resumeDAO;
        this.educationDAO = educationDAO;
        this.skillDAO = skillDAO;
        this.projectDAO = projectDAO;
        this.awardDAO = awardDAO;
    }

    // ==================== 简历主表CRUD ====================

    public Result createResume(ResumeDTO dto, Integer userId) {
        Result validation = validateCreateResumeParams(dto, userId);
        if (validation != null) return validation;

        try {
            boolean isFirst = isEmpty(resumeDAO.findByUserId(userId));
            Resume resume = buildResumeFromDTO(dto, userId);
            resume.setIsDefault(isFirst ? DEFAULT_YES : DEFAULT_NO);
            resume.setStatus(STATUS_PUBLISHED);

            return resumeDAO.save(resume) ? Result.ok(resume)
                    : Result.error(500, "创建简历失败");
        } catch (Exception e) {
            return Result.error(500, "创建简历失败: " + e.getMessage());
        }
    }

    public Result updateResume(Integer resumeId, ResumeDTO dto, Integer userId) {
        Result validation = validateUpdateResumeParams(resumeId, dto, userId);
        if (validation != null) return validation;

        try {
            Resume resume = findResumeOrFail(resumeId);
            if (isDeleted(resume)) return Result.error(400, "简历已删除");
            if (!isOwner(resume, userId)) return Result.error(403, "无权限更新此简历");

            applyResumeUpdates(resume, dto);
            return resumeDAO.update(resume) ? Result.ok(resume)
                    : Result.error(500, "更新简历失败");
        } catch (ResumeNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "更新简历失败: " + e.getMessage());
        }
    }

    public Result deleteResume(Integer resumeId, Integer userId) {
        return executeResumeOperation(resumeId, userId, "删除",
                () -> resumeDAO.softDelete(resumeId), "删除简历失败");
    }

    public Result setDefaultResume(Integer resumeId, Integer userId) {
        return executeResumeOperation(resumeId, userId, "设置",
                () -> resumeDAO.setDefaultResume(resumeId, userId), "设置默认简历失败");
    }

    public Result getResumeDetail(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) return validation;

        try {
            Resume resume = findResumeOrFail(resumeId);
            if (isDeleted(resume)) return Result.error(400, "简历已删除");
            if (!isOwner(resume, userId)) return Result.error(403, "无权限查看此简历");

            loadSubItems(resume);
            return Result.ok(resume);
        } catch (ResumeNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取简历详情失败: " + e.getMessage());
        }
    }

    public Result listResumes(Integer userId, int page) {
        if (userId == null) return Result.error(400, "用户ID不能为空");
        try {
            return Result.ok(resumeDAO.findByUserId(userId));
        } catch (Exception e) {
            return Result.error(500, "获取简历列表失败: " + e.getMessage());
        }
    }

    public Result getRecycleBin(Integer userId) {
        if (userId == null) return Result.error(400, "用户ID不能为空");
        try {
            return Result.ok(resumeDAO.findDeletedByUserId(userId));
        } catch (Exception e) {
            return Result.error(500, "获取回收站失败: " + e.getMessage());
        }
    }

    public Result restoreResume(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) return validation;

        try {
            Resume resume = findResumeOrFail(resumeId, true);
            if (!isDeleted(resume)) return Result.error(400, "只能恢复已删除的简历");
            if (!isOwner(resume, userId)) return Result.error(403, "无权限恢复此简历");

            return resumeDAO.restore(resumeId) ? Result.ok()
                    : Result.error(500, "恢复简历失败");
        } catch (Exception e) {
            return Result.error(500, "恢复简历失败: " + e.getMessage());
        }
    }

    public Result permanentDelete(Integer resumeId, Integer userId) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) return validation;

        try {
            Resume resume = findResumeOrFail(resumeId, true);
            if (!isDeleted(resume)) return Result.error(400, "只能永久删除已回收的简历");
            if (!isOwner(resume, userId)) return Result.error(403, "无权限删除此简历");

            return resumeDAO.hardDelete(resumeId) ? Result.ok()
                    : Result.error(500, "永久删除简历失败");
        } catch (Exception e) {
            return Result.error(500, "永久删除简历失败: " + e.getMessage());
        }
    }

    // ==================== 子项目CRUD ====================

    public Result addEducation(Integer resumeId, ResumeEducationDTO dto, Integer userId) {
        if (resumeId == null) return Result.error(400, "简历ID不能为空");
        if (dto == null) return Result.error(400, "教育经历信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        if (isBlank(dto.getSchoolName())) return Result.error(400, "学校名称不能为空");

        return addSubItem(resumeId, dto, userId, "教育经历",
                () -> {
                    ResumeEducation edu = buildEducationFromDTO(dto);
                    edu.setResumeId(resumeId);
                    return edu;
                },
                educationDAO::save);
    }

    public Result updateEducation(Integer educationId, ResumeEducationDTO dto, Integer userId) {
        return updateSubItem(educationId, userId, "教育经历",
                educationDAO::findById, ResumeEducation::getResumeId,
                (edu) -> applyEducationUpdates(edu, dto),
                educationDAO::update);
    }

    public Result deleteEducation(Integer educationId, Integer userId) {
        return deleteSubItem(educationId, userId, "教育经历",
                educationDAO::findById, ResumeEducation::getResumeId,
                educationDAO::delete);
    }

    public Result addSkill(Integer resumeId, ResumeSkillDTO dto, Integer userId) {
        Result validation = validateSkillParams(dto);
        if (validation != null) return validation;
        return addSubItem(resumeId, dto, userId, "技能",
                () -> {
                    ResumeSkill skill = buildSkillFromDTO(dto);
                    skill.setResumeId(resumeId);
                    return skill;
                },
                skillDAO::save);
    }

    public Result updateSkill(Integer skillId, ResumeSkillDTO dto, Integer userId) {
        Result validation = validateSkillParams(dto);
        if (validation != null) return validation;
        return updateSubItem(skillId, userId, "技能",
                skillDAO::findById, ResumeSkill::getResumeId,
                (skill) -> applySkillUpdates(skill, dto),
                skillDAO::update);
    }

    public Result deleteSkill(Integer skillId, Integer userId) {
        return deleteSubItem(skillId, userId, "技能",
                skillDAO::findById, ResumeSkill::getResumeId,
                skillDAO::delete);
    }

    public Result addProject(Integer resumeId, ResumeProjectDTO dto, Integer userId) {
        if (resumeId == null) return Result.error(400, "简历ID不能为空");
        if (dto == null) return Result.error(400, "项目经历信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        if (isBlank(dto.getProjectName())) return Result.error(400, "项目名称不能为空");

        return addSubItem(resumeId, dto, userId, "项目经历",
                () -> {
                    ResumeProject project = buildProjectFromDTO(dto);
                    project.setResumeId(resumeId);
                    return project;
                },
                projectDAO::save);
    }

    public Result updateProject(Integer projectId, ResumeProjectDTO dto, Integer userId) {
        return updateSubItem(projectId, userId, "项目经历",
                projectDAO::findById, ResumeProject::getResumeId,
                (project) -> applyProjectUpdates(project, dto),
                projectDAO::update);
    }

    public Result deleteProject(Integer projectId, Integer userId) {
        return deleteSubItem(projectId, userId, "项目经历",
                projectDAO::findById, ResumeProject::getResumeId,
                projectDAO::delete);
    }

    public Result addAward(Integer resumeId, ResumeAwardDTO dto, Integer userId) {
        if (resumeId == null) return Result.error(400, "简历ID不能为空");
        if (dto == null) return Result.error(400, "获奖信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        if (isBlank(dto.getAwardName())) return Result.error(400, "奖项名称不能为空");

        return addSubItem(resumeId, dto, userId, "获奖情况",
                () -> {
                    ResumeAward award = buildAwardFromDTO(dto);
                    award.setResumeId(resumeId);
                    return award;
                },
                awardDAO::save);
    }

    public Result updateAward(Integer awardId, ResumeAwardDTO dto, Integer userId) {
        return updateSubItem(awardId, userId, "获奖情况",
                awardDAO::findById, ResumeAward::getResumeId,
                (award) -> applyAwardUpdates(award, dto),
                awardDAO::update);
    }

    public Result deleteAward(Integer awardId, Integer userId) {
        return deleteSubItem(awardId, userId, "获奖情况",
                awardDAO::findById, ResumeAward::getResumeId,
                awardDAO::delete);
    }

    // ==================== 私有辅助方法 ====================

    private Result executeResumeOperation(Integer resumeId, Integer userId, String operationName,
                                          java.util.function.Supplier<Boolean> operation, String errorMessage) {
        Result validation = validateIdAndUserId(resumeId, userId);
        if (validation != null) return validation;

        try {
            Resume resume = findResumeOrFail(resumeId);
            if (isDeleted(resume)) return Result.error(400, "简历已删除");
            if (!isOwner(resume, userId)) return Result.error(403, "无权限" + operationName + "此简历");

            return operation.get() ? Result.ok() : Result.error(500, errorMessage);
        } catch (ResumeNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, errorMessage + ": " + e.getMessage());
        }
    }

    private Resume findResumeOrFail(Integer resumeId) {
        return findResumeOrFail(resumeId, false);
    }

    private Resume findResumeOrFail(Integer resumeId, boolean includeDeleted) {
        Resume resume = includeDeleted
                ? resumeDAO.findById(resumeId, true)
                : resumeDAO.findById(resumeId);
        if (resume == null) {
            throw new ResumeNotFoundException("简历不存在");
        }
        return resume;
    }

    private boolean isOwner(Resume resume, Integer userId) {
        return resume.getUserId().equals(userId);
    }

    private void loadSubItems(Resume resume) {
        Integer id = resume.getId();
        resume.setEducations(educationDAO.findByResumeId(id));
        resume.setSkills(skillDAO.findByResumeId(id));
        resume.setProjects(projectDAO.findByResumeId(id));
        resume.setAwards(awardDAO.findByResumeId(id));
    }

    // ==================== 子项目通用CRUD ====================

    private <T> Result addSubItem(Integer resumeId, Object dto, Integer userId, String itemName,
                                   java.util.function.Supplier<T> builder,
                                   java.util.function.Function<T, Boolean> saver) {
        if (resumeId == null) return Result.error(400, "简历ID不能为空");
        if (dto == null) return Result.error(400, itemName + "信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");

        try {
            Resume resume = findResumeOrFail(resumeId);
            if (!isOwner(resume, userId)) return Result.error(403, "无权限添加" + itemName);

            T item = builder.get();
            return saver.apply(item) ? Result.ok(item)
                    : Result.error(500, "添加" + itemName + "失败");
        } catch (ResumeNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "添加" + itemName + "失败: " + e.getMessage());
        }
    }

    private <T> Result updateSubItem(Integer itemId, Integer userId, String itemName,
                                     java.util.function.Function<Integer, T> finder,
                                     java.util.function.Function<T, Integer> resumeIdGetter,
                                     java.util.function.Consumer<T> updater,
                                     java.util.function.Function<T, Boolean> saver) {
        Result validation = validateSubItemIdAndUserId(itemId, userId, itemName);
        if (validation != null) return validation;

        try {
            T item = finder.apply(itemId);
            if (item == null) return Result.error(404, itemName + "不存在");

            Resume resume = findResumeOrFail(resumeIdGetter.apply(item));
            if (!isOwner(resume, userId)) return Result.error(403, "无权限更新此" + itemName);

            updater.accept(item);
            return saver.apply(item) ? Result.ok(item)
                    : Result.error(500, "更新" + itemName + "失败");
        } catch (Exception e) {
            return Result.error(500, "更新" + itemName + "失败: " + e.getMessage());
        }
    }

    private <T> Result deleteSubItem(Integer itemId, Integer userId, String itemName,
                                      java.util.function.Function<Integer, T> finder,
                                      java.util.function.Function<T, Integer> resumeIdGetter,
                                      java.util.function.Function<Integer, Boolean> deleter) {
        Result validation = validateSubItemIdAndUserId(itemId, userId, itemName);
        if (validation != null) return validation;

        try {
            T item = finder.apply(itemId);
            if (item == null) return Result.error(404, itemName + "不存在");

            Resume resume = findResumeOrFail(resumeIdGetter.apply(item));
            if (!isOwner(resume, userId)) return Result.error(403, "无权限删除此" + itemName);

            return deleter.apply(itemId) ? Result.ok()
                    : Result.error(500, "删除" + itemName + "失败");
        } catch (Exception e) {
            return Result.error(500, "删除" + itemName + "失败: " + e.getMessage());
        }
    }

    // ==================== 验证方法 ====================

    private Result validateCreateResumeParams(ResumeDTO dto, Integer userId) {
        if (dto == null) return Result.error(400, "简历信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        if (isBlank(dto.getResumeName())) return Result.error(400, "简历名称不能为空");
        return null;
    }

    private Result validateUpdateResumeParams(Integer resumeId, ResumeDTO dto, Integer userId) {
        if (resumeId == null) return Result.error(400, "简历ID不能为空");
        if (resumeId <= 0) return Result.error(400, "ID必须大于0");
        if (dto == null) return Result.error(400, "简历信息不能为空");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        return null;
    }

    private Result validateIdAndUserId(Integer id, Integer userId) {
        if (id == null) return Result.error(400, "ID不能为空");
        if (id <= 0) return Result.error(400, "ID必须大于0");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        return null;
    }

    private Result validateSubItemIdAndUserId(Integer itemId, Integer userId, String itemName) {
        if (itemId == null) return Result.error(400, "ID不能为空");
        if (itemId <= 0) return Result.error(400, "ID必须大于0");
        if (userId == null) return Result.error(400, "用户ID不能为空");
        return null;
    }

    private Result validateSkillParams(ResumeSkillDTO dto) {
        if (dto == null) return Result.error(400, "技能信息不能为空");
        if (isBlank(dto.getSkillName())) return Result.error(400, "技能名称不能为空");
        if (isBlank(dto.getProficiency())) return Result.error(400, "熟练程度不能为空");
        if (!VALID_PROFICIENCIES.contains(dto.getProficiency())) {
            return Result.error(400, "熟练程度枚举值无效");
        }
        if (dto.getProficiencyScore() != null &&
            (dto.getProficiencyScore() < 0 || dto.getProficiencyScore() > 100)) {
            return Result.error(400, "熟练度分数必须在0-100之间");
        }
        return null;
    }

    // ==================== DTO -> Entity 转换 ====================

    private Resume buildResumeFromDTO(ResumeDTO dto, Integer userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setResumeName(dto.getResumeName());
        resume.setTemplateStyle(nvl(dto.getTemplateStyle(), "default"));
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
        ResumeEducation edu = new ResumeEducation();
        edu.setSchoolName(dto.getSchoolName());
        edu.setMajor(dto.getMajor());
        edu.setDegree(dto.getDegree());
        edu.setStartDate(dto.getStartDate());
        edu.setEndDate(dto.getEndDate());
        edu.setIsCurrent(nvl(dto.getIsCurrent(), 0));
        edu.setDescription(dto.getDescription());
        edu.setDisplayOrder(0);
        return edu;
    }

    private ResumeSkill buildSkillFromDTO(ResumeSkillDTO dto) {
        ResumeSkill skill = new ResumeSkill();
        skill.setSkillName(dto.getSkillName());
        skill.setProficiency(dto.getProficiency());
        skill.setProficiencyScore(nvl(dto.getProficiencyScore(), 50));
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
        project.setIsCurrent(nvl(dto.getIsCurrent(), 0));
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

    // ==================== Entity 更新方法 ====================

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

    private void applyEducationUpdates(ResumeEducation edu, ResumeEducationDTO dto) {
        edu.setSchoolName(dto.getSchoolName());
        edu.setMajor(dto.getMajor());
        edu.setDegree(dto.getDegree());
        edu.setStartDate(dto.getStartDate());
        edu.setEndDate(dto.getEndDate());
        edu.setIsCurrent(dto.getIsCurrent());
        edu.setDescription(dto.getDescription());
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

    // ==================== 工具方法 ====================

    private boolean isDeleted(Resume resume) {
        return resume.getDeleted() != null && resume.getDeleted() == DELETED_YES;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    private <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    // ==================== 内部异常 ====================

    private static class ResumeNotFoundException extends RuntimeException {
        ResumeNotFoundException(String message) {
            super(message);
        }
    }
}
