package com.hku.barrage.api.aspect;

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

import com.hku.barrage.api.support.UserSupport;
import com.hku.barrage.domain.UserMoment;
import com.hku.barrage.domain.annotation.ApiLimitedRole;
import com.hku.barrage.domain.auth.UserRole;
import com.hku.barrage.domain.constant.AuthRoleConstant;
import com.hku.barrage.domain.exception.ConditionException;
import com.hku.barrage.service.UserRoleService;

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {
    
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.hku.barrage.api.annotation.DataLimited)")
    public void check() {
        
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRolesByUserId(userId);
        Set<String> userRoleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                if(userRoleCodeSet.contains(AuthRoleConstant.ROLE_CODE_LV0) && !"0".equals(type)) {
                    throw new ConditionException("Parameter 'type' is limited for your role.");
                }
            }
        }
    }

}
