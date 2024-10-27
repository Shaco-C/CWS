package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.*;
import com.watergun.mapper.MerchantsMapper;
import com.watergun.service.*;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantService {

    private final JwtUtil jwtUtil;
    private final ProductService productService;
    private final UserService userService;
    private final BankAccountsService bankAccountsService;
    private final WithdrawalRecordsService withdrawalRecordsService;
    private final MerchantApplicationService merchantApplicationService;

    private final PendingAmountLogService pendingAmountLogService;

    // 使用构造函数注入依赖

    public MerchantServiceImpl(JwtUtil jwtUtil, ProductService productService,
                               UserService userService, BankAccountsService bankAccountsService,
                               WithdrawalRecordsService withdrawalRecordsService,
                               MerchantApplicationService merchantApplicationService,
                               PendingAmountLogService pendingAmountLogService) {
        this.jwtUtil = jwtUtil;
        this.productService = productService;
        this.userService = userService;
        this.bankAccountsService = bankAccountsService;
        this.withdrawalRecordsService = withdrawalRecordsService;
        this.merchantApplicationService = merchantApplicationService;
        this.pendingAmountLogService = pendingAmountLogService;
    }


    //---------methods-------------

    @Override
    public void addPendingAmount(Long merchantId, BigDecimal amount) {
        log.info("addPendingAmount:商家{}增加待处理金额{}", merchantId, amount);
        Merchants merchants = this.getById(merchantId);
        BigDecimal pendingBalance = merchants.getPendingBalance();
        BigDecimal newPendingBalance = pendingBalance.add(amount);
        merchants.setPendingBalance(newPendingBalance);
        log.info("addPendingAmount:商家{}的新待处理金额{}", merchantId, newPendingBalance);
        this.updateById(merchants);
    }

    //待确认金额变更日志
    @Override
    public void addPendingAmountLog(Long merchantId,  BigDecimal amount,String description,String currency) {
        log.info("addPendingAmountLog:商家{}增加待确认金额{}", merchantId, amount);
        PendingAmountLog pendingAmountLog = new PendingAmountLog();
        pendingAmountLog.setAmount(amount);
        pendingAmountLog.setMerchantId(merchantId);
        pendingAmountLog.setDescription(description);
        pendingAmountLog.setCurrency(currency);

        boolean result = pendingAmountLogService.save(pendingAmountLog);
        if (!result){
            throw new CustomException("添加待确认金额日志失败");
        }
        log.info("添加待确认金额日志成功");

    }

    //-----------------serviceLogic--------------

    @Override
    public R<Merchants> getMerchantByMerchantId(Long merchantId) {
        log.info("获取商家信息，商家ID: {}", merchantId);
        if (merchantId == null) {
            return R.error("商家ID不能为空");
        }
        Merchants merchants = this.getById(merchantId);
        if (merchants == null) {
            return R.error("商家不存在");
        }
        return R.success(merchants);
    }

    @Override
    public R<String> updateMerchant(String token, Merchants merchants) {
        log.info("token: {}", token);
        log.info("merchants: {}", merchants);

        // 校验 token 是否有效
        if (jwtUtil.isTokenExpired(token)) {
            return R.error("无效的token");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);
        log.info("商家ID: {}", merchantId);
        log.info("userRole: {}", userRole);
        // 如果是管理员，可以修改任何商家信息
        if ("admin".equals(userRole)) {
            log.info("管理员{}正在修改商家信息", merchantId);
        } else if (merchantId == null || !merchantId.equals(merchants.getMerchantId())) {
            // 如果是商家本人，必须匹配商家ID
            log.warn("商家{}尝试修改无权限的商家{}信息", merchantId, merchants.getMerchantId());
            return R.error("权限不足");
        }

        // 更新商家信息
        boolean result = this.updateById(merchants);
        if (!result) {
            return R.error("修改失败，可能是数据库问题或商家信息不存在");
        }

        return R.success("商家信息修改成功");
    }

    //可化简，多余方法ShopDTO
    @Override
    public R<ShopDTO> getMerchantInfo(Long merchantId) {
        log.info("获取商家信息，商家ID: {}", merchantId);

        if (merchantId == null) {
            return R.error("商家ID不能为空");
        }

        // 获取商家信息
        Merchants merchants = this.getById(merchantId);
        log.info("商家信息: {}", merchants);

        if (merchants == null) {
            return R.error("商家不存在");
        }

        // 初始化ShopDTO对象
        ShopDTO shopDTO = new ShopDTO(merchants);

        // 获取商家的产品列表
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(Products::getMerchantId, merchantId);
        List<Products> products = productService.list(productsLambdaQueryWrapper);

        // 设置产品列表
        shopDTO.setProductsList(products);

        log.info("商铺信息和产品列表: {}", shopDTO);

        // 返回商铺DTO信息
        return R.success(shopDTO);
    }

    @Override
    @Transactional
    public R<String> deleteMerchant(String token) {
        log.info("deleteMerchant已经被调用");
        log.debug("token: {}", token);

        // 校验 token 是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token has expired: {}", token);
            return R.error("token 已过期");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 检查商家是否存在商品
        Long cnt = productService.count(new LambdaQueryWrapper<Products>().eq(Products::getMerchantId, merchantId).last("LIMIT 1"));
        if (cnt > 0) {
            log.warn("商家下还有产品，无法删除");
            return R.error("商家下还有产品，无法删除");
        }

        // 检查商家的钱包是否处理完毕
        Merchants merchants = this.getById(merchantId);
        BigDecimal walletBalance = merchants.getWalletBalance();
        BigDecimal pendingBalance = merchants.getPendingBalance();

        if (walletBalance.compareTo(BigDecimal.ZERO) != 0 || pendingBalance.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("商家钱包未处理完毕，无法删除");
            throw new CustomException("商家钱包未处理完毕，无法删除");
        }

        // 检查角色是否为商家
        if (!"merchant".equals(userRole)) {
            log.warn("非法调用");
            throw new CustomException("非法调用");
        }

        // 将商家角色改为普通用户
        Users users = new Users();
        users.setUserId(merchantId);
        users.setRole("user");

        boolean userUpdateResult = userService.updateById(users);
        if (!userUpdateResult) {
            log.warn("用户角色更新失败");
            throw new CustomException("用户角色更新失败");
        }

        // 删除商家
        boolean result = this.removeById(merchantId);
        if (!result) {
            log.warn("删除失败，可能是数据库问题或商家信息不存在");
            throw new CustomException("删除失败，可能是数据库问题或商家信息不存在");
        }

        return R.success("商家删除成功");
    }

    @Override
    @Transactional
    public R<String> withdrawApplication(String token, BigDecimal amount, Long bankAccountId) {
        log.info("withdraw方法: token: {}, amount: {}, bankAccountId: {}", token, amount, bankAccountId);

        // 校验用户身份
        String userRole = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        if (!"merchant".equals(userRole)) {
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
        Merchants merchants = this.getById(userId);
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

        // 记录提现申请，状态为 pending
        WithdrawalRecords withdrawalRecord = new WithdrawalRecords();
        withdrawalRecord.setMerchantId(userId);
        withdrawalRecord.setAmount(amount);
        withdrawalRecord.setStatus("pending"); // 初始状态为 pending
        withdrawalRecord.setBankAccountId(bankAccountId);
        withdrawalRecord.setCurrency("CNY"); // 根据需求动态设定货币类型
        withdrawalRecord.setRequestTime(LocalDateTime.now());

        boolean recordSaved = withdrawalRecordsService.save(withdrawalRecord);
        if (!recordSaved) {
            log.warn("withdraw方法: 提现记录保存失败");
            return R.error("提现申请失败");
        }

        // 扣除余额并更新商家信息
        merchants.setWalletBalance(walletBalance.subtract(amount));
        boolean merchantUpdated = this.updateById(merchants);
        if (!merchantUpdated) {
            log.warn("withdraw方法: 提现失败，余额更新错误");
            return R.error("提现失败");
        }

        // 提交异步处理提现请求
        withdrawalRecordsService.processWithdrawAsync(withdrawalRecord);

        log.info("withdraw方法: 提现申请成功，剩余余额: {}", merchants.getWalletBalance());
        return R.success("提现申请成功，剩余余额: " + merchants.getWalletBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public R<Page> getWithdrawApplications(int page, int pageSize, String token, String status) {
        log.info("getWithdrawApplications方法: page: {}, pageSize: {}, token: {}, status: {}", page, pageSize, token, status);

        // 校验用户身份
        try {
            String userRole = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            log.info("getWithdrawApplications方法: userRole: {}, userId: {}", userRole, userId);
            if (!"merchant".equals(userRole)) {
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
            pageInfo = withdrawalRecordsService.page(pageInfo, queryWrapper);

            log.info("getWithdrawApplications方法: 查询成功，结果数量: {}", pageInfo.getRecords().size());
            return R.success(pageInfo);
        } catch (Exception e) {
            log.error("getWithdrawApplications方法: 查询提现申请时发生错误", e);
            return R.error("获取提现申请失败，请稍后重试");
        }
    }



    //----------管理员方法---------
    // 管理员分页查询用户申请成为商家的申请
    @Override
    public R<Page> adminGetMerchantApplicationPage(int page, int pageSize, String status) {
        log.info("Admin querying merchant applications - page: {}, pageSize: {}, status: {}", page, pageSize, status);

        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<MerchantApplication> merchantApplicationsLambdaQueryWrapper = new LambdaQueryWrapper();
        merchantApplicationsLambdaQueryWrapper.eq(io.micrometer.common.util.StringUtils.isNotEmpty(status), MerchantApplication::getStatus,status)
                .orderByDesc(MerchantApplication::getUpdatedAt);

        merchantApplicationService.page(pageInfo,merchantApplicationsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //管理员审核用户申请成为商家的申请是否通过审核(转移到MerchantApplication中)
    @Override
    @Transactional
    public R<String> adminApproveMerchantApplication(Long merchantApplicationId,String status,String token){
        log.info("merchantId: {}, status: {}", merchantApplicationId, status);
        log.info("token: {}", token);

        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        if (!"admin".equals(userRole)) {
            return R.error("hello, you are not admin");
        }

        //获取申请详细信息
        LambdaQueryWrapper<MerchantApplication> merchantApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
        merchantApplicationLambdaQueryWrapper.eq(MerchantApplication::getApplicationId,merchantApplicationId);

        //申请表详细信息
        MerchantApplication merchantApplication = merchantApplicationService.getOne(merchantApplicationLambdaQueryWrapper);

        if ("rejected".equals(status)){
            merchantApplication.setStatus("rejected");
            merchantApplicationService.updateById(merchantApplication);
            return R.success("用户成为商家申请被拒绝");
        }

        //从这开始之后就是申请通过的逻辑了
        //更新申请表信息
        merchantApplication.setStatus("approved");
        merchantApplicationService.updateById(merchantApplication);

        //将申请表信息注入商家表
        Merchants merchants = new Merchants();
        merchants.setMerchantId(merchantApplication.getUserId());
        merchants.setShopName(merchantApplication.getShopName());
        merchants.setAddress(merchantApplication.getAddress());
        merchants.setCountry(merchantApplication.getCountry());

        //以下的信息可以是空的值
        merchants.setTaxId(merchantApplication.getTaxId());
        merchants.setPaymentInfo(merchantApplication.getPaymentInfo());
        merchants.setShopAvatarUrl(merchantApplication.getShopAvatarUrl());

        this.save(merchants);

        //同时也要更新用户表，将用户角色改为商家
        Users users = new Users();
        users.setRole("merchant");
        users.setUserId(merchantApplication.getUserId());
        userService.updateById(users);

        return R.success("用户成为商家申请通过");
    }


}
