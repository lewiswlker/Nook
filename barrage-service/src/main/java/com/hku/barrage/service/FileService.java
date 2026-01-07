package com.hku.barrage.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hku.barrage.dao.FileDao;
import com.hku.barrage.domain.File;
import com.hku.barrage.service.util.FastDFSUtil;
import com.hku.barrage.service.util.MD5Util;

import io.netty.util.internal.StringUtil;

@Service
public class FileService {

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private FileDao fileDao;

    public String uploadFileBySlices(MultipartFile multipartFile, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        File dbFileMD5 = fileDao.getFileByMD5(fileMd5);
        if (dbFileMD5 != null) {
            return dbFileMD5.getUrl();
        }
        String url = fastDFSUtil.uploadFileBySlices(multipartFile, fileMd5, sliceNo, totalSliceNo);
        if (!StringUtil.isNullOrEmpty(url)) {
            dbFileMD5 = new File();
            dbFileMD5.setUrl(url);
            dbFileMD5.setCreateTime(new Date());
            dbFileMD5.setType(fastDFSUtil.getFileType(multipartFile));
            dbFileMD5.setMd5(fileMd5);
            fileDao.addFile(dbFileMD5);
        }
        return url;
    }

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }
    
}
