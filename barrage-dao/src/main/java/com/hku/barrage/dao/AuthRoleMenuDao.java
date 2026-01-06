package com.hku.barrage.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hku.barrage.domain.auth.AuthRoleMenu;

@Mapper
public interface AuthRoleMenuDao {

    List<AuthRoleMenu> getRoleMenusByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);

}
