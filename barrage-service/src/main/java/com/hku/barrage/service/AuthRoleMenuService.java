package com.hku.barrage.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hku.barrage.dao.AuthRoleMenuDao;
import com.hku.barrage.domain.auth.AuthRoleMenu;

@Service
public class AuthRoleMenuService {

    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;

    public List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getRoleMenusByRoleIds(roleIdSet);
    }

}
