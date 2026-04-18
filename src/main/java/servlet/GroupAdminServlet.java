package servlet;

import dao.ActivityGroupDAO;
import dao.GroupMessageDAO;
import dao.GroupMemberDAO;
import dao.UserGroupDAO;
import model.ActivityGroup;
import model.GroupMember;
import model.GroupMessage;
import model.User;
import util.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class GroupAdminServlet extends HttpServlet {

    private ActivityGroupDAO groupDAO = new ActivityGroupDAO();
    private GroupMessageDAO messageDAO = new GroupMessageDAO();
    private GroupMemberDAO memberDAO = new GroupMemberDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AuthHelper.checkAdmin(req, resp)) {
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listGroups(req, resp);
                break;
            case "myGroups":
                listMyGroups(req, resp);
                break;
            case "detail":
                showGroupDetail(req, resp);
                break;
            case "messages":
                showGroupMessages(req, resp);
                break;
            case "delete":
                deleteGroup(req, resp);
                break;
            case "joinAsAdmin":
                joinAsAdmin(req, resp);
                break;
            default:
                listGroups(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AuthHelper.checkAdmin(req, resp)) {
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        switch (action) {
            case "mute":
                muteGroup(req, resp);
                break;
            case "unmute":
                unmuteGroup(req, resp);
                break;
            case "deleteMessage":
                deleteMessage(req, resp);
                break;
            case "delete":
                deleteGroup(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void listGroups(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        List<ActivityGroup> groups = groupDAO.findAll();
        java.util.Map<Integer, Boolean> memberStatus = new java.util.HashMap<>();
        for (ActivityGroup group : groups) {
            boolean isMember = memberDAO.isMember(group.getId(), currentUser.getId());
            memberStatus.put(group.getId(), isMember);
        }
        req.setAttribute("groups", groups);
        req.setAttribute("memberStatus", memberStatus);
        req.getRequestDispatcher("/jsp/admin/group/groupList.jsp").forward(req, resp);
    }

    private void listMyGroups(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = (User) req.getSession().getAttribute("user");

        List<model.UserGroup> userGroups = userGroupDAO.findByUserId(currentUser.getId());

        List<ActivityGroup> ownedGroups = groupDAO.findByOwnerId(currentUser.getId());
        java.util.Set<Integer> existingGroupIds = new java.util.HashSet<>();
        for (model.UserGroup ug : userGroups) {
            existingGroupIds.add(ug.getGroupId());
        }

        for (ActivityGroup group : ownedGroups) {
            if (!existingGroupIds.contains(group.getId())) {
                model.UserGroup ug = new model.UserGroup();
                ug.setGroupId(group.getId());
                ug.setGroupName(group.getGroupName());
                ug.setOwnerId(currentUser.getId());
                ug.setMemberCount(memberDAO.countByGroupId(group.getId()));
                int unreadCount = memberDAO.getUnreadCount(group.getId(), currentUser.getId());
                ug.setUnreadCount(unreadCount);
                userGroups.add(ug);
            }
        }

        req.setAttribute("userGroups", userGroups);
        req.getRequestDispatcher("/jsp/group/my-groups.jsp").forward(req, resp);
    }

    private void showGroupDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String groupIdStr = req.getParameter("id");
        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);
            if (group == null) {
                resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
                return;
            }

            List<GroupMessage> recentMessages = messageDAO.findRecentByGroupId(groupId, 50);
            int totalMessages = messageDAO.countByGroupId(groupId);

            req.setAttribute("group", group);
            req.setAttribute("recentMessages", recentMessages);
            req.setAttribute("totalMessages", totalMessages);
            req.getRequestDispatcher("/jsp/admin/group/groupDetail.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void showGroupMessages(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String groupIdStr = req.getParameter("id");
        String pageStr = req.getParameter("page");
        int page = 1;
        int pageSize = 50;

        if (pageStr != null) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
            }
        }

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);
            if (group == null) {
                resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
                return;
            }

            int offset = (page - 1) * pageSize;
            List<GroupMessage> messages = messageDAO.findByGroupId(groupId, pageSize, offset);
            int totalMessages = messageDAO.countByGroupId(groupId);
            int totalPages = (int) Math.ceil((double) totalMessages / pageSize);

            req.setAttribute("group", group);
            req.setAttribute("messages", messages);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalMessages", totalMessages);
            req.getRequestDispatcher("/jsp/admin/group/groupMessages.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void muteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String groupIdStr = req.getParameter("groupId");
        String duration = req.getParameter("duration");
        String reason = req.getParameter("reason");

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            Integer groupId = Integer.parseInt(groupIdStr);
            Date mutedUntil = null;

            if (duration != null && !duration.isEmpty()) {
                int hours = Integer.parseInt(duration);
                if (hours > 0) {
                    mutedUntil = new Date(System.currentTimeMillis() + hours * 60 * 60 * 1000L);
                }
            }

            boolean success = groupDAO.muteGroup(groupId, mutedUntil, reason);
            String redirectUrl = req.getContextPath() + "/group/admin?action=detail&id=" + groupId;
            if (success) {
                resp.sendRedirect(redirectUrl + "&success=" + URLEncoder.encode("禁言成功", "UTF-8"));
            } else {
                resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("禁言失败", "UTF-8"));
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void unmuteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String groupIdStr = req.getParameter("groupId");

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            Integer groupId = Integer.parseInt(groupIdStr);
            boolean success = groupDAO.unmuteGroup(groupId);
            String redirectUrl = req.getContextPath() + "/group/admin?action=detail&id=" + groupId;
            if (success) {
                resp.sendRedirect(redirectUrl + "&success=" + URLEncoder.encode("已解除禁言", "UTF-8"));
            } else {
                resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("解除禁言失败", "UTF-8"));
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void deleteMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String messageIdStr = req.getParameter("messageId");
        String groupIdStr = req.getParameter("groupId");

        if (messageIdStr == null || messageIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            int messageId = Integer.parseInt(messageIdStr);
            int groupId = Integer.parseInt(groupIdStr);

            boolean success = messageDAO.delete(messageId);
            String redirectUrl = req.getContextPath() + "/group/admin?action=messages&id=" + groupId;
            if (success) {
                resp.sendRedirect(redirectUrl + "&success=" + URLEncoder.encode("消息已删除", "UTF-8"));
            } else {
                resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("删除失败", "UTF-8"));
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void deleteGroup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String groupIdStr = req.getParameter("groupId");

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            
            messageDAO.deleteByGroupId(groupId);
            memberDAO.deleteByGroupId(groupId);
            userGroupDAO.deleteByGroupId(groupId);
            groupDAO.delete(groupId);

            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list&success=" + URLEncoder.encode("群聊已删除", "UTF-8"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }

    private void joinAsAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String groupIdStr = req.getParameter("groupId");
        User currentUser = (User) req.getSession().getAttribute("user");

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);
            
            if (group == null) {
                resp.sendRedirect(req.getContextPath() + "/group/admin?action=list&error=" + URLEncoder.encode("群聊不存在", "UTF-8"));
                return;
            }

            if (!memberDAO.isMember(groupId, currentUser.getId())) {
                memberDAO.insertMember(groupId, currentUser.getId());
                userGroupDAO.insertUserToGroup(currentUser.getId(), groupId);
            }

            resp.sendRedirect(req.getContextPath() + "/group/chat/" + groupId);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/group/admin?action=list");
        }
    }
}
