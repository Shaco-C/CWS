package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Categories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface CategoryService extends IService<Categories> {
    R<Map<String, List<Categories>>> getCategorySortedList();

    R<String> addCategories(String token, Categories categories);

    R<String> updateCategories(String token, Categories categories);

    R<String> deleteCategories(String token,Long categoryId);

    //----------管理员方法---------
    //管理员分页查询所有分类标签
    R<Page> adminGetCategoriesPage(int page, int pageSize, Long parentId);

}
