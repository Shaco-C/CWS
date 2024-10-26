package com.watergun.dto.requestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderRequest {

    private List<Long> orderIds;

    private String paymentMethod;
}
