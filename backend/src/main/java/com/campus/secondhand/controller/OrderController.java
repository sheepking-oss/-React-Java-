package com.campus.secondhand.controller;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderDTO;
import com.campus.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Map<String, String>> create(@Validated @RequestBody OrderDTO dto) {
        String orderNo = orderService.createOrder(dto);
        Map<String, String> result = new HashMap<>();
        result.put("orderNo", orderNo);
        return Result.success(result);
    }

    @GetMapping("/detail/{orderNo}")
    public Result<Map<String, Object>> detail(@PathVariable String orderNo) {
        Map<String, Object> result = orderService.getOrderDetail(orderNo);
        return Result.success(result);
    }

    @GetMapping("/myOrders")
    public Result<PageResult<Map<String, Object>>> myOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = orderService.getMyOrders(status, current, size);
        return Result.success(result);
    }

    @GetMapping("/mySales")
    public Result<PageResult<Map<String, Object>>> mySales(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = orderService.getMySales(status, current, size);
        return Result.success(result);
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo, @RequestBody(required = false) Map<String, String> params) {
        String reason = params != null ? params.get("reason") : null;
        orderService.cancelOrder(orderNo, reason);
        return Result.success();
    }

    @PostMapping("/pay/{orderNo}")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.payOrder(orderNo);
        return Result.success();
    }

    @PostMapping("/ship/{orderNo}")
    public Result<Void> ship(@PathVariable String orderNo) {
        orderService.shipOrder(orderNo);
        return Result.success();
    }

    @PostMapping("/confirm/{orderNo}")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceive(orderNo);
        return Result.success();
    }
}
