package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.Categories;
import com.watergun.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> createCategory(@RequestBody Categories category) {

        return R.success("Category created successfully");
    }

    @PutMapping("/{id}")
    public R<String> updateCategory(@PathVariable Integer id, @RequestBody Categories category) {

        return R.success("Category updated successfully");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteCategory(@PathVariable Integer id) {

        return R.success("Category deleted successfully");
    }
}
