package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.AwardImage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖项图片数据访问层
 */
@Repository
public class AwardImageDAO {

    public List<AwardImage> findByAwardId(Integer awardId) {
        return new ArrayList<>();
    }

    public boolean insert(AwardImage image) {
        return true;
    }

    public boolean deleteByAwardId(Integer awardId) {
        return true;
    }
}
