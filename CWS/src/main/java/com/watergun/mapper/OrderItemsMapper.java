package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.OrderItems;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemsMapper extends BaseMapper<OrderItems> {
}
