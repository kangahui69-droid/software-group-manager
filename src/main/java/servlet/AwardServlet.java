package servlet;

import com.google.gson.Gson;
import dao.AwardDAO;
import dao.AwardImageDAO;
import dao.FileStorageDAO;
import dao.UserDAO;
import dto.AwardDTO;
import model.Award;
import model.User;
import service.AwardService;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 奖项Servlet - 4.7 Servlet改造
 * 调用AwardService处理业务逻辑
 */
@MultipartConfig
public class AwardServlet extends HttpServlet {

    private AwardService awardService;

    @Override
    public void init() throws ServletException {
        AwardDAO awardDAO = new AwardDAO();
        AwardImageDAO awardImageDAO = new AwardImageDAO();
        UserDAO userDAO = new UserDAO();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        this.awardService = new AwardService(awardDAO, awardImageDAO, userDAO, fileStorageDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // 如果action为空，尝试从pathInfo获取
        if (action == null || action.isEmpty()) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/")) {
                action = pathInfo.substring(1);
            }
        }

        // filter 公开查询，不需要登录
        if ("filter".equals(action)) {
            filterAwards(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                listAwards(request, response, user);
                break;
            case "myAwards":
                getMyAwards(request, response, user);
                break;
            case "statistics":
                getStatistics(request, response, user);
                break;
            case "submit":
                showSubmitForm(request, response, user);
                break;
            case "approveList":
                listAwardsForApproval(request, response, user);
                break;
            case "approveDetail":
                viewAwardForApproval(request, response, user);
                break;
            case "detail":
                viewAwardDetail(request, response, user);
                break;
            case "edit":
                showEditForm(request, response, user);
                break;
            case "delete":
                deleteAward(request, response, user);
                break;
            default:
                listAwards(request, response, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        switch (action) {
            case "submit":
                submitAward(request, response, user);
                break;
            case "update":
                updateAward(request, response, user);
                break;
            case "approve":
                approveAward(request, response, user);
                break;
            case "reject":
                rejectAward(request, response, user);
                break;
            case "addImage":
                addAwardImage(request, response, user);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void filterAwards(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        String competitionLevel = request.getParameter("competitionLevel");
        String awardLevel = request.getParameter("awardLevel");

        Result result = awardService.getMyAwards(((User) request.getSession().getAttribute("user")).getId());
        List<Award> awards = result.isSuccess() ? (List<Award>) result.getData() : List.of();

        // 根据筛选条件过滤
        List<Award> filteredAwards = awards.stream()
            .filter(a -> {
                if (status != null && !status.isEmpty() && !status.equals(a.getAwardStatus())) {
                    return false;
                }
                if (competitionLevel != null && !competitionLevel.isEmpty()) {
                    if (a.getCompetitionLevel() == null || !competitionLevel.equals(String.valueOf(a.getCompetitionLevel()))) {
                        return false;
                    }
                }
                if (awardLevel != null && !awardLevel.isEmpty()) {
                    if (a.getAwardLevel() == null || !awardLevel.equals(String.valueOf(a.getAwardLevel()))) {
                        return false;
                    }
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(filteredAwards));
    }

    private void listAwards(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        // 管理员显示审批页面
        if ("ADMIN".equals(user.getRole())) {
            listAwardsForApproval(request, response, user);
            return;
        }

        // 普通用户显示我的奖项页面
        getMyAwards(request, response, user);
    }

    private void getMyAwards(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Result result = awardService.getMyAwards(user.getId());
        if (result.isSuccess()) {
            request.setAttribute("awards", result.getData());
        }
        // 获取统计数据
        Result statsResult = awardService.getAwardStatistics(user.getId());
        if (statsResult.isSuccess()) {
            request.setAttribute("awardStats", statsResult.getData());
        }
        // 获取字典数据
        Result awardTypesResult = awardService.getAwardTypes();
        if (awardTypesResult.isSuccess()) {
            request.setAttribute("awardTypes", awardTypesResult.getData());
        }
        Result awardLevelsResult = awardService.getAwardLevels();
        if (awardLevelsResult.isSuccess()) {
            request.setAttribute("awardLevels", awardLevelsResult.getData());
        }
        Result competitionLevelsResult = awardService.getCompetitionLevels();
        if (competitionLevelsResult.isSuccess()) {
            request.setAttribute("competitionLevels", competitionLevelsResult.getData());
        }
        request.getRequestDispatcher("/jsp/member/award/list.jsp").forward(request, response);
    }

    private void getStatistics(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Result result = awardService.getAwardStatistics(user.getId());
        if (result.isSuccess()) {
            request.setAttribute("statistics", result.getData());
        }
        request.getRequestDispatcher("/jsp/award/statistics.jsp").forward(request, response);
    }

    private void showSubmitForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        loadDictionaryData(request);
        request.getRequestDispatcher("/jsp/member/award/submit.jsp").forward(request, response);
    }

    private void submitAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String competition = request.getParameter("competition");
        String competitionTime = request.getParameter("competitionTime");
        String awardLevel = request.getParameter("awardLevel");
        String awardType = request.getParameter("awardType");
        String awardCategory = request.getParameter("awardCategory");
        String competitionLevel = request.getParameter("competitionLevel");
        String teamName = request.getParameter("teamName");
        String competitionLocation = request.getParameter("competitionLocation");
        String competitionSession = request.getParameter("competitionSession");

        if (competition == null || competition.trim().isEmpty()) {
            request.setAttribute("error", "竞赛名称不能为空");
            loadDictionaryData(request);
            request.getRequestDispatcher("/jsp/member/award/submit.jsp").forward(request, response);
            return;
        }

        if (competitionTime != null && !competitionTime.matches("\\d{4}-\\d{2}-\\d{2}")) {
            request.setAttribute("error", "日期格式错误");
            loadDictionaryData(request);
            request.getRequestDispatcher("/jsp/member/award/submit.jsp").forward(request, response);
            return;
        }

        try {
            AwardDTO dto = new AwardDTO();
            dto.setCompetition(competition);
            dto.setCompetitionTime(competitionTime);
            if (awardLevel != null && !awardLevel.isEmpty()) {
                dto.setAwardLevel(Integer.parseInt(awardLevel));
            }
            if (awardType != null && !awardType.isEmpty()) {
                dto.setAwardType(Integer.parseInt(awardType));
            }
            if (awardCategory != null && !awardCategory.isEmpty()) {
                dto.setAwardCategory(Integer.parseInt(awardCategory));
            }
            if (competitionLevel != null && !competitionLevel.isEmpty()) {
                dto.setCompetitionLevel(Integer.parseInt(competitionLevel));
            }
            dto.setTeamName(teamName);
            dto.setCompetitionLocation(competitionLocation);
            dto.setCompetitionSession(competitionSession);

            // 提取上传的图片文件
            Object[] images = extractAwardImages(request);

            Result result = awardService.submitAward(dto, user.getId(), images);
            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/award?action=myAwards&message=submit_success");
                return;
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "提交失败: " + e.getMessage());
        }
        loadDictionaryData(request);
        request.getRequestDispatcher("/jsp/member/award/submit.jsp").forward(request, response);
    }

    /**
     * 从multipart请求中提取奖项图片（直接使用Part对象）
     */
    private Object[] extractAwardImages(HttpServletRequest request) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();
        List<Object> imageList = new ArrayList<>();

        for (Part part : parts) {
            String partName = part.getName();
            if ("awardImages".equals(partName) && part.getContentType() != null && part.getSize() > 0) {
                // AwardService.extractImageInfo 使用反射调用 Part 的方法
                // 直接传入 Part 对象即可
                imageList.add(part);
            }
        }

        return imageList.isEmpty() ? null : imageList.toArray();
    }

    private void loadDictionaryData(HttpServletRequest request) {
        Result awardTypesResult = awardService.getAwardTypes();
        if (awardTypesResult.isSuccess()) {
            request.setAttribute("awardTypes", awardTypesResult.getData());
        }
        Result awardCategoriesResult = awardService.getAwardCategories();
        if (awardCategoriesResult.isSuccess()) {
            request.setAttribute("awardCategories", awardCategoriesResult.getData());
        }
        Result awardLevelsResult = awardService.getAwardLevels();
        if (awardLevelsResult.isSuccess()) {
            request.setAttribute("awardLevels", awardLevelsResult.getData());
        }
        Result competitionLevelsResult = awardService.getCompetitionLevels();
        if (competitionLevelsResult.isSuccess()) {
            request.setAttribute("competitionLevels", competitionLevelsResult.getData());
        }
    }

    private void approveAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.approveAward(id, user.getId());
        } catch (Exception e) {
            // 忽略错误
        }
        response.sendRedirect(request.getContextPath() + "/award?action=approveList");
    }

