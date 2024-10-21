package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.Orders;
import com.watergun.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;



    //用户直接购买产品

    //取消订单

    //查看订单详情

    //用户申请退货

    //--------------商家方法---------------
    //商家查看自己订单

}
