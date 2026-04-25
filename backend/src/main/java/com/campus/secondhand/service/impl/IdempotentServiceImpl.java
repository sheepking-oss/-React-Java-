package com.campus.secondhand.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.campus.secondhand.service.IdempotentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotentServiceImpl implements IdempotentService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotentServiceImpl.class);

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:request:";

    private static final long DEFAULT_EXPIRE_SECONDS = 60;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean checkAndSetRequestId(String key, long expireSeconds) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + key;

        try {
            if (redisTemplate != null) {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(redisKey, System.currentTimeMillis(), expireSeconds, TimeUnit.SECONDS);
                if (success != null && success) {
                    logger.info("幂等性校验通过，key: {}", key);
                    return true;
                } else {
                    logger.warn("重复请求被拦截，key: {}", key);
                    return false;
                }
            } else {
                logger.warn("Redis 未配置，跳过幂等性校验");
                return true;
            }
        } catch (Exception e) {
            logger.error("幂等性校验异常: {}", e.getMessage(), e);
            return true;
        }
    }

    @Override
    public boolean checkAndSetRequestId(String key) {
        return checkAndSetRequestId(key, DEFAULT_EXPIRE_SECONDS);
    }

    @Override
    public void removeRequestId(String key) {
        try {
            if (redisTemplate != null) {
                String redisKey = IDEMPOTENT_KEY_PREFIX + key;
                redisTemplate.delete(redisKey);
                logger.info("移除幂等性标识，key: {}", key);
            }
        } catch (Exception e) {
            logger.error("移除幂等性标识异常: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            if (redisTemplate != null) {
                String redisKey = IDEMPOTENT_KEY_PREFIX + key;
                return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
            }
        } catch (Exception e) {
            logger.error("检查幂等性标识存在性异常: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String generateKey(Long userId, String requestId, String action) {
        StringBuilder sb = new StringBuilder();
        if (userId != null) {
            sb.append(userId).append(":");
        }
        if (requestId != null) {
            sb.append(requestId).append(":");
        }
        if (action != null) {
            sb.append(action);
        }
        return DigestUtil.md5Hex(sb.toString());
    }
}
