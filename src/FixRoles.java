import util.DBUtil;
import java.sql.*;

public class FixRoles {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Updating user roles to uppercase...");
            String sql = "UPDATE user SET role = UPPER(role)";
            try (Statement stmt = conn.createStatement()) {
                int count = stmt.executeUpdate(sql);
                System.out.println("Updated " + count + " users.");
            }

            // Verify
            System.out.println("\nVerifying roles:");
            String verifySql = "SELECT username, role FROM user";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(verifySql)) {
                while (rs.next()) {
                    System.out.println(rs.getString("username") + ": " + rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
