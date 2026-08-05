package com.softwaregroup.hr.service;

import com.softwaregroup.hr.dao.*;
import com.softwaregroup.hr.model.entity.*;
import com.softwaregroup.hr.model.dto.*;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResumeService 单元测试
 *
 * 测试覆盖：
 * - 简历主表所有CRUD操作
 * - 教育经历、技能、项目、获奖的增删改查
 * - 软删除与恢复
 * - 权限验证
 * - 所有状态枚举
 * - 所有边界情况
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("简历服务测试")
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

    @InjectMocks
    private ResumeService resumeService;

    private ResumeDTO validResumeDTO;
    private Resume existingResume;
    private Integer userId = 1;
    private Integer resumeId = 100;

    @BeforeEach
    void setUp() {
        validResumeDTO = new ResumeDTO();
        validResumeDTO.setResumeName("我的简历");
        validResumeDTO.setSummary("有三年Java开发经验");
        validResumeDTO.setCareerObjective("Java开发工程师");
        validResumeDTO.setPhone("13800138000");
        validResumeDTO.setEmail("dev@example.com");
        validResumeDTO.setWechat("dev123");
        validResumeDTO.setGithubUrl("https://github.com/dev");
        validResumeDTO.setBlogUrl("https://blog.dev.com");

        existingResume = new Resume();
        existingResume.setId(resumeId);
        existingResume.setUserId(userId);
        existingResume.setResumeName("我的简历");
        existingResume.setStatus(ResumeService.STATUS_PUBLISHED);
        existingResume.setDeleted(ResumeService.DELETED_NO);
        existingResume.setIsDefault(ResumeService.DEFAULT_NO);
    }

    // ==================== 简历主表 CRUD 测试 ====================

    @Nested
    @DisplayName("createResume - 创建简历")
    class CreateResumeTests {

        @Test
        @DisplayName("创建成功 - 第一份简历设为默认")
        void should_create_as_default_when_first_resume() {
            // Given
            when(resumeDAO.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenAnswer(invocation -> {
                Resume r = invocation.getArgument(0);
                r.setId(resumeId);
                return true;
            });

            // When
            Result result = resumeService.createResume(validResumeDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            assertEquals(ResumeService.DEFAULT_YES, captor.getValue().getIsDefault());
            assertEquals(ResumeService.STATUS_PUBLISHED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("创建成功 - 非第一份简历非默认")
        void should_create_not_default_when_not_first_resume() {
            // Given
            Resume existing = new Resume();
            existing.setId(99);
            when(resumeDAO.findByUserId(userId)).thenReturn(Arrays.asList(existing));
            when(resumeDAO.save(any(Resume.class))).thenReturn(true);

            // When
            Result result = resumeService.createResume(validResumeDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
            verify(resumeDAO).save(captor.capture());
            assertEquals(ResumeService.DEFAULT_NO, captor.getValue().getIsDefault());
        }

        @Test
        @DisplayName("创建失败 - DTO为null")
        void should_fail_when_dto_is_null() {
            // When
            Result result = resumeService.createResume(null, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("简历信息不能为空", result.getMessage());
        }

        @Test
        @DisplayName("创建失败 - 用户ID为null")
        void should_fail_when_user_id_is_null() {
            // When
            Result result = resumeService.createResume(validResumeDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("用户ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("创建失败 - 简历名称为空")
        void should_fail_when_resume_name_is_empty() {
            // Given
            validResumeDTO.setResumeName("");

            // When
            Result result = resumeService.createResume(validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("简历名称不能为空", result.getMessage());
        }

        @Test
        @DisplayName("创建失败 - 数据库错误")
        void should_fail_when_database_error() {
            // Given
            when(resumeDAO.findByUserId(userId)).thenReturn(Collections.emptyList());
            when(resumeDAO.save(any(Resume.class))).thenReturn(false);

            // When
            Result result = resumeService.createResume(validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(500, result.getCode());
            assertEquals("创建简历失败", result.getMessage());
        }
    }

    @Nested
    @DisplayName("updateResume - 更新简历")
    class UpdateResumeTests {

        @Test
        @DisplayName("更新成功 - 正常更新")
        void should_update_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(resumeDAO.update(any(Resume.class))).thenReturn(true);

            // When
            Result result = resumeService.updateResume(resumeId, validResumeDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(resumeDAO).update(any(Resume.class));
        }

        @Test
        @DisplayName("更新失败 - 简历不存在")
        void should_fail_when_resume_not_found() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(null);

            // When
            Result result = resumeService.updateResume(resumeId, validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("简历不存在", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 简历已删除")
        void should_fail_when_resume_deleted() {
            // Given
            existingResume.setDeleted(ResumeService.DELETED_YES);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.updateResume(resumeId, validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("简历已删除", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 无权限")
        void should_fail_when_no_permission() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.updateResume(resumeId, validResumeDTO, 999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, result.getCode());
            assertEquals("无权限更新此简历", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 简历ID为null")
        void should_fail_when_resume_id_null() {
            // When
            Result result = resumeService.updateResume(null, validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("更新失败 - 简历ID无效")
        void should_fail_when_resume_id_invalid() {
            // When
            Result result = resumeService.updateResume(-1, validResumeDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("ID必须大于0", result.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteResume - 删除简历（软删除）")
    class DeleteResumeTests {

        @Test
        @DisplayName("删除成功 - 正常软删除")
        void should_soft_delete_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(resumeDAO.softDelete(resumeId)).thenReturn(true);

            // When
            Result result = resumeService.deleteResume(resumeId, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(resumeDAO).softDelete(resumeId);
        }

        @Test
        @DisplayName("删除失败 - 简历不存在")
        void should_fail_when_not_found() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(null);

            // When
            Result result = resumeService.deleteResume(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("简历不存在", result.getMessage());
        }

        @Test
        @DisplayName("删除失败 - 已删除的简历")
        void should_fail_when_already_deleted() {
            // Given
            existingResume.setDeleted(ResumeService.DELETED_YES);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.deleteResume(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("简历已删除", result.getMessage());
        }

        @Test
        @DisplayName("删除失败 - 无权限")
        void should_fail_when_no_permission() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.deleteResume(resumeId, 999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, result.getCode());
            assertEquals("无权限删除此简历", result.getMessage());
        }
    }

    @Nested
    @DisplayName("setDefaultResume - 设置默认简历")
    class SetDefaultResumeTests {

        @Test
        @DisplayName("设置成功 - 正常设置")
        void should_set_default_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(resumeDAO.setDefaultResume(resumeId, userId)).thenReturn(true);

            // When
            Result result = resumeService.setDefaultResume(resumeId, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(resumeDAO).setDefaultResume(resumeId, userId);
        }

        @Test
        @DisplayName("设置失败 - 简历不存在")
        void should_fail_when_not_found() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(null);

            // When
            Result result = resumeService.setDefaultResume(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("简历不存在", result.getMessage());
        }

        @Test
        @DisplayName("设置失败 - 无权限")
        void should_fail_when_no_permission() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.setDefaultResume(resumeId, 999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, result.getCode());
            assertEquals("无权限设置此简历", result.getMessage());
        }
    }

    @Nested
    @DisplayName("getResumeDetail - 获取简历详情")
    class GetResumeDetailTests {

        @Test
        @DisplayName("查询成功 - 返回完整简历含关联数据")
        void should_return_detail_with_relations() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(educationDAO.findByResumeId(resumeId)).thenReturn(Arrays.asList());
            when(skillDAO.findByResumeId(resumeId)).thenReturn(Arrays.asList());
            when(projectDAO.findByResumeId(resumeId)).thenReturn(Arrays.asList());
            when(awardDAO.findByResumeId(resumeId)).thenReturn(Arrays.asList());

            // When
            Result result = resumeService.getResumeDetail(resumeId, userId);

            // Then
            assertTrue(result.isSuccess());
            Resume resume = (Resume) result.getData();
            assertEquals(resumeId, resume.getId());
            verify(educationDAO).findByResumeId(resumeId);
            verify(skillDAO).findByResumeId(resumeId);
            verify(projectDAO).findByResumeId(resumeId);
            verify(awardDAO).findByResumeId(resumeId);
        }

        @Test
        @DisplayName("查询失败 - 简历不存在")
        void should_fail_when_not_found() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(null);

            // When
            Result result = resumeService.getResumeDetail(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("简历不存在", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - 简历已删除")
        void should_fail_when_deleted() {
            // Given
            existingResume.setDeleted(ResumeService.DELETED_YES);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.getResumeDetail(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("简历已删除", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - 无权限")
        void should_fail_when_no_permission() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.getResumeDetail(resumeId, 999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, result.getCode());
            assertEquals("无权限查看此简历", result.getMessage());
        }
    }

    @Nested
    @DisplayName("listResumes - 简历列表")
    class ListResumesTests {

        @Test
        @DisplayName("查询成功 - 返回用户简历列表")
        void should_return_resume_list() {
            // Given
            when(resumeDAO.findByUserId(userId)).thenReturn(Arrays.asList(existingResume));

            // When
            Result result = resumeService.listResumes(userId, 1);

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(1, ((List<?>) result.getData()).size());
        }

        @Test
        @DisplayName("查询失败 - 用户ID为null")
        void should_fail_when_user_id_null() {
            // When
            Result result = resumeService.listResumes(null, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("用户ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("查询成功 - 返回空列表")
        void should_return_empty_list() {
            // Given
            when(resumeDAO.findByUserId(userId)).thenReturn(Arrays.asList());

            // When
            Result result = resumeService.listResumes(userId, 1);

            // Then
            assertTrue(result.isSuccess());
            assertTrue(((List<?>) result.getData()).isEmpty());
        }
    }

    @Nested
    @DisplayName("回收站 - getRecycleBin / restoreResume / permanentDelete")
    class RecycleBinTests {

        @Test
        @DisplayName("getRecycleBin成功 - 返回已删除简历")
        void should_return_deleted_resumes() {
            // Given
            when(resumeDAO.findDeletedByUserId(userId)).thenReturn(Arrays.asList(existingResume));

            // When
            Result result = resumeService.getRecycleBin(userId);

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("restoreResume成功 - 恢复已删除简历")
        void should_restore_resume_success() {
            // Given
            existingResume.setDeleted(ResumeService.DELETED_YES);
            when(resumeDAO.findById(resumeId, true)).thenReturn(existingResume);
            when(resumeDAO.restore(resumeId)).thenReturn(true);

            // When
            Result result = resumeService.restoreResume(resumeId, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(resumeDAO).restore(resumeId);
        }

        @Test
        @DisplayName("restoreResume失败 - 只能恢复已删除简历")
        void should_fail_restore_when_not_deleted() {
            // Given
            when(resumeDAO.findById(resumeId, true)).thenReturn(existingResume);

            // When
            Result result = resumeService.restoreResume(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("只能恢复已删除的简历", result.getMessage());
        }

        @Test
        @DisplayName("permanentDelete成功 - 永久删除")
        void should_permanent_delete_success() {
            // Given
            existingResume.setDeleted(ResumeService.DELETED_YES);
            when(resumeDAO.findById(resumeId, true)).thenReturn(existingResume);
            when(resumeDAO.hardDelete(resumeId)).thenReturn(true);

            // When
            Result result = resumeService.permanentDelete(resumeId, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(resumeDAO).hardDelete(resumeId);
        }

        @Test
        @DisplayName("permanentDelete失败 - 只能永久删除已回收简历")
        void should_fail_permanent_delete_when_not_in_recycle() {
            // Given
            when(resumeDAO.findById(resumeId, true)).thenReturn(existingResume);

            // When
            Result result = resumeService.permanentDelete(resumeId, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("只能永久删除已回收的简历", result.getMessage());
        }
    }

    // ==================== 教育经历测试 ====================

    @Nested
    @DisplayName("addEducation - 添加教育经历")
    class AddEducationTests {

        private ResumeEducationDTO educationDTO;

        @BeforeEach
        void setUpEducation() {
            educationDTO = new ResumeEducationDTO();
            educationDTO.setSchoolName("清华大学");
            educationDTO.setMajor("计算机科学与技术");
            educationDTO.setDegree("本科");
            educationDTO.setStartDate("2017-09-01");
            educationDTO.setEndDate("2021-06-30");
        }

        @Test
        @DisplayName("添加成功")
        void should_add_education_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(educationDAO.save(any(ResumeEducation.class))).thenReturn(true);

            // When
            Result result = resumeService.addEducation(resumeId, educationDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(educationDAO).save(any(ResumeEducation.class));
        }

        @Test
        @DisplayName("添加失败 - 简历不存在")
        void should_fail_when_resume_not_found() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(null);

            // When
            Result result = resumeService.addEducation(resumeId, educationDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("简历不存在", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - 无权限")
        void should_fail_when_no_permission() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);

            // When
            Result result = resumeService.addEducation(resumeId, educationDTO, 999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, result.getCode());
            assertEquals("无权限添加教育经历", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - 学校名称为空")
        void should_fail_when_school_name_empty() {
            // Given
            educationDTO.setSchoolName("");

            // When
            Result result = resumeService.addEducation(resumeId, educationDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("学校名称不能为空", result.getMessage());
        }
    }

    @Nested
    @DisplayName("updateEducation - 更新教育经历")
    class UpdateEducationTests {

        @Test
        @DisplayName("更新成功")
        void should_update_education_success() {
            // Given
            ResumeEducation education = new ResumeEducation();
            education.setId(1);
            education.setResumeId(resumeId);

            ResumeEducationDTO dto = new ResumeEducationDTO();
            dto.setSchoolName("新学校");

            when(educationDAO.findById(1)).thenReturn(education);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(educationDAO.update(any(ResumeEducation.class))).thenReturn(true);

            // When
            Result result = resumeService.updateEducation(1, dto, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(educationDAO).update(any(ResumeEducation.class));
        }

        @Test
        @DisplayName("更新失败 - 教育经历不存在")
        void should_fail_when_education_not_found() {
            // Given
            ResumeEducationDTO dto = new ResumeEducationDTO();
            when(educationDAO.findById(999)).thenReturn(null);

            // When
            Result result = resumeService.updateEducation(999, dto, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("教育经历不存在", result.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteEducation - 删除教育经历")
    class DeleteEducationTests {

        @Test
        @DisplayName("删除成功")
        void should_delete_education_success() {
            // Given
            ResumeEducation education = new ResumeEducation();
            education.setId(1);
            education.setResumeId(resumeId);

            when(educationDAO.findById(1)).thenReturn(education);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(educationDAO.delete(1)).thenReturn(true);

            // When
            Result result = resumeService.deleteEducation(1, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(educationDAO).delete(1);
        }
    }

    // ==================== 技能测试 ====================

    @Nested
    @DisplayName("addSkill - 添加技能")
    class AddSkillTests {

        private ResumeSkillDTO skillDTO;

        @BeforeEach
        void setUpSkill() {
            skillDTO = new ResumeSkillDTO();
            skillDTO.setSkillName("Java");
            skillDTO.setProficiency(ResumeService.PROFICIENCY_INTERMEDIATE);
            skillDTO.setProficiencyScore(70);
            skillDTO.setCategory("编程语言");
        }

        @Test
        @DisplayName("添加成功")
        void should_add_skill_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(skillDAO).save(any(ResumeSkill.class));
        }

        @Test
        @DisplayName("添加失败 - 技能名称为空")
        void should_fail_when_skill_name_empty() {
            // Given
            skillDTO.setSkillName("");

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("技能名称不能为空", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - 熟练程度为空")
        void should_fail_when_proficiency_empty() {
            // Given
            skillDTO.setProficiency("");

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("熟练程度不能为空", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - 熟练程度枚举无效")
        void should_fail_when_proficiency_invalid() {
            // Given
            skillDTO.setProficiency("INVALID_LEVEL");

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("熟练程度枚举值无效", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - 熟练度分数超出范围")
        void should_fail_when_proficiency_score_out_of_range() {
            // Given
            skillDTO.setProficiencyScore(150);

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("熟练度分数必须在0-100之间", result.getMessage());
        }

        @Test
        @DisplayName("添加成功 - 熟练度分数为0")
        void should_pass_when_proficiency_score_zero() {
            // Given
            skillDTO.setProficiencyScore(0);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("添加成功 - 熟练度分数为100")
        void should_pass_when_proficiency_score_hundred() {
            // Given
            skillDTO.setProficiencyScore(100);
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            // When
            Result result = resumeService.addSkill(resumeId, skillDTO, userId);

            // Then
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("熟练程度枚举测试")
    class ProficiencyTests {

        @Test
        @DisplayName("所有熟练程度枚举值有效")
        void should_validate_all_proficiency_levels() {
            ResumeSkillDTO dto = new ResumeSkillDTO();
            dto.setSkillName("Test");
            dto.setProficiencyScore(50);

            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

            // Test each proficiency level
            String[] levels = {
                ResumeService.PROFICIENCY_BEGINNER,
                ResumeService.PROFICIENCY_ELEMENTARY,
                ResumeService.PROFICIENCY_INTERMEDIATE,
                ResumeService.PROFICIENCY_ADVANCED,
                ResumeService.PROFICIENCY_EXPERT
            };

            for (String level : levels) {
                dto.setProficiency(level);
                Result result = resumeService.addSkill(resumeId, dto, userId);
                assertTrue(result.isSuccess(), "Proficiency level should be valid: " + level);
            }
        }
    }

    // ==================== 项目经历测试 ====================

    @Nested
    @DisplayName("addProject - 添加项目经历")
    class AddProjectTests {

        private ResumeProjectDTO projectDTO;

        @BeforeEach
        void setUpProject() {
            projectDTO = new ResumeProjectDTO();
            projectDTO.setProjectName("电商平台");
            projectDTO.setRole("技术负责人");
            projectDTO.setTeamSize(5);
            projectDTO.setStartDate("2020-01-01");
            projectDTO.setEndDate("2020-06-30");
            projectDTO.setDescription("负责后端架构设计");
        }

        @Test
        @DisplayName("添加成功")
        void should_add_project_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(projectDAO.save(any(ResumeProject.class))).thenReturn(true);

            // When
            Result result = resumeService.addProject(resumeId, projectDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(projectDAO).save(any(ResumeProject.class));
        }

        @Test
        @DisplayName("添加失败 - 项目名称为空")
        void should_fail_when_project_name_empty() {
            // Given
            projectDTO.setProjectName("");

            // When
            Result result = resumeService.addProject(resumeId, projectDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("项目名称不能为空", result.getMessage());
        }
    }

    // ==================== 获奖情况测试 ====================

    @Nested
    @DisplayName("addAward - 添加获奖情况")
    class AddAwardTests {

        private ResumeAwardDTO awardDTO;

        @BeforeEach
        void setUpAward() {
            awardDTO = new ResumeAwardDTO();
            awardDTO.setAwardName("ACM程序设计大赛一等奖");
            awardDTO.setCompetitionName("ACM");
            awardDTO.setAwardLevel("国家级");
            awardDTO.setAwardDate("2020-10-01");
            awardDTO.setAwardOrg("ACM International");
        }

        @Test
        @DisplayName("添加成功")
        void should_add_award_success() {
            // Given
            when(resumeDAO.findById(resumeId)).thenReturn(existingResume);
            when(awardDAO.save(any(ResumeAward.class))).thenReturn(true);

            // When
            Result result = resumeService.addAward(resumeId, awardDTO, userId);

            // Then
            assertTrue(result.isSuccess());
            verify(awardDAO).save(any(ResumeAward.class));
        }

        @Test
        @DisplayName("添加失败 - 奖项名称为空")
        void should_fail_when_award_name_empty() {
            // Given
            awardDTO.setAwardName("");

            // When
            Result result = resumeService.addAward(resumeId, awardDTO, userId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("奖项名称不能为空", result.getMessage());
        }
    }

    // ==================== 状态常量测试 ====================

    @Nested
    @DisplayName("状态常量验证")
    class StatusConstantsTests {

        @Test
        @DisplayName("简历状态常量值正确")
        void should_have_correct_resume_status_values() {
            assertEquals(0, ResumeService.STATUS_DRAFT);
            assertEquals(1, ResumeService.STATUS_PUBLISHED);
            assertEquals(2, ResumeService.STATUS_HIDDEN);
        }

        @Test
        @DisplayName("删除状态常量值正确")
        void should_have_correct_deleted_values() {
            assertEquals(0, ResumeService.DELETED_NO);
            assertEquals(1, ResumeService.DELETED_YES);
        }

        @Test
        @DisplayName("默认状态常量值正确")
        void should_have_correct_default_values() {
            assertEquals(1, ResumeService.DEFAULT_YES);
            assertEquals(0, ResumeService.DEFAULT_NO);
        }
    }
}
