package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.BankAccounts;
import com.watergun.service.BankAccountsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bankAccounts")
public class BankAccountsController {

    private final BankAccountsService bankAccountsService;

    public BankAccountsController(BankAccountsService bankAccountsService) {
        this.bankAccountsService = bankAccountsService;
    }

    //获取所有银行账户
    @GetMapping("/page")
    public R<Page> getAllBankAccounts(HttpServletRequest request,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){

        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return bankAccountsService.getAllBankAccounts(token,page,pageSize);
    }
    //添加
    @PostMapping("/addBankAccount")
    public R<String> addBankAccount(HttpServletRequest request,@RequestBody BankAccounts bankAccounts){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return bankAccountsService.addBankAccount(token,bankAccounts);
    }
    //删除
    @DeleteMapping("/deleteBankAccount/{bankAccountId}")
    public R<String> deleteBankAccount(HttpServletRequest request,@PathVariable Long bankAccountId){
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return bankAccountsService.deleteBankAccount(token,bankAccountId);
    }


}
