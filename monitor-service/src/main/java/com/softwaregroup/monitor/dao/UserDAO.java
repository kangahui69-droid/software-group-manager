package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.User;

/**
 * 用户数据访问接口
 */
public interface UserDAO {
    User findById(Integer id);
}
