package com.watergun.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayLogicUtil {
    //暂时全部返回成功
    public boolean processPayment(Long userId, BigDecimal totalAmount,String paymentMethod){
        return true;
    }
}
