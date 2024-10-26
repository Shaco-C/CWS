package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.Products;

import java.util.List;


public interface ProductService extends IService<Products> {

    //通过Id批量获取产品
    List<Products> getProductsByIds(List<Long> productIds);

    //创建产品
    R<String> createProduct(String token, Products products);

    //分页查询产品，categoryId为产品种类，sortField为排序字段，sortOrder为排序顺序.也可以对在搜索框中进行模糊搜索
    R<Page> page(int page, int pageSize, Long categoryId  , String sortField, String sortOrder, String searchBox);

    //当用户点击产品时,显示产品的详细信息页面，包括产品图片、产品名称、产品描述、产品价格、产品库存、产品评论等
    R<Products> getProductDetiails(Long id);

    //商家删除产品
    R<String> deleteProduct(String token,Long productId);

    //商家修改产品信息
    R<String> updateProduct(String token,Products products);

    //商家上/下架产品 点击一下下架按钮
    R<String> setActiveOrInActiveProduct(String token,Long productId,Boolean active);

    //商家查看自己的产品信息
    R<Page> getMyProducts(int page,int pageSize,String token,String sortField, String sortOrder);

    //-----------管理员方法----------------
    // 管理员分页查询待审核产品
    R<Page> adminGetProductsPage(int page,int pageSize, String status);

    //管理员审核商品是否通过审核
    R<String> adminApproveProduct(Long productId, String status, String token);
}
