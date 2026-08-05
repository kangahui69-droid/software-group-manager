package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.FileStorage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储数据访问层
 */
@Repository
public class FileStorageDAO {

    public List<FileStorage> findByIds(List<Integer> ids) {
        return new ArrayList<>();
    }

    public FileStorage findById(Integer id) {
        return null;
    }
}
