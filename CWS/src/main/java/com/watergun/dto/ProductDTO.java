package com.watergun.dto;

import com.watergun.entity.Products;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO  extends Products {
    private Integer quantity;
    private String shopName;
    private String address;
    private String shopAvatarUrl;
    private List<ReviewDTO> reviewsList;
    private String message;

    public ProductDTO(Products products) {
        super(products.getProductId(), true,products.getMerchantId(), products.getName(), products.getDescription(), products.getPrice(), products.getStock(), products.getCategoryId(), products.getImageUrl(), products.getSales(), products.getStatus(),products.getCreatedAt() , products.getUpdatedAt());
    }
}
