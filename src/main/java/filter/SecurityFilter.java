package filter;

import util.XSSFilterUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全过滤器
 * 提供 XSS 防护
 */
@WebFilter(filterName = "SecurityFilter", urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[SecurityFilter] 安全过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 包装请求以处理 XSS
        HttpServletRequest wrappedRequest = new XSSRequestWrapper(httpRequest);
        // 继续过滤链
        chain.doFilter(wrappedRequest, response);
    }
    @Override
    public void destroy() {
        System.out.println("[SecurityFilter] 安全过滤器销毁");
    }
    /**
     * XSS 请求包装器
     */
    private static class XSSRequestWrapper extends HttpServletRequestWrapper {
        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
        }
        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return XSSFilterUtil.filter(value);
        }
        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] filteredValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                filteredValues[i] = XSSFilterUtil.filter(values[i]);
            }
            return filteredValues;
        }
        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> paramMap = super.getParameterMap();
            Map<String, String[]> filteredMap = new HashMap<>();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String[] values = entry.getValue();
                if (values != null) {
                    String[] filteredValues = new String[values.length];
                    for (int i = 0; i < values.length; i++) {
                        filteredValues[i] = XSSFilterUtil.filter(values[i]);
                    }
                    filteredMap.put(entry.getKey(), filteredValues);
                } else {
                    filteredMap.put(entry.getKey(), null);
                }
            }
            return filteredMap;
        }
        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return XSSFilterUtil.filter(value);
        }
    }
}