package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Products;
import com.watergun.entity.Reviews;
import com.watergun.service.ProductService;
import com.watergun.service.ReviewService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;
    //展示产品通过审核的评论
    @GetMapping("/reviews/{productId}")
    public R<List<Reviews>> getApprovedReviewsByProduct(@PathVariable Long productId) {
        log.info("productId: {}", productId);
        List<Reviews> approvedReviews = reviewService.getApprovedReviewsByProductId(productId);
        log.info("approvedReviews: {}", approvedReviews);
        return R.success(approvedReviews);
    }

    //创建产品
    @PostMapping
    public R<String> createProduct(HttpServletRequest request, @RequestBody Products products) {
        log.info("products: {}", products);
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);

        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        if (!userRole.equals("merchant")) {
            return R.error("用户不是商家，无法创建产品");
        }

        Long UserId = jwtUtil.extractUserId(token);
        log.info("UserId: {}", UserId);

        products.setMerchantId(UserId);
        products.setStatus("pending");
        productService.save(products);

        return R.success("产品创建成功");
    }

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Integer categoryId  ,String sortField, String sortOrder, String searchBox){
        log.info("page: {}, pageSize: {}, categoryId: {}, sortField: {}, sortOrder: {}, searchBox: {}", page, pageSize, categoryId, sortField, sortOrder, searchBox);

        Page pageInfo = new  Page(page, pageSize);
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(categoryId!=null,Products::getCategoryId,categoryId);
        productsLambdaQueryWrapper.like(StringUtils.isNotEmpty(searchBox),Products::getName,searchBox)
                .or()
                .like(StringUtils.isNotEmpty(searchBox),Products::getDescription,searchBox);
        productsLambdaQueryWrapper.eq(Products::getStatus,"approved");//只展示通过审核的产品

        // 排序
        if ("price".equals(sortField)) {//按照价格排序
            productsLambdaQueryWrapper.orderBy(true, "asc".equals(sortOrder), Products::getPrice);
        } else if("sales".equals(sortField)){//按照销量排序
            productsLambdaQueryWrapper.orderBy(true,"asc".equals(sortOrder),Products::getSales);
        } else {
            productsLambdaQueryWrapper.orderByDesc(Products::getCreatedAt);  // 默认按上架时间排序
        }

        productService.page(pageInfo,productsLambdaQueryWrapper);
        return R.success(pageInfo);
    }


    //管理员审核商品是否通过审核
    @PutMapping("/admin/approve/{productId}")
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

    // 管理员分页查询待审核产品
    @GetMapping("/admin/page")
    public R<Page> adminPage(int page, int pageSize, String status) {
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

}
