package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Users;
import com.watergun.enums.MerchantApplicationsStatus;
import com.watergun.enums.UserRoles;
import com.watergun.mapper.UsersMapper;
import com.watergun.service.MerchantApplicationService;
import com.watergun.service.UserService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
;import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {

    private final JwtUtil jwtUtil;

    private final MerchantApplicationService merchantApplicationService;

    public UserServiceImpl(JwtUtil jwtUtil, MerchantApplicationService merchantApplicationService) {
        this.jwtUtil = jwtUtil;
        this.merchantApplicationService = merchantApplicationService;
    }

    @Override
    public List<Users> getUsersByIds(List<Long> userIds) {
        log.info("==================getUsersByIds======================");
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Users::getUserId, userIds);

        return list(queryWrapper); // 使用 MyBatis-Plus 的 list 方法来批量查询
    }

    //登陆
    @Override
    public R<String> login(HttpServletRequest request, Users user) {
        log.info("====================login==============================");
        log.info("请求登陆的user信息为{}",user);
        // 根据用户提交的邮箱查询数据库
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        Users users = this.getOne(queryWrapper);

        // 如果没有查询到则返回登陆失败结果
        if (users == null) {
            return R.error("不存在该邮箱");
        }

        // 使用 BCrypt 比对密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(user.getPassword(), users.getPassword())) {
            return R.error("密码错误");
        }

        // 登录成功，生成 JWT
        String token = jwtUtil.generateToken(users.getEmail(), users.getRole().name(), users.getUserId());
        log.info("token为:{}",token);
        // 将用户 ID 存入 Session（可选）
        request.getSession().setAttribute("UserId", users.getUserId());

        // 返回 JWT 给客户端
        return R.success(token);
    }

    //注册用户
    //先注册用户，然后调用创建购物车方法
    @Override
    @Transactional
    public R<Users> createUser(Users user) {
        log.info("====================createUser==============================");
        log.info("调用创建用户请求");
        log.info("user: {}", user);
        String password = user.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //加密
        password= passwordEncoder.encode(password);
        user.setPassword(password);
        this.save(user);
        return R.success(user);
    }

    //用户更新自己的信息
    @Override
    public R<String> updateUser(String token, Users user) {
        log.info("====================updateUser==============================");
        log.info("调用更新用户请求");
        log.info("user: {}", user);
        log.info("token: {}", token);
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        String userRole = jwtUtil.extractRole(token);
        if (!userId.equals(user.getUserId())&&!UserRoles.ADMIN.name().equals(userRole)){
            log.warn("用户ID {} 尝试修改非本人信息，当前角色: {}", userId, userRole);
            return R.error("无权限");
        }
        if (UserRoles.USER.name().equals(userRole) && (user.getRole() == null || !UserRoles.USER.equals(user.getRole()))) {
            log.warn("用户ID {} 尝试修改权限，当前角色: {}", userId, userRole);
            return R.error("请不要修改自己的权限");
        }
        log.info("更新用户:{}",user.toString());
        this.updateById(user);
        return R.success("更新用户成功");
    }


    //删除用户 or 用户注销账号
    //先删除购物车再删除用户
    @Override
    @Transactional
    public R<String> deleteUser(String token, Long userId) {
        log.info("====================deleteUser==============================");
        log.info("调用删除用户请求");
        log.info("token: {}", token);
        log.info("userId: {}", userId);
        Long userIdNow = jwtUtil.extractUserId(token);
        String userRole =jwtUtil.extractRole(token);
        if (!userRole.equals(UserRoles.ADMIN.name())&&!userIdNow.equals(userId)){
            return R.error("无权限");
        }
        log.info("删除用户:{}",userId);
        this.removeById(userId);
        return R.success("删除用户成功");
    }

    //用户申请成为商家
    @Override
    @Transactional
    public R<String> merchantApplication(String token, MerchantApplication merchantApplication) {
        log.info("====================merchantApplication==============================");
        log.info("调用商家申请请求");
        log.info("token: {}", token);
        log.info("merchantApplication: {}", merchantApplication);
        Long userId = jwtUtil.extractUserId(token);
        String userRole =jwtUtil.extractRole(token);
        if (!UserRoles.USER.name().equals(userRole)){
            return R.error("管理员或商家不能够申请成为商家");
        }
        merchantApplication.setStatus(MerchantApplicationsStatus.PENDING);
        merchantApplication.setUserId(userId);
        merchantApplicationService.save(merchantApplication);
        return R.success("申请成功");
    }


    //---------管理员方法---------
    //管理员查看所有用户请求
    @Override
    public R<Page> adminGetUsersPage(int page, int pageSize, String role) {
        log.info("====================adminGetUsersPage==============================");
        log.info("分页查询请求");
        log.info("page = {}, pageSize = {}, role = {}", page, pageSize, role);

        Page pageInfo = new Page(page, pageSize);
        log.info("查看 Users 表信息");

        // 构造查询条件
        LambdaQueryWrapper<Users> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(role)) {
            // 确保 role 与数据库中存储的格式一致
            try {
                String roleEnum = UserRoles.valueOf(role.toUpperCase()).name();
                lambdaQueryWrapper.like(Users::getRole, roleEnum);
            } catch (IllegalArgumentException e) {
                log.warn("传入的角色 {} 无效，将返回所有用户", role);
            }
        }

        lambdaQueryWrapper.orderByDesc(Users::getUpdatedAt);
        this.page(pageInfo, lambdaQueryWrapper);

        return R.success(pageInfo);
    }


}
