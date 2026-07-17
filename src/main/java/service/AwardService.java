package service;

import dao.AwardDAO;
import dao.AwardImageDAO;
import dao.DictionaryDAO;
import dao.FileStorageDAO;
import dao.UserDAO;
import dto.AwardDTO;
import model.Award;
import model.AwardImage;
import model.Dictionary;
import model.FileStorage;
import model.User;
import util.FileUtil;
import util.Result;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 奖项服务层
 *
 * 服务分层与API化重构计划.md 4.5 AwardService 奖项服务
 */
public class AwardService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private static final int TYPE_PERSONAL = 1;
    private static final int TYPE_TEAM = 2;

    private static final int LEVEL_NATIONAL = 1;
    private static final int LEVEL_PROVINCIAL = 2;
    private static final int LEVEL_SCHOOL = 3;

    private static final String ADMIN_ROLE = "ADMIN";
    private static final int STATUS_NORMAL = 1;
    private static final String AWARD_CATEGORY = "awards";
    private static final String AWARD_STORAGE_PATH = "/localstorage/files/awards/";

    private static final long MAX_IMAGE_SIZE = 100 * 1024 * 1024L;
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    };

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final AwardDAO awardDAO;
    private final AwardImageDAO awardImageDAO;
    private final UserDAO userDAO;
    private final FileStorageDAO fileStorageDAO;

    public AwardService() {
        this.awardDAO = new AwardDAO();
        this.awardImageDAO = new AwardImageDAO();
        this.userDAO = new UserDAO();
        this.fileStorageDAO = new FileStorageDAO();
    }

    public AwardService(AwardDAO awardDAO, AwardImageDAO awardImageDAO, UserDAO userDAO, FileStorageDAO fileStorageDAO) {
        this.awardDAO = awardDAO;
        this.awardImageDAO = awardImageDAO;
        this.userDAO = userDAO;
        this.fileStorageDAO = fileStorageDAO;
    }

    public AwardService(AwardDAO awardDAO, AwardImageDAO awardImageDAO) {
        this.awardDAO = awardDAO;
        this.awardImageDAO = awardImageDAO;
        this.userDAO = new UserDAO();
        this.fileStorageDAO = new FileStorageDAO();
    }

    // ==================== 公开业务方法 ====================

    /**
     * 提交奖项
     */
    public Result submitAward(AwardDTO dto, Integer userId, Object[] images) {
        if (dto == null) {
            return Result.error(400, "奖项信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        // 验证用户是否存在
        User user;
        try {
            user = userDAO.findById(userId);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Result validation = validateAwardDTO(dto);
        if (validation != null) {
            return validation;
        }

        Award award = buildAwardFromDTO(dto, userId);

        try {
            boolean inserted = awardDAO.insert(award);
            if (!inserted) {
                return Result.error(500, "提交失败");
            }

            // 处理图片
            if (images != null && images.length > 0) {
                processAwardImages(award.getId(), userId, images);
            }

            return Result.ok(award);
        } catch (RuntimeException e) {
            return Result.error(500, "提交失败");
        }
    }

    /**
     * 提交奖项（Map版本 - 测试用）
     */
    public Result submitAward(java.util.Map<String, String> params, Integer userId, Object[] images) {
        AwardDTO dto = new AwardDTO();
        dto.setCompetition(params.get("competition"));
        dto.setCompetitionTime(params.get("compTime"));
        if (params.get("awardLevel") != null) {
            dto.setAwardLevel(Integer.parseInt(params.get("awardLevel")));
        }
        if (params.get("awardType") != null) {
            dto.setAwardType(Integer.parseInt(params.get("awardType")));
        }
        return submitAward(dto, userId, images);
    }

    /**
     * 审批通过
     */
    public Result approveAward(Integer id, Integer operatorId) {
        Result validation = validateApprovalRequest(id, operatorId);
        if (validation != null) {
            return validation;
        }

        Award award;
        try {
            award = awardDAO.findById(id);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        Result statusCheck = validateAwardPendingStatus(award);
        if (statusCheck != null) {
            return statusCheck;
        }

        User operator;
        try {
            operator = userDAO.findById(operatorId);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (operator == null) {
            return Result.error(404, "操作者不存在");
        }
        if (!ADMIN_ROLE.equals(operator.getRole())) {
            return Result.error(403, "无权限操作");
        }

        try {
            boolean approved = awardDAO.approveAward(id, operatorId);
            if (!approved) {
                return Result.error(500, "审批失败");
            }
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 驳回
     */
    public Result rejectAward(Integer id, String reason, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (isBlank(reason)) {
            return Result.error(400, "驳回原因不能为空");
        }

        Award award;
        try {
            award = awardDAO.findById(id);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        Result statusCheck = validateAwardPendingStatus(award);
        if (statusCheck != null) {
            return statusCheck;
        }

        User operator;
        try {
            operator = userDAO.findById(operatorId);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (operator == null) {
            return Result.error(404, "操作者不存在");
        }
        if (!ADMIN_ROLE.equals(operator.getRole())) {
            return Result.error(403, "无权限操作");
        }

        try {
            boolean rejected = awardDAO.rejectAward(id, operatorId);
            if (!rejected) {
                return Result.error(500, "驳回失败");
            }
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 添加图片
     */
    public Result addAwardImage(Integer id, Object file, Integer userId) {
        if (id == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (file == null) {
            return Result.error(400, "文件不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Award award;
        try {
            award = awardDAO.findById(id);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        User user;
        try {
            user = userDAO.findById(userId);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (!isOwnerOrAdmin(userId, award, user)) {
            return Result.error(403, "没有权限添加图片");
        }

        AwardImageInfo imageInfo = extractImageInfo(file);
        if (imageInfo == null) {
            return Result.error(400, "文件信息获取失败");
        }

        Result fileValidation = validateImageFile(imageInfo.size, imageInfo.contentType);
        if (fileValidation != null) {
            return fileValidation;
        }

        try {
            FileStorage fileStorage = createAwardFileStorage(userId, imageInfo);
            // 实际保存文件内容
            boolean saved = saveImageFile(file, fileStorage);
            if (!saved) {
                return Result.error(500, "文件保存失败");
            }

            Integer fileId = fileStorageDAO.insert(fileStorage);
            if (fileId == null || fileId <= 0) {
                return Result.error(500, "文件保存失败");
            }

            AwardImage awardImage = buildAwardImage(id, userId, fileId);

            boolean inserted = awardImageDAO.insert(awardImage);
            if (!inserted) {
                return Result.error(500, "图片保存失败");
            }

            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 列表
     */
    public Result listAwards(String filter, int page) {
        int normalizedPage = normalizePage(page);

        List<Award> awards;
        if (filter == null || filter.trim().isEmpty() || "ALL".equals(filter)) {
            awards = awardDAO.findAll();
        } else if (filter.equals(STATUS_APPROVED) || filter.equals(STATUS_PENDING) || filter.equals(STATUS_REJECTED)) {
            awards = awardDAO.findByStatus(filter);
        } else {
            awards = awardDAO.findByStatusAndKeyword(filter, null);
        }

        return Result.ok(awards);
    }

    /**
     * 筛选奖项（公开接口，测试用）
     */
    public Result filterAwards(String filter, String status) {
        if (status != null && !status.isEmpty()) {
            return listAwards(status, 1);
        }
        return listAwards(filter, 1);
    }

    /**
     * 个人获奖统计
     */
    public Result getAwardStatistics(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<Award> awards = awardDAO.findByUserId(userId);
        if (awards == null) {
            awards = List.of();
        }

        AwardStatistics statistics = calculateStatistics(awards);
        return Result.ok(statistics);
    }

    /**
     * 获取我的奖项（用户所有奖项）
     */
    public Result getMyAwards(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<Award> awards = awardDAO.findByUserId(userId);
        if (awards == null) {
            return Result.ok(List.of());
        }

        return Result.ok(awards);
    }

    /**
     * 筛选用户奖项
     */
    public Result filterAwardsForUser(Integer userId, String filter) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<Award> awards = awardDAO.findByUserId(userId);
        if (awards == null || awards.isEmpty()) {
            return Result.ok(List.of());
        }

        if (filter == null || filter.trim().isEmpty()) {
            return Result.ok(awards);
        }

        List<Award> filtered = awards.stream()
            .filter(award -> filter.equals(award.getAwardStatus()))
            .collect(java.util.stream.Collectors.toList());

        return Result.ok(filtered);
    }

    /**
     * 获取奖项详情
     */
    public Result getAwardDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "奖项ID不能为空");
        }

        Award award = awardDAO.findById(id);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }
        return Result.ok(award);
    }

    /**
     * 获取奖项图片列表
     */
    public Result getAwardImages(Integer awardId) {
        if (awardId == null) {
            return Result.error(400, "奖项ID不能为空");
        }

        List<AwardImage> images = awardImageDAO.findByAwardId(awardId);
        return Result.ok(images);
    }

    /**
     * 管理员查询奖项列表（用于审批）
     */
    public Result listAwardsForApproval(String status, String keyword, String awardType, String awardCategory, String awardLevel, String competitionLevel) {
        List<Award> awards = awardDAO.findByConditions(status, keyword, awardType, awardCategory, awardLevel, competitionLevel);
        return Result.ok(awards);
    }

    /**
     * 删除奖项
     */
    public Result deleteAward(Integer id, Integer userId) {
        if (id == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Award award = awardDAO.findById(id);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        // 检查权限：创建者或管理员才能删除
        if (!userId.equals(award.getCreatedBy()) && !"ADMIN".equals(userDAO.findById(userId).getRole())) {
            return Result.error(403, "没有权限删除此奖项");
        }

        // 只有待审核状态才能删除
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "只能删除待审核的奖项");
        }

        boolean deleted = awardDAO.delete(id);
        if (!deleted) {
            return Result.error(500, "删除失败");
        }
        return Result.ok();
    }

    /**
     * 更新奖项
     */
    public Result updateAward(Integer id, AwardDTO dto, Integer userId) {
        if (id == null) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        Award award = awardDAO.findById(id);
        if (award == null) {
            return Result.error(404, "奖项不存在");
        }

        // 检查权限
        if (!userId.equals(award.getCreatedBy())) {
            return Result.error(403, "没有权限修改此奖项");
        }

        // 只有待审核状态才能修改
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "只能修改待审核的奖项");
        }

        // 更新字段
        if (dto.getCompetition() != null) {
            award.setCompetition(dto.getCompetition());
        }
        if (dto.getCompetitionTime() != null) {
            award.setCompetitionTime(dto.parseCompetitionTime());
        }
        if (dto.getAwardLevel() != null) {
            award.setAwardLevel(dto.getAwardLevel());
        }
        if (dto.getAwardType() != null) {
            award.setAwardType(dto.getAwardType());
        }
        if (dto.getAwardCategory() != null) {
            award.setAwardCategory(dto.getAwardCategory());
        }
        if (dto.getCompetitionLevel() != null) {
            award.setCompetitionLevel(dto.getCompetitionLevel());
        }
        if (dto.getTeamName() != null) {
            award.setTeamName(dto.getTeamName());
        }
        if (dto.getCompetitionLocation() != null) {
            award.setCompetitionLocation(dto.getCompetitionLocation());
        }
        if (dto.getCompetitionSession() != null) {
            award.setCompetitionSession(dto.getCompetitionSession());
        }

        boolean updated = awardDAO.update(award);
        if (!updated) {
            return Result.error(500, "更新失败");
        }
        return Result.ok();
    }

    // ==================== 字典数据获取方法 ====================

    /**
     * 获取奖项类型字典
     */
    public Result getAwardTypes() {
        DictionaryDAO dictionaryDAO = new DictionaryDAO();
        List<Dictionary> types = dictionaryDAO.findByType("award_type");
        return Result.ok(types);
    }

    /**
     * 获取奖项类别字典
     */
    public Result getAwardCategories() {
        DictionaryDAO dictionaryDAO = new DictionaryDAO();
        List<Dictionary> categories = dictionaryDAO.findByType("award_category");
        return Result.ok(categories);
    }

    /**
     * 获取奖项等级字典
     */
    public Result getAwardLevels() {
        DictionaryDAO dictionaryDAO = new DictionaryDAO();
        List<Dictionary> levels = dictionaryDAO.findByType("award_level");
        return Result.ok(levels);
    }

    /**
     * 获取比赛等级字典
     */
    public Result getCompetitionLevels() {
        DictionaryDAO dictionaryDAO = new DictionaryDAO();
        List<Dictionary> levels = dictionaryDAO.findByType("competition_level");
        return Result.ok(levels);
    }

    // ==================== 验证方法 ====================

    private Result validateAwardDTO(AwardDTO dto) {
        if (isBlank(dto.getCompetition())) {
            return Result.error(400, "竞赛名称不能为空");
        }

        if (dto.getCompetitionTime() != null && !dto.getCompetitionTime().trim().isEmpty()) {
            if (!DATE_PATTERN.matcher(dto.getCompetitionTime()).matches()) {
                return Result.error(400, "比赛时间格式错误");
            }
        }

        return null;
    }

    private Result validateApprovalRequest(Integer id, Integer operatorId) {
        if (id == null || id <= 0) {
            return Result.error(400, "奖项ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private Result validateAwardPendingStatus(Award award) {
        if (!STATUS_PENDING.equals(award.getAwardStatus())) {
            return Result.error(400, "只能审批待审核的奖项");
        }
        return null;
    }

    private boolean isOwnerOrAdmin(Integer userId, Award award, User user) {
        boolean isOwner = userId.equals(award.getCreatedBy());
        boolean isAdmin = ADMIN_ROLE.equals(user.getRole());
        return isOwner || isAdmin;
    }

    private AwardImage buildAwardImage(Integer awardId, Integer userId, Integer fileId) {
        AwardImage awardImage = new AwardImage();
        awardImage.setAwardId(awardId);
        awardImage.setMemberId(userId);
        awardImage.setFileStorageId(fileId);
        awardImage.setIsMain(false);
        return awardImage;
    }

    private void processAwardImages(Integer awardId, Integer userId, Object[] images) {
        for (Object image : images) {
            AwardImageInfo imageInfo = extractImageInfo(image);
            if (imageInfo != null) {
                Result imageValidation = validateImageFile(imageInfo.size, imageInfo.contentType);
                if (imageValidation == null) {
                    FileStorage fileStorage = createAwardFileStorage(userId, imageInfo);
                    // 实际保存文件内容
                    boolean saved = saveImageFile(image, fileStorage);
                    if (saved) {
                        Integer fileId = fileStorageDAO.insert(fileStorage);
                        if (fileId != null && fileId > 0) {
                            AwardImage awardImage = buildAwardImage(awardId, userId, fileId);
                            awardImageDAO.insert(awardImage);
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存图片文件到磁盘
     */
    private boolean saveImageFile(Object image, FileStorage fileStorage) {
        try {
            InputStream inputStream = getInputStreamFromImage(image);
            if (inputStream == null) {
                return false;
            }

            String categorySubDir = "files/" + AWARD_CATEGORY;
            String uploadPath = FileUtil.getCategoryDir(categorySubDir);
            File destFile = new File(uploadPath, fileStorage.getStoredName());

            Files.copy(inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            // 更新文件路径为完整路径
            fileStorage.setFilePath("/localstorage/" + categorySubDir + "/" + fileStorage.getStoredName());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从图片对象获取输入流
     */
    private InputStream getInputStreamFromImage(Object image) {
        try {
            if (image instanceof javax.servlet.http.Part) {
                return ((javax.servlet.http.Part) image).getInputStream();
            }
            // 尝试通过反射获取
            java.lang.reflect.Method method = image.getClass().getMethod("getInputStream");
            Object result = method.invoke(image);
            if (result instanceof InputStream) {
                return (InputStream) result;
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }

    private AwardStatistics calculateStatistics(List<Award> awards) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int nationalCount = 0;
        int provincialCount = 0;
        int personalCount = 0;
        int teamCount = 0;
        int currentYearCount = 0;

        for (Award award : awards) {
            if (STATUS_APPROVED.equals(award.getAwardStatus())) {
                if (award.getAwardLevel() != null) {
                    if (award.getAwardLevel() == LEVEL_NATIONAL) {
                        nationalCount++;
                    } else if (award.getAwardLevel() == LEVEL_PROVINCIAL) {
                        provincialCount++;
                    }
                }
                if (award.getAwardType() != null) {
                    if (award.getAwardType() == TYPE_PERSONAL) {
                        personalCount++;
                    } else if (award.getAwardType() == TYPE_TEAM) {
                        teamCount++;
                    }
                }
                if (award.getYear() != null && award.getYear() == currentYear) {
                    currentYearCount++;
                }
            }
        }

        AwardStatistics statistics = new AwardStatistics();
        statistics.setTotalPersonalAwards(awards.size());
        statistics.setNationalAwards(nationalCount);
        statistics.setProvincialAwards(provincialCount);
        statistics.setPersonalAwards(personalCount);
        statistics.setTeamAwards(teamCount);
        statistics.setCurrentYearAwards(currentYearCount);
        return statistics;
    }

    private Result validateImageFile(long fileSize, String contentType) {
        if (fileSize <= 0) {
            return Result.error(400, "文件不能为空");
        }
        if (fileSize >= MAX_IMAGE_SIZE) {
            return Result.error(400, "文件大小不能超过100MB");
        }
        if (contentType == null || !isAllowedImageType(contentType)) {
            return Result.error(400, "不支持的图片格式");
        }
        return null;
    }

    private boolean isAllowedImageType(String contentType) {
        for (String allowed : ALLOWED_IMAGE_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 业务逻辑方法 ====================

    private Award buildAwardFromDTO(AwardDTO dto, Integer userId) {
        Award award = new Award();
        award.setName(dto.getCompetition());  // name字段使用competition的值
        award.setCompetition(dto.getCompetition());
        award.setYear(dto.extractYear());
        award.setCompetitionTime(dto.parseCompetitionTime());
        award.setCompetitionLocation(dto.getCompetitionLocation());
        award.setAwardType(dto.getAwardType());
        award.setAwardCategory(dto.getAwardCategory());
        award.setAwardLevel(dto.getAwardLevel());
        award.setCompetitionLevel(dto.getCompetitionLevel());
        award.setTeamName(dto.getTeamName());
        award.setDescription(dto.getDescription());
        award.setAwardStatus(STATUS_PENDING);
        award.setCreatedBy(userId);
        return award;
    }

    private FileStorage createAwardFileStorage(Integer userId, AwardImageInfo imageInfo) {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(userId);
        fileStorage.setOriginalName(imageInfo.fileName);
        fileStorage.setStoredName(generateStoredName(userId, imageInfo.fileName));
        fileStorage.setFilePath(AWARD_STORAGE_PATH + fileStorage.getStoredName());
        fileStorage.setFileType(imageInfo.contentType);
        fileStorage.setFileSize(imageInfo.size);
        fileStorage.setCategory(AWARD_CATEGORY);
        fileStorage.setStatus(STATUS_NORMAL);
        return fileStorage;
    }

    private String generateStoredName(Integer userId, String fileName) {
        String ext = getFileExtension(fileName);
        return "award_" + userId + "_" + System.currentTimeMillis() + ext;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    // ==================== 工具方法 ====================

    private AwardImageInfo extractImageInfo(Object file) {
        try {
            long size = (Long) file.getClass().getMethod("getSize").invoke(file);
            String contentType = (String) file.getClass().getMethod("getContentType").invoke(file);
            String fileName = (String) file.getClass().getMethod("getSubmittedFileName").invoke(file);
            return new AwardImageInfo(size, contentType, fileName);
        } catch (Exception e) {
            return null;
        }
    }

    private int normalizePage(int page) {
        if (page <= 0) {
            return 1;
        }
        return page;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // ==================== 内部类 ====================

    private static class AwardImageInfo {
        final long size;
        final String contentType;
        final String fileName;

        AwardImageInfo(long size, String contentType, String fileName) {
            this.size = size;
            this.contentType = contentType;
            this.fileName = fileName;
        }
    }

    public static class AwardStatistics {
        private int totalPersonalAwards;
        private int personalAwards;
        private int teamAwards;
        private int nationalAwards;
        private int provincialAwards;
        private int currentYearAwards;

        public int getTotalPersonalAwards() {
            return totalPersonalAwards;
        }

        public void setTotalPersonalAwards(int totalPersonalAwards) {
            this.totalPersonalAwards = totalPersonalAwards;
        }

        public int getPersonalAwards() {
            return personalAwards;
        }

        public void setPersonalAwards(int personalAwards) {
            this.personalAwards = personalAwards;
        }

        public int getTeamAwards() {
            return teamAwards;
        }

        public void setTeamAwards(int teamAwards) {
            this.teamAwards = teamAwards;
        }

        public int getNationalAwards() {
            return nationalAwards;
        }

        public void setNationalAwards(int nationalAwards) {
            this.nationalAwards = nationalAwards;
        }

        public int getProvincialAwards() {
            return provincialAwards;
        }

        public void setProvincialAwards(int provincialAwards) {
            this.provincialAwards = provincialAwards;
        }

        public int getCurrentYearAwards() {
            return currentYearAwards;
        }

        public void setCurrentYearAwards(int currentYearAwards) {
            this.currentYearAwards = currentYearAwards;
        }
    }
}