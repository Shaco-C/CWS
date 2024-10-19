package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.CustomException;
import com.watergun.common.R;
import com.watergun.dto.OrderDTO;
import com.watergun.dto.ShopDTO;
import com.watergun.entity.*;
import com.watergun.mapper.MerchantsMapper;
import com.watergun.service.*;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantService {

    @Autowired
    @Lazy
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private ProductService productService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private OrderService orderService;

    @Autowired
    @Lazy
    private OrderItemsService orderItemsService;

    @Override
    public R<String> updateMerchant(String token, Merchants merchants) {
        log.info("token: {}", token);
        log.info("merchants: {}", merchants);

        // 校验 token 是否有效
        if (!jwtUtil.isTokenExpired(token)) {
            return R.error("无效的token");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 如果是管理员，可以修改任何商家信息
        if ("admin".equals(userRole)) {
            log.info("管理员{}正在修改商家信息", merchantId);
        } else if (merchantId == null || !merchantId.equals(merchants.getMerchantId())) {
            // 如果是商家本人，必须匹配商家ID
            log.warn("商家{}尝试修改无权限的商家信息", merchantId);
            return R.error("权限不足");
        }

        // 更新商家信息
        boolean result = this.updateById(merchants);
        if (!result) {
            return R.error("修改失败，可能是数据库问题或商家信息不存在");
        }

        return R.success("商家信息修改成功");
    }

    @Override
    public R<ShopDTO> getMerchantInfo(Long merchantId) {
        log.info("获取商家信息，商家ID: {}", merchantId);

        if (merchantId == null) {
            return R.error("商家ID不能为空");
        }

        // 获取商家信息
        Merchants merchants = this.getById(merchantId);
        log.info("商家信息: {}", merchants);

        if (merchants == null) {
            return R.error("商家不存在");
        }

        // 初始化ShopDTO对象
        ShopDTO shopDTO = new ShopDTO(merchants);  // 假设你有一个构造函数可以初始化

        // 获取商家的产品列表
        LambdaQueryWrapper<Products> productsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productsLambdaQueryWrapper.eq(Products::getMerchantId, merchantId);
        List<Products> products = productService.list(productsLambdaQueryWrapper);

        // 设置产品列表
        shopDTO.setProductsList(products);

        log.info("商铺信息和产品列表: {}", shopDTO);

        // 返回商铺DTO信息
        return R.success(shopDTO);
    }

    @Override
    @Transactional
    public R<String> deleteMerchant(String token) {
        log.info("deleteMerchant已经被调用");
        log.debug("token: {}", token);

        // 校验 token 是否过期
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token has expired: {}", token);
            return R.error("token 已过期");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        // 检查商家是否存在商品
        Long cnt = productService.count(new LambdaQueryWrapper<Products>().eq(Products::getMerchantId, merchantId).last("LIMIT 1"));
        if (cnt > 0) {
            log.warn("商家下还有产品，无法删除");
            return R.error("商家下还有产品，无法删除");
        }

        // 检查商家的钱包是否处理完毕
        Merchants merchants = this.getById(merchantId);
        BigDecimal walletBalance = merchants.getWalletBalance();
        BigDecimal pendingBalance = merchants.getPendingBalance();

        if (walletBalance.compareTo(BigDecimal.ZERO) != 0 || pendingBalance.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("商家钱包未处理完毕，无法删除");
            throw new CustomException("商家钱包未处理完毕，无法删除");
        }

        // 检查角色是否为商家
        if (!"merchant".equals(userRole)) {
            log.warn("非法调用");
            throw new CustomException("非法调用");
        }

        // 将商家角色改为普通用户
        Users users = new Users();
        users.setUserId(merchantId);
        users.setRole("user");

        boolean userUpdateResult = userService.updateById(users);
        if (!userUpdateResult) {
            log.warn("用户角色更新失败");
            throw new CustomException("用户角色更新失败");
        }

        // 删除商家
        boolean result = this.removeById(merchantId);
        if (!result) {
            log.warn("删除失败，可能是数据库问题或商家信息不存在");
            throw new CustomException("删除失败，可能是数据库问题或商家信息不存在");
        }

        return R.success("商家删除成功");
    }

    @Override
    public R<Page> getOrders(int page, int pageSize, String token, String status, String returnStatus) {
        log.info("getOrders已经被调用");
        log.info("getOrders:page: {}, pageSize: {}, token: {}, status: {}, returnStatus: {}", page, pageSize, token, status, returnStatus);

        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Token has expired: {}", token);
            return R.error("token 已过期");
        }

        Long merchantId = jwtUtil.extractUserId(token);
        String userRole = jwtUtil.extractRole(token);

        log.info("getOrders方法:merchantId: {}, userRole: {}", merchantId, userRole);
        if (!"merchant".equals(userRole)) {
            log.warn("非法调用");
            throw new CustomException("非法调用");
        }

        // 分页查询商家订单
        Page<Orders> orderPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getMerchantId, merchantId)
                .eq(StringUtils.isNotEmpty(status), Orders::getStatus, status)
                .eq(StringUtils.isNotEmpty(returnStatus), Orders::getReturnStatus, returnStatus);

        // 分页查询商家的订单数据
        Page<Orders> paginatedOrders = orderService.page(orderPage, ordersLambdaQueryWrapper);

        // 获取所有订单ID并查询对应的订单详情
        List<Long> orderIdList = paginatedOrders.getRecords().stream().map(Orders::getOrderId).distinct().toList();
        LambdaQueryWrapper<OrderItems> orderItemsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderItemsLambdaQueryWrapper.in(OrderItems::getOrderId, orderIdList);
        List<OrderItems> orderItemsList = orderItemsService.list(orderItemsLambdaQueryWrapper);

        // 将同一个orderId的OrderItems放在同一个map的list中
        Map<Long, List<OrderItems>> orderItemsMap = orderItemsList.stream()
                .collect(Collectors.groupingBy(OrderItems::getOrderId));

        // 将订单和订单详情进行关联，转换为OrderDTO对象
        List<OrderDTO> orderDTOList = paginatedOrders.getRecords().stream().map(order -> {
            OrderDTO orderDTO = new OrderDTO(order);
            orderDTO.setOrderItemsList(orderItemsMap.get(order.getOrderId()));
            return orderDTO;
        }).toList();

        // 创建新的Page对象，用于返回分页后的OrderDTO数据
        Page<OrderDTO> dtoPage = new Page<>(page, pageSize);
        dtoPage.setRecords(orderDTOList);
        dtoPage.setTotal(paginatedOrders.getTotal()); // 保持原分页查询的总数
        dtoPage.setCurrent(paginatedOrders.getCurrent());
        dtoPage.setSize(paginatedOrders.getSize());

        return R.success(dtoPage);
    }

    @Override
    @Transactional
    public R<String> withdraw(String token , BigDecimal amount)  {
        log.info("withdraw方法: token: {}, amount: {}", token, amount);

        // 校验用户身份
        String userRole = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        if (!"merchant".equals(userRole)) {
            log.warn("withdraw方法: 非法用户角色尝试提现");
            return R.error("只有商家角色可以进行提现操作");
        }

        // 校验提现金额
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("withdraw方法: 提现金额不合法");
            return R.error("提现金额不合法");
        }

        // 检查提现金额是否过大（可选，具体根据业务规则）
        BigDecimal maxWithdrawLimit = new BigDecimal("10000.00"); // 假设最大一次提现上限为10000
        if (amount.compareTo(maxWithdrawLimit) > 0) {
            log.warn("withdraw方法: 提现金额超过最大限制");
            return R.error("提现金额超过最大限制");
        }

        // 检查商家信息
        Merchants merchants = this.getById(userId);
        if (merchants == null) {
            log.warn("withdraw方法: 商家不存在");
            return R.error("商家不存在");
        }

        // 校验余额是否足够
        BigDecimal walletBalance = merchants.getWalletBalance();
        if (walletBalance.compareTo(amount) < 0) {
            log.warn("withdraw方法: 商家余额不足，当前余额: {}, 请求提现金额: {}", walletBalance, amount);
            return R.error("商家余额不足");
        }

        // 更新余额并保存
        merchants.setWalletBalance(walletBalance.subtract(amount));
        boolean result = this.updateById(merchants);
        if (!result) {
            log.warn("withdraw方法: 提现失败");
            return R.error("提现失败");
        }

        log.info("withdraw方法: 提现成功，剩余余额: {}", merchants.getWalletBalance());
        return R.success("提现成功，剩余余额: " + merchants.getWalletBalance());
    }

}
