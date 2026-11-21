package com.softwaregroup.hr.service;

import com.softwaregroup.hr.dao.MemberProfileDAO;
import com.softwaregroup.hr.dao.RecruitApplicationDAO;
import com.softwaregroup.hr.dao.UserDAO;
import com.softwaregroup.hr.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock RecruitApplicationDAO applications;
    @Mock UserDAO users;
    @Mock MemberProfileDAO profiles;
    @InjectMocks RecruitService service;

    @Test
    void memberCannotApproveOrDeleteRecruitmentApplication() {
        User member = new User();
        member.setRole("MEMBER");
        when(users.findById(1)).thenReturn(member);
        assertThat(service.approveApplication(7, 1).getCode()).isEqualTo(403);
        assertThat(service.rejectApplication(7, 1).getCode()).isEqualTo(403);
        assertThat(service.deleteApplication(7, 1).getCode()).isEqualTo(403);
        verifyNoInteractions(applications);
    }
}
