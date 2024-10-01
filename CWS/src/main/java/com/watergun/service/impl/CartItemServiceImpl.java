package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.CartItems;
import com.watergun.mapper.CartItemsMapper;
import com.watergun.service.CartItemService;
import org.springframework.stereotype.Service;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemsMapper, CartItems> implements CartItemService {
}
