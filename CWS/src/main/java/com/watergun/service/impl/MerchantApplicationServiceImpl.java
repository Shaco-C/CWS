package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Merchants;
import com.watergun.entity.Users;
import com.watergun.enums.MerchantApplicationsStatus;
import com.watergun.enums.UserRoles;
import com.watergun.mapper.MerchantApplicationMapper;
import com.watergun.service.MerchantApplicationService;
import com.watergun.service.MerchantService;
import com.watergun.service.UserService;
import com.watergun.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MerchantApplicationServiceImpl extends ServiceImpl<MerchantApplicationMapper, MerchantApplication> implements MerchantApplicationService {


    private final MerchantService merchantService;

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public MerchantApplicationServiceImpl(MerchantService merchantService, UserService userService, JwtUtil jwtUtil) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    //用户申请成为商家
    @Override
    @Transactional
    public R<String> merchantApplication(String token, MerchantApplication merchantApplication) {
        log.info("====================merchantApplication==============================");
        log.info("调用商家申请请求");
        log.info("token: {}", token);
        log.info("merchantApplication: {}", merchantApplication);
        Long userId = jwtUtil.extractUserId(token);
        String userRole =jwtUtil.extractRole(token);
        if (!UserRoles.USER.name().equals(userRole)){
            return R.error("管理员或商家不能够申请成为商家");
        }
        merchantApplication.setStatus(MerchantApplicationsStatus.PENDING);
        merchantApplication.setUserId(userId);
        this.save(merchantApplication);
        return R.success("申请成功");
    }


    // 管理员分页查询用户申请成为商家的申请
    @Override
    public R<Page> adminGetMerchantApplicationPage(int page, int pageSize, String status) {
        log.info("=======================adminGetMerchantApplicationPage=========================");
        log.info("Admin querying merchant applications - page: {}, pageSize: {}, status: {}", page, pageSize, status);

        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<MerchantApplication> merchantApplicationsLambdaQueryWrapper = new LambdaQueryWrapper();
        merchantApplicationsLambdaQueryWrapper.eq(io.micrometer.common.util.StringUtils.isNotEmpty(status), MerchantApplication::getStatus,status)
                .orderByDesc(MerchantApplication::getUpdatedAt);

        this.page(pageInfo,merchantApplicationsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //管理员审核用户申请成为商家的申请是否通过审核(转移到MerchantApplication中)
    @Override
    @Transactional
    public R<String> adminApproveMerchantApplication(Long merchantApplicationId,String status,String token){
        log.info("=======================adminApproveMerchantApplication=========================");
        log.info("merchantId: {}, status: {}", merchantApplicationId, status);
        log.info("token: {}", token);

        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        if (!UserRoles.ADMIN.name().equals(userRole)) {
            return R.error("hello, you are not admin");
        }

        //获取申请详细信息
        LambdaQueryWrapper<MerchantApplication> merchantApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
        merchantApplicationLambdaQueryWrapper.eq(MerchantApplication::getApplicationId,merchantApplicationId);

        //申请表详细信息
        MerchantApplication merchantApplication = this.getOne(merchantApplicationLambdaQueryWrapper);

        if ("REJECTED".equals(status)){
            merchantApplication.setStatus(MerchantApplicationsStatus.REJECTED);
            this.updateById(merchantApplication);
            return R.success("用户成为商家申请被拒绝");
        }

        //从这开始之后就是申请通过的逻辑了
        //更新申请表信息
        merchantApplication.setStatus(MerchantApplicationsStatus.APPROVED);
        this.updateById(merchantApplication);

        //将申请表信息注入商家表
        Merchants merchants = new Merchants();
        merchants.setMerchantId(merchantApplication.getUserId());
        merchants.setShopName(merchantApplication.getShopName());
        merchants.setAddress(merchantApplication.getAddress());
        merchants.setCountry(merchantApplication.getCountry());

        //以下的信息可以是空的值
        merchants.setTaxId(merchantApplication.getTaxId());
        merchants.setPaymentInfo(merchantApplication.getPaymentInfo());
        merchants.setShopAvatarUrl(merchantApplication.getShopAvatarUrl());

        merchantService.save(merchants);

        //同时也要更新用户表，将用户角色改为商家
        Users users = new Users();
        users.setRole(UserRoles.MERCHANT);
        users.setUserId(merchantApplication.getUserId());
        userService.updateById(users);

        return R.success("用户成为商家申请通过");
    }
}
