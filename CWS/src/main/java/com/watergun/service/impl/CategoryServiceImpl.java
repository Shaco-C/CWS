package com.watergun.service.impl;

import com.watergun.entity.Categories;
import com.watergun.mapper.CategoriesMapper;
import com.watergun.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoriesMapper, Categories> implements CategoryService {

}
