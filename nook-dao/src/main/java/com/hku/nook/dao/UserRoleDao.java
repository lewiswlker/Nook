package com.hku.nook.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.nook.domain.auth.UserRole;

import java.util.*;


@Mapper
public interface UserRoleDao {

    List<UserRole> getUserRoleByUserId(Long userId);

    void addUserRole(UserRole userRole);

}
