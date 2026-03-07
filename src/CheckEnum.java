import util.DBUtil;
import java.sql.*;

public class CheckEnum {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SHOW COLUMNS FROM user LIKE 'role'";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    System.out.println("Column: " + rs.getString("Field"));
                    System.out.println("Type: " + rs.getString("Type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
