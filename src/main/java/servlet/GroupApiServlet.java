package servlet;

import dao.GroupMemberDAO;
import dao.GroupMessageDAO;
import model.GroupMessage;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 群消息AJAX接口Servlet
 */
@WebServlet("/group/api/*")
public class GroupApiServlet extends HttpServlet {

    private GroupMessageDAO messageDAO = new GroupMessageDAO();
    private GroupMemberDAO memberDAO = new GroupMemberDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (pathInfo == null || pathInfo.equals("/messages")) {
            getMessages(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        request.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/send")) {
            sendMessage(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getMessages(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");

        if (groupIdStr == null) {
            response.getWriter().write("{\"success\":false,\"error\":\"参数不完整\"}");
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);

        if (!memberDAO.isMember(groupId, currentUser.getId())) {
            response.getWriter().write("{\"success\":false,\"error\":\"无权限\"}");
            return;
        }

        int limit = 20;
        List<GroupMessage> messages = messageDAO.findByGroupId(groupId, limit, 0);

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"messages\":[");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < messages.size(); i++) {
            GroupMessage msg = messages.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(msg.getId());
            json.append(",\"groupId\":").append(msg.getGroupId());
            json.append(",\"senderId\":").append(msg.getSenderId());
            json.append(",\"senderName\":\"").append(escapeJson(msg.getSenderName())).append("\"");
            json.append(",\"content\":\"").append(escapeJson(msg.getContent())).append("\"");
            json.append(",\"sentAt\":\"").append(sdf.format(msg.getSentAt())).append("\"");
            json.append("}");
        }
        json.append("],\"count\":").append(messages.size()).append("}");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
    }

    private void sendMessage(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String content = request.getParameter("content");

        if (groupIdStr == null || content == null || content.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false,\"error\":\"参数不完整\"}");
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);

        if (!memberDAO.isMember(groupId, currentUser.getId())) {
            response.getWriter().write("{\"success\":false,\"error\":\"无权限\"}");
            return;
        }

        GroupMessage message = new GroupMessage(groupId, currentUser.getId(), content.trim());
        int messageId = messageDAO.insert(message);

        if (messageId > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"message\":{");
            json.append("\"id\":").append(messageId);
            json.append(",\"groupId\":").append(groupId);
            json.append(",\"senderId\":").append(currentUser.getId());
            json.append(",\"senderName\":\"").append(escapeJson(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername())).append("\"");
            json.append(",\"content\":\"").append(escapeJson(content.trim())).append("\"");
            json.append(",\"sentAt\":\"").append(sdf.format(message.getSentAt())).append("\"");
            json.append("}}");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json.toString());
        } else {
            response.getWriter().write("{\"success\":false,\"error\":\"发送失败\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
