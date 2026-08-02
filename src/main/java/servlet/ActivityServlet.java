package servlet;

import dao.ActivityDAO;
import dao.ActivityParticipantDAO;
import dao.DictionaryDAO;
import dao.UserDAO;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.Activity;
import model.Dictionary;
import model.Registration;
import model.User;
import service.ActivityService;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动Servlet - 4.7 Servlet改造
 * 调用ActivityService处理业务逻辑
 */
public class ActivityServlet extends HttpServlet {

    private ActivityService activityService;
    private DictionaryDAO dictionaryDAO;
    private ActivityParticipantDAO registrationDAO;

    @Override
    public void init() throws ServletException {
        this.activityService = new ActivityService();
        this.dictionaryDAO = new DictionaryDAO();
        this.registrationDAO = new ActivityParticipantDAO();
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
            case "createForm":
                showCreateForm(request, response, user);
                break;
            case "manage":
                manageActivities(request, response, user);
                break;
            case "approveActivity":
                approveActivity(request, response, user);
                break;
            case "rejectActivity":
                rejectActivity(request, response, user);
                break;
            case "participants":
                getParticipants(request, response, user);
                break;
            case "edit":
                editActivity(request, response, user);
                break;
            case "delete":
                deleteActivityAction(request, response, user);
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
        // 只显示已审核通过的活动
        filter.setApprovalStatus("approved");

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

        // 传递错误和成功消息
        String error = request.getParameter("error");
        String success = request.getParameter("success");
        if (error != null && !error.isEmpty()) {
            try {
                request.setAttribute("error", java.net.URLDecoder.decode(error, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("error", error);
            }
        }
        if (success != null && !success.isEmpty()) {
            try {
                request.setAttribute("success", java.net.URLDecoder.decode(success, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("success", success);
            }
        }

        request.getRequestDispatcher("/jsp/activity/myCreatedActivities.jsp").forward(request, response);
    }

    private void createActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        try {
            ActivityDTO dto = extractActivityFromRequest(request);
            Result result = activityService.createActivity(dto, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&success=" + encode("活动创建成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=createForm&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=createForm&error=" + encode(e.getMessage()));
        }
    }

    private void updateActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            ActivityDTO dto = extractActivityFromRequest(request);
            Result result = activityService.updateActivity(id, dto, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动更新成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=edit&id=" + id + "&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(e.getMessage()));
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

    private void manageActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 获取所有活动（管理员管理）
        ActivityFilterDTO filter = new ActivityFilterDTO();
        String status = request.getParameter("status");
        if (status != null && !status.isEmpty()) {
            filter.setStatus(status);
        }

        Result result = activityService.listActivities(filter, 1, 50);
        if (result.isSuccess()) {
            request.setAttribute("activities", result.getData());
        }

        // 获取活动分类字典
        List<Dictionary> activityTypes = dictionaryDAO.findByType("activity_type");
        request.setAttribute("activityTypes", activityTypes);

        // 传递错误和成功消息
        String error = request.getParameter("error");
        String success = request.getParameter("success");
        if (error != null && !error.isEmpty()) {
            try {
                request.setAttribute("error", java.net.URLDecoder.decode(error, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("error", error);
            }
        }
        if (success != null && !success.isEmpty()) {
            try {
                request.setAttribute("success", java.net.URLDecoder.decode(success, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("success", success);
            }
        }

        request.getRequestDispatcher("/jsp/admin/activity/manage.jsp").forward(request, response);
    }

    private void approveActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动ID不能为空"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(idStr);
            Result result = activityService.approveActivity(activityId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动已批准"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(e.getMessage()));
        }
    }

    private void rejectActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        String reason = request.getParameter("reason");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动ID不能为空"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(idStr);
            Result result = activityService.rejectActivity(activityId, reason, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动已拒绝"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(e.getMessage()));
        }
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 获取活动分类字典
        List<Dictionary> activityTypes = dictionaryDAO.findByType("activity_type");
        request.setAttribute("activityTypes", activityTypes);
        request.setAttribute("returnUrl", request.getContextPath() + "/activity?action=myCreatedActivities");

        // 传递错误和成功消息
        String error = request.getParameter("error");
        String success = request.getParameter("success");
        if (error != null && !error.isEmpty()) {
            try {
                request.setAttribute("error", java.net.URLDecoder.decode(error, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("error", error);
            }
        }
        if (success != null && !success.isEmpty()) {
            try {
                request.setAttribute("success", java.net.URLDecoder.decode(success, "UTF-8"));
            } catch (Exception e) {
                request.setAttribute("success", success);
            }
        }

        request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
    }

    private void getParticipants(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动ID不能为空"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(idStr);
            Result activityResult = activityService.getActivityDetail(activityId, user.getId());
            if (activityResult.isSuccess()) {
                request.setAttribute("activity", activityResult.getData());
            }

            List<Registration> participants = registrationDAO.findByActivityId(activityId);
            request.setAttribute("participants", participants);

            request.getRequestDispatcher("/jsp/admin/activity/participants.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的活动ID"));
        }
    }

    private void editActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动ID不能为空"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(idStr);
            Result result = activityService.getActivityDetail(activityId, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("activity", result.getData());
            }

            List<Dictionary> activityTypes = dictionaryDAO.findByType("activity_type");
            request.setAttribute("activityTypes", activityTypes);
            request.setAttribute("returnUrl", request.getContextPath() + "/activity?action=manage");

            request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的活动ID"));
        }
    }

    private void deleteActivityAction(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动ID不能为空"));
            return;
        }

        try {
            Integer activityId = Integer.parseInt(idStr);
            Result result = activityService.deleteActivity(activityId, user.getId());

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动已删除"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(result.getMessage()));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode(e.getMessage()));
        }
    }
}
