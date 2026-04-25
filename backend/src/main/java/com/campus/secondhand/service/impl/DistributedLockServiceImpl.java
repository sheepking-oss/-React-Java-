package com.campus.secondhand.service.impl;

import com.campus.secondhand.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockServiceImpl.class);

    private static final String LOCK_KEY_PREFIX = "distributed:lock:";

    private static final long DEFAULT_WAIT_TIME = 0;

    private static final long DEFAULT_LEASE_TIME = 30;

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RedisLockRegistry redisLockRegistry;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = LOCK_KEY_PREFIX + key;

        if (redisTemplate != null) {
            try {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, System.currentTimeMillis(), leaseTime, unit);
                if (Boolean.TRUE.equals(success)) {
                    logger.info("获取分布式锁成功, key: {}", lockKey);
                    return true;
                } else {
                    logger.warn("获取分布式锁失败, key: {}", lockKey);
                    return false;
                }
            } catch (Exception e) {
                logger.error("获取分布式锁异常, key: {}", lockKey, e);
            }
        }

        logger.warn("Redis 未配置，跳过分布式锁获取，key: {}", lockKey);
        return true;
    }

    @Override
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT);
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;

        if (redisTemplate != null) {
            try {
                redisTemplate.delete(lockKey);
                logger.info("释放分布式锁成功, key: {}", lockKey);
            } catch (Exception e) {
                logger.error("释放分布式锁异常, key: {}", lockKey, e);
            }
        }
    }

    @Override
    public boolean isLocked(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;

        if (redisTemplate != null) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
            } catch (Exception e) {
                logger.error("检查分布式锁状态异常, key: {}", lockKey, e);
            }
        }
        return false;
    }

    @Override
    public void executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, Runnable task) {
        boolean locked = tryLock(key, waitTime, leaseTime, unit);
        if (!locked) {
            throw new RuntimeException("获取锁失败，请稍后重试");
        }

        try {
            task.run();
        } finally {
            unlock(key);
        }
    }

    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, LockCallback<T> callback) {
        boolean locked = tryLock(key, waitTime, leaseTime, unit);
        if (!locked) {
            throw new RuntimeException("获取锁失败，请稍后重试");
        }

        try {
            return callback.execute();
        } finally {
            unlock(key);
        }
    }

    public static String getProductLockKey(Long productId) {
        return "product:" + productId + ":trade";
    }

    public static String getOrderLockKey(Long userId, Long productId) {
        return "order:" + userId + ":" + productId;
    }

    public static String getExchangeLockKey(Long productId) {
        return "exchange:product:" + productId;
    }
}
