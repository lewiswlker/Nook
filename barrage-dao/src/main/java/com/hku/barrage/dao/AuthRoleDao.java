package com.hku.barrage.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.barrage.domain.auth.AuthRole;

@Mapper
public interface AuthRoleDao {

    AuthRole getRoleByCode(String code);
    
}
