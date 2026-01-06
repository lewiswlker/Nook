package com.hku.barrage.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.barrage.domain.UserMoment;

@Mapper
public interface UserMomentsDao {

    void addUserMoments(UserMoment userMoment);

}
