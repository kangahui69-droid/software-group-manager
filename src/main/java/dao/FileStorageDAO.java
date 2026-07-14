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
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return insert(fileStorage, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 插入文件记录（事务重载版本）
     */
    public Integer insert(FileStorage fileStorage, Connection conn) {
        if (fileStorage.getOriginalName() == null || fileStorage.getOriginalName().trim().isEmpty()) {
            throw new RuntimeException("插入文件记录失败：原始文件名不能为空");
        }
        String sql = "INSERT INTO file_storage (create_by, original_name, stored_name, file_path, file_type, file_size, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setObject(1, fileStorage.getCreateBy());
            pstmt.setString(2, fileStorage.getOriginalName());

            String storedName = fileStorage.getStoredName() != null ? fileStorage.getStoredName() : fileStorage.getOriginalName();
            pstmt.setString(3, storedName);
            pstmt.setString(4, fileStorage.getFilePath());

            String fileType = fileStorage.getFileType();
            if (fileType != null && fileType.length() > 200) {
                fileType = fileType.substring(0, 200);
            }
            pstmt.setString(5, fileType);
            pstmt.setObject(6, fileStorage.getFileSize());
            pstmt.setString(7, fileStorage.getCategory());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    fileStorage.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入文件记录失败", e);
        } finally {
            closeResources(null, pstmt, generatedKeys);
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
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return delete(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 删除文件记录（事务重载版本）
     */
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM file_storage WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("删除文件记录失败：文件不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("删除文件记录失败", e);
        } finally {
            closeResources(null, pstmt, null);
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
     * 软删除文件（更新status为0）
     */
    public boolean softDelete(Integer id) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return softDelete(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 软删除文件（更新status为0，事务重载版本）
     */
    public boolean softDelete(Integer id, Connection conn) {
        String sql = "UPDATE file_storage SET status = 0 WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("软删除文件失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
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