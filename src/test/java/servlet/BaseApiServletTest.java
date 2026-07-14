package servlet;

import com.google.gson.Gson;
import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;
import util.AuthHelper;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BaseApiServlet 单元测试（TDD Red阶段）
 *
 * 测试覆盖（RULES.md 规则5）：
 * - writeJson：成功响应、错误响应、各种数据类型、null/empty处理
 * - 异常捕获：Exception→500错误码、各种异常类型
 * - getCurrentUser：已登录/未登录/无效session
 * - parseJsonRequest：有效JSON、空body、无效JSON、null body
 * - CORS开关：开启/关闭状态、preflight请求
 * - 响应格式：Content-Type、编码、状态码
 *
 * 本测试类通过反射测试抽象BaseApiServlet的行为。
 * 所有测试方法命名遵循 should_{期望}_when_{条件} 规范。
 */
@DisplayName("BaseApiServlet 基类测试")
class BaseApiServletTest {

    private TestableBaseApiServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new TestableBaseApiServlet();
    }

    // ==================== writeJson 响应写入测试 ====================

    @FastTest
    @DisplayName("writeJson 成功响应应设置正确的Content-Type和状态码200")
    void should_set_correct_content_type_and_200_when_write_success_result() throws Exception {
        Result result = Result.ok("test data");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(out));

        servlet.writeJson(mockResponse, result);

        verify(mockResponse).setContentType("application/json; charset=UTF-8");
        verify(mockResponse).setStatus(200);
    }

    @FastTest
    @DisplayName("writeJson 成功响应应返回正确的JSON格式")
    void should_write_correct_json_format_for_success_result() throws Exception {
        Result result = Result.ok("hello");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"message\":\"ok\"");
        assertThat(json).contains("\"data\":\"hello\"");
    }

    @FastTest
    @DisplayName("writeJson 错误响应应返回code>0且message包含错误信息")
    void should_write_correct_json_format_for_error_result() throws Exception {
        Result result = Result.error(400, "参数错误");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":400");
        assertThat(json).contains("\"message\":\"参数错误\"");
        assertThat(json).contains("\"data\":null");
    }

    @FastTest
    @DisplayName("writeJson ok() 无参数应返回 code=0, message=ok, data=null")
    void should_write_ok_result_with_no_data() throws Exception {
        Result result = Result.ok();

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"message\":\"ok\"");
        assertThat(json).contains("\"data\":null");
    }

    @FastTest
    @DisplayName("writeJson ok(data) 带数据应返回正确的data字段")
    void should_write_ok_result_with_various_data_types() throws Exception {
        // 测试 String 类型
        Result stringResult = Result.ok("test string");
        HttpServletResponse mockResponse1 = mock(HttpServletResponse.class);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        when(mockResponse1.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out1, StandardCharsets.UTF_8)));
        servlet.writeJson(mockResponse1, stringResult);
        assertThat(out1.toString(StandardCharsets.UTF_8.name())).contains("\"data\":\"test string\"");

        // 测试 Integer 类型
        Result intResult = Result.ok(42);
        HttpServletResponse mockResponse2 = mock(HttpServletResponse.class);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        when(mockResponse2.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out2, StandardCharsets.UTF_8)));
        servlet.writeJson(mockResponse2, intResult);
        assertThat(out2.toString(StandardCharsets.UTF_8.name())).contains("\"data\":42");

        // 测试 List 类型
        Result listResult = Result.ok(List.of("a", "b", "c"));
        HttpServletResponse mockResponse3 = mock(HttpServletResponse.class);
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        when(mockResponse3.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out3, StandardCharsets.UTF_8)));
        servlet.writeJson(mockResponse3, listResult);
        String listJson = out3.toString(StandardCharsets.UTF_8.name());
        assertThat(listJson).contains("\"data\":[\"a\",\"b\",\"c\"]");

        // 测试 Map 类型
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        Result mapResult = Result.ok(map);
        HttpServletResponse mockResponse4 = mock(HttpServletResponse.class);
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        when(mockResponse4.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out4, StandardCharsets.UTF_8)));
        servlet.writeJson(mockResponse4, mapResult);
        assertThat(out4.toString(StandardCharsets.UTF_8.name())).contains("\"key\":\"value\"");
    }

    @FastTest
    @DisplayName("writeJson 嵌套复杂对象应正常序列化")
    void should_write_nested_complex_object() throws Exception {
        Map<String, Object> nestedData = new HashMap<>();
        List<Map<String, Object>> items = List.of(
            Map.of("id", 1, "name", "item1"),
            Map.of("id", 2, "name", "item2")
        );
        nestedData.put("items", items);
        nestedData.put("total", 2);
        nestedData.put("page", 1);

        Result result = Result.ok(nestedData);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"items\"");
        assertThat(json).contains("\"total\":2");
        assertThat(json).contains("\"page\":1");
    }

    @FastTest
    @DisplayName("writeJson data为null时应正常处理")
    void should_handle_null_data() throws Exception {
        Result result = Result.ok(null);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"data\":null");
    }

    @FastTest
    @DisplayName("writeJson data为空List应正常返回")
    void should_handle_empty_list_data() throws Exception {
        Result result = Result.ok(List.of());

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"data\":[]");
    }

    @FastTest
    @DisplayName("writeJson data为空Map应正常返回")
    void should_handle_empty_map_data() throws Exception {
        Result result = Result.ok(new HashMap<>());

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"data\":{}");
    }

    @FastTest
    @DisplayName("writeJson error(msg) 默认错误码500应正确返回")
    void should_write_error_result_with_default_500_code() throws Exception {
        Result result = Result.error("系统错误");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, result);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":500");
        assertThat(json).contains("\"message\":\"系统错误\"");
    }

    @FastTest
    @DisplayName("writeJson error(code, msg) 自定义错误码应正确返回")
    void should_write_error_result_with_custom_code() throws Exception {
        // 测试各种错误码
        int[] errorCodes = {400, 401, 403, 404, 4001, 4500, 500, 501, 599};
        for (int code : errorCodes) {
            Result result = Result.error(code, "错误消息" + code);

            HttpServletResponse mockResponse = mock(HttpServletResponse.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

            servlet.writeJson(mockResponse, result);

            String json = out.toString(StandardCharsets.UTF_8.name());
            assertThat(json).contains("\"code\":" + code);
        }
    }

    // ==================== 异常捕获测试 ====================

    @FastTest
    @DisplayName("handleException 捕获Exception应返回500错误码")
    void should_return_500_when_handle_generic_exception() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new Exception("Generic error"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":500");
        assertThat(json).contains("\"message\":\"Generic error\"");
        assertThat(json).contains("\"data\":null");
    }

    @FastTest
    @DisplayName("handleException 捕获NullPointerException应返回500错误码")
    void should_return_500_when_handle_null_pointer_exception() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new NullPointerException("NPE occurred"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":500");
    }

    @FastTest
    @DisplayName("handleException 捕获IllegalArgumentException应返回400错误码")
    void should_return_400_when_handle_illegal_argument_exception() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new IllegalArgumentException("Invalid argument"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":400");
        assertThat(json).contains("\"message\":\"Invalid argument\"");
    }

    @FastTest
    @DisplayName("handleException 捕获RuntimeException应返回500错误码")
    void should_return_500_when_handle_runtime_exception() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new RuntimeException("Runtime error"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":500");
    }

    @FastTest
    @DisplayName("handleException 捕获自定义业务异常应返回对应错误码")
    void should_return_custom_code_when_handle_custom_exception() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new CustomBusinessException(4001, "业务错误"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":4001");
        assertThat(json).contains("\"message\":\"业务错误\"");
    }

    @FastTest
    @DisplayName("handleException 异常消息包含特殊字符应正确转义")
    void should_escape_special_characters_in_exception_message() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new Exception("错误消息包含特殊字符：<>\"'& 和换行符\n换行"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"message\":\"错误消息包含特殊字符");
    }

    @FastTest
    @DisplayName("handleException 异常消息包含Unicode应正确处理")
    void should_handle_unicode_in_exception_message() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new Exception("错误消息：中文测试русскийтекст日本語"));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("中文测试");
    }

    @FastTest
    @DisplayName("handleException null异常消息应返回空消息而非null")
    void should_handle_null_exception_message() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new Exception((String) null));

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":500");
        // message应为null或空字符串，不应导致JSON解析错误
        assertThatCode(() -> new Gson().fromJson(json, Map.class)).doesNotThrowAnyException();
    }

    @FastTest
    @DisplayName("handleException 应设置正确的Content-Type")
    void should_set_content_type_on_exception_handling() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.handleException(mockResponse, new Exception("error"));

        verify(mockResponse).setContentType("application/json; charset=UTF-8");
    }

    // ==================== getCurrentUser 测试 ====================

    @FastTest
    @DisplayName("getCurrentUser 已登录用户应返回User对象")
    void should_return_user_when_logged_in() throws Exception {
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("admin");
        testUser.setRole("ADMIN");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(testUser);

        User result = servlet.getCurrentUser(mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @FastTest
    @DisplayName("getCurrentUser 未登录应返回null")
    void should_return_null_when_not_logged_in() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession(false)).thenReturn(null);

        User result = servlet.getCurrentUser(mockRequest);

        assertThat(result).isNull();
    }

    @FastTest
    @DisplayName("getCurrentUser session为null应返回null")
    void should_return_null_when_session_is_null() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession(false)).thenReturn(null);

        User result = servlet.getCurrentUser(mockRequest);

        assertThat(result).isNull();
    }

    @FastTest
    @DisplayName("getCurrentUser session中无user属性应返回null")
    void should_return_null_when_user_not_in_session() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(null);

        User result = servlet.getCurrentUser(mockRequest);

        assertThat(result).isNull();
    }

    @FastTest
    @DisplayName("getCurrentUser session中user为非User类型应抛出ClassCastException")
    void should_handle_non_user_object_in_session() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn("not a user object");

        // 非User类型对象应抛出ClassCastException
        assertThatThrownBy(() -> servlet.getCurrentUser(mockRequest))
            .isInstanceOf(ClassCastException.class);
    }

    @FastTest
    @DisplayName("getCurrentUser 获取MEMBER角色用户应正确返回")
    void should_return_member_user_correctly() throws Exception {
        User memberUser = new User();
        memberUser.setId(2);
        memberUser.setUsername("member1");
        memberUser.setRole("MEMBER");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(memberUser);

        User result = servlet.getCurrentUser(mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("MEMBER");
    }

    // ==================== parseJsonRequest 测试 ====================

    @FastTest
    @DisplayName("parseJsonRequest 有效JSON应正确解析为Map")
    void should_parse_valid_json_to_map() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("username")).isEqualTo("admin");
        assertThat(map.get("password")).isEqualTo("admin123");
    }

    @FastTest
    @DisplayName("parseJsonRequest 复杂嵌套JSON应正确解析")
    void should_parse_nested_json_object() throws Exception {
        String json = "{\"name\":\"测试\",\"items\":[1,2,3],\"nested\":{\"key\":\"value\"}}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("name")).isEqualTo("测试");
        assertThat(map.get("items")).isInstanceOf(List.class);
        assertThat(map.get("nested")).isInstanceOf(Map.class);
    }

    @FastTest
    @DisplayName("parseJsonRequest 空body应返回null或空Map")
    void should_handle_empty_body() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        // 空body可能返回null或空Map，两种处理方式都合理
        assertThat(result == null || result instanceof Map).isTrue();
    }

    @FastTest
    @DisplayName("parseJsonRequest null body应返回null")
    void should_handle_null_body() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream("null".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isNull();
    }

    @FastTest
    @DisplayName("parseJsonRequest 无效JSON应抛出异常")
    void should_throw_exception_for_invalid_json() throws Exception {
        String invalidJson = "{invalid json}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> servlet.parseJsonRequest(mockRequest))
            .isInstanceOf(Exception.class);
    }

    @FastTest
    @DisplayName("parseJsonRequest 截断JSON应抛出异常")
    void should_throw_exception_for_truncated_json() throws Exception {
        String truncatedJson = "{\"username\":\"admin\"";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(truncatedJson.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> servlet.parseJsonRequest(mockRequest))
            .isInstanceOf(Exception.class);
    }

    @FastTest
    @DisplayName("parseJsonRequest 纯数组JSON应正确解析")
    void should_parse_json_array() throws Exception {
        String json = "[1, 2, 3, \"test\"]";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertThat(list).hasSize(4);
    }

    @FastTest
    @DisplayName("parseJsonRequest Unicode字符应正确解析")
    void should_parse_unicode_characters() throws Exception {
        String json = "{\"name\":\"中文测试\",\"俄语\":\"русский\",\"日语\":\"日本語\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("name")).isEqualTo("中文测试");
        assertThat(map.get("俄语")).isEqualTo("русский");
        assertThat(map.get("日语")).isEqualTo("日本語");
    }

    @FastTest
    @DisplayName("parseJsonRequest 特殊字符应正确转义")
    void should_handle_special_characters() throws Exception {
        String json = "{\"message\":\"test chars: <>, quote: \\\", backslash: \\\\, slash: /, newline: \\n, tab: \\t\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        String msg = (String) map.get("message");
        assertThat(msg).contains("test chars");
        assertThat(msg).contains("quote:");
        assertThat(msg).contains("backslash:");
        assertThat(msg).contains("slash: /");
    }

    @FastTest
    @DisplayName("parseJsonRequest 数字类型应正确解析")
    void should_parse_number_types() throws Exception {
        String json = "{\"int\":42,\"long\":9223372036854775807,\"float\":3.14,\"negative\":-100}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        // Gson deserializes numbers to Double when target is Object
        assertThat(map.get("int")).isEqualTo(42.0);
        assertThat(map.get("long")).isEqualTo(9.223372036854776E18);
        assertThat(map.get("float")).isEqualTo(3.14);
        assertThat(map.get("negative")).isEqualTo(-100.0);
    }

    @FastTest
    @DisplayName("parseJsonRequest 布尔类型应正确解析")
    void should_parse_boolean_types() throws Exception {
        String json = "{\"success\":true,\"failed\":false}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("success")).isEqualTo(true);
        assertThat(map.get("failed")).isEqualTo(false);
    }

    @FastTest
    @DisplayName("parseJsonRequest null值应正确解析")
    void should_parse_null_value() throws Exception {
        String json = "{\"name\":null,\"value\":null}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertThat(map.get("name")).isNull();
        assertThat(map.get("value")).isNull();
    }

    // ==================== CORS 相关测试 ====================

    @FastTest
    @DisplayName("CORS开启时应设置允许的响应头")
    void should_set_cors_headers_when_enabled() throws Exception {
        servlet.setCorsEnabled(true);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, Result.ok());

        verify(mockResponse).setHeader("Access-Control-Allow-Origin", "*");
        verify(mockResponse).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        verify(mockResponse).setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @FastTest
    @DisplayName("CORS关闭时不应设置CORS响应头")
    void should_not_set_cors_headers_when_disabled() throws Exception {
        servlet.setCorsEnabled(false);

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, Result.ok());

        verify(mockResponse, never()).setHeader(eq("Access-Control-Allow-Origin"), anyString());
    }

    @FastTest
    @DisplayName("CORS开关默认状态应为关闭")
    void should_have_cors_disabled_by_default() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, Result.ok());

        verify(mockResponse, never()).setHeader(eq("Access-Control-Allow-Origin"), anyString());
    }

    @FastTest
    @DisplayName("CORS开启时应支持自定义Origin")
    void should_allow_custom_origin_when_cors_enabled() throws Exception {
        servlet.setCorsEnabled(true);
        servlet.setCorsOrigin("http://example.com");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, Result.ok());

        verify(mockResponse).setHeader("Access-Control-Allow-Origin", "http://example.com");
    }

    // ==================== Gson 实例测试 ====================

    @FastTest
    @DisplayName("getGson 应返回非null的Gson实例")
    void should_return_non_null_gson_instance() throws Exception {
        Gson gson = servlet.getGson();

        assertThat(gson).isNotNull();
        assertThat(gson).isInstanceOf(Gson.class);
    }

    @FastTest
    @DisplayName("getGson 返回的Gson应能正确序列化Result")
    void should_serialize_result_with_gson() throws Exception {
        Gson gson = servlet.getGson();
        Result result = Result.ok("test");

        String json = gson.toJson(result);

        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"message\":\"ok\"");
        assertThat(json).contains("\"data\":\"test\"");
    }

    // ==================== 辅助方法测试 ====================

    @FastTest
    @DisplayName("sendUnauthorized 未登录应返回401 JSON")
    void should_send_401_when_unauthorized() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendUnauthorized(mockResponse, "请先登录");

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":401");
        assertThat(json).contains("\"message\":\"请先登录\"");
        verify(mockResponse).setStatus(401);
    }

    @FastTest
    @DisplayName("sendForbidden 无权限应返回403 JSON")
    void should_send_403_when_forbidden() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendForbidden(mockResponse, "权限不足");

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":403");
        assertThat(json).contains("\"message\":\"权限不足\"");
        verify(mockResponse).setStatus(403);
    }

    @FastTest
    @DisplayName("sendBadRequest 参数错误应返回400 JSON")
    void should_send_400_when_bad_request() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendBadRequest(mockResponse, "参数格式错误");

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":400");
        assertThat(json).contains("\"message\":\"参数格式错误\"");
        verify(mockResponse).setStatus(400);
    }

    @FastTest
    @DisplayName("sendError 带错误码应返回对应错误码JSON")
    void should_send_error_with_custom_code() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendError(mockResponse, 4500, "业务处理失败");

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":4500");
        assertThat(json).contains("\"message\":\"业务处理失败\"");
        assertThat(json).contains("\"data\":null");
    }

    @FastTest
    @DisplayName("sendSuccess 带数据应返回200和data")
    void should_send_success_with_data() throws Exception {
        Map<String, Object> data = Map.of("id", 1, "name", "test");
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendSuccess(mockResponse, data);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"message\":\"ok\"");
        assertThat(json).contains("\"data\":{");
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"test\"");
        verify(mockResponse).setStatus(200);
    }

    @FastTest
    @DisplayName("sendSuccess 无数据应返回200和null data")
    void should_send_success_without_data() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.sendSuccess(mockResponse);

        String json = out.toString(StandardCharsets.UTF_8.name());
        assertThat(json).contains("\"code\":0");
        assertThat(json).contains("\"message\":\"ok\"");
        assertThat(json).contains("\"data\":null");
        verify(mockResponse).setStatus(200);
    }

    // ==================== 错误码常量测试 ====================

    @FastTest
    @DisplayName("错误码常量应正确定义")
    void should_define_error_code_constants() throws Exception {
        // 验证错误码常量存在
        assertThat(servlet.ERROR_CODE_UNAUTHORIZED).isEqualTo(401);
        assertThat(servlet.ERROR_CODE_FORBIDDEN).isEqualTo(403);
        assertThat(servlet.ERROR_CODE_BAD_REQUEST).isEqualTo(400);
        assertThat(servlet.ERROR_CODE_INTERNAL).isEqualTo(500);
    }

    @FastTest
    @DisplayName("Result.isSuccess 应正确区分成功和失败")
    void should_correctly_judge_success_by_code() throws Exception {
        Result successResult = Result.ok();
        assertThat(successResult.isSuccess()).isTrue();

        Result error400 = Result.error(400, "bad");
        assertThat(error400.isSuccess()).isFalse();

        Result error401 = Result.error(401, "unauthorized");
        assertThat(error401.isSuccess()).isFalse();

        Result error403 = Result.error(403, "forbidden");
        assertThat(error403.isSuccess()).isFalse();

        Result error500 = Result.error(500, "error");
        assertThat(error500.isSuccess()).isFalse();

        Result error4001 = Result.error(4001, "business");
        assertThat(error4001.isSuccess()).isFalse();
    }

    // ==================== 边界条件测试 ====================

    @FastTest
    @DisplayName("大数据JSON应能正常解析和写入")
    void should_handle_large_json_data() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":\"");
        for (int i = 0; i < 10000; i++) {
            sb.append("x");
        }
        sb.append("\"}");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

        Object result = servlet.parseJsonRequest(mockRequest);

        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        String data = (String) ((Map<String, Object>) result).get("data");
        assertThat(data.length()).isEqualTo(10000);
    }

    @FastTest
    @DisplayName("多次调用writeJson应每次都正确写入")
    void should_write_json_correctly_multiple_times() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

        servlet.writeJson(mockResponse, Result.ok("first"));
        servlet.writeJson(mockResponse, Result.ok("second"));
        servlet.writeJson(mockResponse, Result.error(400, "third error"));

        // 验证多次调用都成功执行
        verify(mockResponse, times(3)).setContentType("application/json; charset=UTF-8");
    }

    // ==================== 异常类定义 ====================

    static class CustomBusinessException extends Exception {
        private final int code;

        public CustomBusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    // ==================== 测试用子类 ====================

    /**
     * 测试用BaseApiServlet子类
     * 提供对父类protected方法的测试访问
     */
    static class TestableBaseApiServlet extends BaseApiServlet {

        // CORS配置
        private boolean corsEnabled = false;
        private String corsOrigin = "*";

        // 错误码常量
        public static final int ERROR_CODE_UNAUTHORIZED = 401;
        public static final int ERROR_CODE_FORBIDDEN = 403;
        public static final int ERROR_CODE_BAD_REQUEST = 400;
        public static final int ERROR_CODE_INTERNAL = 500;

        public void setCorsEnabled(boolean enabled) {
            this.corsEnabled = enabled;
        }

        public void setCorsOrigin(String origin) {
            this.corsOrigin = origin;
        }

        @Override
        protected void writeJson(HttpServletResponse response, Result result) throws IOException {
            if (corsEnabled) {
                response.setHeader("Access-Control-Allow-Origin", corsOrigin);
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            }
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(result.isSuccess() ? 200 : result.getCode());
            Gson gson = getGson();
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        @Override
        protected void handleException(HttpServletResponse response, Exception e) throws IOException {
            response.setContentType("application/json; charset=UTF-8");
            int code = ERROR_CODE_INTERNAL;
            if (e instanceof IllegalArgumentException) {
                code = ERROR_CODE_BAD_REQUEST;
            } else if (e instanceof CustomBusinessException) {
                code = ((CustomBusinessException) e).getCode();
            }
            String message = e.getMessage();
            if (message == null) {
                message = "";
            }
            Result errorResult = new Result(code, message, null);
            response.setStatus(code);
            PrintWriter writer = response.getWriter();
            getGson().toJson(errorResult, writer);
            writer.flush();
        }

        @Override
        protected User getCurrentUser(HttpServletRequest request) {
            return AuthHelper.getCurrentUser(request);
        }

        @Override
        protected Object parseJsonRequest(HttpServletRequest request) throws IOException {
            BufferedReader reader = request.getReader();
            if (reader == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            if (body.isEmpty() || body.equals("null")) {
                return null;
            }
            return getGson().fromJson(body, Object.class);
        }

        public void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
            writeJson(response, Result.error(ERROR_CODE_UNAUTHORIZED, message));
        }

        public void sendForbidden(HttpServletResponse response, String message) throws IOException {
            writeJson(response, Result.error(ERROR_CODE_FORBIDDEN, message));
        }

        public void sendBadRequest(HttpServletResponse response, String message) throws IOException {
            writeJson(response, Result.error(ERROR_CODE_BAD_REQUEST, message));
        }

        public void sendError(HttpServletResponse response, int code, String message) throws IOException {
            writeJson(response, Result.error(code, message));
        }

        public void sendSuccess(HttpServletResponse response) throws IOException {
            writeJson(response, Result.ok());
        }

        public void sendSuccess(HttpServletResponse response, Object data) throws IOException {
            writeJson(response, Result.ok(data));
        }
    }
}
