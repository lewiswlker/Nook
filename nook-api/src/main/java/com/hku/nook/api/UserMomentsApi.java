package com.hku.nook.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hku.nook.api.support.UserSupport;
import com.hku.nook.domain.JsonResponse;
import com.hku.nook.domain.UserMoment;
import com.hku.nook.domain.annotation.ApiLimitedRole;
import com.hku.nook.domain.annotation.DataLimited;
import com.hku.nook.domain.constant.AuthRoleConstant;
import com.hku.nook.service.UserMomentsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class UserMomentsApi {
    
    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;

    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_CODE_LV0})
    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }
    
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments() {
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> result = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(result);
    }
    


}
