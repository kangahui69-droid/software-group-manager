package dao;

import model.Dictionary;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据访问层
 */
public class DictionaryDAO {

    /**
     * 根据类型获取字典列表
     */
    public List<Dictionary> findByType(String type) {
        List<Dictionary> dictionaries = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE type = ? AND status = 1 ORDER BY sort_order";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                dictionaries.add(mapResultSetToDictionary(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return dictionaries;
    }

    /**
     * 根据类型和代码获取字典
     */
    public Dictionary findByTypeAndCode(String type, String code) {
        String sql = "SELECT * FROM dictionary WHERE type = ? AND code = ? AND status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type);
            pstmt.setString(2, code);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDictionary(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 插入字典数据
     */
    public boolean insert(Dictionary dictionary) {
        String sql = "INSERT INTO dictionary (code, name, type, sort_order, status, description) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dictionary.getCode());
            pstmt.setString(2, dictionary.getName());
            pstmt.setString(3, dictionary.getType());
            pstmt.setInt(4, dictionary.getSortOrder());
            pstmt.setInt(5, dictionary.getStatus() ? 1 : 0);
            pstmt.setString(6, dictionary.getDescription());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Dictionary对象
     */
    private Dictionary mapResultSetToDictionary(ResultSet rs) throws SQLException {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(rs.getInt("id"));
        dictionary.setCode(rs.getString("code"));
        dictionary.setName(rs.getString("name"));
        dictionary.setType(rs.getString("type"));
        dictionary.setSortOrder(rs.getInt("sort_order"));
        dictionary.setStatus(rs.getInt("status") == 1);
        dictionary.setDescription(rs.getString("description"));
        dictionary.setCreatedAt(rs.getTimestamp("created_at"));
        dictionary.setUpdatedAt(rs.getTimestamp("updated_at"));
        return dictionary;
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
