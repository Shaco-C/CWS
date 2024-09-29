package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.dto.ReviewDTO;
import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import com.watergun.entity.Reviews;
import com.watergun.service.MerchantService;
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
    private MerchantService merchantService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;
    //展示产品通过审核的评论(之后需要整合到ProductDTO中，显示在产品详情中去)
    @GetMapping("/reviews/{productId}")
    public R<List<ReviewDTO>> getApprovedReviewsByProductId(@PathVariable Long productId) {
        log.info("productId: {}", productId);
        List<ReviewDTO> approvedReviews = reviewService.getApprovedReviewsByProductId(productId);
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

    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    @GetMapping("/{id}")
    public R<ProductDTO> getProductDetiails(@PathVariable Long id){
        log.info("正在看产品详情，id为{}",id);
        Products products = productService.getById(id);
        if (products == null) {
            return R.error("商品下架了");
        }
        Merchants merchants = merchantService.getById(products.getMerchantId());
        ProductDTO productDTO = new ProductDTO(products);
        productDTO.setShopName(merchants.getShopName());
        productDTO.setAddress(merchants.getAddress());
        productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
        //获取该产品的所有评论
        productDTO.setReviewsList(reviewService.getApprovedReviewsByProductId(id));

        return R.success(productDTO);

    }
}
