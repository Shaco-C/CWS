package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.Merchants;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantService extends IService<Merchants> {
    R<String> updateMerchant(String token, Merchants merchants);
    R<ShopDTO> getMerchantInfo(Long merchantId);
    R<String> deleteMerchant(String token);
    R<Page> getOrders(int page,int pageSize,String token,String status,String returnStatus);

    R<String> withdraw(String token, BigDecimal amount);
}
