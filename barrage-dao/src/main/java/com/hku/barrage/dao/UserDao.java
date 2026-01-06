package com.hku.barrage.dao;

import com.alibaba.fastjson.JSONObject;
import com.hku.barrage.domain.RefreshTokenDetail;
import com.hku.barrage.domain.User;
import com.hku.barrage.domain.UserInfo;

import io.lettuce.core.dynamic.annotation.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserDao {
    User getUserByPhone(String phone);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    User getUserById(Long id);

    UserInfo getUserInfoByUserId(Long userId);

    Integer updateUsers(User user);

    User getUserByPhoneOrEmail(String phoneOrEmail);

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    Integer pageCountUserInfos(Map<String, Object> params);

    List<UserInfo> pageListUserInfos(Map<String, Object> params);

    void deleteRefreshToken(@Param("refreshToken") String refreshToken, 
                            @Param("userId") Long userId);

    void addRefreshToken(@Param("refreshToken") String refreshToken, 
                        @Param("userId") Long userId, 
                        @Param("createTime") Date createTime);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);
}
