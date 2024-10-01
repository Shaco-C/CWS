package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Cart;
import com.watergun.entity.CartItems;
import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import com.watergun.mapper.CartMapper;
import com.watergun.service.CartItemService;
import com.watergun.service.CartService;
import com.watergun.service.MerchantService;
import com.watergun.service.ProductService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    @Lazy
    private ProductService productService;

    @Autowired
    private MerchantService merchantService;

    @Override
    public void firstCreateCart(Long userId) {
        log.info("firstCreateCart: userId = {}", userId);
        Cart cart = new Cart();
        if (userId==null){
            log.warn("userId is null");
            throw new CustomException("userId is null");
        }
        cart.setUserId(userId);
        this.save(cart);
    }

    @Override
    public void removeCartByUserId(Long userId) {
        log.info("removeCartByuserId: userId = {}", userId);
        if (userId==null){
            log.warn("userId is null");
            throw new CustomException("userId is null");
        }
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        this.remove(cartLambdaQueryWrapper);
    }

    @Override
    public R<List<ProductDTO>> getCartList(String token) {
        if( token==null){
            log.warn("token is null");
            return R.error("token is null");
        }
        log.info("getCartList: token = {}", token);
        Long userId = jwtUtil.extractUserId(token);
        if (userId==null){
            log.warn("userId is null");
            return R.error("userId is null");
        }

        //获取用户购物车信息
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper= new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        Cart cart = this.getOne(cartLambdaQueryWrapper);

        //获取购物车中商品信息
        LambdaQueryWrapper<CartItems> cartItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartItemsLambdaQueryWrapper.eq(CartItems::getCartId,cart.getCartId());
        List<CartItems> cartItems = cartItemService.list(cartItemsLambdaQueryWrapper);

        if (cartItems.isEmpty()){
            return R.success(Collections.emptyList());
        }

        //提取所有productId
        List<Long> productIds = cartItems.stream()
                .map(CartItems::getProductId)
                .toList();


        //获取所有商品信息
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.in(Products::getProductId, productIds);
        List<Products> productsList = productService.list(productsLambdaQueryWrapper);

        //获取所有商家信息
        List<Long> merchantIds = productsList.stream()
                .map(Products::getMerchantId)
                .toList();

        //批量查询所有相关商家信息.
        Map<Long, Merchants> merchantsMap = merchantService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchants::getMerchantId, merchants -> merchants));

        //将商品信息与商家信息组合成ProductDTO表
        List<ProductDTO> productDTOList = productsList.stream().map(products -> {
            ProductDTO productDTO = new ProductDTO(products);
            Merchants merchants = merchantsMap.get(products.getMerchantId());
            if (merchants != null) {
                productDTO.setAddress(merchants.getAddress());
                productDTO.setShopName(merchants.getShopName());
                productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
            }
            return productDTO;
        }).toList();
        return R.success(productDTOList);
    }

}
