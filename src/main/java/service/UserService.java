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
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 用户服务层
 *
 * Service方法统一返回Result，不做JSON序列化，不做forward/redirect
 * 只接受普通参数/DTO、执行业务、返回Result
 */
public class UserService {

    private static final int MAX_AVATAR_SIZE = 500 * 1024; // 500KB
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("^(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_-]+/?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLOG_URL_PATTERN = Pattern.compile("^(https?://)?[A-Za-z0-9.-]+\\.[A-Za-z]{2,}/?$", Pattern.CASE_INSENSITIVE);

    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;
    private final FileStorageDAO fileStorageDAO;

    public UserService(UserDAO userDAO, MemberProfileDAO memberProfileDAO, FileStorageDAO fileStorageDAO) {
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
        this.fileStorageDAO = fileStorageDAO;
    }

    /**
     * 登录验证
     */
    public Result login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Result.error(400, "用户名或密码不能为空");
        }

        // 先检查用户名是否存在
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

    /**
     * 修改密码
     */
    public Result changePassword(Integer userId, String oldPwd, String newPwd) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (oldPwd == null || oldPwd.trim().isEmpty()) {
            return Result.error(400, "旧密码不能为空");
        }
        if (newPwd == null || newPwd.trim().isEmpty()) {
            return Result.error(400, "新密码不能为空");
        }
        if (newPwd.length() < 6) {
            return Result.error(400, "新密码长度至少6位");
        }
        if (newPwd.length() > 20) {
            return Result.error(400, "新密码长度不能超过20位");
        }
        if (newPwd.equals(oldPwd)) {
            return Result.error(400, "新密码不能与旧密码相同");
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

    /**
     * 更新个人档案
     */
    public Result updateProfile(Integer userId, ProfileDTO profileDTO) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (profileDTO == null) {
            return Result.error(400, "档案信息不能为空");
        }

        // 验证邮箱格式（空白字符也视为错误）
        if (profileDTO.getEmail() != null) {
            String email = profileDTO.getEmail().trim();
            if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                return Result.error(400, "邮箱格式错误");
            }
        }

        // 验证手机号格式
        if (profileDTO.getPhone() != null && !profileDTO.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(profileDTO.getPhone().trim()).matches()) {
                return Result.error(400, "手机号格式错误");
            }
        }

        // 验证生日格式
        if (profileDTO.getBirthday() != null && !profileDTO.getBirthday().trim().isEmpty()) {
            if (!DATE_PATTERN.matcher(profileDTO.getBirthday().trim()).matches()) {
                return Result.error(400, "生日格式错误");
            }
        }

        // 验证简介长度
        if (profileDTO.getIntroduction() != null && profileDTO.getIntroduction().length() > 500) {
            return Result.error(400, "简介不能超过500字符");
        }

        // 验证GitHub URL
        if (profileDTO.getGithub() != null && !profileDTO.getGithub().trim().isEmpty()) {
            if (!GITHUB_URL_PATTERN.matcher(profileDTO.getGithub().trim()).matches()) {
                return Result.error(400, "GitHub链接格式错误");
            }
        }

        // 验证博客URL
        if (profileDTO.getBlog() != null && !profileDTO.getBlog().trim().isEmpty()) {
            if (!BLOG_URL_PATTERN.matcher(profileDTO.getBlog().trim()).matches()) {
                return Result.error(400, "博客链接格式错误");
            }
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 检查姓名是否被修改（不可修改）
        if (profileDTO.getName() != null && !profileDTO.getName().equals(user.getName())) {
            return Result.error(400, "姓名不可修改");
        }

        MemberProfile existingProfile = memberProfileDAO.findByUserId(userId);

        // 检查学号是否被修改（不可修改）
        if (profileDTO.getStudentId() != null && existingProfile != null
                && !profileDTO.getStudentId().equals(existingProfile.getStudentId())) {
            return Result.error(400, "学号不可修改");
        }

        // 更新用户信息
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

        // 创建或更新档案
        MemberProfile profile = existingProfile != null ? existingProfile : new MemberProfile();
        profile.setUserId(userId);

        if (profileDTO.getBirthday() != null && !profileDTO.getBirthday().trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                profile.setBirthday(sdf.parse(profileDTO.getBirthday()));
            } catch (Exception e) {
                return Result.error(400, "生日格式错误");
            }
        }
        if (profileDTO.getMajor() != null) {
            profile.setMajor(profileDTO.getMajor());
        }
        if (profileDTO.getGrade() != null) {
            profile.setGrade(profileDTO.getGrade());
        }
        if (profileDTO.getIntroduction() != null) {
            profile.setIntroduction(profileDTO.getIntroduction());
        }
        if (profileDTO.getGithub() != null) {
            profile.setGithub(profileDTO.getGithub());
        }
        if (profileDTO.getBlog() != null) {
            profile.setBlog(profileDTO.getBlog());
        }

