import util.DBUtil;
import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Checking 'user' table columns:");
            try (ResultSet rs = metaData.getColumns(null, null, "user", null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String typeName = rs.getString("TYPE_NAME");
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    System.out.println(columnName + ": " + typeName + "(" + columnSize + ")");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
