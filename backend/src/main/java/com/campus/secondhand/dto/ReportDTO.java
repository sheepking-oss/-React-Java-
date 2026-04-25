package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReportDTO {

    @NotNull(message = "被举报用户ID不能为空")
    private Long reportedUserId;

    private Long productId;

    @NotBlank(message = "举报类型不能为空")
    private String type;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private List<String> imageUrls;
}
