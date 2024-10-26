package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.Orders;

import java.util.List;
import java.util.Map;


public interface OrderService extends IService<Orders> {
    //------------methods----------
    List<Orders> getOrdersByIdsAndUserId(List<Long> orderIds,Long userId);


    //------------serviceLogic---------
    R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus);

    //创建订单
    R<List<Long>> createOrder(String token, List<Long>  productIds, Map<Long, Integer>  quantities,Long addressId);

    //用户支付订单
    R<String> payOrders(String token,List<Long> orderIds, String paymentMethod);
}
