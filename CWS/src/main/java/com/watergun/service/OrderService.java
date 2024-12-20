package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.OrdersDTO;
import com.watergun.dto.requestDTO.RefundRequest;
import com.watergun.entity.Orders;

import java.util.List;
import java.util.Map;


public interface OrderService extends IService<Orders> {
    //------------methods----------
    List<Orders> getOrdersByIdsAndUserId(List<Long> orderIds,Long userId);
    boolean isOrderBelongsToMerchant(Orders order, Long merchantId);


    //------------serviceLogic---------


    //用户查看购买的订单
    R<Page> getHistoryOrders(int page, int pageSize, String token, String status, String returnStatus);

    //查看订单详情
    R<OrdersDTO> getOrderDetail(String token, Long orderId);

    //创建订单
    R<List<Long>> createOrder(String token, List<Long>  productIds, Map<Long, Integer>  quantities,Long addressId);

    //用户支付订单
    R<String> payOrders(String token,List<Long> orderIds, String paymentMethod);

    //用户取消订单
    R<String> cancelOrder(String token,Long orderId);

    //用户收货方法
    R<String> receivedProduct(String token,Long orderId);

    //用户申请退货
    R<String> userReturnProductApplication(String token, RefundRequest returnRequest);

    //用户进行退货
    R<String> userRefundProducts(String token,Long orderId);

    //----------

    //模拟快递员送货方法
    R<String> transitProduct(Long orderId);

    //模拟快递送达目的地方法
    R<String> deliveredProduct(Long orderId);

    //----------

    //商家查看被售卖的订单
    R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus);

    //商家发货
    R<String> merchantsShipppedProduct(String token,Long orderId);

    //商家处理退货申请
    R<String> merchantsHandleReturnRequest(String token,Long orderId,String status);
}
