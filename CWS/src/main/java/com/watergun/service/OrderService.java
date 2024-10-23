package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.Orders;


public interface OrderService extends IService<Orders> {
    R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus);

}
