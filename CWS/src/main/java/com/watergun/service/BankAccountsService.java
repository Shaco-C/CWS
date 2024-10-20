package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.entity.BankAccounts;

public interface BankAccountsService extends IService<BankAccounts> {
    R<Page> getAllBankAccounts(String token, int page, int pageSize);
    R<String> addBankAccount(String token,BankAccounts bankAccounts);

    R<String> deleteBankAccount(String token,Long bankAccountId);
}
