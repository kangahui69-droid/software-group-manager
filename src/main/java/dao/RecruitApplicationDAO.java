package dao;

import model.RecruitApplication;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 招新申请数据访问层
 */
public class RecruitApplicationDAO {

    /**
     * 添加申请
     */
    public boolean insert(RecruitApplication application) {
        String sql = "INSERT INTO recruit_application (name, student_id, major, grade, phone, email, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, application.getName());
            pstmt.setString(2, application.getStudentId());
            pstmt.setString(3, application.getMajor());
            pstmt.setString(4, application.getGrade());
            pstmt.setString(5, application.getPhone());
            // 如果邮箱为空或空字符串，设置为null，避免唯一约束冲突
            String email = application.getEmail();
            pstmt.setString(6, (email != null && !email.trim().isEmpty()) ? email : null);
            pstmt.setString(7, application.getReason());
            pstmt.setInt(8, application.getStatus() != null ? application.getStatus() : 1);

            int result = pstmt.executeUpdate();
            System.out.println("RecruitApplicationDAO.insert - Rows affected: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("RecruitApplicationDAO.insert - SQL Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("数据库插入失败: " + e.getMessage(), e);
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 查询所有申请，可选年份过滤
     */
    public List<RecruitApplication> findAll(Integer year) {
        return findByConditions(null, year, null, null);
    }

    /**
     * 根据条件搜索申请
     */
    public List<RecruitApplication> findByConditions(String keyword, Integer year, String status, Integer round) {
        List<RecruitApplication> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM recruit_application WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR student_id LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (year != null) {
            sql.append(" AND YEAR(created_at) = ?");
            params.add(year);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (round != null) {
            sql.append(" AND round = ?");
            params.add(round);
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
                applications.add(mapResultSetToApplication(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return applications;
    }

    public List<RecruitApplication> findAll() {
        return findAll(null);
    }

    /**
     * 根据ID查询申请
     */
    public RecruitApplication findById(Integer id) {
        String sql = "SELECT * FROM recruit_application WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToApplication(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 更新申请状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE recruit_application SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
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
     * 更新申请信息
     */
    public boolean update(RecruitApplication application) {
        String sql = "UPDATE recruit_application SET name = ?, student_id = ?, major = ?, grade = ?, phone = ?, email = ?, reason = ?, status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, application.getName());
            pstmt.setString(2, application.getStudentId());
            pstmt.setString(3, application.getMajor());
            pstmt.setString(4, application.getGrade());
            pstmt.setString(5, application.getPhone());
            pstmt.setString(6, application.getEmail());
            pstmt.setString(7, application.getReason());
            pstmt.setInt(8, application.getStatus());
            pstmt.setInt(9, application.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除申请
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM recruit_application WHERE id = ?";
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
     * 获取所有报名的年份
     */
    public List<Integer> findAllYears() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(created_at) as year FROM recruit_application ORDER BY year DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                years.add(rs.getInt("year"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return years;
    }

    /**
     * 将ResultSet映射为RecruitApplication对象
     */
    private RecruitApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        RecruitApplication application = new RecruitApplication();
        application.setId(rs.getInt("id"));
        application.setName(rs.getString("name"));
        application.setStudentId(rs.getString("student_id"));
        application.setMajor(rs.getString("major"));
        application.setGrade(rs.getString("grade"));
        application.setPhone(rs.getString("phone"));
        application.setEmail(rs.getString("email"));
        application.setReason(rs.getString("reason"));
        application.setStatus(rs.getInt("status"));
        application.setCreatedAt(rs.getTimestamp("created_at"));
        return application;
    }

    /**
     * 统计待审核的招新申请数量（status = 1 表示待审核）
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM recruit_application WHERE status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return 0;
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
