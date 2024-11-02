package com.watergun.dto;

import com.watergun.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersDTO {
    private Orders orders;
    private List<ProductDTO> productDTOList;

    public OrdersDTO(Orders orders) {
        this.orders = orders;
    }

}
