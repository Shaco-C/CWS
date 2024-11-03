package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.OrdersDTO;
import com.watergun.dto.ProductDTO;
import com.watergun.dto.requestDTO.RefundRequest;
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

    private final WalletBalanceLogService walletBalanceLogService;

    private final PendingAmountLogService pendingAmountLogService;

    public OrderServiceImpl(JwtUtil jwtUtil, OrderItemsService orderItemsService, ProductService productService, MerchantService merchantService, UserAddressService userAddressService, PayLogicUtil payLogicUtil, WalletBalanceLogService walletBalanceLogService, PendingAmountLogService pendingAmountLogService) {
        this.jwtUtil = jwtUtil;
        this.orderItemsService = orderItemsService;
        this.productService = productService;
        this.merchantService = merchantService;
        this.userAddressService = userAddressService;
        this.payLogicUtil = payLogicUtil;
        this.walletBalanceLogService = walletBalanceLogService;
        this.pendingAmountLogService = pendingAmountLogService;
    }

    //-------------method--------------
    /**
     * 用户一次性购买多个商家的物品时，会生成多个订单，在这里统一验证
     * @param orderIds 订单id列表
     * @param userId 用户id
     * @return 属于用户的订单列表
     * @author CJ
     */
    
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

    /**
     * 判断订单是否属于商家
     * @param order 订单
     * @param merchantId 商家id
     * @return boolean
     * @author CJ
     */
    @Override
    public boolean isOrderBelongsToMerchant(Orders order, Long merchantId) {
        log.info("========================isOrderBelongsToMerchant 被调用====================");
        return order.getMerchantId().equals(merchantId);
    }
    
    /**
     * 查看订单方法，根据是否是商家查看被购买的订单进行分类讨论
     * @param status 订单状态
     * @param returnStatus 订单是否要求退货状态
     * @param isMerchant 是否为你商家查看自己被购买的订单
     * @return dtoPage 订单数据
     * @author CJ
     */

    // 新增私有方法处理订单查询和分页转换
    private R<Page> getOrders(int page, int pageSize, String token, String status, String returnStatus, boolean isMerchant) {
        log.info("======== getOrders 被调用 ========");
        log.info("参数: page={}, pageSize={}, token={}, status={}, returnStatus={}, isMerchant={}", page, pageSize, token, status, returnStatus, isMerchant);

        // 校验token是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token已过期: {}", token);
            return R.error("token 已过期");
        }
        log.info("Token未过期: {}", token);

        Long userId = jwtUtil.getUserIdFromToken(token);
        String userRole = jwtUtil.getUserRoleFromToken(token);

        // 确认调用者角色是否匹配
        if (isMerchant && !UserRoles.MERCHANT.name().equals(userRole)) {
            log.warn("非法调用 - 非商家用户尝试调用接口");
            throw new CustomException("非法调用");
        }

        // 构建分页对象和查询条件
        Page<Orders> orderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(isMerchant, Orders::getMerchantId, userId)
                .eq(!isMerchant, Orders::getUserId, userId)
                .eq(StringUtils.isNotEmpty(status), Orders::getStatus, status)
                .eq(StringUtils.isNotEmpty(returnStatus), Orders::getReturnStatus, returnStatus);

        // 分页查询
        Page<Orders> paginatedOrders = this.page(orderPage, ordersQueryWrapper);
        List<Orders> ordersList = paginatedOrders.getRecords();
        List<Long> orderIdList = ordersList.stream()
                .map(Orders::getOrderId)
                .distinct()
                .toList();

        if (orderIdList.isEmpty()) {
            log.info("没有符合条件的订单");
            return R.success(new Page<>(page, pageSize));
        }
        log.info("符合条件的订单ID列表: {}", orderIdList);

        // 提取所有 merchantId
        List<Long> merchantIds = ordersList.stream()
                .map(Orders::getMerchantId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询所有相关商家信息
        Map<Long, Merchants> merchantsMap = merchantService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchants::getMerchantId, merchant -> merchant));

        // 查询订单详情
        List<OrderItems> orderItemsList = orderItemsService.list(
                new LambdaQueryWrapper<OrderItems>().in(OrderItems::getOrderId, orderIdList));
        Map<Long, List<OrderItems>> orderItemsMap = orderItemsList.stream()
                .collect(Collectors.groupingBy(OrderItems::getOrderId));

        // 提取所有ProductId并查询产品信息
        List<Long> productIdList = orderItemsList.stream()
                .map(OrderItems::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Products> productsMap = productService.getProductsByIds(productIdList).stream()
                .collect(Collectors.toMap(Products::getProductId, product -> product));

        // 根据OrderId将Product整合到一起
        Map<Long, List<ProductDTO>> orderItemsProductMap = orderItemsList.stream()
                .collect(Collectors.groupingBy(OrderItems::getOrderId,
                        Collectors.mapping(orderItem -> {
                            Products product = productsMap.get(orderItem.getProductId());
                            if (product != null) {
                                Merchants merchants = merchantsMap.get(product.getMerchantId());
                                // 创建 ProductDTO，并设置 quantity
                                ProductDTO productDTO = new ProductDTO(product);
                                productDTO.setQuantity(orderItem.getQuantity());
                                if (merchants != null) {
                                    productDTO.setShopName(merchants.getShopName());
                                    productDTO.setAddress(merchants.getAddress());
                                    productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
                                }
                                return productDTO;
                            }
                            return null;
                        }, Collectors.toList())));

        // 整合成OrdersDTO
        List<OrdersDTO> ordersDTOList = paginatedOrders.getRecords().stream()
                .map(order -> {
                    OrdersDTO ordersDTO = new OrdersDTO(order);
                    ordersDTO.setProductDTOList(orderItemsProductMap.get(order.getOrderId()));
                    return ordersDTO;
                }).toList();

        Page<OrdersDTO> dtoPage = new Page<>(page, pageSize);
        dtoPage.setRecords(ordersDTOList);
        dtoPage.setTotal(paginatedOrders.getTotal());
        dtoPage.setCurrent(paginatedOrders.getCurrent());
        dtoPage.setSize(paginatedOrders.getSize());
        log.info("查询完毕");
        return R.success(dtoPage);
    }



    //-------------serviceLogic-----------

    //商家查看属于自己的订单
    // 商家查看待处理订单
    @Override
    public R<Page> merchantsGetOrders(int page, int pageSize, String token, String status, String returnStatus) {
        log.info("=========================调用 merchantsGetOrders方法=========================");
        log.info("商家查看待处理订单");
        return getOrders(page, pageSize, token, status, returnStatus, true);
    }

    // 用户查看历史订单
    @Override
    public R<Page> getHistoryOrders(int page, int pageSize, String token, String status, String returnStatus) {
        log.info("=========================调用 getHistoryOrders方法=========================");
        log.info("用户查看历史订单");
        return getOrders(page, pageSize, token, status, returnStatus, false);
    }

    // 查看订单详情
    @Override
    public R<OrdersDTO> getOrderDetail(String token, Long orderId) {
        log.info("========================= 调用 getOrderDetail 方法 =========================");
        log.info("查看订单详情，订单ID: {}", orderId);

        // 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("用户ID: {}", userId);

        // 查询订单
        Orders order = this.getById(orderId);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", orderId);
            return R.error("Order not found");
        }

        // 校验订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            log.error("订单不属于用户，订单ID: {}", orderId);
            return R.error("Invalid order ID");
        }

        // 获取商家信息
        Merchants merchants = merchantService.getById(order.getMerchantId());
        log.info("商家信息: {}", merchants);

        // 查询该订单的所有订单项
        LambdaQueryWrapper<OrderItems> orderItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderItemsLambdaQueryWrapper.eq(OrderItems::getOrderId, orderId);

        List<OrderItems> orderItemsList = orderItemsService.list(orderItemsLambdaQueryWrapper);
        if (orderItemsList.isEmpty()) {
            log.error("未找到订单详情，订单ID: {}", orderId);
            return R.error("Order details error");
        }
        log.info("找到订单项数量: {}", orderItemsList.size());

        // 提取所有产品ID并计算每个产品的数量
        List<Long> productIds = orderItemsList.stream()
                .map(OrderItems::getProductId)
                .distinct()
                .toList();

        // 使用 Map 存储每个产品的数量
        Map<Long, Integer> quantities = orderItemsList.stream()
                .collect(Collectors.toMap(OrderItems::getProductId, OrderItems::getQuantity));

        // 批量查询所有相关产品信息
        List<Products> products = productService.getProductsByIds(productIds);
        log.info("找到产品数量: {}", products.size());

        // 将产品信息转换为 ProductDTO，并设置数量和商家信息
        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductDTO productDTO = new ProductDTO(product);
            productDTO.setQuantity(quantities.get(product.getProductId()));
            productDTO.setShopName(merchants.getShopName());
            productDTO.setAddress(merchants.getAddress());
            productDTO.setShopAvatarUrl(merchants.getShopAvatarUrl());
            return productDTO;
        }).toList();

        // 创建 OrdersDTO 并设置产品列表
        OrdersDTO ordersDTO = new OrdersDTO(order);
        ordersDTO.setProductDTOList(productDTOList);

        log.info("订单详情查询成功，返回订单DTO: {}", ordersDTO);
        return R.success(ordersDTO);
    }


    // 创建订单  用户下单
    @Override
    @Transactional
    public R<List<Long>> createOrder(String token, List<Long> productIds, Map<Long, Integer> quantities, Long addressId) {
        log.info("=========================调用 createOrder方法=========================");
        log.info("Creating order: token={}, productIds={}, quantities={}, addressId={}", token, productIds, quantities, addressId);

        // 从 token 中提取用户 ID
        Long userId = jwtUtil.getUserIdFromToken(token);

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
            order.setTotalAmount(totalAmount);//商品总金额
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
        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("payOrders: User ID extracted from token: {}", userId);

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

            merchantService.modifyPendingAmount(merchantId, amountToAdd); // 更新商家的待处理金额
            pendingAmountLogService.modifyPendingAmountLog(merchantId,amountToAdd,"用户支付订单增加待处理金额","USD");

        }
        log.info("用户 ID: {} 的订单支付成功，订单 IDs: {},订单总金额为:{}", userId, orderIds, totalAmount);
        return R.success("Orders paid successfully");
    }

    //用户取消订单
    @Override
    public R<String> cancelOrder(String token, Long orderId) {
        log.info("======================cancelOrder======================");
        log.info("用户 {} 请求取消订单，订单 ID: {}", token, orderId);

        Long userId = jwtUtil.getUserIdFromToken(token);

        Orders order = this.getById(orderId);
        if (order == null) {
            log.error("订单 {} 不存在", orderId);
            return R.error("Order does not exist");
        }

        if (!userId.equals(order.getUserId())) {
            log.error("用户 {} 无权取消订单 {}", userId, orderId);
            return R.error("Unauthorized access");
        }

        if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            log.warn("订单 {} 的状态不能被取消", orderId);
            return R.error("Order cannot be cancelled due to its current status");
        }

        // 对订单涉及到的 product 进行库存恢复
        // 获取涉及到的 OrderItems
        LambdaQueryWrapper<OrderItems> orderItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderItemsLambdaQueryWrapper.eq(OrderItems::getOrderId, orderId);
        List<OrderItems> orderItemsList = orderItemsService.list(orderItemsLambdaQueryWrapper);

        // 获取所有 productId 并批量查询产品信息
        List<Long> productIds = orderItemsList.stream()
                .map(OrderItems::getProductId)
                .collect(Collectors.toList());
        List<Products> productsList = productService.getProductsByIds(productIds);

        // 将 Products 转换为 Map，方便通过 productId 快速查找对应产品
        Map<Long, Products> productsMap = productsList.stream()
                .collect(Collectors.toMap(Products::getProductId, product -> product));

        // 逐一更新库存和销量
        for (OrderItems orderItems : orderItemsList) {
            Products product = productsMap.get(orderItems.getProductId());
            if (product != null) {
                // 增加库存
                product.setStock(product.getStock() + orderItems.getQuantity());
                // 减少销量
                product.setSales(product.getSales() - orderItems.getQuantity());
            }
        }

        // 批量更新产品信息
        productService.updateBatchById(productsList);

        // 对待处理状态的订单进行额外处理
        // 减少商家的待确认金额
        if (OrderStatus.PENDING.equals(order.getStatus())) {
            Merchants merchants = merchantService.getById(order.getMerchantId());
            if (merchants == null) {
                log.error("商家 ID {} 不存在", order.getMerchantId());
                return R.error("Merchant does not exist");
            }

            BigDecimal amountToSubtract = order.getTotalAmount();
            log.info("商家 {} 的待处理金额减少: {}", merchants.getMerchantId(), amountToSubtract);
            merchantService.modifyPendingAmount(merchants.getMerchantId(), amountToSubtract.negate());
            pendingAmountLogService.modifyPendingAmountLog(merchants.getMerchantId(), amountToSubtract.negate(), "用户取消订单减少待处理金额", "USD");
        }

        order.setStatus(OrderStatus.CANCELLED);
        boolean result = this.updateById(order);
        if (!result) {
            log.error("订单 {} 取消失败", orderId);
            return R.error("Order cancellation failed");
        }
        log.info("订单 {} 已成功取消", orderId);

        return R.success("Order cancelled successfully");
    }


    //商家发货
    @Override
    @Transactional
    public R<String> merchantsShipppedProduct(String token, Long orderId) {
        log.info("======================merchantsShipppedProduct======================");
        log.info("商家 {} 请求发货，订单 ID: {}", token, orderId);

        Long merchantId = jwtUtil.getUserIdFromToken(token);

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


    //模拟快递员送货方法
    @Override
    public R<String> transitProduct(Long orderId) {
        log.info("======================transitProduct======================");
        log.info("请求物流，订单 ID: {}", orderId);
        Orders order = this.getById(orderId);
        log.info("查询订单是否存在");
        if (order == null) {
            log.info("订单 {} 不存在", orderId);
            return R.error("Order does not exist");
        }
        log.info("订单存在");
        log.info("验证订单状态是否为SHIPPED");
        if (!OrderStatus.SHIPPED.equals(order.getStatus())) {
            log.info("订单 {} 状态非已发货", orderId);
            return R.error("Order status is not shipped");
        }
        log.info("订单状态为SHIPPED");
        order.setStatus(OrderStatus.IN_TRANSIT);
        String msg = order.getShippingInfo() + "\n" + LocalDateTime.now() + " : 物流已取件,正在发往所在地";
        order.setShippingInfo(msg);
        boolean result = this.updateById(order);
        if (!result) {
            log.info("更新订单状态失败，订单 ID: {}", orderId);
            throw new CustomException("Failed to update order status");
        }
        log.info("订单 {} 已成功发货", orderId);
        return R.success("Order transit successfully");
    }

    //模拟快递送达目的地方法
    @Override
    public R<String> deliveredProduct(Long orderId) {
        log.info("======================deliveredProduct======================");
        log.info("请求物流，订单 ID: {}", orderId);
        Orders order = this.getById(orderId);
        if (order == null) {
            log.info("订单 {} 不存在", orderId);
            return R.error("Order does not exist");
        }
        log.info("检查订单状态是否为IN_TRANSIT");
        if (!OrderStatus.IN_TRANSIT.equals(order.getStatus())) {
            log.info("订单 {} 状态非在途中", orderId);
            return R.error("Order status is not in transit");
        }
        log.info("订单状态为IN_TRANSIT");
        order.setStatus(OrderStatus.DELIVERED);
        UserAddress userAddress = userAddressService.getById(order.getAddressId());
        String msg = order.getShippingInfo() + "\n" + LocalDateTime.now() + " : 物流已送达 "
                + userAddress.getFullAddress();
        order.setShippingInfo(msg);
        boolean result = this.updateById(order);
        if (!result) {
            log.info("更新订单状态失败，订单 ID: {}", orderId);
            return R.error("Failed to update order");
        }
        log.info("订单 {} 成功抵达取货点", orderId);
        return R.success("Order delivered successfully");
    }

    //用户确认收货
    @Override
    @Transactional
    public R<String> receivedProduct(String token, Long orderId) {
        log.info("======================receivedProduct======================");
        log.info("请求物流，订单 ID: {}", orderId);
        Long userId = jwtUtil.getUserIdFromToken(token);

        Orders orders = this.getById(orderId);
        if (orders == null) {
            log.info("订单 {} 不存在", orderId);
            return R.error("Order does not exist");
        }
        if (!userId.equals(orders.getUserId())) {
            log.info("用户 {} 无权限确认订单 {} 的收货", userId, orderId);
            return R.error("User does not have permission to confirm receipt");
        }
        if (!OrderStatus.DELIVERED.equals(orders.getStatus())) {
            log.info("订单 {} 状态非已送达", orderId);
            return R.error("Order status is not delivered");
        }
        log.info("订单 {} 状态为已送达", orderId);
        log.info("用户 {} 确认收货", userId);
        orders.setStatus(OrderStatus.RECEIVED);
        String msg = orders.getShippingInfo() + "\n" + LocalDateTime.now() + " : 用户已确认收货";
        orders.setShippingInfo(msg);
        boolean result = this.updateById(orders);
        if (!result) {
            log.info("更新订单状态失败，订单 ID: {}", orderId);
            return R.error("Failed to update order status");
        }
        log.info("订单 {} 已成功收货", orderId);

        //将商家待确认金额添加到确认金额中
        merchantService.modifyWalletBalance(orders.getMerchantId(),orders.getTotalAmount());

        //添加待确认余额变更日志
        pendingAmountLogService.modifyPendingAmountLog(orders.getMerchantId(), orders.getTotalAmount().negate(),
                "用户确认收货，待确认金额转入确认金额", orders.getCurrency());

        //添加确认金额变更日志
        walletBalanceLogService.modifyWalletBalanceLog(orders.getMerchantId(),orderId, orders.getTotalAmount(),
                "用户确认收货，确认金额增加", orders.getCurrency());
        log.info("订单 {} 已成功收货，商家待确认金额已添加到确认金额中", orderId);
        return R.success("Order received successfully");
    }


    //用户申请退货
    @Override
    public R<String> userReturnProductApplication(String token, RefundRequest returnRequest) {
        log.info("======================userReturnProductApplication======================");

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("user{} apply for return", userId);
        if (!Objects.equals(returnRequest.getUserId(), userId)) {
            log.info("用户 {} 无权限申请退货", userId);
            return R.error("User does not have permission to apply for return");
        }

        Orders orders = this.getById(returnRequest.getOrderId());

        if (orders == null){
            log.info("订单 {} 不存在", returnRequest.getOrderId());
            return R.error("Order does not found");
        }

        if (!orders.getStatus().equals(OrderStatus.IN_TRANSIT)){
            log.info("订单 {} 只有在非收货的情况下才能够申请退货", returnRequest.getOrderId());
            return R.error("Order status is not in right status to apply for return");
        }

        if (!orders.getReturnStatus().equals(OrdersReturnStatus.NOT_REQUESTED)){
            log.info("订单 {} 已经申请退货", returnRequest.getOrderId());
            return R.error("Order has already applied for return");
        }

        orders.setReturnStatus(OrdersReturnStatus.REQUESTED);
        orders.setReturnReason(returnRequest.getReturnReason());

        log.info("orders:{}",orders);
        boolean result = this.updateById(orders);

        if (!result){
            log.error("Failed to apply for return");
            return R.error("Failed to apply for return");
        }
        log.info("Return application submitted successfully");
        return R.success("Return application submitted successfully");
    }

    //用户进行退货（在退货请求审核通过后，对应订单return_status状态为APPROVED）
    @Override
    @Transactional
    public R<String> userRefundProducts(String token, Long orderId) {
        log.info("======================userRefundProducts======================");
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 1. 验证订单存在性和用户权限
        Orders orders = this.getById(orderId);
        if (orders == null) {
            log.info("订单 {} 不存在", orderId);
            return R.error("Order does not found");
        }
        if (!orders.getUserId().equals(userId)) {
            log.info("用户 {} 无权限进行退货", userId);
            return R.error("User does not have permission to return products");
        }
        if (!orders.getReturnStatus().equals(OrdersReturnStatus.APPROVED)) {
            log.info("订单 {} 退货状态不是已批准", orderId);
            return R.error("Return status is not approved");
        }
        if (!orders.getStatus().equals(OrderStatus.IN_TRANSIT)) {
            log.info("订单 {} 状态不对", orderId);
            return R.error("Order status is not delivered or in transit");
        }

        // 2. 更新商家的待确认金额
        Merchants merchants = merchantService.getById(orders.getMerchantId());
        if (merchants == null) {
            log.info("商家 {} 不存在", orders.getMerchantId());
            return R.error("Merchant does not found");
        }
        merchants.setPendingBalance(merchants.getPendingBalance().subtract(orders.getTotalAmount()));
        boolean merchantResult = merchantService.updateById(merchants);
        pendingAmountLogService.modifyPendingAmountLog(
                merchants.getMerchantId(),
                orders.getTotalAmount().negate(),
                "用户退货，待确认金额退回",
                orders.getCurrency()
        );
        if (!merchantResult) {
            log.error("Failed to update merchant's pending balance");
            return R.error("Failed to update merchant's pending balance");
        }

        // 3. 更新产品库存和销量
        LambdaQueryWrapper<OrderItems> orderItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderItemsLambdaQueryWrapper.eq(OrderItems::getOrderId, orderId);
        List<OrderItems> orderItemsList = orderItemsService.list(orderItemsLambdaQueryWrapper);

        Map<Long, Integer> productSalesNumMap = orderItemsList.stream()
                .collect(Collectors.toMap(OrderItems::getProductId, OrderItems::getQuantity));

        List<Long> productIds = new ArrayList<>(productSalesNumMap.keySet());
        List<Products> productsList = productService.listByIds(productIds);

        productsList.forEach(product -> {
            Integer quantity = productSalesNumMap.get(product.getProductId());
            if (quantity != null) {
                product.setSales(product.getSales() - quantity);
                product.setStock(product.getStock() + quantity);
            }
        });

        boolean productResult = productService.updateBatchById(productsList);
        if (!productResult) {
            log.error("Failed to update product's stock and sales");
            return R.error("Failed to update product's stock and sales");
        }

        // 4. 更新订单的退货状态
        orders.setReturnStatus(OrdersReturnStatus.RETURNED);
        String msg = orders.getShippingInfo()+"\n "+LocalDateTime.now()+": 商品已被退货";
        boolean orderResult = this.updateById(orders);
        if (!orderResult) {
            log.error("Failed to update order's status");
            return R.error("Failed to update order's status");
        }

        return R.success("Return products successfully");
    }



    //商家处理退货申请
    @Override
    public R<String> merchantsHandleReturnRequest(String token, Long orderId, String status) {
        log.info("======================merchantsHandleReturnRequest======================");

        Long merchantId = jwtUtil.getUserIdFromToken(token);
        log.info("merchant{} handle return request{}", merchantId,orderId);

        if (!status.equals(OrdersReturnStatus.REJECTED.name())&&!status.equals(OrdersReturnStatus.APPROVED.name())){
            log.info("退货状态 {} 不合法", status);
            return R.error("Return status is not valid");
        }

        String userRole = jwtUtil.extractRole(token);
        if (!UserRoles.MERCHANT.name().equals(userRole)){
            log.info("用户 {} 无权限处理退货请求", merchantId);
            return R.error("User does not have permission to handle return request");
        }
        Orders orders = this.getById(orderId);
        if (orders == null){
            log.info("订单 {} 不存在", orderId);
            return R.error("Order does not found");
        }

        if (!orders.getReturnStatus().equals(OrdersReturnStatus.REQUESTED)){
            log.info("订单已处理，或无需处理");
            return R.error("Order has been processed or does not need to be processed");
        }
        if (status.equals(OrdersReturnStatus.REJECTED.name())){
            orders.setReturnStatus(OrdersReturnStatus.REJECTED);
        }else{
            orders.setReturnStatus(OrdersReturnStatus.APPROVED);
        }
        boolean result = this.updateById(orders);
        if (!result){
            log.error("Failed to handle return request");
            return R.error("Failed to handle return request");
        }
        log.info("Return request handled successfully");
        return R.success("Return request handled successfully");
    }
}
