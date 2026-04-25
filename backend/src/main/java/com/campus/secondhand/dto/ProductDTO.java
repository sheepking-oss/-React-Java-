package com.campus.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    private String description;

    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotBlank(message = "商品分类不能为空")
    private String category;

    private String coverImage;

    private List<String> images;

    private String location;

    private String tags;

    private String condition;

    private Integer isNegotiable;
}
