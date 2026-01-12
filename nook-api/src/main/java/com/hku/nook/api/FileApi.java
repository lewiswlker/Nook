package com.hku.nook.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hku.nook.domain.JsonResponse;
import com.hku.nook.service.FileService;

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
    public JsonResponse<String> uploadFileBySlices(@RequestParam(value = "slice", required = false) MultipartFile slice,
                                                    @RequestParam(value = "multipartFile", required = false) MultipartFile multipartFile,
                                                    @RequestParam String fileMd5,
                                                    @RequestParam Integer sliceNo,
                                                    @RequestParam Integer totalSliceNo) throws Exception {
        MultipartFile file = slice != null ? slice : multipartFile;
        String filePath = fileService.uploadFileBySlices(file, fileMd5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);   
    }



}
