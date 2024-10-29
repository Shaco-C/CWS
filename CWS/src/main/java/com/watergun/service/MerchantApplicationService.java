package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface MerchantApplicationService extends IService<MerchantApplication>  {

    //用户申请成为商家
    R<String> merchantApplication(String token, MerchantApplication merchantApplication);
    // 管理员分页查询用户申请成为商家的申请
    R<Page> adminGetMerchantApplicationPage(int page, int pageSize, String status);
    //管理员审核用户申请成为商家的申请是否通过审核
    R<String> adminApproveMerchantApplication(Long merchantApplicationId,String status,String token);


}
