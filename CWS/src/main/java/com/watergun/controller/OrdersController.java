package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.dto.requestDTO.CreateOrderRequest;
import com.watergun.dto.requestDTO.PayOrderRequest;
import com.watergun.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrdersController {


    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    //创建订单    用户直接购买产品  购物车购买产品 都先创建订单
    @PostMapping("/createOrders")
    public R<List<Long>> createOrder(HttpServletRequest request, @RequestBody CreateOrderRequest createOrderRequest){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.createOrder(token,createOrderRequest.getProductIds(),createOrderRequest.getQuantities(),createOrderRequest.getAddressId());
    }

    //用户支付订单
    @PutMapping("/payOrders")
    public R<String> payOrders(HttpServletRequest request, @RequestBody PayOrderRequest payOrderRequest){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.payOrders(token,payOrderRequest.getOrderIds(),payOrderRequest.getPaymentMethod());
    }

    //用户取消订单

    //用户确认收货

    //查看历史订单

    //查看订单详情

    //取消订单

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
