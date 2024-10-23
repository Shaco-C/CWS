package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Users;
import com.watergun.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@Slf4j
public class UsersController {


    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    //注册用户
    @PostMapping
    public R<Users> createUser(@RequestBody Users user) {
        return userService.createUser(user);
    }

    @PutMapping
    public R<String> updateUser(HttpServletRequest request, @RequestBody Users user) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return userService.updateUser(token,user);
    }

    @DeleteMapping("/{userId}")
    //需要为用户自己或者是管理员
    public R<String> deleteUser(HttpServletRequest request,@PathVariable Long userId) {
        log.info("删除id为:"+userId+"的用户");

        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return userService.deleteUser(token,userId);
    }


    //用户申请成为商家
    @PostMapping("/merchantApplication")
    public R<String> merchantApplication(HttpServletRequest request, @RequestBody MerchantApplication merchantApplication){
        log.info("用户申请成为商家:"+merchantApplication.toString());
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return userService.merchantApplication(token,merchantApplication);

    }

    @PostMapping("/login")
    public R<String> login(HttpServletRequest request, @RequestBody Users user) {
        return userService.login(request,user);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        log.info("退出登录");
        // 清除Session中存储的用户ID
        request.getSession().removeAttribute("UserId");

        // 这里可以考虑实现JWT黑名单机制，记录当前用户的Token，阻止其再次使用
        return R.success("Successfully logged out");
    }


    //---------管理员方法---------

    //管理员查看所有用户请求
    @GetMapping("/admin/getUserPage")
    public R<Page> adminGetUsersPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                     @RequestParam(value = "pageSize", defaultValue = "1") int pageSize,
                                     String role){
        log.info("管理员查看所有用户请求");
        return  userService.adminGetUsersPage(page,pageSize,role);
    }
}
