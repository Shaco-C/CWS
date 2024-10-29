package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.PendingAmountLog;

import java.math.BigDecimal;

public interface PendingAmountLogService extends IService<PendingAmountLog> {
    void modifyPendingAmountLog(Long merchantId,  BigDecimal amount,String description,String currency);
}
