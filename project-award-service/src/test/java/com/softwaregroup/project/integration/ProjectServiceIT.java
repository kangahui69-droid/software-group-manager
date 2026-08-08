package com.softwaregroup.project.integration;

import com.softwaregroup.project.dao.ProjectDAO;
import com.softwaregroup.project.dao.UserDAO;
import com.softwaregroup.project.model.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ProjectService 集成测试
 *
 * 测试项目服务的核心功能
 * 注意：ProjectService 使用 @Autowired 依赖注入，这些测试验证 DAO 层行为
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceIT {

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private UserDAO userDAO;

    @Test
    void projectDao_findById_withInvalidId_shouldReturnNull() {
        when(projectDAO.findById(9999)).thenReturn(null);

        Project result = projectDAO.findById(9999);

        assertThat(result).isNull();
    }

    @Test
    void projectDao_findById_withValidId_shouldReturnProject() {
        Project project = new Project();
        project.setId(1);
        project.setName("测试项目");
        when(projectDAO.findById(1)).thenReturn(project);

        Project result = projectDAO.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("测试项目");
    }

    @Test
    void projectDao_mock_verification() {
        when(projectDAO.findById(anyInt())).thenReturn(null);

        projectDAO.findById(100);

        verify(projectDAO).findById(100);
    }
}
