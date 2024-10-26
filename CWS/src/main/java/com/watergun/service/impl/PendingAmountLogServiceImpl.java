package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.PendingAmountLog;
import com.watergun.mapper.PendingAmountLogMapper;
import com.watergun.service.PendingAmountLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PendingAmountLogServiceImpl extends ServiceImpl<PendingAmountLogMapper,PendingAmountLog> implements PendingAmountLogService {
}
