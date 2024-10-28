package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Favorites;
import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import com.watergun.mapper.FavoritesMapper;
import com.watergun.service.FavoritesService;
import com.watergun.service.MerchantService;
import com.watergun.service.ProductService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    private final JwtUtil jwtUtil;
    private final ProductService productService;
    private final MerchantService merchantService;

    public FavoritesServiceImpl(JwtUtil jwtUtil, ProductService productService, MerchantService merchantService) {
        this.jwtUtil = jwtUtil;
        this.productService = productService;
        this.merchantService = merchantService;
    }

    //添加收藏
    @Override
    public R<String> addToFavorites(String token, Long productId) {
        log.info("============================调用addToFavorites方法========================");
        log.info("addToFavorites: token={}, productId={}", token, productId);

        Long userId =jwtUtil.extractUserId(token);
        log.info("addToFavorites: userId={}", userId);
        if (userId == null || productId==null) {
            return R.error("添加收藏失败,系统错误");
        }
        LambdaQueryWrapper<Favorites> favoritesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        favoritesLambdaQueryWrapper.eq(Favorites::getUserId, userId)
                .eq(Favorites::getProductId, productId);
        if (this.getOne(favoritesLambdaQueryWrapper) != null) {
            return R.error("添加收藏失败，该商品已在收藏列表中");
        }
        Favorites favorites = new Favorites();
        favorites.setProductId(productId);
        favorites.setUserId(userId);
        favorites.setCreatedAt(LocalDateTime.now());
        this.save(favorites);
        return R.success("添加收藏成功");
    }

    //移除收藏
    @Override
    public R<String> removeFavorites(String token, Long productId) {
        log.info("=================================调用removeFavorites方法=======================");
        log.info("removeFavorites: token={}, productId={}", token, productId);

        if( token == null || productId == null) {
            return R.error("移除收藏失败,系统错误");
        }
        Long userId =jwtUtil.extractUserId(token);
        log.info("removeFavorites: userId={}", userId);
        LambdaQueryWrapper<Favorites> favoritesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        favoritesLambdaQueryWrapper.eq(Favorites::getUserId, userId);
        favoritesLambdaQueryWrapper.eq(Favorites::getProductId, productId);

        Favorites favorites = this.getOne(favoritesLambdaQueryWrapper);
        if (favorites == null) {
            return R.error("移除收藏失败，该商品不在收藏列表中");
        }
        this.removeById(favorites);
        return R.success("移除收藏成功");
    }

    //获取收藏列表
    @Override
    public R<List<ProductDTO>> getFavorites(String token) {
        log.info("=====================================调用getFavorites方法===========================");
        log.info("getFavorites: token={}", token);

        if (token == null) {
            return R.error("获取收藏列表失败, 系统错误");
        }

        Long userId = jwtUtil.extractUserId(token);
        log.info("getFavorites: userId={}", userId);

        if (userId == null) {
            return R.error("用户未登录");
        }

        // 获取用户的收藏列表
        LambdaQueryWrapper<Favorites> favoritesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        favoritesLambdaQueryWrapper.eq(Favorites::getUserId, userId);
        List<Favorites> favoritesList = this.list(favoritesLambdaQueryWrapper);

        if (favoritesList.isEmpty()) {
            return R.success(Collections.emptyList());
        }

        // 提取所有 productId
        List<Long> productIds = favoritesList.stream()
                .map(Favorites::getProductId)
                .collect(Collectors.toList());

        // 批量查询所有收藏的产品
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.in(Products::getProductId, productIds);
        List<Products> productsList = productService.list(productsLambdaQueryWrapper);

        // 提取所有 merchantId
        List<Long> merchantIds = productsList.stream()
                .map(Products::getMerchantId)
                .collect(Collectors.toList());

        // 批量查询所有相关商家信息
        Map<Long, Merchants> merchantsMap = merchantService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchants::getMerchantId, merchant -> merchant));

        // 构建返回的 ProductDTO 列表
        List<ProductDTO> productDTOList = productsList.stream().map(product -> {
            ProductDTO productDTO = new ProductDTO(product);
            Merchants merchants = merchantsMap.get(product.getMerchantId());
            if (merchants != null) {
                productDTO.setAddress(merchants.getAddress());
                productDTO.setShopName(merchants.getShopName());
                productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
            }
            return productDTO;
        }).collect(Collectors.toList());

        return R.success(productDTOList);
    }
}
