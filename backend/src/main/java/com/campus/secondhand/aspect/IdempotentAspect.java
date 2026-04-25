package com.campus.secondhand.aspect;

import com.campus.secondhand.annotation.Idempotent;
import com.campus.secondhand.service.IdempotentService;
import com.campus.secondhand.utils.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class IdempotentAspect {

    private static final Logger logger = LoggerFactory.getLogger(IdempotentAspect.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Autowired
    private IdempotentService idempotentService;

    @Around("@annotation(com.campus.secondhand.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        Long userId = UserContext.getUserId();
        String requestId = getRequestId();
        String action = idempotent.action();
        if (action == null || action.isEmpty()) {
            action = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        if (requestId == null) {
            logger.warn("请求未携带 requestId，跳过幂等性校验");
            return joinPoint.proceed();
        }

        String key = idempotentService.generateKey(userId, requestId, action);

        boolean success = idempotentService.checkAndSetRequestId(key, idempotent.expireSeconds());

        if (!success) {
            logger.warn("重复请求被拦截，userId: {}, requestId: {}, action: {}", userId, requestId, action);
            throw new RuntimeException("重复请求，请稍后再试");
        }

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            idempotentService.removeRequestId(key);
            throw throwable;
        }
    }

    private String getRequestId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String requestId = request.getHeader(REQUEST_ID_HEADER);
                if (requestId != null && !requestId.isEmpty()) {
                    return requestId;
                }
            }
        } catch (Exception e) {
            logger.error("获取 requestId 异常: {}", e.getMessage(), e);
        }
        return null;
    }
}
