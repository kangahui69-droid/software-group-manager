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
import util.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
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
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                listActivities(request, response, null);
                break;
            case "detail":
                showActivityDetail(request, response);
                break;
            case "create":
            case "createForm":
                if (!AuthHelper.checkLogin(request, response)) return;
                showCreateForm(request, response);
                break;
            case "edit":
                if (!AuthHelper.checkAdmin(request, response)) return;
                showEditForm(request, response);
                break;
            case "myActivities":
                if (!AuthHelper.checkLogin(request, response)) return;
                User user1 = AuthHelper.getCurrentUser(request);
                showMyActivities(request, response, user1);
                break;
            case "myCreatedActivities":
                if (!AuthHelper.checkLogin(request, response)) return;
                User user3 = AuthHelper.getCurrentUser(request);
                showMyCreatedActivities(request, response, user3);
                break;
            case "manage":
                if (!AuthHelper.checkAdmin(request, response)) return;
                showActivityManage(request, response);
                break;
            case "participants":
                if (!AuthHelper.checkAdmin(request, response)) return;
                showParticipants(request, response);
                break;
            case "approve":
                if (!AuthHelper.checkAdmin(request, response)) return;
                approveParticipant(request, response);
                break;
            case "reject":
                if (!AuthHelper.checkAdmin(request, response)) return;
                rejectParticipant(request, response);
                break;
            case "deleteRegistration":
                if (!AuthHelper.checkAdmin(request, response)) return;
                deleteRegistration(request, response);
                break;
            case "delete":
                if (!AuthHelper.checkAdmin(request, response)) return;
                deleteActivity(request, response);
                break;
            case "approveActivity":
                if (!AuthHelper.checkAdmin(request, response)) return;
                approveActivity(request, response);
                break;
            case "rejectActivity":
                if (!AuthHelper.checkAdmin(request, response)) return;
                rejectActivityAction(request, response);
                break;
            case "cancel":
                if (!AuthHelper.checkLogin(request, response)) return;
                User user2 = AuthHelper.getCurrentUser(request);
                cancelRegistration(request, response, user2);
                break;
            default:
                listActivities(request, response, null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 检查是否已登录
        if (!AuthHelper.checkLogin(request, response)) {
            return;
        }
        User user = AuthHelper.getCurrentUser(request);

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
                if (!AuthHelper.checkAdmin(request, response)) return;
                updateActivity(request, response);
                break;
            case "delete":
                if (!AuthHelper.checkAdmin(request, response)) return;
                deleteActivity(request, response);
                break;
            case "register":
                registerActivity(request, response, user);
                break;
            case "cancel":
                cancelRegistration(request, response, user);
                break;
            case "approve":
                if (!AuthHelper.checkAdmin(request, response)) return;
                approveParticipant(request, response);
                break;
            case "reject":
                if (!AuthHelper.checkAdmin(request, response)) return;
                rejectParticipant(request, response);
                break;
            case "deleteRegistration":
                if (!AuthHelper.checkAdmin(request, response)) return;
                deleteRegistration(request, response);
                break;
            case "batchApprove":
                if (!AuthHelper.checkAdmin(request, response)) return;
                batchApproveParticipants(request, response);
                break;
            case "batchReject":
                if (!AuthHelper.checkAdmin(request, response)) return;
                batchRejectParticipants(request, response);
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

        // 根据用户角色决定显示内容
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
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

        // 检查当前用户已报名的活动（仅对已登录用户）
        if (user != null) {
            for (Activity activity : activities) {
                boolean hasRegistered = registrationDAO.isRegistered(activity.getId(), user.getId());
                activity.setRegisteredByCurrentUser(hasRegistered);
                if (hasRegistered) {
                    activity.setRegistrationOpen(false); // 已报名则不能再报名
                }
            }
        }

        request.setAttribute("activities", activities);
        request.setAttribute("viewMode", viewMode);
        request.setAttribute("keyword", keyword);
        request.setAttribute("activityType", activityType);

        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
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

    private void showMyCreatedActivities(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Activity> createdActivities = activityDAO.findByCreatorId(user.getId());
        request.setAttribute("createdActivities", createdActivities);
        request.getRequestDispatcher("/jsp/activity/myCreatedActivities.jsp").forward(request, response);
    }

    // ==================== Admin Activity Management ====================
    
    private void showActivityManage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String activityType = request.getParameter("activityType");
        String status = request.getParameter("status");
        String approvalStatus = request.getParameter("approvalStatus");
        
        List<Activity> activities = activityDAO.findByConditions(keyword, activityType, status, approvalStatus);
        
        request.setAttribute("activities", activities);
        request.setAttribute("keyword", keyword);
        request.setAttribute("activityType", activityType);
        request.setAttribute("status", status);
        request.setAttribute("approvalStatus", approvalStatus);
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

    private void createActivity(HttpServletRequest request, HttpServletResponse response, User creator) throws IOException {
        try {
            Activity activity = extractFromRequest(request);
            
            // 非管理员创建活动时设置为待审核状态
            boolean isAdmin = "ADMIN".equalsIgnoreCase(creator.getRole());
            if (isAdmin) {
                activity.setApprovalStatus("approved");
            } else {
                activity.setApprovalStatus("pending");
                activity.setCreatorId(creator.getId());
            }

            // ====== 时间校验 ======
            String timeError = validateActivityTime(activity);
            if (timeError != null) {
                response.sendRedirect(request.getContextPath() +
                    "/activity?action=create&error=" + encode(timeError));
                return;
            }
            // ======================

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
                if (isAdmin) {
                    response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动创建成功"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&success=" + encode("活动已提交，等待管理员审核"));
                }
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

            // ====== 时间校验 ======
            String timeError = validateActivityTime(activity);
            if (timeError != null) {
                response.sendRedirect(request.getContextPath() +
                    "/activity?action=edit&id=" + activity.getId() + "&error=" + encode(timeError));
                return;
            }
            // ======================

            // 同名活动时间冲突校验（编辑时排除自己） ======
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
        Connection conn = null;
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

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 删除该活动的所有报名记录（包括pending、rejected等）
            registrationDAO.deleteByActivityId(id, conn);

            // 删除活动
            boolean success = activityDAO.delete(id, conn);

            if (success) {
                conn.commit();  // 提交事务
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动删除成功"));
            } else {
                conn.rollback();  // 回滚
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("删除失败"));
            }
        } catch (NumberFormatException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的ID"));
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("删除失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // 恢复自动提交
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
    
    /**
     * 报名活动（使用悲观锁保证并发安全）
     */
    private void registerActivity(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("无效的活动"));
            return;
        }

        java.sql.Connection conn = null;
        try {
            int activityId = Integer.parseInt(activityIdStr);

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 使用悲观锁查询活动（锁定活动行，防止并发修改）
            Activity activity = activityDAO.findByIdForUpdate(activityId, conn);
            if (activity == null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("活动不存在"));
                return;
            }

            // 检查是否在报名有效期内
            if (!activity.isInRegistrationPeriod()) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("不在报名有效期内"));
                return;
            }

            // 检查是否已报名（使用同一连接查询）
            String existingStatus = registrationDAO.getRegistrationStatus(activityId, user.getId());
            if (existingStatus != null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("您已报名此活动"));
                return;
            }

            // 报名人数限制检查（在锁的保护下检查，确保原子性）
            // 检查 pending + confirmed 总人数
            if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
                int pendingCount = registrationDAO.getParticipantCount(activityId, "pending", conn);
                int confirmedCount = registrationDAO.getParticipantCount(activityId, "confirmed", conn);
                int totalCount = pendingCount + confirmedCount;

                if (totalCount >= activity.getMaxParticipants()) {
                    conn.rollback();
                    response.sendRedirect(request.getContextPath() +
                        "/activity?action=list&error=" +
                        encode("活动报名人数已满"));
                    return;
                }
            }

            // 保存报名（在锁的保护下插入）
            boolean success = registrationDAO.register(activityId, user.getId(), conn);

            if (success) {
                // 提交事务
                conn.commit();
                response.sendRedirect(request.getContextPath() + "/activity?action=myActivities&success=" + encode("报名成功，请等待审核"));
            } else {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("报名失败"));
            }
        } catch (NumberFormatException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("无效的活动ID"));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=list&error=" + encode("报名失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * 审核通过报名（使用悲观锁保证并发安全）
     */
    private void approveParticipant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        java.sql.Connection conn = null;
        try {
            int activityId = Integer.parseInt(activityIdStr);
            int userId = Integer.parseInt(userIdStr);

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 使用悲观锁查询活动（锁定活动行，防止并发修改）
            Activity activity = activityDAO.findByIdForUpdate(activityId, conn);
            if (activity == null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动不存在"));
                return;
            }

            // 检查活动是否已过期
            if (activity.isRegistrationEnded()) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }

            // 检查人数上限（在锁的保护下检查，确保原子性）
            if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
                int currentCount = registrationDAO.getParticipantCount(activityId, "confirmed");
                if (currentCount >= activity.getMaxParticipants()) {
                    conn.rollback();
                    response.sendRedirect(request.getContextPath() +
                        "/activity?action=participants&id=" + activityId + "&error=" +
                        encode("审核失败：活动报名人数已满（上限" + activity.getMaxParticipants() + "人）"));
                    return;
                }
            }

            // 更新报名状态（在锁的保护下更新）
            registrationDAO.updateStatus(activityId, userId, "confirmed", null, conn);

            // 提交事务
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("已通过"));
        } catch (NumberFormatException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("审核失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 驳回报名（使用悲观锁保证并发安全）
     */
    private void rejectParticipant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String userIdStr = request.getParameter("userId");
        String reason = request.getParameter("reason");
        if (activityIdStr == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        java.sql.Connection conn = null;
        try {
            int activityId = Integer.parseInt(activityIdStr);
            int userId = Integer.parseInt(userIdStr);

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 使用悲观锁查询活动（锁定活动行，防止并发修改）
            Activity activity = activityDAO.findByIdForUpdate(activityId, conn);
            if (activity == null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动不存在"));
                return;
            }

            // 检查活动是否已过期
            if (activity.isRegistrationEnded()) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }

            // 更新报名状态（在锁的保护下更新）
            registrationDAO.updateStatus(activityId, userId, "rejected", reason, conn);

            // 提交事务
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("已驳回"));
        } catch (NumberFormatException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("驳回失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * 批量审核通过报名（使用悲观锁保证并发安全）
     */
    private void batchApproveParticipants(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String[] userIdsStr = request.getParameterValues("userIds");

        if (activityIdStr == null || userIdsStr == null || userIdsStr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("请选择要审核的报名"));
            return;
        }

        java.sql.Connection conn = null;
        try {
            int activityId = Integer.parseInt(activityIdStr);

            // 解析用户ID列表
            List<Integer> userIds = new ArrayList<>();
            for (String uid : userIdsStr) {
                userIds.add(Integer.parseInt(uid));
            }

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 使用悲观锁查询活动（锁定活动行，防止并发修改）
            Activity activity = activityDAO.findByIdForUpdate(activityId, conn);
            if (activity == null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动不存在"));
                return;
            }

            // 检查活动是否已过期
            if (activity.isRegistrationEnded()) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }

            // 检查人数上限（在锁的保护下检查，确保原子性）
            if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
                int currentCount = registrationDAO.getParticipantCount(activityId, "confirmed");
                int availableSlots = activity.getMaxParticipants() - currentCount;

                if (availableSlots <= 0) {
                    conn.rollback();
                    response.sendRedirect(request.getContextPath() +
                        "/activity?action=participants&id=" + activityId + "&error=" +
                        encode("审核失败：活动报名人数已满（上限" + activity.getMaxParticipants() + "人）"));
                    return;
                }

                // 如果要审核的人数超过剩余名额，阻止操作
                if (userIds.size() > availableSlots) {
                    conn.rollback();
                    response.sendRedirect(request.getContextPath() +
                        "/activity?action=participants&id=" + activityId + "&error=" +
                        encode("审核失败：剩余名额不足，最多还可通过 " + availableSlots + " 人，当前选择了 " + userIds.size() + " 人"));
                    return;
                }
            }

            // 批量更新报名状态（在锁的保护下更新）
            registrationDAO.batchUpdateStatus(userIds, activityId, "confirmed", conn);

            // 提交事务
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("批量通过成功"));
        } catch (NumberFormatException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("批量审核失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 批量驳回报名（使用悲观锁保证并发安全）
     */
    private void batchRejectParticipants(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("activityId");
        String[] userIdsStr = request.getParameterValues("userIds");

        if (activityIdStr == null || userIdsStr == null || userIdsStr.length == 0) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("请选择要审核的报名"));
            return;
        }

        java.sql.Connection conn = null;
        try {
            int activityId = Integer.parseInt(activityIdStr);

            // 解析用户ID列表
            List<Integer> userIds = new ArrayList<>();
            for (String uid : userIdsStr) {
                userIds.add(Integer.parseInt(uid));
            }

            // 获取数据库连接并开启事务
            conn = util.DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 使用悲观锁查询活动（锁定活动行，防止并发修改）
            Activity activity = activityDAO.findByIdForUpdate(activityId, conn);
            if (activity == null) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动不存在"));
                return;
            }

            // 检查活动是否已过期
            if (activity.isRegistrationEnded()) {
                conn.rollback();
                response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&error=" + encode("活动已过期，无法审核"));
                return;
            }

            // 批量更新报名状态（在锁的保护下更新）
            registrationDAO.batchUpdateStatus(userIds, activityId, "rejected", conn);

            // 提交事务
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/activity?action=participants&id=" + activityId + "&success=" + encode("批量驳回成功"));
        } catch (NumberFormatException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数格式错误"));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("批量驳回失败：" + e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 校验活动时间合理性
     * @param activity 活动对象
     * @return null表示校验通过，String表示错误信息
     */
    private String validateActivityTime(Activity activity) {
        // 1. 活动开始时间不能为空
        if (activity.getActivityStartTime() == null) {
            return "活动开始时间不能为空";
        }

        // 2. 活动结束时间不能为空
        if (activity.getActivityEndTime() == null) {
            return "活动结束时间不能为空";
        }

        // 3. 活动结束时间必须晚于开始时间
        if (!activity.getActivityEndTime().after(activity.getActivityStartTime())) {
            return "活动结束时间必须晚于开始时间";
        }

        // 4. 活动持续时间不能超过合理范围（如不超过30天）
        long durationMs = activity.getActivityEndTime().getTime() - activity.getActivityStartTime().getTime();
        long maxDurationMs = 30L * 24 * 60 * 60 * 1000; // 30天
        if (durationMs > maxDurationMs) {
            return "活动持续时间不能超过30天";
        }

        // 5. 报名开始时间不能为空
        if (activity.getRegistrationStartTime() == null) {
            return "报名开始时间不能为空";
        }

        // 6. 报名截止时间不能为空
        if (activity.getRegistrationEndTime() == null) {
            return "报名截止时间不能为空";
        }

        // 7. 报名结束时间必须晚于开始时间
        if (!activity.getRegistrationEndTime().after(activity.getRegistrationStartTime())) {
            return "报名截止时间必须晚于报名开始时间";
        }

        // 8. 报名截止时间必须早于活动开始时间
        if (!activity.getRegistrationEndTime().before(activity.getActivityStartTime())) {
            return "报名截止时间必须早于活动开始时间";
        }

        return null; // 校验通过
    }

    private String encode(String message) {
        try {
            return URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }

    // ==================== Activity Approval (Admin) ====================

    private void approveActivity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("id");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            if (activityDAO.approveActivity(activityId)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动已批准"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("操作失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的活动ID"));
        }
    }

    private void rejectActivityAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activityIdStr = request.getParameter("id");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("参数错误"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            if (activityDAO.rejectActivity(activityId)) {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&success=" + encode("活动已拒绝"));
            } else {
                response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("操作失败"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=manage&error=" + encode("无效的活动ID"));
        }
    }
}
