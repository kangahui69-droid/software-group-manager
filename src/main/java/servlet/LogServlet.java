package servlet;

import dao.OperationLogDAO;
import model.OperationLog;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 日志Servlet
 */
public class LogServlet extends HttpServlet {
    private OperationLogDAO logDAO = new OperationLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        if ("list".equals(action)) {
            listLogs(request, response);
        }
    }

    private void listLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        int pageSize = 20;

        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
            try {
                pageSize = Integer.parseInt(pageSizeStr);
            } catch (NumberFormatException e) {
                pageSize = 20;
            }
        }

        String keyword = request.getParameter("keyword");
        String operation = request.getParameter("operation");
        String module = request.getParameter("module");
        String dateRange = request.getParameter("dateRange");

        List<OperationLog> logs = logDAO.findByConditions(keyword, operation, module, dateRange, page, pageSize);
        int totalLogs = logDAO.countByConditions(keyword, operation, module, dateRange);
        int totalPages = (int) Math.ceil((double) totalLogs / pageSize);

        request.setAttribute("logs", logs);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalLogs", totalLogs);
        request.setAttribute("keyword", keyword);
        request.setAttribute("operation", operation);
        request.setAttribute("module", module);
        request.setAttribute("dateRange", dateRange);
        request.getRequestDispatcher("/jsp/log/list.jsp").forward(request, response);
    }
}
