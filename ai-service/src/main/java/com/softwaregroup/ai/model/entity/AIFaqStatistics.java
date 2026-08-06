package com.softwaregroup.ai.model.entity;

import java.util.Date;

/**
 * AI FAQ统计实体
 */
public class AIFaqStatistics {
    private Integer id;
    private String questionHash;
    private String normalizedQuestion;
    private Integer queryCount;
    private Double avgRating;
    private Date lastQueryAt;

    public AIFaqStatistics() {
        this.queryCount = 0;
        this.avgRating = 0.0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestionHash() {
        return questionHash;
    }

    public void setQuestionHash(String questionHash) {
        this.questionHash = questionHash;
    }

    public String getNormalizedQuestion() {
        return normalizedQuestion;
    }

    public void setNormalizedQuestion(String normalizedQuestion) {
        this.normalizedQuestion = normalizedQuestion;
    }

    public Integer getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(Integer queryCount) {
        this.queryCount = queryCount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Date getLastQueryAt() {
        return lastQueryAt;
    }

    public void setLastQueryAt(Date lastQueryAt) {
        this.lastQueryAt = lastQueryAt;
    }
}
