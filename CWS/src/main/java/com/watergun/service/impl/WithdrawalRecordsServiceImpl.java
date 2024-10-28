package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.WithdrawalRecords;
import com.watergun.enums.WithdrawalRecordsStatus;
import com.watergun.mapper.WithdrawalRecordsMapper;
import com.watergun.service.WithdrawalRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class WithdrawalRecordsServiceImpl extends ServiceImpl<WithdrawalRecordsMapper, WithdrawalRecords> implements WithdrawalRecordsService {

    @Async // 异步执行
    public void processWithdrawAsync(WithdrawalRecords withdrawalRecord) {
        log.info("==================processWithdrawAsync===============================");
        log.info("开始处理异步提现申请: 提现记录ID: {}, 金额: {}", withdrawalRecord.getWithdrawalId(), withdrawalRecord.getAmount());

        try {
            // 模拟银行接口调用，假设调用耗时
            Thread.sleep(5000); // 模拟银行接口延迟

            // 模拟银行返回的处理结果
            boolean bankSuccess = true;

            // 更新提现记录状态
            if (bankSuccess) {
                withdrawalRecord.setStatus(WithdrawalRecordsStatus.COMPLETED);
                withdrawalRecord.setCompletionTime(LocalDateTime.now());
                log.info("异步提现处理成功: 提现记录ID: {}", withdrawalRecord.getWithdrawalId());
            } else {
                withdrawalRecord.setStatus(WithdrawalRecordsStatus.FAILED);
                withdrawalRecord.setFailureReason("银行处理失败");
                log.warn("异步提现处理失败: 提现记录ID: {}, 原因: 银行处理失败", withdrawalRecord.getWithdrawalId());
            }

            // 保存提现记录的更新状态
            this.updateById(withdrawalRecord);

        } catch (Exception e) {
            log.error("异步提现处理异常: 提现记录ID: {}, 异常: {}", withdrawalRecord.getWithdrawalId(), e.getMessage(), e);
        }
    }
}
