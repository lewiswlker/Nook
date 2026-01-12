package com.hku.nook.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.nook.domain.UserMoment;

@Mapper
public interface UserMomentsDao {

    void addUserMoments(UserMoment userMoment);

}
