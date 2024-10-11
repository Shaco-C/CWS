package com.watergun.controller;

import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.entity.Categories;
import com.watergun.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//管理员管理所有分类
@RestController
@RequestMapping("/categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    //获取所有分类,按照parentId进行分类
    @GetMapping("/list")
    public R<Map<String, List<Categories>>> list() {

        return categoryService.getCategorySortedList();
    }

    //添加分类
    @PostMapping("/add")
    public R<String> addCategories(HttpServletRequest request, @RequestBody Categories categories) {
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return categoryService.addCategories(token,categories);
    }
    //修改分类
    @PutMapping
    public R<String> updateCategories(HttpServletRequest request, @RequestBody Categories categories) {
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return categoryService.updateCategories(token,categories);
    }
    //删除分类
    @DeleteMapping("/{categoryId}")
    public R<String> deleteCategories(HttpServletRequest request, @PathVariable("categoryId") Long categoryId) {
        String token = request.getHeader("Authorization").replace("Bearer ", ""); //获取token
        return categoryService.deleteCategories(token,categoryId);
    }
}