        if (existingProfile != null) {
            memberProfileDAO.update(profile);
        } else {
            profile.setStudentId(profileDTO.getStudentId() != null ? profileDTO.getStudentId() : "");
            memberProfileDAO.insert(profile);
        }

        return Result.ok();
    }

    /**
     * 上传头像
     */
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

        long fileSize = 0;
        String contentType = "";
        String fileName = "avatar.png";

        try {
            fileSize = (Long) filePart.getClass().getMethod("getSize").invoke(filePart);
            contentType = (String) filePart.getClass().getMethod("getContentType").invoke(filePart);
            fileName = (String) filePart.getClass().getMethod("getSubmittedFileName").invoke(filePart);
        } catch (Exception e) {
            return Result.error(400, "文件信息获取失败");
        }

        if (fileSize <= 0) {
            return Result.error(400, "文件不能为空");
        }
        if (fileSize > MAX_AVATAR_SIZE) {
            return Result.error(400, "文件大小不能超过500KB");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }

        // 创建文件存储记录
        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(userId);
        fileStorage.setOriginalName(fileName);
        fileStorage.setStoredName("avatar_" + userId + "_" + System.currentTimeMillis() + getFileExtension(fileName));
        fileStorage.setFilePath("/localstorage/images/avatar/" + fileStorage.getStoredName());
        fileStorage.setFileType(contentType);
        fileStorage.setFileSize(fileSize);
        fileStorage.setCategory("images/avatar");
        fileStorage.setStatus(1);

        Integer fileId = fileStorageDAO.insert(fileStorage);
        if (fileId == null) {
            return Result.error(500, "文件保存失败");
        }

        // 更新用户头像
        profile.setAvatarFileId(fileId);
        memberProfileDAO.update(profile);

        return Result.ok();
    }

    /**
     * 获取用户详情（含profile）
     */
    public Result getUserDetail(Integer userId) {
        if (userId == null || userId <= 0) {
            return Result.error(400, "用户ID无效");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(userId);

        // 返回用户和档案的组合结果
        return Result.ok(new UserDetailResult(user, profile));
    }

    /**
     * 成员列表（admin）
     */
    public Result listMembers(Integer operatorId, String keyword, String role, int page, int pageSize) {
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }

        User operator = userDAO.findById(operatorId);
        if (operator == null) {
            return Result.error(404, "操作者不存在");
        }
        if (!"ADMIN".equals(operator.getRole())) {
            return Result.error(403, "无权限操作");
        }

        // 验证分页参数
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        java.util.List<User> users = userDAO.findByConditions(keyword, role, null);
        return Result.ok(users);
    }

    /**
     * 管理员头像更新
     */
    public Result updateAdminAvatar(Integer adminId, Object filePart) {
        if (adminId == null) {
            return Result.error(400, "管理员ID不能为空");
        }
        if (filePart == null) {
            return Result.error(400, "文件不能为空");
        }

        User admin = userDAO.findById(adminId);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }
        if (!"ADMIN".equals(admin.getRole())) {
            return Result.error(403, "无权限操作");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(adminId);
        if (profile == null) {
            return Result.error(400, "管理员档案不存在");
        }

        long fileSize = 0;
        String contentType = "";
        String fileName = "avatar.png";

        try {
            fileSize = (Long) filePart.getClass().getMethod("getSize").invoke(filePart);
            contentType = (String) filePart.getClass().getMethod("getContentType").invoke(filePart);
            fileName = (String) filePart.getClass().getMethod("getSubmittedFileName").invoke(filePart);
        } catch (Exception e) {
            return Result.error(400, "文件信息获取失败");
        }

        if (fileSize <= 0) {
            return Result.error(400, "文件不能为空");
        }
        if (fileSize > MAX_AVATAR_SIZE) {
            return Result.error(400, "文件大小不能超过500KB");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }

        // 创建文件存储记录
        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(adminId);
        fileStorage.setOriginalName(fileName);
        fileStorage.setStoredName("avatar_" + adminId + "_" + System.currentTimeMillis() + getFileExtension(fileName));
        fileStorage.setFilePath("/localstorage/images/avatar/" + fileStorage.getStoredName());
        fileStorage.setFileType(contentType);
        fileStorage.setFileSize(fileSize);
        fileStorage.setCategory("images/avatar");
        fileStorage.setStatus(1);

        Integer fileId = fileStorageDAO.insert(fileStorage);
        if (fileId == null) {
            return Result.error(500, "文件保存失败");
        }

        // 更新管理员头像
        profile.setAvatarFileId(fileId);
        memberProfileDAO.update(profile);

        return Result.ok();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return ".png";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    /**
     * 用户详情结果包装类
     */
    public static class UserDetailResult {
        private User user;
        private MemberProfile profile;

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