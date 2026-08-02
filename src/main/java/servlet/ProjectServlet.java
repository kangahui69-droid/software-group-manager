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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
                listProjects(request, response, user);  // 根据用户角色转发到不同页面
                break;
            case "detail":
                getProjectDetail(request, response, user);
                break;
            case "edit":
                editProject(request, response, user);  // 编辑项目表单
                break;
            case "myProjects":
                getMyProjects(request, response, user);  // 用户端"我的项目"（我创建的项目）
                break;
            case "myApplications":
                getMyApplications(request, response, user);  // 我的申请记录（我申请加入的项目）
                break;
            case "manage":
                listProjects(request, response, user);  // 管理端项目管理
                break;
            case "createForm":
                showCreateForm(request, response, user);
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

        // 根据用户角色决定转发到哪个页面
        if (user != null && "ADMIN".equals(user.getRole())) {
            request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/jsp/member/project/list.jsp").forward(request, response);
        }
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
                // Service返回的是Map，需要分解为各个属性
                java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
                request.setAttribute("project", data.get("project"));
                request.setAttribute("members", data.get("members"));
                request.setAttribute("isMember", data.get("isMember"));
                request.setAttribute("hasApplication", data.get("hasApplication"));
                request.setAttribute("labels", data.get("labels"));
                request.setAttribute("plans", data.get("plans"));
                request.setAttribute("progress", data.get("progress"));
                request.setAttribute("history", data.get("history"));
            }
            // 根据用户角色决定转发到哪个页面
            if (user != null && "ADMIN".equals(user.getRole())) {
                request.getRequestDispatcher("/jsp/admin/project/detail.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/jsp/member/project/detail.jsp").forward(request, response);
            }
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
        request.getRequestDispatcher("/jsp/member/project/list.jsp").forward(request, response);
    }

    private void editProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.getProjectDetail(id, user != null ? user.getId() : null);
            if (result.isSuccess()) {
                request.setAttribute("project", result.getData());
            }
            request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        }
    }

    private void getMyApplications(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        // TODO: 调用 projectService.getMyApplications(user.getId()) 获取申请记录
        request.getRequestDispatcher("/jsp/member/project/myApplications.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        request.getRequestDispatcher("/jsp/member/project/create.jsp").forward(request, response);
    }

    private void createProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "项目名称不能为空");
            request.getRequestDispatcher("/jsp/member/project/create.jsp").forward(request, response);
            return;
        }

        if (name.length() > 100) {
            request.setAttribute("error", "项目名称不能超过100个字符");
            request.getRequestDispatcher("/jsp/member/project/create.jsp").forward(request, response);
            return;
        }

        try {
            ProjectDTO dto = extractProjectFromRequest(request);
            dto.setName(name);

            Result result = projectService.createProject(dto, user.getId());
            if (result.isSuccess()) {
                // 创建成功后跳转到"我的项目"页面
                response.sendRedirect(request.getContextPath() + "/project?action=myApplications");
                return;
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "创建失败: " + e.getMessage());
        }
        request.getRequestDispatcher("/jsp/member/project/create.jsp").forward(request, response);
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            ProjectDTO dto = extractProjectFromRequest(request);

            Result result = projectService.updateProject(id, dto, user.getId());
            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/project?action=list");
            } else {
                request.setAttribute("error", result.getMessage());
                request.setAttribute("project", dto);
                request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "更新失败: " + e.getMessage());
            request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.deleteProject(id, user.getId());
            // 删除后直接跳转回列表页，不留在此页面
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
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
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.approveProject(id, user.getId());
            // 审批后跳转回列表页
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
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
            response.sendRedirect(request.getContextPath() + "/project?action=list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            Result result = projectService.rejectProject(id, reason, user.getId());
            // 驳回后跳转回列表页
            response.sendRedirect(request.getContextPath() + "/project?action=list");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/project?action=list");
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
