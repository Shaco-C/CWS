package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.entity.Merchants;
import com.watergun.entity.WalletBalanceLog;
import com.watergun.mapper.WalletBalanceLogMapper;
import com.watergun.service.MerchantService;
import com.watergun.service.WalletBalanceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class WalletBalanceLogServiceImpl extends ServiceImpl<WalletBalanceLogMapper, WalletBalanceLog> implements WalletBalanceLogService {


    private final MerchantService merchantService;

    public WalletBalanceLogServiceImpl(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    //钱包余额变更日志
    @Override
    public void modifyWalletBalanceLog(Long merchantId, Long orderId, BigDecimal amount, String description, String currency) {
        log.info("====================addWalletBalanceLog====================");
        log.info("addWalletBalanceLog:商家{}钱包余额变动{}", merchantId, amount);
        WalletBalanceLog walletBalanceLog = new WalletBalanceLog();

        Merchants merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            throw new CustomException("商家不存在");
        }
        walletBalanceLog.setMerchantId(merchantId);
        if (orderId!=null){
            walletBalanceLog.setOrderId(orderId);
        }
        walletBalanceLog.setAmountChange(amount);
        walletBalanceLog.setNewBalance(merchant.getWalletBalance());
        walletBalanceLog.setDescription(description);
        walletBalanceLog.setCurrency(currency);
        boolean result = this.save(walletBalanceLog);
        if (!result){
            throw new CustomException("添加钱包余额日志失败");
        }
        log.info("添加钱包余额日志成功");
    }
}
