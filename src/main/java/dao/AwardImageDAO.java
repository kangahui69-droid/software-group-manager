package dao;

import model.AwardImage;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖项图片数据访问层
 */
public class AwardImageDAO {

    /**
     * 根据奖项ID获取图片列表
     */
    public List<AwardImage> findByAwardId(Integer awardId) {
        List<AwardImage> images = new ArrayList<>();
        String sql = "SELECT * FROM award_image WHERE award_id = ? ORDER BY is_main DESC, created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, awardId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                images.add(mapResultSetToAwardImage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return images;
    }

    /**
     * 根据奖项ID和成员ID获取图片列表
     */
    public List<AwardImage> findByAwardIdAndMemberId(Integer awardId, Integer memberId) {
        List<AwardImage> images = new ArrayList<>();
        String sql = "SELECT * FROM award_image WHERE award_id = ? AND member_id = ? ORDER BY is_main DESC, created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, awardId);
            pstmt.setInt(2, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                images.add(mapResultSetToAwardImage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return images;
    }

    /**
     * 插入奖项图片
     */
    public boolean insert(AwardImage image) {
        String sql = "INSERT INTO award_image (award_id, member_id, is_main, file_storage_id) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, image.getAwardId());
            pstmt.setObject(2, image.getMemberId());
            pstmt.setBoolean(3, image.getIsMain() != null ? image.getIsMain() : false);
            pstmt.setInt(4, image.getFileStorageId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除奖项图片
     */
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM award_image WHERE id = ?";
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
     * 将ResultSet映射为AwardImage对象
     */
    private AwardImage mapResultSetToAwardImage(ResultSet rs) throws SQLException {
        AwardImage image = new AwardImage();
        image.setId(rs.getInt("id"));
        image.setAwardId(rs.getInt("award_id"));
        try {
            image.setMemberId(rs.getInt("member_id"));
        } catch (SQLException e) {
            image.setMemberId(null);
        }
        image.setIsMain(rs.getBoolean("is_main"));
        image.setCreatedAt(rs.getTimestamp("created_at"));
        image.setFileStorageId(rs.getInt("file_storage_id"));
        return image;
    }

    /**
     * 关闭资源
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
