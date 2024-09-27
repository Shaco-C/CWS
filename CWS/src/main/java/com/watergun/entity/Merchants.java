package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("merchants")
@AllArgsConstructor
@NoArgsConstructor
public class Merchants {
    @TableId(type = IdType.ASSIGN_ID)
    private Long merchantId;

    private String shopName;
    private String address;
    private String country;
    private String taxId;//税号
    private String paymentInfo;
    private String shopAvatarUrl;//店铺头像

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
