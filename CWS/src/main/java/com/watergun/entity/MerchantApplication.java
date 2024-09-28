package com.watergun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("merchant_applications")
@AllArgsConstructor
@NoArgsConstructor
public class MerchantApplication {

    @TableId(type = IdType.ASSIGN_ID)
    private Long applicationId; // 申请记录的唯一ID

    private Long userId; // 申请者的用户ID，关联到 Users 表

    private String shopName; // 商店名称

    private String address; // 商家地址

    private String country; // 所在国家

    private String taxId; // 税号

    private String paymentInfo; // 支付和结算信息

    private String shopAvatarUrl; // 店铺头像

    private String status; // 申请状态：pending, approved, rejected

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 申请提交时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 申请更新时间
}
