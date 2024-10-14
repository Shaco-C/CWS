package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.Merchants;

import java.util.List;

public interface MerchantService extends IService<Merchants> {
    R<String> updateMerchant(String token, Merchants merchants);
    R<ShopDTO> getMerchantInfo(Long merchantId);
}
