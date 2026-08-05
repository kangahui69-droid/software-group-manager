package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.MemberProfile;

/**
 * 成员档案数据访问接口
 */
public interface MemberProfileDAO {
    boolean insert(MemberProfile profile);
    MemberProfile findById(Integer id);
    MemberProfile findByUserId(Integer userId);
}
