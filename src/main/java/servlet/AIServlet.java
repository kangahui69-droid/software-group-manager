package servlet;

import com.google.gson.Gson;
import model.AIMessage;
import model.User;
import service.AIService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIServlet extends HttpServlet {

    private AIService aiService = new AIService();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("chat".equals(action)) {
            HttpSession session = req.getSession(false);
            String sessionId = (String) session.getAttribute("aiSessionId");
            if (sessionId == null) {
                sessionId = aiService.generateSessionId();
                session.setAttribute("aiSessionId", sessionId);
            }
            
            User user = null;
            if (session != null && session.getAttribute("user") != null) {
                user = (User) session.getAttribute("user");
            }
            
            String userRole = "GUEST";
            if (user != null) {
                userRole = user.getRole();
            }
            
            req.setAttribute("sessionId", sessionId);
            req.setAttribute("userRole", userRole);
            req.getRequestDispatcher("/jsp/ai/ai_chat.jsp").forward(req, resp);
        } else if ("history".equals(action)) {
            getChatHistory(req, resp);
        } else if ("statistics".equals(action)) {
            showStatistics(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/ai?action=chat");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");

        if ("send".equals(action)) {
            sendMessage(req, resp);
        } else if ("clear".equals(action)) {
            clearConversation(req, resp);
        }
    }

    private void sendMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = req.getParameter("message");
        String sessionId = req.getParameter("session_id");
        
        if (message == null || message.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "消息不能为空");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(error));
            return;
        }

        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        }

        String aiResponse = aiService.getAIResponse(message, sessionId, user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("response", aiResponse);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(result));
    }

    private void getChatHistory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getParameter("session_id");
        List<AIMessage> history = aiService.getConversationHistory(sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("history", history);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(result));
    }

    private void clearConversation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getParameter("session_id");
        aiService.clearConversation(sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(result));
    }

    private void showStatistics(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<model.AIFaqStatistics> stats = aiService.getAllFaqStatistics();
        req.setAttribute("stats", stats);
        req.getRequestDispatcher("/jsp/ai/ai_statistics.jsp").forward(req, resp);
    }
}