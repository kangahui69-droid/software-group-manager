import util.DBUtil;
import util.StringUtil;
import java.sql.*;

public class FixDatabaseData {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Connecting to database to fix user data and schema...");

            Statement stmt = conn.createStatement();

            // 1. Alter schema if needed (Change status to INT)
            try {
                System.out.println("Attempting to alter status column to INT...");
                stmt.execute("ALTER TABLE user MODIFY COLUMN status INT DEFAULT 1");
                System.out.println("Successfully altered status column to INT.");
            } catch (SQLException e) {
                System.out.println(
                        "Note: Could not alter column (might already be INT or have data issues): " + e.getMessage());
                // If it fails, we still try to update the values
            }

            // 2. Fix passwords (hash plaintext passwords)
            String selectSql = "SELECT id, username, password FROM user";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");

                    if (password == null || password.length() != 32) {
                        String hashed = StringUtil.md5(password);
                        String updateSql = "UPDATE user SET password = ? WHERE id = ?";
                        try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                            upstmt.setString(1, hashed);
                            upstmt.setInt(2, id);
                            upstmt.executeUpdate();
                            System.out.println("Hashed password for user: " + username);
                        }
                    }
                }
            }

            // 3. Fix status (set to 1 for everyone)
            try {
                int count = stmt.executeUpdate("UPDATE user SET status = 1");
                System.out.println("Updated status to 1 for " + count + " users.");
            } catch (SQLException e) {
                System.err.println("Error updating status: " + e.getMessage());
            }

            System.out.println("Database fix complete.");

        } catch (SQLException e) {
            System.err.println("Critical Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
