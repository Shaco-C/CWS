package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.MerchantApplication;
import com.watergun.mapper.MerchantApplicationMapper;
import com.watergun.service.MerchantApplicationService;
import org.springframework.stereotype.Service;

@Service
public class MerchantApplicationServiceImpl extends ServiceImpl<MerchantApplicationMapper, MerchantApplication> implements MerchantApplicationService {
}
