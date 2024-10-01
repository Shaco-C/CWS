package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.service.FavoritesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

    //商品添加到收藏
    @PostMapping("/{productId}")
    public R<String> addToFavorites(HttpServletRequest request,@PathVariable Long productId) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return favoritesService.addToFavorites(token,productId);
    }

    //商品从收藏中移除
    @DeleteMapping("/{productId}")
    public R<String> removeFavorites(HttpServletRequest request,@PathVariable Long productId) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return favoritesService.removeFavorites(token,productId);
    }

    //获取用户收藏列表
    @GetMapping
    public R<List<ProductDTO>> getFavorites(HttpServletRequest request) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return favoritesService.getFavorites(token);
    }

}
