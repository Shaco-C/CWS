package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.Cart;

public interface CartService extends IService<Cart> {
    void firstCreateCart(Long userId);

    void removeCartByUserId(Long userId);

}
