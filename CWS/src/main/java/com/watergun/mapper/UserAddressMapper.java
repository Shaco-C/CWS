package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}