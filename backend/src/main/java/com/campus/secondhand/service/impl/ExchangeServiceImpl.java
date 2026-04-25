package com.campus.secondhand.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ExchangeDTO;
import com.campus.secondhand.entity.Exchange;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ExchangeMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.DistributedLockService;
import com.campus.secondhand.service.ExchangeService;
import com.campus.secondhand.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeServiceImpl extends ServiceImpl<ExchangeMapper, Exchange> implements ExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    private static final int LOCK_EXPIRE_SECONDS = 3600;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DistributedLockService distributedLockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExchange(ExchangeDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (userId.equals(dto.getTargetId())) {
            throw new RuntimeException("不能与自己换物");
        }

        Long targetProductId = dto.getTargetProductId();
        String lockKey = DistributedLockServiceImpl.getExchangeLockKey(targetProductId);

        distributedLockService.executeWithLock(lockKey, 2, 60, TimeUnit.SECONDS, () -> {
            Product targetProduct = productMapper.selectById(targetProductId);
            if (targetProduct == null) {
                throw new RuntimeException("目标商品不存在");
            }

            if (!targetProduct.getUserId().equals(dto.getTargetId())) {
                throw new RuntimeException("目标商品不属于目标用户");
            }

            validateProductForExchange(targetProduct, "目标商品");

            if (dto.getInitiatorProductId() != null) {
                Product initiatorProduct = productMapper.selectById(dto.getInitiatorProductId());
                if (initiatorProduct == null) {
                    throw new RuntimeException("发起商品不存在");
                }
                if (!initiatorProduct.getUserId().equals(userId)) {
                    throw new RuntimeException("发起商品不属于您");
                }
                validateProductForExchange(initiatorProduct, "发起商品");
            }

            Exchange exchange = new Exchange();
            exchange.setInitiatorId(userId);
            exchange.setTargetId(dto.getTargetId());
            exchange.setInitiatorProductId(dto.getInitiatorProductId());
            exchange.setTargetProductId(targetProductId);
            exchange.setInitiatorDescription(dto.getInitiatorDescription());
            exchange.setStatus("PENDING");

            this.save(exchange);

            lockProductForExchange(targetProduct, exchange.getId(), userId);

            logger.info("换物申请创建成功, exchangeId: {}, targetProductId: {}, initiatorId: {}", 
                exchange.getId(), targetProductId, userId);

            return null;
        });
    }

    private void validateProductForExchange(Product product, String productName) {
        if (product.getStatus() == null) {
            throw new RuntimeException(productName + "状态异常");
        }

        if (product.getStatus() == Product.STATUS_LOCKED) {
            throw new RuntimeException(productName + "正在换物申请处理中，请稍后再试");
        }

        if (product.getStatus() == Product.STATUS_IN_EXCHANGE) {
            throw new RuntimeException(productName + "已进入换物流程");
        }

        if (product.getStatus() == Product.STATUS_SOLD) {
            throw new RuntimeException(productName + "已售出");
        }

        if (product.getStatus() == Product.STATUS_OFF_SHELF) {
            throw new RuntimeException(productName + "已下架");
        }

        if (product.getStatus() != Product.STATUS_AVAILABLE) {
            throw new RuntimeException(productName + "状态异常");
        }

        if (product.getAuditStatus() != 1) {
            throw new RuntimeException(productName + "未通过审核");
        }
    }

    private void lockProductForExchange(Product product, Long exchangeId, Long initiatorId) {
        product.setStatus(Product.STATUS_LOCKED);
        product.setLockExpireTime(System.currentTimeMillis() / 1000 + LOCK_EXPIRE_SECONDS);
        product.setLockHolderId(exchangeId);
        productMapper.updateById(product);
        
        logger.info("商品已锁定，等待换物确认, productId: {}, exchangeId: {}, initiatorId: {}", 
            product.getId(), exchangeId, initiatorId);
    }

    @Override
    public Map<String, Object> getExchangeDetail(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Exchange exchange = this.getById(id);
        if (exchange == null) {
            throw new RuntimeException("换物申请不存在");
        }

        if (!exchange.getInitiatorId().equals(userId) && !exchange.getTargetId().equals(userId)) {
            throw new RuntimeException("无权查看此换物申请");
        }

        return buildExchangeDetail(exchange);
    }

    @Override
    public PageResult<Map<String, Object>> getMyExchanges(String status, Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Exchange> page = new Page<>(current, size);
        LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<Exchange>()
                .eq(Exchange::getInitiatorId, userId)
                .orderByDesc(Exchange::getCreateTime);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Exchange::getStatus, status);
        }

        Page<Exchange> exchangePage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Exchange exchange : exchangePage.getRecords()) {
            records.add(buildExchangeDetail(exchange));
        }

        return new PageResult<>(records, exchangePage.getTotal(), exchangePage.getSize(), exchangePage.getCurrent());
    }

    @Override
    public PageResult<Map<String, Object>> getMyReceivedExchanges(String status, Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Exchange> page = new Page<>(current, size);
        LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<Exchange>()
                .eq(Exchange::getTargetId, userId)
                .orderByDesc(Exchange::getCreateTime);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Exchange::getStatus, status);
        }

        Page<Exchange> exchangePage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Exchange exchange : exchangePage.getRecords()) {
            records.add(buildExchangeDetail(exchange));
        }

        return new PageResult<>(records, exchangePage.getTotal(), exchangePage.getSize(), exchangePage.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptExchange(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Exchange exchange = this.getById(id);
        if (exchange == null) {
            throw new RuntimeException("换物申请不存在");
        }

        if (!exchange.getTargetId().equals(userId)) {
            throw new RuntimeException("无权操作此换物申请");
        }

        if (!"PENDING".equals(exchange.getStatus())) {
            throw new RuntimeException("换物申请状态不允许操作");
        }

        String lockKey = DistributedLockServiceImpl.getExchangeLockKey(exchange.getTargetProductId());
        distributedLockService.executeWithLock(lockKey, 2, 60, TimeUnit.SECONDS, () -> {
            Product targetProduct = productMapper.selectById(exchange.getTargetProductId());
            if (targetProduct != null && targetProduct.getStatus() == Product.STATUS_LOCKED) {
                targetProduct.setStatus(Product.STATUS_IN_EXCHANGE);
                productMapper.updateById(targetProduct);
                logger.info("换物申请已接受，商品进入换物流程, productId: {}, exchangeId: {}", 
                    exchange.getTargetProductId(), id);
            }

            exchange.setStatus("ACCEPTED");
            exchange.setHandleTime(LocalDateTime.now());
            this.updateById(exchange);

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectExchange(Long id, String reason) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Exchange exchange = this.getById(id);
        if (exchange == null) {
            throw new RuntimeException("换物申请不存在");
        }

        if (!exchange.getTargetId().equals(userId)) {
            throw new RuntimeException("无权操作此换物申请");
        }

        if (!"PENDING".equals(exchange.getStatus())) {
            throw new RuntimeException("换物申请状态不允许操作");
        }

        String lockKey = DistributedLockServiceImpl.getExchangeLockKey(exchange.getTargetProductId());
        distributedLockService.executeWithLock(lockKey, 2, 60, TimeUnit.SECONDS, () -> {
            unlockProduct(exchange.getTargetProductId(), exchange.getId());

            exchange.setStatus("REJECTED");
            exchange.setHandleTime(LocalDateTime.now());
            exchange.setRejectReason(reason);
            this.updateById(exchange);

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExchange(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Exchange exchange = this.getById(id);
        if (exchange == null) {
            throw new RuntimeException("换物申请不存在");
        }

        if (!exchange.getInitiatorId().equals(userId)) {
            throw new RuntimeException("无权取消此换物申请");
        }

        if (!"PENDING".equals(exchange.getStatus())) {
            throw new RuntimeException("换物申请状态不允许取消");
        }

        String lockKey = DistributedLockServiceImpl.getExchangeLockKey(exchange.getTargetProductId());
        distributedLockService.executeWithLock(lockKey, 2, 60, TimeUnit.SECONDS, () -> {
            unlockProduct(exchange.getTargetProductId(), exchange.getId());

            exchange.setStatus("CANCELLED");
            exchange.setHandleTime(LocalDateTime.now());
            this.updateById(exchange);

            return null;
        });
    }

    private void unlockProduct(Long productId, Long exchangeId) {
        Product product = productMapper.selectById(productId);
        if (product != null && product.getStatus() == Product.STATUS_LOCKED
                && exchangeId.equals(product.getLockHolderId())) {
            product.setStatus(Product.STATUS_AVAILABLE);
            product.setLockExpireTime(null);
            product.setLockHolderId(null);
            productMapper.updateById(product);
            logger.info("商品已解锁, productId: {}, exchangeId: {}", productId, exchangeId);
        }
    }

    private Map<String, Object> buildExchangeDetail(Exchange exchange) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", exchange.getId());
        result.put("initiatorId", exchange.getInitiatorId());
        result.put("targetId", exchange.getTargetId());
        result.put("initiatorProductId", exchange.getInitiatorProductId());
        result.put("targetProductId", exchange.getTargetProductId());
        result.put("initiatorDescription", exchange.getInitiatorDescription());
        result.put("status", exchange.getStatus());
        result.put("handleTime", exchange.getHandleTime());
        result.put("rejectReason", exchange.getRejectReason());
        result.put("createTime", exchange.getCreateTime());

        if (exchange.getInitiatorProductId() != null) {
            Product product = productMapper.selectById(exchange.getInitiatorProductId());
            if (product != null) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("title", product.getTitle());
                productMap.put("coverImage", product.getCoverImage());
                productMap.put("price", product.getPrice());
                result.put("initiatorProduct", productMap);
            }
        }

        Product targetProduct = productMapper.selectById(exchange.getTargetProductId());
        if (targetProduct != null) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", targetProduct.getId());
            productMap.put("title", targetProduct.getTitle());
            productMap.put("coverImage", targetProduct.getCoverImage());
            productMap.put("price", targetProduct.getPrice());
            result.put("targetProduct", productMap);
        }

        User initiator = userMapper.selectById(exchange.getInitiatorId());
        if (initiator != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", initiator.getId());
            userMap.put("nickname", initiator.getNickname());
            userMap.put("avatar", initiator.getAvatar());
            userMap.put("creditScore", initiator.getCreditScore());
            result.put("initiator", userMap);
        }

        User target = userMapper.selectById(exchange.getTargetId());
        if (target != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", target.getId());
            userMap.put("nickname", target.getNickname());
            userMap.put("avatar", target.getAvatar());
            userMap.put("creditScore", target.getCreditScore());
            result.put("target", userMap);
        }

        return result;
    }
}
