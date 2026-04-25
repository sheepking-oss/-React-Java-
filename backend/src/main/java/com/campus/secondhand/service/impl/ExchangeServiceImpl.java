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
import com.campus.secondhand.service.ExchangeService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeServiceImpl extends ServiceImpl<ExchangeMapper, Exchange> implements ExchangeService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

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

        Product targetProduct = productMapper.selectById(dto.getTargetProductId());
        if (targetProduct == null) {
            throw new RuntimeException("目标商品不存在");
        }

        if (!targetProduct.getUserId().equals(dto.getTargetId())) {
            throw new RuntimeException("目标商品不属于目标用户");
        }

        if (targetProduct.getStatus() != 1) {
            throw new RuntimeException("目标商品已下架或已售出");
        }

        if (dto.getInitiatorProductId() != null) {
            Product initiatorProduct = productMapper.selectById(dto.getInitiatorProductId());
            if (initiatorProduct == null) {
                throw new RuntimeException("发起商品不存在");
            }
            if (!initiatorProduct.getUserId().equals(userId)) {
                throw new RuntimeException("发起商品不属于您");
            }
            if (initiatorProduct.getStatus() != 1) {
                throw new RuntimeException("发起商品已下架或已售出");
            }
        }

        Exchange exchange = new Exchange();
        exchange.setInitiatorId(userId);
        exchange.setTargetId(dto.getTargetId());
        exchange.setInitiatorProductId(dto.getInitiatorProductId());
        exchange.setTargetProductId(dto.getTargetProductId());
        exchange.setInitiatorDescription(dto.getInitiatorDescription());
        exchange.setStatus("PENDING");

        this.save(exchange);
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

        exchange.setStatus("ACCEPTED");
        exchange.setHandleTime(LocalDateTime.now());

        this.updateById(exchange);
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

        exchange.setStatus("REJECTED");
        exchange.setHandleTime(LocalDateTime.now());
        exchange.setRejectReason(reason);

        this.updateById(exchange);
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

        exchange.setStatus("CANCELLED");
        exchange.setHandleTime(LocalDateTime.now());

        this.updateById(exchange);
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
