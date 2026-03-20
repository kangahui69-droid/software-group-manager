package dao;

import model.ResumeAward;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历-获奖情况数据访问对象
 * 对应数据库表: resume_awards
 */
public class ResumeAwardDAO {

    /**
     * 保存获奖记录
     * @param award 获奖对象
     * @return 是否成功
     */
    public boolean save(ResumeAward award) {
        // 修复：当 award_id 为 null 时，不使用外键约束，直接插入 NULL
        String sql = "INSERT INTO resume_awards (resume_id, award_id, award_name, competition_name, award_level, " +
                "award_date, award_org, description, is_from_system, display_order, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 修复：正确处理 resumeId，确保不为 null
            if (award.getResumeId() != null) {
                pstmt.setInt(1, award.getResumeId());
            } else {
                System.err.println("[ResumeAwardDAO.save] 错误：resumeId 不能为空");
                return false;
            }

            // 修复：正确处理 award_id，允许为 null
            if (award.getAwardId() != null) {
                pstmt.setInt(2, award.getAwardId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setString(3, award.getAwardName());
            pstmt.setString(4, award.getCompetitionName());
            pstmt.setString(5, award.getAwardLevel());
            pstmt.setDate(6, award.getAwardDate());
            pstmt.setString(7, award.getAwardOrg());
            pstmt.setString(8, award.getDescription());
            pstmt.setInt(9, award.getIsFromSystem() != null ? award.getIsFromSystem() : 0);
            pstmt.setInt(10, award.getDisplayOrder() != null ? award.getDisplayOrder() : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    award.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeAwardDAO.save] 保存获奖记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新获奖记录
     * @param award 获奖对象
     * @return 是否成功
     */
    public boolean update(ResumeAward award) {
        // 修复：添加 award_id 和 is_from_system 字段的更新
        String sql = "UPDATE resume_awards SET award_id = ?, award_name = ?, competition_name = ?, award_level = ?, " +
                "award_date = ?, award_org = ?, description = ?, is_from_system = ?, display_order = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            // 修复：正确处理 award_id，允许为 null
            if (award.getAwardId() != null) {
                pstmt.setInt(1, award.getAwardId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            pstmt.setString(2, award.getAwardName());
            pstmt.setString(3, award.getCompetitionName());
            pstmt.setString(4, award.getAwardLevel());
            pstmt.setDate(5, award.getAwardDate());
            pstmt.setString(6, award.getAwardOrg());
            pstmt.setString(7, award.getDescription());
            pstmt.setInt(8, award.getIsFromSystem() != null ? award.getIsFromSystem() : 0);
            pstmt.setInt(9, award.getDisplayOrder() != null ? award.getDisplayOrder() : 0);
            pstmt.setInt(10, award.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeAwardDAO.update] 更新获奖记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询获奖记录
     * @param id 记录ID
     * @return 获奖对象
     */
    public ResumeAward findById(Integer id) {
        String sql = "SELECT ra.*, aw.competition as award_competition FROM resume_awards ra " +
                "LEFT JOIN award aw ON ra.award_id = aw.id WHERE ra.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ResumeAwardDAO.findById] 查询获奖记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询简历的所有获奖记录
     * @param resumeId 简历ID
     * @return 获奖记录列表
     */
    public List<ResumeAward> findByResumeId(Integer resumeId) {
        String sql = "SELECT ra.*, aw.competition as award_competition FROM resume_awards ra " +
                "LEFT JOIN award aw ON ra.award_id = aw.id WHERE ra.resume_id = ? ORDER BY ra.display_order ASC, ra.award_date DESC";
        List<ResumeAward> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, resumeId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ResumeAwardDAO.findByResumeId] 查询获奖记录列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除获奖记录
     * @param id 记录ID
     * @return 是否成功
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_awards WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeAwardDAO.delete] 删除获奖记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * ResultSet映射为实体
     */
    private ResumeAward mapResultSetToEntity(ResultSet rs) throws SQLException {
        ResumeAward award = new ResumeAward();
        award.setId(rs.getInt("id"));
        award.setResumeId(rs.getInt("resume_id"));

        // 修复：正确处理 award_id 可能为 NULL 的情况
        int awardId = rs.getInt("award_id");
        if (rs.wasNull()) {
            award.setAwardId(null);
        } else {
            award.setAwardId(awardId);
        }

        award.setAwardName(rs.getString("award_name"));
        award.setCompetitionName(rs.getString("competition_name"));
        award.setAwardLevel(rs.getString("award_level"));
        award.setAwardDate(rs.getDate("award_date"));
        award.setAwardOrg(rs.getString("award_org"));
        award.setDescription(rs.getString("description"));

        // 修复：同样处理 is_from_system 可能为 NULL 的情况
        int isFromSystem = rs.getInt("is_from_system");
        if (rs.wasNull()) {
            award.setIsFromSystem(null);
        } else {
            award.setIsFromSystem(isFromSystem);
        }

        award.setDisplayOrder(rs.getInt("display_order"));
        award.setCreatedAt(rs.getTimestamp("created_at"));
        return award;
    }
}
