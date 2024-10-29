package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.Merchants;
import com.watergun.entity.WithdrawalRecords;

import java.math.BigDecimal;

public interface WithdrawalRecordsService extends IService<WithdrawalRecords>  {
    //模拟银行异步处理 always true
    void processWithdrawAsync(WithdrawalRecords withdrawalRecord, Merchants merchants);

    //添加提现记录
    R<String> withdrawApplication(String token, BigDecimal amount, Long bankAccountId);

    //查看所有提现记录
    R<Page> getWithdrawApplications(int page, int pageSize, String token, String status);

}
