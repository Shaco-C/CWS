package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.MerchantApplication;
import com.watergun.entity.Users;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService extends IService<Users> {
    List<Users> getUsersByIds(List<Long> userIds); // 根据用户ID列表获取用户信息

    //登陆
    R<String> login(HttpServletRequest request, Users user);
    //创建用户
    R<Users> createUser(Users user);
    //更新用户信息
    R<String> updateUser(String token, Users user);
    //删除用户
    R<String> deleteUser(String token, Long userId);


    //---------管理员方法---------
    //管理员查看所有用户请求
    R<Page> adminGetUsersPage(int page, int pageSize, String role);

}
