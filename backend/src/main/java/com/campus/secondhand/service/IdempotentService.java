package com.campus.secondhand.service;

public interface IdempotentService {

    boolean checkAndSetRequestId(String key, long expireSeconds);

    boolean checkAndSetRequestId(String key);

    void removeRequestId(String key);

    boolean exists(String key);

    String generateKey(Long userId, String requestId, String action);
}
