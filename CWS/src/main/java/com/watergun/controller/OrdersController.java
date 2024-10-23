package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrdersController {


    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }
    //用户直接购买产品

    //取消订单

    //查看订单详情

    //用户申请退货

    //--------------商家方法---------------
    //商家查看自己订单
    @GetMapping("/merchants/getOrders")
    public R<Page> merchantsGetOrders(HttpServletRequest request,
                                      @RequestParam(value = "page", defaultValue = "1")int page,
                                      @RequestParam(value = "pageSize", defaultValue = "1")int pageSize,
                                      String status, String returnStatus){
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return orderService.merchantsGetOrders(page,pageSize,token,status,returnStatus);
    }
}
