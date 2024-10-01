package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Users;
import com.watergun.mapper.UsersMapper;
import com.watergun.service.CartService;
import com.watergun.service.MerchantApplicationService;
import com.watergun.service.UserService;
import com.watergun.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
;import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MerchantApplicationService merchantApplicationService;

    @Autowired
    private CartService cartService;


    @Override
    public List<Users> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Users::getUserId, userIds);

        return list(queryWrapper); // 使用 MyBatis-Plus 的 list 方法来批量查询
    }

    @Override
    @Transactional
    public R<String> createUser(Users user) {
        log.info("调用创建用户请求");
        log.info("user: {}", user);
        String password = user.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //加密
        password= passwordEncoder.encode(password);
        user.setPassword(password);
        this.save(user);
        //为用户添加购物车模块
        cartService.firstCreateCart(user.getUserId());
        return R.success("创建用户成功");
    }

    @Override
    public R<String> updateUser(String token, Users user) {
        log.info("调用更新用户请求");
        log.info("user: {}", user);
        log.info("token: {}", token);
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        String userRole = jwtUtil.extractRole(token);
        if (!userId.equals(user.getUserId())&&!userRole.equals("admin")){
            log.warn("用户ID {} 尝试修改非本人信息，当前角色: {}", userId, userRole);
            return R.error("无权限");
        }
        if (userRole.equals("user") && (user.getRole() == null || !user.getRole().equals("user"))) {
            log.warn("用户ID {} 尝试修改权限，当前角色: {}", userId, userRole);
            return R.error("请不要修改自己的权限");
        }
        log.info("更新用户:{}",user.toString());
        this.updateById(user);
        return R.success("更新用户成功");
    }

    @Override
    @Transactional
    public R<String> deleteUser(String token, Long userId) {
        log.info("调用删除用户请求");
        log.info("token: {}", token);
        log.info("userId: {}", userId);
        Long userIdNow = jwtUtil.extractUserId(token);
        String userRole =jwtUtil.extractRole(token);
        if (!userRole.equals("admin")&&!userIdNow.equals(userId)){
            return R.error("无权限");
        }
        log.info("删除用户:{}",userId);
        this.removeById(userId);
        cartService.removeCartByUserId(userId);
        return R.success("删除用户成功");
    }

    @Override
    public R<String> merchantApplication(String token, MerchantApplication merchantApplication) {
        log.info("调用商家申请请求");
        log.info("token: {}", token);
        log.info("merchantApplication: {}", merchantApplication);
        Long userId = jwtUtil.extractUserId(token);
        String userRole =jwtUtil.extractRole(token);
        if (!userRole.equals("user")){
            return R.error("管理员或商家不能够申请成为商家");
        }
        merchantApplication.setStatus("pending");
        merchantApplication.setUserId(userId);
        merchantApplicationService.save(merchantApplication);
        return R.success("申请成功");
    }

}
