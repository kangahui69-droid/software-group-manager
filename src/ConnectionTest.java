import util.DBUtil;
import java.sql.Connection;

public class ConnectionTest {
    public static void main(String[] args) {
        try {
            Connection conn = DBUtil.getConnection();
            if (conn != null) {
                System.out.println("Success: Database connected correctly.");
                conn.close();
            }
        } catch (Throwable e) {
            System.err.println("Error: Failed to connect to database.");
            System.err.println("Message: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}
