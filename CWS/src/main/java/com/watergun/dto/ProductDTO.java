package com.watergun.dto;

import com.watergun.entity.Products;
import com.watergun.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO  extends Products {
    private String shopName;
    private String address;
    private String shopAvatarUrl;
    private List<ReviewDTO> reviewsList;

    public ProductDTO(Products products) {
        super(products.getProductId(), products.getMerchantId(), products.getName(), products.getDescription(), products.getPrice(), products.getStock(), products.getCategoryId(), products.getImageUrl(), products.getSales(), products.getStatus(),products.getCreatedAt() , products.getUpdatedAt());
    }
}
