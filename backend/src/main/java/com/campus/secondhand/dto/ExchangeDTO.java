package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ExchangeDTO {

    @NotNull(message = "目标用户ID不能为空")
    private Long targetId;

    @NotNull(message = "对方商品ID不能为空")
    private Long targetProductId;

    private Long initiatorProductId;

    private String initiatorDescription;
}
