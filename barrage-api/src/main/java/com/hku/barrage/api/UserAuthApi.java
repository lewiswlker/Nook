package com.hku.barrage.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hku.barrage.api.support.UserSupport;
import com.hku.barrage.domain.JsonResponse;
import com.hku.barrage.domain.auth.UserAuthorities;
import com.hku.barrage.service.UserAuthService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class UserAuthApi {
    
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities() {
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }
    

}
