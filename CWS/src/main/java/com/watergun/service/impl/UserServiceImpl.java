package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.Users;
import com.watergun.mapper.UsersMapper;
import com.watergun.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
;
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UsersMapper, Users> implements UserService {


}
