package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.UserAddress;
import com.watergun.mapper.UserAddressMapper;
import com.watergun.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
}
