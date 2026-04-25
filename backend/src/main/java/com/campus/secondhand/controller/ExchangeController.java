package com.campus.secondhand.controller;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ExchangeDTO;
import com.campus.secondhand.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody ExchangeDTO dto) {
        exchangeService.createExchange(dto);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> result = exchangeService.getExchangeDetail(id);
        return Result.success(result);
    }

    @GetMapping("/myExchanges")
    public Result<PageResult<Map<String, Object>>> myExchanges(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = exchangeService.getMyExchanges(status, current, size);
        return Result.success(result);
    }

    @GetMapping("/myReceived")
    public Result<PageResult<Map<String, Object>>> myReceived(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = exchangeService.getMyReceivedExchanges(status, current, size);
        return Result.success(result);
    }

    @PostMapping("/accept/{id}")
    public Result<Void> accept(@PathVariable Long id) {
        exchangeService.acceptExchange(id);
        return Result.success();
    }

    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        String reason = params != null ? params.get("reason") : null;
        exchangeService.rejectExchange(id, reason);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        exchangeService.cancelExchange(id);
        return Result.success();
    }
}
