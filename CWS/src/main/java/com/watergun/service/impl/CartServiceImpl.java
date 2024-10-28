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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final JwtUtil jwtUtil;
    private final CartItemService cartItemService;
    private final ProductService productService;
    private final MerchantService merchantService;

    public CartServiceImpl(JwtUtil jwtUtil, CartItemService cartItemService, ProductService productService, MerchantService merchantService) {
        this.jwtUtil = jwtUtil;
        this.cartItemService = cartItemService;
        this.productService = productService;
        this.merchantService = merchantService;
    }

    @Override
    public R<String> firstCreateCart(Long userId) {
        log.info("====================firstCreateCart====================");
        log.info("firstCreateCart: userId = {}", userId);
        Cart cart = new Cart();
        if (userId==null){
            log.warn("userId is null");
            throw new CustomException("userId is null");
        }
        cart.setUserId(userId);
        boolean result= this.save(cart);
        if (!result){
            return R.error("create cart failed");
        }
        return R.success("create cart success");
    }

    @Override
    public R<String> removeCartByUserId(Long userId) {
        log.info("====================removeCartByUserId====================");
        log.info("removeCartByuserId: userId = {}", userId);
        if (userId==null){
            log.warn("userId is null");
            throw new CustomException("userId is null");
        }
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        boolean result = this.remove(cartLambdaQueryWrapper);
        if (!result){
            return R.error("remove cart failed");
        }
        return R.success("remove cart success");
    }

    @Override
    public R<List<ProductDTO>> getCartList(String token) {
        log.info("====================getCartList====================");
        if (token == null) {
            log.warn("token is null");
            return R.error("token is null");
        }
        log.info("getCartList: token = {}", token);

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.warn("userId is null");
            return R.error("userId is null");
        }

        // 获取用户购物车信息
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        Cart cart = this.getOne(cartLambdaQueryWrapper);

        if (cart == null) {
            return R.error("购物车不存在");
        }

        // 获取购物车中的商品信息
        LambdaQueryWrapper<CartItems> cartItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartItemsLambdaQueryWrapper.eq(CartItems::getCartId, cart.getCartId());
        List<CartItems> cartItems = cartItemService.list(cartItemsLambdaQueryWrapper);

        if (cartItems.isEmpty()) {
            return R.success(Collections.emptyList());
        }

        // 提取所有 productId 和数量映射
        Map<Long, Integer> productQuantityMap = cartItems.stream()
                .collect(Collectors.toMap(CartItems::getProductId, CartItems::getQuantity));

        // 获取所有商品信息
        List<Long> productIds = new ArrayList<>(productQuantityMap.keySet());
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.in(Products::getProductId, productIds);
        List<Products> productsList = productService.list(productsLambdaQueryWrapper);

        // 获取所有商家信息
        List<Long> merchantIds = productsList.stream()
                .map(Products::getMerchantId)
                .toList();

        // 批量查询所有相关商家信息
        Map<Long, Merchants> merchantsMap = merchantService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchants::getMerchantId, merchants -> merchants));

        // 检查商品是否存在并且是否有效
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (CartItems cartItem : cartItems) {
            Products product = productsList.stream()
                    .filter(p -> p.getProductId().equals(cartItem.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (product == null || !product.getIsActive()) {
                // 商品已下架或删除，添加相应提示
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(cartItem.getProductId());
                productDTO.setMessage("商品已下架或已删除");
                productDTOList.add(productDTO);
            } else {
                // 正常商品
                ProductDTO productDTO = new ProductDTO(product);
                productDTO.setQuantity(productQuantityMap.get(product.getProductId()));

                // 设置商家信息
                Merchants merchants = merchantsMap.get(product.getMerchantId());
                if (merchants != null) {
                    productDTO.setAddress(merchants.getAddress());
                    productDTO.setShopName(merchants.getShopName());
                    productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
                }
                productDTOList.add(productDTO);
            }
        }

        return R.success(productDTOList);
    }


    //将商品添加到购物车中
    @Transactional
    @Override
    public R<String> addProductToCartItem(String token, CartItems cartItems) {
        log.info("========================addProductToCartItem========================");
        log.info("addProductToCartItem:{}", cartItems);
        log.info("token:{}", token);

        if (cartItems == null || token == null) {
            log.error("addProductToCartItem:参数为空");
            return R.error("参数为空");
        }
        if (cartItems.getProductId() == null) {
            log.error("addProductToCartItem: Product ID is null");
            return R.error("Product ID is required");
        }
        if (cartItems.getQuantity() == null) {
            log.error("addProductToCartItem: Quantity is null");
            return R.error("Quantity is required");
        }

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.error("addProductToCartItem:用户不存在");
            return R.error("用户不存在");
        }

        // 检查商品是否下架
        Products product = productService.getById(cartItems.getProductId());
        if (product == null || !product.getIsActive()) {
            return R.error("商品已下架或不存在，无法添加到购物车");
        }

        // 获取用户的购物车
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        Cart cart = this.getOne(cartLambdaQueryWrapper);

        if (cart == null) {
            log.error("addProductToCartItem:购物车不存在");
            return R.error("购物车不存在");
        }

        Long cartId = cart.getCartId();

        // 检查是否已存在相同的商品在购物车中
        LambdaQueryWrapper<CartItems> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItems::getCartId, cartId).eq(CartItems::getProductId, cartItems.getProductId());
        CartItems existingCartItem = cartItemService.getOne(queryWrapper);

        if (existingCartItem != null) {
            // 商品已存在，更新数量
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItems.getQuantity());
            cartItemService.updateById(existingCartItem);
            return R.success("商品数量已更新");
        }

        cartItems.setCartId(cartId);
        cartItemService.save(cartItems);

        return R.success("添加成功");
    }


    //将商品从购物车中去除
    @Override
    public R<String> deleteProductFromCartItem(String token, Long cartItemId) {
        log.info("========================deleteProductFromCartItem========================");
        log.info("deleteProductFromCartItem:{}", cartItemId);
        log.info("token:{}", token);

        if (cartItemId == null || token == null) {
            log.error("deleteProductFromCartItem:参数为空");
            return R.error("参数为空");
        }

        // 通过token获取用户ID
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.error("deleteProductFromCartItem:用户不存在");
            return R.error("用户不存在");
        }

        // 验证购物车详情项是否存在，并且属于该用户的购物车
        CartItems cartItem = cartItemService.getById(cartItemId);
        if (cartItem == null || !cartItem.getCartId().equals(userId)) {
            log.error("deleteProductFromCartItem:购物车详情项不存在或不属于当前用户");
            return R.error("购物车详情项不存在或不属于当前用户");
        }

        // 执行删除操作
        boolean removed = cartItemService.removeById(cartItemId);
        if (!removed) {
            log.error("deleteProductFromCartItem:删除失败");
            return R.error("删除失败");
        }

        return R.success("删除成功");
    }





}
