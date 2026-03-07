import util.DBUtil;
import java.sql.*;

public class CheckUserTest {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM user";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Checking user table:");
                boolean found = false;
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(meta.getColumnName(i) + "(" + meta.getColumnTypeName(i) + ") | ");
                }
                System.out.println();
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getObject("id") +
                            ", Username: " + rs.getString("username") +
                            ", Password(Hash): " + rs.getString("password") +
                            ", Role: " + rs.getString("role") +
                            ", Status: " + rs.getString("status"));
                }
                if (!found) {
                    System.out.println("No users found in the table.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
