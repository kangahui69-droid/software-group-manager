import util.DESUtil;

/**
 * 密码工具类 - 用于验证和重置密码
 */
public class PasswordTool {
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("        密码加密工具");
        System.out.println("==============================================\n");

        // 1. 解密数据库中现有的admin密码
        String dbAdminPassword = "sEVTxpZ/OQykR2QyQb9BWw==";
        System.out.println("1. 数据库中admin账号的密码分析:");
        System.out.println("   加密值: " + dbAdminPassword);
        String decryptedAdmin = DESUtil.decrypt(dbAdminPassword);
        System.out.println("   解密结果: " + (decryptedAdmin != null ? decryptedAdmin : "解密失败！"));
        System.out.println();

        // 2. 常见密码测试
        System.out.println("2. 常见管理员密码测试:");
        String[] commonPasswords = {"admin", "admin123", "123456", "password", "root"};
        for (String pwd : commonPasswords) {
            String encrypted = DESUtil.encrypt(pwd);
            System.out.println("   " + pwd + " => " + encrypted);
        }
        System.out.println();

        // 3. 重置密码推荐
        System.out.println("3. 推荐的重置密码方案:");
        String resetPassword = "admin123";
        String resetEncrypted = DESUtil.encrypt(resetPassword);
        System.out.println("   新密码: " + resetPassword);
        System.out.println("   加密值: " + resetEncrypted);
        System.out.println();
        System.out.println("   SQL更新语句:");
        System.out.println("   UPDATE user SET password = '" + resetEncrypted + "' WHERE username = 'admin';");
        System.out.println();

        System.out.println("==============================================");
        System.out.println("提示: 如果数据库中的密码无法解密，可能是密钥已更改！");
        System.out.println("==============================================");
    }
}
