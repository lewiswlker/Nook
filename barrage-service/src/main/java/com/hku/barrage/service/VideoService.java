package com.hku.barrage.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import com.hku.barrage.dao.VideoDao;
import com.hku.barrage.domain.JsonResponse;
import com.hku.barrage.domain.PageResult;
import com.hku.barrage.domain.Video;
import com.hku.barrage.domain.VideoTag;
import com.hku.barrage.domain.exception.ConditionException;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private VideoService videoService;

    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> videoTagList = video.getVideoTagList();
        videoTagList.forEach(videoTag -> {
            videoTag.setVideoId(videoId);
            videoTag.setCreateTime(now);
        });
        videoDao.batchAddVideoTags(videoTagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if(size == null || no == null){
            throw new ConditionException("Illegal parameters");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no-1)*size);
        params.put("limit", size);
        params.put("area" , area);
        List<Video> list = new ArrayList<>();
        Integer total = videoDao.pageCountVideos(params);
        if(total > 0){
            list = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total, list);
    }

    

    

    
}
