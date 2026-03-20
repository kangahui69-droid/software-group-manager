package util;

import java.util.regex.Pattern;

/**
 * XSS 过滤工具类
 * 提供 HTML、JavaScript、CSS 等内容的过滤功能
 */
public class XSSFilterUtil {

    // 禁止的事件处理器
    private static final Pattern[] EVENT_PATTERNS = new Pattern[]{
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
    };

    // 禁止的脚本标签
    private static final Pattern[] SCRIPT_PATTERNS = new Pattern[]{
        Pattern.compile("<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<script[^>]*?/>", Pattern.CASE_INSENSITIVE),
    };

    // 禁止的危险标签
    private static final Pattern[] DANGEROUS_TAG_PATTERNS = new Pattern[]{
        Pattern.compile("<iframe[^>]*?>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<iframe[^>]*?/>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<object[^>]*?>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<embed[^>]*?/>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<form[^>]*?>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</form>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<input[^>]*?/>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<textarea[^>]*?>.*?</textarea>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
    };

    // 危险的 URL 协议
    private static final Pattern[] DANGEROUS_PROTOCOL_PATTERNS = new Pattern[]{
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("mhtml:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("file:", Pattern.CASE_INSENSITIVE),
    };

    // CSS 表达式（IE 特有）
    private static final Pattern[] CSS_EXPRESSION_PATTERNS = new Pattern[]{
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("moz-binding", Pattern.CASE_INSENSITIVE),
    };

    // HTML 实体编码的危险内容
    private static final Pattern[] ENCODED_DANGER_PATTERNS = new Pattern[]{
        Pattern.compile("&#\\d+;"),
        Pattern.compile("&#[xX][0-9a-fA-F]+;"),
    };

    /**
     * 私有构造方法，防止实例化
     */
    private XSSFilterUtil() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }

    /**
     * 过滤字符串中的 XSS 内容
     *
     * @param input 输入字符串
     * @return 过滤后的安全字符串
     */
    public static String filter(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String output = input;

        // 1. 过滤 HTML 实体编码的危险内容
        for (Pattern pattern : ENCODED_DANGER_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 2. 过滤脚本标签
        for (Pattern pattern : SCRIPT_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 3. 过滤危险标签
        for (Pattern pattern : DANGEROUS_TAG_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 4. 过滤事件处理器
        for (Pattern pattern : EVENT_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 5. 过滤危险协议
        for (Pattern pattern : DANGEROUS_PROTOCOL_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 6. 过滤 CSS 表达式
        for (Pattern pattern : CSS_EXPRESSION_PATTERNS) {
            output = pattern.matcher(output).replaceAll("");
        }

        // 7. 移除其他危险字符和标签
        output = output.replaceAll("(?i)<!\\[CDATA\\[", "");
        output = output.replaceAll("(?i)\\]\\]>", "");
        output = output.replaceAll("(?i)<!--", "");
        output = output.replaceAll("(?i)-->", "");

        return output;
    }

    /**
     * 过滤 HTML 内容，保留安全的 HTML 标签
     *
     * @param input 输入字符串
     * @return 过滤后的安全 HTML
     */
    public static String filterHtml(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        // 先进行基本过滤
        String output = filter(input);

        // 允许的安全标签白名单（可以根据需要调整）
        // 这里使用正则表达式恢复允许的标签
        // 注意：这个方法比较简单，如果需要更复杂的 HTML 过滤，建议使用 JSoup 等库

        return output;
    }

    /**
     * 检查是否包含 XSS 危险内容
     *
     * @param input 输入字符串
     * @return 如果包含危险内容返回 true
     */
    public static boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String filtered = filter(input);
        return !input.equals(filtered);
    }

    /**
     * 清理 URL 参数
     *
     * @param url URL 字符串
     * @return 清理后的 URL
     */
    public static String sanitizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }

        String lowerUrl = url.toLowerCase().trim();

        // 检查危险协议
        for (Pattern pattern : DANGEROUS_PROTOCOL_PATTERNS) {
            if (pattern.matcher(lowerUrl).find()) {
                return "#"; // 返回空链接
            }
        }

        return url;
    }
}