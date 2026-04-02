package servlet;

import dao.ActivityDAO;
import dao.ActivityGroupDAO;
import dao.GroupMemberDAO;
import dao.GroupMessageDAO;
import dao.RegistrationDAO;
import dao.UserGroupDAO;
import model.Activity;
import model.ActivityGroup;
import model.GroupMember;
import model.GroupMessage;
import model.Registration;
import model.User;
import model.UserGroup;

import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 群聊Servlet
 */
@WebServlet("/group/*")
public class GroupServlet extends HttpServlet {

    private ActivityGroupDAO groupDAO = new ActivityGroupDAO();
    private GroupMemberDAO memberDAO = new GroupMemberDAO();
    private GroupMessageDAO messageDAO = new GroupMessageDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();
    private ActivityDAO activityDAO = new ActivityDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (pathInfo == null || pathInfo.equals("/")) {
            if ("createForActivity".equals(action)) {
                showCreateForActivityForm(request, response, currentUser);
            } else if ("myGroups".equals(action)) {
                listMyGroups(request, response, currentUser);
            } else {
                listGroups(request, response, currentUser);
            }
        } else if (pathInfo.equals("/my-groups") || "myGroups".equals(action)) {
            listMyGroups(request, response, currentUser);
        } else if (pathInfo.startsWith("/chat/")) {
            String groupIdStr = pathInfo.substring("/chat/".length());
            int groupId = Integer.parseInt(groupIdStr);
            showChat(request, response, currentUser, groupId);
        } else if (pathInfo.equals("/create") || "create".equals(action)) {
            showCreateForm(request, response, currentUser);
        } else if (pathInfo.equals("/createForActivity") || "createForActivity".equals(action)) {
            showCreateForActivityForm(request, response, currentUser);
        } else if (pathInfo.equals("/add-members") || "addMembers".equals(action)) {
            showAddMembersForm(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        request.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/create")) {
            createGroup(request, response, currentUser);
        } else if (pathInfo.equals("/send")) {
            sendMessage(request, response, currentUser);
        } else if (pathInfo.equals("/delete")) {
            deleteGroup(request, response, currentUser);
        } else if (pathInfo.equals("/addMultiple")) {
            addMultipleMembers(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void listGroups(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        List<ActivityGroup> groups = groupDAO.findByUserId(currentUser.getId());
        request.setAttribute("groups", groups);
        request.getRequestDispatcher("/jsp/group/list.jsp").forward(request, response);
    }

    private void listMyGroups(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        List<UserGroup> userGroups = userGroupDAO.findByUserId(currentUser.getId());
        request.setAttribute("userGroups", userGroups);
        request.getRequestDispatcher("/jsp/group/my-groups.jsp").forward(request, response);
    }

    private void showChat(HttpServletRequest request, HttpServletResponse response, User currentUser, int groupId)
            throws ServletException, IOException {
        ActivityGroup group = groupDAO.findById(groupId);
        if (group == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "群不存在");
            return;
        }

        if (!memberDAO.isMember(groupId, currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您不是群成员");
            return;
        }

        List<GroupMember> members = memberDAO.findByGroupId(groupId);
        List<GroupMessage> messages = messageDAO.findRecentByGroupId(groupId, 50);
        boolean isOwner = memberDAO.isOwner(groupId, currentUser.getId());

        request.setAttribute("group", group);
        request.setAttribute("members", members);
        request.setAttribute("messages", messages);
        request.setAttribute("isOwner", isOwner);
        request.getRequestDispatcher("/jsp/group/chat.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/group/create.jsp").forward(request, response);
    }

    private void showCreateForActivityForm(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr == null || activityIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&error=" + URLEncoder.encode("无效的活动ID", "UTF-8"));
            return;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&error=" + URLEncoder.encode("活动不存在", "UTF-8"));
                return;
            }

            if (!activity.getApprovalStatus().equals("approved")) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&error=" + URLEncoder.encode("活动未通过审核，无法创建群聊", "UTF-8"));
                return;
            }

            if (!activity.getCreatorId().equals(currentUser.getId())) {
                response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&error=" + URLEncoder.encode("只有活动发起者可以创建群聊", "UTF-8"));
                return;
            }

            ActivityGroup existingGroup = groupDAO.findByActivityId(activityId);
            if (existingGroup != null) {
                response.sendRedirect(request.getContextPath() + "/group/chat/" + existingGroup.getId());
                return;
            }

            List<Registration> participants = registrationDAO.findByActivityId(activityId);

            request.setAttribute("activity", activity);
            request.setAttribute("participants", participants);
            request.getRequestDispatcher("/jsp/group/create-for-activity.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/activity?action=myCreatedActivities&error=无效的活动ID");
        }
    }

