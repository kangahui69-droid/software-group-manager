package dao;

import model.StudySession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class StudySessionDAOTimeTest {
    private static final ZoneId STUDY_ZONE = ZoneId.of("Asia/Shanghai");
    private final StudySessionDAO dao = new StudySessionDAO();

    @BeforeAll static void init() { H2Database.initSchema(); }
    @BeforeEach void reset() { H2Database.reset(); }

    @Test void newSessionUsesDatabaseClockAndNeverStartsEightHoursAway() throws Exception {
        StudySession request = new StudySession();
        request.setUserId(2);
        request.setSessionDate(new Date(System.currentTimeMillis() + 24 * 60 * 60_000L));
        request.setCheckInTime(new Date(System.currentTimeMillis() + 8 * 60 * 60_000L));

        int id = dao.startStudy(request);
        StudySession active = dao.getTodayActiveSession(2);
        assertThat(active).isNotNull();
        assertThat(active.getId()).isEqualTo(id);
        assertThat(Math.abs(active.getCheckInTime().getTime() - System.currentTimeMillis())).isLessThan(120_000L);
        assertThat(dao.getActiveElapsedSeconds(id)).isBetween(0, 120);

        assertThat(dao.endStudy(id)).isEqualTo(1);
        StudySession completed = dao.findById(id);
        assertThat(completed.getDuration()).isBetween(0, 2);
        assertThat(dao.getTodayDuration(2)).isBetween(0, 2);
    }

    @Test void legacyNegativeRecordUsesDatabaseTimestampsInMemberAndAdminViews() throws Exception {
        LocalDateTime now = LocalDateTime.now(STUDY_ZONE).withNano(0);
        insertLegacy(now.minusMinutes(5), now.plusHours(8), now, -475, "COMPLETED");

        StudySession member = dao.getSessionList(2, null, null, 0, 20).get(0);
        StudySession admin = dao.getAllSessions(null, null, null, 0, 20).get(0);
        assertThat(member.getDuration()).isEqualTo(5);
        assertThat(admin.getDuration()).isEqualTo(5);
        assertThat(member.getCheckInTime()).isEqualTo(member.getCreatedAt());
        assertThat(dao.getTodayDuration(2)).isEqualTo(5);
        assertThat(dao.getWeekStatistics(2).get("weekDuration")).isEqualTo(5);
        assertThat(dao.getStatistics(2, null, null).get("totalDuration")).isEqualTo(5);
    }

    @Test void endingLegacyActiveRecordDoesNotCreateNegativeDuration() throws Exception {
        LocalDateTime now = LocalDateTime.now(STUDY_ZONE).withNano(0);
        int id = insertLegacy(now.minusMinutes(2), now.plusHours(8), null, null, "ACTIVE");
        assertThat(dao.getActiveElapsedSeconds(id)).isBetween(120, 125);

        assertThat(dao.endStudy(id)).isEqualTo(1);
        StudySession completed = dao.findById(id);
        assertThat(completed.getDuration()).isBetween(2, 3);
        assertThat(completed.getCheckInTime()).isEqualTo(completed.getCreatedAt());
    }

    private int insertLegacy(LocalDateTime created, LocalDateTime checkIn, LocalDateTime checkOut,
                             Integer duration, String status) throws Exception {
        String sql = "INSERT INTO study_session (user_id,session_date,check_in_time,check_out_time,duration,status,created_at) "
                + "VALUES (2,CURDATE(),?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, checkIn);
            ps.setObject(2, checkOut);
            ps.setObject(3, duration);
            ps.setString(4, status);
            ps.setObject(5, created);
            ps.executeUpdate();
            try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                assertThat(keys.next()).isTrue();
                return keys.getInt(1);
            }
        }
    }
}
