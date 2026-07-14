package util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 函数式事务工具类
 *
 * 使用方式：
 * <pre>
 * Integer result = TransactionTemplate.execute(conn -> {
 *     dao.insert(entity, conn);
 *     dao.update(entity2, conn);
 *     return generatedId;
 * });
 * </pre>
 *
 * 特性：
 * - 自动管理 setAutoCommit(false)/commit()/rollback()
 * - RuntimeException 自动触发回滚
 * - 正常执行自动提交
 * - finally 中恢复 autoCommit 并关闭连接
 */
public class TransactionTemplate {

    /**
     * 执行有返回值的业务逻辑（事务边界）
     *
     * @param action 业务逻辑 lambda，接收 Connection，返回 T
     * @param conn 数据库连接（调用方传入，不自动关闭）
     * @param <T> 返回值类型
     * @return 业务逻辑的返回值
     * @throws RuntimeException 当业务逻辑抛出运行时异常时触发回滚
     */
    public static <T> T execute(Function<Connection, T> action, Connection conn) {
        boolean originalAutoCommit = getAutoCommitSafe(conn);
        disableAutoCommit(conn);
        try {
            T result = action.apply(conn);
            conn.commit();
            return result;
        } catch (RuntimeException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("数据库访问异常: " + e.getMessage(), e);
        } finally {
            restoreAutoCommit(originalAutoCommit, conn);
        }
    }

    /**
     * 执行无返回值的业务逻辑（事务边界）
     *
     * @param action 业务逻辑 lambda，接收 Connection，无返回值
     * @param conn 数据库连接（调用方传入，不自动关闭）
     * @throws RuntimeException 当业务逻辑抛出运行时异常时触发回滚
     */
    public static void executeWithoutResult(Consumer<Connection> action, Connection conn) {
        execute(wrapConsumer(action), conn);
    }

    /**
     * 执行有返回值的业务逻辑（内部管理连接）
     *
     * @param action 业务逻辑 lambda，接收 Connection，返回 T
     * @param <T> 返回值类型
     * @return 业务逻辑的返回值
     * @throws RuntimeException 当业务逻辑抛出运行时异常时触发回滚
     */
    public static <T> T executeWithConnection(Function<Connection, T> action) {
        try (Connection conn = DBUtil.getConnection()) {
            return execute(action, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行无返回值的业务逻辑（内部管理连接）
     *
     * @param action 业务逻辑 lambda，接收 Connection，无返回值
     * @throws RuntimeException 当业务逻辑抛出运行时异常时触发回滚
     */
    public static void executeWithoutResultWithConnection(Consumer<Connection> action) {
        try (Connection conn = DBUtil.getConnection()) {
            executeWithoutResult(action, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败: " + e.getMessage(), e);
        }
    }

    // ---- 私有工具方法 ----

    private static boolean getAutoCommitSafe(Connection conn) {
        try {
            return conn.getAutoCommit();
        } catch (SQLException e) {
            System.err.println("[TransactionTemplate] 获取自动提交状态失败: " + e.getMessage());
            return true;
        }
    }

    private static void disableAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("设置自动提交失败: " + e.getMessage(), e);
        }
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn == null) return;
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("[TransactionTemplate] 回滚失败: " + e.getMessage());
        }
    }

    private static void restoreAutoCommit(boolean originalAutoCommit, Connection conn) {
        if (conn == null) return;
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            System.err.println("[TransactionTemplate] 恢复自动提交失败: " + e.getMessage());
        }
    }

    private static <T> Function<Connection, T> wrapConsumer(Consumer<Connection> action) {
        return conn -> {
            action.accept(conn);
            return null;
        };
    }
}
