package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Cart;
import com.watergun.entity.CartItems;
import com.watergun.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public R<List<ProductDTO>> getCart(HttpServletRequest request) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return cartService.getCartList(token);

    }

    @PostMapping
    public R<String> addProductToCartItem(HttpServletRequest request, @RequestBody CartItems cartItems){
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return cartService.addProductToCartItem(token, cartItems);
    }


    @DeleteMapping("/{cartItemId}")
    public R<String> deleteProductFromCartItem(HttpServletRequest request, @PathVariable Long cartItemId){
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return cartService.deleteProductFromCartItem(token, cartItemId);
    }
}
