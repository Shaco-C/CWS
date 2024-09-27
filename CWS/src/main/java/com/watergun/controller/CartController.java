package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.Cart;
import com.watergun.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

//    @GetMapping("/{userId}")
//    public R<Cart> getCartByUserId(@PathVariable Integer userId) {
//        return R.success(cartService.getCartByUserId(userId));
//    }

    @PostMapping
    public R<String> createCart(@RequestBody Cart cart) {

        return R.success("success");
    }

    @DeleteMapping("/{userId}")
    public R<String> deleteCart(@PathVariable Integer userId) {

        return R.success("success");
    }
}
