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


}
