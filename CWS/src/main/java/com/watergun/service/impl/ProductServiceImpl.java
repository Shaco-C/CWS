package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import com.watergun.mapper.ProductsMapper;
import com.watergun.service.MerchantService;
import com.watergun.service.ProductService;
import com.watergun.service.ReviewService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductService {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ReviewService reviewService;

    //创建产品
    @Override
    public R<String> createProduct(String token, Products products) {
      log.info("createProduct method is called");
      log.info("token: {}", token);
      log.info("products: {}", products);

      Long userId = jwtUtil.extractUserId(token);
      log.info("userId: {}", userId);

      String userRole = jwtUtil.extractRole(token);
      log.info("userRole: {}", userRole);

      if (!userRole.equals("merchant")) {
        return R.error("You are not authorized to create a product");
      }
      products.setMerchantId(userId);
      products.setStatus("pending");
      this.save(products);
      return R.success("Product created successfully");
    }

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    @Override
    public R<Page> page(int page, int pageSize, Integer categoryId, String sortField, String sortOrder, String searchBox) {
        log.info("page:{}, pageSize:{}, categoryId:{}, sortField:{}, sortOrder:{}, searchBox:{}", page, pageSize, categoryId, sortField, sortOrder, searchBox);
        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper =new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(categoryId != null, Products::getCategoryId, categoryId); //根据产品种类进行筛选
        productsLambdaQueryWrapper.like(StringUtils.isNotBlank(searchBox), Products::getName, searchBox)//根据搜索框进行模糊搜索
                .or().like(StringUtils.isNotBlank(searchBox), Products::getDescription, searchBox);
        productsLambdaQueryWrapper.eq(Products::getStatus, "approved"); //只显示已审核的产品

        //根据排序字段和排序顺序进行排序
        if ("price".equals(sortField)) {
            productsLambdaQueryWrapper.orderBy(true,"asc".equals(sortOrder),Products::getPrice);
        } else if ("sales".equals(sortField)) {//根据销量进行排序
            productsLambdaQueryWrapper.orderBy(true,"asc".equals(sortOrder),Products::getSales);
        }else {
            productsLambdaQueryWrapper.orderByDesc(Products::getCreatedAt);//默认按照创建时间进行排序
        }

        this.page(pageInfo, productsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    @Override
    public R<ProductDTO> getProductDetiails(Long id) {
        log.info("getProductDetiails method is called");
        log.info("id: {}", id);

        Products products = this.getById(id);

        if (products == null) {
            return R.error("Product not found");
        }

        Merchants merchants = merchantService.getById(products.getMerchantId());
        ProductDTO productDTO = new ProductDTO(products);
        productDTO.setShopName(merchants.getShopName());
        productDTO.setAddress(merchants.getAddress());
        productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
        //获取该产品所有通过审核的评论
        productDTO.setReviewsList(reviewService.getApprovedReviewsByProductId(id));

        return R.success(productDTO);
    }
}
