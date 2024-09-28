package com.watergun.dto;

import com.watergun.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO  extends Products {
    private String shopName;
    private String address;
    private String shopAvatarUrl;

}
