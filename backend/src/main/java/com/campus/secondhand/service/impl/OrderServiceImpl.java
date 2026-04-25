package com.campus.secondhand.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.OrderDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.DistributedLockService;
import com.campus.secondhand.service.OrderService;
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
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DistributedLockService distributedLockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Long productId = dto.getProductId();
        String lockKey = DistributedLockServiceImpl.getProductLockKey(productId);

        return distributedLockService.executeWithLock(lockKey, 2, 60, TimeUnit.SECONDS, () -> {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            validateProductStatus(product, userId);

            if (product.getStatus() != Product.STATUS_AVAILABLE) {
                String errorMsg = getStatusErrorMessage(product.getStatus());
                throw new RuntimeException(errorMsg);
            }

            Order order = new Order();
            order.setOrderNo("SH" + IdUtil.getSnowflakeNextIdStr());
            order.setProductId(dto.getProductId());
            order.setSellerId(product.getUserId());
            order.setBuyerId(userId);
            order.setPrice(product.getPrice());
            order.setStatus("PENDING");
            order.setBuyerAddress(dto.getBuyerAddress());
            order.setBuyerPhone(dto.getBuyerPhone());
            order.setBuyerName(dto.getBuyerName());

            this.save(order);

            logger.info("订单创建成功, orderNo: {}, productId: {}, userId: {}", 
                order.getOrderNo(), productId, userId);

            return order.getOrderNo();
        });
    }

    private void validateProductStatus(Product product, Long userId) {
        if (product.getStatus() == null) {
            throw new RuntimeException("商品状态异常");
        }

        if (product.getAuditStatus() != 1) {
            throw new RuntimeException("商品未通过审核");
        }

        if (product.getUserId().equals(userId)) {
            throw new RuntimeException("不能购买自己发布的商品");
        }

        if (product.getStatus() == Product.STATUS_LOCKED) {
            throw new RuntimeException("商品正在换物申请处理中，请稍后再试");
        }

        if (product.getStatus() == Product.STATUS_IN_EXCHANGE) {
            throw new RuntimeException("商品已进入换物流程，无法购买");
        }

        if (product.getStatus() == Product.STATUS_SOLD) {
            throw new RuntimeException("商品已售出");
        }

        if (product.getStatus() == Product.STATUS_OFF_SHELF) {
            throw new RuntimeException("商品已下架");
        }
    }

    private String getStatusErrorMessage(Integer status) {
        if (status == null) {
            return "商品状态异常";
        }
        switch (status) {
            case Product.STATUS_LOCKED:
                return "商品正在换物申请处理中，请稍后再试";
            case Product.STATUS_IN_EXCHANGE:
                return "商品已进入换物流程，无法购买";
            case Product.STATUS_SOLD:
                return "商品已售出";
            case Product.STATUS_OFF_SHELF:
                return "商品已下架";
            default:
                return "商品状态异常";
        }
    }

    @Override
    public Map<String, Object> getOrderDetail(String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        Map<String, Object> result = buildOrderDetail(order);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> getMyOrders(String status, Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId)
                .orderByDesc(Order::getCreateTime);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Order::getStatus, status);
        }

        Page<Order> orderPage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            records.add(buildOrderDetail(order));
        }

        return new PageResult<>(records, orderPage.getTotal(), orderPage.getSize(), orderPage.getCurrent());
    }

    @Override
    public PageResult<Map<String, Object>> getMySales(String status, Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, userId)
                .orderByDesc(Order::getCreateTime);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Order::getStatus, status);
        }

        Page<Order> orderPage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            records.add(buildOrderDetail(order));
        }

        return new PageResult<>(records, orderPage.getTotal(), orderPage.getSize(), orderPage.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, String reason) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消此订单");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许取消");
        }

        order.setStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);

        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权支付此订单");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");
        }

        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());

        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许发货");
        }

        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());

        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (!"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许确认收货");
        }

        order.setStatus("COMPLETED");
        order.setReceiveTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());

        this.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(Product.STATUS_SOLD);
            productMapper.updateById(product);
            logger.info("订单已完成，商品标记为已售出, productId: {}, orderNo: {}", 
                order.getProductId(), orderNo);
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            buyer.setTradeCount(buyer.getTradeCount() + 1);
            buyer.setSuccessCount(buyer.getSuccessCount() + 1);
            userMapper.updateById(buyer);
        }

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            seller.setTradeCount(seller.getTradeCount() + 1);
            seller.setSuccessCount(seller.getSuccessCount() + 1);
            userMapper.updateById(seller);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (!"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许确认完成");
        }

        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());

        this.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(Product.STATUS_SOLD);
            productMapper.updateById(product);
            logger.info("订单已完成，商品标记为已售出, productId: {}, orderNo: {}", 
                order.getProductId(), orderNo);
        }
    }

    private Map<String, Object> buildOrderDetail(Order order) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("productId", order.getProductId());
        result.put("sellerId", order.getSellerId());
        result.put("buyerId", order.getBuyerId());
        result.put("price", order.getPrice());
        result.put("status", order.getStatus());
        result.put("buyerAddress", order.getBuyerAddress());
        result.put("buyerPhone", order.getBuyerPhone());
        result.put("buyerName", order.getBuyerName());
        result.put("payTime", order.getPayTime());
        result.put("shipTime", order.getShipTime());
        result.put("receiveTime", order.getReceiveTime());
        result.put("completeTime", order.getCompleteTime());
        result.put("cancelTime", order.getCancelTime());
        result.put("cancelReason", order.getCancelReason());
        result.put("createTime", order.getCreateTime());

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("coverImage", product.getCoverImage());
            productMap.put("price", product.getPrice());
            productMap.put("description", product.getDescription());
            result.put("product", productMap);
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            Map<String, Object> buyerMap = new HashMap<>();
            buyerMap.put("id", buyer.getId());
            buyerMap.put("nickname", buyer.getNickname());
            buyerMap.put("avatar", buyer.getAvatar());
            buyerMap.put("phone", buyer.getPhone());
            result.put("buyer", buyerMap);
        }

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            Map<String, Object> sellerMap = new HashMap<>();
            sellerMap.put("id", seller.getId());
            sellerMap.put("nickname", seller.getNickname());
            sellerMap.put("avatar", seller.getAvatar());
            sellerMap.put("phone", seller.getPhone());
            result.put("seller", sellerMap);
        }

        return result;
    }
}
