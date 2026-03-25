package model;

import java.util.Date;

public class AIKnowledgeBase {
    private Integer id;
    private String category;
    private String question;
    private String answer;
    private String keywords;
    private Integer status;
    private Date createdAt;

    public AIKnowledgeBase() {
    }

    public AIKnowledgeBase(String category, String question, String answer, String keywords) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.keywords = keywords;
        this.status = 1;
        this.createdAt = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}