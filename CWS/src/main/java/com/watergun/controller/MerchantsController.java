package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.Merchants;
import com.watergun.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/merchants")
public class MerchantsController {


    private final MerchantService merchantService;

    public MerchantsController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    //获得商家信息ByMerchantId
    @GetMapping("/getMerchantByMerchantId/{merchantId}")
    public R<Merchants> getMerchantByMerchantId(@PathVariable Long merchantId){
        return merchantService.getMerchantByMerchantId(merchantId);
    }

    //修改商店信息
    @PutMapping("/update")
    public R<String> updateMerchant(HttpServletRequest request, @RequestBody Merchants merchants){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return merchantService.updateMerchant(token,merchants);
    }

    //获取商店详情信息,商店的主页面
    @GetMapping("/getMerchants/{merchantId}")
    public R<ShopDTO> getMerchant(@PathVariable Long merchantId){
        return merchantService.getMerchantInfo(merchantId);
    }

    //注销商店，成为普通用户（需要提前把商品删除干净，钱包提取干净，才可以注销）
    @DeleteMapping("/deleteMerchant")
    public R<String> deleteMerchant(HttpServletRequest request){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return merchantService.deleteMerchant(token);
    }


    //查看商店特定日期范围的营业额

    //处理退货请求


}