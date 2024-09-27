package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.Favorites;
import com.watergun.service.FavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

//    @GetMapping("/{userId}")
//    public R<List<Favorites>>  getFavoritesByUserId(@PathVariable Integer userId) {
//        return R.success(favoritesService.getFavoritesByUserId(userId));
//    }

    @PostMapping
    public R<String> addFavorite(@RequestBody Favorites favorite) {

        return R.success("success");
    }

    @DeleteMapping("/{userId}/{productId}")
    public R<String> removeFavorite(@PathVariable Integer userId, @PathVariable Integer productId) {

        return R.success("success");
    }
}
