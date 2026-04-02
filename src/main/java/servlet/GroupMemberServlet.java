package servlet;

import dao.GroupMemberDAO;
import dao.UserGroupDAO;
import model.GroupMember;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 群成员管理Servlet
 */
@WebServlet("/group/member/*")
public class GroupMemberServlet extends HttpServlet {

    private GroupMemberDAO memberDAO = new GroupMemberDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();

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

        if (pathInfo == null || pathInfo.equals("/add")) {
            addMember(request, response, currentUser);
        } else if (pathInfo.equals("/remove")) {
            removeMember(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addMember(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String userIdStr = request.getParameter("userId");

        if (groupIdStr == null || userIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数不完整");
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);
        int userId = Integer.parseInt(userIdStr);

        if (!memberDAO.isOwner(groupId, currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "只有群主可以添加成员");
            return;
        }

        if (memberDAO.isMember(groupId, userId)) {
            response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId + "?error=already_member");
            return;
        }

        GroupMember member = new GroupMember(groupId, userId, GroupMember.ROLE_MEMBER);
        if (memberDAO.insert(member)) {
            userGroupDAO.insertUserToGroup(userId, groupId);
        }

        response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId);
    }

    private void removeMember(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String userIdStr = request.getParameter("userId");

        if (groupIdStr == null || userIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数不完整");
            return;
        }

        int groupId = Integer.parseInt(groupIdStr);
        int userId = Integer.parseInt(userIdStr);

        if (!memberDAO.isOwner(groupId, currentUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "只有群主可以移除成员");
            return;
        }

        if (memberDAO.isOwner(groupId, userId)) {
            response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId + "?error=cannot_remove_owner");
            return;
        }

        memberDAO.delete(groupId, userId);
        userGroupDAO.delete(userId, groupId);

        response.sendRedirect(request.getContextPath() + "/group/chat/" + groupId);
    }
}
