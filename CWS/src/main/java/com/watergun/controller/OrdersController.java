package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.dto.OrdersDTO;
import com.watergun.dto.requestDTO.CreateOrderRequest;
import com.watergun.dto.requestDTO.PayOrderRequest;
import com.watergun.dto.requestDTO.RefundRequest;
import com.watergun.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    //模拟快递员送货方法
    //后续再继续实现，现在只是为了实现订单状态改变的临时方法
    @PutMapping("/transitProduct")
    public R<String> transitProduct(@RequestParam Long orderId){
        return orderService.transitProduct(orderId);
    }

    //模拟快递送达目的地方法
    //后续再继续实现，现在只是为了实现订单状态改变的临时方法
    @PutMapping("/deliveredProduct")
    public R<String> deliveredProduct(@RequestParam Long orderId){
        return orderService.deliveredProduct(orderId);
    }

    //用户确认收货
    @PutMapping("/receivedProduct")
    public R<String> receivedProduct(HttpServletRequest request,@RequestParam Long orderId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.receivedProduct(token,orderId);
    }
    //用户取消订单
    @PutMapping("/cancelOrder")
    public R<String> cancelOrder(HttpServletRequest request,@RequestParam Long orderId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.cancelOrder(token,orderId);

    }
    //查看历史订单
    @GetMapping("/getHistoryOrders")
    public R<Page> getHistoryOrders(HttpServletRequest request,
                                    @RequestParam(value = "page",defaultValue = "1") int page,
                                    @RequestParam(value = "pageSize",defaultValue = "1") int pageSize,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String returnStatus){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.getHistoryOrders(page,pageSize,token,status,returnStatus);
    }

    //查看订单详情
    @GetMapping("/getOrderDetail")
    public R<OrdersDTO> getOrderDetail(HttpServletRequest request, @RequestParam Long orderId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.getOrderDetail(token,orderId);
    }

    //用户申请退货
    @PutMapping("/refundApplication")
    public R<String> userReturnProduct(HttpServletRequest request, @RequestBody RefundRequest refundRequest){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.userReturnProductApplication(token,refundRequest);
    }
    //用户进行退货（在退货请求审核通过后，对应订单return_status状态为APPROVED）
    @PutMapping("/refundProduct")
    public R<String> userRefundProducts(HttpServletRequest request,@RequestParam Long orderId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.userRefundProducts(token,orderId);
    }


    //--------------商家方法---------------
    //商家查看自己订单 （待发货、已发货、已完成）(退货请求)
    //商家查看待处理退货请求
    @GetMapping("/merchants/getOrders")
    public R<Page> merchantsGetOrders(HttpServletRequest request,
                                      @RequestParam(value = "page", defaultValue = "1")int page,
                                      @RequestParam(value = "pageSize", defaultValue = "1")int pageSize,
                                      @RequestParam(required = false)String status,
                                      @RequestParam(required = false)String returnStatus){
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return orderService.merchantsGetOrders(page,pageSize,token,status,returnStatus);
    }
    //商家发货
    @PutMapping("/merchants/shippped")
    public R<String> merchantsShipppedProduct(HttpServletRequest request,@RequestParam Long orderId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.merchantsShipppedProduct(token,orderId);
    }
    //商家审核退货请求
    @PutMapping("/merchants/handleRequest")
    public R<String> merchantsHandleReturnRequest(HttpServletRequest request,
                                                  @RequestParam Long orderId,
                                                  @RequestParam String status){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return orderService.merchantsHandleReturnRequest(token,orderId,status);
    }

}
