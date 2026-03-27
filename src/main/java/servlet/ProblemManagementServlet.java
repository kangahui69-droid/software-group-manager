package servlet;

import com.google.gson.Gson;
import dao.ProblemManagementDAO;
import model.ProblemReport;
import model.User;
import util.HtmlSanitizer;

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

public class ProblemManagementServlet extends HttpServlet {
    private ProblemManagementDAO problemManagementDAO = new ProblemManagementDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        System.out.println("ProblemManagementServlet doGet: requestURI=" + request.getRequestURI() + ", pathInfo=" + pathInfo);

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            listProblems(request, response);
        } else if (pathInfo.equals("/detail") || "/detail".equals(request.getParameter("action"))) {
            getProblemDetail(request, response);
        } else if ("/test".equals(pathInfo)) {
            // 测试端点
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"message\":\"test ok\"}");
            response.getWriter().flush();
        } else {
            System.out.println("ProblemManagementServlet: pathInfo not matched, sending 404");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("update".equals(action)) {
            updateProblem(request, response);
        } else if ("delete".equals(action)) {
            deleteProblem(request, response);
        } else {
            sendJsonResponse(response, false, "未知操作");
        }
    }

    private void listProblems(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            request.setAttribute("error", "您没有权限访问此页面");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        String category = request.getParameter("category");
        
        List<ProblemReport> verifiedList = problemManagementDAO.findByCategory("VERIFIED");
        List<ProblemReport> invalidList = problemManagementDAO.findByCategory("INVALID");
        List<ProblemReport> unverifiedList = problemManagementDAO.findByCategory("UNVERIFIED");
        
        request.setAttribute("verifiedList", verifiedList);
        request.setAttribute("invalidList", invalidList);
        request.setAttribute("unverifiedList", unverifiedList);
        request.setAttribute("verifiedCount", verifiedList.size());
        request.setAttribute("invalidCount", invalidList.size());
        request.setAttribute("unverifiedCount", unverifiedList.size());
        
        request.getRequestDispatcher("/jsp/admin/problem/manage.jsp").forward(request, response);
    }

    private void getProblemDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "权限不足");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            sendJsonResponse(response, false, "缺少问题ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            System.out.println("ProblemManagementServlet: Getting detail for id=" + id);
            ProblemReport report = problemManagementDAO.findByReportId(id);
            System.out.println("ProblemManagementServlet: Found report=" + (report != null ? report.getTitle() : "null"));
            if (report != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", report.getId());
                data.put("title", escapeForJson(report.getTitle()));
                data.put("content", escapeForJson(report.getContent()));
                data.put("reporterName", escapeForJson(report.getReporterName()));
                data.put("reporterContact", escapeForJson(report.getReporterContact()));
                data.put("reporterType", report.getReporterType());
                data.put("category", report.getCategory());
                data.put("status", report.getStatus());
                data.put("adminComment", escapeForJson(report.getAdminComment()));
                data.put("userName", report.getUserName());
                data.put("handlerName", report.getHandlerName());
                if (report.getCreatedAt() != null) {
                    data.put("createdAt", report.getCreatedAt().toString());
                }
                if (report.getUpdatedAt() != null) {
                    data.put("updatedAt", report.getUpdatedAt().toString());
                }
                System.out.println("ProblemManagementServlet: Prepared data map with " + data.size() + " entries");
                sendJsonResponse(response, true, "success", data);
            } else {
                System.out.println("ProblemManagementServlet: Report not found");
                sendJsonResponse(response, false, "问题不存在");
            }
        } catch (NumberFormatException e) {
            System.out.println("ProblemManagementServlet: Invalid id format");
            sendJsonResponse(response, false, "无效的ID格式");
        } catch (Exception e) {
            System.out.println("ProblemManagementServlet: Error getting detail");
            e.printStackTrace();
            sendJsonResponse(response, false, "获取详情失败");
        }
    }

    private void updateProblem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "权限不足");
            return;
        }

        String idStr = request.getParameter("id");
        String category = request.getParameter("category");
        String status = request.getParameter("status");
        String adminComment = request.getParameter("adminComment");

        if (idStr == null || idStr.isEmpty() || category == null || category.isEmpty()) {
            sendJsonResponse(response, false, "缺少必要参数");
            return;
        }

        if (adminComment != null) {
            adminComment = HtmlSanitizer.sanitizeBasic(adminComment);
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean success = problemManagementDAO.updateCategoryAndStatus(id, category, status, adminComment, user.getId());
            if (success) {
                sendJsonResponse(response, true, "更新成功");
            } else {
                sendJsonResponse(response, false, "更新失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void deleteProblem(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "权限不足");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            sendJsonResponse(response, false, "缺少问题ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean success = problemManagementDAO.updateCategoryAndStatus(id, null, null, null, null);
            if (success) {
                sendJsonResponse(response, true, "删除成功");
            } else {
                sendJsonResponse(response, false, "删除失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.resetBuffer();
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        out.print(gson.toJson(result));
        out.flush();
        if (out.checkError()) {
            System.out.println("Error flushing response");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.resetBuffer();
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        String jsonStr = gson.toJson(result);
        System.out.println("Sending JSON length: " + jsonStr.length());
        out.print(jsonStr);
        out.flush();
        if (out.checkError()) {
            System.out.println("Error flushing response");
        }
    }

    private String escapeForJson(String str) {
        if (str == null) return null;
        return str.replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\"", "\\\"");
    }
}
