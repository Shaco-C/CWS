package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.*;
import com.watergun.service.*;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductService productService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantApplicationService merchantApplicationService;
    @Autowired
    private JwtUtil jwtUtil;

    //管理员查看所有用户请求
    @GetMapping("/users/page")
    public R<Page> usersPage(int page, int pageSize, String role){
        log.info("分页查询请求");
        log.info("page = {}, pageSize = {}, role = {}",page,pageSize,role);
        Page pageInfo = new Page(page,pageSize);
        log.info("查看Users表信息");
        LambdaQueryWrapper<Users> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(role),Users::getRole,role);
        lambdaQueryWrapper.orderByDesc(Users::getUpdatedAt);

        userService.page(pageInfo,lambdaQueryWrapper);

        return  R.success(pageInfo);
    }

    //展示所有评论，按照status进行分类，如果前端没传默认查询所有的评论  (对管理员)
    @GetMapping("/reviews/page")
    public R<Page> reviewsPage(int page, int pageSize, String status){
        log.info("page: {}, pageSize: {}, status: {}", page, pageSize, status);
        Page pageInfo = new Page(page,pageSize);
        log.info("查看评论");
        LambdaQueryWrapper<Reviews> reviewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        reviewsLambdaQueryWrapper.like(StringUtils.isNotEmpty(status),Reviews::getStatus,status)
                .orderByDesc(Reviews::getUpdatedAt); //按照时间排序
        reviewService.page(pageInfo,reviewsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    // 管理员分页查询待审核产品
    @GetMapping("/products/page")
    public R<Page> productsPage(int page, int pageSize, String status) {
        log.info("Admin querying products - page: {}, pageSize: {}, status: {}", page, pageSize, status);

        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 根据状态筛选待审核的产品（pending、approved、rejected）
        if (StringUtils.isNotEmpty(status)) {
            productsLambdaQueryWrapper.eq(Products::getStatus, status);
        }

        // 默认按照产品上架时间倒序排列
        productsLambdaQueryWrapper.orderByDesc(Products::getCreatedAt);

        productService.page(pageInfo, productsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    // 管理员分页查询用户申请成为商家的申请
    @GetMapping("/merchantApplication/page")
    public R<Page> merchantApplicationsPage(int page, int pageSize, String status) {
        log.info("Admin querying merchant applications - page: {}, pageSize: {}, status: {}", page, pageSize, status);

        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<MerchantApplication> merchantApplicationsLambdaQueryWrapper = new LambdaQueryWrapper();
        merchantApplicationsLambdaQueryWrapper.eq(StringUtils.isNotEmpty(status), MerchantApplication::getStatus,status)
                .orderByDesc(MerchantApplication::getUpdatedAt);

        merchantApplicationService.page(pageInfo,merchantApplicationsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    // 管理员审核评论（通过或拒绝）
    @PutMapping("/reviews/reviewStatus/{id}")
    public R<String> reviewStatus(@PathVariable Long id, @RequestParam String status, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);
        // 提取 JWT 中的用户角色
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);

        // 检查用户是否是管理员
        if (!"admin".equals(userRole)) {
            return R.error("你无权进行审核操作");
        }

        // 根据评论ID获取评论详情
        Reviews review = reviewService.getById(id);
        if (review == null) {
            return R.error("评论不存在");
        }

        // 更新评论状态
        review.setStatus(status);
        reviewService.updateById(review);

        return R.success("评论状态更新为: " + status);
    }

    //管理员审核商品是否通过审核
    @PutMapping("/products/productStatus/{productId}")
    public R<String> approveProduct(@PathVariable Long productId,@RequestParam String status,HttpServletRequest request){
        log.info("productId: {}, status: {}", productId, status);

        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);

        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        if (!userRole.equals("admin")) {
            return R.error("hello, you are not admin");
        }

        // 检查状态是否有效
        if (!status.equals("approved") && !status.equals("rejected")) {
            return R.error("Invalid status");
        }

        Products products = new Products();
        products.setProductId(productId);
        products.setStatus(status);

        if(!productService.updateById(products)){
            return R.error("产品审核失败");
        }

        return R.success("产品审核完成");
    }


    //管理员审核用户申请成为商家的申请是否通过审核
    @Transactional
    @PutMapping("/merchantApplication/merchantApplicationStatus/{merchantApplicationId}")
    public R<String> approveMerchantApplication(@PathVariable Long merchantApplicationId,@RequestParam String status,HttpServletRequest request){
        log.info("merchantId: {}, status: {}", merchantApplicationId, status);

        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);

        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        if (!userRole.equals("admin")) {
            return R.error("hello, you are not admin");
        }

        //获取申请详细信息
        LambdaQueryWrapper<MerchantApplication> merchantApplicationLambdaQueryWrapper = new LambdaQueryWrapper<>();
        merchantApplicationLambdaQueryWrapper.eq(MerchantApplication::getApplicationId,merchantApplicationId);

        //申请表详细信息
        MerchantApplication merchantApplication = merchantApplicationService.getOne(merchantApplicationLambdaQueryWrapper);

        if (status.equals("rejected")){
            merchantApplication.setStatus("rejected");
            merchantApplicationService.updateById(merchantApplication);
            return R.success("用户成为商家申请被拒绝");
        }

        //从这开始之后就是申请通过的逻辑了
        //更新申请表信息
        merchantApplication.setStatus("approved");
        merchantApplicationService.updateById(merchantApplication);

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
        users.setRole("merchant");
        users.setUserId(merchantApplication.getUserId());
        userService.updateById(users);

        return R.success("用户成为商家申请通过");
    }
}
