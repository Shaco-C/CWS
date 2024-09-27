package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.Merchants;
import com.watergun.mapper.MerchantsMapper;
import com.watergun.service.MerchantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantsMapper, Merchants> implements MerchantService {


}
