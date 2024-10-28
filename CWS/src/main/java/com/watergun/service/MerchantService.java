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

    //待确认金额变更日志

    void modifyPendingAmountLog(Long merchantId,BigDecimal amount,String description,String currency);

    void modifyWalletBalanceLog(Long merchantId,Long orderId,BigDecimal amount,String description,String currency);


    //------------serviceLogic----------------
    R<Merchants> getMerchantByMerchantId(Long merchantId);
    R<String> updateMerchant(String token, Merchants merchants);
    R<ShopDTO> getMerchantInfo(Long merchantId);
    R<String> deleteMerchant(String token);

    R<String> withdrawApplication(String token, BigDecimal amount, Long bankAccountId);

    R<Page> getWithdrawApplications(int page,int pageSize,String token,String status);



    //----------管理员方法---------
    // 管理员分页查询用户申请成为商家的申请
    R<Page> adminGetMerchantApplicationPage(int page, int pageSize, String status);

    //管理员审核用户申请成为商家的申请是否通过审核
    R<String> adminApproveMerchantApplication(Long merchantApplicationId,String status,String token);
}
