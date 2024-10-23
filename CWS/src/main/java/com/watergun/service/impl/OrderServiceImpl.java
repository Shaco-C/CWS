package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.OrderDTO;
import com.watergun.entity.OrderItems;
import com.watergun.entity.Orders;
import com.watergun.mapper.OrdersMapper;
import com.watergun.service.OrderItemsService;
import com.watergun.service.OrderService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final JwtUtil jwtUtil;
    private final OrderItemsService orderItemsService;

    public OrderServiceImpl(JwtUtil jwtUtil, OrderItemsService orderItemsService) {
        this.jwtUtil = jwtUtil;
        this.orderItemsService = orderItemsService;
    }

    @Override
    public R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus) {
        log.info("getOrders 被调用");
        log.info("参数: page={}, pageSize={}, token={}, status={}, returnStatus={}", page, pageSize, token, status, returnStatus);

        // 校验token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token已过期: {}", token);
            return R.error("token 已过期");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 确认用户角色是否为商家
        log.info("商家ID: {}, 用户角色: {}", merchantId, userRole);
        if (!"merchant".equals(userRole)) {
            log.warn("非法调用 - 非商家用户尝试调用接口");
            throw new CustomException("非法调用");
        }

        // 构建分页对象和查询条件
        Page<Orders> orderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getMerchantId, merchantId)
                .eq(StringUtils.isNotEmpty(status), Orders::getStatus, status)
                .eq(StringUtils.isNotEmpty(returnStatus), Orders::getReturnStatus, returnStatus);

        // 分页查询商家的订单数据
        Page<Orders> paginatedOrders = this.page(orderPage, ordersLambdaQueryWrapper);

        // 获取所有订单ID并进行查询
        List<Long> orderIdList = paginatedOrders.getRecords().stream()
                .map(Orders::getOrderId)
                .distinct()
                .toList();

        // 检查是否有订单ID，避免空列表查询错误
        if (orderIdList.isEmpty()) {
            log.info("没有符合条件的订单");
            // 返回空的分页数据
            Page<OrderDTO> emptyPage = new Page<>(page, pageSize);
            return R.success(emptyPage);
        }

        // 查询订单详情
        LambdaQueryWrapper<OrderItems> orderItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderItemsLambdaQueryWrapper.in(OrderItems::getOrderId, orderIdList);
        List<OrderItems> orderItemsList = orderItemsService.list(orderItemsLambdaQueryWrapper);

        // 将同一个 orderId 的 OrderItems 分组放入 map
        Map<Long, List<OrderItems>> orderItemsMap = orderItemsList.stream()
                .collect(Collectors.groupingBy(OrderItems::getOrderId));

        // 转换为 OrderDTO 并关联订单详情
        List<OrderDTO> orderDTOList = paginatedOrders.getRecords().stream()
                .map(order -> {
                    OrderDTO orderDTO = new OrderDTO(order);
                    orderDTO.setOrderItemsList(orderItemsMap.get(order.getOrderId()));
                    return orderDTO;
                }).toList();

        // 创建新的 Page 对象用于返回
        Page<OrderDTO> dtoPage = new Page<>(page, pageSize);
        dtoPage.setRecords(orderDTOList);
        dtoPage.setTotal(paginatedOrders.getTotal()); // 保留分页查询总数
        dtoPage.setCurrent(paginatedOrders.getCurrent());
        dtoPage.setSize(paginatedOrders.getSize());

        return R.success(dtoPage);
    }

}
