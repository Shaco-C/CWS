package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Cart;
import com.watergun.entity.CartItems;

import java.util.List;

public interface CartService extends IService<Cart> {
    R<String> firstCreateCart(Long userId);

    R<String> removeCartByUserId(Long userId);

    R<List<ProductDTO>> getCartList(String token);

    R<String> addProductToCartItem(String token, CartItems cartItems);

    R<String> deleteProductFromCartItem(String token,Long cartItemId);

}
