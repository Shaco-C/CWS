package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.watergun.enums.OrderStatus;
import com.watergun.enums.OrdersReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Enumerated(EnumType.STRING)
    private OrdersReturnStatus returnStatus;

    private String paymentMethod;
    private String shippingInfo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
