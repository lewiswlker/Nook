package com.hku.nook.dao;

import org.apache.ibatis.annotations.Mapper;

import com.hku.nook.domain.auth.AuthRole;

@Mapper
public interface AuthRoleDao {

    AuthRole getRoleByCode(String code);
    
}
