package com.hku.barrage.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hku.barrage.domain.JsonResponse;
import com.hku.barrage.service.FileService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class FileApi {
    
    @Autowired
    private FileService fileService;

    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception {
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<String>(fileMD5);
    }
    

    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile multipartFile,
                                                    String fileMd5,
                                                    Integer sliceNo,
                                                    Integer totalSliceNo) throws Exception {
        String filePath = fileService.uploadFileBySlices(multipartFile, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);   
    }



}
