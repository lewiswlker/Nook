package com.hku.barrage.service;

import com.alibaba.fastjson.JSONObject;
import com.hku.barrage.dao.UserDao;
import com.hku.barrage.domain.PageResult;
import com.hku.barrage.domain.User;
import com.hku.barrage.domain.UserInfo;
import com.hku.barrage.domain.constant.UserConstant;
import com.hku.barrage.domain.exception.ConditionException;
import com.hku.barrage.service.util.MD5Util;
import com.hku.barrage.service.util.RSAUtil;
import com.hku.barrage.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("Phone number is invalid!");
        }
        User dbUser = getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("Phone number is exist!");
        }
        Date now = new Date();
        String salt = now.getTime() + "";
        String password = user.getPassword();
        String rowPassword;
        try {
            rowPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("Password is invalid!");
        }
        String md5Password = MD5Util.sign(rowPassword, salt, "UTF-8");
        user.setPassword(md5Password);
        user.setSalt(salt);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userDao.addUser(user);
        // add userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTHDAY);
        userInfo.setGender(UserConstant.GENDER_UNKNOWN);
        userInfo.setCreateTime(now);
        userInfo.setUpdateTime(now);
        userDao.addUserInfo(userInfo);
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new ConditionException("Login type is invalid!");
        }
        String phoneOrEmail = phone + email;
        User dbUser = userDao.getUserByPhoneOrEmail(phoneOrEmail);
        if (dbUser == null) {
            throw new ConditionException("User is not exist!");
        }
        String password = dbUser.getPassword();
        String rowPassword;
        try {
            rowPassword = RSAUtil.decrypt(user.getPassword());
        } catch (Exception e) {
            throw new ConditionException("Password is invalid!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rowPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("Password is wrong!");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new ConditionException("User is not exist!");
        }
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        if (userInfo == null) {
            throw new ConditionException("UserInfo is not exist!");
        }
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUsers(User user) throws Exception {
        Long userId = user.getId();
        User dbUser = userDao.getUserById(userId);
        if (dbUser == null) {
            throw new ConditionException("User is not exist!");
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            String rowPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rowPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long id) {
        return userDao.getUserById(id);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        Integer start = (no - 1) * size;
        params.put("start", start);
        params.put("limit", size);
        Integer total = userDao.pageCountUserInfos(params);
        List<UserInfo> list = new ArrayList<>();
        if (total > 0) {
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total, list);
    }

}
