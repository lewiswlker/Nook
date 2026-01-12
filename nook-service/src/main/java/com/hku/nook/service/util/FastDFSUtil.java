package com.hku.nook.service.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.hku.nook.domain.exception.ConditionException;

import io.netty.util.internal.StringUtil;


@Component
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
            String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
            Integer uploadedNo = StringUtil.isNullOrEmpty(uploadedNoStr) ? 0 : Integer.valueOf(uploadedNoStr);
            if (slicesNo != uploadedNo + 1) {
                throw new ConditionException("Illegal File Slice Order!");
            }
            appendFileStorageClient.appendFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize());
            redisTemplate.opsForValue().set(uploadedNoKey, String.valueOf(slicesNo));
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

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    // download
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String path) throws Exception{
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long totalFileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr + path;
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        String rangeStr = request.getHeader("Range");
        String[] range;
        if(StringUtil.isNullOrEmpty(rangeStr)){
            rangeStr = "bytes=0-" + (totalFileSize-1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if(range.length >= 2){
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize-1;
        if(range.length >= 3){
            end = Long.parseLong(range[2]);
        }
        long len = end - begin + 1;
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int)len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url, headers, response);
    }

    public void downLoadFile(String url, String localPath) {
        fastFileStorageClient.downloadFile(DEFAULT_GROUP, url,
                new DownloadCallback<String>() {
                    @Override
                    public String recv(InputStream ins) throws IOException {
                        File file = new File(localPath);
                        OutputStream os = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = ins.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        ins.close();
                        return "success";
                    }
                });
    }
}
