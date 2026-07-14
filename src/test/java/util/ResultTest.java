package util;

import org.junit.jupiter.api.*;
import support.FastTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Result 统一响应模型单元测试
 *
 * 测试覆盖（RULES.md 规则5）：
 * - 所有静态工厂方法：ok()/ok(data)/error(msg)/error(code,msg)
 * - isSuccess() 布尔判断
 * - 错误码分段：401未登录、403无权限、400参数错误、4xxx业务错误、500系统错误
 * - 边界：null数据、空数据、大数据、嵌套对象
 * - 异常场景：非法错误码、toString序列化
 */
@DisplayName("Result 统一响应模型测试")
class ResultTest {

    // ==================== 静态工厂方法测试 ====================

    @FastTest
    @DisplayName("ok() 无参数应返回 code=0, message=ok, data=null")
    void should_return_success_result_with_no_data() {
        Result result = Result.ok();

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @FastTest
    @DisplayName("ok(data) 带数据应返回 code=0, message=ok, data=传入对象")
    void should_return_success_result_with_data() {
        String testData = "test string";
        Result result = Result.ok(testData);

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getData()).isEqualTo(testData);
        assertThat(result.isSuccess()).isTrue();
    }

    @FastTest
    @DisplayName("ok(data) 支持各种数据类型：String")
    void should_support_string_data() {
        Result result = Result.ok("hello");
        assertThat(result.getData()).isEqualTo("hello");
    }

    @FastTest
    @DisplayName("ok(data) 支持各种数据类型：Integer")
    void should_support_integer_data() {
        Result result = Result.ok(42);
        assertThat(result.getData()).isEqualTo(42);
    }

    @FastTest
    @DisplayName("ok(data) 支持各种数据类型：List")
    void should_support_list_data() {
        List<String> list = List.of("a", "b", "c");
        Result result = Result.ok(list);
        assertThat(result.getData()).isEqualTo(list);
    }

