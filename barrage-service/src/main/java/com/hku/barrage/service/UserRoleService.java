package com.hku.barrage.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hku.barrage.dao.UserRoleDao;
import com.hku.barrage.domain.auth.UserRole;

@Service
public class UserRoleService {
    
    @Autowired
    private UserRoleDao userRoleDao;

    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleDao.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRole.setCreateTime(new Date());
        userRoleDao.addUserRole(userRole);
    }

}
