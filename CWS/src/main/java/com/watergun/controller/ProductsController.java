package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.dto.ProductDTO;
import com.watergun.entity.Products;
import com.watergun.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductService productService;


    //创建产品
    @PostMapping
    public R<String> createProduct(HttpServletRequest request, @RequestBody Products products) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.createProduct(token,products);
    }

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Integer categoryId  ,String sortField, String sortOrder, String searchBox){
        return productService.page(page,pageSize,categoryId,sortField,sortOrder,searchBox);
    }

    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    @GetMapping("/{id}")
    public R<ProductDTO> getProductDetiails(@PathVariable Long id){
        log.info("正在看产品详情，id为{}",id);
        return productService.getProductDetiails(id);
    }
}
