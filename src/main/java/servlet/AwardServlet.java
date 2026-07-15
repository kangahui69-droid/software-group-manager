package servlet;

import dao.AwardDAO;
import dao.AwardImageDAO;
import dao.FileStorageDAO;
import dao.UserDAO;
import dto.AwardDTO;
import model.Award;
import model.User;
import service.AwardService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 奖项Servlet - 4.7 Servlet改造
 * 调用AwardService处理业务逻辑
 */
@MultipartConfig
public class AwardServlet extends HttpServlet {

    private AwardService awardService;

    @Override
    public void init() throws ServletException {
        AwardDAO awardDAO = new AwardDAO();
        AwardImageDAO awardImageDAO = new AwardImageDAO();
        UserDAO userDAO = new UserDAO();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        this.awardService = new AwardService(awardDAO, awardImageDAO, userDAO, fileStorageDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // filter 公开查询，不需要登录
        if ("filter".equals(action)) {
            filterAwards(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                listAwards(request, response, user);
                break;
            case "myAwards":
                getMyAwards(request, response, user);
                break;
            case "statistics":
                getStatistics(request, response, user);
                break;
            default:
                listAwards(request, response, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        switch (action) {
            case "submit":
                submitAward(request, response, user);
                break;
            case "approve":
                approveAward(request, response, user);
                break;
            case "reject":
                rejectAward(request, response, user);
                break;
            case "addImage":
                addAwardImage(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void filterAwards(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        if (status != null && !status.isEmpty()) {
            Result result = awardService.filterAwards(null, status);
            if (result.isSuccess()) {
                request.setAttribute("awards", result.getData());
            }
        } else {
            Result result = awardService.listAwards(null, 1);
            if (result.isSuccess()) {
                request.setAttribute("awards", result.getData());
            }
        }
        request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
    }

    private void listAwards(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String filter = request.getParameter("filter");
        String pageStr = request.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {
            }
        }

        Result result = awardService.listAwards(filter, page);
        if (result.isSuccess()) {
            request.setAttribute("awards", result.getData());
        }
        request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
    }

    private void getMyAwards(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Result result = awardService.getMyAwards(user.getId());
        if (result.isSuccess()) {
            request.setAttribute("awards", result.getData());
        }
        request.getRequestDispatcher("/jsp/award/myAwards.jsp").forward(request, response);
    }

    private void getStatistics(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Result result = awardService.getAwardStatistics(user.getId());
        if (result.isSuccess()) {
            request.setAttribute("statistics", result.getData());
        }
        request.getRequestDispatcher("/jsp/award/statistics.jsp").forward(request, response);
    }

    private void submitAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String competition = request.getParameter("competition");
        String compTime = request.getParameter("compTime");
        String awardLevel = request.getParameter("awardLevel");
        String awardType = request.getParameter("awardType");

        if (competition == null || competition.trim().isEmpty()) {
            request.setAttribute("error", "竞赛名称不能为空");
            request.getRequestDispatcher("/jsp/award/submit.jsp").forward(request, response);
            return;
        }

        if (compTime != null && !compTime.matches("\\d{4}-\\d{2}-\\d{2}")) {
            request.setAttribute("error", "日期格式错误");
            request.getRequestDispatcher("/jsp/award/submit.jsp").forward(request, response);
            return;
        }

        try {
            Map<String, String> params = new HashMap<>();
            params.put("competition", competition);
            params.put("compTime", compTime);
            params.put("awardLevel", awardLevel);
            params.put("awardType", awardType);

            AwardDTO dto = new AwardDTO();
            dto.setCompetition(competition);
            dto.setCompetitionTime(compTime);
            if (awardLevel != null && !awardLevel.isEmpty()) {
                dto.setAwardLevel(Integer.parseInt(awardLevel));
            }
            if (awardType != null && !awardType.isEmpty()) {
                dto.setAwardType(Integer.parseInt(awardType));
            }

            Result result = awardService.submitAward(dto, user.getId(), null);
            if (result.isSuccess()) {
                request.setAttribute("success", "奖项提交成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "提交失败: " + e.getMessage());
        }
        request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
    }

    private void approveAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "奖项ID不能为空");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.approveAward(id, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "奖项审批通过");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "审批失败");
        }
    }

    private void rejectAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        String reason = request.getParameter("reason");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "奖项ID不能为空");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.rejectAward(id, reason, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "奖项已驳回");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "驳回失败");
        }
    }

    private void addAwardImage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "奖项ID不能为空");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.addAwardImage(id, null, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "图片添加成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "添加失败");
        }
    }
}
