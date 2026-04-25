package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MessageDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "接收者ID不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String imageUrl;
}
