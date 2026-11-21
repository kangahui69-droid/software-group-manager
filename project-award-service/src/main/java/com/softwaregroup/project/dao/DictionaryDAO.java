package com.softwaregroup.project.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DictionaryDAO {
    private final JdbcTemplate jdbc;

    public DictionaryDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String findByCode(String code) {
        List<String> names = jdbc.query("SELECT name FROM dictionary WHERE code=? AND status=1 " +
                "ORDER BY sort_order,id LIMIT 1", (rs, row) -> rs.getString("name"), code);
        return names.isEmpty() ? null : names.get(0);
    }

    public List<Object> findByCategory(String category) {
        return new ArrayList<>(jdbc.queryForList("SELECT id,code,name,type,sort_order FROM dictionary " +
                "WHERE type=? AND status=1 ORDER BY sort_order,id", category));
    }
}
