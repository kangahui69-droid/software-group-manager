package com.softwaregroup.activity.service;

import com.softwaregroup.activity.dao.ActivityDAO;
import com.softwaregroup.activity.dao.ActivityParticipantDAO;
import com.softwaregroup.activity.dao.StudySessionDAO;
import com.softwaregroup.activity.feign.UserFeignClient;
import com.softwaregroup.activity.model.entity.Activity;
import com.softwaregroup.activity.model.entity.StudySession;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock ActivityDAO activities;
    @Mock ActivityParticipantDAO registrations;
    @Mock StudySessionDAO sessions;
    @Mock UserFeignClient users;
    @InjectMocks ActivityService activityService;
    @InjectMocks StudyService studyService;

    @Test
    void pendingActivityIsHiddenFromOtherUsers() {
        Activity activity = new Activity();
        activity.setCreatorId(2);
        activity.setApprovalStatus("pending");
        when(activities.findById(7)).thenReturn(activity);
        assertThat(activityService.getActivityDetail(7, 1, false).getCode()).isEqualTo(404);
    }

    @Test
    void anotherUserCannotReadStudySession() {
        StudySession session = new StudySession();
        session.setUserId(2);
        when(sessions.findById(7)).thenReturn(session);
        assertThat(studyService.getSessionDetail(7, 1).getCode()).isEqualTo(403);
    }

    @Test
    void publicActivityListForcesApprovedStatus() {
        activityService.listActivities(null, null);
        verify(activities).findByConditions(null, null, null, "approved");
        activityService.listActivities(null, new CurrentUser(1, "admin", "ADMIN"));
        verify(activities).findByConditions(null, null, null, null);
    }

    @Test
    void organizerCanApproveOwnParticipantButOtherMemberCannot() {
        Activity activity = new Activity();
        activity.setCreatorId(1);
        activity.setMaxParticipants(0);
        when(activities.findById(7)).thenReturn(activity);
        when(users.getUserRole(1)).thenReturn(Result.ok("MEMBER"));
        when(users.getUserRole(3)).thenReturn(Result.ok("MEMBER"));
        when(registrations.getRegistrationStatus(7, 2)).thenReturn("pending");
        when(registrations.updateStatus(7, 2, "confirmed", "审批通过")).thenReturn(true);

        assertThat(activityService.approveParticipant(7, 2, 1).isSuccess()).isTrue();
        assertThat(activityService.approveParticipant(7, 2, 3).getCode()).isEqualTo(403);
        verify(registrations, times(1)).updateStatus(7, 2, "confirmed", "审批通过");
    }
}
