package com.hku.nook.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hku.nook.dao.AuthRoleMenuDao;
import com.hku.nook.domain.auth.AuthRoleMenu;

@Service
public class AuthRoleMenuService {

    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;

    public List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getRoleMenusByRoleIds(roleIdSet);
    }

}
