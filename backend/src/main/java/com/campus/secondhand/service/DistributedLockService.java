package com.campus.secondhand.service;

import java.util.concurrent.TimeUnit;

public interface DistributedLockService {

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    boolean tryLock(String key);

    void unlock(String key);

    boolean isLocked(String key);

    void executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, Runnable task);

    <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, LockCallback<T> callback);

    @FunctionalInterface
    interface LockCallback<T> {
        T execute();
    }
}
