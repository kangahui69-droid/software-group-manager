package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.FileStorage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class FileStorageDAO {
    private final JdbcTemplate jdbc;
    public FileStorageDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<FileStorage> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT id,original_name,file_path,file_type,file_size,create_by,category " +
                "FROM file_storage WHERE status=1 AND id IN (" + placeholders + ")",
                (rs, row) -> map(rs), ids.toArray());
    }
    public FileStorage findById(Integer id) {
        List<FileStorage> rows = jdbc.query("SELECT id,original_name,file_path,file_type,file_size,create_by,category " +
                "FROM file_storage WHERE status=1 AND id=?", (rs, row) -> map(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }
    private static FileStorage map(ResultSet rs) throws SQLException {
        FileStorage file = new FileStorage();
        file.setId(rs.getInt("id"));
        file.setFileName(rs.getString("original_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileType(rs.getString("file_type"));
        file.setFileSize((Long) rs.getObject("file_size"));
        file.setUploadedBy(rs.getInt("create_by"));
        file.setCategory(rs.getString("category"));
        return file;
    }
}
