package com.watergun.dto;

import com.watergun.entity.OrderItems;
import com.watergun.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO extends Orders {
    private List<OrderItems> orderItemsList;

    public OrderDTO(Orders orders){
        super(orders.getOrderId(),
                orders.getUserId(),
                orders.getMerchantId(),
                orders.getTotalAmount(),
                orders.getTaxAmount(),
                orders.getShippingFee(),
                orders.getCurrency(),
                orders.getStatus(),
                orders.getReturnStatus(),
                orders.getPaymentMethod(),
                orders.getShippingInfo(),
                orders.getCreatedAt(),
                orders.getUpdatedAt());
    }
}
