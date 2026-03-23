package servlet;

import dao.ActivityDAO;
import dao.RegistrationDAO;
import dao.MemberProfileDAO;
import dao.DictionaryDAO;
import model.Activity;
import model.Registration;
import model.User;
import model.MemberProfile;
import model.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 活动Servlet（新版 - 支持时间窗口报名）
 */
public class ActivityServlet extends HttpServlet {
    private ActivityDAO activityDAO = new ActivityDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
    private DictionaryDAO dictionaryDAO = new DictionaryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                listActivities(request, response, user);
                break;
            case "detail":
                showActivityDetail(request, response);
                break;
            case "create":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    showCreateForm(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "edit":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    showEditForm(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "myActivities":
                showMyActivities(request, response, user);
                break;
            case "manage":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    showActivityManage(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "participants":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    showParticipants(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "approve":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    approveParticipant(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "reject":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    rejectParticipant(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "deleteRegistration":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    deleteRegistration(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "delete":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    deleteActivity(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "cancel":
                cancelRegistration(request, response, user);
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
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    createActivity(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "update":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    updateActivity(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "delete":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    deleteActivity(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "register":
                registerActivity(request, response, user);
                break;
            case "cancel":
                cancelRegistration(request, response, user);
                break;
            case "approve":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    approveParticipant(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "reject":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    rejectParticipant(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "deleteRegistration":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    deleteRegistration(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "batchApprove":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    batchApproveParticipants(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            case "batchReject":
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    batchRejectParticipants(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // ==================== Page A: Activity List ====================
    
    private void listActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String viewMode = request.getParameter("viewMode");
        String keyword = request.getParameter("keyword");
        String activityType = request.getParameter("activityType");
        
        List<Activity> activities;
        
        // 成员访问默认显示所有活动，管理员也显示所有活动
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            // 管理员访问活动管理页面，显示所有活动
            activities = activityDAO.findByConditions(keyword, activityType, null);
        } else {
            // 成员访问活动列表，默认显示所有活动
            if ("inPeriod".equals(viewMode)) {
                // 查看有效期内活动
                activities = activityDAO.findInRegistrationPeriod();
            } else {
                // 默认显示所有活动
                activities = activityDAO.findByConditions(keyword, activityType, null);
            }
        }
        
        // 检查当前用户已报名的活动
        for (Activity activity : activities) {
            boolean hasRegistered = registrationDAO.isRegistered(activity.getId(), user.getId());
            activity.setRegisteredByCurrentUser(hasRegistered);
            if (hasRegistered) {
                activity.setRegistrationOpen(false); // 已报名则不能再报名
            }
        }
        
        request.setAttribute("activities", activities);
        request.setAttribute("viewMode", viewMode);
        request.setAttribute("keyword", keyword);
        request.setAttribute("activityType", activityType);
        
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            request.getRequestDispatcher("/jsp/admin/activity/manage.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/jsp/activity/list.jsp").forward(request, response);
        }
    }

    private void showActivityDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Activity activity = activityDAO.findById(id);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list");
                return;
            }
            
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute("user");
            boolean isRegistered = false;
            if (user != null) {
                isRegistered = registrationDAO.isRegistered(id, user.getId());
            }
            
            int currentParticipants = registrationDAO.getParticipantCount(id, "confirmed");
            activity.setCurrentParticipants(currentParticipants);
            
            request.setAttribute("activity", activity);
            request.setAttribute("isRegistered", isRegistered);
            request.getRequestDispatcher("/jsp/activity/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list");
        }
    }

    // ==================== Page B: My Activities ====================
    
    private void showMyActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String statusFilter = request.getParameter("status");
        List<Registration> registrations;
        
        if (statusFilter != null && !statusFilter.isEmpty()) {
            registrations = registrationDAO.findByUserIdAndStatus(user.getId(), statusFilter);
        } else {
            registrations = registrationDAO.findByUserId(user.getId());
        }
        
        request.setAttribute("registrations", registrations);
        request.setAttribute("statusFilter", statusFilter);
        request.getRequestDispatcher("/jsp/activity/myActivities.jsp").forward(request, response);
    }

    // ==================== Admin Activity Management ====================
    
    private void showActivityManage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String activityType = request.getParameter("activityType");
        String status = request.getParameter("status");
        
        List<Activity> activities = activityDAO.findByConditions(keyword, activityType, status);
        
        request.setAttribute("activities", activities);
        request.setAttribute("keyword", keyword);
        request.setAttribute("activityType", activityType);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/jsp/admin/activity/manage.jsp").forward(request, response);
    }

    private void showParticipants(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String activityIdStr = request.getParameter("id");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage");
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage");
                return;
            }

            List<Registration> registrations = registrationDAO.findByActivityId(activityId);
            
            request.setAttribute("activity", activity);
            request.setAttribute("registrations", registrations);
            request.getRequestDispatcher("/jsp/admin/activity/participants.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage");
        }
    }

    // ==================== Activity CRUD ====================
    
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("activityTypes", dictionaryDAO.findByType("ACTIVITY_TYPE"));
        request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Activity activity = activityDAO.findById(id);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage");
                return;
            }

            request.setAttribute("activity", activity);
            request.setAttribute("activityTypes", dictionaryDAO.findByType("ACTIVITY_TYPE"));
            request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage");
        }
    }

    private void createActivity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Activity activity = extractFromRequest(request);

            // 后端安全校验：新建活动时只允许"即将开始"或"进行中"
            String status = activity.getStatus();
            if ("completed".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status)) {
                // 非法状态，自动修正为默认值
                activity.setStatus("upcoming");
                // 或者返回错误：
                // response.sendRedirect(request.getContextPath() + "/activity?action=create&error=" + encode("新建活动不能选择已结束或已取消状态"));
                // return;
            }

            // ====== 同名活动时间冲突校验 ======
            // 检查是否存在时间重叠的同名活动
            if (activityDAO.existsByTitleAndTimeWindow(
                    activity.getTitle(),
                    activity.getActivityStartTime(),
                    activity.getActivityEndTime(),
                    null)) {
                response.sendRedirect(request.getContextPath() +
                    "/activity?action=create&error=" +
                    encode("不能创建相同名称相同时间段的活动"));
                return;
            }
            // =====================================

            if (activityDAO.insert(activity)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动创建成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=create&error=" + encode("创建失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=create&error=" + encode(e.getMessage()));
        }
    }

    private void updateActivity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Activity activity = extractFromRequest(request);
            activity.setId(Integer.parseInt(request.getParameter("id")));

            // ====== 新增：同名活动时间冲突校验（编辑时排除自己） ======
            if (activityDAO.existsByTitleAndTimeWindow(
                    activity.getTitle(),
                    activity.getActivityStartTime(),
                    activity.getActivityEndTime(),
                    activity.getId())) {
                response.sendRedirect(request.getContextPath() +
                    "/activity?action=edit&id=" + activity.getId() + "&error=" +
                    encode("不能创建相同名称相同时间段的活动"));
                return;
            }
            // =======================================================

            if (activityDAO.update(activity)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动更新成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=edit&id=" + activity.getId() + "&error=" + encode("更新失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            String id = request.getParameter("id");
            response.sendRedirect(request.getContextPath() + "/activity?action=edit&id=" + id + "&error=" + encode(e.getMessage()));
        }
    }

