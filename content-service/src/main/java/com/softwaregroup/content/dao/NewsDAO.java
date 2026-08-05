package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.News;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻数据访问层
 */
@Repository
public class NewsDAO {

    public List<News> findByConditions(String keyword, String type, Integer status) {
        return new ArrayList<>();
    }

    public List<News> findByType(String type) {
        return new ArrayList<>();
    }

    public News findById(Integer id) {
        return null;
    }

    public boolean insert(News news) {
        return true;
    }

    public boolean update(News news) {
        return true;
    }

    public boolean updateStatus(Integer id, Integer status) {
        return true;
    }
}
