package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.Favorites;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoritesMapper extends BaseMapper<Favorites> {
}
