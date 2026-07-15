package servlet;

import dao.ActivityDAO;
import dao.ActivityGroupDAO;
import dao.FileStorageDAO;
import dao.GroupMemberDAO;
import dao.GroupMessageDAO;
import dao.RegistrationDAO;
import dao.UserGroupDAO;
import model.Activity;
import model.ActivityGroup;
import model.FileStorage;
import model.GroupMember;
import model.GroupMessage;
import model.Registration;
import model.User;
import model.UserGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.servlet.ServletException;
import util.FileUtil;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * 群聊Servlet
 */
@WebServlet("/group/*")
@MultipartConfig(maxFileSize = 1024 * 1024 * 100, maxRequestSize = 1024 * 1024 * 100)
public class GroupServlet extends HttpServlet {

    private ActivityGroupDAO groupDAO = new ActivityGroupDAO();
    private GroupMemberDAO memberDAO = new GroupMemberDAO();
    private GroupMessageDAO messageDAO = new GroupMessageDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();
    private ActivityDAO activityDAO = new ActivityDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private FileStorageDAO fileStorageDAO = new FileStorageDAO();
    private static final String UPLOAD_BASE_DIR = "group_files";

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
            if ("deleteGroup".equals(action)) {
                deleteGroup(request, response, currentUser);
            } else if ("createForActivity".equals(action)) {
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
        } else if (pathInfo.equals("/downloadFile")) {
            downloadFile(request, response, currentUser);
        } else if ("deleteGroup".equals(action)) {
            deleteGroup(request, response, currentUser);
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
        } else if (pathInfo.equals("/sendFile")) {
            sendFileMessage(request, response, currentUser);
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

        memberDAO.updateLastReadAt(groupId, currentUser.getId());

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
            ActivityGroup createdGroup = null;
            Integer newGroupId = null;
            
            if (activityId != null) {
                createdGroup = groupDAO.findByActivityId(activityId);
                if (createdGroup != null) {
                    newGroupId = createdGroup.getId();
                }
            }
            
            if (newGroupId == null) {
                List<ActivityGroup> groups = groupDAO.findByOwnerId(currentUser.getId());
                if (!groups.isEmpty()) {
                    createdGroup = groups.get(0);
                    newGroupId = createdGroup.getId();
                }
            }
            
            if (newGroupId != null) {
                memberDAO.insertOwner(newGroupId, currentUser.getId());
                userGroupDAO.insertUserToGroup(currentUser.getId(), newGroupId);

                if (selectedUserIds != null && selectedUserIds.length > 0) {
                    for (String userIdStr : selectedUserIds) {
                        try {
                            int userId = Integer.parseInt(userIdStr);
                            if (userId != currentUser.getId() && !memberDAO.isMember(newGroupId, userId)) {
                                GroupMember member = new GroupMember(newGroupId, userId, GroupMember.ROLE_MEMBER);
                                memberDAO.insert(member);
                                userGroupDAO.insertUserToGroup(userId, newGroupId);
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

        ActivityGroup group = groupDAO.findById(groupId);
        if (group != null && group.isMuted()) {
            response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId + "?error=" + URLEncoder.encode("当前群聊已被禁言", "UTF-8"));
            return;
        }

        GroupMessage message = new GroupMessage(groupId, currentUser.getId(), content.trim());
        int messageId = messageDAO.insert(message);

        response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId);
    }

    private void sendFileMessage(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");

        if (groupIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);

        if (!memberDAO.isMember(groupId, currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ActivityGroup group = groupDAO.findById(groupId);
        if (group != null && group.isMuted()) {
            response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId + "?error=" + URLEncoder.encode("当前群聊已被禁言", "UTF-8"));
            return;
        }

        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "没有选择文件");
            return;
        }

        String fileName = extractFileName(filePart);
        if (fileName == null || fileName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文件名");
            return;
        }

        System.out.println("[群聊文件上传] 文件名: " + fileName + ", 大小: " + filePart.getSize() + ", 类型: " + filePart.getContentType());

        String uploadPath = FileUtil.getCategoryDir(UPLOAD_BASE_DIR);
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String filePath = uploadPath + File.separator + uniqueFileName;

        System.out.println("[群聊文件上传] 上传路径: " + filePath);

        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[群聊文件上传] 文件保存成功");
        } catch (Exception e) {
            System.out.println("[群聊文件上传] 文件保存失败: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件上传失败");
            return;
        }

        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(currentUser.getId());
        fileStorage.setOriginalName(fileName);
        fileStorage.setStoredName(uniqueFileName);
        fileStorage.setFilePath("/localstorage/" + UPLOAD_BASE_DIR + "/" + uniqueFileName);
        fileStorage.setFileType(filePart.getContentType() != null ? filePart.getContentType() : "application/octet-stream");
        fileStorage.setFileSize(filePart.getSize());
        fileStorage.setCategory("group_file");

        Integer fileId = fileStorageDAO.insert(fileStorage);
        System.out.println("[群聊文件上传] 文件记录ID: " + fileId);

        if (fileId == null) {
            System.out.println("[群聊文件上传] 文件记录插入失败");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件记录保存失败");
            return;
        }

        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(currentUser.getId());
        message.setContent("[文件] " + fileName);
        message.setMessageType(GroupMessage.MESSAGE_TYPE_FILE);
        message.setFileId(fileId);
        message.setFileName(fileName);
        message.setFileSize(filePart.getSize());
        message.setFileType(filePart.getContentType() != null ? filePart.getContentType() : "application/octet-stream");
        message.setFilePath("/" + UPLOAD_BASE_DIR + "/" + uniqueFileName);

        messageDAO.insert(message);

        response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId);
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) {
            return null;
        }
        String[] items = contentDisp.split("; ");
        for (String s : items) {
            if (s.startsWith("filename=")) {
                try {
                    String fileName = s.substring(s.indexOf("=") + 2, s.length() - 1);
                    if (fileName.isEmpty()) {
                        return null;
                    }
                    int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
                    if (lastSlash > 0) {
                        fileName = fileName.substring(lastSlash + 1);
                    }
                    return URLDecoder.decode(fileName, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
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
        memberDAO.deleteByGroupId(groupId);
        userGroupDAO.deleteByGroupId(groupId);
        groupDAO.delete(groupId);

        response.sendRedirect(request.getContextPath() + "/group/my-groups");
    }

    private void downloadFile(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String fileIdStr = request.getParameter("fileId");
        String fileName = request.getParameter("fileName");

        if (fileIdStr == null) {
            fileIdStr = request.getParameter("id");
        }

        if (fileIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件ID不能为空");
            return;
        }

        Integer fileId = Integer.parseInt(fileIdStr);
        FileStorage fileStorage = fileStorageDAO.findById(fileId);

        if (fileStorage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        System.out.println("[群聊文件下载] fileId: " + fileId + ", 文件名: " + fileStorage.getOriginalName());
        System.out.println("[群聊文件下载] storedName: " + fileStorage.getStoredName());
        System.out.println("[群聊文件下载] filePath: " + fileStorage.getFilePath());

        String filePath = FileUtil.resolvePhysicalPath(fileStorage.getFilePath());
        File file = new File(filePath);

        if (!file.exists()) {
            String legacyPath = getServletContext().getRealPath(fileStorage.getFilePath());
            File legacyFile = new File(legacyPath);
            if (legacyFile.exists()) {
                file = legacyFile;
                filePath = legacyPath;
            }
        }

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setContentLengthLong(fileStorage.getFileSize());

        String downloadFileName = fileStorage.getOriginalName();
        if (fileName != null && !fileName.isEmpty()) {
            downloadFileName = fileName;
        }

        response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(downloadFileName, "UTF-8") + "\"");

        try (InputStream input = new FileInputStream(file);
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
}
