package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.Users;

import java.util.List;

public interface UserService extends IService<Users> {
    List<Users> getUsersByIds(List<Long> userIds); // 根据用户ID列表获取用户信息
}
