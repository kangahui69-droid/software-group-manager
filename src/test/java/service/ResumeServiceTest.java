package service;

import dao.*;
import dto.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResumeService TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 4.4 ResumeService 简历服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock依赖：
 * - ResumeDAO: save / update / findById / findByUserId / findDeletedByUserId / softDelete / restore / hardDelete / setDefaultResume / findDefaultByUserId
 * - ResumeEducationDAO: save / update / findById / findByResumeId / delete
 * - ResumeSkillDAO: save / update / findById / findByResumeId / delete
 * - ResumeProjectDAO: save / update / findById / findByResumeId / delete
 * - ResumeAwardDAO: save / update / findById / findByResumeId / delete
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ResumeService 简历服务测试")
class ResumeServiceTest {

    @Mock
    private ResumeDAO resumeDAO;

    @Mock
    private ResumeEducationDAO educationDAO;

    @Mock
    private ResumeSkillDAO skillDAO;

    @Mock
    private ResumeProjectDAO projectDAO;

    @Mock
    private ResumeAwardDAO awardDAO;

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(resumeDAO, educationDAO, skillDAO, projectDAO, awardDAO);
    }

    // ==================== 测试数据常量 ====================

    private static final Integer USER_ID = 5;
    private static final Integer RESUME_ID = 1;
    private static final Integer OTHER_USER_ID = 6;
    private static final Integer OPERATOR_ID = 1;

    // 简历状态枚举
    private static final Integer STATUS_DRAFT = 0;
    private static final Integer STATUS_PUBLISHED = 1;
    private static final Integer STATUS_HIDDEN = 2;

    // 删除状态枚举
    private static final Integer DELETED_NO = 0;
    private static final Integer DELETED_YES = 1;

    // 默认简历标识
    private static final Integer DEFAULT_YES = 1;
    private static final Integer DEFAULT_NO = 0;

    // 技能熟练程度枚举
    private static final String PROFICIENCY_BEGINNER = "beginner";
    private static final String PROFICIENCY_ELEMENTARY = "elementary";
    private static final String PROFICIENCY_INTERMEDIATE = "intermediate";
    private static final String PROFICIENCY_ADVANCED = "advanced";
    private static final String PROFICIENCY_EXPERT = "expert";

    // ==================== 测试数据构建 ====================

    private Resume createResume(Integer id, Integer userId, String name, Integer isDefault, Integer status, Integer deleted) {
        Resume resume = new Resume();
        resume.setId(id);
        resume.setUserId(userId);
        resume.setResumeName(name);
        resume.setTemplateStyle("default");
        resume.setSummary("个人简介");
        resume.setCareerObjective("求职意向");
        resume.setPhone("13800138000");
        resume.setEmail("test@example.com");
        resume.setWechat("wechat123");
        resume.setGithubUrl("https://github.com/test");
        resume.setBlogUrl("https://blog.example.com");
        resume.setIsDefault(isDefault);
        resume.setStatus(status);
        resume.setViewCount(0);
        resume.setDeleted(deleted);
        return resume;
    }

    private ResumeDTO createResumeDTO(String name) {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName(name);
        dto.setTemplateStyle("default");
        dto.setSummary("个人简介");
        dto.setCareerObjective("求职意向");
        dto.setPhone("13800138000");
        dto.setEmail("test@example.com");
        dto.setWechat("wechat123");
        dto.setGithubUrl("https://github.com/test");
        dto.setBlogUrl("https://blog.example.com");
        return dto;
    }

    private ResumeEducation createEducation(Integer id, Integer resumeId, String schoolName) {
        ResumeEducation edu = new ResumeEducation();
        edu.setId(id);
        edu.setResumeId(resumeId);
        edu.setSchoolName(schoolName);
        edu.setMajor("计算机科学与技术");
        edu.setDegree("本科");
        edu.setStartDate(Date.valueOf("2020-09-01"));
        edu.setEndDate(Date.valueOf("2024-06-30"));
        edu.setIsCurrent(0);
        edu.setDescription("在校经历");
        edu.setDisplayOrder(0);
        return edu;
    }

    private ResumeEducationDTO createEducationDTO(String schoolName) {
        ResumeEducationDTO dto = new ResumeEducationDTO();
        dto.setSchoolName(schoolName);
        dto.setMajor("计算机科学与技术");
        dto.setDegree("本科");
        dto.setStartDate(Date.valueOf("2020-09-01"));
        dto.setEndDate(Date.valueOf("2024-06-30"));
        dto.setIsCurrent(0);
        dto.setDescription("在校经历");
        return dto;
    }

    private ResumeSkill createSkill(Integer id, Integer resumeId, String skillName, String proficiency) {
        ResumeSkill skill = new ResumeSkill();
        skill.setId(id);
        skill.setResumeId(resumeId);
        skill.setSkillName(skillName);
        skill.setProficiency(proficiency);
        skill.setProficiencyScore(50);
        skill.setCategory("编程语言");
        skill.setDescription("技能描述");
        skill.setDisplayOrder(0);
        return skill;
    }

    private ResumeSkillDTO createSkillDTO(String skillName, String proficiency) {
        ResumeSkillDTO dto = new ResumeSkillDTO();
        dto.setSkillName(skillName);
        dto.setProficiency(proficiency);
        dto.setProficiencyScore(50);
        dto.setCategory("编程语言");
        dto.setDescription("技能描述");
        return dto;
    }

    private ResumeProject createProject(Integer id, Integer resumeId, String projectName) {
        ResumeProject project = new ResumeProject();
        project.setId(id);
        project.setResumeId(resumeId);
        project.setProjectName(projectName);
        project.setRole("负责人");
        project.setTeamSize(5);
        project.setStartDate(Date.valueOf("2023-01-01"));
        project.setEndDate(Date.valueOf("2023-06-30"));
        project.setIsCurrent(0);
        project.setDescription("项目描述");
        project.setResponsibilities("职责描述");
        project.setTechnologies("Java, Spring");
        project.setProjectUrl("https://project.example.com");
        project.setAchievements("项目成果");
        project.setDisplayOrder(0);
        project.setIsFromSystem(0);
        return project;
    }

    private ResumeProjectDTO createProjectDTO(String projectName) {
        ResumeProjectDTO dto = new ResumeProjectDTO();
        dto.setProjectName(projectName);
        dto.setRole("负责人");
        dto.setTeamSize(5);
        dto.setStartDate(Date.valueOf("2023-01-01"));
        dto.setEndDate(Date.valueOf("2023-06-30"));
        dto.setIsCurrent(0);
        dto.setDescription("项目描述");
        dto.setResponsibilities("职责描述");
        dto.setTechnologies("Java, Spring");
        dto.setProjectUrl("https://project.example.com");
        dto.setAchievements("项目成果");
        return dto;
    }

    private ResumeAward createAward(Integer id, Integer resumeId, String awardName) {
        ResumeAward award = new ResumeAward();
        award.setId(id);
        award.setResumeId(resumeId);
        award.setAwardName(awardName);
        award.setCompetitionName("蓝桥杯");
        award.setAwardLevel("一等奖");
        award.setAwardDate(Date.valueOf("2024-03-20"));
        award.setAwardOrg("黄山学院");
        award.setDescription("获奖描述");
        award.setIsFromSystem(0);
        award.setDisplayOrder(0);
        return award;
    }

    private ResumeAwardDTO createAwardDTO(String awardName) {
        ResumeAwardDTO dto = new ResumeAwardDTO();
        dto.setAwardName(awardName);
        dto.setCompetitionName("蓝桥杯");
        dto.setAwardLevel("一等奖");
        dto.setAwardDate(Date.valueOf("2024-03-20"));
        dto.setAwardOrg("黄山学院");
        dto.setDescription("获奖描述");
        return dto;
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("简历状态常量应与Resume模型一致")
        void resume_status_constants_should_match() {
            assertThat(ResumeService.STATUS_DRAFT).isEqualTo(0);
            assertThat(ResumeService.STATUS_PUBLISHED).isEqualTo(1);
            assertThat(ResumeService.STATUS_HIDDEN).isEqualTo(2);
        }

        @Test
        @DisplayName("删除状态常量应正确")
        void delete_status_constants_should_match() {
            assertThat(ResumeService.DELETED_NO).isEqualTo(0);
            assertThat(ResumeService.DELETED_YES).isEqualTo(1);
        }

        @Test
        @DisplayName("默认简历标识常量应正确")
        void default_resume_constants_should_match() {
            assertThat(ResumeService.DEFAULT_YES).isEqualTo(1);
            assertThat(ResumeService.DEFAULT_NO).isEqualTo(0);
        }

        @Test
        @DisplayName("技能熟练程度常量应正确")
        void proficiency_constants_should_match() {
            assertThat(ResumeService.PROFICIENCY_BEGINNER).isEqualTo("beginner");
            assertThat(ResumeService.PROFICIENCY_ELEMENTARY).isEqualTo("elementary");
            assertThat(ResumeService.PROFICIENCY_INTERMEDIATE).isEqualTo("intermediate");
            assertThat(ResumeService.PROFICIENCY_ADVANCED).isEqualTo("advanced");
            assertThat(ResumeService.PROFICIENCY_EXPERT).isEqualTo("expert");
        }
    }

    // ==================== createResume 创建简历 ====================

    @Nested
    @DisplayName("createResume 创建简历")
    class CreateResumeTests {

        @FastTest
        @DisplayName("创建简历成功应返回成功")
        void should_create_resume_successfully() {
            ResumeDTO dto = createResumeDTO("我的简历");

            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            Resume saved = captor.getValue();
            assertThat(saved.getResumeName()).isEqualTo("我的简历");
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
        }

        @FastTest
        @DisplayName("创建第一个简历时应设为默认")
        void should_set_first_resume_as_default() {
            ResumeDTO dto = createResumeDTO("第一个简历");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            assertThat(captor.getValue().getIsDefault()).isEqualTo(DEFAULT_YES);
        }

        @FastTest
        @DisplayName("创建非第一个简历时不应设为默认")
        void should_not_set_non_first_resume_as_default() {
            ResumeDTO dto = createResumeDTO("第二个简历");
            Resume existing = createResume(1, USER_ID, "第一个简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.singletonList(existing));
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            assertThat(captor.getValue().getIsDefault()).isEqualTo(DEFAULT_NO);
        }

        @FastTest
        @DisplayName("创建简历时应设置默认状态为已发布")
        void should_set_default_status_as_published() {
            ResumeDTO dto = createResumeDTO("我的简历");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(STATUS_PUBLISHED);
        }

        @FastTest
        @DisplayName("创建简历时姓名为空应返回错误")
        void should_return_error_when_name_empty() {
            ResumeDTO dto = createResumeDTO("");
            dto.setResumeName("");

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建简历时姓名为null应返回错误")
        void should_return_error_when_name_null() {
            ResumeDTO dto = createResumeDTO("我的简历");
            dto.setResumeName(null);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建简历时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = resumeService.createResume(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建简历时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            ResumeDTO dto = createResumeDTO("我的简历");

            Result result = resumeService.createResume(dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建简历时应保存所有字段")
        void should_save_all_fields_when_create() {
            ResumeDTO dto = createResumeDTO("完整简历");
            dto.setTemplateStyle("academic");
            dto.setSummary("详细简介");
            dto.setCareerObjective("后端开发");
            dto.setPhone("13900139000");
            dto.setEmail("full@example.com");
            dto.setWechat("wechat456");
            dto.setGithubUrl("https://github.com/full");
            dto.setBlogUrl("https://blog.full.com");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            Resume saved = captor.getValue();
            assertThat(saved.getTemplateStyle()).isEqualTo("academic");
            assertThat(saved.getSummary()).isEqualTo("详细简介");
            assertThat(saved.getCareerObjective()).isEqualTo("后端开发");
            assertThat(saved.getPhone()).isEqualTo("13900139000");
            assertThat(saved.getEmail()).isEqualTo("full@example.com");
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            ResumeDTO dto = createResumeDTO("我的简历");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(false);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            ResumeDTO dto = createResumeDTO("我的简历");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("姓名仅包含空格应返回错误")
        void should_return_error_when_name_only_spaces() {
            ResumeDTO dto = createResumeDTO("   ");

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateResume 更新简历 ====================

    @Nested
    @DisplayName("updateResume 更新简历")
    class UpdateResumeTests {

        @FastTest
        @DisplayName("更新简历成功应返回成功")
        void should_update_resume_successfully() {
            ResumeDTO dto = createResumeDTO("更新后的简历");
            Resume existing = createResume(RESUME_ID, USER_ID, "原简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.update(any(Resume.class))).thenReturn(true);

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).update(captor.capture());
            assertThat(captor.getValue().getResumeName()).isEqualTo("更新后的简历");
        }

        @FastTest
        @DisplayName("更新简历时应保留原userId")
        void should_preserve_user_id_when_update() {
            ResumeDTO dto = createResumeDTO("更新后的简历");
            Resume existing = createResume(RESUME_ID, USER_ID, "原简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.update(any(Resume.class))).thenReturn(true);

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).update(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        }

        @FastTest
        @DisplayName("更新他人简历应返回错误")
        void should_return_error_when_update_others_resume() {
            ResumeDTO dto = createResumeDTO("更新的简历");
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的简历应返回错误")
        void should_return_error_when_resume_not_exists() {
            ResumeDTO dto = createResumeDTO("更新的简历");
            when(resumeDAO.findById(999)).thenReturn(null);

            Result result = resumeService.updateResume(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新已删除的简历应返回错误")
        void should_return_error_when_resume_already_deleted() {
            ResumeDTO dto = createResumeDTO("更新的简历");
            Resume existing = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新简历时id为null应返回错误")
        void should_return_error_when_id_null() {
            ResumeDTO dto = createResumeDTO("更新的简历");

            Result result = resumeService.updateResume(null, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新简历时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            ResumeDTO dto = createResumeDTO("更新的简历");

            Result result = resumeService.updateResume(RESUME_ID, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新简历时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = resumeService.updateResume(RESUME_ID, null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            ResumeDTO dto = createResumeDTO("更新的简历");
            Resume existing = createResume(RESUME_ID, USER_ID, "原简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.update(any(Resume.class))).thenReturn(false);

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            ResumeDTO dto = createResumeDTO("更新的简历");
            Resume existing = createResume(RESUME_ID, USER_ID, "原简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.update(any(Resume.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.updateResume(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteResume 删除简历（软删除）====================

    @Nested
    @DisplayName("deleteResume 删除简历（软删除）")
    class DeleteResumeTests {

        @FastTest
        @DisplayName("删除简历成功应返回成功")
        void should_delete_resume_successfully() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.softDelete(RESUME_ID)).thenReturn(true);

            Result result = resumeService.deleteResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(resumeDAO).softDelete(RESUME_ID);
        }

        @FastTest
        @DisplayName("删除他人简历应返回错误")
        void should_return_error_when_delete_others_resume() {
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.deleteResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的简历应返回错误")
        void should_return_error_when_resume_not_exists() {
            when(resumeDAO.findById(999)).thenReturn(null);

            Result result = resumeService.deleteResume(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除已删除的简历应返回错误")
        void should_return_error_when_already_deleted() {
            Resume existing = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.deleteResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除简历时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.deleteResume(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除简历时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.deleteResume(RESUME_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库删除失败应返回错误")
        void should_return_error_when_database_delete_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.softDelete(RESUME_ID)).thenReturn(false);

            Result result = resumeService.deleteResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.softDelete(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.deleteResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== setDefaultResume 设置默认简历 ====================

    @Nested
    @DisplayName("setDefaultResume 设置默认简历")
    class SetDefaultResumeTests {

        @FastTest
        @DisplayName("设置默认简历成功应返回成功")
        void should_set_default_resume_successfully() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.setDefaultResume(RESUME_ID, USER_ID)).thenReturn(true);

            Result result = resumeService.setDefaultResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(resumeDAO).setDefaultResume(RESUME_ID, USER_ID);
        }

        @FastTest
        @DisplayName("设置他人简历为默认应返回错误")
        void should_return_error_when_set_others_resume_as_default() {
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.setDefaultResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("设置不存在的简历为默认应返回错误")
        void should_return_error_when_resume_not_exists() {
            when(resumeDAO.findById(999)).thenReturn(null);

            Result result = resumeService.setDefaultResume(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("设置已删除简历为默认应返回错误")
        void should_return_error_when_resume_already_deleted() {
            Resume existing = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.setDefaultResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("设置默认时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.setDefaultResume(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("设置默认时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.setDefaultResume(RESUME_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.setDefaultResume(RESUME_ID, USER_ID)).thenReturn(false);

            Result result = resumeService.setDefaultResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getResumeDetail 简历详情 ====================

    @Nested
    @DisplayName("getResumeDetail 简历详情")
    class GetResumeDetailTests {

        @FastTest
        @DisplayName("获取简历详情成功应返回成功")
        void should_get_resume_detail_successfully() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(skillDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(projectDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(awardDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取简历详情时应加载关联数据")
        void should_load_related_data_when_get_detail() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            ResumeEducation edu = createEducation(1, RESUME_ID, "黄山学院");
            ResumeSkill skill = createSkill(1, RESUME_ID, "Java", PROFICIENCY_INTERMEDIATE);
            ResumeProject project = createProject(1, RESUME_ID, "电商项目");
            ResumeAward award = createAward(1, RESUME_ID, "蓝桥杯一等奖");

            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenReturn(Arrays.asList(edu));
            when(skillDAO.findByResumeId(RESUME_ID)).thenReturn(Arrays.asList(skill));
            when(projectDAO.findByResumeId(RESUME_ID)).thenReturn(Arrays.asList(project));
            when(awardDAO.findByResumeId(RESUME_ID)).thenReturn(Arrays.asList(award));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(educationDAO).findByResumeId(RESUME_ID);
            verify(skillDAO).findByResumeId(RESUME_ID);
            verify(projectDAO).findByResumeId(RESUME_ID);
            verify(awardDAO).findByResumeId(RESUME_ID);
        }

        @FastTest
        @DisplayName("获取他人简历详情应返回错误")
        void should_return_error_when_get_others_resume_detail() {
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("获取不存在的简历详情应返回错误")
        void should_return_error_when_resume_not_exists() {
            when(resumeDAO.findById(999)).thenReturn(null);

            Result result = resumeService.getResumeDetail(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("获取已删除简历详情应返回错误")
        void should_return_error_when_resume_already_deleted() {
            Resume existing = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("获取详情时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.getResumeDetail(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("获取详情时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.getResumeDetail(RESUME_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== listResumes 简历列表 ====================

    @Nested
    @DisplayName("listResumes 简历列表")
    class ListResumesTests {

        @FastTest
        @DisplayName("获取用户简历列表应成功")
        void should_list_resumes_successfully() {
            Resume resume1 = createResume(1, USER_ID, "简历1", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            Resume resume2 = createResume(2, USER_ID, "简历2", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Arrays.asList(resume1, resume2));

            Result result = resumeService.listResumes(USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            Result result = resumeService.listResumes(USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取列表时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.listResumes(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(resumeDAO.findByUserId(USER_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.listResumes(USER_ID, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("分页参数应为1")
        void should_handle_page_parameter() {
            Resume resume = createResume(1, USER_ID, "简历1", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Arrays.asList(resume));

            Result result = resumeService.listResumes(USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getRecycleBin 回收站 ====================

    @Nested
    @DisplayName("getRecycleBin 回收站")
    class GetRecycleBinTests {

        @FastTest
        @DisplayName("获取回收站简历列表应成功")
        void should_get_recycle_bin_successfully() {
            Resume deleted = createResume(1, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findDeletedByUserId(USER_ID)).thenReturn(Arrays.asList(deleted));

            Result result = resumeService.getRecycleBin(USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(resumeDAO).findDeletedByUserId(USER_ID);
        }

        @FastTest
        @DisplayName("空回收站应返回成功")
        void should_return_success_with_empty_recycle_bin() {
            when(resumeDAO.findDeletedByUserId(USER_ID)).thenReturn(Collections.emptyList());

            Result result = resumeService.getRecycleBin(USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取回收站时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.getRecycleBin(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(resumeDAO.findDeletedByUserId(USER_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getRecycleBin(USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== restoreResume 恢复简历 ====================

    @Nested
    @DisplayName("restoreResume 恢复简历")
    class RestoreResumeTests {

        @FastTest
        @DisplayName("恢复简历成功应返回成功")
        void should_restore_resume_successfully() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.restore(RESUME_ID)).thenReturn(true);

            Result result = resumeService.restoreResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(resumeDAO).restore(RESUME_ID);
        }

        @FastTest
        @DisplayName("恢复他人简历应返回错误")
        void should_return_error_when_restore_others_resume() {
            Resume deleted = createResume(RESUME_ID, OTHER_USER_ID, "他人已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);

            Result result = resumeService.restoreResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("恢复未删除的简历应返回错误")
        void should_return_error_when_resume_not_deleted() {
            Resume existing = createResume(RESUME_ID, USER_ID, "正常简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(existing);

            Result result = resumeService.restoreResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("恢复不存在的简历应返回错误")
        void should_return_error_when_resume_not_exists() {
            when(resumeDAO.findById(999, true)).thenReturn(null);

            Result result = resumeService.restoreResume(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("恢复时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.restoreResume(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("恢复时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.restoreResume(RESUME_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库恢复失败应返回错误")
        void should_return_error_when_database_restore_fails() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.restore(RESUME_ID)).thenReturn(false);

            Result result = resumeService.restoreResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== permanentDelete 永久删除 ====================

    @Nested
    @DisplayName("permanentDelete 永久删除")
    class PermanentDeleteTests {

        @FastTest
        @DisplayName("永久删除简历成功应返回成功")
        void should_permanent_delete_successfully() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.hardDelete(RESUME_ID)).thenReturn(true);

            Result result = resumeService.permanentDelete(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(resumeDAO).hardDelete(RESUME_ID);
        }

        @FastTest
        @DisplayName("永久删除他人简历应返回错误")
        void should_return_error_when_permanent_delete_others_resume() {
            Resume deleted = createResume(RESUME_ID, OTHER_USER_ID, "他人已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);

            Result result = resumeService.permanentDelete(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("永久删除未删除的简历应返回错误")
        void should_return_error_when_resume_not_deleted() {
            Resume existing = createResume(RESUME_ID, USER_ID, "正常简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(existing);

            Result result = resumeService.permanentDelete(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("永久删除不存在的简历应返回错误")
        void should_return_error_when_resume_not_exists() {
            when(resumeDAO.findById(999, true)).thenReturn(null);

            Result result = resumeService.permanentDelete(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("永久删除时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.permanentDelete(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("永久删除时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.permanentDelete(RESUME_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库永久删除失败应返回错误")
        void should_return_error_when_database_hard_delete_fails() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.hardDelete(RESUME_ID)).thenReturn(false);

            Result result = resumeService.permanentDelete(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== addEducation 添加教育经历 ====================

    @Nested
    @DisplayName("addEducation 添加教育经历")
    class AddEducationTests {

        @FastTest
        @DisplayName("添加教育经历成功应返回成功")
        void should_add_education_successfully() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.save(any(ResumeEducation.class))).thenReturn(true);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeEducation> captor = ArgumentCaptor.forClass(ResumeEducation.class);
            verify(educationDAO).save(captor.capture());
            ResumeEducation saved = captor.getValue();
            assertThat(saved.getSchoolName()).isEqualTo("黄山学院");
            assertThat(saved.getResumeId()).isEqualTo(RESUME_ID);
        }

        @FastTest
        @DisplayName("添加教育经历时简历不属于当前用户应返回错误")
        void should_return_error_when_resume_not_owned_by_user() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加教育经历时学校名称为空应返回错误")
        void should_return_error_when_school_name_empty() {
            ResumeEducationDTO dto = createEducationDTO("");
            dto.setSchoolName("");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加教育经历时学校名称为null应返回错误")
        void should_return_error_when_school_name_null() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");
            dto.setSchoolName(null);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加教育经历时resumeId为null应返回错误")
        void should_return_error_when_resume_id_null() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");

            Result result = resumeService.addEducation(null, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加教育经历时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");

            Result result = resumeService.addEducation(RESUME_ID, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加教育经历时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = resumeService.addEducation(RESUME_ID, null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("简历不存在时应返回错误")
        void should_return_error_when_resume_not_exists() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");
            when(resumeDAO.findById(999)).thenReturn(null);

            Result result = resumeService.addEducation(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            ResumeEducationDTO dto = createEducationDTO("黄山学院");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.save(any(ResumeEducation.class))).thenReturn(false);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("学校名称仅包含空格应返回错误")
        void should_return_error_when_school_name_only_spaces() {
            ResumeEducationDTO dto = createEducationDTO("   ");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateEducation 更新教育经历 ====================

    @Nested
    @DisplayName("updateEducation 更新教育经历")
    class UpdateEducationTests {

        @FastTest
        @DisplayName("更新教育经历成功应返回成功")
        void should_update_education_successfully() {
            ResumeEducationDTO dto = createEducationDTO("更新后的学校");
            dto.setId(1);
            ResumeEducation existing = createEducation(1, RESUME_ID, "原学校");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(educationDAO.update(any(ResumeEducation.class))).thenReturn(true);

            Result result = resumeService.updateEducation(1, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeEducation> captor = ArgumentCaptor.forClass(ResumeEducation.class);
            verify(educationDAO).update(captor.capture());
            assertThat(captor.getValue().getSchoolName()).isEqualTo("更新后的学校");
        }

        @FastTest
        @DisplayName("更新他人教育经历应返回错误")
        void should_return_error_when_update_others_education() {
            ResumeEducationDTO dto = createEducationDTO("更新的学校");
            dto.setId(1);
            ResumeEducation existing = createEducation(1, RESUME_ID, "原学校");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.updateEducation(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的教育经历应返回错误")
        void should_return_error_when_education_not_exists() {
            ResumeEducationDTO dto = createEducationDTO("更新的学校");
            dto.setId(999);
            when(educationDAO.findById(999)).thenReturn(null);

            Result result = resumeService.updateEducation(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新教育经历时id为null应返回错误")
        void should_return_error_when_id_null() {
            ResumeEducationDTO dto = createEducationDTO("更新的学校");

            Result result = resumeService.updateEducation(null, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新教育经历时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            ResumeEducationDTO dto = createEducationDTO("更新的学校");
            dto.setId(1);

            Result result = resumeService.updateEducation(1, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            ResumeEducationDTO dto = createEducationDTO("更新的学校");
            dto.setId(1);
            ResumeEducation existing = createEducation(1, RESUME_ID, "原学校");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(educationDAO.update(any(ResumeEducation.class))).thenReturn(false);

            Result result = resumeService.updateEducation(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteEducation 删除教育经历 ====================

    @Nested
    @DisplayName("deleteEducation 删除教育经历")
    class DeleteEducationTests {

        @FastTest
        @DisplayName("删除教育经历成功应返回成功")
        void should_delete_education_successfully() {
            ResumeEducation existing = createEducation(1, RESUME_ID, "黄山学院");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(educationDAO.delete(1)).thenReturn(true);

            Result result = resumeService.deleteEducation(1, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(educationDAO).delete(1);
        }

        @FastTest
        @DisplayName("删除他人教育经历应返回错误")
        void should_return_error_when_delete_others_education() {
            ResumeEducation existing = createEducation(1, RESUME_ID, "黄山学院");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.deleteEducation(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的教育经历应返回错误")
        void should_return_error_when_education_not_exists() {
            when(educationDAO.findById(999)).thenReturn(null);

            Result result = resumeService.deleteEducation(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.deleteEducation(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = resumeService.deleteEducation(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库删除失败应返回错误")
        void should_return_error_when_database_delete_fails() {
            ResumeEducation existing = createEducation(1, RESUME_ID, "黄山学院");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(educationDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(educationDAO.delete(1)).thenReturn(false);

            Result result = resumeService.deleteEducation(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== addSkill 添加技能 ====================

    @Nested
    @DisplayName("addSkill 添加技能")
    class AddSkillTests {

        @FastTest
        @DisplayName("添加技能成功应返回成功")
        void should_add_skill_successfully() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_INTERMEDIATE);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeSkill> captor = ArgumentCaptor.forClass(ResumeSkill.class);
            verify(skillDAO).save(captor.capture());
            ResumeSkill saved = captor.getValue();
            assertThat(saved.getSkillName()).isEqualTo("Java");
            assertThat(saved.getProficiency()).isEqualTo(PROFICIENCY_INTERMEDIATE);
        }

        @FastTest
        @DisplayName("添加技能时简历不属于当前用户应返回错误")
        void should_return_error_when_resume_not_owned_by_user() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_INTERMEDIATE);
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加技能时技能名称为空应返回错误")
        void should_return_error_when_skill_name_empty() {
            ResumeSkillDTO dto = createSkillDTO("", PROFICIENCY_INTERMEDIATE);
            dto.setSkillName("");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("技能名称");
        }

        @FastTest
        @DisplayName("添加技能时熟练程度为空应返回错误")
        void should_return_error_when_proficiency_empty() {
            ResumeSkillDTO dto = createSkillDTO("Java", "");
            dto.setProficiency("");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("熟练程度");
        }

        @FastTest
        @DisplayName("添加技能时熟练程度为无效枚举应返回错误")
        void should_return_error_when_proficiency_invalid() {
            ResumeSkillDTO dto = createSkillDTO("Java", "invalid_level");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加技能时熟练度分数超出范围应返回错误")
        void should_return_error_when_proficiency_score_out_of_range() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_INTERMEDIATE);
            dto.setProficiencyScore(150);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加技能时熟练度分数为负数应返回错误")
        void should_return_error_when_proficiency_score_negative() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_INTERMEDIATE);
            dto.setProficiencyScore(-1);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加技能时应保存所有字段")
        void should_save_all_fields_when_add_skill() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_ADVANCED);
            dto.setProficiencyScore(90);
            dto.setCategory("编程语言");
            dto.setDescription("深入理解JVM原理");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeSkill> captor = ArgumentCaptor.forClass(ResumeSkill.class);
            verify(skillDAO).save(captor.capture());
            ResumeSkill saved = captor.getValue();
            assertThat(saved.getSkillName()).isEqualTo("Java");
            assertThat(saved.getProficiency()).isEqualTo(PROFICIENCY_ADVANCED);
            assertThat(saved.getProficiencyScore()).isEqualTo(90);
            assertThat(saved.getCategory()).isEqualTo("编程语言");
            assertThat(saved.getDescription()).isEqualTo("深入理解JVM原理");
        }

        @FastTest
        @DisplayName("所有熟练程度枚举值应被接受")
        void should_accept_all_valid_proficiency_values() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            String[] proficiencies = {PROFICIENCY_BEGINNER, PROFICIENCY_ELEMENTARY, PROFICIENCY_INTERMEDIATE, PROFICIENCY_ADVANCED, PROFICIENCY_EXPERT};

            for (String prof : proficiencies) {
                ResumeSkillDTO dto = createSkillDTO("TestSkill", prof);
                Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);
                assertThat(result.isSuccess()).isTrue();
            }
        }
    }

    // ==================== updateSkill 更新技能 ====================

    @Nested
    @DisplayName("updateSkill 更新技能")
    class UpdateSkillTests {

        @FastTest
        @DisplayName("更新技能成功应返回成功")
        void should_update_skill_successfully() {
            ResumeSkillDTO dto = createSkillDTO("更新后的技能", PROFICIENCY_ADVANCED);
            dto.setId(1);
            ResumeSkill existing = createSkill(1, RESUME_ID, "原技能", PROFICIENCY_INTERMEDIATE);
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(skillDAO.update(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.updateSkill(1, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeSkill> captor = ArgumentCaptor.forClass(ResumeSkill.class);
            verify(skillDAO).update(captor.capture());
            assertThat(captor.getValue().getSkillName()).isEqualTo("更新后的技能");
            assertThat(captor.getValue().getProficiency()).isEqualTo(PROFICIENCY_ADVANCED);
        }

        @FastTest
        @DisplayName("更新他人技能应返回错误")
        void should_return_error_when_update_others_skill() {
            ResumeSkillDTO dto = createSkillDTO("更新的技能", PROFICIENCY_INTERMEDIATE);
            dto.setId(1);
            ResumeSkill existing = createSkill(1, RESUME_ID, "原技能", PROFICIENCY_INTERMEDIATE);
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.updateSkill(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的技能应返回错误")
        void should_return_error_when_skill_not_exists() {
            ResumeSkillDTO dto = createSkillDTO("更新的技能", PROFICIENCY_INTERMEDIATE);
            dto.setId(999);
            when(skillDAO.findById(999)).thenReturn(null);

            Result result = resumeService.updateSkill(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新技能时id为null应返回错误")
        void should_return_error_when_id_null() {
            ResumeSkillDTO dto = createSkillDTO("更新的技能", PROFICIENCY_INTERMEDIATE);

            Result result = resumeService.updateSkill(null, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            ResumeSkillDTO dto = createSkillDTO("更新的技能", PROFICIENCY_INTERMEDIATE);
            dto.setId(1);
            ResumeSkill existing = createSkill(1, RESUME_ID, "原技能", PROFICIENCY_INTERMEDIATE);
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(skillDAO.update(any(ResumeSkill.class))).thenReturn(false);

            Result result = resumeService.updateSkill(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteSkill 删除技能 ====================

    @Nested
    @DisplayName("deleteSkill 删除技能")
    class DeleteSkillTests {

        @FastTest
        @DisplayName("删除技能成功应返回成功")
        void should_delete_skill_successfully() {
            ResumeSkill existing = createSkill(1, RESUME_ID, "Java", PROFICIENCY_INTERMEDIATE);
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(skillDAO.delete(1)).thenReturn(true);

            Result result = resumeService.deleteSkill(1, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(skillDAO).delete(1);
        }

        @FastTest
        @DisplayName("删除他人技能应返回错误")
        void should_return_error_when_delete_others_skill() {
            ResumeSkill existing = createSkill(1, RESUME_ID, "Java", PROFICIENCY_INTERMEDIATE);
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.deleteSkill(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的技能应返回错误")
        void should_return_error_when_skill_not_exists() {
            when(skillDAO.findById(999)).thenReturn(null);

            Result result = resumeService.deleteSkill(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = resumeService.deleteSkill(null, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库删除失败应返回错误")
        void should_return_error_when_database_delete_fails() {
            ResumeSkill existing = createSkill(1, RESUME_ID, "Java", PROFICIENCY_INTERMEDIATE);
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(skillDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(skillDAO.delete(1)).thenReturn(false);

            Result result = resumeService.deleteSkill(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== addProject 添加项目经历 ====================

    @Nested
    @DisplayName("addProject 添加项目经历")
    class AddProjectTests {

        @FastTest
        @DisplayName("添加项目经历成功应返回成功")
        void should_add_project_successfully() {
            ResumeProjectDTO dto = createProjectDTO("电商项目");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(projectDAO.save(any(ResumeProject.class))).thenReturn(true);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeProject> captor = ArgumentCaptor.forClass(ResumeProject.class);
            verify(projectDAO).save(captor.capture());
            ResumeProject saved = captor.getValue();
            assertThat(saved.getProjectName()).isEqualTo("电商项目");
            assertThat(saved.getResumeId()).isEqualTo(RESUME_ID);
        }

        @FastTest
        @DisplayName("添加项目经历时简历不属于当前用户应返回错误")
        void should_return_error_when_resume_not_owned_by_user() {
            ResumeProjectDTO dto = createProjectDTO("电商项目");
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加项目经历时项目名称为空应返回错误")
        void should_return_error_when_project_name_empty() {
            ResumeProjectDTO dto = createProjectDTO("");
            dto.setProjectName("");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加项目经历时项目名称为null应返回错误")
        void should_return_error_when_project_name_null() {
            ResumeProjectDTO dto = createProjectDTO("电商项目");
            dto.setProjectName(null);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加项目经历时应保存所有字段")
        void should_save_all_fields_when_add_project() {
            ResumeProjectDTO dto = createProjectDTO("完整项目");
            dto.setRole("核心开发");
            dto.setTeamSize(10);
            dto.setStartDate(Date.valueOf("2023-01-01"));
            dto.setEndDate(Date.valueOf("2023-12-31"));
            dto.setIsCurrent(0);
            dto.setDescription("项目描述");
            dto.setResponsibilities("负责核心模块开发");
            dto.setTechnologies("Java, Spring, MySQL");
            dto.setProjectUrl("https://project.example.com");
            dto.setAchievements("项目获奖");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(projectDAO.save(any(ResumeProject.class))).thenReturn(true);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeProject> captor = ArgumentCaptor.forClass(ResumeProject.class);
            verify(projectDAO).save(captor.capture());
            ResumeProject saved = captor.getValue();
            assertThat(saved.getProjectName()).isEqualTo("完整项目");
            assertThat(saved.getRole()).isEqualTo("核心开发");
            assertThat(saved.getTeamSize()).isEqualTo(10);
            assertThat(saved.getTechnologies()).isEqualTo("Java, Spring, MySQL");
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            ResumeProjectDTO dto = createProjectDTO("电商项目");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(projectDAO.save(any(ResumeProject.class))).thenReturn(false);

            Result result = resumeService.addProject(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== updateProject 更新项目经历 ====================

    @Nested
    @DisplayName("updateProject 更新项目经历")
    class UpdateProjectTests {

        @FastTest
        @DisplayName("更新项目经历成功应返回成功")
        void should_update_project_successfully() {
            ResumeProjectDTO dto = createProjectDTO("更新后的项目");
            dto.setId(1);
            ResumeProject existing = createProject(1, RESUME_ID, "原项目");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(projectDAO.update(any(ResumeProject.class))).thenReturn(true);

            Result result = resumeService.updateProject(1, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeProject> captor = ArgumentCaptor.forClass(ResumeProject.class);
            verify(projectDAO).update(captor.capture());
            assertThat(captor.getValue().getProjectName()).isEqualTo("更新后的项目");
        }

        @FastTest
        @DisplayName("更新他人项目经历应返回错误")
        void should_return_error_when_update_others_project() {
            ResumeProjectDTO dto = createProjectDTO("更新的项目");
            dto.setId(1);
            ResumeProject existing = createProject(1, RESUME_ID, "原项目");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.updateProject(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的项目经历应返回错误")
        void should_return_error_when_project_not_exists() {
            ResumeProjectDTO dto = createProjectDTO("更新的项目");
            dto.setId(999);
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = resumeService.updateProject(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            ResumeProjectDTO dto = createProjectDTO("更新的项目");
            dto.setId(1);
            ResumeProject existing = createProject(1, RESUME_ID, "原项目");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(projectDAO.update(any(ResumeProject.class))).thenReturn(false);

            Result result = resumeService.updateProject(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteProject 删除项目经历 ====================

    @Nested
    @DisplayName("deleteProject 删除项目经历")
    class DeleteProjectTests {

        @FastTest
        @DisplayName("删除项目经历成功应返回成功")
        void should_delete_project_successfully() {
            ResumeProject existing = createProject(1, RESUME_ID, "电商项目");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(projectDAO.delete(1)).thenReturn(true);

            Result result = resumeService.deleteProject(1, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(projectDAO).delete(1);
        }

        @FastTest
        @DisplayName("删除他人项目经历应返回错误")
        void should_return_error_when_delete_others_project() {
            ResumeProject existing = createProject(1, RESUME_ID, "电商项目");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.deleteProject(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的项目经历应返回错误")
        void should_return_error_when_project_not_exists() {
            when(projectDAO.findById(999)).thenReturn(null);

            Result result = resumeService.deleteProject(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库删除失败应返回错误")
        void should_return_error_when_database_delete_fails() {
            ResumeProject existing = createProject(1, RESUME_ID, "电商项目");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(projectDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(projectDAO.delete(1)).thenReturn(false);

            Result result = resumeService.deleteProject(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== addAward 添加获奖情况 ====================

    @Nested
    @DisplayName("addAward 添加获奖情况")
    class AddAwardTests {

        @FastTest
        @DisplayName("添加获奖情况成功应返回成功")
        void should_add_award_successfully() {
            ResumeAwardDTO dto = createAwardDTO("蓝桥杯一等奖");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(awardDAO.save(any(ResumeAward.class))).thenReturn(true);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeAward> captor = ArgumentCaptor.forClass(ResumeAward.class);
            verify(awardDAO).save(captor.capture());
            ResumeAward saved = captor.getValue();
            assertThat(saved.getAwardName()).isEqualTo("蓝桥杯一等奖");
            assertThat(saved.getResumeId()).isEqualTo(RESUME_ID);
        }

        @FastTest
        @DisplayName("添加获奖情况时简历不属于当前用户应返回错误")
        void should_return_error_when_resume_not_owned_by_user() {
            ResumeAwardDTO dto = createAwardDTO("蓝桥杯一等奖");
            Resume existing = createResume(RESUME_ID, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加获奖情况时奖项名称为空应返回错误")
        void should_return_error_when_award_name_empty() {
            ResumeAwardDTO dto = createAwardDTO("");
            dto.setAwardName("");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("奖项名称");
        }

        @FastTest
        @DisplayName("添加获奖情况时奖项名称为null应返回错误")
        void should_return_error_when_award_name_null() {
            ResumeAwardDTO dto = createAwardDTO("蓝桥杯一等奖");
            dto.setAwardName(null);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加获奖情况时应保存所有字段")
        void should_save_all_fields_when_add_award() {
            ResumeAwardDTO dto = createAwardDTO("完整获奖");
            dto.setCompetitionName("ACM竞赛");
            dto.setAwardLevel("金奖");
            dto.setAwardDate(Date.valueOf("2024-05-20"));
            dto.setAwardOrg("ACM国际大学生程序设计竞赛");
            dto.setDescription("全球总决赛金奖");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(awardDAO.save(any(ResumeAward.class))).thenReturn(true);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeAward> captor = ArgumentCaptor.forClass(ResumeAward.class);
            verify(awardDAO).save(captor.capture());
            ResumeAward saved = captor.getValue();
            assertThat(saved.getAwardName()).isEqualTo("完整获奖");
            assertThat(saved.getCompetitionName()).isEqualTo("ACM竞赛");
            assertThat(saved.getAwardLevel()).isEqualTo("金奖");
            assertThat(saved.getAwardOrg()).isEqualTo("ACM国际大学生程序设计竞赛");
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            ResumeAwardDTO dto = createAwardDTO("蓝桥杯一等奖");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(awardDAO.save(any(ResumeAward.class))).thenReturn(false);

            Result result = resumeService.addAward(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== updateAward 更新获奖情况 ====================

    @Nested
    @DisplayName("updateAward 更新获奖情况")
    class UpdateAwardTests {

        @FastTest
        @DisplayName("更新获奖情况成功应返回成功")
        void should_update_award_successfully() {
            ResumeAwardDTO dto = createAwardDTO("更新后的奖项");
            dto.setId(1);
            ResumeAward existing = createAward(1, RESUME_ID, "原奖项");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(awardDAO.update(any(ResumeAward.class))).thenReturn(true);

            Result result = resumeService.updateAward(1, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<ResumeAward> captor = ArgumentCaptor.forClass(ResumeAward.class);
            verify(awardDAO).update(captor.capture());
            assertThat(captor.getValue().getAwardName()).isEqualTo("更新后的奖项");
        }

        @FastTest
        @DisplayName("更新他人获奖情况应返回错误")
        void should_return_error_when_update_others_award() {
            ResumeAwardDTO dto = createAwardDTO("更新的奖项");
            dto.setId(1);
            ResumeAward existing = createAward(1, RESUME_ID, "原奖项");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.updateAward(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的获奖情况应返回错误")
        void should_return_error_when_award_not_exists() {
            ResumeAwardDTO dto = createAwardDTO("更新的奖项");
            dto.setId(999);
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = resumeService.updateAward(999, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            ResumeAwardDTO dto = createAwardDTO("更新的奖项");
            dto.setId(1);
            ResumeAward existing = createAward(1, RESUME_ID, "原奖项");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(awardDAO.update(any(ResumeAward.class))).thenReturn(false);

            Result result = resumeService.updateAward(1, dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteAward 删除获奖情况 ====================

    @Nested
    @DisplayName("deleteAward 删除获奖情况")
    class DeleteAwardTests {

        @FastTest
        @DisplayName("删除获奖情况成功应返回成功")
        void should_delete_award_successfully() {
            ResumeAward existing = createAward(1, RESUME_ID, "蓝桥杯一等奖");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(awardDAO.delete(1)).thenReturn(true);

            Result result = resumeService.deleteAward(1, USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(awardDAO).delete(1);
        }

        @FastTest
        @DisplayName("删除他人获奖情况应返回错误")
        void should_return_error_when_delete_others_award() {
            ResumeAward existing = createAward(1, RESUME_ID, "蓝桥杯一等奖");
            Resume otherUserResume = createResume(2, OTHER_USER_ID, "他人简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(otherUserResume);

            Result result = resumeService.deleteAward(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的获奖情况应返回错误")
        void should_return_error_when_award_not_exists() {
            when(awardDAO.findById(999)).thenReturn(null);

            Result result = resumeService.deleteAward(999, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库删除失败应返回错误")
        void should_return_error_when_database_delete_fails() {
            ResumeAward existing = createAward(1, RESUME_ID, "蓝桥杯一等奖");
            Resume resume = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(awardDAO.findById(1)).thenReturn(existing);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(resume);
            when(awardDAO.delete(1)).thenReturn(false);

            Result result = resumeService.deleteAward(1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("简历ID为0应返回错误")
        void should_return_error_when_id_zero() {
            Result result = resumeService.getResumeDetail(0, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("简历ID为负数应返回错误")
        void should_return_error_when_id_negative() {
            Result result = resumeService.getResumeDetail(-1, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("超长简历名称应正常处理")
        void should_handle_very_long_resume_name() {
            ResumeDTO dto = createResumeDTO("简历" + "啊".repeat(500));
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("超长学校名称应正常处理")
        void should_handle_very_long_school_name() {
            ResumeEducationDTO dto = createEducationDTO("学校" + "啊".repeat(500));
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.save(any(ResumeEducation.class))).thenReturn(true);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("超长技能名称应正常处理")
        void should_handle_very_long_skill_name() {
            ResumeSkillDTO dto = createSkillDTO("技能" + "啊".repeat(500), PROFICIENCY_INTERMEDIATE);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("特殊字符在学校名称中应正常处理")
        void should_handle_special_characters_in_school_name() {
            ResumeEducationDTO dto = createEducationDTO("黄山'学院\"&测试<Script>");
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.save(any(ResumeEducation.class))).thenReturn(true);

            Result result = resumeService.addEducation(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("熟练度分数边界值0应正常处理")
        void should_handle_proficiency_score_zero() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_BEGINNER);
            dto.setProficiencyScore(0);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("熟练度分数边界值100应正常处理")
        void should_handle_proficiency_score_hundred() {
            ResumeSkillDTO dto = createSkillDTO("Java", PROFICIENCY_EXPERT);
            dto.setProficiencyScore(100);
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            Result result = resumeService.addSkill(RESUME_ID, dto, USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("数据库查询简历异常应返回错误")
        void should_return_error_when_find_resume_by_id_fails() {
            when(resumeDAO.findById(RESUME_ID)).thenThrow(new RuntimeException("数据库连接失败"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库查询用户简历列表异常应返回错误")
        void should_return_error_when_find_user_resumes_fails() {
            when(resumeDAO.findByUserId(USER_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.listResumes(USER_ID, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库保存简历异常应返回错误")
        void should_return_error_when_save_resume_fails() {
            ResumeDTO dto = createResumeDTO("我的简历");
            when(resumeDAO.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.createResume(dto, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库查询教育经历异常应返回错误")
        void should_return_error_when_find_education_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库查询技能异常应返回错误")
        void should_return_error_when_find_skill_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(skillDAO.findByResumeId(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库查询项目经历异常应返回错误")
        void should_return_error_when_find_project_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(skillDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(projectDAO.findByResumeId(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库查询获奖情况异常应返回错误")
        void should_return_error_when_find_award_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_YES, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(educationDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(skillDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(projectDAO.findByResumeId(RESUME_ID)).thenReturn(Collections.emptyList());
            when(awardDAO.findByResumeId(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.getResumeDetail(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("setDefaultResume数据库异常应返回错误")
        void should_return_error_when_set_default_fails() {
            Resume existing = createResume(RESUME_ID, USER_ID, "我的简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_NO);
            when(resumeDAO.findById(RESUME_ID)).thenReturn(existing);
            when(resumeDAO.setDefaultResume(RESUME_ID, USER_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.setDefaultResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("restore数据库异常应返回错误")
        void should_return_error_when_restore_fails() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.restore(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.restoreResume(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("hardDelete数据库异常应返回错误")
        void should_return_error_when_hard_delete_fails() {
            Resume deleted = createResume(RESUME_ID, USER_ID, "已删除简历", DEFAULT_NO, STATUS_PUBLISHED, DELETED_YES);
            when(resumeDAO.findById(RESUME_ID, true)).thenReturn(deleted);
            when(resumeDAO.hardDelete(RESUME_ID)).thenThrow(new RuntimeException("数据库错误"));

            Result result = resumeService.permanentDelete(RESUME_ID, USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }
}
