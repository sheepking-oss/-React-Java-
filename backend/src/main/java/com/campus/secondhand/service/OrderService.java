package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.OrderDTO;
import com.campus.secondhand.entity.Order;

import java.util.Map;

public interface OrderService extends IService<Order> {

    String createOrder(OrderDTO dto);

    Map<String, Object> getOrderDetail(String orderNo);

    PageResult<Map<String, Object>> getMyOrders(String status, Integer current, Integer size);

    PageResult<Map<String, Object>> getMySales(String status, Integer current, Integer size);

    void cancelOrder(String orderNo, String reason);

    void payOrder(String orderNo);

    void shipOrder(String orderNo);

    void confirmReceive(String orderNo);

    void completeOrder(String orderNo);
}
