package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Favorites;

import java.util.List;

public interface FavoritesService extends IService<Favorites> {
    R<String> addToFavorites(String token,Long productId);

    R<String> removeFavorites(String token,Long productId);

    R<List<ProductDTO>> getFavorites(String token);
}
