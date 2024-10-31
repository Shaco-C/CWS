package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.UserAddress;
import com.watergun.mapper.UserAddressMapper;
import com.watergun.service.UserAddressService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户地址服务的实现类
 * 继承了 MyBatis-Plus 提供的 ServiceImpl 类，封装了基础的数据库操作
 * 实现了自定义的 UserAddressService 接口，处理用户地址相关的业务逻辑
 */
@Service
@Slf4j
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    // JWT工具类，用于解析Token并获取用户信息
    private final JwtUtil jwtUtil;

    /**
     * 构造方法，注入 JwtUtil 实例
     *
     * @param jwtUtil JWT工具类，用于解析Token并获取用户信息
     */
    public UserAddressServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 根据Token获取用户ID
     *
     * @param token 用户的JWT Token
     * @return 用户ID
     * @throws IllegalStateException 如果用户未登录或Token无效，将抛出异常
     */
    private Long getUserIdFromToken(String token) {
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        if (userId == null) {
            log.warn("用户未登录");
            throw new IllegalStateException("user not login");
        }
        return userId;
    }

    /**
     * 检查用户ID是否与地址的用户ID匹配
     *
     * @param userId        当前登录用户的ID
     * @param addressUserId 地址所属的用户ID
     * @return 如果匹配返回 true，否则返回 false
     */
    private boolean isUserAuthorized(Long userId, Long addressUserId) {
        if (!userId.equals(addressUserId)) {
            log.warn("用户id不匹配");
            return false;
        }
        log.info("用户id匹配");
        return true;
    }

    /**
     * 添加新地址
     *
     * @param token       用户的JWT Token
     * @param userAddress 新地址信息
     * @return 操作结果，成功或失败的信息
     */
    @Override
    public R<String> addAddress(String token, UserAddress userAddress) {
        log.info("============addAddress==============");
        log.info("Entering addAddress with token: {}, userAddress: {}", token, userAddress);
        Long userId = getUserIdFromToken(token);
        log.info("userId: {}", userId);
        // 验证用户ID是否匹配
        if (!isUserAuthorized(userId, userAddress.getUserId())) {
            log.warn("user id not match");
            return R.error("user id not match");
        }
        log.info("user id is match");
        // 保存地址信息
        boolean result = this.save(userAddress);
        if (!result) {
            log.warn("添加地址失败");
            return R.error("failed to create a new address");
        }
        log.info("添加地址成功");
        return R.success("success to create a new address");
    }

    /**
     * 更新地址信息
     *
     * @param token       用户的JWT Token
     * @param userAddress 更新后的地址信息
     * @return 操作结果，成功或失败的信息
     */
    @Override
    public R<String> updateAddress(String token, UserAddress userAddress) {
        log.info("================updateAddress====================");
        log.info("Entering updateAddress with token: {}, userAddress: {}", token, userAddress);
        Long userId = getUserIdFromToken(token);
        log.info("userId: {}", userId);
        // 验证用户ID是否匹配
        if (!isUserAuthorized(userId, userAddress.getUserId())) {
            log.warn("user id not match");
            return R.error("user id not match");
        }
        log.info("user id is match");

        // 更新地址信息
        boolean result = this.updateById(userAddress);
        if (!result) {
            log.warn("更新地址失败");
            return R.error("failed to update address");
        }
        log.info("更新地址成功");
        return R.success("success to update address");
    }

    /**
     * 删除地址
     *
     * @param token     用户的JWT Token
     * @param addressId 要删除的地址ID
     * @return 操作结果，成功或失败的信息
     */
    @Override
    public R<String> deleteAddress(String token, Long addressId) {
        log.info("================deleteAddress====================");
        log.info("Entering deleteAddress with token: {}, addressId: {}", token, addressId);
        Long userId = getUserIdFromToken(token);
        log.info("userId: {}", userId);

        // 检查地址是否存在并且属于当前用户
        UserAddress address = this.getById(addressId);
        log.info("address: {}", address);
        if (address == null || !isUserAuthorized(userId, address.getUserId())) {
            return R.error("user id not match or address not found");
        }

        // 删除地址
        boolean result = this.removeById(addressId);
        if (!result) {
            log.warn("删除地址失败");
            return R.error("failed to delete address");
        }
        log.info("删除地址成功");
        return R.success("success to delete address");
    }

    /**
     * 设置默认地址
     *
     * @param token 用户的JWT Token
     * @param id    要设置为默认地址的ID
     * @return 操作结果，成功或失败的信息
     */
    @Override
    public R<String> setDefaultAddress(String token, Long id) {
        log.info("================setDefaultAddress====================");
        log.info("Entering setDefaultAddress with token: {}, id: {}", token, id);
        Long userId = getUserIdFromToken(token);
        log.info("userId: {}", userId);

        // 先将用户的所有地址的isDefault字段设置为false
        LambdaQueryWrapper<UserAddress> resetWrapper = new LambdaQueryWrapper<>();
        resetWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, true);

        UserAddress resetAddress = new UserAddress();
        resetAddress.setIsDefault(false);
        this.update(resetAddress, resetWrapper);
        log.info("reset default address success");

        // 设置指定的地址为默认地址
        UserAddress userAddress = new UserAddress();
        userAddress.setIsDefault(true);
        userAddress.setAddressId(id);
        log.info("userAddress: {}", userAddress);

        // 更新数据库，设置新默认地址
        boolean result = this.updateById(userAddress);
        if (!result) {
            log.warn("设置默认地址失败");
            return R.error("failed to set default address");
        }
        log.info("设置默认地址成功");
        return R.success("success to set default address");
    }

    /**
     * 获取用户地址列表
     * @param  token 用户的JWT Token
     * @return 用户地址列表
     * @author CJ
     */
    @Override
    public R<List<UserAddress>> getAddressList(String token) {
        log.info("================getAddressList====================");
        log.info("Entering getAddressList with token: {}", token);

        // 校验 token 是否有效
        if (token == null || token.isEmpty()) {
            log.warn("Token is null or empty");
            return R.error("Invalid token");
        }

        Long userId = getUserIdFromToken(token);

        // 查询用户的所有地址
        LambdaQueryWrapper<UserAddress> userAddressLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userAddressLambdaQueryWrapper.eq(UserAddress::getUserId, userId);

        List<UserAddress> userAddressList = this.list(userAddressLambdaQueryWrapper);
        log.info("Fetched userAddressList for userId {}: {}", userId, userAddressList);

        // 使用 Optional.isEmpty() 替代手动检查空列表
        return R.success(Optional.ofNullable(userAddressList).orElse(Collections.emptyList()));
    }

}

