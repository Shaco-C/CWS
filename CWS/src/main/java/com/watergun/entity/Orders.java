package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@TableName("orders")
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    private Long userId;
    private Long merchantId;
    private Long addressId;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;

    private String trackingNumber; //快递单号
    private String currency;
    private String status;
    private String returnStatus;
    private String paymentMethod;
    private String shippingInfo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
