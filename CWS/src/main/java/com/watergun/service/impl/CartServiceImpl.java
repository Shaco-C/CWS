package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.entity.Cart;
import com.watergun.mapper.CartMapper;
import com.watergun.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

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
}
