package com.watergun.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.BankAccounts;
import com.watergun.mapper.BankAccountsMapper;
import com.watergun.service.BankAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BankAccountsServiceImpl extends ServiceImpl<BankAccountsMapper, BankAccounts> implements BankAccountsService {
}
