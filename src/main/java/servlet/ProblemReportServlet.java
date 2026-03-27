package servlet;

import com.google.gson.Gson;
import dao.ProblemReportDAO;
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

/**
 * 问题反馈Servlet
 */
public class ProblemReportServlet extends HttpServlet {
    private ProblemReportDAO problemReportDAO = new ProblemReportDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                listReports(request, response);
                break;
            case "detail":
                getReportDetail(request, response);
                break;
            case "getByCategory":
                getReportsByCategory(request, response);
                break;
            case "getByStatus":
                getReportsByStatus(request, response);
                break;
            default:
                listReports(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (action == null || action.isEmpty()) {
            sendJsonResponse(response, false, "缺少action参数");
            return;
        }

        switch (action) {
            case "submit":
                submitReport(request, response);
                break;
            case "updateCategory":
                updateCategory(request, response);
                break;
            case "updateStatus":
                updateStatus(request, response);
                break;
            case "delete":
                deleteReport(request, response);
                break;
            case "updateComment":
                updateComment(request, response);
                break;
            default:
                sendJsonResponse(response, false, "未知操作");
                break;
        }
    }

    private void listReports(HttpServletRequest request, HttpServletResponse response)
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

        List<ProblemReport> reports = problemReportDAO.findAll();
        request.setAttribute("reports", reports);
        
        request.getRequestDispatcher("/jsp/admin/problem/list.jsp").forward(request, response);
    }

    private void getReportDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            sendJsonResponse(response, false, "缺少问题ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            ProblemReport report = problemReportDAO.findById(id);
            if (report != null) {
                sendJsonResponse(response, true, "success", report);
            } else {
                sendJsonResponse(response, false, "问题不存在");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void getReportsByCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String category = request.getParameter("category");
        if (category == null || category.isEmpty()) {
            sendJsonResponse(response, false, "缺少分类参数");
            return;
        }

        List<ProblemReport> reports = problemReportDAO.findByCategory(category);
        sendJsonResponse(response, true, "success", reports);
    }

    private void getReportsByStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String status = request.getParameter("status");
        if (status == null || status.isEmpty()) {
            sendJsonResponse(response, false, "缺少状态参数");
            return;
        }

        List<ProblemReport> reports = problemReportDAO.findByStatus(status);
        sendJsonResponse(response, true, "success", reports);
    }

    private void submitReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String reporterName = request.getParameter("reporterName");
        String reporterContact = request.getParameter("reporterContact");

        if (title == null || title.trim().isEmpty()) {
            sendJsonResponse(response, false, "标题不能为空");
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "内容不能为空");
            return;
        }

        title = HtmlSanitizer.sanitizeBasic(title);
        content = HtmlSanitizer.sanitizeBasic(content);
        if (reporterName != null) reporterName = HtmlSanitizer.sanitizeBasic(reporterName);
        if (reporterContact != null) reporterContact = HtmlSanitizer.sanitizeBasic(reporterContact);

        HttpSession session = request.getSession(false);
        ProblemReport report = new ProblemReport();
        report.setTitle(title);
        report.setContent(content);
        report.setReporterName(reporterName);
        report.setReporterContact(reporterContact);
        report.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
        report.setStatus(ProblemReport.STATUS_PENDING);

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            report.setUserId(user.getId());
            if ("ADMIN".equals(user.getRole())) {
                report.setReporterType(ProblemReport.REPORTER_TYPE_ADMIN);
            } else {
                report.setReporterType(ProblemReport.REPORTER_TYPE_MEMBER);
            }
        } else {
            report.setReporterType(ProblemReport.REPORTER_TYPE_GUEST);
        }

        int id = problemReportDAO.insert(report);
        if (id > 0) {
            sendJsonResponse(response, true, "提交成功，我们会尽快处理您的反馈");
        } else {
            sendJsonResponse(response, false, "提交失败，请稍后重试");
        }
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String idStr = request.getParameter("id");
        String category = request.getParameter("category");
        String adminComment = request.getParameter("adminComment");

        if (idStr == null || idStr.isEmpty() || category == null || category.isEmpty()) {
            sendJsonResponse(response, false, "缺少必要参数");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String status = ProblemReport.STATUS_PENDING;
            if (ProblemReport.CATEGORY_VERIFIED.equals(category)) {
                status = ProblemReport.STATUS_PENDING;
            }
            
            if (adminComment != null) {
                adminComment = HtmlSanitizer.sanitizeBasic(adminComment);
            }
            
            boolean success = problemReportDAO.updateCategoryAndStatus(id, category, status, adminComment, user.getId());
            if (success) {
                sendJsonResponse(response, true, "分类更新成功");
            } else {
                sendJsonResponse(response, false, "更新失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        String adminComment = request.getParameter("adminComment");

        if (idStr == null || idStr.isEmpty() || status == null || status.isEmpty()) {
            sendJsonResponse(response, false, "缺少必要参数");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            ProblemReport report = problemReportDAO.findById(id);
            if (report == null) {
                sendJsonResponse(response, false, "问题不存在");
                return;
            }
            
            if (!ProblemReport.CATEGORY_VERIFIED.equals(report.getCategory())) {
                sendJsonResponse(response, false, "只有属实的 问题才能更新状态");
                return;
            }
            
            if (adminComment != null) {
                adminComment = HtmlSanitizer.sanitizeBasic(adminComment);
            }
            
            boolean success = problemReportDAO.updateCategoryAndStatus(id, report.getCategory(), status, adminComment, user.getId());
            if (success) {
                sendJsonResponse(response, true, "状态更新成功");
            } else {
                sendJsonResponse(response, false, "更新失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void deleteReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            sendJsonResponse(response, false, "缺少问题ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean success = problemReportDAO.delete(id);
            if (success) {
                sendJsonResponse(response, true, "删除成功");
            } else {
                sendJsonResponse(response, false, "删除失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void updateComment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            sendJsonResponse(response, false, "您没有权限执行此操作");
            return;
        }

        String idStr = request.getParameter("id");
        String adminComment = request.getParameter("adminComment");

        if (idStr == null || idStr.isEmpty()) {
            sendJsonResponse(response, false, "缺少问题ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (adminComment != null) {
                adminComment = HtmlSanitizer.sanitizeBasic(adminComment);
            }
            boolean success = problemReportDAO.updateAdminComment(id, adminComment);
            if (success) {
                sendJsonResponse(response, true, "备注更新成功");
            } else {
                sendJsonResponse(response, false, "更新失败");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "无效的ID格式");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        out.print(gson.toJson(result));
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        out.print(gson.toJson(result));
    }
}