package servlet;

import com.google.gson.Gson;
import model.AIMessage;
import model.User;
import service.AIService;
import util.AIClientUtil;

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
            HttpSession session = req.getSession(true);
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
        } else if ("init".equals(action)) {
            initAI(req, resp);
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
        } else if ("sendStream".equals(action)) {
            sendMessageStream(req, resp);
        } else if ("clear".equals(action)) {
            clearConversation(req, resp);
        } else if ("execute".equals(action)) {
            executeAction(req, resp);
        }
    }

    private void sendMessageStream(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = req.getParameter("message");
        String sessionId = req.getParameter("session_id");

        if (message == null || message.trim().isEmpty()) {
            resp.setContentType("text/event-stream;charset=UTF-8");
            resp.getWriter().write("data: [ERROR] 消息不能为空\n\n");
            return;
        }

        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        }

        String userRole = user != null ? user.getRole() : "GUEST";
        String systemPrompt = aiService.buildSystemPrompt(userRole);
        String context = aiService.buildContext(message, userRole);
        String userContent = "用户消息: " + message + "\n\n" + context;

        try {
            AIClientUtil aiClient = AIClientUtil.getInstance();
            aiClient.chatStream(systemPrompt, userContent, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/event-stream;charset=UTF-8");
            resp.getWriter().write("data: [ERROR] " + e.getMessage() + "\n\n");
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

    private void initAI(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        }
        
        String userRole = user != null ? user.getRole() : "GUEST";
        String sessionId = req.getParameter("session_id");
        
        aiService.initConversation(sessionId, user);
        
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"success\":true}");
    }

    private void showStatistics(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<model.AIFaqStatistics> stats = aiService.getAllFaqStatistics();
        req.setAttribute("stats", stats);
        req.getRequestDispatcher("/jsp/ai/ai_statistics.jsp").forward(req, resp);
    }

    private void executeAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String actionType = req.getParameter("actionType");
        String actionString = req.getParameter("actionString");
        String sessionId = req.getParameter("session_id");

        System.out.println("[AIServlet] executeAction - actionType: " + actionType + ", actionString: " + actionString);

        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        }

        if (actionType == null || actionType.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "未指定操作类型");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(error));
            return;
        }

        Map<String, String> params = aiService.parseActionParams(actionString);
        Map<String, Object> result = aiService.executeAction(actionType, params, user);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(result));
    }
}