    private void rejectAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        String reason = request.getParameter("reason");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.rejectAward(id, reason, user.getId());
        } catch (Exception e) {
            // 忽略错误
        }
        response.sendRedirect(request.getContextPath() + "/award?action=approveList");
    }

    private void addAwardImage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            request.setAttribute("error", "奖项ID不能为空");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            // 提取上传的图片文件
            Object file = extractSingleAwardImage(request);

            Result result = awardService.addAwardImage(id, file, user.getId());
            if (result.isSuccess()) {
                request.setAttribute("success", "图片添加成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "添加失败: " + e.getMessage());
        }
    }

    /**
     * 从multipart请求中提取单个奖项图片（直接使用Part对象）
     */
    private Object extractSingleAwardImage(HttpServletRequest request) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();

        for (Part part : parts) {
            String partName = part.getName();
            if ("awardImage".equals(partName) && part.getContentType() != null && part.getSize() > 0) {
                return part;
            }
        }

        // 兼容：也尝试 awardImages（复数形式）
        for (Part part : parts) {
            String partName = part.getName();
            if ("awardImages".equals(partName) && part.getContentType() != null && part.getSize() > 0) {
                return part;
            }
        }

        return null;
    }

    private void listAwardsForApproval(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        String awardType = request.getParameter("awardType");
        String awardCategory = request.getParameter("awardCategory");
        String awardLevel = request.getParameter("awardLevel");
        String competitionLevel = request.getParameter("competitionLevel");

        Result result = awardService.listAwardsForApproval(status, keyword, awardType, awardCategory, awardLevel, competitionLevel);
        if (result.isSuccess()) {
            request.setAttribute("awards", result.getData());
        }

        // 设置字典数据
        Result awardTypesResult = awardService.getAwardTypes();
        if (awardTypesResult.isSuccess()) {
            request.setAttribute("awardTypes", awardTypesResult.getData());
        }
        Result awardCategoriesResult = awardService.getAwardCategories();
        if (awardCategoriesResult.isSuccess()) {
            request.setAttribute("awardCategories", awardCategoriesResult.getData());
        }
        Result awardLevelsResult = awardService.getAwardLevels();
        if (awardLevelsResult.isSuccess()) {
            request.setAttribute("awardLevels", awardLevelsResult.getData());
        }
        Result competitionLevelsResult = awardService.getCompetitionLevels();
        if (competitionLevelsResult.isSuccess()) {
            request.setAttribute("competitionLevels", competitionLevelsResult.getData());
        }

        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/jsp/admin/award/approve.jsp").forward(request, response);
    }

    private void viewAwardForApproval(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.getAwardDetail(id);
            if (result.isSuccess()) {
                request.setAttribute("award", result.getData());
                // 获取奖项图片
                Result imagesResult = awardService.getAwardImages(id);
                if (imagesResult.isSuccess()) {
                    request.setAttribute("awardImages", imagesResult.getData());
                }
            }

            // 设置字典数据
            Result awardTypesResult = awardService.getAwardTypes();
            if (awardTypesResult.isSuccess()) {
                request.setAttribute("awardTypes", awardTypesResult.getData());
            }
            Result awardCategoriesResult = awardService.getAwardCategories();
            if (awardCategoriesResult.isSuccess()) {
                request.setAttribute("awardCategories", awardCategoriesResult.getData());
            }
            Result awardLevelsResult = awardService.getAwardLevels();
            if (awardLevelsResult.isSuccess()) {
                request.setAttribute("awardLevels", awardLevelsResult.getData());
            }
            Result competitionLevelsResult = awardService.getCompetitionLevels();
            if (competitionLevelsResult.isSuccess()) {
                request.setAttribute("competitionLevels", competitionLevelsResult.getData());
            }

            request.getRequestDispatcher("/jsp/admin/award/approve-detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
        }
    }

    private void viewAwardDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.getAwardDetail(id);
            if (result.isSuccess()) {
                request.setAttribute("award", result.getData());
                // 获取奖项图片
                Result imagesResult = awardService.getAwardImages(id);
                if (imagesResult.isSuccess()) {
                    request.setAttribute("awardImages", imagesResult.getData());
                }
            }

            // 设置字典数据
            Result awardTypesResult = awardService.getAwardTypes();
            if (awardTypesResult.isSuccess()) {
                request.setAttribute("awardTypes", awardTypesResult.getData());
            }
            Result awardCategoriesResult = awardService.getAwardCategories();
            if (awardCategoriesResult.isSuccess()) {
                request.setAttribute("awardCategories", awardCategoriesResult.getData());
            }
            Result awardLevelsResult = awardService.getAwardLevels();
            if (awardLevelsResult.isSuccess()) {
                request.setAttribute("awardLevels", awardLevelsResult.getData());
            }
            Result competitionLevelsResult = awardService.getCompetitionLevels();
            if (competitionLevelsResult.isSuccess()) {
                request.setAttribute("competitionLevels", competitionLevelsResult.getData());
            }

            request.getRequestDispatcher("/jsp/member/award/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.getAwardDetail(id);
            if (result.isSuccess()) {
                request.setAttribute("award", result.getData());
                // 获取奖项图片
                Result imagesResult = awardService.getAwardImages(id);
                if (imagesResult.isSuccess()) {
                    request.setAttribute("awardImages", imagesResult.getData());
                }
            }

            // 设置字典数据
            Result awardTypesResult = awardService.getAwardTypes();
            if (awardTypesResult.isSuccess()) {
                request.setAttribute("awardTypes", awardTypesResult.getData());
            }
            Result awardCategoriesResult = awardService.getAwardCategories();
            if (awardCategoriesResult.isSuccess()) {
                request.setAttribute("awardCategories", awardCategoriesResult.getData());
            }
            Result awardLevelsResult = awardService.getAwardLevels();
            if (awardLevelsResult.isSuccess()) {
                request.setAttribute("awardLevels", awardLevelsResult.getData());
            }
            Result competitionLevelsResult = awardService.getCompetitionLevels();
            if (competitionLevelsResult.isSuccess()) {
                request.setAttribute("competitionLevels", competitionLevelsResult.getData());
            }

            request.getRequestDispatcher("/jsp/member/award/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
        }
    }

    private void deleteAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Result result = awardService.deleteAward(id, user.getId());
        } catch (NumberFormatException e) {
        }
        response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
    }

    private void updateAward(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            String competition = request.getParameter("competition");
            String compTime = request.getParameter("competitionTime");
            String awardLevel = request.getParameter("awardLevel");
            String awardType = request.getParameter("awardType");
            String awardCategory = request.getParameter("awardCategory");
            String teamName = request.getParameter("teamName");
            String competitionLevel = request.getParameter("competitionLevel");
            String competitionLocation = request.getParameter("competitionLocation");
            String competitionSession = request.getParameter("competitionSession");

            if (competition == null || competition.trim().isEmpty()) {
                request.setAttribute("error", "竞赛名称不能为空");
                showEditForm(request, response, user);
                return;
            }

            AwardDTO dto = new AwardDTO();
            dto.setCompetition(competition);
            dto.setCompetitionTime(compTime);
            if (awardLevel != null && !awardLevel.isEmpty()) {
                dto.setAwardLevel(Integer.parseInt(awardLevel));
            }
            if (awardType != null && !awardType.isEmpty()) {
                dto.setAwardType(Integer.parseInt(awardType));
            }
            if (awardCategory != null && !awardCategory.isEmpty()) {
                dto.setAwardCategory(Integer.parseInt(awardCategory));
            }
            if (competitionLevel != null && !competitionLevel.isEmpty()) {
                dto.setCompetitionLevel(Integer.parseInt(competitionLevel));
            }
            dto.setTeamName(teamName);
            dto.setCompetitionLocation(competitionLocation);
            dto.setCompetitionSession(competitionSession);

            Result result = awardService.updateAward(id, dto, user.getId());
            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/award?action=myAwards");
                return;
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "更新失败: " + e.getMessage());
        }
        showEditForm(request, response, user);
    }
}
