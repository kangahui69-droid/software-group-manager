package servlet;

import dao.PublicationNotificationDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** User-facing publication notifications. The account comes only from the session. */
public class PublicationNotificationServlet extends HttpServlet {
    private final PublicationNotificationDAO notifications = new PublicationNotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) return;
        response.setHeader("Cache-Control", "no-store");
        if ("/count".equals(request.getPathInfo())) {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"unread\":" + notifications.unreadCount(user.getId()) + "}");
            return;
        }
        if (request.getPathInfo() != null && !"/".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("notifications", notifications.list(user.getId(), 100));
        request.setAttribute("unreadCount", notifications.unreadCount(user.getId()));
        request.getRequestDispatcher("/WEB-INF/jsp/member/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireUser(request, response);
        if (user == null) return;
        if (!"/read-source".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String type = request.getParameter("type");
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (id <= 0) throw new NumberFormatException();
            notifications.markSourceRead(user.getId(), type, id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (session.getAttribute("user") instanceof User
                ? (User) session.getAttribute("user") : null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }
}
