package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.WalletBalanceLog;
import com.watergun.mapper.WalletBalanceLogMapper;
import com.watergun.service.WalletBalanceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletBalanceLogServiceImpl extends ServiceImpl<WalletBalanceLogMapper, WalletBalanceLog> implements WalletBalanceLogService {
}
