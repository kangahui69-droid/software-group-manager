package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.AwardImage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AwardImageDAO {
    private final JdbcTemplate jdbc;

    public AwardImageDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AwardImage> findByAwardId(Integer awardId) {
        return jdbc.query("SELECT id,award_id,file_storage_id FROM award_image WHERE award_id=? " +
                "ORDER BY is_main DESC,id", (rs, row) -> {
            AwardImage image = new AwardImage();
            image.setId(rs.getInt("id"));
            image.setAwardId(rs.getInt("award_id"));
            image.setFileId(rs.getInt("file_storage_id"));
            return image;
        }, awardId);
    }

    public boolean insert(AwardImage image) {
        return jdbc.update("INSERT INTO award_image (award_id,file_storage_id) VALUES (?,?)",
                image.getAwardId(), image.getFileId()) == 1;
    }

    public boolean deleteByAwardId(Integer awardId) {
        return jdbc.update("DELETE FROM award_image WHERE award_id=?", awardId) > 0;
    }
}
