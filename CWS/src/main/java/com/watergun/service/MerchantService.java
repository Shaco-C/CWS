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

    R<String> withdrawApplication(String token, BigDecimal amount, Long bankAccountId);

    R<Page> getWithdrawApplications(int page,int pageSize,String token,String status);

    //----------管理员方法---------
    // 管理员分页查询用户申请成为商家的申请
    R<Page> adminGetMerchantApplicationPage(int page, int pageSize, String status);

    //管理员审核用户申请成为商家的申请是否通过审核
    R<String> adminApproveMerchantApplication(Long merchantApplicationId,String status,String token);
}
