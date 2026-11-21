package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.Award;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/** Award persistence; deleted awards are excluded from all reads. */
@Repository
public class AwardDAO {
    private static final String COLUMNS = "a.id,a.name,a.competition,a.competition_time,a.award_status," +
            "a.award_level,a.award_type,a.created_by,a.created_at,a.updated_at,a.year";
    private static final RowMapper<Award> MAPPER = (rs, row) -> {
        Award award = new Award();
        award.setId(rs.getInt("id"));
        award.setName(rs.getString("name"));
        award.setCompetition(rs.getString("competition"));
        java.sql.Date competitionTime = rs.getDate("competition_time");
        award.setCompetitionTime(competitionTime == null ? null : competitionTime.toString());
        award.setAwardStatus(rs.getString("award_status"));
        award.setAwardLevel((Integer) rs.getObject("award_level"));
        award.setAwardType((Integer) rs.getObject("award_type"));
        award.setCreatedBy((Integer) rs.getObject("created_by"));
        award.setCreatedAt(rs.getTimestamp("created_at"));
        award.setUpdatedAt(rs.getTimestamp("updated_at"));
        award.setYear(rs.getInt("year"));
        return award;
    };

    private final JdbcTemplate jdbc;

    public AwardDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Award> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM award a WHERE a.deleted=0 " +
                "ORDER BY a.created_at DESC,a.id DESC", MAPPER);
    }

    public List<Award> findByStatus(String status) {
        return jdbc.query("SELECT " + COLUMNS + " FROM award a WHERE a.deleted=0 AND a.award_status=? " +
                "ORDER BY a.created_at DESC,a.id DESC", MAPPER, status);
    }

    public Award findById(Integer id) {
        List<Award> rows = jdbc.query("SELECT " + COLUMNS + " FROM award a WHERE a.id=? AND a.deleted=0",
                MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Award> findByUserId(Integer userId) {
        return jdbc.query("SELECT DISTINCT " + COLUMNS + " FROM award a " +
                "LEFT JOIN award_member am ON am.award_id=a.id " +
                "WHERE a.deleted=0 AND (a.created_by=? OR am.member_id=?) " +
                "ORDER BY a.created_at DESC,a.id DESC", MAPPER, userId, userId);
    }

    public boolean insert(Award award) {
        if (award.getName() == null || award.getName().isBlank()) {
            award.setName(award.getCompetition());
        }
        KeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO award " +
                    "(name,competition,year,competition_time,award_type,award_level,award_status,created_by,deleted) " +
                    "VALUES (?,?,?,?,?,?,?, ?,0)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, award.getName());
            ps.setString(2, award.getCompetition());
            ps.setInt(3, award.getYear());
            ps.setDate(4, sqlDate(award.getCompetitionTime()));
            ps.setObject(5, award.getAwardType());
            ps.setObject(6, award.getAwardLevel());
            ps.setString(7, award.getAwardStatus());
            ps.setObject(8, award.getCreatedBy());
            return ps;
        }, keys);
        if (rows == 1) {
            if (keys.getKey() == null) {
                throw new IllegalStateException("Award insert did not return an ID");
            }
            award.setId(keys.getKey().intValue());
            return true;
        }
        return false;
    }

    public boolean update(Award award) {
        return jdbc.update("UPDATE award SET name=?,competition=?,competition_time=?,award_level=?,award_type=? " +
                        "WHERE id=? AND deleted=0 AND award_status='PENDING'",
                award.getName(), award.getCompetition(), sqlDate(award.getCompetitionTime()), award.getAwardLevel(),
                award.getAwardType(), award.getId()) == 1;
    }

    public boolean delete(Integer id) {
        return jdbc.update("UPDATE award SET deleted=1 WHERE id=? AND deleted=0 AND award_status='PENDING'", id) == 1;
    }

    public boolean approveAward(Integer id, Integer approverId) {
        return jdbc.update("UPDATE award SET award_status='APPROVED',approved_by=?,approved_at=NOW() " +
                "WHERE id=? AND deleted=0 AND award_status='PENDING'", approverId, id) == 1;
    }

    public boolean rejectAward(Integer id, Integer approverId) {
        return jdbc.update("UPDATE award SET award_status='REJECTED',approved_by=?,approved_at=NOW() " +
                "WHERE id=? AND deleted=0 AND award_status='PENDING'", approverId, id) == 1;
    }

    public boolean addMember(Integer awardId, Integer userId) {
        return jdbc.update("INSERT INTO award_member (award_id,member_id) VALUES (?,?)", awardId, userId) == 1;
    }

    private static java.sql.Date sqlDate(String value) {
        return value == null || value.isBlank() ? null : java.sql.Date.valueOf(value);
    }
}
