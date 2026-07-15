package service;

import dao.FileStorageDAO;
import dao.MemberProfileDAO;
import dao.UserDAO;
import dto.ProfileDTO;
import model.FileStorage;
import model.MemberProfile;
import model.User;
import util.Result;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * 用户服务层
 */
public class UserService {

    private static final int MAX_AVATAR_SIZE = 500 * 1024;
    private static final int MAX_INTRODUCTION_LENGTH = 500;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private static final String AVATAR_CATEGORY = "images/avatar";
    private static final String AVATAR_STORAGE_PATH = "/localstorage/images/avatar/";
    private static final String DEFAULT_AVATAR_EXTENSION = ".png";
    private static final String ADMIN_ROLE = "ADMIN";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_-]+/?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLOG_URL_PATTERN = Pattern.compile(
            "^(https?://)?[A-Za-z0-9.-]+\\.[A-Za-z]{2,}/?$", Pattern.CASE_INSENSITIVE);

    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;
    private final FileStorageDAO fileStorageDAO;

    public UserService(UserDAO userDAO, MemberProfileDAO memberProfileDAO, FileStorageDAO fileStorageDAO) {
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
        this.fileStorageDAO = fileStorageDAO;
    }

    public Result login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return Result.error(400, "用户名或密码不能为空");
        }

        User existingUser = userDAO.findByUsername(username);
        if (existingUser == null) {
            return Result.error(404, "用户名或密码错误");
        }

        User user = userDAO.findByUsernameAndPassword(username, password);
        if (user == null) {
            return Result.error(400, "密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(403, "用户已被禁用");
        }

        return Result.ok(user);
    }

    public Result changePassword(Integer userId, String oldPwd, String newPwd) {
        Result validation = validateChangePasswordParams(userId, oldPwd, newPwd);
        if (validation != null) {
            return validation;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        boolean updated = userDAO.updatePassword(userId, newPwd);
        if (!updated) {
            return Result.error(500, "密码修改失败");
        }

        return Result.ok();
    }

    public Result updateProfile(Integer userId, ProfileDTO profileDTO) {
        Result validation = validateProfileUpdateParams(userId, profileDTO);
        if (validation != null) {
            return validation;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Result immutableCheck = checkImmutableFields(user, profileDTO);
        if (immutableCheck != null) {
            return immutableCheck;
        }

        MemberProfile existingProfile = memberProfileDAO.findByUserId(userId);
        return saveProfile(user, profileDTO, existingProfile);
    }

    public Result uploadAvatar(Integer userId, Object filePart) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (filePart == null) {
            return Result.error(400, "文件不能为空");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(userId);
        if (profile == null) {
            return Result.error(400, "用户档案不存在");
        }

        FileInfo fileInfo = extractFileInfo(filePart);
        if (fileInfo == null) {
            return Result.error(400, "文件信息获取失败");
        }

        Result fileValidation = validateAvatarFile(fileInfo.size, fileInfo.contentType);
        if (fileValidation != null) {
            return fileValidation;
        }

        return saveAvatarFile(profile, userId, fileInfo);
    }

    public Result getUserDetail(Integer userId) {
        if (userId == null || userId <= 0) {
            return Result.error(400, "用户ID无效");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(userId);
        return Result.ok(new UserDetailResult(user, profile));
    }

    public Result listMembers(Integer operatorId, String keyword, String role, int page, int pageSize) {
        Result authCheck = checkAdminPermission(operatorId);
        if (authCheck != null) {
            return authCheck;
        }

        page = normalizePage(page);
        pageSize = normalizePageSize(pageSize);

        return Result.ok(userDAO.findByConditions(keyword, role, null));
    }

    public Result updateAdminAvatar(Integer adminId, Object filePart) {
        if (adminId == null) {
            return Result.error(400, "管理员ID不能为空");
        }
        if (filePart == null) {
            return Result.error(400, "文件不能为空");
        }

        Result authCheck = checkAdminPermission(adminId);
        if (authCheck != null) {
            return authCheck;
        }

        MemberProfile profile = memberProfileDAO.findByUserId(adminId);
        if (profile == null) {
            return Result.error(400, "管理员档案不存在");
        }

        FileInfo fileInfo = extractFileInfo(filePart);
        if (fileInfo == null) {
            return Result.error(400, "文件信息获取失败");
        }

        Result fileValidation = validateAvatarFile(fileInfo.size, fileInfo.contentType);
        if (fileValidation != null) {
            return fileValidation;
        }

        return saveAvatarFile(profile, adminId, fileInfo);
    }

    // ==================== 验证方法 ====================

    private Result validateChangePasswordParams(Integer userId, String oldPwd, String newPwd) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(oldPwd)) {
            return Result.error(400, "旧密码不能为空");
        }
        if (isBlank(newPwd)) {
            return Result.error(400, "新密码不能为空");
        }
        if (newPwd.length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "新密码长度至少6位");
        }
        if (newPwd.length() > MAX_PASSWORD_LENGTH) {
            return Result.error(400, "新密码长度不能超过20位");
        }
        if (newPwd.equals(oldPwd)) {
            return Result.error(400, "新密码不能与旧密码相同");
        }
        return null;
    }

    private Result validateProfileUpdateParams(Integer userId, ProfileDTO profileDTO) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (profileDTO == null) {
            return Result.error(400, "档案信息不能为空");
        }

        Result fieldValidation = validateProfileFields(profileDTO);
        if (fieldValidation != null) {
            return fieldValidation;
        }
        return null;
    }

    private Result validateProfileFields(ProfileDTO profileDTO) {
        if (profileDTO.getEmail() != null) {
            String email = profileDTO.getEmail().trim();
            if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                return Result.error(400, "邮箱格式错误");
            }
        }

        if (profileDTO.getPhone() != null && !profileDTO.getPhone().trim().isEmpty()
                && !PHONE_PATTERN.matcher(profileDTO.getPhone().trim()).matches()) {
            return Result.error(400, "手机号格式错误");
        }

        if (profileDTO.getBirthday() != null && !profileDTO.getBirthday().trim().isEmpty()
                && !DATE_PATTERN.matcher(profileDTO.getBirthday().trim()).matches()) {
            return Result.error(400, "生日格式错误");
        }

        if (profileDTO.getIntroduction() != null
                && profileDTO.getIntroduction().length() > MAX_INTRODUCTION_LENGTH) {
            return Result.error(400, "简介不能超过500字符");
        }

        if (profileDTO.getGithub() != null && !profileDTO.getGithub().trim().isEmpty()
                && !GITHUB_URL_PATTERN.matcher(profileDTO.getGithub().trim()).matches()) {
            return Result.error(400, "GitHub链接格式错误");
        }

        if (profileDTO.getBlog() != null && !profileDTO.getBlog().trim().isEmpty()
                && !BLOG_URL_PATTERN.matcher(profileDTO.getBlog().trim()).matches()) {
            return Result.error(400, "博客链接格式错误");
        }

        return null;
    }

    private Result validateAvatarFile(long fileSize, String contentType) {
        if (fileSize <= 0) {
            return Result.error(400, "文件不能为空");
        }
        if (fileSize > MAX_AVATAR_SIZE) {
            return Result.error(400, "文件大小不能超过500KB");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }
        return null;
    }

    // ==================== 权限检查 ====================

    private Result checkAdminPermission(Integer operatorId) {
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }

        User operator = userDAO.findById(operatorId);
        if (operator == null) {
            return Result.error(404, "操作者不存在");
        }
        if (!ADMIN_ROLE.equals(operator.getRole())) {
            return Result.error(403, "无权限操作");
        }
        return null;
    }

    // ==================== 业务逻辑方法 ====================

    private Result checkImmutableFields(User user, ProfileDTO profileDTO) {
        if (profileDTO.getName() != null && !profileDTO.getName().equals(user.getName())) {
            return Result.error(400, "姓名不可修改");
        }

        MemberProfile existingProfile = memberProfileDAO.findByUserId(user.getId());
        if (profileDTO.getStudentId() != null && existingProfile != null
                && !profileDTO.getStudentId().equals(existingProfile.getStudentId())) {
            return Result.error(400, "学号不可修改");
        }
        return null;
    }

    private Result saveProfile(User user, ProfileDTO profileDTO, MemberProfile existingProfile) {
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }

        try {
            userDAO.update(user);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                return Result.error(400, "邮箱已被使用");
            }
            throw e;
        }

        MemberProfile profile = buildProfileFromDTO(user.getId(), profileDTO, existingProfile);
        if (existingProfile != null) {
            memberProfileDAO.update(profile);
        } else {
            profile.setStudentId(profileDTO.getStudentId() != null ? profileDTO.getStudentId() : "");
            memberProfileDAO.insert(profile);
        }

        return Result.ok();
    }

    private MemberProfile buildProfileFromDTO(Integer userId, ProfileDTO profileDTO, MemberProfile existing) {
        MemberProfile profile = existing != null ? existing : new MemberProfile();
        profile.setUserId(userId);

        if (profileDTO.getBirthday() != null && !profileDTO.getBirthday().trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                profile.setBirthday(sdf.parse(profileDTO.getBirthday()));
            } catch (Exception e) {
                return profile;
            }
        }

        maybeSetField(profileDTO.getMajor(), profile::setMajor);
        maybeSetField(profileDTO.getGrade(), profile::setGrade);
        maybeSetField(profileDTO.getIntroduction(), profile::setIntroduction);
        maybeSetField(profileDTO.getGithub(), profile::setGithub);
        maybeSetField(profileDTO.getBlog(), profile::setBlog);

        return profile;
    }

    private Result saveAvatarFile(MemberProfile profile, Integer userId, FileInfo fileInfo) {
        FileStorage fileStorage = createAvatarFileStorage(userId, fileInfo);

        Integer fileId = fileStorageDAO.insert(fileStorage);
        if (fileId == null) {
            return Result.error(500, "文件保存失败");
        }

        profile.setAvatarFileId(fileId);
        memberProfileDAO.update(profile);

        return Result.ok();
    }

    private FileStorage createAvatarFileStorage(Integer userId, FileInfo fileInfo) {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(userId);
        fileStorage.setOriginalName(fileInfo.fileName);
        fileStorage.setStoredName(generateAvatarStoredName(userId, fileInfo.fileName));
        fileStorage.setFilePath(AVATAR_STORAGE_PATH + fileStorage.getStoredName());
        fileStorage.setFileType(fileInfo.contentType);
        fileStorage.setFileSize(fileInfo.size);
        fileStorage.setCategory(AVATAR_CATEGORY);
        fileStorage.setStatus(1);
        return fileStorage;
    }

    // ==================== 工具方法 ====================

    private FileInfo extractFileInfo(Object filePart) {
        try {
            long size = (Long) filePart.getClass().getMethod("getSize").invoke(filePart);
            String contentType = (String) filePart.getClass().getMethod("getContentType").invoke(filePart);
            String fileName = (String) filePart.getClass().getMethod("getSubmittedFileName").invoke(filePart);
            return new FileInfo(size, contentType, fileName);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateAvatarStoredName(Integer userId, String fileName) {
        return "avatar_" + userId + "_" + System.currentTimeMillis() + getFileExtension(fileName);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return DEFAULT_AVATAR_EXTENSION;
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private int normalizePage(int page) {
        return page <= 0 ? 1 : page;
    }

    private int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void maybeSetField(String value, java.util.function.Consumer<String> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    // ==================== 内部类 ====================

    private static class FileInfo {
        final long size;
        final String contentType;
        final String fileName;

        FileInfo(long size, String contentType, String fileName) {
            this.size = size;
            this.contentType = contentType;
            this.fileName = fileName;
        }
    }

    public static class UserDetailResult {
        private final User user;
        private final MemberProfile profile;

        public UserDetailResult(User user, MemberProfile profile) {
            this.user = user;
            this.profile = profile;
        }

        public User getUser() {
            return user;
        }

        public MemberProfile getProfile() {
            return profile;
        }
    }
}