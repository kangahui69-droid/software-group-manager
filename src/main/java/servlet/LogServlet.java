package servlet;

import dao.OperationLogDAO;
import model.AuditLogCategory;
import model.LogTimeRange;
import model.OperationLog;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        LogTimeRange range;
        try {
            range = hasValue(startTime) || hasValue(endTime)
                    ? LogTimeRange.explicit(startTime, endTime) : LogTimeRange.legacy(dateRange);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "日期范围参数无效");
            return;
        }

        List<OperationLog> logs = logDAO.findByTimeRange(keyword, operation, module,
                range.getStart(), range.getEnd(), page, pageSize);
        int totalLogs = logDAO.countByTimeRange(keyword, operation, module, range.getStart(), range.getEnd());
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
        request.setAttribute("startTime", startTime);
        request.setAttribute("endTime", endTime);
        request.setAttribute("operationOptions", AuditLogCategory.operations());
        request.setAttribute("moduleOptions", AuditLogCategory.modules());
        request.getRequestDispatcher("/jsp/log/list.jsp").forward(request, response);
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
