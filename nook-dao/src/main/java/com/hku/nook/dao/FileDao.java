package com.hku.nook.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.nook.domain.File;

@Mapper
public interface FileDao {
    
    Integer addFile(File file);

    File getFileByMD5(String fileMd5);
}
