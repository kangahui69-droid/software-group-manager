package servlet;

import util.CSRFTokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CSRF Token 获取 Servlet
 * 用于 AJAX 请求获取新的 CSRF Token
 */
@WebServlet(name = "CSRFTokenServlet", urlPatterns = {"/csrf-token"})
public class CSRFTokenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置响应类型
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 获取或创建 CSRF Token
        String token = CSRFTokenUtil.getOrCreateToken(req);
        resp.setHeader("Cache-Control", "no-store");

        // 返回 JSON 格式的 Token
        String jsonResponse = String.format("{\"%s\": \"%s\", \"success\": true}",
                CSRFTokenUtil.CSRF_TOKEN_NAME, token);

        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