    private void showAddMembersForm(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        if (groupIdStr == null || groupIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/group/my-groups");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);
            if (group == null) {
                response.sendRedirect(request.getContextPath() + "/group/my-groups?error=" + URLEncoder.encode("群不存在", "UTF-8"));
                return;
            }

            if (!memberDAO.isOwner(groupId, currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, URLEncoder.encode("只有群主可以添加成员", "UTF-8"));
                return;
            }

            List<Registration> participants = null;
            if (group.getActivityId() != null) {
                participants = registrationDAO.findByActivityId(group.getActivityId());
            }

            // 获取已在群中的成员ID列表
            List<GroupMember> existingMembers = memberDAO.findByGroupId(groupId);
            List<Integer> existingMemberIds = new java.util.ArrayList<>();
            for (GroupMember m : existingMembers) {
                existingMemberIds.add(m.getUserId());
            }

            request.setAttribute("groupId", groupId);
            request.setAttribute("group", group);
            request.setAttribute("participants", participants);
            request.setAttribute("existingMemberIds", existingMemberIds);
            request.getRequestDispatcher("/jsp/group/add-members.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/group/my-groups?error=" + URLEncoder.encode("无效的群ID", "UTF-8"));
        }
    }

    private void addMultipleMembers(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String[] selectedUserIds = request.getParameterValues("selectedUsers");

        if (groupIdStr == null || groupIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/group/my-groups");
            return;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);
            if (group == null) {
                response.sendRedirect(request.getContextPath() + "/group/my-groups?error=" + URLEncoder.encode("群不存在", "UTF-8"));
                return;
            }

            if (!memberDAO.isOwner(groupId, currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, URLEncoder.encode("只有群主可以添加成员", "UTF-8"));
                return;
            }

            int addedCount = 0;
            if (selectedUserIds != null && selectedUserIds.length > 0) {
                for (String userIdStr : selectedUserIds) {
                    try {
                        int userId = Integer.parseInt(userIdStr);
                        if (!memberDAO.isMember(groupId, userId)) {
                            GroupMember member = new GroupMember(groupId, userId, GroupMember.ROLE_MEMBER);
                            memberDAO.insert(member);
                            userGroupDAO.insertUserToGroup(userId, groupId);
                            addedCount++;
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无效的用户ID
                    }
                }
            }

            response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId + "?success=" + URLEncoder.encode("添加了" + addedCount + "名成员", "UTF-8"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/group/my-groups?error=" + URLEncoder.encode("无效的群ID", "UTF-8"));
        }
    }

    private void createGroup(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupName = request.getParameter("groupName");
        Integer activityId = null;
        String activityIdStr = request.getParameter("activityId");
        if (activityIdStr != null && !activityIdStr.isEmpty()) {
            activityId = Integer.parseInt(activityIdStr);
        }
        String[] selectedUserIds = request.getParameterValues("selectedUsers");

        if (groupName == null || groupName.trim().isEmpty()) {
            request.setAttribute("error", "群名称不能为空");
            request.getRequestDispatcher("/jsp/group/create.jsp").forward(request, response);
            return;
        }

        ActivityGroup group = new ActivityGroup();
        group.setGroupName(groupName.trim());
        group.setGroupOwnerId(currentUser.getId());
        group.setActivityId(activityId);

        if (groupDAO.insert(group)) {
            ActivityGroup createdGroup = groupDAO.findByActivityId(activityId);
            if (createdGroup == null) {
                List<ActivityGroup> groups = groupDAO.findByOwnerId(currentUser.getId());
                if (!groups.isEmpty()) {
                    createdGroup = groups.get(0);
                }
            }
            
            if (createdGroup != null) {
                memberDAO.insertOwner(createdGroup.getId(), currentUser.getId());
                userGroupDAO.insertUserToGroup(currentUser.getId(), createdGroup.getId());

                if (selectedUserIds != null && selectedUserIds.length > 0) {
                    for (String userIdStr : selectedUserIds) {
                        try {
                            int userId = Integer.parseInt(userIdStr);
                            if (userId != currentUser.getId() && !memberDAO.isMember(createdGroup.getId(), userId)) {
                                GroupMember member = new GroupMember(createdGroup.getId(), userId, GroupMember.ROLE_MEMBER);
                                memberDAO.insert(member);
                                userGroupDAO.insertUserToGroup(userId, createdGroup.getId());
                            }
                        } catch (NumberFormatException e) {
                            // 忽略无效的用户ID
                        }
                    }
                }
            }

            response.sendRedirect(request.getContextPath() + "/group/my-groups");
        } else {
            request.setAttribute("error", "创建群失败");
            request.getRequestDispatcher("/jsp/group/create.jsp").forward(request, response);
        }
    }

    private void sendMessage(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String content = request.getParameter("content");

        if (groupIdStr == null || content == null || content.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);

        if (!memberDAO.isMember(groupId, currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        GroupMessage message = new GroupMessage(groupId, currentUser.getId(), content.trim());
        int messageId = messageDAO.insert(message);

        response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId);
    }

    private void deleteGroup(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        if (groupIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);
        ActivityGroup group = groupDAO.findById(groupId);

        if (group == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "群不存在");
            return;
        }

        if (!group.getGroupOwnerId().equals(currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "只有群主可以解散群");
            return;
        }

        messageDAO.deleteByGroupId(groupId);
        groupDAO.delete(groupId);

        response.sendRedirect(request.getContextPath() + "/group/my-groups");
    }
}
