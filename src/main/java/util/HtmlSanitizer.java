package util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * HTML 净化工具类
 * 用于过滤富文本内容，防止XSS攻击
 */
public class HtmlSanitizer {

    /**
     * 富文本白名单 - 允许的HTML标签和属性
     */
    private static final Safelist RICH_TEXT_WHITELIST = Safelist.relaxed()
            // 允许的标签已通过 relaxed() 包含，以下是额外配置
            // 允许的图片来源
            .addAttributes(":all", "src", "alt", "title", "width", "height")
            // 允许的链接
            .addAttributes("a", "href", "title", "target")
            // 允许的样式
            .addAttributes(":all", "style", "class")
            // 移除危险的协议
            .removeProtocols("img", "src", "javascript", "vbscript", "data")
            .removeProtocols("a", "href", "javascript", "vbscript", "data");

    /**
     * 基础白名单 - 仅允许安全的文本格式标签
     */
    private static final Safelist BASIC_WHITELIST = Safelist.none()
            .addTags("p", "br", "strong", "em", "b", "i", "u", "span")
            .addAttributes("span", "style");

    /**
     * 私有构造方法，防止实例化
     */
    private HtmlSanitizer() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }

    /**
     * 净化富文本内容（用于新闻正文等）
     * 允许大多数常用HTML标签，但移除危险内容
     *
     * @param dirtyHtml 原始HTML内容
     * @return 净化后的安全HTML
     */
    public static String sanitizeRichText(String dirtyHtml) {
        if (dirtyHtml == null || dirtyHtml.trim().isEmpty()) {
            return "";
        }
        try {
            // 使用 Jsoup 净化HTML
            String cleanHtml = Jsoup.clean(dirtyHtml, "", RICH_TEXT_WHITELIST,
                    new Document.OutputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.base));
            return cleanHtml;
        } catch (Exception e) {
            // 如果处理失败，返回空字符串
            System.err.println("[HtmlSanitizer] 净化富文本失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 净化基础文本（用于标题、摘要等）
     * 仅允许最基本的文本格式
     *
     * @param dirtyHtml 原始文本
     * @return 净化后的安全文本
     */
    public static String sanitizeBasic(String dirtyHtml) {
        if (dirtyHtml == null || dirtyHtml.trim().isEmpty()) {
            return "";
        }
        try {
            // 先移除所有HTML标签
            String text = Jsoup.clean(dirtyHtml, "", BASIC_WHITELIST,
                    new Document.OutputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.base));
            return text;
        } catch (Exception e) {
            System.err.println("[HtmlSanitizer] 净化基础文本失败: " + e.getMessage());
            return dirtyHtml; // 失败时返回原文，让其他地方处理
        }
    }

    /**
     * 完全移除HTML标签（用于纯文本字段）
     *
     * @param html 原始HTML
     * @return 纯文本
     */
    public static String stripAllHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

    /**
     * 检查内容是否包含潜在的XSS风险
     *
     * @param html 原始HTML
     * @return true-包含风险，false-安全
     */
    public static boolean containsRisk(String html) {
        if (html == null || html.trim().isEmpty()) {
            return false;
        }
        try {
            String before = html;
            String after = Jsoup.clean(html, "", Safelist.none());
            return !before.equals(after);
        } catch (Exception e) {
            return true; // 出错时视为有风险
        }
    }
}
