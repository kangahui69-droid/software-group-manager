package dao;

import model.Award;
import model.Dictionary;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖项数据访问层
 */
public class AwardDAO {
    private DictionaryDAO dictionaryDAO = new DictionaryDAO();

    /**
     * 查询所有已审核通过的奖项
     */
    public List<Award> findApproved() {
        List<Award> awards = new ArrayList<>();
        String sql = "SELECT * FROM award WHERE award_status = 'APPROVED' ORDER BY competition_year DESC, created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }
    
    /**
     * 根据用户ID查询奖项
     */
    public List<Award> findByUserId(Integer userId) {
        List<Award> awards = new ArrayList<>();
        String sql = "SELECT DISTINCT a.* FROM award a " +
                     "LEFT JOIN award_member am ON a.id = am.award_id " +
                     "WHERE a.created_by = ? OR am.member_id = ? " +
                     "ORDER BY a.competition_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }

    /**
     * 查询所有奖项（管理员用）
     */
    public List<Award> findAll() {
        List<Award> awards = new ArrayList<>();
        String sql = "SELECT * FROM award ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }
    
    /**
     * 根据状态查询奖项
     */
    public List<Award> findByStatus(String status) {
        List<Award> awards = new ArrayList<>();
        System.out.println("=== findByStatus called with status: " + status);
        String sql;
        if (status == null || status.isEmpty() || "ALL".equals(status)) {
            sql = "SELECT * FROM award ORDER BY created_at DESC";
            System.out.println("=== Using SQL without WHERE clause (status is null/empty/ALL)");
        } else {
            sql = "SELECT * FROM award WHERE award_status = ? ORDER BY created_at DESC";
            System.out.println("=== Using SQL with WHERE award_status = ?");
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            System.out.println("=== Database connection obtained");
            if (status == null || status.isEmpty() || "ALL".equals(status)) {
                pstmt = conn.prepareStatement(sql);
            } else {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, status);
                System.out.println("=== Set parameter 1 = " + status);
            }
            rs = pstmt.executeQuery();
            System.out.println("=== Query executed successfully");
            int count = 0;
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
                count++;
            }
            System.out.println("=== findByStatus returned " + count + " awards");
        } catch (SQLException e) {
            System.err.println("=== SQL Error in findByStatus: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }

    /**
     * 根据状态和关键词查询奖项
     */
    public List<Award> findByStatusAndKeyword(String status, String keyword) {
        List<Award> awards = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM award WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            sql.append(" AND award_status = ?");
            params.add(status);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR competition LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }

    /**
     * 根据多个条件查询奖项
     */
    public List<Award> findByConditions(String status, String keyword, String awardType, String awardCategory, String awardLevel, String competitionLevel) {
        List<Award> awards = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM award WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            sql.append(" AND award_status = ?");
            params.add(status);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR competition LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        
        if (awardType != null && !awardType.trim().isEmpty()) {
            sql.append(" AND award_type = ?");
            params.add(awardType);
        }
        
        if (awardCategory != null && !awardCategory.trim().isEmpty()) {
            sql.append(" AND award_category = ?");
            params.add(awardCategory);
        }
        
        if (awardLevel != null && !awardLevel.trim().isEmpty()) {
            sql.append(" AND award_level = ?");
            params.add(awardLevel);
        }
        
        if (competitionLevel != null && !competitionLevel.trim().isEmpty()) {
            sql.append(" AND competition_level = ?");
            params.add(competitionLevel);
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return awards;
    }

    /**
     * 根据ID查询奖项
     */
    public Award findById(Integer id) {
        String sql = "SELECT * FROM award WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAward(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 添加奖项
     */
    public boolean insert(Award award) {
        String sql = "INSERT INTO award (name, competition, year, competition_time, competition_location, competition_session, award_type, award_category, award_level, competition_level, team_name, award_status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, award.getName());
            pstmt.setString(2, award.getCompetition());
            pstmt.setObject(3, award.getYear());
            pstmt.setDate(4, award.getCompetitionTime() != null ? new java.sql.Date(award.getCompetitionTime().getTime()) : null);
            pstmt.setString(5, award.getCompetitionLocation());
            pstmt.setString(6, award.getCompetitionSession());
            pstmt.setObject(7, award.getAwardType());
            pstmt.setObject(8, award.getAwardCategory());
            pstmt.setObject(9, award.getAwardLevel());
            pstmt.setObject(10, award.getCompetitionLevel());
            pstmt.setString(11, award.getTeamName());
            pstmt.setString(12, award.getAwardStatus() != null ? award.getAwardStatus() : "PENDING");
            pstmt.setInt(13, award.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    award.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, generatedKeys);
        }
        return false;
    }

    /**
     * 更新奖项审核状态
     */
    public boolean updateAwardStatus(Integer id, String status) {
        String sql = "UPDATE award SET award_status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }
    
    /**
     * 更新奖项信息
     */
    public boolean update(Award award) {
        String sql = "UPDATE award SET name = ?, competition = ?, year = ?, competition_time = ?, competition_location = ?, competition_session = ?, award_type = ?, award_category = ?, award_level = ?, competition_level = ?, team_name = ?, award_status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, award.getName());
            pstmt.setString(2, award.getCompetition());
            pstmt.setObject(3, award.getYear());
            pstmt.setDate(4, award.getCompetitionTime() != null ? new java.sql.Date(award.getCompetitionTime().getTime()) : null);
            pstmt.setString(5, award.getCompetitionLocation());
            pstmt.setString(6, award.getCompetitionSession());
            pstmt.setObject(7, award.getAwardType());
            pstmt.setObject(8, award.getAwardCategory());
            pstmt.setObject(9, award.getAwardLevel());
            pstmt.setObject(10, award.getCompetitionLevel());
            pstmt.setString(11, award.getTeamName());
            pstmt.setString(12, award.getAwardStatus());
            pstmt.setInt(13, award.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }
    
    /**
     * 审核通过奖项
     */
    public boolean approveAward(Integer id, Integer approvedBy) {
        String sql = "UPDATE award SET award_status = 'APPROVED', approved_by = ?, approved_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, approvedBy);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }
    
    /**
     * 拒绝奖项
     */
    public boolean rejectAward(Integer id, Integer approvedBy) {
        String sql = "UPDATE award SET award_status = 'REJECTED', approved_by = ?, approved_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, approvedBy);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 添加奖项成员关联
     */
    public boolean addMember(Integer awardId, Integer memberId) {
        String sql = "INSERT INTO award_member (award_id, member_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, awardId);
            pstmt.setInt(2, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 移除奖项成员关联
     */
    public boolean removeMember(Integer awardId, Integer memberId) {
        String sql = "DELETE FROM award_member WHERE award_id = ? AND member_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, awardId);
            pstmt.setInt(2, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除奖项（软删除）
     */
    public boolean delete(Integer awardId) {
        String sql = "UPDATE award SET deleted = 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, awardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Award对象
     */
    private Award mapResultSetToAward(ResultSet rs) throws SQLException {
        Award award = new Award();
        award.setId(rs.getInt("id"));
        try {
            award.setName(rs.getString("name"));
        } catch (SQLException e) {
            award.setName(null);
        }
        try {
            // 使用getObject方法处理可能为null的整数字段
            Object awardLevelObj = rs.getObject("award_level");
            if (awardLevelObj != null) {
                award.setAwardLevel((Integer) awardLevelObj);
            } else {
                award.setAwardLevel(null);
            }
        } catch (SQLException e) {
            award.setAwardLevel(null);
        }
        award.setCompetition(rs.getString("competition"));
        // 移除对不存在字段的映射
        // award.setLevel(rs.getString("level"));
        // award.setCategory(rs.getString("category"));
        // award.setYear(rs.getInt("year"));
        // award.setCertificatePath(rs.getString("certificate_path"));
        try {
            award.setCompetitionTime(rs.getDate("competition_time"));
        } catch (SQLException e) {
            award.setCompetitionTime(null);
        }
        try {
            award.setCompetitionLocation(rs.getString("competition_location"));
        } catch (SQLException e) {
            award.setCompetitionLocation(null);
        }
        try {
            award.setCompetitionSession(rs.getString("competition_session"));
        } catch (SQLException e) {
            award.setCompetitionSession(null);
        }
        try {
            // 使用getObject方法处理可能为null的整数字段
            Object awardTypeObj = rs.getObject("award_type");
            if (awardTypeObj != null) {
                award.setAwardType((Integer) awardTypeObj);
            } else {
                award.setAwardType(null);
            }
        } catch (SQLException e) {
            award.setAwardType(null);
        }
        try {
            // 使用getObject方法处理可能为null的整数字段
            Object awardCategoryObj = rs.getObject("award_category");
            if (awardCategoryObj != null) {
                award.setAwardCategory((Integer) awardCategoryObj);
            } else {
                award.setAwardCategory(null);
            }
        } catch (SQLException e) {
            award.setAwardCategory(null);
        }
        try {
            // 使用getObject方法处理可能为null的整数字段
            Object competitionLevelObj = rs.getObject("competition_level");
            if (competitionLevelObj != null) {
                award.setCompetitionLevel((Integer) competitionLevelObj);
            } else {
                award.setCompetitionLevel(null);
            }
        } catch (SQLException e) {
            award.setCompetitionLevel(null);
        }
        try {
            award.setTeamName(rs.getString("team_name"));
        } catch (SQLException e) {
            award.setTeamName(null);
        }
        try {
            award.setAwardStatus(rs.getString("award_status"));
        } catch (SQLException e) {
            award.setAwardStatus(null);
        }
        try {
            award.setCreatedBy(rs.getInt("created_by"));
        } catch (SQLException e) {
            // 如果字段不存在或值为null，设置为null
            award.setCreatedBy(null);
        }
        try {
            award.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {
            award.setCreatedAt(null);
        }
        try {
            award.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {
            award.setUpdatedAt(null);
        }
        try {
            award.setApprovedBy(rs.getInt("approved_by"));
        } catch (SQLException e) {
            // 如果字段不存在，设置为null
            award.setApprovedBy(null);
        }
        try {
            award.setApprovedAt(rs.getTimestamp("approved_at"));
        } catch (SQLException e) {
            award.setApprovedAt(null);
        }
        return award;
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

