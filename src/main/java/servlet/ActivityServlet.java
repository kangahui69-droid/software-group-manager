package servlet;

import dao.ActivityDAO;
import dao.RegistrationDAO;
import dao.UserDAO;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.Activity;
import model.User;
import service.ActivityService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 活动Servlet - 4.7 Servlet改造
 * 调用ActivityService处理业务逻辑
 */
public class ActivityServlet extends HttpServlet {

    private ActivityService activityService;

    @Override
    public void init() throws ServletException {
        this.activityService = new ActivityService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        switch (action) {
            case "list":
                listActivities(request, response, user);
                break;
            case "detail":
                getActivityDetail(request, response, user);
                break;
            case "myActivities":
                getMyActivities(request, response, user);
                break;
            case "myCreatedActivities":
                getMyCreatedActivities(request, response, user);
                break;
            default:
                listActivities(request, response, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = (User) session.getAttribute("user");

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "create":
                createActivity(request, response, user);
                break;
            case "update":
                updateActivity(request, response, user);
                break;
            case "delete":
                deleteActivity(request, response, user);
                break;
            case "register":
                registerActivity(request, response, user);
                break;
            case "cancel":
                cancelActivity(request, response, user);
                break;
            case "approve":
                approveParticipant(request, response, user);
                break;
            case "reject":
                rejectParticipant(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void listActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String activityType = request.getParameter("activityType");

        ActivityFilterDTO filter = new ActivityFilterDTO();
        filter.setKeyword(keyword);
        filter.setActivityType(activityType);

        Result result = activityService.listActivities(filter, 1, 20);
        if (result.isSuccess()) {
            request.setAttribute("activities", result.getData());
        }
        request.setAttribute("viewMode", request.getParameter("viewMode"));
        request.setAttribute("keyword", keyword);
        request.setAttribute("activityType", activityType);
        request.getRequestDispatcher("/jsp/activity/list.jsp").forward(request, response);
    }

    private void getActivityDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = activityService.getActivityDetail(id, user != null ? user.getId() : null);
            if (result.isSuccess()) {
                request.setAttribute("activity", result.getData());
            }
            request.getRequestDispatcher("/jsp/activity/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
        }
    }

    private void getMyActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Result result = activityService.getMyActivities(user.getId(), 1, 20);
        if (result.isSuccess()) {
            request.setAttribute("activities", result.getData());
        }
        request.getRequestDispatcher("/jsp/activity/myActivities.jsp").forward(request, response);
    }

    private void getMyCreatedActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Result result = activityService.getMyCreatedActivities(user.getId(), 1, 20);
        if (result.isSuccess()) {
            request.setAttribute("createdActivities", result.getData());
        }
        request.getRequestDispatcher("/jsp/activity/myCreatedActivities.jsp").forward(request, response);
    }

    private void createActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        try {
            ActivityDTO dto = extractActivityFromRequest(request);
            Result result = activityService.createActivity(dto, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&success=" + encode("活动创建成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=create&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=create&error=" + encode(e.getMessage()));
        }
    }

    private void updateActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            ActivityDTO dto = extractActivityFromRequest(request);
            Result result = activityService.updateActivity(id, dto, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&success=" + encode("活动更新成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=edit&id=" + id + "&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(e.getMessage()));
        }
    }

    private void deleteActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = activityService.deleteActivity(id, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&success=" + encode("活动删除成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(e.getMessage()));
        }
    }

    private void registerActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("无效的活动"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(activityIdStr);
            Result result = activityService.register(activityId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&success=" + encode("报名成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(e.getMessage()));
        }
    }

    private void cancelActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode("无效的活动"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(activityIdStr);
            Result result = activityService.cancelActivity(activityId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&success=" + encode("取消报名成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode(e.getMessage()));
        }
    }

    private void approveParticipant(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(activityIdStr);
            Integer targetUserId = Integer.parseInt(userIdStr);
            Result result = activityService.approveParticipant(activityId, targetUserId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&success=" + encode("审批通过"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(e.getMessage()));
        }
    }

    private void rejectParticipant(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        String reason = request.getParameter("reason");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(activityIdStr);
            Integer targetUserId = Integer.parseInt(userIdStr);
            Result result = activityService.rejectParticipant(activityId, targetUserId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&success=" + encode("已驳回"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode(e.getMessage()));
        }
    }

    private ActivityDTO extractActivityFromRequest(HttpServletRequest request) throws Exception {
        ActivityDTO dto = new ActivityDTO();
        dto.setTitle(request.getParameter("title"));
        dto.setDescription(request.getParameter("description"));
        dto.setActivityType(request.getParameter("activityType"));
        dto.setLocation(request.getParameter("location"));
        dto.setOrganizers(request.getParameter("organizers"));
        dto.setContactInfo(request.getParameter("contactInfo"));

        String maxParticipantsStr = request.getParameter("maxParticipants");
        if (maxParticipantsStr != null && !maxParticipantsStr.isEmpty()) {
            dto.setMaxParticipants(Integer.parseInt(maxParticipantsStr));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        String startTime = request.getParameter("activityStartTime");
        String endTime = request.getParameter("activityEndTime");
        String regStartTime = request.getParameter("registrationStartTime");
        String regEndTime = request.getParameter("registrationEndTime");

        if (startTime != null && !startTime.isEmpty()) {
            dto.setActivityStartTime(sdf.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            dto.setActivityEndTime(sdf.parse(endTime));
        }
        if (regStartTime != null && !regStartTime.isEmpty()) {
            dto.setRegistrationStartTime(sdf.parse(regStartTime));
        }
        if (regEndTime != null && !regEndTime.isEmpty()) {
            dto.setRegistrationEndTime(sdf.parse(regEndTime));
        }

        return dto;
    }

    private String encode(String message) {
        try {
            return java.net.URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }
}
