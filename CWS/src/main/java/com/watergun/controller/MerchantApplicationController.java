package com.watergun.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.service.MerchantApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant-application")
public class MerchantApplicationController {

    private final MerchantApplicationService merchantApplicationService;

    public MerchantApplicationController(MerchantApplicationService merchantApplicationService) {
        this.merchantApplicationService = merchantApplicationService;
    }

    //用户申请成为商家

    @PostMapping("/merchantApplication")
    public R<String> merchantApplication(HttpServletRequest request, @RequestBody MerchantApplication merchantApplication){
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return merchantApplicationService.merchantApplication(token,merchantApplication);

    }



    //----------管理员方法---------
    // 管理员分页查询用户申请成为商家的申请
    @GetMapping("/admin/getMerchantApplicationPage")
    public R<Page> adminGetMerchantApplicationPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "pageSize", defaultValue = "1") int pageSize,
                                                   String status) {
        return merchantApplicationService.adminGetMerchantApplicationPage(page, pageSize, status);
    }

    //管理员审核用户申请成为商家的申请是否通过审核
    @Transactional
    @PutMapping("/admin/approveMerchantApplicationStatus/{merchantApplicationId}")
    public R<String> adminApproveMerchantApplication(@PathVariable Long merchantApplicationId, @RequestParam String status, HttpServletRequest request){
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return merchantApplicationService.adminApproveMerchantApplication(merchantApplicationId,status,token);
    }
}
