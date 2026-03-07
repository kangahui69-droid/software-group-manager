package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UpdateSchema {
    public static void main(String[] args) {
        String sqlFile = "d:/Code/自动工具/test4_idea/Software_group_v2/update_award_tables.sql";
        System.out.println("Reading SQL file: " + sqlFile);

        List<String> sqlStatements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFile))) {
            String line;
            StringBuilder currentParams = new StringBuilder();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                currentParams.append(line).append(" ");
                if (line.endsWith(";")) {
                    sqlStatements.add(currentParams.toString());
                    currentParams.setLength(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Found " + sqlStatements.size() + " SQL statements.");

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement()) {

            for (String sql : sqlStatements) {
                String cleanSql = sql.trim();
                if (cleanSql.endsWith(";")) {
                    cleanSql = cleanSql.substring(0, cleanSql.length() - 1);
                }

                System.out.println(
                        "Executing: " + (cleanSql.length() > 50 ? cleanSql.substring(0, 50) + "..." : cleanSql));
                try {
                    stmt.executeUpdate(cleanSql);
                    System.out.println("Success.");
                } catch (Exception e) {
                    System.out.println("Failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
