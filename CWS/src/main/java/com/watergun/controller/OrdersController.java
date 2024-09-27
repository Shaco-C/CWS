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

//    @GetMapping
//    public R<List<Orders> >getAllOrders() {
//        return R.success(orderService.getAllOrders()) ;
//    }
//
//    @GetMapping("/{id}")
//    public R<Orders> getOrderById(@PathVariable Integer id) {
//        return R.success(orderService.getOrderById(id));
//    }

    @PostMapping
    public R<String> createOrder(@RequestBody Orders order) {

        return R.success("Order created successfully");
    }

    @PutMapping("/{id}")
    public R<String> updateOrder(@PathVariable Integer id, @RequestBody Orders order) {

        return R.success("Order updated successfully");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteOrder(@PathVariable Integer id) {

        return R.success("Order deleted successfully");
    }
}
