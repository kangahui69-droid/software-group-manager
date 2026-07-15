package servlet;

import dao.AwardDAO;
import dao.AwardImageDAO;
import dao.DictionaryDAO;
import dao.FileStorageDAO;
import model.Award;
import model.AwardImage;
import model.FileStorage;
import model.User;
import model.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import util.FileUtil;

/**
 * 奖项Servlet
 */
@javax.servlet.annotation.MultipartConfig
public class AwardServlet extends HttpServlet {
    private AwardDAO awardDAO = new AwardDAO();
    private AwardImageDAO awardImageDAO = new AwardImageDAO();
    private DictionaryDAO dictionaryDAO = new DictionaryDAO();
    private final String UPLOAD_DIR = "localstorage/images/award";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 打印请求信息
        System.out.println("===== 处理GET请求 ======");
        System.out.println("请求URL: " + request.getRequestURL().toString());
        System.out.println("查询参数: " + request.getQueryString());
        
        // 首先检查是否是筛选请求
        String action = request.getParameter("action");
        System.out.println("获取到的action参数: " + action);
        
        // 如果是筛选请求，直接处理，不需要登录
        if ("filter".equals(action)) {
            System.out.println("直接处理筛选请求，不需要登录");
            filterAwards(request, response);
            return;
        }
        
        HttpSession session = request.getSession(false);
        System.out.println("会话是否存在: " + (session != null));
        
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("会话不存在或用户未登录，重定向到登录页面");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        System.out.println("当前用户: " + user.getUsername() + " (角色: " + user.getRole() + ")");

        // 如果没有action参数，尝试从URL路径中获取
        if (action == null || action.isEmpty()) {
            System.out.println("action参数为空，尝试从URL路径中获取");
            // 获取请求路径信息
            String pathInfo = request.getPathInfo();
            System.out.println("路径信息: " + pathInfo);
            if (pathInfo != null && !pathInfo.isEmpty()) {
                // 去除开头的斜杠，只获取第一个路径部分作为action
                String[] pathParts = pathInfo.substring(1).split("/");
                if (pathParts.length > 0) {
                    action = pathParts[0];
                    System.out.println("从路径中获取到的action: " + action);
                    // 如果有第二个路径部分，将其设置为id参数
                    if (pathParts.length > 1) {
                        request.setAttribute("pathId", pathParts[1]);
                        System.out.println("从路径中获取到的id: " + pathParts[1]);
                    }
                }
            }
            if (action == null || action.isEmpty()) {
                action = "list";
                System.out.println("action参数仍为空，使用默认值: " + action);
            }
        }

        // 根据用户角色和action参数决定调用哪个方法
        System.out.println("最终决定的action: " + action);
        if ("ADMIN".equals(user.getRole())) {
            System.out.println("当前用户是管理员");
            // 管理员用户
            if ("list".equals(action)) {
                System.out.println("调用listAwardsForApproval方法");
                // 管理员访问/award/list时，应该调用审核列表方法
                listAwardsForApproval(request, response);
            } else if ("approveList".equals(action)) {
                System.out.println("调用listAwardsForApproval方法");
                listAwardsForApproval(request, response);
            } else if ("approveDetail".equals(action)) {
                System.out.println("调用viewAwardForApproval方法");
                viewAwardForApproval(request, response);
            } else {
                // 管理员也可以访问普通用户的功能
                System.out.println("管理员访问普通用户功能");
                if ("detail".equals(action)) {
                    System.out.println("调用viewAward方法");
                    viewAward(request, response);
                } else if ("addImage".equals(action)) {
                    System.out.println("调用showAddImageForm方法");
                    showAddImageForm(request, response);
                } else {
                    System.out.println("调用默认方法listAwardsForApproval");
                    // 默认显示审核列表
                    listAwardsForApproval(request, response);
                }
            }
        } else {
            System.out.println("当前用户是普通用户");
            // 普通用户
            if ("list".equals(action)) {
                System.out.println("调用listAwards方法");
                listAwards(request, response);
            } else if ("detail".equals(action)) {
                System.out.println("调用viewAward方法");
                viewAward(request, response);
            } else if ("submit".equals(action)) {
                System.out.println("调用showSubmitForm方法");
                showSubmitForm(request, response);
            } else if ("edit".equals(action)) {
                System.out.println("调用showEditForm方法");
                showEditForm(request, response);
            } else if ("addImage".equals(action)) {
                System.out.println("调用showAddImageForm方法");
                showAddImageForm(request, response);
            } else if ("delete".equals(action)) {
                System.out.println("调用deleteAward方法");
                deleteAward(request, response);
            } else {
                System.out.println("action参数不匹配，没有调用任何方法");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/error.jsp");
            return;
        }

        if ("submit".equals(action)) {
            submitAward(request, response);
        } else if ("update".equals(action)) {
            updateAward(request, response);
        } else if ("approve".equals(action)) {
            approveAward(request, response);
        } else if ("reject".equals(action)) {
            rejectAward(request, response);
        } else if ("addImage".equals(action)) {
            addAwardImage(request, response);
        }
    }

