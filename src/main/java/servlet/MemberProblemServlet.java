package servlet;

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

public class MemberProblemServlet extends HttpServlet {
    private ProblemReportDAO problemReportDAO = new ProblemReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            listReports(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("submit".equals(action)) {
            submitReport(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
        List<ProblemReport> reports = problemReportDAO.findByUserId(user.getId());
        request.setAttribute("reports", reports);
        request.getRequestDispatcher("/jsp/member/problem/list.jsp").forward(request, response);
    }

    private void submitReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendJsonResponse(response, false, "请先登录");
            return;
        }

        String title = request.getParameter("title");
        String content = request.getParameter("content");

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

        User user = (User) session.getAttribute("user");
        ProblemReport report = new ProblemReport();
        report.setTitle(title);
        report.setContent(content);
        report.setUserId(user.getId());
        report.setReporterType("ADMIN".equals(user.getRole()) ? ProblemReport.REPORTER_TYPE_ADMIN : ProblemReport.REPORTER_TYPE_MEMBER);
        report.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
        report.setStatus(ProblemReport.STATUS_PENDING);
        
        String reporterName = user.getName();
        if (reporterName == null || reporterName.trim().isEmpty()) {
            reporterName = "成员_" + user.getId();
        }
        report.setReporterName(reporterName);

        int id = problemReportDAO.insert(report);
        if (id > 0) {
            sendJsonResponse(response, true, "提交成功，我们会尽快处理您的反馈");
        } else {
            sendJsonResponse(response, false, "提交失败，请稍后重试");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        out.print(new com.google.gson.Gson().toJson(result));
    }
}