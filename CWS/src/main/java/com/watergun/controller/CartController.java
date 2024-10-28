package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.CartItems;
import com.watergun.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {


    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    //为用户创建购物车
    @PostMapping("/createCart/{userId}")
    public R<String> createCart( @PathVariable("userId") Long userId) {
        return cartService.firstCreateCart(userId);
    }
    //用户注销时删除购物车
    @DeleteMapping("/deleteCart/{userId}")
    public R<String> deleteCart(@PathVariable("userId") Long userId) {
        return cartService.removeCartByUserId(userId);
    }

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

    //用户购买购物车中的商品，一次性全买下

}
