package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.WalletBalanceLog;

import java.math.BigDecimal;


public interface WalletBalanceLogService extends IService<WalletBalanceLog> {
    void modifyWalletBalanceLog(Long merchantId, Long orderId, BigDecimal amount, String description, String currency);
}
