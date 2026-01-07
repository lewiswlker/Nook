package com.hku.barrage.service.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.hku.barrage.domain.exception.ConditionException;

import io.netty.util.internal.StringUtil;

public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";

    private static final String PATH_KEY = "path-key";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key";

    private static final String UPLOADED_NO_KEY = "uploaded-no-key";

    private static final int SLICE_SIZE = 1024 * 1024 * 2;

    public String getFileType(MultipartFile file) {
        if (file == null) {
            throw new ConditionException("Illegal File!");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    // upload
    public String uploadCommonFile(MultipartFile file) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType,
                metaDataSet);
        return storePath.getPath();
    }

    // resumable transfer
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer slicesNo, Integer slicesTotalNo)
            throws Exception {
        if (file == null || slicesNo == null || slicesTotalNo == null) {
            throw new ConditionException("Illegal File Slice!");
        }
        String pathKey = PATH_KEY + fileMd5;
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;

        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = StringUtil.isNullOrEmpty(uploadedSizeStr) ? 0L : Long.valueOf(uploadedSizeStr);
        String fileType = getFileType(file);
        if (slicesNo == 1) {
            String path = uploadAppendFile(file);
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("Upload failed!");
            }
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else {
            String filePath = redisTemplate.opsForValue().get(pathKey);
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("Upload failed!");
            }
            modifyAppenderFileString(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        // update uploaded size and number
        uploadedSize += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        // all slices uploaded
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = StringUtil.isNullOrEmpty(uploadedNoStr) ? 0 : Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(slicesTotalNo)) {
            // clean redis keys
            String filePath = redisTemplate.opsForValue().get(pathKey);
            resultPath = filePath;
            redisTemplate.delete(pathKey);
            redisTemplate.delete(uploadedSizeKey);
            redisTemplate.delete(uploadedNoKey);
        }
        return resultPath;
    }

    public String uploadAppendFile(MultipartFile file) throws Exception {
        String fileType = getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(),
                file.getSize(), fileType);
        return storePath.getPath();
    }

    public void modifyAppenderFileString(MultipartFile file, String filePath, Long offset) throws Exception {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), offset, file.getSize());
    }

    // split the file
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception {
        String fileType = getFileType(multipartFile);
        File file = multipartFileToFile(multipartFile);
        long fileLength = file.length();
        int count = 1;
        for(int i = 0; i < fileLength; i += SLICE_SIZE) {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(i);
            byte[] bytes = new byte[SLICE_SIZE];
            int len = raf.read(bytes);
            String path = "/Users/liubo/Downloads/slice/" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            raf.close();
            count++;
        }
        file.delete();
    }

    public File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        String originalFileName = multipartFile.getOriginalFilename();
        String[] fileName = originalFileName.split("\\.");
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }

    // delete
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }

    // download
}
