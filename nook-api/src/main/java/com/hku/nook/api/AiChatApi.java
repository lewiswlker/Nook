package com.hku.nook.api;

import com.hku.nook.api.support.UserSupport;
import com.hku.nook.domain.AiChatRequest;
import com.hku.nook.domain.AiChatResponse;
import com.hku.nook.domain.JsonResponse;
import com.hku.nook.domain.auth.UserRole;
import com.hku.nook.domain.constant.AuthRoleConstant;
import com.hku.nook.service.UserRoleService;
import com.hku.nook.service.AiAgentService;
import com.hku.nook.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai")
public class AiChatApi {

    private static final ExecutorService SSE_EXECUTOR = Executors.newCachedThreadPool();
    private static final String LOGIN_REQUIRED_MESSAGE = "Please login first";
    private static final String LEVEL_REQUIRED_MESSAGE = "Only LV1 and above users can use AI feature.";

    @Autowired
    private AiAgentService aiAgentService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping("/chat")
    public JsonResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        AccessResult accessResult = validateUserAccess();
        if (accessResult.error != null) {
            return new JsonResponse<>("401", accessResult.error);
        }
        try {
            AiChatResponse response = aiAgentService.chat(request, accessResult.userId);
            return new JsonResponse<>(response);
        } catch (Exception e) {
            return new JsonResponse<>("500", e.getMessage());
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        AccessResult accessResult = validateUserAccess();
        if (accessResult.error != null) {
            SSE_EXECUTOR.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data(accessResult.error));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });
            return emitter;
        }
        SSE_EXECUTOR.execute(() -> aiAgentService.streamChat(request, emitter, accessResult.userId));
        return emitter;
    }

    private AccessResult validateUserAccess() {
        AccessResult result = new AccessResult();
        Long userId;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (ConditionException e) {
            result.error = LOGIN_REQUIRED_MESSAGE;
            return result;
        }
        if (!isLv1OrAbove(userId)) {
            result.error = LEVEL_REQUIRED_MESSAGE;
            return result;
        }
        result.userId = userId;
        return result;
    }

    private boolean isLv1OrAbove(Long userId) {
        if (userId == null) {
            return false;
        }
        java.util.List<UserRole> roles = userRoleService.getUserRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (UserRole role : roles) {
            if (role == null || role.getRoleCode() == null) {
                continue;
            }
            String code = role.getRoleCode();
            if (AuthRoleConstant.ROLE_CODE_LV1.equals(code)
                    || AuthRoleConstant.ROLE_CODE_LV2.equals(code)
                    || AuthRoleConstant.ROLE_CODE_LV3.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private static class AccessResult {
        private Long userId;
        private String error;
    }
}
