package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.entity.Merchants;
import com.watergun.entity.PendingAmountLog;
import com.watergun.mapper.PendingAmountLogMapper;
import com.watergun.service.PendingAmountLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PendingAmountLogServiceImpl extends ServiceImpl<PendingAmountLogMapper,PendingAmountLog> implements PendingAmountLogService {
    //待确认金额变更日志
    @Override
    public void modifyPendingAmountLog(Long merchantId,  BigDecimal amount,String description,String currency) {
        log.info("====================addPendingAmountLog====================");
        log.info("addPendingAmountLog:商家{}增加待确认金额{}", merchantId, amount);
        PendingAmountLog pendingAmountLog = new PendingAmountLog();
        pendingAmountLog.setAmount(amount);
        pendingAmountLog.setMerchantId(merchantId);
        pendingAmountLog.setDescription(description);
        pendingAmountLog.setCurrency(currency);

        boolean result = this.save(pendingAmountLog);
        if (!result){
            throw new CustomException("添加待确认金额日志失败");
        }
        log.info("添加待确认金额日志成功");
    }
}
