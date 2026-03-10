package dao;

import model.User;
import util.DBUtil;
import util.DESUtil;
import util.StringUtil;

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
        System.out.println("[UserDAO] 开始登录验证 - 用户名: " + username);
        System.out.println("[UserDAO] 输入的明文密码: " + password);
        
        // 先根据用户名查询用户
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
                System.out.println("[UserDAO] 找到用户记录");
                
                // 获取数据库中存储的加密密码
                String storedPassword = rs.getString("password");
                System.out.println("[UserDAO] 数据库中的加密密码: " + storedPassword);
                
                // 使用DES加密用户输入的密码
                String encryptedPassword = DESUtil.encrypt(password);
                System.out.println("[UserDAO] 加密后的输入密码: " + encryptedPassword);
                System.out.println("[UserDAO] 当前DES密钥: " + config.Config.getDesKey());
                System.out.println("[UserDAO] 密码是否匹配: " + storedPassword.equals(encryptedPassword));
                
                // 比较加密后的密码是否匹配
                if (storedPassword.equals(encryptedPassword)) {
                    System.out.println("[UserDAO] 密码验证成功！");
                    return mapResultSetToUser(rs);
                } else {
                    System.out.println("[UserDAO] 密码验证失败！");
                }
            } else {
                System.out.println("[UserDAO] 未找到用户记录");
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] SQL异常: " + e.getMessage());
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
    public boolean insert(User user) throws SQLException {
        System.out.println("=== DEBUG UserDAO.insert: username=" + user.getUsername() + ", role=" + user.getRole());
        String sql = "INSERT INTO user (username, password, name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, DESUtil.encrypt(user.getPassword()));
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getRole());
            pstmt.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    System.out.println("=== DEBUG UserDAO.insert: generated id = " + user.getId());
                }
            }
            return result;
        } catch (SQLException e) {
            System.out.println("=== DEBUG UserDAO.insert ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception for caller to handle
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 更新用户状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE user SET status = ? WHERE id = ?";
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
     * 更新用户信息
     */
    public boolean update(User user) {
        String sql = "UPDATE user SET username = ?, name = ?, email = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getRole());
            pstmt.setInt(6, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(7, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
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
        String sql = "DELETE FROM user WHERE id = ?";
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
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            System.out.println("[DEBUG] Checking if email exists: " + email);
            conn = DBUtil.getConnection();
            System.out.println("[DEBUG] Database connection established: " + (conn != null));
            pstmt = conn.prepareStatement(sql);
            System.out.println("[DEBUG] PreparedStatement created: " + (pstmt != null));
            pstmt.setString(1, email);
            System.out.println("[DEBUG] Email parameter set: " + email);
            rs = pstmt.executeQuery();
            System.out.println("[DEBUG] Query executed, ResultSet: " + (rs != null));
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("[DEBUG] Email check result: " + count + " records found");
                return count > 0;
            } else {
                System.out.println("[DEBUG] ResultSet has no rows for email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
            System.out.println("[DEBUG] Resources closed for email check");
        }
        System.out.println("[DEBUG] Email does not exist or error occurred: " + email);
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
            System.out.println("[DEBUG] Checking if username exists: " + username);
            conn = DBUtil.getConnection();
            System.out.println("[DEBUG] Database connection established: " + (conn != null));
            pstmt = conn.prepareStatement(sql);
            System.out.println("[DEBUG] PreparedStatement created: " + (pstmt != null));
            pstmt.setString(1, username);
            System.out.println("[DEBUG] Username parameter set: " + username);
            rs = pstmt.executeQuery();
            System.out.println("[DEBUG] Query executed, ResultSet: " + (rs != null));
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("[DEBUG] Username check result: " + count + " records found");
                return count > 0;
            } else {
                System.out.println("[DEBUG] ResultSet has no rows for username: " + username);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error checking username existence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
            System.out.println("[DEBUG] Resources closed for username check");
        }
        System.out.println("[DEBUG] Username does not exist or error occurred: " + username);
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
}
