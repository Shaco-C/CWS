package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.Merchants;

import java.math.BigDecimal;

public interface MerchantService extends IService<Merchants> {
    //--------methods----------

    //用户付款之后，金额转入商家的待确认金额
    void modifyPendingAmount(Long merchantId, BigDecimal amount);

    //用户收货之后，待确认金额转入确认金额
    void modifyWalletBalance(Long merchantId, BigDecimal amount);


    //------------serviceLogic----------------
    R<Merchants> getMerchantByMerchantId(Long merchantId);
    R<String> updateMerchant(String token, Merchants merchants);
    R<ShopDTO> getMerchantInfo(Long merchantId);
    R<String> deleteMerchant(String token);


}
