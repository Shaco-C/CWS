package com.watergun.enums;

public enum OrderStatus {
    PENDING_PAYMENT, // 待支付
    PENDING,         // 已支付待处理
    SHIPPED,         // 已发货
    IN_TRANSIT,      // 在途，等待收货
    DELIVERED,       // 已送达
    RECEIVED,        // 已收货
    CANCELLED        // 已取消
}
