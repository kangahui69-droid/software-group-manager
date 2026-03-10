import util.DESUtil;

public class password_tool {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("用法: java password_tool <密码>");
            System.out.println("示例: java password_tool admin123");
            return;
        }
        
        String password = args[0];
        String encrypted = DESUtil.encrypt(password);
        
        System.out.println("================================");
        System.out.println("原始密码: " + password);
        System.out.println("加密结果: " + encrypted);
        System.out.println("================================");
        System.out.println("\nSQL更新语句:");
        System.out.println("UPDATE user SET password = '" + encrypted + "' WHERE username = 'admin';");
    }
}
