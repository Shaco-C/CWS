package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.entity.BankAccounts;
import com.watergun.entity.Merchants;
import com.watergun.entity.WithdrawalRecords;
import com.watergun.enums.UserRoles;
import com.watergun.enums.WithdrawalRecordsStatus;
import com.watergun.mapper.WithdrawalRecordsMapper;
import com.watergun.service.BankAccountsService;
import com.watergun.service.MerchantService;
import com.watergun.service.WithdrawalRecordsService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class WithdrawalRecordsServiceImpl extends ServiceImpl<WithdrawalRecordsMapper, WithdrawalRecords> implements WithdrawalRecordsService {

    private final JwtUtil jwtUtil;
    private final MerchantService merchantService;

    private final BankAccountsService bankAccountsService;

    public WithdrawalRecordsServiceImpl(JwtUtil jwtUtil, MerchantService merchantService, BankAccountsService bankAccountsService) {
        this.jwtUtil = jwtUtil;
        this.merchantService = merchantService;
        this.bankAccountsService = bankAccountsService;
    }
    //================methods=================
    @Async // 异步执行
    @Override
    public void processWithdrawAsync(WithdrawalRecords withdrawalRecord,Merchants merchants) {
        log.info("==================processWithdrawAsync===============================");
        log.info("开始处理异步提现申请: 提现记录ID: {}, 金额: {}", withdrawalRecord.getWithdrawalId(), withdrawalRecord.getAmount());

        try {
            // 模拟银行接口调用，假设调用耗时
            Thread.sleep(5000); // 模拟银行接口延迟

            // 模拟银行返回的处理结果
            boolean bankSuccess = false;

            // 更新提现记录状态
            if (bankSuccess) {
                withdrawalRecord.setStatus(WithdrawalRecordsStatus.COMPLETED);
                withdrawalRecord.setCompletionTime(LocalDateTime.now());
                log.info("异步提现处理成功: 提现记录ID: {}", withdrawalRecord.getWithdrawalId());
            } else {
                withdrawalRecord.setStatus(WithdrawalRecordsStatus.FAILED);
                withdrawalRecord.setFailureReason("银行处理失败");
                log.warn("异步提现处理失败: 提现记录ID: {}, 原因: 银行处理失败", withdrawalRecord.getWithdrawalId());
                merchants.setWalletBalance(merchants.getWalletBalance().add(withdrawalRecord.getAmount()));
                boolean result =merchantService.updateById(merchants); // 保存提现记录的更新状态
                if (!result){
                    log.error("提现失败，将钱退回商家钱包失败: 商家ID: {}, 金额: {}", merchants.getMerchantId(), withdrawalRecord.getAmount());
                    throw new CustomException("提现失败，将钱退回商家钱包失败");
                }
                log.info("提现失败，将钱退回商家钱包: 商家ID: {}, 金额: {}", merchants.getMerchantId(), withdrawalRecord.getAmount());
                merchantService.modifyWalletBalanceLog(merchants.getMerchantId(),null,withdrawalRecord.getAmount(),
                        "提现失败，将钱退回商家钱包", withdrawalRecord.getCurrency());
            }

            // 保存提现记录的更新状态
            this.updateById(withdrawalRecord);

        } catch (Exception e) {
            log.error("异步提现处理异常: 提现记录ID: {}, 异常: {}", withdrawalRecord.getWithdrawalId(), e.getMessage(), e);
        }
    }
    //===========serviceLogic============
    //申请提现确认金额中的钱
    @Override
    @Transactional
    public R<String> withdrawApplication(String token, BigDecimal amount, Long bankAccountId) {
        log.info("=======================withdrawApplication=========================");
        log.info("withdraw方法: token: {}, amount: {}, bankAccountId: {}", token, amount, bankAccountId);

        // 校验用户身份
        String userRole = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        if (!UserRoles.MERCHANT.name().equals(userRole)) {
            log.warn("withdraw方法: 非法用户角色尝试提现");
            return R.error("只有商家角色可以进行提现操作");
        }

        // 校验提现金额
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("withdraw方法: 提现金额不合法");
            return R.error("提现金额不合法");
        }

        // 检查提现金额是否过大
        BigDecimal maxWithdrawLimit = new BigDecimal("10000.00"); // 假设最大一次提现上限为10000
        if (amount.compareTo(maxWithdrawLimit) > 0) {
            log.warn("withdraw方法: 提现金额超过最大限制");
            return R.error("提现金额超过最大限制");
        }

        // 检查商家信息
        Merchants merchants = merchantService.getById(userId);
        if (merchants == null) {
            log.warn("withdraw方法: 商家不存在");
            return R.error("商家不存在");
        }

        // 校验余额是否足够
        BigDecimal walletBalance = merchants.getWalletBalance();
        if (walletBalance.compareTo(amount) < 0) {
            log.warn("withdraw方法: 商家余额不足，当前余额: {}, 请求提现金额: {}", walletBalance, amount);
            return R.error("商家余额不足");
        }

        // 校验银行账户是否存在
        BankAccounts bankAccount = bankAccountsService.getById(bankAccountId);
        if (bankAccount == null || !bankAccount.getUserId().equals(userId)) {
            log.warn("withdraw方法: 银行账户不存在或不属于当前商家");
            return R.error("无效的银行账户");
        }

        // 记录提现申请，状态为 pending(转移到withdrawalRecordsService处理)
        WithdrawalRecords withdrawalRecord = new WithdrawalRecords();
        withdrawalRecord.setMerchantId(userId);
        withdrawalRecord.setAmount(amount);
        withdrawalRecord.setStatus(WithdrawalRecordsStatus.PENDING); // 初始状态为 pending
        withdrawalRecord.setBankAccountId(bankAccountId);
        withdrawalRecord.setCurrency("CNY"); // 根据需求动态设定货币类型
        withdrawalRecord.setRequestTime(LocalDateTime.now());

        boolean recordSaved = this.save(withdrawalRecord);
        if (!recordSaved) {
            log.warn("withdraw方法: 提现记录保存失败");
            return R.error("提现申请失败");
        }

        // 扣除余额并更新商家信息
        merchants.setWalletBalance(walletBalance.subtract(amount));
        boolean merchantUpdated = merchantService.updateById(merchants);
        if (!merchantUpdated) {
            log.warn("withdraw方法: 提现失败，余额更新错误");
            return R.error("提现失败");
        }

        //添加确认金额变更日志
        merchantService.modifyWalletBalanceLog(userId,null, amount.negate(),
                "提现到银行"+bankAccount.getBankName()+"账户为:"+bankAccount.getAccountNumber(),"CNY");

        // 提交异步处理提现请求
        this.processWithdrawAsync(withdrawalRecord,merchants);

        log.info("withdraw方法: 提现申请成功，剩余余额: {}", merchants.getWalletBalance());
        return R.success("提现申请成功" );
    }

    //查看所有提现记录
    @Override
    @Transactional(readOnly = true)
    public R<Page> getWithdrawApplications(int page, int pageSize, String token, String status) {
        log.info("=======================getWithdrawApplications=========================");
        log.info("getWithdrawApplications方法: page: {}, pageSize: {}, token: {}, status: {}", page, pageSize, token, status);

        // 校验用户身份
        try {
            String userRole = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            log.info("getWithdrawApplications方法: userRole: {}, userId: {}", userRole, userId);
            if (!UserRoles.MERCHANT.name().equals(userRole)) {
                log.warn("getWithdrawApplications方法: 非法用户角色尝试获取提现申请");
                return R.error("只有商家角色可以获取提现申请");
            }

            // 创建查询条件
            LambdaQueryWrapper<WithdrawalRecords> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WithdrawalRecords::getMerchantId, userId)
                    .eq(StringUtils.isNotEmpty(status), WithdrawalRecords::getStatus, status)
                    .orderByDesc(WithdrawalRecords::getRequestTime); // 按申请时间倒序排列

            // 执行分页查询
            Page<WithdrawalRecords> pageInfo = new Page<>(page, pageSize);
            pageInfo = this.page(pageInfo, queryWrapper);

            log.info("getWithdrawApplications方法: 查询成功，结果数量: {}", pageInfo.getRecords().size());
            return R.success(pageInfo);
        } catch (Exception e) {
            log.error("getWithdrawApplications方法: 查询提现申请时发生错误", e);
            return R.error("获取提现申请失败，请稍后重试");
        }
    }
}
