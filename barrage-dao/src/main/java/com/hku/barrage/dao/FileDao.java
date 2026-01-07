package com.hku.barrage.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.barrage.domain.File;

@Mapper
public interface FileDao {
    
    Integer addFile(File file);

    File getFileByMD5(String fileMd5);
}
