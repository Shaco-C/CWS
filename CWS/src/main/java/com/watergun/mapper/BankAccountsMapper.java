package com.watergun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.entity.BankAccounts;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankAccountsMapper extends BaseMapper<BankAccounts> {
}
