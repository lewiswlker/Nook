package com.hku.nook.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hku.nook.domain.auth.AuthRoleMenu;

@Mapper
public interface AuthRoleMenuDao {

    List<AuthRoleMenu> getRoleMenusByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);

}
