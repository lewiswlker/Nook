package com.hku.nook.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hku.nook.domain.auth.AuthRoleElementOperation;

@Mapper
public interface AuthRoleElementOperationDao {

    List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);

}
