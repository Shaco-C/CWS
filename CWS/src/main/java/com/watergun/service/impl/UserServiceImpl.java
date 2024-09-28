package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.Users;
import com.watergun.mapper.UsersMapper;
import com.watergun.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
;import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {


    @Override
    public List<Users> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Users::getUserId, userIds);

        return list(queryWrapper); // 使用 MyBatis-Plus 的 list 方法来批量查询
    }

}
