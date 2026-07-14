package util;

/**
 * 统一响应模型
 *
 * 使用方式：
 * <pre>
 * // 成功响应
 * return Result.ok(data);
 * return Result.ok();
 *
 * // 错误响应
 * return Result.error("错误信息");
 * return Result.error(400, "参数错误");
 * </pre>
 */
public class Result {

    private static final int DEFAULT_SUCCESS_CODE = 0;
    private static final int DEFAULT_ERROR_CODE = 500;

    private int code;
    private String message;
    private Object data;

    public Result() {
    }

    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应（无数据）
     */
    public static Result ok() {
        return ok(null);
    }

    /**
     * 创建成功响应（带数据）
     */
    public static Result ok(Object data) {
        return new Result(DEFAULT_SUCCESS_CODE, "ok", data);
    }

    /**
     * 创建错误响应（默认500错误码）
     */
    public static Result error(String message) {
        return error(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 创建错误响应（指定错误码）
     */
    public static Result error(int code, String message) {
        return new Result(code, message, null);
    }

    /**
     * 判断是否为成功响应
     */
    public boolean isSuccess() {
        return code == 0;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", message='" + message + "', data=" + data + "}";
    }
}
