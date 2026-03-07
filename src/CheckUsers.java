import util.DBUtil;
import java.sql.*;

public class CheckUsers {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Checking users in database...");
            String sql = "SELECT id, username, role, status FROM user";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                System.out.printf("%-5s | %-15s | %-15s | %-10s%n", "ID", "Username", "Role", "Status");
                System.out.println("-------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-5d | %-15s | %-15s | %-10d%n",
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getInt("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
