package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {
    void firstCreateCart(Long userId);

    void removeCartByUserId(Long userId);

    R<List<ProductDTO>> getCartList(String token);

}
