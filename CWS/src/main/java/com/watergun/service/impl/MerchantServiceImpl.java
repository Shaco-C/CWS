package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import com.watergun.mapper.MerchantsMapper;
import com.watergun.service.MerchantService;
import com.watergun.service.ProductService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantService {

    @Autowired
    @Lazy
    private JwtUtil jwtUtil;

    @Autowired
    private ProductService productService;
    @Override
    public R<String> updateMerchant(String token, Merchants merchants) {
        log.info("token: {}", token);
        log.info("merchants: {}", merchants);

        // 校验 token 是否有效
        if (!jwtUtil.isTokenExpired(token)) {
            return R.error("无效的token");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 如果是管理员，可以修改任何商家信息
        if ("admin".equals(userRole)) {
            log.info("管理员{}正在修改商家信息", merchantId);
        } else if (merchantId == null || !merchantId.equals(merchants.getMerchantId())) {
            // 如果是商家本人，必须匹配商家ID
            log.warn("商家{}尝试修改无权限的商家信息", merchantId);
            return R.error("权限不足");
        }

        // 更新商家信息
        boolean result = this.updateById(merchants);
        if (!result) {
            return R.error("修改失败，可能是数据库问题或商家信息不存在");
        }

        return R.success("商家信息修改成功");
    }

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
        ShopDTO shopDTO = new ShopDTO(merchants);  // 假设你有一个构造函数可以初始化

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


}
