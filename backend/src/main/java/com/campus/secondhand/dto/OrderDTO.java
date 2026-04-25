package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "收货地址不能为空")
    private String buyerAddress;

    @NotBlank(message = "联系电话不能为空")
    private String buyerPhone;

    @NotBlank(message = "收货人姓名不能为空")
    private String buyerName;
}
