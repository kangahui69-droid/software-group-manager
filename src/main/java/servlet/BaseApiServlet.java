package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.User;
import util.AuthHelper;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * API Servlet 基类
 *
 * 所有 /api/* 端点的抽象基类，提供：
 * - 统一的JSON响应writeJson方法
 * - 统一的异常捕获handleException方法
 * - 获取当前登录用户getCurrentUser方法
 * - JSON请求体解析parseJsonRequest方法
 * - Gson实例getGson方法
 *
 * 使用方式：
 * <pre>
 * public class ActivityApiServlet extends BaseApiServlet {
 *     {@literal @}Override
 *     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 *         User currentUser = getCurrentUser(req);
 *         if (currentUser == null) {
 *             sendUnauthorized(resp, "请先登录");
 *             return;
 *         }
 *         // 业务处理...
 *         writeJson(resp, Result.ok(data));
 *     }
 * }
 * </pre>
 */
public abstract class BaseApiServlet extends HttpServlet {

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final int DEFAULT_ERROR_CODE = 500;
    private static final int BAD_REQUEST_ERROR_CODE = 400;
    private static final int HTTP_OK = 200;

    private transient Gson gson;

    // ==================== Gson实例 ====================

    /**
     * 获取Gson实例（懒加载，线程安全）
     * 配置serializeNulls以确保Result中data为null时仍会序列化
     */
    protected Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().serializeNulls().create();
        }
        return gson;
    }

    // ==================== JSON响应写入 ====================

    /**
     * 统一响应写入方法
     * 设置Content-Type为application/json，根据Result的isSuccess()设置状态码
     *
     * @param response HttpServletResponse
     * @param result 统一响应模型
     * @throws IOException 写入异常
     */
    protected void writeJson(HttpServletResponse response, Result result) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setStatus(resolveHttpStatus(result));
        serializeAndFlush(response, result);
    }

    /**
     * 统一异常捕获处理
     *
     * @param response HttpServletResponse
     * @param e 异常
     * @throws IOException 写入异常
     */
    protected void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        int errorCode = resolveErrorCode(e);
        String message = e.getMessage() != null ? e.getMessage() : "";
        response.setStatus(errorCode);
        serializeAndFlush(response, Result.error(errorCode, message));
    }

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest
     * @return 登录用户，未登录返回null
     */
    protected User getCurrentUser(HttpServletRequest request) {
        return AuthHelper.getCurrentUser(request);
    }

    /**
     * 解析JSON请求体
     *
     * @param request HttpServletRequest
     * @return 解析后的对象（Map/List/基本类型），解析失败返回null
     * @throws IOException 读取请求体异常
     */
    protected Object parseJsonRequest(HttpServletRequest request) throws IOException {
        String body = readRequestBody(request);
        if (body.isEmpty() || "null".equals(body)) {
            return null;
        }
        return getGson().fromJson(body, Object.class);
    }

    // ==================== 便捷响应方法 ====================

    /**
     * 发送未授权响应（401）
     */
    protected void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(401, message));
    }

    /**
     * 发送禁止响应（403）
     */
    protected void sendForbidden(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(403, message));
    }

    /**
     * 发送参数错误响应（400）
     */
    protected void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(400, message));
    }

    /**
     * 发送业务错误响应
     */
    protected void sendError(HttpServletResponse response, int code, String message) throws IOException {
        writeJson(response, Result.error(code, message));
    }

    /**
     * 发送成功响应（无数据）
     */
    protected void sendSuccess(HttpServletResponse response) throws IOException {
        writeJson(response, Result.ok());
    }

    /**
     * 发送成功响应（带数据）
     */
    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        writeJson(response, Result.ok(data));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据Result解析HTTP状态码
     * 成功返回200，失败返回Result中的错误码
     */
    private int resolveHttpStatus(Result result) {
        return result.isSuccess() ? HTTP_OK : result.getCode();
    }

    /**
     * 根据异常类型解析错误码
     * - IllegalArgumentException → 400
     * - 其他异常 → 500
     */
    private int resolveErrorCode(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return BAD_REQUEST_ERROR_CODE;
        }
        return DEFAULT_ERROR_CODE;
    }

    /**
     * 将Result序列化为JSON并写入响应，最后flush
     */
    private void serializeAndFlush(HttpServletResponse response, Result result) throws IOException {
        PrintWriter writer = response.getWriter();
        getGson().toJson(result, writer);
        writer.flush();
    }

    /**
     * 读取请求体内容
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
