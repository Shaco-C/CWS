package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.OrderDTO;
import com.watergun.entity.*;
import com.watergun.enums.*;
import com.watergun.mapper.OrdersMapper;
import com.watergun.service.*;
import com.watergun.utils.JwtUtil;
import com.watergun.utils.PayLogicUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final JwtUtil jwtUtil;
    private final OrderItemsService orderItemsService;

    private final ProductService productService;

    private final MerchantService merchantService;

    private final UserAddressService userAddressService;

    private final PayLogicUtil payLogicUtil;

    public OrderServiceImpl(JwtUtil jwtUtil, OrderItemsService orderItemsService, ProductService productService,
                            MerchantService merchantService, UserAddressService userAddressService,
                            PayLogicUtil payLogicUtil) {
        this.jwtUtil = jwtUtil;
        this.orderItemsService = orderItemsService;
        this.productService = productService;
        this.merchantService = merchantService;
        this.userAddressService = userAddressService;
        this.payLogicUtil = payLogicUtil;
    }

    //-------------method--------------
    @Override
    public List<Orders> getOrdersByIdsAndUserId(List<Long> orderIds, Long userId) {
        log.info("========================getOrdersByIdsAndUserId 被调用====================");
        // 检查传入的订单 ID 列表是否为空
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("订单 ID 列表不为空");

        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Orders::getOrderId, orderIds)
                .eq(Orders::getUserId, userId);

        // 返回符合条件的订单列表
        log.info("返回符合条件的订单列表");
        return this.list(queryWrapper);
    }

    @Override
    public boolean isOrderBelongsToMerchant(Orders order, Long merchantId) {
        log.info("========================isOrderBelongsToMerchant 被调用====================");
        return order.getMerchantId().equals(merchantId);
    }



    //-------------serviceLogic-----------
    @Override
    public R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus) {
        log.info("========================merchantsGetOrders 被调用====================");
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
        if (!UserRoles.MERCHANT.name().equals(userRole)) {
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

    // 创建订单  用户下单
    @Override
    @Transactional
    public R<List<Long>> createOrder(String token, List<Long> productIds, Map<Long, Integer> quantities, Long addressId) {
        log.info("=========================调用 createOrder方法=========================");
        log.info("Creating order: token={}, productIds={}, quantities={}, addressId={}", token, productIds, quantities, addressId);

        // 从 token 中提取用户 ID
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.error("无效的 token，无法提取用户 ID");
            return R.error("Invalid token");
        }

        // 检查地址是否属于用户
        UserAddress address = userAddressService.getById(addressId);
        if (address == null){
            log.error("地址不存在，地址ID: {}", addressId);
            return R.error("Address not found");
        }
        log.info("用户ID: {}, 地址ID: {},地址用户ID:{}", userId, addressId,address.getUserId());
        if ( !address.getUserId().equals(userId)) {
            log.error("地址不属于用户，地址 ID: {}", addressId);
            return R.error("Invalid address ID");
        }

        // 获取商品列表
        List<Products> products = productService.getProductsByIds(productIds);
        if (products.isEmpty()) {
            log.error("未找到对应的产品，产品 ID 列表: {}", productIds);
            return R.error("Products not found");
        }
        log.info("Found products: {}", products);

        // 根据 merchantId 对产品进行分组
        Map<Long, List<Products>> productsByMerchant = products.stream()
                .collect(Collectors.groupingBy(Products::getMerchantId));

        // 存储所有订单 ID 以便返回
        List<Long> createdOrderIds = new ArrayList<>();

        // 遍历每个商家的产品，生成各自的订单
        for (Map.Entry<Long, List<Products>> entry : productsByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<Products> merchantProducts = entry.getValue();
            log.info("Creating order for merchant: {}", merchantId);
            log.info("Merchant products: {}", merchantProducts);
            // 验证库存并计算商家订单的总价
            // 检查商品是否属于可购买状态
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Products product : merchantProducts) {
                int quantity = quantities.getOrDefault(product.getProductId(), 0);
                if (quantity <= 0 || product.getStock() < quantity) {
                    log.warn("产品 {} 库存不足或购买数量无效", product.getProductId());
                    return R.error("Product " + product.getName() + " is out of stock or invalid quantity");
                }
                if (!product.getIsActive()||!ProductsStatus.APPROVED.equals(product.getStatus())){
                    log.warn("产品 {} 不可购买", product.getProductId());
                    return R.error("Product " + product.getName() + " is not active");
                }
                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                log.info("Product price: {}, quantity: {}, total amount: {}", product.getPrice(), quantity, totalAmount);
            }

            // 创建订单对象，设置用户、商家信息、地址和总金额
            Orders order = new Orders();
            order.setUserId(userId);
            order.setMerchantId(merchantId); // 设置商家 ID
            order.setAddressId(addressId); // 设置用户的收货地址 ID
            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.PENDING_PAYMENT); // 设置订单状态为待支付，等待用户调用支付接口
            order.setReturnStatus(OrdersReturnStatus.NOT_REQUESTED); // 设置退货状态为未申请
            order.setTaxAmount(BigDecimal.ZERO); // 暂时设置默认税额为 0 元
            order.setShippingFee(BigDecimal.valueOf(30)); // 暂时设置默认运费为 30 元
            log.info("Creating order for user: {}, merchant: {}, total amount: {}, Tax Amount:{},ShippingFee:{}",
                    userId, merchantId, totalAmount,order.getTaxAmount(), order.getShippingFee());
            // 保存订单到数据库
            if (!this.save(order)) {
                log.error("用户 {} 创建商家 {} 的订单失败", userId, merchantId);
                return R.error("Order creation failed");
            }
            createdOrderIds.add(order.getOrderId()); // 记录订单 ID
            log.info("Created order with ID: {}", order.getOrderId());

            // 创建订单详情并扣减库存
            List<OrderItems> orderDetailsList = new ArrayList<>();
            for (Products product : merchantProducts) {
                int quantity = quantities.get(product.getProductId());
                log.info("Creating order details for product: {}, quantity: {}", product.getProductId(), quantity);

                // 扣减库存
                product.setStock(product.getStock() - quantity);
                //增加销量
                product.setSales(product.getSales() + quantity);
                if (!productService.updateById(product)) {
                    log.error("更新产品 {} 库存失败", product.getProductId());
                    return R.error("Failed to update product stock");
                }
                log.info("Updated product {} stock to {}", product.getProductId(), product.getStock());
                log.info("Updated product {} sales to {}", product.getProductId(), product.getSales());

                // 创建订单详情
                OrderItems orderDetails = new OrderItems();
                orderDetails.setOrderId(order.getOrderId());
                orderDetails.setProductId(product.getProductId());
                orderDetails.setReturnStatus(OrderItemsReturnStatus.NOT_REQUESTED);
                orderDetails.setQuantity(quantity);
                orderDetails.setPrice(product.getPrice());
                orderDetailsList.add(orderDetails);

                log.info("Created order details for product: {}, quantity: {}", product.getProductId(), quantity);
            }

            // 批量保存订单详情
            if (!orderItemsService.saveBatch(orderDetailsList)) {
                log.error("保存订单详情失败，订单 ID: {}", order.getOrderId());
                return R.error("Failed to save order details");
            }
        }

        // 返回创建的所有订单 ID 信息
        log.info("订单创建成功，用户 ID: {}，订单 IDs: {}", userId, createdOrderIds);
        return R.success(createdOrderIds);
    }

    //用户支付订单
    @Override
    @Transactional
    public R<String> payOrders(String token, List<Long> orderIds, String paymentMethod) {
        log.info("=================调用 payOrders 方法=================");
        // 从 token 中提取用户 ID
        Long userId = jwtUtil.extractUserId(token);
        log.info("payOrders: User ID extracted from token: {}", userId);
        if (userId == null) {
            log.error("无效的 token，无法提取用户 ID");
            return R.error("Invalid token");
        }

        // 检查 orderIds 是否为空
        if (orderIds == null || orderIds.isEmpty()) {
            log.error("订单 ID 列表为空，无法进行支付");
            return R.error("Order IDs cannot be empty");
        }
        log.info("payOrders: Order IDs: {}", orderIds);

        // 获取用户传入的订单列表
        List<Orders> ordersToPay = this.getOrdersByIdsAndUserId(orderIds, userId);
        log.info("=========getOrdersByIdsAndUserId调用结束==============");
        log.info("payOrders: Orders to pay: {}", ordersToPay);

        // 检查订单是否存在，且为待付款状态
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, BigDecimal> merchantAmountMap = new HashMap<>(); // 存储每个商家应增加的待处理金额
        for (Orders order : ordersToPay) {
            BigDecimal merchantTotalAmount = BigDecimal.ZERO;
            if (!OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
                log.warn("订单 {} 状态无效，无法支付", order.getOrderId());
                return R.error("Order " + order.getOrderId() + " is not in pending payment status");
            }
            //订单总金额
            merchantTotalAmount=merchantTotalAmount.add(order.getTotalAmount()) //商品总价格
                    .add(order.getTaxAmount())//税费
                    .add(order.getShippingFee());//运费

            log.info("payOrders:订单 {} 的商品总价格为: {},税费:{},运费:{}",
                    order.getOrderId(),order.getTotalAmount(),order.getTaxAmount(),order.getShippingFee());
            log.info("payOrders:订单 {} 的总金额为: {}", order.getOrderId(), merchantTotalAmount);

            // 累加每个商家的订单金额
            //商家只添加商品总价格
            merchantAmountMap.merge(order.getMerchantId(), order.getTotalAmount(), BigDecimal::add);

            //用户需要支付运费+税费+商品总价格
            log.info("payOrders:原金额为: {},加上的金额为:{}", totalAmount,merchantTotalAmount);
            totalAmount = totalAmount.add(merchantTotalAmount); // 累加订单总金额
            log.info("payOrders:总金额更新为: {}", totalAmount);
        }

        // 调用支付服务进行支付 (假设有 `paymentService` 完成支付逻辑)
        boolean paymentSuccess = payLogicUtil.processPayment(userId, totalAmount, paymentMethod);
        if (!paymentSuccess) {
            log.error("用户 {} 的支付失败", userId);
            return R.error("Payment failed");
        }
        log.info("用户 {} 的支付成功", userId);

        // 更新订单状态为已支付并记录支付时间
        for (Orders order : ordersToPay) {
            log.info("payOrders:订单 {} 的状态为: {}", order.getOrderId(), order.getStatus());
            order.setStatus(OrderStatus.PENDING); // 设置为已支付待处理状态
            order.setPaymentMethod(paymentMethod);
            this.updateById(order); // 更新订单状态
            log.info("payOrders:订单 {} 的状态更新为: {}", order.getOrderId(), order.getStatus());
        }

        // 更新商家的待处理金额
        for (Map.Entry<Long, BigDecimal> entry : merchantAmountMap.entrySet()) {
            Long merchantId = entry.getKey();
            BigDecimal amountToAdd = entry.getValue();

            log.info("payOrders:商家 {} 的待处理金额增加: {}", merchantId, amountToAdd);

            merchantService.addPendingAmount(merchantId, amountToAdd); // 更新商家的待处理金额
            merchantService.addPendingAmountLog(merchantId,amountToAdd,"用户支付订单增加待处理金额","USD");

        }
        log.info("用户 ID: {} 的订单支付成功，订单 IDs: {},订单总金额为:{}", userId, orderIds, totalAmount);
        return R.success("Orders paid successfully");
    }

    //商家发货
    @Override
    @Transactional
    public R<String> merchantsShipppedProduct(String token, Long orderId) {
        log.info("======================merchantsShipppedProduct======================");
        log.info("商家 {} 请求发货，订单 ID: {}", token, orderId);

        Long merchantId = jwtUtil.extractUserId(token);
        if (merchantId == null) {
            log.error("无效的商家ID");
            return R.error("Invalid merchant ID");
        }

        Orders order = this.getById(orderId);
        if (order == null) {
            log.error("订单 {} 不存在", orderId);
            return R.error("Order does not exist");
        }

        if (!isOrderBelongsToMerchant(order, merchantId)) {
            log.error("订单 {} 不属于商家 {}", orderId, merchantId);
            return R.error("Unauthorized access");
        }

        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            log.warn("订单 {} 状态非待发货", orderId);
            return R.error("Order status is not pending");
        }

        Merchants merchants = merchantService.getById(merchantId);
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippingInfo("\n" + LocalDateTime.now() + " : 商家已从 " + merchants.getAddress() + " 发货");

        boolean isUpdated = this.updateById(order);
        if (!isUpdated) {
            log.error("更新订单状态失败，订单 ID: {}", orderId);
            throw new CustomException("Failed to update order status");
        }

        log.info("订单 {} 已成功发货", orderId);
        return R.success("Order shipped successfully");
    }
}
