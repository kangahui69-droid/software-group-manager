package com.softwaregroup.project.dao;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典数据访问层
 */
@Repository
public class DictionaryDAO {

    public String findByCode(String code) {
        return null;
    }

    public List<Object> findByCategory(String category) {
        return new ArrayList<>();
    }
}
