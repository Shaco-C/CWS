package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.Products;
import com.watergun.mapper.ProductsMapper;
import com.watergun.service.ProductService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductService {


    private final JwtUtil jwtUtil;

    public ProductServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

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
      products.setIsActive(true);
      this.save(products);
      return R.success("Product created successfully");
    }

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    @Override
    public R<Page> page(int page, int pageSize, Long categoryId, String sortField, String sortOrder, String searchBox) {
        log.info("page: {}, pageSize: {}, categoryId: {}, sortField: {}, sortOrder: {}, searchBox: {}", page, pageSize, categoryId, sortField, sortOrder, searchBox);

        Page<Products> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 根据 categoryId 进行筛选
        if (categoryId != null) {
            productsLambdaQueryWrapper.eq(Products::getCategoryId, categoryId);
        }

        // 模糊搜索条件：name 和 description
        if (StringUtils.isNotBlank(searchBox)) {
            productsLambdaQueryWrapper.and(wrapper -> wrapper
                    .like(Products::getName, searchBox)
                    .or()
                    .like(Products::getDescription, searchBox)
            );
        }

        // 只显示审核通过的和上架的产品
        productsLambdaQueryWrapper.eq(Products::getStatus, "approved");
        productsLambdaQueryWrapper.eq(Products::getIsActive, true);

        // 根据排序字段和排序顺序进行排序
        if ("price".equals(sortField)) {
            productsLambdaQueryWrapper.orderBy(true, "asc".equals(sortOrder), Products::getPrice);
        } else if ("sales".equals(sortField)) {
            productsLambdaQueryWrapper.orderBy(true, "asc".equals(sortOrder), Products::getSales);
        } else {
            productsLambdaQueryWrapper.orderByDesc(Products::getCreatedAt); // 默认按创建时间降序排列
        }

        this.page(pageInfo, productsLambdaQueryWrapper);
        return R.success(pageInfo);
    }


    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    //需要前端调取3个axios请求------替代掉原有的ProductDTO
    //1.获取产品详情
    //2.获取商家信息
    //3.获取产品评论
    @Override
    public R<Products> getProductDetiails(Long id) {
        log.info("getProductDetiails method is called");
        log.info("id: {}", id);

        Products products = this.getById(id);

        if (products == null) {
            return R.error("Product not found");
        }

        if (!products.getIsActive()){
            return R.error("Product is not on sale");
        }

        return R.success(products);
    }

    @Override
    public R<String> deleteProduct(String token, Long productId) {
        log.info("deleteProduct method is called");
        log.info("token: {}, productId: {}", token, productId);
        Long userId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);
        if (userId == null || token == null || productId == null) {
            return R.error("Invalid request");
        }
        if (!userRole.equals("merchant")) {
            return R.error("You are not authorized to delete a product");
        }
        Products products = this.getById(productId);
        if (products == null) {
            return R.error("Product not found");
        }
        if (!products.getMerchantId().equals(userId)) {
            return R.error("You are not authorized to delete this product");
        }
        Boolean result = this.removeById(products);
        if (!result){
            return R.error("Product delete failed");
        }
        return R.success("Product deleted successfully");
    }

    //商家修改产品信息
    //前端需要回传productId,merchantId
    @Override
    public R<String> updateProduct(String token, Products products) {
        log.info("updateProduct method is called");
        log.info("token: {}, products: {}", token, products);

        Long userId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 验证用户是否是商家
        if (!userRole.equals("merchant")) {
            log.warn("User {} is not a merchant, not authorized to update product", userId);
            return R.error("You are not authorized to update a product");
        }

        // 验证用户是否有权更新该产品
        if (!userId.equals(products.getMerchantId())) {
            log.warn("User {} is not the owner of product {}", userId, products.getProductId());
            return R.error("You are not authorized to update this product");
        }

        //商家不能够更新产品的status
        products.setStatus(null);

        // 更新产品信息
        boolean result = this.updateById(products);
        if (!result) {
            return R.error("Product update failed");
        }

        return R.success("Product updated successfully");
    }

    @Override
    public R<String> setActiveOrInActiveProduct(String token, Long productId, Boolean active) {
        log.info("setActiveOrInActiveProduct method is called");
        log.info("setActiveOrInActiveProduct:token: {}, productId: {}, active: {}", token, productId, active);
        Long userId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        log.info("userId: {}", userId);
        if (userId == null || token == null || productId == null) {
            return R.error("Invalid request");
        }
        if (!"merchant".equals(userRole))  {
            return R.error("You are not authorized to update a product");
        }

        Products products = this.getById(productId);
        if (products == null) {
            return R.error("Product not found");
        }

        if (!products.getMerchantId().equals(userId)) {
            return R.error("You are not authorized to update this product");
        }

        products.setIsActive(!active);
        boolean result = this.updateById(products);
        if (!result) {
            return R.error("Product update failed");
        }
        return R.success("Product updated successfully");
    }


    //商家查看自己的产品
    @Override
    public R<Page> getMyProducts(int page,int pageSize,String token,String sortField, String sortOrder) {
        log.info("getMyProducts method is called");
        log.info("token: {}", token);
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        if (userId == null) {
            return R.error("Invalid request");
        }
        Page<Products> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(Products::getMerchantId,userId);

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



    //------------管理员方法--------------
    // 管理员分页查询待审核产品
    @Override
    public R<Page> adminGetProductsPage(int page, int pageSize, String status) {
        log.info("Admin querying products - page: {}, pageSize: {}, status: {}", page, pageSize, status);

        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 根据状态筛选待审核的产品（pending、approved、rejected）
        if (StringUtils.isNotEmpty(status)) {
            productsLambdaQueryWrapper.eq(Products::getStatus, status);
        }

        // 默认按照产品上架时间倒序排列
        productsLambdaQueryWrapper.orderByDesc(Products::getCreatedAt);

        this.page(pageInfo, productsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //管理员审核商品是否通过审核
    @Override
    public R<String> adminApproveProduct(Long productId, String status, String token) {
        log.info("productId: {}, status: {}", productId, status);
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

        if(!this.updateById(products)){
            return R.error("产品审核失败");
        }

        return R.success("产品审核完成");
    }

}
