import util.DBUtil;

public class DriverTest {
    public static void main(String[] args) {
        try {
            // Force initialization of DBUtil which loads the driver
            Class.forName("util.DBUtil");
            System.out.println("Success: Database driver loaded correctly.");
        } catch (Throwable e) {
            System.err.println("Error: Failed to load driver.");
            e.printStackTrace();
        }
    }
}
