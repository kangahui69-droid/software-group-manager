package service;

import dao.UserDAO;
import dao.MemberProfileDAO;
import dao.FileStorageDAO;
import dao.AdminProfileDAO;
import dao.AwardDAO;
import model.MemberProfile;
import model.User;
import model.Award;
import model.FileStorage;
import util.Result;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 成员服务层
 *
 * 服务分层与API化完整计划.md 5.3 MemberService 成员服务
 */
public class MemberService {

    // ==================== 分页常量 ====================
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== 验证常量 ====================
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final String DEFAULT_RESET_PASSWORD = "123456";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    // ==================== 状态常量 ====================
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_DISABLED = 0;

    // ==================== 依赖 ====================
    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;
    private final FileStorageDAO fileStorageDAO;
    private final AdminProfileDAO adminProfileDAO;
    private final AwardDAO awardDAO;

    // ==================== 构造函数 ====================

    public MemberService() {
        this.userDAO = new UserDAO();
        this.memberProfileDAO = new MemberProfileDAO();
        this.fileStorageDAO = new FileStorageDAO();
        this.adminProfileDAO = new AdminProfileDAO();
        this.awardDAO = new AwardDAO();
    }

    public MemberService(UserDAO userDAO, MemberProfileDAO memberProfileDAO,
                         FileStorageDAO fileStorageDAO, AdminProfileDAO adminProfileDAO,
                         AwardDAO awardDAO) {
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
        this.fileStorageDAO = fileStorageDAO;
        this.awardDAO = awardDAO;
        this.adminProfileDAO = adminProfileDAO;
    }

    // ==================== 成员列表(分页) ====================

    public Result listMembers(Map<String, Object> filter, Integer page, Integer pageSize) {
        page = normalizePage(page);
        pageSize = normalizePageSize(pageSize);

        String keyword = extractFilterString(filter, "keyword");
        String role = extractFilterString(filter, "role");
        String status = extractFilterString(filter, "status");

        try {
            List<User> members = userDAO.findByConditions(keyword, role, status);
            int total = userDAO.count();
            return Result.ok(buildPageResult(members, total, page, pageSize));
        } catch (Exception e) {
            return Result.error(500, "获取成员列表失败: " + e.getMessage());
        }
    }

    // ==================== 成员详情(含档案) ====================

