package dao;

import model.FileStorage;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储数据访问层
 */
public class FileStorageDAO {

    /**
     * 插入文件记录
     */
    public Integer insert(FileStorage fileStorage) {
        String sql = "INSERT INTO file_storage (create_by, original_name, stored_name, file_path, file_type, file_size, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setObject(1, fileStorage.getCreateBy());
            pstmt.setString(2, fileStorage.getOriginalName());
            pstmt.setString(3, fileStorage.getStoredName() != null ? fileStorage.getStoredName() : fileStorage.getOriginalName());
            pstmt.setString(4, fileStorage.getFilePath());
            pstmt.setString(5, fileStorage.getFileType());
            pstmt.setObject(6, fileStorage.getFileSize());
            pstmt.setString(7, fileStorage.getCategory());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, generatedKeys);
        }
        return null;
    }

    /**
     * 根据ID查询文件记录
     */
    public FileStorage findById(Integer id) {
        String sql = "SELECT * FROM file_storage WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToFileStorage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 根据创建者ID查询文件记录
     */
    public List<FileStorage> findByCreateBy(Integer createBy) {
        List<FileStorage> fileStorages = new ArrayList<>();
        String sql = "SELECT * FROM file_storage WHERE create_by = ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, createBy);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                fileStorages.add(mapResultSetToFileStorage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return fileStorages;
    }

    /**
     * 根据分类查询文件记录
     */
    public List<FileStorage> findByCategory(String category) {
        List<FileStorage> fileStorages = new ArrayList<>();
        String sql = "SELECT * FROM file_storage WHERE category = ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                fileStorages.add(mapResultSetToFileStorage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return fileStorages;
    }

    /**
     * 删除文件记录
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM file_storage WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为FileStorage对象
     */
    private FileStorage mapResultSetToFileStorage(ResultSet rs) throws SQLException {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setId(rs.getInt("id"));
        fileStorage.setCreateBy(rs.getInt("create_by"));
        fileStorage.setOriginalName(rs.getString("original_name"));
        fileStorage.setFilePath(rs.getString("file_path"));
        fileStorage.setFileType(rs.getString("file_type"));
        fileStorage.setFileSize(rs.getLong("file_size"));
        fileStorage.setCategory(rs.getString("category"));
        fileStorage.setCreatedAt(rs.getTimestamp("created_at"));
        return fileStorage;
    }

    /**
     * 软删除文件（更新status为0）
     */
    public boolean softDelete(Integer id) {
        String sql = "UPDATE file_storage SET status = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 关闭资源
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}