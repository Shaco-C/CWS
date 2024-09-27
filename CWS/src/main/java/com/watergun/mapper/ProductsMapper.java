package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.Products;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductsMapper extends BaseMapper<Products> {
}
