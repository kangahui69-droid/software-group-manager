import util.DBUtil;
import java.sql.*;

public class FixDatabaseFinal {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Altering 'role' column ENUM to uppercase...");
            String alterSql = "ALTER TABLE user MODIFY COLUMN role ENUM('ADMIN', 'MEMBER', 'TEACHER', 'GUEST') NOT NULL DEFAULT 'MEMBER'";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(alterSql);
                System.out.println("Column altered successfully.");
            }

            System.out.println("Updating roles to uppercase (to be safe)...");
            String updateSql = "UPDATE user SET role = 'ADMIN' WHERE role = 'admin'";
            String updateSql2 = "UPDATE user SET role = 'MEMBER' WHERE role = 'member'";
            try (Statement stmt = conn.createStatement()) {
                int count1 = stmt.executeUpdate(updateSql);
                int count2 = stmt.executeUpdate(updateSql2);
                System.out.println("Updated " + (count1 + count2) + " users.");
            }

            // Verify
            System.out.println("\nFinal verification of roles:");
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
