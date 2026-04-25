package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ExchangeDTO;
import com.campus.secondhand.entity.Exchange;

import java.util.Map;

public interface ExchangeService extends IService<Exchange> {

    void createExchange(ExchangeDTO dto);

    Map<String, Object> getExchangeDetail(Long id);

    PageResult<Map<String, Object>> getMyExchanges(String status, Integer current, Integer size);

    PageResult<Map<String, Object>> getMyReceivedExchanges(String status, Integer current, Integer size);

    void acceptExchange(Long id);

    void rejectExchange(Long id, String reason);

    void cancelExchange(Long id);
}
