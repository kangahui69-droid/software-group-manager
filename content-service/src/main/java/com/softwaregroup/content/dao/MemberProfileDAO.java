package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.MemberProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MemberProfileDAO {
    private final JdbcTemplate jdbc;
    public MemberProfileDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public MemberProfile findByUserId(Integer userId) {
        List<MemberProfile> rows = jdbc.query("SELECT p.id,p.user_id,p.student_id,p.major,p.avatar_file_id," +
                        "u.name,u.phone,u.email FROM member_profile p JOIN user u ON u.id=p.user_id " +
                        "WHERE p.user_id=? AND u.status=1 ORDER BY p.id DESC LIMIT 1",
                (rs, row) -> {
                    MemberProfile profile = new MemberProfile();
                    profile.setId(rs.getInt("id"));
                    profile.setUserId(rs.getInt("user_id"));
                    profile.setStudentId(rs.getString("student_id"));
                    profile.setMajor(rs.getString("major"));
                    profile.setAvatarFileId((Integer) rs.getObject("avatar_file_id"));
                    profile.setName(rs.getString("name"));
                    profile.setPhone(rs.getString("phone"));
                    profile.setEmail(rs.getString("email"));
                    return profile;
                }, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
