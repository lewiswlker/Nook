package com.hku.nook.api;

import com.hku.nook.api.support.UserSupport;
import com.hku.nook.domain.FollowingGroup;
import com.hku.nook.domain.JsonResponse;
import com.hku.nook.domain.UserFollowing;
import com.hku.nook.service.UserFollowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class UserFollowingApi {

    @Autowired
    private UserFollowingService userFollowingService;

    @Autowired
    private UserSupport userSupport;

    @PostMapping("/user-followings")
    public JsonResponse<String> addUserFollowings(@RequestBody UserFollowing userFollowing) {
        Long userId = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowings(userFollowing);
        return JsonResponse.success();
    }

   @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings() {
       Long userId = userSupport.getCurrentUserId();
       List<FollowingGroup> result = userFollowingService.getUserFollowings(userId);
       return new JsonResponse<>(result);
   }

   @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>> getUserFans() {
       Long userId = userSupport.getCurrentUserId();
       List<UserFollowing> result = userFollowingService.getUserFans(userId);
       return new JsonResponse<>(result);
   }

   @PostMapping("/user-following-groups")
    public JsonResponse<Long> addUserFollowingGroup(@RequestBody FollowingGroup followingGroup) {
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId = userFollowingService.addUserFollowingGroups(followingGroup);
        return new JsonResponse<>(groupId);
   }

   @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroups() {
       Long userId = userSupport.getCurrentUserId();
       List<FollowingGroup> result = userFollowingService.addUserFollowingGroups(userId);
       return new JsonResponse<>(result);
   }
}
