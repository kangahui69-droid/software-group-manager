package servlet;

import util.CSRFTokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        HttpSession session = req.getSession();
        String token = CSRFTokenUtil.generateToken();
        session.setAttribute(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR, token);

        // 返回 JSON 格式的 Token
        String jsonResponse = String.format("{\"%s\": \"%s\", \"success\": true}",
                CSRFTokenUtil.CSRF_TOKEN_NAME, token);

        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // POST 请求也返回新的 Token
        doGet(req, resp);
    }
}