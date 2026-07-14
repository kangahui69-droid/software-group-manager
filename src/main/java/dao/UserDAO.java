package dao;

import model.User;
import util.DBUtil;
import util.DESUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问层
 */
public class UserDAO {

    /**
     * 根据用户名和密码查询用户（用于登录）
     */
    public User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM user WHERE username = ? AND status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String encryptedPassword = DESUtil.encrypt(password);
                if (storedPassword.equals(encryptedPassword)) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 根据ID查询用户
     */
    public User findById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return findByConditions(null, null, null);
    }

    /**
     * 根据条件搜索用户
     */
    public List<User> findByConditions(String keyword, String role, String status) {
        List<User> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR name LIKE ? OR email LIKE ? OR phone LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
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
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return users;
    }

    /**
     * 添加用户
     */
    public boolean insert(User user) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return insert(user, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 添加用户（事务重载版本）
     */
    public boolean insert(User user, Connection conn) {
        String sql = "INSERT INTO user (username, password, name, email, phone, role, status, must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, DESUtil.encrypt(user.getPassword()));
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getRole());
            pstmt.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(8, Boolean.TRUE.equals(user.getMustChangePassword()) ? 1 : 0);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("插入用户失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 更新用户状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return updateStatus(id, status, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 更新用户状态（事务重载版本）
     */
    public boolean updateStatus(Integer id, Integer status, Connection conn) {
        String sql = "UPDATE user SET status = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新用户状态失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 更新用户信息
     */
    public boolean update(User user) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return update(user, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 更新用户信息（事务重载版本）
     */
    public boolean update(User user, Connection conn) {
        String sql = "UPDATE user SET username = ?, name = ?, email = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getRole());
            pstmt.setInt(6, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(7, user.getId());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("更新用户信息失败：用户不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新用户信息失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 更新用户密码（自动加密）
     */
    public boolean updatePassword(Integer userId, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, DESUtil.encrypt(newPassword));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 重置密码（设置随机密码并标记必须修改）
     * @param userId 用户ID
     * @param newPassword 新密码（明文，会自动加密）
     * @return 是否成功
     */
    public boolean resetPassword(Integer userId, String newPassword) {
        String sql = "UPDATE user SET password = ?, must_change_password = 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, DESUtil.encrypt(newPassword));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 清除必须修改密码标志
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean clearMustChangePassword(Integer userId) {
        String sql = "UPDATE user SET must_change_password = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 直接更新密码（不加密，用于密码加密方式转换）
     */
    public boolean updatePasswordDirect(Integer userId, String encryptedPassword) {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, encryptedPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 验证用户密码
     */
    public boolean verifyPassword(Integer userId, String password) {
        String sql = "SELECT COUNT(*) FROM user WHERE id = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, DESUtil.encrypt(password));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 删除用户
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
     * 删除用户（事务重载版本）
     */
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM user WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("删除用户失败：用户不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("删除用户失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 将ResultSet映射为User对象
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));

        // 处理 must_change_password 字段（可能为null）
        try {
            int mustChange = rs.getInt("must_change_password");
            user.setMustChangePassword(mustChange == 1);
        } catch (SQLException e) {
            // 字段不存在时忽略
            user.setMustChangePassword(false);
        }

        return user;
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

    /**
     * 统计用户总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM user WHERE status = 1";
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
}
