package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.CartItems;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemsMapper extends BaseMapper<CartItems> {
}
