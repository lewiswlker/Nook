package com.hku.nook.api.support;

import com.hku.nook.domain.exception.ConditionException;
import com.hku.nook.service.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserSupport {
    public Long getCurrentUserId() {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new ConditionException("Request attributes are null!");
        }
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId = TokenUtil.verifyToken(token);
        if (userId == null || userId < 0) {
            throw new ConditionException("Illegal token!");
        }
        return userId;
    }
}
