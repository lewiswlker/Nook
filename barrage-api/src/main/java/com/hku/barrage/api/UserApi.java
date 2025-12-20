package com.hku.barrage.api;

import com.alibaba.fastjson.JSONObject;
import com.hku.barrage.api.support.UserSupport;
import com.hku.barrage.domain.JsonResponse;
import com.hku.barrage.domain.PageResult;
import com.hku.barrage.domain.User;
import com.hku.barrage.domain.UserInfo;
import com.hku.barrage.service.UserFollowingService;
import com.hku.barrage.service.UserService;
import com.hku.barrage.service.util.RSAUtil;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserFollowingService userFollowingService;

    @GetMapping("/users")
    public JsonResponse<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }


    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey() {
        return new JsonResponse<>(RSAUtil.getPublicKeyStr());
    }

    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return JsonResponse.success();
    }

    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }

    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no, @RequestParam Integer size, String nick) {
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        params.put("no", no);
        params.put("size", size);
        params.put("nick", nick);
        params.put("userId", userId);
        PageResult<UserInfo> result = userService.pageListUserInfos(params);
        if (result.getTotal() > 0) {
            List<UserInfo> checkedUserInfoList = userFollowingService.checkFollowingStatus(userId, result.getList());
            result.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(result);
    }


}
