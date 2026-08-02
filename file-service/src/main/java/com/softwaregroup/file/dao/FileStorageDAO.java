package com.softwaregroup.file.dao;

import com.softwaregroup.file.model.entity.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储数据访问层
 */
@Repository
public class FileStorageDAO {

    @Autowired
    private DataSource dataSource;

    /**
     * 插入文件记录
     */
    public Integer insert(FileStorage fileStorage) {
        String sql = "INSERT INTO file_storage (create_by, original_name, stored_name, file_path, file_type, file_size, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setObject(1, fileStorage.getCreateBy());
            pstmt.setString(2, fileStorage.getOriginalName());
            pstmt.setString(3, fileStorage.getStoredName() != null ? fileStorage.getStoredName() : fileStorage.getOriginalName());
            pstmt.setString(4, fileStorage.getFilePath());
            pstmt.setString(5, truncateFileType(fileStorage.getFileType()));
            pstmt.setObject(6, fileStorage.getFileSize());
            pstmt.setString(7, fileStorage.getCategory());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        fileStorage.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件记录失败", e);
        }
        return null;
    }

    /**
     * 根据ID查询文件记录
     */
    public FileStorage findById(Integer id) {
        String sql = "SELECT * FROM file_storage WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFileStorage(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件记录失败", e);
        }
        return null;
    }

    /**
     * 根据创建者ID查询文件记录
     */
    public List<FileStorage> findByCreateBy(Integer createBy) {
        List<FileStorage> fileStorages = new ArrayList<>();
        String sql = "SELECT * FROM file_storage WHERE create_by = ? ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, createBy);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fileStorages.add(mapResultSetToFileStorage(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件记录失败", e);
        }
        return fileStorages;
    }

    /**
     * 根据分类查询文件记录
     */
    public List<FileStorage> findByCategory(String category) {
        List<FileStorage> fileStorages = new ArrayList<>();
        String sql = "SELECT * FROM file_storage WHERE category = ? ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fileStorages.add(mapResultSetToFileStorage(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询文件记录失败", e);
        }
        return fileStorages;
    }

    /**
     * 软删除文件（更新status为0）
     */
    public boolean softDelete(Integer id) {
        String sql = "UPDATE file_storage SET status = 0 WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("软删除文件失败", e);
        }
    }

    /**
     * 将ResultSet映射为FileStorage对象
     */
    private FileStorage mapResultSetToFileStorage(ResultSet rs) throws SQLException {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setId(rs.getInt("id"));
        fileStorage.setCreateBy(rs.getInt("create_by"));
        fileStorage.setOriginalName(rs.getString("original_name"));
        fileStorage.setStoredName(rs.getString("stored_name"));
        fileStorage.setFilePath(rs.getString("file_path"));
        fileStorage.setFileType(rs.getString("file_type"));
        fileStorage.setFileSize(rs.getLong("file_size"));
        fileStorage.setCategory(rs.getString("category"));
        fileStorage.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            fileStorage.setStatus(rs.getInt("status"));
        } catch (SQLException e) {
            fileStorage.setStatus(1);
        }
        return fileStorage;
    }

    /**
     * 截断文件类型字符串（防止超长）
     */
    private String truncateFileType(String fileType) {
        if (fileType != null && fileType.length() > 200) {
            return fileType.substring(0, 200);
        }
        return fileType;
    }
}
