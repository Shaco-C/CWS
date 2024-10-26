package com.watergun.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {


    private List<Long> productIds;

    private Map<Long, Integer> quantities;

    private Long addressId;

}
