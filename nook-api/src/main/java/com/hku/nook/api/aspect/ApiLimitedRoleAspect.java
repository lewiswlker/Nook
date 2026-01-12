package com.hku.nook.api.aspect;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hku.nook.api.support.UserSupport;
import com.hku.nook.domain.annotation.ApiLimitedRole;
import com.hku.nook.domain.auth.UserRole;
import com.hku.nook.domain.exception.ConditionException;
import com.hku.nook.service.UserRoleService;

@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {
    
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.hku.nook.domain.annotation.ApiLimitedRole)")
    public void check() {
        
    }

    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRolesByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> userRoleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        userRoleCodeSet.retainAll(limitedRoleCodeSet);
        if (userRoleCodeSet.size() > 0) {
            throw new ConditionException("Access denied due to limited role.");
        }
    }

}
