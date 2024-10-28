package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Products;
import com.watergun.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductsController {


    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    //商家创建产品
    @PostMapping
    public R<String> createProduct(HttpServletRequest request, @RequestBody Products products) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.createProduct(token,products);
    }

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Long categoryId  ,String sortField, String sortOrder, String searchBox){
        return productService.page(page,pageSize,categoryId,sortField,sortOrder,searchBox);
    }

    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    //需要前端调取3个axios请求------替代掉原有的ProductDTO
    //1.获取产品详情
    //2.获取商家信息
    //3.获取产品评论
    @GetMapping("/{productId}")
    public R<Products> getProductDetiails(@PathVariable Long productId){
        log.info("正在看产品详情，id为{}",productId);
        return productService.getProductDetiails(productId);
    }

    //商家删除产品
    @DeleteMapping("/{productId}")
    public R<String> deleteProduct(HttpServletRequest request,@PathVariable Long productId){
        log.info("正在删除产品，id为{}",productId);
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.deleteProduct(token,productId);
    }


    //商家修改产品信息
    @PutMapping("/updateProduct")
    public R<String> updateProduct(HttpServletRequest request,@RequestBody Products products){
        log.info("正在修改产品{}",products.toString());
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.updateProduct(token,products);
    }

    //商家上/下架产品 点击一下下架按钮
    @PutMapping("/setActive")
    public R<String> setActiveOrInActiveProduct(HttpServletRequest request,@RequestParam Long productId,@RequestParam Boolean active){
        log.info("正在修改产品{}的上架状态",productId);
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.setActiveOrInActiveProduct(token,productId,active);
    }

    //商家查看自己的产品
    @GetMapping("/myProducts")
    public R<Page> getMyProducts(HttpServletRequest request,int page,int pageSize,String sortField, String sortOrder){
        log.info("正在查看自己的产品");

        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.getMyProducts(page,pageSize,token,sortField,sortOrder);
    }

    //----------------管理员方法------------------------
    // 管理员分页查询待审核产品
    @GetMapping("/admin/getProductsPage")
    public R<Page> adminGetProductsPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "pageSize", defaultValue = "1")int pageSize,
                                String status) {
        return productService.adminGetProductsPage(page,pageSize,status);
    }

    //管理员审核商品是否通过审核
    @PutMapping("/admin/productStatus/{productId}")
    public R<String> adminApproveProduct(@PathVariable Long productId,@RequestParam String status,HttpServletRequest request){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return productService.adminApproveProduct(productId,status,token);
    }


}
