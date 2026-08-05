package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.User;

/**
 * 用户数据访问接口
 */
public interface UserDAO {
    boolean insert(User user);
    User findById(Integer id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
