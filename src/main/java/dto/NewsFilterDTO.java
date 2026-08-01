package dto;

/**
 * 新闻筛选条件数据传输对象
 */
public class NewsFilterDTO {
    private String keyword;
    private String type;
    private Integer status;

    // 状态常量
    public static final Integer STATUS_PUBLISHED = 1;
    public static final Integer STATUS_DELETED = 0;

    // 类型常量
    public static final String TYPE_AWARD = "award";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_NOTICE = "notice";

    public NewsFilterDTO() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
