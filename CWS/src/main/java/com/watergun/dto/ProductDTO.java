package com.watergun.dto;

import com.watergun.entity.Products;
import com.watergun.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO  extends Products {
    private String shopName;
    private String address;
    private String shopAvatarUrl;
    private List<ReviewDTO> reviewsList;

}
