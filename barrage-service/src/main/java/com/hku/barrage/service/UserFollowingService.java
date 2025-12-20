package com.hku.barrage.service;

import com.hku.barrage.dao.UserFollowingDao;
import com.hku.barrage.domain.FollowingGroup;
import com.hku.barrage.domain.User;
import com.hku.barrage.domain.UserFollowing;
import com.hku.barrage.domain.UserInfo;
import com.hku.barrage.domain.constant.FollowingConstant;
import com.hku.barrage.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            FollowingGroup followingGroup = followingGroupService.getByType(FollowingConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null) {
                throw new ConditionException("Following group not exist!");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new ConditionException("The user you want to follow is not exist!");
        }
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowings(userFollowing);
    }

    public List<FollowingGroup> getUserFollowings(Long userId) {
        // 获取关注列表
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        // 获取关注列表的博主的 info
        if (followingIdSet.size() > 0) {
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        for (UserFollowing userFollowing : list) {
            for (UserInfo userInfo : userInfoList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    // 设置关注列表的博主的 info
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(FollowingConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);
        for (FollowingGroup group : groupList) {
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing : list){
                if (group.getId().equals(userFollowing.getGroupId())) {
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }
        return result;
    }

    public List<UserFollowing> getUserFans(Long userId) {
        // fans list
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0) {
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);
        for (UserFollowing fan : fanList) {
            for (UserInfo userInfo : userInfoList) {
                if (fan.getUserId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for(UserFollowing following : followingList){
                if (following.getFollowingId().equals(fan.getUserId())) {
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(FollowingConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> addUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(Long userId, List<UserInfo> userInfoList) {
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);
        for (UserInfo userInfo : userInfoList) {
            userInfo.setFollowed(false);
            for (UserFollowing userFollowing : followingList) {
                if (userInfo.getUserId().equals(userFollowing.getFollowingId())) {
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
