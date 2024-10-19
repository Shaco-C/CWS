package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.WithdrawalRecords;
import com.watergun.mapper.WithdrawalRecordsMapper;
import com.watergun.service.WithdrawalRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WithdrawalRecordsServiceImpl extends ServiceImpl<WithdrawalRecordsMapper, WithdrawalRecords> implements WithdrawalRecordsService {
}
