package com.hku.nook.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hku.nook.domain.UserPreference;
import com.hku.nook.domain.Video;
import com.hku.nook.domain.VideoBinaryPicture;
import com.hku.nook.domain.VideoCoin;
import com.hku.nook.domain.VideoCollection;
import com.hku.nook.domain.VideoComment;
import com.hku.nook.domain.VideoLike;
import com.hku.nook.domain.VideoTag;
import com.hku.nook.domain.VideoView;

@Mapper
public interface VideoDao {
    
    Integer addVideos(Video video);

    Integer batchAddVideoTags(List<VideoTag> videoTagList);

    Integer pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Video getVideoById(Long id);

    VideoLike getVideoLikeByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    Integer addVideoLike(VideoLike videoLike);

    Integer deleteVideoLike(@Param("videoId") Long videoId,
                            @Param("userId") Long userId);

    Long getVideoLikes(Long videoId);

    Integer deleteVideoCollection(@Param("videoId") Long videoId,
                                  @Param("userId") Long userId);

    Integer addVideoCollection(VideoCollection videoCollection);

    Long getVideoCollections(@Param("videoId") Long videoId);

    VideoCollection getVideoCollectionByVideoIdAndUserId(@Param("videoId") Long videoId,
                                                         @Param("userId") Long userId);

    VideoCoin getVideoCoinByVideoIdAndUserId(@Param("videoId") Long videoId,
                                             @Param("userId") Long userId);

    Integer addVideoCoin(VideoCoin videoCoin);

    Integer updateVideoCoin(VideoCoin videoCoin);

    Long getVideoCoinsAmount(Long videoId);

    Integer addVideoComment(VideoComment videoComment);

    Integer pageCountVideoComments(Map<String, Object> params);

    List<VideoComment> pageListVideoComments(Map<String, Object> params);

    List<VideoComment> batchGetVideoCommentsByRootIds(@Param("rootIdList") List<Long> rootIdList);

    Video getVideoDetails(Long videoId);

    Integer addVideoView(VideoView videoView);

    Integer getVideoViewCounts(Long videoId);

    VideoView getVideoView(Map<String, Object> params);

    List<UserPreference> getAllUserPreference();

    List<Video> batchGetVideosByIds(@Param("idList") List<Long> idList);

    Integer batchAddVideoBinaryPictures(@Param("pictureList") List<VideoBinaryPicture> pictureList);

    List<VideoTag> getVideoTagsByVideoId(Long videoId);

    Integer deleteVideoTags(@Param("tagIdList") List<Long> tagIdList,
                            @Param("videoId") Long videoId);

    List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params);
}
