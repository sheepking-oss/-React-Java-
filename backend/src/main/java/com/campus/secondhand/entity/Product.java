package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int STATUS_AVAILABLE = 1;
    public static final int STATUS_SOLD = 2;
    public static final int STATUS_OFF_SHELF = 0;
    public static final int STATUS_LOCKED = 3;
    public static final int STATUS_IN_EXCHANGE = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String category;

    private String coverImage;

    private Integer status;

    private Integer viewCount;

    private Integer favoriteCount;

    private String location;

    private String tags;

    private String condition;

    private Integer isNegotiable;

    private Integer auditStatus;

    private String auditReason;

    @Version
    private Integer version;

    private Long lockExpireTime;

    private Long lockHolderId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public boolean isAvailableForTrade() {
        return this.status != null && this.status == STATUS_AVAILABLE;
    }

    public boolean isLocked() {
        return this.status != null && (this.status == STATUS_LOCKED || this.status == STATUS_IN_EXCHANGE);
    }

    public boolean isSold() {
        return this.status != null && this.status == STATUS_SOLD;
    }

    public boolean isOffShelf() {
        return this.status != null && this.status == STATUS_OFF_SHELF;
    }
}
