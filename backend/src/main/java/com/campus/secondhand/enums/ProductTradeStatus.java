package com.campus.secondhand.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTradeStatus {

    AVAILABLE(1, "可交易", "商品可被购买或换物"),
    LOCKED(3, "锁定中", "商品收到换物申请，正在处理中，禁止新的购买订单"),
    IN_EXCHANGE(4, "换物中", "换物申请已被同意"),
    SOLD(2, "已售出", "商品已完成交易"),
    OFF_SHELF(0, "已下架", "卖家主动下架");

    private final Integer code;
    private final String name;
    private final String description;

    public static ProductTradeStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductTradeStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static boolean canTrade(Integer code) {
        return AVAILABLE.getCode().equals(code);
    }

    public static boolean canCreateOrder(Integer code) {
        return AVAILABLE.getCode().equals(code);
    }

    public static boolean canCreateExchange(Integer code) {
        return AVAILABLE.getCode().equals(code);
    }

    public static boolean isLocked(Integer code) {
        return LOCKED.getCode().equals(code) || IN_EXCHANGE.getCode().equals(code);
    }

    public static boolean isSold(Integer code) {
        return SOLD.getCode().equals(code);
    }
}