    public Result getMemberDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "成员ID不能为空");
        }

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(id);
        return Result.ok(buildMemberDetailResult(user, profile));
    }

    // ==================== 创建成员 ====================

    public Result createMember(Map<String, Object> dto) {
        Result validation = validateCreateMemberInput(dto);
        if (validation != null) return validation;

        String username = extractString(dto, "username");
        String password = extractString(dto, "password");
        String role = extractString(dto, "role");
        String email = extractString(dto, "email");
        String phone = extractString(dto, "phone");

        try {
            if (userDAO.findByUsername(username) != null) {
                return Result.error(400, "用户名已存在");
            }

            User user = buildUserForCreate(username, password, role, dto, email, phone);
            boolean inserted = userDAO.insert(user);
            if (!inserted) {
                return Result.error(500, "创建成员失败");
            }
            return Result.ok(user);
        } catch (Exception e) {
            return Result.error(500, "创建成员失败: " + e.getMessage());
        }
    }

    // ==================== 更新成员 ====================

    public Result updateMember(Integer id, Map<String, Object> dto, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "成员ID不能为空");
        }
        if (dto == null || dto.isEmpty()) {
            return Result.error(400, "更新信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = validateUpdateAuthorization(id, operatorId);
        if (authResult != null) return authResult;

        String email = extractString(dto, "email");
        String phone = extractString(dto, "phone");

        if (hasInvalidEmail(email) || hasInvalidPhone(phone)) {
            return email != null ? Result.error(400, "邮箱格式不正确") : Result.error(400, "手机号格式不正确");
        }

        try {
            applyMemberUpdates(user, dto, email, phone);
            boolean updated = userDAO.update(user);
            return updated ? Result.ok(user) : Result.error(500, "更新成员失败");
        } catch (Exception e) {
            return Result.error(500, "更新成员失败: " + e.getMessage());
        }
    }

    // ==================== 删除成员 ====================

    public Result deleteMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) return validation;

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId, "删除成员");
        if (authResult != null) return authResult;

        if (isSelfOperation(id, operatorId)) {
            return Result.error(400, "不能删除自己");
        }

        try {
            boolean deleted = userDAO.delete(id);
            return deleted ? Result.ok() : Result.error(500, "删除成员失败");
        } catch (Exception e) {
            return Result.error(500, "删除成员失败: " + e.getMessage());
        }
    }

    // ==================== 启用成员 ====================

    public Result enableMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) return validation;

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId, "启用成员");
        if (authResult != null) return authResult;

        return executeStatusUpdate(id, STATUS_ENABLED, "启用成员");
    }

    // ==================== 禁用成员 ====================

    public Result disableMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) return validation;

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId, "禁用成员");
        if (authResult != null) return authResult;

        if (isSelfOperation(id, operatorId)) {
            return Result.error(400, "不能禁用自己");
        }

        return executeStatusUpdate(id, STATUS_DISABLED, "禁用成员");
    }

    // ==================== 重置密码 ====================

    public Result resetPassword(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) return validation;

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId, "重置密码");
        if (authResult != null) return authResult;

        if (isSelfOperation(id, operatorId)) {
            return Result.error(400, "不能重置自己的密码");
        }

        try {
            boolean reset = userDAO.resetPassword(id, DEFAULT_RESET_PASSWORD);
            return reset ? Result.ok() : Result.error(500, "重置密码失败");
        } catch (Exception e) {
            return Result.error(500, "重置密码失败: " + e.getMessage());
        }
    }

    // ==================== 成员获奖列表 ====================

    public Result getMemberAwards(Integer id) {
        if (id == null) {
            return Result.error(400, "成员ID不能为空");
        }

        try {
            List<Award> awards = awardDAO.findByUserId(id);
            return Result.ok(awards);
        } catch (Exception e) {
            return Result.error(500, "获取获奖列表失败: " + e.getMessage());
        }
    }

    // ==================== 更新个人档案 ====================

    public Result updateProfile(Integer id, Map<String, Object> dto, Integer userId) {
        if (id == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (dto == null || dto.isEmpty()) {
            return Result.error(400, "档案信息不能为空");
        }

        Result authResult = validateProfileUpdateAuthorization(id, userId);
        if (authResult != null) return authResult;

        String studentId = extractString(dto, "studentId");
        String grade = extractString(dto, "grade");

        if (hasInvalidStudentId(studentId) || hasInvalidGrade(grade)) {
            return studentId != null ? Result.error(400, "学号格式错误") : Result.error(400, "年级格式错误");
        }

        try {
            MemberProfile profile = getOrCreateProfile(id);
            applyProfileUpdates(profile, dto, studentId, grade);
            boolean updated = saveProfile(profile);
            return updated ? Result.ok(profile) : Result.error(500, "更新档案失败");
        } catch (Exception e) {
            return Result.error(500, "更新档案失败: " + e.getMessage());
        }
    }

    // ==================== 获取个人档案 ====================

    public Result getProfile(Integer id) {
        if (id == null) {
            return Result.error(400, "用户ID不能为空");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(id);
        if (profile == null) {
            return Result.error(404, "档案不存在");
        }
        return Result.ok(profile);
    }

    // ==================== 上传头像 ====================

    public Result uploadAvatar(Integer id, InputStream file, String filename, Integer userId) {
        Result validation = validateAvatarUpload(id, file, filename);
        if (validation != null) return validation;

        Result authResult = validateAvatarUploadAuthorization(id, userId);
        if (authResult != null) return authResult;

        if (!isValidImageFile(filename)) {
            return Result.error(400, "只支持上传图片文件(jpg/jpeg/png/gif)");
        }

        try {
            MemberProfile profile = getOrCreateProfileForAvatar(id);
            Integer fileId = saveAvatarFile(filename, userId);
            if (fileId == null || fileId <= 0) {
                return Result.error(500, "保存头像文件失败");
            }
            profile.setAvatarFileId(fileId);
            memberProfileDAO.update(profile);
            return Result.ok(fileId);
        } catch (Exception e) {
            return Result.error(500, "上传头像失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    // ----- 分页相关 -----

    private int normalizePage(Integer page) {
        if (page == null || page <= 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    private Map<String, Object> buildPageResult(List<User> members, int total, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", members);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    private Map<String, Object> buildMemberDetailResult(User user, MemberProfile profile) {
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("profile", profile);
        return result;
    }

    // ----- 验证相关 -----

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "MEMBER".equals(role) || "TEACHER".equals(role);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }

    private boolean isValidStudentId(String studentId) {
        return studentId.matches("^\\d+$");
    }

    private boolean isValidGrade(String grade) {
        return grade.matches("^\\d{4}$");
    }

    private boolean isValidImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
               lower.endsWith(".png") || lower.endsWith(".gif");
    }

    private boolean hasInvalidEmail(String email) {
        return !isBlank(email) && !isValidEmail(email);
    }

    private boolean hasInvalidPhone(String phone) {
        return !isBlank(phone) && !isValidPhone(phone);
    }

    private boolean hasInvalidStudentId(String studentId) {
        return !isBlank(studentId) && !isValidStudentId(studentId);
    }

    private boolean hasInvalidGrade(String grade) {
        return !isBlank(grade) && !isValidGrade(grade);
    }

    // ----- 授权相关 -----

    private boolean isAdmin(Integer operatorId) {
        if (operatorId == null) return false;
        User operator = userDAO.findById(operatorId);
        return operator != null && "ADMIN".equals(operator.getRole());
    }

    private boolean isSelfOperation(Integer id, Integer operatorId) {
        return id != null && id.equals(operatorId);
    }

    private Result requireAdminRole(Integer operatorId, String operation) {
        if (!isAdmin(operatorId)) {
            return Result.error(403, "无权限" + operation);
        }
        return null;
    }

    private Result validateUpdateAuthorization(Integer id, Integer operatorId) {
        if (!isAdmin(operatorId) && !isSelfOperation(id, operatorId)) {
            return Result.error(403, "无权限更新此成员信息");
        }
        return null;
    }

    private Result validateProfileUpdateAuthorization(Integer id, Integer userId) {
        boolean isOwner = isSelfOperation(id, userId);
        if (!isOwner && !isAdmin(userId)) {
            return Result.error(403, "无权限更新此档案");
        }
        return null;
    }

    private Result validateAvatarUploadAuthorization(Integer id, Integer userId) {
        boolean isOwner = isSelfOperation(id, userId);
        if (!isOwner && !isAdmin(userId)) {
            return Result.error(403, "无权限上传此头像");
        }
        return null;
    }

    // ----- 参数解析 -----

    private Result validateIdAndOperator(Integer id, Integer operatorId, String idName, String operatorName) {
        if (id == null) {
            return Result.error(400, idName + "不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, operatorName + "不能为空");
        }
        return null;
    }

    private String extractString(Map<String, Object> dto, String key) {
        Object value = dto.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private String extractString(Map<String, Object> dto, String key, String defaultValue) {
        String value = extractString(dto, key);
        return isBlank(value) ? defaultValue : value;
    }

    private String extractFilterString(Map<String, Object> filter, String key) {
        if (filter == null) return null;
        Object value = filter.get(key);
        if (value == null) return null;
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    // ----- 业务逻辑辅助 -----

    private boolean isMemberExists(Integer id) {
        return userDAO.findById(id) != null;
    }

    private Result executeStatusUpdate(Integer id, Integer status, String operation) {
        try {
            boolean updated = userDAO.updateStatus(id, status);
            return updated ? Result.ok() : Result.error(500, operation + "失败");
        } catch (Exception e) {
            return Result.error(500, operation + "失败: " + e.getMessage());
        }
    }

    // ----- 创建成员相关 -----

    private Result validateCreateMemberInput(Map<String, Object> dto) {
        if (dto == null || dto.isEmpty()) {
            return Result.error(400, "成员信息不能为空");
        }

        String username = extractString(dto, "username");
        String password = extractString(dto, "password");
        String role = extractString(dto, "role");

        if (isBlank(username)) return Result.error(400, "用户名不能为空");
        if (isBlank(password)) return Result.error(400, "密码不能为空");
        if (isBlank(role)) return Result.error(400, "角色不能为空");
        if (!isValidRole(role)) return Result.error(400, "无效的角色");
        if (username.length() > MAX_USERNAME_LENGTH) {
            return Result.error(400, "用户名不能超过" + MAX_USERNAME_LENGTH + "个字符");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "密码不能少于" + MIN_PASSWORD_LENGTH + "个字符");
        }

        String email = extractString(dto, "email");
        String phone = extractString(dto, "phone");

        if (hasInvalidEmail(email)) return Result.error(400, "邮箱格式不正确");
        if (hasInvalidPhone(phone)) return Result.error(400, "手机号格式不正确");

        return null;
    }

    private User buildUserForCreate(String username, String password, String role,
                                    Map<String, Object> dto, String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(extractString(dto, "name", username));
        user.setEmail(isBlank(email) ? username + "@default.com" : email);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(STATUS_ENABLED);
        return user;
    }

    private void applyMemberUpdates(User user, Map<String, Object> dto, String email, String phone) {
        String name = extractString(dto, "name");
        if (!isBlank(name)) user.setName(name);
        if (!isBlank(email)) user.setEmail(email);
        if (!isBlank(phone)) user.setPhone(phone);
    }

    // ----- 档案相关 -----

    private MemberProfile getOrCreateProfile(Integer id) {
        MemberProfile profile = memberProfileDAO.findByUserId(id);
        if (profile == null) {
            profile = new MemberProfile();
            profile.setUserId(id);
        }
        return profile;
    }

    private void applyProfileUpdates(MemberProfile profile, Map<String, Object> dto,
                                     String studentId, String grade) {
        if (!isBlank(studentId)) profile.setStudentId(studentId);
        if (!isBlank(extractString(dto, "major"))) profile.setMajor(extractString(dto, "major"));
        if (!isBlank(grade)) profile.setGrade(grade);
        if (dto.containsKey("introduction")) profile.setIntroduction(extractString(dto, "introduction"));
        if (dto.containsKey("github")) profile.setGithub(extractString(dto, "github"));
        if (dto.containsKey("blog")) profile.setBlog(extractString(dto, "blog"));
    }

    private boolean saveProfile(MemberProfile profile) {
        if (profile.getId() == null) {
            return memberProfileDAO.insert(profile);
        } else {
            return memberProfileDAO.update(profile);
        }
    }

    // ----- 头像相关 -----

    private Result validateAvatarUpload(Integer id, InputStream file, String filename) {
        if (id == null) return Result.error(400, "用户ID不能为空");
        if (file == null) return Result.error(400, "文件不能为空");
        if (isBlank(filename)) return Result.error(400, "文件名不能为空");
        return null;
    }

    private MemberProfile getOrCreateProfileForAvatar(Integer id) {
        MemberProfile profile = memberProfileDAO.findByUserId(id);
        if (profile == null) {
            profile = new MemberProfile();
            profile.setUserId(id);
            memberProfileDAO.insert(profile);
        }
        return profile;
    }

    private Integer saveAvatarFile(String filename, Integer userId) {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setOriginalName(filename);
        fileStorage.setFileSize(0L);
        fileStorage.setFileType(getFileExtension(filename));
        fileStorage.setFilePath("localstorage/images/avatar/");
        fileStorage.setCreateBy(userId);
        return fileStorageDAO.insert(fileStorage);
    }
}
