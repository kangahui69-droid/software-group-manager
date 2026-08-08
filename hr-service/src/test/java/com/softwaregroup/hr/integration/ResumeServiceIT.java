package com.softwaregroup.hr.integration;

import com.softwaregroup.hr.dao.*;
import com.softwaregroup.hr.model.entity.*;
import com.softwaregroup.hr.model.dto.*;
import com.softwaregroup.hr.service.ResumeService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResumeService 集成测试
 *
 * 测试简历服务的核心功能：简历CRUD、子项目CRUD
 */
@ExtendWith(MockitoExtension.class)
class ResumeServiceIT {

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

    @Test
    void createResume_withValidData_shouldReturnSuccess() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("我的简历");
        dto.setSummary("个人简介");

        List<Resume> existingResumes = null;
        when(resumeDAO.findByUserId(1)).thenReturn(existingResumes);
        when(resumeDAO.save(any(Resume.class))).thenReturn(true);

        Result result = resumeService.createResume(dto, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(resumeDAO).save(any(Resume.class));
    }

    @Test
    void createResume_withNullDto_shouldReturnError() {
        Result result = resumeService.createResume(null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createResume_withNullUserId_shouldReturnError() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("我的简历");

        Result result = resumeService.createResume(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createResume_withEmptyName_shouldReturnError() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("");

        Result result = resumeService.createResume(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void updateResume_withValidData_shouldReturnSuccess() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("更新后的简历");

        Resume existingResume = new Resume();
        existingResume.setId(1);
        existingResume.setUserId(1);
        existingResume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(existingResume);
        when(resumeDAO.update(any(Resume.class))).thenReturn(true);

        Result result = resumeService.updateResume(1, dto, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void updateResume_withInvalidId_shouldReturnError() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("更新后的简历");

        Result result = resumeService.updateResume(null, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void updateResume_withNonOwner_shouldReturnError() {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("更新后的简历");

        Resume existingResume = new Resume();
        existingResume.setId(1);
        existingResume.setUserId(2);

        when(resumeDAO.findById(1)).thenReturn(existingResume);

        Result result = resumeService.updateResume(1, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void deleteResume_withValidParams_shouldReturnSuccess() {
        Resume existingResume = new Resume();
        existingResume.setId(1);
        existingResume.setUserId(1);
        existingResume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(existingResume);
        when(resumeDAO.softDelete(1)).thenReturn(true);

        Result result = resumeService.deleteResume(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addEducation_withValidData_shouldReturnSuccess() {
        ResumeEducationDTO dto = new ResumeEducationDTO();
        dto.setSchoolName("黄山学院");
        dto.setMajor("计算机科学");

        Resume resume = new Resume();
        resume.setId(1);
        resume.setUserId(1);
        resume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(resume);
        when(educationDAO.save(any(ResumeEducation.class))).thenReturn(true);

        Result result = resumeService.addEducation(1, dto, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addEducation_withEmptySchoolName_shouldReturnError() {
        ResumeEducationDTO dto = new ResumeEducationDTO();
        dto.setSchoolName("");

        Result result = resumeService.addEducation(1, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("学校名称");
    }

    @Test
    void addSkill_withValidData_shouldReturnSuccess() {
        ResumeSkillDTO dto = new ResumeSkillDTO();
        dto.setSkillName("Java");
        dto.setProficiency("intermediate");

        Resume resume = new Resume();
        resume.setId(1);
        resume.setUserId(1);
        resume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(resume);
        when(skillDAO.save(any(ResumeSkill.class))).thenReturn(true);

        Result result = resumeService.addSkill(1, dto, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addSkill_withInvalidProficiency_shouldReturnError() {
        ResumeSkillDTO dto = new ResumeSkillDTO();
        dto.setSkillName("Java");
        dto.setProficiency("invalid");

        Result result = resumeService.addSkill(1, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("熟练程度");
    }

    @Test
    void addProject_withValidData_shouldReturnSuccess() {
        ResumeProjectDTO dto = new ResumeProjectDTO();
        dto.setProjectName("管理系统");

        Resume resume = new Resume();
        resume.setId(1);
        resume.setUserId(1);
        resume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(resume);
        when(projectDAO.save(any(ResumeProject.class))).thenReturn(true);

        Result result = resumeService.addProject(1, dto, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addProject_withEmptyProjectName_shouldReturnError() {
        ResumeProjectDTO dto = new ResumeProjectDTO();
        dto.setProjectName("");

        Result result = resumeService.addProject(1, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("项目名称");
    }

    @Test
    void addAward_withValidData_shouldReturnSuccess() {
        ResumeAwardDTO dto = new ResumeAwardDTO();
        dto.setAwardName("一等奖");

        Resume resume = new Resume();
        resume.setId(1);
        resume.setUserId(1);
        resume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(resume);
        when(awardDAO.save(any(ResumeAward.class))).thenReturn(true);

        Result result = resumeService.addAward(1, dto, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addAward_withEmptyAwardName_shouldReturnError() {
        ResumeAwardDTO dto = new ResumeAwardDTO();
        dto.setAwardName("");

        Result result = resumeService.addAward(1, dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("奖项名称");
    }

    @Test
    void getResumeDetail_withValidId_shouldReturnResume() {
        Resume resume = new Resume();
        resume.setId(1);
        resume.setUserId(1);
        resume.setDeleted(0);

        when(resumeDAO.findById(1)).thenReturn(resume);
        when(educationDAO.findByResumeId(1)).thenReturn(Arrays.asList());
        when(skillDAO.findByResumeId(1)).thenReturn(Arrays.asList());
        when(projectDAO.findByResumeId(1)).thenReturn(Arrays.asList());
        when(awardDAO.findByResumeId(1)).thenReturn(Arrays.asList());

        Result result = resumeService.getResumeDetail(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getResumeDetail_withNullId_shouldReturnError() {
        Result result = resumeService.getResumeDetail(null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listResumes_withValidUserId_shouldReturnResumeList() {
        Resume resume = new Resume();
        resume.setId(1);
        when(resumeDAO.findByUserId(1)).thenReturn(Arrays.asList(resume));

        Result result = resumeService.listResumes(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void listResumes_withNullUserId_shouldReturnError() {
        Result result = resumeService.listResumes(null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }
}
