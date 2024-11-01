package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.*;
import com.watergun.enums.MerchantApplicationsStatus;
import com.watergun.enums.UserRoles;
import com.watergun.mapper.MerchantsMapper;
import com.watergun.service.*;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantService {

    private final JwtUtil jwtUtil;
    private final ProductService productService;
    private final UserService userService;

    // 使用构造函数注入依赖

    public MerchantServiceImpl(JwtUtil jwtUtil, ProductService productService,
                               UserService userService) {
        this.jwtUtil = jwtUtil;
        this.productService = productService;
        this.userService = userService;
    }


    //---------methods-------------

    @Override
    public void modifyPendingAmount(Long merchantId, BigDecimal amount) {
        log.info("====================addPendingAmount====================");
        log.info("addPendingAmount:商家{}修改待处理金额{}", merchantId, amount);
        Merchants merchants = this.getById(merchantId);
        if (merchants==null){
            log.info("addPendingAmount:商家{}不存在", merchantId);
            throw new CustomException("商家不存在");
        }
        BigDecimal pendingBalance = merchants.getPendingBalance();
        BigDecimal newPendingBalance = pendingBalance.add(amount);
        merchants.setPendingBalance(newPendingBalance);
        log.info("addPendingAmount:商家{}的新待处理金额{}", merchantId, newPendingBalance);
        boolean result = this.updateById(merchants);
        if (!result){
            log.info("addPendingAmount:商家{}的待处理金额修改失败", merchantId);
            throw new CustomException("待处理金额修改失败");
        }
        log.info("addPendingAmount:商家{}的待处理金额修改成功", merchantId);
    }

    //待确认金额转入确认金额
    @Override
    public void modifyWalletBalance(Long merchantId, BigDecimal amount) {
        log.info("====================modifyWalletBalance====================");
        log.info("modifyWalletBalance:商家{}修改钱包余额{}", merchantId, amount);
        Merchants merchants = this.getById(merchantId);
        if (merchants==null){
            log.info("modifyWalletBalance:商家{}不存在", merchantId);
            throw new CustomException("商家不存在");
        }
        log.info("modifyWalletBalance:商家{}的待处理金额{}", merchantId, merchants.getPendingBalance());
        this.modifyPendingAmount(merchantId, amount.negate());
        log.info("modifyWalletBalance:商家{}的待处理金额变更：{}", merchantId, amount.negate());
        BigDecimal walletBalance = merchants.getWalletBalance();
        BigDecimal newWalletBalance = walletBalance.add(amount);
        merchants.setWalletBalance(newWalletBalance);
        log.info("modifyWalletBalance:商家{}的新钱包余额{}", merchantId, newWalletBalance);
        boolean result = this.updateById(merchants);
        if (!result){
            log.info("modifyWalletBalance:商家{}的钱包余额修改失败", merchantId);
            throw new CustomException("钱包余额修改失败");
        }
        log.info("modifyWalletBalance:商家{}的钱包余额修改成功", merchantId);
    }


    //-----------------serviceLogic--------------

    @Override
    public R<Merchants> getMerchantByMerchantId(Long merchantId) {
        log.info("=======================getMerchantByMerchantId=========================");
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
        log.info("=======================updateMerchant=========================");
        log.info("token: {}", token);
        log.info("merchants: {}", merchants);

        // 校验 token 是否有效
        if (jwtUtil.isTokenExpired(token)) {
            return R.error("无效的token");
        }

        Long merchantId = jwtUtil.getUserIdFromToken(token);
        String userRole = jwtUtil.getUserRoleFromToken(token);
        log.info("商家ID: {}", merchantId);
        log.info("userRole: {}", userRole);
        // 如果是管理员，可以修改任何商家信息
        if (UserRoles.ADMIN.name().equals(userRole)) {
            log.info("管理员{}正在修改商家信息", merchantId);
        } else if ( !merchantId.equals(merchants.getMerchantId())) {
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
        log.info("=======================getMerchantInfo=========================");
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
        log.info("=======================deleteMerchant=========================");
        log.info("deleteMerchant已经被调用");
        log.debug("token: {}", token);

        // 校验 token 是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token has expired: {}", token);
            return R.error("token 已过期");
        }

        Long merchantId = jwtUtil.getUserIdFromToken(token);
        String userRole = jwtUtil.getUserRoleFromToken(token);

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
        if (!UserRoles.MERCHANT.name().equals(userRole)) {
            log.warn("非法调用");
            throw new CustomException("非法调用");
        }

        // 将商家角色改为普通用户
        Users users = new Users();
        users.setUserId(merchantId);
        users.setRole(UserRoles.USER);

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
}
