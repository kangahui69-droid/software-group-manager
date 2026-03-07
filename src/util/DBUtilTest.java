package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBUtilTest {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            System.out.println("Database connection established successfully");
            
            // 创建Statement
            stmt = conn.createStatement();
            
            // 查询user表的结构
            String sql = "DESCRIBE user";
            rs = stmt.executeQuery(sql);
            
            System.out.println("User table structure:");
            System.out.println("Field\tType\tNull\tKey\tDefault\tExtra");
            System.out.println("----------------------------------------------------------------");
            
            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                String nullable = rs.getString("Null");
                String key = rs.getString("Key");
                String defaultValue = rs.getString("Default");
                String extra = rs.getString("Extra");
                
                System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\n", 
                    field, type, nullable, key, defaultValue, extra);
            }
            
            // 查询user表的索引
            sql = "SHOW INDEX FROM user";
            rs = stmt.executeQuery(sql);
            
            System.out.println("\nUser table indexes:");
            System.out.println("Table\tNon_unique\tKey_name\tSeq_in_index\tColumn_name\tCollation\tCardinality\tSub_part\tPacked\tNull\tIndex_type\tComment\tIndex_comment");
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            
            while (rs.next()) {
                String table = rs.getString("Table");
                int nonUnique = rs.getInt("Non_unique");
                String keyName = rs.getString("Key_name");
                int seqInIndex = rs.getInt("Seq_in_index");
                String columnName = rs.getString("Column_name");
                String collation = rs.getString("Collation");
                int cardinality = rs.getInt("Cardinality");
                String subPart = rs.getString("Sub_part");
                String packed = rs.getString("Packed");
                String nullVal = rs.getString("Null");
                String indexType = rs.getString("Index_type");
                String comment = rs.getString("Comment");
                String indexComment = rs.getString("Index_comment");
                
                System.out.printf("%s\t%d\t%s\t%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\t%s\n", 
                    table, nonUnique, keyName, seqInIndex, columnName, collation, cardinality, subPart, packed, nullVal, indexType, comment, indexComment);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}