package com.watergun.dto;

import com.watergun.entity.Merchants;
import com.watergun.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


//用来显示店铺的页面信息
//多余的
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO extends Merchants {
    private List<Products> productsList;

    public ShopDTO(Merchants merchants){
        super(merchants.getMerchantId(),merchants.getWalletBalance(),merchants.getPendingBalance(),
                merchants.getShopName(),merchants.getAddress(),merchants.getCountry(),merchants.getTaxId(),
                merchants.getPaymentInfo(),merchants.getShopAvatarUrl(),merchants.getCreatedAt(),merchants.getUpdatedAt());
    }

}
