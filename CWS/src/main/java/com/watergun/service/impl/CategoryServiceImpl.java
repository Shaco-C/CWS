package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.entity.Categories;
import com.watergun.entity.Products;
import com.watergun.mapper.CategoriesMapper;
import com.watergun.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.service.ProductService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoriesMapper, Categories> implements CategoryService {

    @Lazy
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProductService productService;

    //按照parentId分类存储
    @Override
    public R<Map<String, List<Categories>>> getCategorySortedList() {
        // 获取所有类别数据
        List<Categories> categoriesList = this.list();

        // 使用Map将类别按parentId分组
        Map<String, List<Categories>> categoryMap = categoriesList.stream()
                .collect(Collectors.groupingBy(category ->
                        category.getParentId() == null ? "parent" : category.getParentId().toString()));
        return R.success(categoryMap);
    }

    //添加类别
    @Override
    public R<String> addCategories(String token, Categories categories) {
        log.info("token: {}", token);
        log.info("categories: {}", categories);
        String userRole = jwtUtil.extractRole(token);
        Long userId =jwtUtil.extractUserId(token);
        if (!userRole.equals("admin")) {
            log.warn("user {} is trying to add Catrgory",userId);
            return R.error("权限不足");
        }
        boolean result = this.save(categories);
        if (!result) {
            return R.error("添加失败");
        }
        return R.success("添加成功");
    }

    //更新类别
    @Override
    public R<String> updateCategories(String token, Categories categories) {
        log.info("token: {}", token);
        log.info("categories: {}", categories);
        String userRole = jwtUtil.extractRole(token);
        Long userId =jwtUtil.extractUserId(token);
        if (!userRole.equals("admin")) {
            log.warn("user {} is trying to update Catrgory",userId);
            return R.error("权限不足");
        }
        boolean result = this.updateById(categories);
        if (!result) {
            return R.error("更新失败");
        }
        return R.success("更新成功");
    }

    @Override
    public R<String> deleteCategories(String token, Long categoryId) {
        log.info("Received request to delete category with ID: {}", categoryId);
        log.info("Token: {}", token);

        // 提取用户角色和ID
        String userRole = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        // 检查是否为管理员
        if (!"admin".equals(userRole)) {
            log.warn("User {} (role: {}) attempted to delete category without sufficient permissions.", userId, userRole);
            return R.error("权限不足，只有管理员可以删除分类。");
        }

        // 检查该分类下是否有商品
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(Products::getCategoryId, categoryId);
        int productCount = (int) productService.count(productsLambdaQueryWrapper);

        log.info("Number of products in category {}: {}", categoryId, productCount);

        // 如果该分类下有商品，抛出异常并阻止删除
        if (productCount > 0) {
            log.warn("Category {} contains {} products, cannot be deleted.", categoryId, productCount);
            throw new CustomException("该类别下有商品，无法删除");
        }

        // 删除分类
        boolean result = this.removeById(categoryId);
        if (!result) {
            log.error("Failed to delete category with ID: {}", categoryId);
            return R.error("删除失败，请稍后重试。");
        }

        log.info("Category {} deleted successfully.", categoryId);
        return R.success("分类删除成功");
    }



}