    // 用户奖项列表
    private void listAwards(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        List<Award> awards = awardDAO.findByUserId(user.getId());

        // 获取字典数据
        List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
        List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
        List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

        // 计算统计数据
        int totalPersonalAwards = awards.size();
        int personalAwards = 0;
        int teamAwards = 0;
        int nationalAwards = 0;
        int provincialAwards = 0;
        int currentYearAwards = 0;
        
        // 获取当前年份
        int currentYear = java.time.Year.now().getValue();
        
        // 获取团队奖项和个人奖项的字典值
        Dictionary personalType = dictionaryDAO.findByTypeAndCode("AWARD_TYPE", "TYPE_PERSONAL");
        Dictionary teamType = dictionaryDAO.findByTypeAndCode("AWARD_TYPE", "TYPE_TEAM");
        
        // 获取国家级和省级奖项的字典值
        Dictionary nationalLevel = dictionaryDAO.findByTypeAndCode("AWARD_LEVEL", "LEVEL_NATIONAL");
        Dictionary provincialLevel = dictionaryDAO.findByTypeAndCode("AWARD_LEVEL", "LEVEL_PROVINCIAL");
        
        for (Award award : awards) {
            // 统计个人奖项和团队奖项
            if (personalType != null && award.getAwardType() != null && award.getAwardType().equals(personalType.getId())) {
                personalAwards++;
            } else if (teamType != null && award.getAwardType() != null && award.getAwardType().equals(teamType.getId())) {
                teamAwards++;
            }
            
            // 统计国家级和省级奖项
            if (nationalLevel != null && award.getAwardLevel() != null && award.getAwardLevel().equals(nationalLevel.getId())) {
                nationalAwards++;
            } else if (provincialLevel != null && award.getAwardLevel() != null && award.getAwardLevel().equals(provincialLevel.getId())) {
                provincialAwards++;
            }
            
            // 统计当前年度奖项
            if (award.getYear() != null && award.getYear().equals(currentYear)) {
                currentYearAwards++;
            }
        }
        
        // 创建统计数据对象
        java.util.Map<String, Integer> awardStats = new java.util.HashMap<>();
        awardStats.put("totalPersonalAwards", totalPersonalAwards);
        awardStats.put("personalAwards", personalAwards);
        awardStats.put("teamAwards", teamAwards);
        awardStats.put("nationalAwards", nationalAwards);
        awardStats.put("provincialAwards", provincialAwards);
        awardStats.put("currentYearAwards", currentYearAwards);
        
        System.out.println("统计数据:");
        System.out.println("  个人获奖总数: " + totalPersonalAwards);
        System.out.println("  个人奖项: " + personalAwards);
        System.out.println("  团队奖项: " + teamAwards);
        System.out.println("  国家级奖项: " + nationalAwards);
        System.out.println("  省级奖项: " + provincialAwards);
        System.out.println("  当前年度奖项: " + currentYearAwards);

        request.setAttribute("awardTypes", awardTypes);
        request.setAttribute("awardCategories", awardCategories);
        request.setAttribute("awardLevels", awardLevels);
        request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));
        request.setAttribute("awardStats", awardStats);

        request.setAttribute("awards", awards);
        request.getRequestDispatcher("/member/award/list.jsp").forward(request, response);
    }

    // 处理奖项筛选请求（返回JSON）
    private void filterAwards(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"未登录\"}");
                return;
            }

            // 获取所有奖项，在内存中筛选（简化实现）
            List<Award> allAwards = awardDAO.findByUserId(user.getId());
            
            String competition = request.getParameter("competition");
            String competitionLevelStr = request.getParameter("competitionLevel");
            String awardLevelStr = request.getParameter("awardLevel");
            String status = request.getParameter("status");

            // 获取字典数据用于转换ID为名称
            Map<Integer, String> competitionLevelMap = new HashMap<>();
            Map<Integer, String> awardLevelMap = new HashMap<>();
            List<Dictionary> competitionLevels = dictionaryDAO.findByType("COMPETITION_LEVEL");
            List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");
            for (Dictionary d : competitionLevels) {
                competitionLevelMap.put(d.getId(), d.getName());
            }
            for (Dictionary d : awardLevels) {
                awardLevelMap.put(d.getId(), d.getName());
            }

            // 筛选
            List<Award> filteredAwards = new ArrayList<>();
            for (Award a : allAwards) {
                boolean match = true;
                if (competition != null && !competition.isEmpty() && 
                    (a.getCompetition() == null || !a.getCompetition().contains(competition))) {
                    match = false;
                }
                if (match && competitionLevelStr != null && !competitionLevelStr.isEmpty()) {
                    try {
                        Integer competitionLevel = Integer.parseInt(competitionLevelStr);
                        if (a.getCompetitionLevel() == null || !a.getCompetitionLevel().equals(competitionLevel)) {
                            match = false;
                        }
                    } catch (NumberFormatException e) {}
                }
                if (match && awardLevelStr != null && !awardLevelStr.isEmpty()) {
                    try {
                        Integer awardLevel = Integer.parseInt(awardLevelStr);
                        if (a.getAwardLevel() == null || !a.getAwardLevel().equals(awardLevel)) {
                            match = false;
                        }
                    } catch (NumberFormatException e) {}
                }
                if (match && status != null && !status.isEmpty() && 
                    (a.getAwardStatus() == null || !a.getAwardStatus().equals(status))) {
                    match = false;
                }
                if (match) {
                    filteredAwards.add(a);
                }
            }

            // 转换为JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < filteredAwards.size(); i++) {
                Award a = filteredAwards.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\":").append(a.getId()).append(",");
                json.append("\"competition\":\"").append(escapeJson(a.getCompetition())).append("\",");
                json.append("\"competitionLevelName\":\"").append(escapeJson(competitionLevelMap.get(a.getCompetitionLevel()))).append("\",");
                json.append("\"awardLevelName\":\"").append(escapeJson(awardLevelMap.get(a.getAwardLevel()))).append("\",");
                json.append("\"competitionLocation\":\"").append(escapeJson(a.getCompetitionLocation())).append("\",");
                json.append("\"competitionTime\":\"").append(a.getCompetitionTime() != null ? a.getCompetitionTime() : "").append("\",");
                json.append("\"awardStatus\":\"").append(escapeJson(a.getAwardStatus())).append("\"");
                json.append("}");
            }
            json.append("]");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"筛选失败，请重试\"}");
        }
    }
    
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // 管理员审核奖项列表
    private void listAwardsForApproval(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        String awardType = request.getParameter("awardType");
        String awardCategory = request.getParameter("awardCategory");
        String awardLevel = request.getParameter("awardLevel");
        String competitionLevel = request.getParameter("competitionLevel");
        
        // 默认显示全部
        if (status == null || status.isEmpty()) {
            status = "ALL";
        }
        
        List<Award> awards = awardDAO.findByConditions(status, keyword, awardType, awardCategory, awardLevel, competitionLevel);

        // 获取字典数据
        List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
        List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
        List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

        request.setAttribute("awardTypes", awardTypes);
        request.setAttribute("awardCategories", awardCategories);
        request.setAttribute("awardLevels", awardLevels);
        request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));
        request.setAttribute("awards", awards);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedAwardType", awardType);
        request.setAttribute("selectedAwardCategory", awardCategory);
        request.setAttribute("selectedAwardLevel", awardLevel);
        request.setAttribute("selectedCompetitionLevel", competitionLevel);

        request.getRequestDispatcher("/admin/award/approve.jsp").forward(request, response);
    }

    // 查看奖项详情（管理员审核用）
    private void viewAwardForApproval(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/award/approve");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Award award = awardDAO.findById(id);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/admin/award/approve");
                return;
            }

            // 获取奖项图片
            List<AwardImage> images = awardImageDAO.findByAwardId(id);

            // 获取字典数据
            List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
            List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
            List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

            request.setAttribute("awardTypes", awardTypes);
            request.setAttribute("awardCategories", awardCategories);
            request.setAttribute("awardLevels", awardLevels);
            request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));
            request.setAttribute("award", award);
            request.setAttribute("images", images);

            request.getRequestDispatcher("/admin/award/approve-detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/award/approve");
        }
    }

    // 查看奖项详情
    private void viewAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Award award = awardDAO.findById(id);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/member/award/list");
                return;
            }

            // 获取奖项图片
            List<AwardImage> images = awardImageDAO.findByAwardId(id);

            // 获取字典数据
            List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
            List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
            List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

            request.setAttribute("awardTypes", awardTypes);
            request.setAttribute("awardCategories", awardCategories);
            request.setAttribute("awardLevels", awardLevels);
            request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));
            request.setAttribute("award", award);
            request.setAttribute("images", images);

            request.getRequestDispatcher("/member/award/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    // 显示提交表单
    private void showSubmitForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取字典数据
        List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
        List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
        List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

        request.setAttribute("awardTypes", awardTypes);
        request.setAttribute("awardCategories", awardCategories);
        request.setAttribute("awardLevels", awardLevels);
        request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));

        // 检查是否有传入的用户ID（管理员为其他成员添加获奖）
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) {
            request.setAttribute("targetUserId", userIdParam);
        }

        request.getRequestDispatcher("/member/award/submit.jsp").forward(request, response);
    }

    // 显示编辑表单
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Award award = awardDAO.findById(id);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/member/award/list");
                return;
            }

            // 获取奖项图片
            List<AwardImage> images = awardImageDAO.findByAwardId(id);

            // 获取字典数据
            List<Dictionary> awardTypes = dictionaryDAO.findByType("AWARD_TYPE");
            List<Dictionary> awardCategories = dictionaryDAO.findByType("AWARD_CATEGORY");
            List<Dictionary> awardLevels = dictionaryDAO.findByType("AWARD_LEVEL");

            request.setAttribute("awardTypes", awardTypes);
            request.setAttribute("awardCategories", awardCategories);
            request.setAttribute("awardLevels", awardLevels);
            request.setAttribute("competitionLevels", dictionaryDAO.findByType("COMPETITION_LEVEL"));
            request.setAttribute("award", award);
            request.setAttribute("images", images);

            request.getRequestDispatcher("/member/award/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    // 显示添加图片表单
    private void showAddImageForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Award award = awardDAO.findById(id);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/member/award/list");
                return;
            }

            request.setAttribute("award", award);
            request.getRequestDispatcher("/member/award/add-image.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    // 提交奖项
    private void submitAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 确定目标用户ID（管理员可以为其他成员添加获奖）
        Integer targetUserId = user.getId();
        String targetUserIdParam = request.getParameter("targetUserId");
        if (targetUserIdParam != null && !targetUserIdParam.isEmpty() && "ADMIN".equals(user.getRole())) {
            targetUserId = Integer.parseInt(targetUserIdParam);
        }

        String competition = request.getParameter("competition");
        String competitionLocation = request.getParameter("competitionLocation");
        String competitionTime = request.getParameter("competitionTime");
        String teamName = request.getParameter("teamName");
        String awardLevel = request.getParameter("awardLevel");
        String awardType = request.getParameter("awardType");
        String awardCategory = request.getParameter("awardCategory");
        String competitionLevel = request.getParameter("competitionLevel");

        // 基本校验
        if (competition == null || competition.trim().isEmpty() ||
            competitionTime == null || competitionTime.trim().isEmpty() ||
            awardLevel == null || awardLevel.trim().isEmpty() ||
            awardType == null || awardType.trim().isEmpty() ||
            awardCategory == null || awardCategory.trim().isEmpty() ||
            competitionLevel == null || competitionLevel.trim().isEmpty()) {
            request.setAttribute("error", "请填写所有必填项");
            showSubmitForm(request, response);
            return;
        }

        Award award = new Award();
        award.setUserId(targetUserId);
        award.setCreatedBy(user.getId()); // 记录实际创建者
        award.setCompetition(competition);
        award.setCompetitionLocation(competitionLocation);
        award.setTeamName(teamName);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(competitionTime);
            award.setCompetitionTime(date);

            // 从比赛时间提取年份
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            award.setYear(cal.get(Calendar.YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        award.setAwardLevel(Integer.parseInt(awardLevel));
        award.setAwardType(Integer.parseInt(awardType));
        award.setAwardCategory(Integer.parseInt(awardCategory));
        award.setCompetitionLevel(Integer.parseInt(competitionLevel));
        award.setAwardStatus("PENDING");
        // 设置name字段（用于列表显示）
        award.setName(competition);

        System.out.println("=== 准备插入奖项 ===");
        System.out.println("  competition: " + competition);
        System.out.println("  competitionTime: " + competitionTime);
        System.out.println("  awardLevel: " + awardLevel);
        System.out.println("  awardType: " + awardType);
        System.out.println("  awardCategory: " + awardCategory);
        System.out.println("  competitionLevel: " + competitionLevel);

        // 保存奖项 - 检查插入结果
        boolean insertSuccess = awardDAO.insert(award);
        System.out.println("=== 插入结果: " + insertSuccess + " ===");
        System.out.println("=== 插入后的 award.getId(): " + award.getId() + " ===");

        if (!insertSuccess || award.getId() == null) {
            System.err.println("=== 奖项插入失败！===");
            request.setAttribute("error", "奖项提交失败，请重试");
            showSubmitForm(request, response);
            return;
        }

        Integer awardId = award.getId();
        System.out.println("=== 开始处理图片上传, awardId: " + awardId + " ===");

        // 处理图片上传
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (part.getName().equals("awardImages") && part.getSize() > 0) {
                    String originalFileName = part.getSubmittedFileName();
                    if (originalFileName != null && !originalFileName.isEmpty()) {
                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0) {
                            fileExtension = originalFileName.substring(dotIndex);
                        }
                        String fileName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;
                        String uploadPath = FileUtil.getCategoryDir("images/award");

                        String filePath = uploadPath + File.separator + fileName;
                        part.write(filePath);
                        System.out.println("=== 图片保存成功: " + filePath + " ===");

                        AwardImage image = new AwardImage();
                        image.setAwardId(awardId);
                        image.setImagePath("/localstorage/images/award/" + fileName);
                        image.setOriginalName(originalFileName);
                        awardImageDAO.insert(image);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("=== 图片上传失败（不影响奖项提交）===");
            // 图片上传失败不影响奖项提交
        }

        System.out.println("=== 奖项提交成功，准备重定向 ===");
        response.sendRedirect(request.getContextPath() + "/award?action=list&success=" + encode("奖项提交成功，请等待管理员审核"));
    }

    // 更新奖项
    private void updateAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Award award = awardDAO.findById(id);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/member/award/list");
                return;
            }

            String competition = request.getParameter("competition");
            String competitionLocation = request.getParameter("competitionLocation");
            String competitionTime = request.getParameter("competitionTime");
            String awardName = request.getParameter("awardName");
            String awardLevel = request.getParameter("awardLevel");
            String awardType = request.getParameter("awardType");
            String awardCategory = request.getParameter("awardCategory");
            String competitionLevel = request.getParameter("competitionLevel");
            String description = request.getParameter("description");

            award.setCompetition(competition);
            award.setCompetitionLocation(competitionLocation);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(competitionTime);
                award.setCompetitionTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            award.setAwardName(awardName);
            award.setAwardLevel(Integer.parseInt(awardLevel));
            award.setAwardType(Integer.parseInt(awardType));
            award.setAwardCategory(Integer.parseInt(awardCategory));
            award.setCompetitionLevel(Integer.parseInt(competitionLevel));
            award.setDescription(description);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(competitionTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                award.setYear(cal.get(Calendar.YEAR));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            awardDAO.update(award);

            response.sendRedirect(request.getContextPath() + "/member/award/list");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    // 审核通过
    private void approveAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            awardDAO.updateAwardStatus(id, "APPROVED");
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
        }
    }

    // 审核拒绝
    private void rejectAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            awardDAO.updateAwardStatus(id, "REJECTED");
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/award?action=approveList");
        }
    }

    // 删除奖项
    private void deleteAward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            awardDAO.delete(id);
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    // 添加奖项图片
    private void addAwardImage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
            return;
        }

        try {
            int awardId = Integer.parseInt(idStr);
            Award award = awardDAO.findById(awardId);
            if (award == null) {
                response.sendRedirect(request.getContextPath() + "/member/award/list");
                return;
            }

            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (part.getName().equals("image") && part.getSize() > 0) {
                    String fileName = System.currentTimeMillis() + "_" + part.getSubmittedFileName();
                    String uploadPath = FileUtil.getCategoryDir("images/award");

                    String filePath = uploadPath + File.separator + fileName;
                    part.write(filePath);

                    AwardImage image = new AwardImage();
                    image.setAwardId(awardId);
                    image.setImagePath("/localstorage/images/award/" + fileName);
                    image.setOriginalName(part.getSubmittedFileName());
                    awardImageDAO.insert(image);
                }
            }

            response.sendRedirect(request.getContextPath() + "/member/award/detail?id=" + awardId);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/member/award/list");
        }
    }

    /**
     * URL编码工具方法
     */
    private String encode(String message) {
        try {
            return java.net.URLEncoder.encode(message, "UTF-8");
        } catch (Exception e) {
            return message;
        }
    }
}