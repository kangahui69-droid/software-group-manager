package servlet;

import dao.DictionaryDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import model.*;
import util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 项目Servlet（增强版）
 */
public class ProjectServlet extends HttpServlet {
    private ProjectDAO projectDAO = new ProjectDAO();
    private DictionaryDAO dictionaryDAO = new DictionaryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listProjects(request, response);
                break;
            case "create":
                showCreateForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "detail":
                showDetail(request, response);
                break;
            case "workspace":
                showWorkspace(request, response);
                break;
            case "myApplications":
                showMyApplications(request, response);
                break;
            case "apply":
                showApplyForm(request, response);
                break;
            case "memberApplications":
                showMemberApplications(request, response);
                break;
            case "plan":
                showPlanManagement(request, response);
                break;
            case "progress":
                showProgressManagement(request, response);
                break;
            case "history":
                showProjectHistory(request, response);
                break;
            case "approve":
                approveProject(request, response, getCurrentUserId(request));
                break;
            case "reject":
                rejectProject(request, response, getCurrentUserId(request));
                break;
            case "approveMember":
                approveMemberApplication(request, response, getCurrentUserId(request));
                break;
            case "rejectMember":
                rejectMemberApplication(request, response, getCurrentUserId(request));
                break;
            case "addLabel":
                addProjectLabel(request, response, getCurrentUserId(request));
                break;
            case "removeLabel":
                removeProjectLabel(request, response, getCurrentUserId(request));
                break;
            case "addPlan":
                addProjectPlan(request, response, getCurrentUserId(request));
                break;
            case "deletePlan":
                deleteProjectPlan(request, response, getCurrentUserId(request));
                break;
            case "addProgress":
                addProjectProgress(request, response, getCurrentUserId(request));
                break;
            case "transferAdmin":
                transferProjectAdmin(request, response, getCurrentUserId(request));
                break;
            case "downloadFile":
                downloadProjectFile(request, response);
                break;
            case "deleteFile":
                deleteProjectFile(request, response, getCurrentUserId(request));
                break;
            default:
                listProjects(request, response);
        }
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return user.getId();
            }
        }
        return null;
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
        String role = user.getRole();

        if ("create".equals(action)) {
            createProject(request, response, user.getId());
        } else if ("applyMember".equals(action)) {
            applyMember(request, response, user.getId());
        } else if ("update".equals(action) && "ADMIN".equalsIgnoreCase(role)) {
            updateProject(request, response);
        } else if ("delete".equals(action) && "ADMIN".equalsIgnoreCase(role)) {
            deleteProject(request, response);
        } else if ("saveProject".equals(action) && "ADMIN".equalsIgnoreCase(role)) {
            saveProject(request, response, user.getId());
        } else if ("approve".equals(action) && "ADMIN".equalsIgnoreCase(role)) {
            approveProject(request, response, user.getId());
        } else if ("reject".equals(action) && "ADMIN".equalsIgnoreCase(role)) {
            rejectProject(request, response, user.getId());
        } else if ("approveMember".equals(action)) {
            approveMemberApplication(request, response, user.getId());
        } else if ("rejectMember".equals(action)) {
            rejectMemberApplication(request, response, user.getId());
        } else if ("addLabel".equals(action)) {
            addProjectLabel(request, response, user.getId());
        } else if ("removeLabel".equals(action)) {
            removeProjectLabel(request, response, user.getId());
        } else if ("addPlan".equals(action)) {
            addProjectPlan(request, response, user.getId());
        } else if ("updatePlan".equals(action)) {
            updateProjectPlan(request, response, user.getId());
        } else if ("addProgress".equals(action)) {
            addProjectProgress(request, response, user.getId());
        } else if ("transferAdmin".equals(action)) {
            transferProjectAdmin(request, response, user.getId());
        } else if ("uploadImage".equals(action)) {
            uploadProjectImage(request, response, user.getId());
        } else if ("uploadFile".equals(action)) {
            uploadProjectFile(request, response, user.getId());
        }
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Project project = projectDAO.findById(id);
            if (project != null) {
                String oldName = project.getName();
                project.setName(request.getParameter("title"));
                project.setDescription(request.getParameter("description"));
                project.setYear(Integer.parseInt(request.getParameter("year")));
                project.setStatus(request.getParameter("status"));
                String leaderIdStr = request.getParameter("leaderId");
                if (leaderIdStr != null && !leaderIdStr.isEmpty()) {
                    project.setLeaderId(Integer.parseInt(leaderIdStr));
                }
                
                String expectedStartDateStr = request.getParameter("expectedStartDate");
                String expectedEndDateStr = request.getParameter("expectedEndDate");
                String actualStartDateStr = request.getParameter("actualStartDate");
                String actualEndDateStr = request.getParameter("actualEndDate");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (expectedStartDateStr != null && !expectedStartDateStr.isEmpty()) {
                    project.setExpectedStartDate(sdf.parse(expectedStartDateStr));
                }
                if (expectedEndDateStr != null && !expectedEndDateStr.isEmpty()) {
                    project.setExpectedEndDate(sdf.parse(expectedEndDateStr));
                }
                if (actualStartDateStr != null && !actualStartDateStr.isEmpty()) {
                    project.setActualStartDate(sdf.parse(actualStartDateStr));
                }
                if (actualEndDateStr != null && !actualEndDateStr.isEmpty()) {
                    project.setActualEndDate(sdf.parse(actualEndDateStr));
                }
                
                projectDAO.update(project);
                
                if (user != null) {
                    projectDAO.addHistory(id, "PROJECT_UPDATE", user.getId(), user.getName(), "修改项目信息", oldName, project.getName());
                }
            }
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        } catch (NumberFormatException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void deleteProject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            projectDAO.delete(id);
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void listProjects(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String yearStr = request.getParameter("year");
        Integer year = null;
        if (yearStr != null && !yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
            }
        }

        List<Project> projects;
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            projects = projectDAO.findByConditions(keyword, status, year);
            request.setAttribute("projects", projects);
            request.setAttribute("keyword", keyword);
            request.setAttribute("status", status);
            request.setAttribute("year", yearStr);
            UserDAO userDAO = new UserDAO();
            request.setAttribute("allUsers", userDAO.findAll());
            request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
        } else if (user != null) {
            projects = projectDAO.findProjectsByUserId(user.getId());
            request.setAttribute("projects", projects);
            request.getRequestDispatcher("/member/project/list.jsp").forward(request, response);
        } else {
            projects = projectDAO.findApprovedProjects();
            request.setAttribute("projects", projects);
            request.getRequestDispatcher("/jsp/project/list.jsp").forward(request, response);
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            if (project == null) {
                response.sendRedirect(request.getContextPath() + "/project?action=list");
                return;
            }

            UserDAO userDAO = new UserDAO();
            request.setAttribute("allUsers", userDAO.findAll());
            request.setAttribute("project", project);
            request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
            request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        }
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
        request.getRequestDispatcher("/member/project/create.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;
            
            request.setAttribute("project", project);
            request.setAttribute("labels", dictionaryDAO.findByType("PROLABEL"));
            
            if (user != null) {
                boolean isMember = projectDAO.isMember(id, user.getId());
                boolean hasApplication = projectDAO.hasPendingApplication(id, user.getId());
                request.setAttribute("isMember", isMember);
                request.setAttribute("hasApplication", hasApplication);
            }
            
            request.getRequestDispatcher("/member/project/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showWorkspace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isMember = projectDAO.isMember(id, user.getId());
            if (!isMember) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<ProjectMember> members = projectDAO.getProjectMembers(id);
            List<String> labels = projectDAO.getLabels(id);
            List<ProjectPlan> plans = projectDAO.getPlans(id);
            List<ProjectProgress> progressList = projectDAO.getProgressList(id);
            List<ProjectHistory> historyList = projectDAO.getHistory(id);
            List<Object[]> projectImages = projectDAO.getProjectImages(id);
            List<Object[]> projectFiles = projectDAO.getProjectFiles(id);

            request.setAttribute("project", project);
            request.setAttribute("members", members);
            request.setAttribute("labels", labels);
            request.setAttribute("allLabels", dictionaryDAO.findByType("PROLABEL"));
            request.setAttribute("plans", plans);
            request.setAttribute("progressList", progressList);
            request.setAttribute("historyList", historyList);
            request.setAttribute("projectImages", projectImages);
            request.setAttribute("projectFiles", projectFiles);
            request.setAttribute("isAdmin", user.getId().equals(project.getAdminId()) || user.getId().equals(project.getLeaderId()));
            
            request.getRequestDispatcher("/member/project/workspace.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showMyApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<ProjectMemberApplication> applications = projectDAO.getMyApplications(user.getId());
        request.setAttribute("applications", applications);
        request.getRequestDispatcher("/member/project/myApplications.jsp").forward(request, response);
    }

    private void showApplyForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isMember = projectDAO.isMember(id, user.getId());
            boolean hasApplication = projectDAO.hasPendingApplication(id, user.getId());
            
            if (isMember) {
                response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + id + "&msg=" + URLEncoder.encode("您已是项目成员", "UTF-8"));
                return;
            }
            
            if (hasApplication) {
                response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + id + "&msg=" + URLEncoder.encode("您已提交过申请", "UTF-8"));
                return;
            }

            request.setAttribute("project", project);
            request.getRequestDispatcher("/member/project/apply.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showMemberApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isAdmin = user.getId().equals(project.getAdminId()) || user.getId().equals(project.getLeaderId());
            if (!isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<ProjectMemberApplication> applications = projectDAO.getMemberApplications(id, null);
            request.setAttribute("project", project);
            request.setAttribute("applications", applications);
            request.getRequestDispatcher("/member/project/memberApplications.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showPlanManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isAdmin = user.getId().equals(project.getAdminId()) || user.getId().equals(project.getLeaderId());
            if (!isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<ProjectPlan> plans = projectDAO.getPlans(id);
            request.setAttribute("project", project);
            request.setAttribute("plans", plans);
            request.getRequestDispatcher("/member/project/plan.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showProgressManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isMember = projectDAO.isMember(id, user.getId());
            if (!isMember) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<ProjectPlan> plans = projectDAO.getPlans(id);
            List<ProjectProgress> progressList = projectDAO.getProgressList(id);
            request.setAttribute("project", project);
            request.setAttribute("plans", plans);
            request.setAttribute("progressList", progressList);
            request.getRequestDispatcher("/member/project/progress.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showProjectHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Project project = projectDAO.findById(id);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean isMember = projectDAO.isMember(id, user.getId());
            if (!isMember) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<ProjectHistory> historyList = projectDAO.getHistory(id);
            request.setAttribute("project", project);
            request.setAttribute("historyList", historyList);
            request.getRequestDispatcher("/member/project/history.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createProject(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String yearStr = request.getParameter("year");
        String expectedStartDateStr = request.getParameter("expectedStartDate");
        String expectedEndDateStr = request.getParameter("expectedEndDate");
        String budgetStr = request.getParameter("budget");
        String repoUrl = request.getParameter("repoUrl");
        String docUrl = request.getParameter("docUrl");

        if (name == null || name.isEmpty()) {
            request.setAttribute("error", "项目名称不能为空");
            request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
            request.getRequestDispatcher("/member/project/create.jsp").forward(request, response);
            return;
        }

        try {
            int year = yearStr != null ? Integer.parseInt(yearStr) : Calendar.getInstance().get(Calendar.YEAR);

            int projectCount = projectDAO.countProjectsByMemberAndYear(userId, year);
            if (projectCount >= 3) {
                request.setAttribute("error", "每年最多只能参与3个项目");
                request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
                request.getRequestDispatcher("/member/project/create.jsp").forward(request, response);
                return;
            }

            Project project = new Project();
            project.setName(name);
            project.setDescription(description);
            project.setCategory(category);
            project.setLeaderId(userId);
            project.setAdminId(userId);
            project.setYear(year);
            project.setStatus("PENDING");
            project.setRepoUrl(repoUrl);
            project.setDocUrl(docUrl);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (expectedStartDateStr != null && !expectedStartDateStr.isEmpty()) {
                project.setExpectedStartDate(sdf.parse(expectedStartDateStr));
            }
            if (expectedEndDateStr != null && !expectedEndDateStr.isEmpty()) {
                project.setExpectedEndDate(sdf.parse(expectedEndDateStr));
            }
            if (budgetStr != null && !budgetStr.isEmpty()) {
                project.setBudget(new BigDecimal(budgetStr));
            }

            if (projectDAO.insert(project)) {
                projectDAO.addMember(project.getId(), userId, "ADMIN");
                UserDAO userDAO = new UserDAO();
                User operator = userDAO.findById(userId);
                projectDAO.addHistory(project.getId(), "PROJECT_APPLY", userId, operator != null ? operator.getName() : "未知", "成员申请创建项目", null, name);
                response.sendRedirect(request.getContextPath() + "/project?action=list&msg=" + URLEncoder.encode("项目申请已提交，等待管理员审批", "UTF-8"));
            } else {
                request.setAttribute("error", "创建失败");
                request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
                request.getRequestDispatcher("/member/project/create.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "创建失败: " + e.getMessage());
            request.setAttribute("categories", dictionaryDAO.findByType("PROJECT_TYPE"));
            request.getRequestDispatcher("/member/project/create.jsp").forward(request, response);
        }
    }

    private void saveProject(HttpServletRequest request, HttpServletResponse response, Integer adminId)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        String yearStr = request.getParameter("year");
        String status = request.getParameter("status");
        String adminIdStr = request.getParameter("adminId");
        String expectedStartDateStr = request.getParameter("expectedStartDate");
        String expectedEndDateStr = request.getParameter("expectedEndDate");
        String actualStartDateStr = request.getParameter("actualStartDate");
        String actualEndDateStr = request.getParameter("actualEndDate");
        String budgetStr = request.getParameter("budget");
        String repoUrl = request.getParameter("repoUrl");
        String docUrl = request.getParameter("docUrl");

        try {
            Project project;
            boolean isNew = (idStr == null || idStr.isEmpty());
            
            if (isNew) {
                project = new Project();
                project.setLeaderId(adminId);
                project.setStatus("APPROVED");
            } else {
                project = projectDAO.findById(Integer.parseInt(idStr));
            }

            project.setName(name);
            project.setDescription(description);
            project.setCategory(category);
            project.setYear(Integer.parseInt(yearStr));
            project.setStatus(status);

            if (adminIdStr != null && !adminIdStr.isEmpty()) {
                project.setAdminId(Integer.parseInt(adminIdStr));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (expectedStartDateStr != null && !expectedStartDateStr.isEmpty()) {
                project.setExpectedStartDate(sdf.parse(expectedStartDateStr));
            }
            if (expectedEndDateStr != null && !expectedEndDateStr.isEmpty()) {
                project.setExpectedEndDate(sdf.parse(expectedEndDateStr));
            }
            if (actualStartDateStr != null && !actualStartDateStr.isEmpty()) {
                project.setActualStartDate(sdf.parse(actualStartDateStr));
            }
            if (actualEndDateStr != null && !actualEndDateStr.isEmpty()) {
                project.setActualEndDate(sdf.parse(actualEndDateStr));
            }
            if (budgetStr != null && !budgetStr.isEmpty()) {
                project.setBudget(new BigDecimal(budgetStr));
            }
            project.setRepoUrl(repoUrl);
            project.setDocUrl(docUrl);

            if (isNew) {
                projectDAO.insert(project);
                if (project.getAdminId() != null) {
                    projectDAO.addMember(project.getId(), project.getAdminId(), "ADMIN");
                }
            } else {
                projectDAO.update(project);
            }
            
            response.sendRedirect(request.getContextPath() + "/project?action=list&msg=" + URLEncoder.encode("保存成功", "UTF-8"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list&msg=" + URLEncoder.encode("保存失败: " + e.getMessage(), "UTF-8"));
        }
    }

    private void applyMember(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String reason = request.getParameter("reason");

        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer projectId = Integer.parseInt(idStr);
            Project project = projectDAO.findById(projectId);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (projectDAO.isMember(projectId, userId)) {
                response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + projectId + "&msg=" + URLEncoder.encode("您已是项目成员", "UTF-8"));
                return;
            }

            if (projectDAO.hasPendingApplication(projectId, userId)) {
                response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + projectId + "&msg=" + URLEncoder.encode("您已提交过申请", "UTF-8"));
                return;
            }

            UserDAO userDAO = new UserDAO();
            User applicant = userDAO.findById(userId);
            
            if (projectDAO.applyMember(projectId, userId, reason)) {
                projectDAO.addHistory(projectId, "MEMBER_APPLY", userId, applicant != null ? applicant.getName() : "未知", "成员申请加入项目", null, applicant != null ? applicant.getName() : "未知");
                response.sendRedirect(request.getContextPath() + "/project?action=myApplications&msg=" + URLEncoder.encode("申请已提交，等待项目管理员审批", "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + projectId + "&msg=" + URLEncoder.encode("申请失败", "UTF-8"));
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/project?action=detail&id=" + idStr + "&msg=" + URLEncoder.encode("申请失败: " + e.getMessage(), "UTF-8"));
        }
    }

    private void approveProject(HttpServletRequest request, HttpServletResponse response, Integer adminId)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                Project project = projectDAO.findById(id);
                
                if (project != null) {
                    projectDAO.approve(id, adminId);
                    
                    UserDAO userDAO = new UserDAO();
                    User approver = userDAO.findById(adminId);
                    projectDAO.addHistory(id, "PROJECT_APPROVE", adminId, approver != null ? approver.getName() : "管理员", "项目审批通过", "PENDING", "APPROVED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=list");
    }

    private void rejectProject(HttpServletRequest request, HttpServletResponse response, Integer adminId)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                Project project = projectDAO.findById(id);
                
                if (project != null) {
                    projectDAO.reject(id, adminId);
                    
                    UserDAO userDAO = new UserDAO();
                    User approver = userDAO.findById(adminId);
                    projectDAO.addHistory(id, "PROJECT_REJECT", adminId, approver != null ? approver.getName() : "管理员", "项目审批驳回", "PENDING", "REJECTED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=list");
    }

    private void approveMemberApplication(HttpServletRequest request, HttpServletResponse response, Integer adminId)
            throws ServletException, IOException {
        String applicationIdStr = request.getParameter("applicationId");
        String projectIdStr = request.getParameter("projectId");
        
        if (applicationIdStr != null) {
            try {
                Integer applicationId = Integer.parseInt(applicationIdStr);
                ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
                
                if (app != null && projectDAO.approveMemberApplication(applicationId, adminId)) {
                    UserDAO userDAO = new UserDAO();
                    User approver = userDAO.findById(adminId);
                    projectDAO.addHistory(app.getProjectId(), "MEMBER_APPROVE", adminId, approver != null ? approver.getName() : "管理员", "成员审批通过", "PENDING", "CONFIRMED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (projectIdStr != null) {
            response.sendRedirect(request.getContextPath() + "/project?action=memberApplications&id=" + projectIdStr);
        } else {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        }
    }

    private void rejectMemberApplication(HttpServletRequest request, HttpServletResponse response, Integer adminId)
            throws ServletException, IOException {
        String applicationIdStr = request.getParameter("applicationId");
        String projectIdStr = request.getParameter("projectId");
        String reason = request.getParameter("reason");
        
        if (applicationIdStr != null) {
            try {
                Integer applicationId = Integer.parseInt(applicationIdStr);
                ProjectMemberApplication app = projectDAO.getMemberApplicationById(applicationId);
                
                if (app != null && projectDAO.rejectMemberApplication(applicationId, adminId, reason)) {
                    UserDAO userDAO = new UserDAO();
                    User approver = userDAO.findById(adminId);
                    projectDAO.addHistory(app.getProjectId(), "MEMBER_REJECT", adminId, approver != null ? approver.getName() : "管理员", "成员审批驳回: " + reason, "PENDING", "REJECTED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (projectIdStr != null) {
            response.sendRedirect(request.getContextPath() + "/project?action=memberApplications&id=" + projectIdStr);
        } else {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        }
    }

    private void addProjectLabel(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String labelCode = request.getParameter("labelCode");
        
        if (projectIdStr != null && labelCode != null) {
            try {
                Integer projectId = Integer.parseInt(projectIdStr);
                projectDAO.addLabel(projectId, labelCode);
                
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(userId);
                projectDAO.addHistory(projectId, "PROJECT_LABEL_ADD", userId, user != null ? user.getName() : "未知", "添加项目标签", null, labelCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void removeProjectLabel(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String labelCode = request.getParameter("labelCode");
        
        if (projectIdStr != null && labelCode != null) {
            try {
                Integer projectId = Integer.parseInt(projectIdStr);
                projectDAO.removeLabel(projectId, labelCode);
                
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(userId);
                projectDAO.addHistory(projectId, "PROJECT_LABEL_REMOVE", userId, user != null ? user.getName() : "未知", "移除项目标签", labelCode, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void addProjectPlan(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        if (projectIdStr != null && title != null && !title.isEmpty()) {
            try {
                Integer projectId = Integer.parseInt(projectIdStr);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                
                ProjectPlan plan = new ProjectPlan();
                plan.setProjectId(projectId);
                plan.setTitle(title);
                plan.setDescription(description);
                plan.setStartDate(sdf.parse(startDateStr));
                plan.setEndDate(sdf.parse(endDateStr));
                
                projectDAO.addPlan(plan);
                
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(userId);
                projectDAO.addHistory(projectId, "PROJECT_INFO_UPDATE", userId, user != null ? user.getName() : "未知", "添加项目计划: " + title, null, title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void updateProjectPlan(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void deleteProjectPlan(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String planIdStr = request.getParameter("planId");
        
        if (planIdStr != null) {
            try {
                projectDAO.deletePlan(Integer.parseInt(planIdStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void addProjectProgress(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String planIdStr = request.getParameter("planId");
        String completionRateStr = request.getParameter("completionRate");
        
        if (projectIdStr != null && title != null && !title.isEmpty()) {
            try {
                Integer projectId = Integer.parseInt(projectIdStr);
                
                ProjectProgress progress = new ProjectProgress();
                progress.setProjectId(projectId);
                progress.setTitle(title);
                progress.setDescription(description);
                progress.setCreatedBy(userId);
                
                if (planIdStr != null && !planIdStr.isEmpty()) {
                    progress.setPlanId(Integer.parseInt(planIdStr));
                }
                
                if (completionRateStr != null && !completionRateStr.isEmpty()) {
                    progress.setCompletionRate(Integer.parseInt(completionRateStr));
                } else {
                    progress.setCompletionRate(0);
                }
                
                projectDAO.addProgress(progress);
                
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(userId);
                projectDAO.addHistory(projectId, "PROJECT_INFO_UPDATE", userId, user != null ? user.getName() : "未知", "添加项目进度: " + title, null, title + " (" + progress.getCompletionRate() + "%)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void transferProjectAdmin(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String newAdminIdStr = request.getParameter("newAdminId");
        
        if (projectIdStr != null && newAdminIdStr != null) {
            try {
                Integer projectId = Integer.parseInt(projectIdStr);
                Integer newAdminId = Integer.parseInt(newAdminIdStr);
                
                Project project = projectDAO.findById(projectId);
                Integer oldAdminId = project.getAdminId();
                
                if (projectDAO.transferAdmin(projectId, newAdminId)) {
                    UserDAO userDAO = new UserDAO();
                    User transferor = userDAO.findById(userId);
                    User newAdmin = userDAO.findById(newAdminId);
                    projectDAO.addHistory(projectId, "PROJECT_TRANSFER", userId, transferor != null ? transferor.getName() : "未知", 
                        "项目管理员转移", oldAdminId != null ? String.valueOf(oldAdminId) : "无", newAdmin != null ? newAdmin.getName() : "未知");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void uploadProjectImage(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        
        try {
            Part imagePart = request.getPart("imageFile");
            
            if (imagePart != null && imagePart.getSize() > 0) {
                Integer projectId = Integer.parseInt(projectIdStr);
                String fileName = imagePart.getSubmittedFileName();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                
                if (!ext.matches("jpg|jpeg|png|gif|bmp|webp")) {
                    response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectId + "&error=" + URLEncoder.encode("只允许上传图片文件", "UTF-8"));
                    return;
                }
                
                if (imagePart.getSize() > 2 * 1024 * 1024) {
                    response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectId + "&error=" + URLEncoder.encode("图片大小不能超过2MB", "UTF-8"));
                    return;
                }
                
                String savePath = request.getServletContext().getRealPath("/localstorage/projects/" + projectId + "/images");
                java.io.File dir = new java.io.File(savePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                String newFileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + "." + ext;
                java.io.File uploadedFile = new java.io.File(savePath, newFileName);
                imagePart.write(uploadedFile.getAbsolutePath());
                
                java.sql.Connection conn = null;
                java.sql.PreparedStatement pstmt = null;
                try {
                    conn = util.DBUtil.getConnection();
                    String sql = "INSERT INTO file_storage (stored_name, original_name, file_path, file_size, file_type, status, created_at) VALUES (?, ?, ?, ?, ?, 1, NOW())";
                    pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, newFileName);
                    pstmt.setString(2, fileName);
                    pstmt.setString(3, "localstorage/projects/" + projectId + "/images/" + newFileName);
                    pstmt.setLong(4, imagePart.getSize());
                    pstmt.setString(5, "image/" + ext);
                    pstmt.executeUpdate();
                    
                    java.sql.ResultSet rs = pstmt.getGeneratedKeys();
                    int fileId = 0;
                    if (rs.next()) {
                        fileId = rs.getInt(1);
                    }
                    rs.close();
                    pstmt.close();
                    
                    sql = "INSERT INTO project_image (project_id, file_id, created_at) VALUES (?, ?, NOW())";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, projectId);
                    pstmt.setInt(2, fileId);
                    pstmt.executeUpdate();
                    
                    UserDAO userDAO = new UserDAO();
                    User user = userDAO.findById(userId);
                    projectDAO.addHistory(projectId, "PROJECT_IMAGE_ADD", userId, user != null ? user.getName() : "未知", "添加项目图片", null, fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void uploadProjectFile(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        
        try {
            Part filePart = request.getPart("docFile");
            
            if (filePart != null && filePart.getSize() > 0) {
                Integer projectId = Integer.parseInt(projectIdStr);
                String fileName = filePart.getSubmittedFileName();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                
                String[] forbiddenExts = {"exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "jsp", "asp", "php", "cgi", "htaccess"};
                for (String forbidden : forbiddenExts) {
                    if (ext.equals(forbidden)) {
                        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectId + "&error=" + URLEncoder.encode("不允许上传可执行文件", "UTF-8"));
                        return;
                    }
                }
                
                if (filePart.getSize() > 50 * 1024 * 1024) {
                    response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectId + "&error=" + URLEncoder.encode("文件大小不能超过50MB", "UTF-8"));
                    return;
                }
                
                String savePath = request.getServletContext().getRealPath("/localstorage/projects/" + projectId + "/files");
                java.io.File dir = new java.io.File(savePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                String newFileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + "." + ext;
                java.io.File uploadedFile = new java.io.File(savePath, newFileName);
                filePart.write(uploadedFile.getAbsolutePath());
                
                java.sql.Connection conn = null;
                java.sql.PreparedStatement pstmt = null;
                try {
                    conn = util.DBUtil.getConnection();
                    String sql = "INSERT INTO file_storage (stored_name, original_name, file_path, file_size, file_type, status, created_at) VALUES (?, ?, ?, ?, ?, 1, NOW())";
                    pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, newFileName);
                    pstmt.setString(2, fileName);
                    pstmt.setString(3, "localstorage/projects/" + projectId + "/files/" + newFileName);
                    pstmt.setLong(4, filePart.getSize());
                    pstmt.setString(5, "application/octet-stream");
                    pstmt.executeUpdate();
                    
                    java.sql.ResultSet rs = pstmt.getGeneratedKeys();
                    int fileId = 0;
                    if (rs.next()) {
                        fileId = rs.getInt(1);
                    }
                    rs.close();
                    pstmt.close();
                    
                    sql = "INSERT INTO project_file (project_id, file_id, file_type, created_at) VALUES (?, ?, ?, NOW())";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, projectId);
                    pstmt.setInt(2, fileId);
                    pstmt.setString(3, ext);
                    pstmt.executeUpdate();
                    
                    UserDAO userDAO = new UserDAO();
                    User user = userDAO.findById(userId);
                    projectDAO.addHistory(projectId, "PROJECT_FILE_ADD", userId, user != null ? user.getName() : "未知", "添加项目文件", null, fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void downloadProjectFile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileIdStr = request.getParameter("fileId");
        String projectIdStr = request.getParameter("projectId");
        
        if (fileIdStr != null) {
            try {
                int fileId = Integer.parseInt(fileIdStr);
                java.sql.Connection conn = null;
                java.sql.PreparedStatement pstmt = null;
                java.sql.ResultSet rs = null;
                try {
                    conn = util.DBUtil.getConnection();
                    String sql = "SELECT * FROM file_storage WHERE id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, fileId);
                    rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        String filePath = request.getServletContext().getRealPath("/") + rs.getString("file_path");
                        String originalName = rs.getString("original_name");
                        java.io.File file = new java.io.File(filePath);
                        
                        if (file.exists()) {
                            response.setContentType("application/octet-stream");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(originalName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
                            java.io.FileInputStream fis = new java.io.FileInputStream(file);
                            java.io.OutputStream os = response.getOutputStream();
                            byte[] buffer = new byte[4096];
                            int bytesRead = -1;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            fis.close();
                            os.flush();
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }

    private void deleteProjectFile(HttpServletRequest request, HttpServletResponse response, Integer userId)
            throws ServletException, IOException {
        String projectIdStr = request.getParameter("projectId");
        String fileIdStr = request.getParameter("fileId");
        
        if (fileIdStr != null && projectIdStr != null) {
            try {
                int fileId = Integer.parseInt(fileIdStr);
                int projectId = Integer.parseInt(projectIdStr);
                
                java.sql.Connection conn = null;
                java.sql.PreparedStatement pstmt = null;
                try {
                    conn = util.DBUtil.getConnection();
                    
                    String sql = "SELECT fs.file_path FROM file_storage fs WHERE fs.id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, fileId);
                    java.sql.ResultSet rs = pstmt.executeQuery();
                    
                    String filePath = null;
                    if (rs.next()) {
                        filePath = rs.getString("file_path");
                    }
                    rs.close();
                    pstmt.close();
                    
                    if (filePath != null) {
                        java.io.File file = new java.io.File(request.getServletContext().getRealPath("/") + filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    
                    pstmt = conn.prepareStatement("DELETE FROM project_image WHERE file_id = ? AND project_id = ?");
                    pstmt.setInt(1, fileId);
                    pstmt.setInt(2, projectId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    pstmt = conn.prepareStatement("DELETE FROM project_file WHERE file_id = ? AND project_id = ?");
                    pstmt.setInt(1, fileId);
                    pstmt.setInt(2, projectId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    pstmt = conn.prepareStatement("UPDATE file_storage SET status = 0 WHERE id = ?");
                    pstmt.setInt(1, fileId);
                    pstmt.executeUpdate();
                    
                    UserDAO userDAO = new UserDAO();
                    User user = userDAO.findById(userId);
                    projectDAO.addHistory(projectId, "PROJECT_FILE_DELETE", userId, user != null ? user.getName() : "未知", "删除项目文件", fileIdStr, null);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/project?action=workspace&id=" + projectIdStr);
    }
}
