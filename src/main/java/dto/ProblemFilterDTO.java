package dto;

/**
 * 问题筛选条件数据传输对象
 */
public class ProblemFilterDTO {
    private String category;
    private String status;
    private Integer userId;
    private String keyword;

    // 分类常量
    public static final String CATEGORY_VERIFIED = "VERIFIED";
    public static final String CATEGORY_UNVERIFIED = "UNVERIFIED";
    public static final String CATEGORY_INVALID = "INVALID";

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SOLVING = "SOLVING";
    public static final String STATUS_SOLVED = "SOLVED";
    public static final String STATUS_UNSOLVED = "UNSOLVED";

    public ProblemFilterDTO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}