    @FastTest
    @DisplayName("ok(data) 支持各种数据类型：Map")
    void should_support_map_data() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        Result result = Result.ok(map);
        assertThat(result.getData()).isEqualTo(map);
    }

    @FastTest
    @DisplayName("ok(data) 支持各种数据类型：自定义对象")
    void should_support_custom_object_data() {
        TestDataObject obj = new TestDataObject(1, "test");
        Result result = Result.ok(obj);

        assertThat(result.getData()).isInstanceOf(TestDataObject.class);
        assertThat(((TestDataObject) result.getData()).getId()).isEqualTo(1);
        assertThat(((TestDataObject) result.getData()).getName()).isEqualTo("test");
    }

    @FastTest
    @DisplayName("ok(data) data为null时应返回null而非空容器")
    void should_handle_null_data_object() {
        Result result = Result.ok(null);
        assertThat(result.getData()).isNull();
    }

    @FastTest
    @DisplayName("error(msg) 应返回 code=500, message=传入消息, data=null")
    void should_return_error_result_with_message() {
        String errorMsg = "Something went wrong";
        Result result = Result.error(errorMsg);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo(errorMsg);
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("error(code, msg) 应返回 code=传入code, message=传入消息, data=null")
    void should_return_error_result_with_code_and_message() {
        Result result = Result.error(400, "Bad request");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("Bad request");
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isFalse();
    }

    // ==================== isSuccess() 测试 ====================

    @FastTest
    @DisplayName("isSuccess() code=0时应返回true")
    void should_return_true_when_code_is_zero() {
        Result result = Result.ok();
        assertThat(result.isSuccess()).isTrue();
    }

    @FastTest
    @DisplayName("isSuccess() code>0时应返回false")
    void should_return_false_when_code_is_positive() {
        Result result = Result.error(400, "error");
        assertThat(result.isSuccess()).isFalse();

        Result result500 = Result.error(500, "error");
        assertThat(result500.isSuccess()).isFalse();

        Result result9999 = Result.error(9999, "error");
        assertThat(result9999.isSuccess()).isFalse();
    }

    // ==================== 错误码分段测试 ====================

    @FastTest
    @DisplayName("错误码401未登录")
    void should_handle_unauthorized_error_code() {
        Result result = Result.error(401, "未登录");

        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMessage()).isEqualTo("未登录");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码403无权限")
    void should_handle_forbidden_error_code() {
        Result result = Result.error(403, "无权限访问");

        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMessage()).isEqualTo("无权限访问");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码400参数错误")
    void should_handle_bad_request_error_code() {
        Result result = Result.error(400, "参数格式错误");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("参数格式错误");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码4xxx业务错误")
    void should_handle_business_error_codes() {
        // 4000-4999 是业务错误码范围
        Result result4001 = Result.error(4001, "业务错误1");
        assertThat(result4001.getCode()).isEqualTo(4001);
        assertThat(result4001.isSuccess()).isFalse();

        Result result4500 = Result.error(4500, "业务错误2");
        assertThat(result4500.getCode()).isEqualTo(4500);
        assertThat(result4500.isSuccess()).isFalse();

        Result result4999 = Result.error(4999, "业务错误最大");
        assertThat(result4999.getCode()).isEqualTo(4999);
        assertThat(result4999.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码500系统错误")
    void should_handle_internal_server_error_code() {
        Result result = Result.error(500, "服务器内部错误");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("服务器内部错误");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码501-599范围测试")
    void should_handle_other_5xx_error_codes() {
        assertThat(Result.error(501, "Not Implemented").getCode()).isEqualTo(501);
        assertThat(Result.error(503, "Service Unavailable").getCode()).isEqualTo(503);
        assertThat(Result.error(599, "Custom Server Error").getCode()).isEqualTo(599);
    }

    // ==================== 边界情况测试 ====================

    @FastTest
    @DisplayName("空字符串消息应正常处理")
    void should_handle_empty_message() {
        Result result = Result.error("");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("null消息应正常处理")
    void should_handle_null_message() {
        Result result = Result.error(null);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("data为空List应正常返回")
    void should_handle_empty_list_data() {
        List<Object> emptyList = new ArrayList<>();
        Result result = Result.ok(emptyList);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(emptyList);
        assertThat(((List<?>) result.getData())).isEmpty();
    }

    @FastTest
    @DisplayName("data为空Map应正常返回")
    void should_handle_empty_map_data() {
        Map<String, Object> emptyMap = new HashMap<>();
        Result result = Result.ok(emptyMap);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(emptyMap);
        assertThat(((Map<?, ?>) result.getData())).isEmpty();
    }

    @FastTest
    @DisplayName("嵌套复杂对象应正常返回")
    void should_handle_nested_complex_object() {
        Map<String, Object> nestedData = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("name", "item1");
        item1.put("tags", List.of("tag1", "tag2"));
        items.add(item1);

        nestedData.put("items", items);
        nestedData.put("total", 1);
        nestedData.put("page", 1);

        Result result = Result.ok(nestedData);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(nestedData);
    }

    @FastTest
    @DisplayName("大数据对象应正常返回")
    void should_handle_large_data_object() {
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("x");
        }
        Result result = Result.ok(largeData.toString());

        assertThat(result.isSuccess()).isTrue();
        assertThat(((String) result.getData()).length()).isEqualTo(10000);
    }

    @FastTest
    @DisplayName("特殊字符消息应正常处理")
    void should_handle_special_characters_in_message() {
        Result result = Result.error("错误消息包含特殊字符：<>\"'& 和换行符\n换行");

        assertThat(result.getMessage()).contains("特殊字符");
        assertThat(result.getMessage()).contains("\n");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("Unicode字符消息应正常处理")
    void should_handle_unicode_characters_in_message() {
        Result result = Result.error("错误消息：中文测试русский текст日本語");

        assertThat(result.getMessage()).contains("中文测试");
        assertThat(result.getMessage()).contains("русский");
        assertThat(result.getMessage()).contains("日本語");
        assertThat(result.isSuccess()).isFalse();
    }

    @FastTest
    @DisplayName("错误码0应视为成功")
    void should_treat_code_zero_as_success() {
        Result result = Result.error(0, "Zero error code");
        assertThat(result.isSuccess()).isTrue();
    }

    @FastTest
    @DisplayName("错误码负数应正常处理")
    void should_handle_negative_error_code() {
        Result result = Result.error(-1, "Negative code");
        assertThat(result.getCode()).isEqualTo(-1);
        assertThat(result.isSuccess()).isFalse();
    }

    // ==================== toString 测试 ====================

    @FastTest
    @DisplayName("toString应包含code、message、data信息")
    void should_include_all_fields_in_toString() {
        Result result = Result.ok("test data");
        String str = result.toString();

        assertThat(str).contains("code=");
        assertThat(str).contains("message=");
        assertThat(str).contains("data=");
    }

    @FastTest
    @DisplayName("toString不应抛出异常")
    void should_not_throw_exception_in_toString() {
        Result result = Result.error(500, "error");
        assertThatCode(() -> result.toString()).doesNotThrowAnyException();
    }

    // ==================== 枚举值测试（如有） ====================

    @FastTest
    @DisplayName("常用错误码常量测试")
    void should_define_common_error_code_constants() {
        // 测试是否定义了常用错误码常量
        // 如果Result类中有 public static final int XXX = xxx; 的定义，解开注释测试

        // assertThat(Result.SUCCESS).isEqualTo(0);
        // assertThat(Result.BAD_REQUEST).isEqualTo(400);
        // assertThat(Result.UNAUTHORIZED).isEqualTo(401);
        // assertThat(Result.FORBIDDEN).isEqualTo(403);
        // assertThat(Result.INTERNAL_ERROR).isEqualTo(500);
    }

    // ==================== 辅助类 ====================

    public static class TestDataObject {
        private int id;
        private String name;

        public TestDataObject() {}

        public TestDataObject(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
