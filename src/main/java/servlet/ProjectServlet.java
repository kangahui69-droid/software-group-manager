package servlet;

import dao.DictionaryDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dto.PlanDTO;
import dto.ProgressDTO;
import dto.ProjectDTO;
import dto.ProjectFilterDTO;
import model.Project;
import model.User;
import service.ProjectService;
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
 * 项目Servlet - 4.7 Servlet改造
 * 调用ProjectService处理业务逻辑
 */
public class ProjectServlet extends HttpServlet {

    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        this.projectService = new ProjectService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        HttpSession session = request.getSession(false);
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        switch (action) {
            case "list":
                listProjects(request, response, user);
                break;
            case "detail":
                getProjectDetail(request, response, user);
                break;
            case "myProjects":
                getMyProjects(request, response, user);
                break;
            default:
                listProjects(request, response, user);
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
                createProject(request, response, user);
                break;
            case "update":
                updateProject(request, response, user);
                break;
            case "delete":
                deleteProject(request, response, user);
                break;
            case "approve":
                approveProject(request, response, user);
                break;
            case "reject":
                rejectProject(request, response, user);
                break;
            case "applyJoin":
                applyJoin(request, response, user);
                break;
            case "addPlan":
                addPlan(request, response, user);
                break;
            case "addProgress":
                addProgress(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void listProjects(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String yearStr = request.getParameter("year");

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setKeyword(keyword);
        filter.setStatus(status);
        if (yearStr != null && !yearStr.isEmpty()) {
            filter.setYear(Integer.parseInt(yearStr));
        }

        Result result = projectService.listProjects(filter, 1, 20);
        if (result.isSuccess()) {
            request.setAttribute("projects", result.getData());
        }
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("year", yearStr);
        request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
    }

    private void getProjectDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.getProjectDetail(id, user != null ? user.getId() : null);
            if (result.isSuccess()) {
                request.setAttribute("project", result.getData());
            }
            request.getRequestDispatcher("/jsp/admin/project/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void getMyProjects(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Result result = projectService.getMyProjects(user.getId(), 1, 20);
        if (result.isSuccess()) {
            request.setAttribute("projects", result.getData());
        }
        request.getRequestDispatcher("/jsp/project/myProjects.jsp").forward(request, response);
    }

    private void createProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "项目名称不能为空");
            request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
            return;
        }

        if (name.length() > 100) {
            request.setAttribute("error", "项目名称不能超过100个字符");
            request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
            return;
        }

        try {
            ProjectDTO dto = extractProjectFromRequest(request);
            dto.setName(name);

            Result result = projectService.createProject(dto, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "项目创建成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "创建失败: " + e.getMessage());
        }
        request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            ProjectDTO dto = extractProjectFromRequest(request);

            Result result = projectService.updateProject(id, dto, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "项目更新成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "更新失败");
        }
    }

    private void deleteProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.deleteProject(id, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "项目删除成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "删除失败");
        }
    }

    private void approveProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.approveProject(id, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "项目审批通过");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "审批失败");
        }
    }

    private void rejectProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        String reason = request.getParameter("reason");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.rejectProject(id, reason, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "项目已驳回");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "驳回失败");
        }
    }

    private void applyJoin(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String projectIdStr = request.getParameter("projectId");
        String reason = request.getParameter("reason");
        if (projectIdStr == null || projectIdStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer projectId = Integer.parseInt(projectIdStr);
            Result result = projectService.applyMember(projectId, user.getId(), reason);
            if (result.isSuccess()) {
                request.setAttribute("success", "申请已提交");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "申请失败");
        }
    }

    private void addPlan(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String projectIdStr = request.getParameter("projectId");
        String content = request.getParameter("content");
        if (projectIdStr == null || projectIdStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer projectId = Integer.parseInt(projectIdStr);
            PlanDTO dto = new PlanDTO();
            dto.setTitle(content);
            dto.setDescription(request.getParameter("description"));

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (startDate != null && !startDate.isEmpty()) {
                dto.setStartDate(sdf.parse(startDate));
            }
            if (endDate != null && !endDate.isEmpty()) {
                dto.setEndDate(sdf.parse(endDate));
            }

            Result result = projectService.addPlan(projectId, dto, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "计划添加成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "添加失败");
        }
    }

    private void addProgress(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String projectIdStr = request.getParameter("projectId");
        String content = request.getParameter("content");
        if (projectIdStr == null || projectIdStr.isEmpty()) {
            request.setAttribute("error", "项目ID不能为空");
            return;
        }

        try {
            Integer projectId = Integer.parseInt(projectIdStr);
            ProgressDTO dto = new ProgressDTO();
            dto.setTitle(content);
            dto.setDescription(request.getParameter("description"));

            String completionRateStr = request.getParameter("completionRate");
            if (completionRateStr != null && !completionRateStr.isEmpty()) {
                dto.setCompletionRate(Integer.parseInt(completionRateStr));
            } else {
                dto.setCompletionRate(0);
            }

            Result result = projectService.addProgress(projectId, dto, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "进度添加成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "添加失败");
        }
    }

    private ProjectDTO extractProjectFromRequest(HttpServletRequest request) throws Exception {
        ProjectDTO dto = new ProjectDTO();
        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));
        dto.setCategory(request.getParameter("category"));

        String yearStr = request.getParameter("year");
        if (yearStr != null && !yearStr.isEmpty()) {
            dto.setYear(Integer.parseInt(yearStr));
        }

        String budgetStr = request.getParameter("budget");
        if (budgetStr != null && !budgetStr.isEmpty()) {
            dto.setBudget(new java.math.BigDecimal(budgetStr));
        }

        dto.setRepoUrl(request.getParameter("repoUrl"));
        dto.setDocUrl(request.getParameter("docUrl"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = request.getParameter("expectedStartDate");
        String endDate = request.getParameter("expectedEndDate");
        if (startDate != null && !startDate.isEmpty()) {
            dto.setExpectedStartDate(sdf.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            dto.setExpectedEndDate(sdf.parse(endDate));
        }

        return dto;
    }
}
