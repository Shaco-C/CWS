package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Users;
import com.watergun.service.MerchantApplicationService;
import com.watergun.service.UserService;
import com.watergun.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
// 加密密码使用BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@RestController
@RequestMapping("/users")
@Slf4j
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantApplicationService merchantApplicationService;

    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping("/{id}")
    public R<Users> getUserById(@PathVariable Long id) {

        log.info("查询id为:"+id+"的用户");
        return R.success(userService.getById(id));
    }

    //注册用户
    @PostMapping
    public R<String> createUser(@RequestBody Users user) {
        log.info("创建用户:"+user.toString());
        String password = user.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 密码加密
        password = passwordEncoder.encode(password);
        user.setPassword(password);
        userService.save(user);
        return R.success("User created successfully");
    }

    @PutMapping
    public R<String> updateUser(@RequestBody Users user) {
        log.info("更新用户:"+user.toString());
        userService.updateById(user);
        return R.success("User updated successfully");
    }

    @DeleteMapping("/{id}")
    //需要为用户自己或者是管理员
    public R<String> deleteUser(HttpServletRequest request,@PathVariable Long id) {
        log.info("删除id为:"+id+"的用户");

        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("当前token为:{}",token);
        Long UserId =  jwtUtil.extractUserId(token);
        log.info("当前用户id为:{}",UserId);
        String UserRole = jwtUtil.extractRole(token);
        log.info("当前用户角色为:{}",UserRole);

        if (!UserRole.equals("admin") &&!UserId.equals(id)){
            return R.error("没有权限删除用户");
        }
        userService.removeById(id);
        return R.success("User deleted successfully");
    }


    //用户申请成为商家
    @PostMapping("/merchantApplication")
    public R<String> merchantApplication(HttpServletRequest request, @RequestBody MerchantApplication merchantApplication){
        log.info("用户申请成为商家:"+merchantApplication.toString());
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        Long UserId = jwtUtil.extractUserId(token);
        log.info("当前用户id为:{}",UserId);

        String userRole = jwtUtil.extractRole(token);
        log.info("当前用户角色为:{}",userRole);

        if (!userRole.equals("user")){
            return R.error("管理员或商家不能够申请成为商家");
        }

        merchantApplication.setStatus("pending");
        merchantApplication.setUserId(UserId);
        merchantApplicationService.save(merchantApplication);
        return R.success("申请提交成功");

    }

    @PostMapping("/login")
    public R<String> login(HttpServletRequest request, @RequestBody Users user) {
        log.info("请求登陆的user信息为",user);
        // 根据用户提交的邮箱查询数据库
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        Users users = userService.getOne(queryWrapper);

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
        String token = jwtUtil.generateToken(users.getEmail(), users.getRole(), users.getUserId());
        log.info("token为:{}",token);
        // 将用户 ID 存入 Session（可选）
        request.getSession().setAttribute("UserId", users.getUserId());

        // 返回 JWT 给客户端
        return R.success(token);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        log.info("退出登录");
        // 清除Session中存储的用户ID
        request.getSession().removeAttribute("UserId");

        // 这里可以考虑实现JWT黑名单机制，记录当前用户的Token，阻止其再次使用
        return R.success("Successfully logged out");
    }
}