    private void deleteActivity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 检查活动是否可以删除
            // 1. 已确认报名的活动不可删除
            // 2. 已结束的活动不可删除
            // 3. 进行中的活动不可删除
            
            Activity activity = activityDAO.findById(id);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("活动不存在"));
                return;
            }
            
            // 检查是否有已确认的报名
            int confirmedCount = registrationDAO.getConfirmedCount(id);
            if (confirmedCount > 0) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("该活动已有学生确认参加，无法删除"));
                return;
            }
            
            // 检查活动状态
            String activityStatus = activity.getStatus();
            if ("completed".equalsIgnoreCase(activityStatus) || "ongoing".equalsIgnoreCase(activityStatus)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("已结束或进行中的活动无法删除"));
                return;
            }
            
            // 删除该活动的所有报名记录（包括pending、rejected等）
            registrationDAO.deleteByActivityId(id);
            
            // 删除活动
            if (activityDAO.delete(id)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动删除成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("删除失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的ID"));
        }
    }

    private Activity extractFromRequest(HttpServletRequest request) throws Exception {
        Activity activity = new Activity();
        activity.setTitle(request.getParameter("title"));
        activity.setDescription(request.getParameter("description"));
        activity.setActivityType(request.getParameter("activityType"));
        activity.setLocation(request.getParameter("location"));
        activity.setOrganizers(request.getParameter("organizers"));
        activity.setContactInfo(request.getParameter("contactInfo"));
        
        String maxParticipantsStr = request.getParameter("maxParticipants");
        if (maxParticipantsStr != null && !maxParticipantsStr.isEmpty()) {
            try {
                activity.setMaxParticipants(Integer.parseInt(maxParticipantsStr));
            } catch (NumberFormatException e) {
                activity.setMaxParticipants(0);
            }
        } else {
            activity.setMaxParticipants(0);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        
        // 计算下一个周六
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSaturday = Calendar.SATURDAY - dayOfWeek;
        if (daysToSaturday <= 0) daysToSaturday += 7; // 如果是周六或更晚，加7天到下周六
        cal.add(Calendar.DAY_OF_MONTH, daysToSaturday);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date defaultStartTime = cal.getTime();
        
        // 结束时间默认 +2小时
        cal.add(Calendar.HOUR_OF_DAY, 2);
        Date defaultEndTime = cal.getTime();
        
        // 当前时间
        Date now = new Date();
        
        // 活动时间
        String activityStartTime = request.getParameter("activityStartTime");
        if (activityStartTime != null && !activityStartTime.isEmpty()) {
            activity.setActivityStartTime(sdf.parse(activityStartTime));
        } else {
            activity.setActivityStartTime(defaultStartTime);
        }
        
        String activityEndTime = request.getParameter("activityEndTime");
        if (activityEndTime != null && !activityEndTime.isEmpty()) {
            activity.setActivityEndTime(sdf.parse(activityEndTime));
        } else {
            activity.setActivityEndTime(defaultEndTime);
        }
        
        // 报名时间
        String registrationStartTime = request.getParameter("registrationStartTime");
        if (registrationStartTime != null && !registrationStartTime.isEmpty()) {
            activity.setRegistrationStartTime(sdf.parse(registrationStartTime));
        } else {
            activity.setRegistrationStartTime(now);
        }
        
        String registrationEndTime = request.getParameter("registrationEndTime");
        if (registrationEndTime != null && !registrationEndTime.isEmpty()) {
            activity.setRegistrationEndTime(sdf.parse(registrationEndTime));
        } else {
            // 报名截止默认活动开始前1天
            Calendar regEndCal = Calendar.getInstance();
            regEndCal.setTime(activity.getActivityStartTime());
            regEndCal.add(Calendar.DAY_OF_MONTH, -1);
            activity.setRegistrationEndTime(regEndCal.getTime());
        }
        
        activity.setStatus(request.getParameter("status") != null ? request.getParameter("status") : "upcoming");
        return activity;
    }

    // ==================== Registration Operations ====================
    
    private void registerActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("无效的活动"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("活动不存在"));
                return;
            }

            // 检查是否在报名有效期内
            if (!activity.isInRegistrationPeriod()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("不在报名有效期内"));
                return;
            }

            // 检查是否已报名
            if (registrationDAO.isRegistered(activityId, user.getId())) {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("您已报名此活动"));
                return;
            }

            // 报名人数限制检查
            if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
                int currentCount = registrationDAO.getParticipantCount(activityId, "confirmed");
                if (currentCount >= activity.getMaxParticipants()) {
                    response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("活动报名人数已满"));
                    return;
                }
            }

            // 保存报名
            boolean success = registrationDAO.register(activityId, user.getId());

            if (success) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&success=" + encode("报名成功，请等待审核"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("报名失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("无效的活动ID"));
        }
    }

    private void cancelRegistration(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode("无效的活动"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            
            // 检查活动报名是否已截止
            Activity activity = activityDAO.findById(activityId);
            if (activity != null && activity.isRegistrationEnded()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode("报名已截止，无法取消"));
                return;
            }
            
            boolean success = registrationDAO.cancelRegistration(activityId, user.getId());
            if (success) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&success=" + encode("取消报名成功"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode("取消报名失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&error=" + encode("无效的活动ID"));
        }
    }

    // ==================== Admin Approval Operations ====================
    
    private void approveParticipant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            int userId = Integer.parseInt(userIdStr);
            
            // 检查活动是否已过期
            Activity activity = activityDAO.findById(activityId);
            if (activity != null && activity.isRegistrationEnded()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }
            
            registrationDAO.updateStatus(activityId, userId, "confirmed", null);
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("已通过"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        }
    }

    private void rejectParticipant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        String reason = request.getParameter("reason");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            int userId = Integer.parseInt(userIdStr);
            
            // 检查活动是否已过期
            Activity activity = activityDAO.findById(activityId);
            if (activity != null && activity.isRegistrationEnded()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }
            
            registrationDAO.updateStatus(activityId, userId, "rejected", reason);
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("已驳回"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        }
    }

    private void deleteRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }
        
        try {
            int activityId = Integer.parseInt(activityIdStr);
            int userId = Integer.parseInt(userIdStr);
            
            boolean success = registrationDAO.delete(activityId, userId);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("报名记录已删除"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("删除失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        }
    }

    private void batchApproveParticipants(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String[] userIdsStr = request.getParameterValues("userIds");
        
        if (activityIdStr == null || userIdsStr == null || userIdsStr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("请选择要审核的报名"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            
            // 检查活动是否已过期
            Activity activity = activityDAO.findById(activityId);
            if (activity != null && activity.isRegistrationEnded()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }
            
            List<Integer> userIds = new ArrayList<>();
            for (String uid : userIdsStr) {
                userIds.add(Integer.parseInt(uid));
            }
            
            registrationDAO.batchUpdateStatus(userIds, activityId, "confirmed");
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("批量通过成功"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        }
    }

    private void batchRejectParticipants(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String[] userIdsStr = request.getParameterValues("userIds");
        
        if (activityIdStr == null || userIdsStr == null || userIdsStr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("请选择要审核的报名"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            
            // 检查活动是否已过期
            Activity activity = activityDAO.findById(activityId);
            if (activity != null && activity.isRegistrationEnded()) {
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }
            
            List<Integer> userIds = new ArrayList<>();
            for (String uid : userIdsStr) {
                userIds.add(Integer.parseInt(uid));
            }
            
            registrationDAO.batchUpdateStatus(userIds, activityId, "rejected");
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("批量驳回成功"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        }
    }
    
    private String encode(String message) {
        try {
            return URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }
}
